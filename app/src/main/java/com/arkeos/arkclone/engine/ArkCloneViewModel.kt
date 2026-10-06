package com.arkeos.arkclone.engine

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arkeos.arkclone.data.CloneStorage
import com.arkeos.arkclone.model.AppCategory
import com.arkeos.arkclone.model.CloneInstallState
import com.arkeos.arkclone.model.ClonedInstance
import com.arkeos.arkclone.model.RealInstalledApk
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

    private val _installedApps = MutableStateFlow<List<RealInstalledApk>>(emptyList())
    val installedApps: StateFlow<List<RealInstalledApk>> = _installedApps.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Dialog & Sheet Controls
    private val _showCloneDialog = MutableStateFlow(false)
    val showCloneDialog: StateFlow<Boolean> = _showCloneDialog.asStateFlow()


    init {
        loadData()
        scanInstalledApks()
        refreshInstallStates()
        viewModelScope.launch {
            CloneInstallEvents.flow.collect { event -> applyInstallEvent(event) }
        }
    }

    private fun applyInstallEvent(event: CloneInstallEvent) {
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
        if (event.state == CloneInstallState.FAILED) {
            toast("Clone install failed: ${event.message.ifBlank { "cancelled or rejected" }}")
        }
    }

    fun refreshInstallStates() {
        _instances.update { list ->
            list.map { inst ->
                if (inst.clonePackageName.isBlank()) return@map inst
                val installed = cloneInstaller.isInstalled(inst.clonePackageName)
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

    private fun toast(message: String) {
        Toast.makeText(getApplication<Application>(), message, Toast.LENGTH_LONG).show()
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
    }

    fun toggleDarkMode() {
        val nextMode = !_isDarkMode.value
        _isDarkMode.value = nextMode
        storage.setDarkMode(nextMode)
    }

    fun scanInstalledApks() {
        viewModelScope.launch {
            _isScanning.value = true
            val apps = appScanner.scanInstalledApps()
            _installedApps.value = apps
            _isScanning.value = false
        }
    }

    fun setCategoryFilter(category: AppCategory) {
        _selectedCategory.value = category
    }

    fun setShowCloneDialog(show: Boolean) {
        _showCloneDialog.value = show
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
                toast("This clone is not installed yet")
                return false
            }
            val intent = pm.getLaunchIntentForPackage(instance.clonePackageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun quickCloneSuggestedApk(targetApk: RealInstalledApk) {
        cloneRealApk(
            targetApk = targetApk,
            cloneCount = 1,
            customCategory = targetApk.category
        )
    }

    fun cloneRealApk(
        targetApk: RealInstalledApk,
        cloneCount: Int = 1,
        aliases: List<String> = emptyList(),
        customCategory: AppCategory = targetApk.category
    ) {
        if (!cloneInstaller.canInstall()) {
            toast("Allow Ark Clone to install apps, then try again")
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
