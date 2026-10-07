package com.arkeos.arkclone.engine

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arkeos.arkclone.data.CloneStorage
import com.arkeos.arkclone.model.AppCategory
import com.arkeos.arkclone.model.ArkToast
import com.arkeos.arkclone.model.CloneInstallState
import com.arkeos.arkclone.model.ClonedInstance
import com.arkeos.arkclone.model.RealInstalledApk
import com.arkeos.arkclone.model.ToastType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class ArkCloneViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = CloneStorage(application)
    private val appScanner = InstalledAppScanner(application)
    private val cloneInstaller = CloneInstaller(application)
    private val repackager = ApkRepackager(CloneSigner())

    private val _instances = MutableStateFlow<List<ClonedInstance>>(emptyList())
    val instances: StateFlow<List<ClonedInstance>> = _instances.asStateFlow()

    private val _activeInstanceId = MutableStateFlow<String?>(null)
    val activeInstanceId: StateFlow<String?> = _activeInstanceId.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)
    val selectedCategory: StateFlow<AppCategory> = _selectedCategory.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _installedApps = MutableStateFlow<List<RealInstalledApk>>(emptyList())
    val installedApps: StateFlow<List<RealInstalledApk>> = _installedApps.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Dialog & Sheet Controls
    private val _showCloneDialog = MutableStateFlow(false)
    val showCloneDialog: StateFlow<Boolean> = _showCloneDialog.asStateFlow()

    private val _cloneDialogTarget = MutableStateFlow<String?>(null)
    val cloneDialogTarget: StateFlow<String?> = _cloneDialogTarget.asStateFlow()

    private val _toast = MutableStateFlow<ArkToast?>(null)
    val toast: StateFlow<ArkToast?> = _toast.asStateFlow()


    init {
        loadData()
        scanInstalledApks()
        viewModelScope.launch {
            CloneInstallEvents.flow.collect { event -> applyInstallEvent(event) }
        }
    }

    private fun applyInstallEvent(event: CloneInstallEvent) {
        val target = _instances.value.find { it.clonePackageName == event.packageName }
        val name = target?.alias ?: event.packageName
        _instances.update { list ->
            if (event.state == CloneInstallState.FAILED) {
                list.filterNot { it.clonePackageName == event.packageName }
            } else {
                list.map { inst ->
                    if (inst.clonePackageName == event.packageName) {
                        inst.copy(installState = event.state, installError = "")
                    } else inst
                }
            }
        }
        storage.saveInstances(_instances.value)
        if (target == null) return
        when (event.state) {
            CloneInstallState.INSTALLED -> showToast("$name installed", ToastType.SUCCESS)
            CloneInstallState.AWAITING_USER -> showToast("Confirm the install to finish $name", ToastType.INFO)
            CloneInstallState.FAILED -> {
                val cancelled = event.message.isBlank() || event.message.contains("ABORTED", ignoreCase = true)
                if (cancelled) {
                    showToast("$name install cancelled", ToastType.ERROR)
                } else {
                    showToast("$name install failed: ${event.message}", ToastType.ERROR)
                }
            }
            else -> Unit
        }
    }

    fun refreshInstallStates() {
        val tracked = _instances.value.map { it.clonePackageName }.filter { it.isNotBlank() }.toSet()
        if (tracked.isEmpty()) return
        viewModelScope.launch {
            val installedPackages = withContext(Dispatchers.IO) {
                tracked.filter { cloneInstaller.isInstalled(it) }.toSet()
            }
            _instances.update { list ->
                list.map { inst ->
                    if (inst.clonePackageName !in tracked) return@map inst
                    val installed = inst.clonePackageName in installedPackages
                    when {
                        installed && inst.installState != CloneInstallState.INSTALLED ->
                            inst.copy(installState = CloneInstallState.INSTALLED, installError = "")
                        !installed && inst.installState == CloneInstallState.INSTALLED ->
                            inst.copy(installState = CloneInstallState.NOT_INSTALLED)
                        else -> inst
                    }
                }
            }
            storage.saveInstances(_instances.value)
        }
    }

    private fun showToast(message: String, type: ToastType = ToastType.INFO) {
        _toast.value = ArkToast(id = System.nanoTime(), message = message, type = type)
    }

    fun dismissToast(id: Long) {
        _toast.update { current -> if (current?.id == id) null else current }
    }

    private fun loadData() {
        val loaded = storage.loadInstances().filter { inst ->
            when (inst.installState) {
                CloneInstallState.FAILED, CloneInstallState.PREPARING, CloneInstallState.AWAITING_USER ->
                    inst.clonePackageName.isNotBlank() && cloneInstaller.isInstalled(inst.clonePackageName)
                else -> true
            }
        }
        _instances.value = loaded
        storage.saveInstances(loaded)
        _isDarkMode.value = storage.isDarkMode()
        _isGridView.value = storage.isGridView()
    }

    fun toggleDarkMode() {
        val nextMode = !_isDarkMode.value
        _isDarkMode.value = nextMode
        storage.setDarkMode(nextMode)
    }

    fun toggleGridView() {
        val nextMode = !_isGridView.value
        _isGridView.value = nextMode
        storage.setGridView(nextMode)
    }

    fun scanInstalledApks() {
        if (_isScanning.value) return
        _isScanning.value = true
        viewModelScope.launch {
            try {
                val apps = withContext(Dispatchers.Default) { appScanner.scanInstalledApps() }
                _installedApps.value = apps
                prefetchIcons(apps)
            } finally {
                _isScanning.value = false
            }
        }
    }

    private fun prefetchIcons(apps: List<RealInstalledApk>) {
        val context = getApplication<Application>()
        viewModelScope.launch(Dispatchers.IO) {
            apps.take(ICON_PREFETCH_COUNT).forEach { AppIconCache.getAppIcon(context, it.packageName) }
        }
    }

    fun setCategoryFilter(category: AppCategory) {
        _selectedCategory.value = category
    }

    fun setShowCloneDialog(show: Boolean) {
        _showCloneDialog.value = show
        if (!show) _cloneDialogTarget.value = null
    }

    fun openCloneDialog(packageName: String? = null) {
        _cloneDialogTarget.value = packageName
        _showCloneDialog.value = true
    }

    fun openDetails(instanceId: String) {
        _activeInstanceId.value = instanceId
    }

    fun launchInstance(instanceId: String) {
        val instance = _instances.value.find { it.id == instanceId } ?: return
        _instances.update { list ->
            list.map { inst ->
                if (inst.id == instanceId) inst.copy(lastActiveAt = System.currentTimeMillis()) else inst
            }
        }
        storage.saveInstances(_instances.value)
        startRealApkProcess(instance)
    }

    fun startRealApkProcess(instance: ClonedInstance): Boolean {
        val context = getApplication<Application>()
        val pm = context.packageManager

        return try {
            if (instance.clonePackageName.isBlank() || instance.installState != CloneInstallState.INSTALLED) {
                showToast("This clone is not installed yet", ToastType.ERROR)
                return false
            }
            val intent = pm.getLaunchIntentForPackage(instance.clonePackageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                showToast("${instance.alias} can't be launched", ToastType.ERROR)
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("${instance.alias} can't be launched", ToastType.ERROR)
            false
        }
    }

    fun openRealAppSettings(packageName: String) {
        val context = getApplication<Application>()
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeDetails() {
        _activeInstanceId.value = null
    }

    fun deleteInstance(instanceId: String) {
        val instance = _instances.value.find { it.id == instanceId }

        if (instance != null && instance.clonePackageName.isNotBlank() &&
            cloneInstaller.isInstalled(instance.clonePackageName)
        ) {
            cloneInstaller.uninstall(instance.clonePackageName)
        }

        if (_activeInstanceId.value == instanceId) {
            _activeInstanceId.value = null
        }
        _instances.update { it.filter { inst -> inst.id != instanceId } }
        storage.saveInstances(_instances.value)
    }

    fun cloneRealApk(
        targetApk: RealInstalledApk,
        cloneCount: Int = 1,
        aliases: List<String> = emptyList(),
        customCategory: AppCategory = targetApk.category
    ) {
        if (!cloneInstaller.canInstall()) {
            showToast("Allow Ark Clone to install apps, then try again", ToastType.INFO)
            cloneInstaller.openInstallPermissionSettings()
            return
        }
        _showCloneDialog.value = false

        viewModelScope.launch {
            val context = getApplication<Application>()

            for (i in 0 until cloneCount) {
                val used = _instances.value
                    .filter { it.packageName == targetApk.packageName }
                    .map { it.cloneNumber }
                    .toSet()
                var number = 1
                while (number in used || cloneInstaller.isInstalled(clonePackageFor(targetApk.packageName, number))) {
                    number++
                }
                val clonePackage = clonePackageFor(targetApk.packageName, number)

                val alias = aliases.getOrNull(i)?.trim().orEmpty()
                    .ifBlank { "${targetApk.appName} Clone #$number" }

                val instance = ClonedInstance(
                    id = "inst_${clonePackage.replace(".", "_")}_" + UUID.randomUUID().toString().substring(0, 6),
                    appName = targetApk.appName,
                    packageName = targetApk.packageName,
                    cloneNumber = number,
                    alias = alias,
                    category = customCategory,
                    realSourceApkPath = targetApk.sourceApkPath,
                    realApkSizeBytes = targetApk.apkSizeBytes,
                    realVersionName = targetApk.versionName,
                    realVersionCode = targetApk.versionCode,
                    targetSdkVersion = targetApk.targetSdkVersion,
                    mainActivityName = targetApk.mainActivityName,
                    clonePackageName = clonePackage,
                    installState = CloneInstallState.PREPARING
                )
                _instances.update { it + instance }
                showToast("Preparing $alias", ToastType.INFO)
                storage.saveInstances(_instances.value)

                val committed = withContext(Dispatchers.IO) {
                    val workDir = File(context.cacheDir, "clone_work/$clonePackage")
                    try {
                        val sources = (listOf(targetApk.sourceApkPath) + targetApk.splitSourcePaths)
                            .map { File(it) }
                        val apks = repackager.repackage(
                            sourceApks = sources,
                            newPackage = clonePackage,
                            cloneSuffix = "clone$number",
                            label = alias,
                            workDir = workDir
                        )
                        cloneInstaller.install(clonePackage, apks)
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        CloneInstallEvents.emit(
                            CloneInstallEvent(
                                clonePackage,
                                CloneInstallState.FAILED,
                                e.message ?: e.javaClass.simpleName
                            )
                        )
                        false
                    } finally {
                        workDir.deleteRecursively()
                    }
                }

                if (committed) {
                    _instances.first { list ->
                        val state = list.find { it.id == instance.id }?.installState
                        state == null || state == CloneInstallState.INSTALLED
                    }
                }
            }
        }
    }

    private companion object {
        const val ICON_PREFETCH_COUNT = 24
    }

    private fun clonePackageFor(original: String, number: Int): String = "$original.clone$number"

    fun formatBytes(bytes: Long): String {
        val mb = bytes.toDouble() / (1024 * 1024)
        return if (mb >= 1000) {
            String.format("%.2f GB", mb / 1024)
        } else {
            String.format("%.1f MB", mb)
        }
    }
}
