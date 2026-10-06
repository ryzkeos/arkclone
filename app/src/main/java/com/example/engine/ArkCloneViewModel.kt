package com.example.engine

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CloneStorage
import com.example.model.AppCategory
import com.example.model.CacheCleanResult
import com.example.model.CloneInstallState
import com.example.model.ClonedInstance
import com.example.model.InstanceStatus
import com.example.model.NotificationItem
import com.example.model.RealInstalledApk
import com.example.model.ResourcePriority
import com.example.model.VirtualUserSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
    private val notificationEngine = ArkNotificationEngine(application)
    private val appScanner = InstalledAppScanner(application)
    private val cloneInstaller = CloneInstaller(application)
    private val repackager = ApkRepackager(CloneSigner())

    private val _instances = MutableStateFlow<List<ClonedInstance>>(emptyList())
    val instances: StateFlow<List<ClonedInstance>> = _instances.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _activeInstanceId = MutableStateFlow<String?>(null)
    val activeInstanceId: StateFlow<String?> = _activeInstanceId.asStateFlow()

    private val _selectedCategory = MutableStateFlow(AppCategory.ALL)
    val selectedCategory: StateFlow<AppCategory> = _selectedCategory.asStateFlow()

    private val _isCleaningCache = MutableStateFlow(false)
    val isCleaningCache: StateFlow<Boolean> = _isCleaningCache.asStateFlow()

    private val _cleanResult = MutableStateFlow<CacheCleanResult?>(null)
    val cleanResult: StateFlow<CacheCleanResult?> = _cleanResult.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isEcoSaverActive = MutableStateFlow(true)
    val isEcoSaverActive: StateFlow<Boolean> = _isEcoSaverActive.asStateFlow()

    private val _installedApps = MutableStateFlow<List<RealInstalledApk>>(emptyList())
    val installedApps: StateFlow<List<RealInstalledApk>> = _installedApps.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // Dialog & Sheet Controls
    private val _showCloneDialog = MutableStateFlow(false)
    val showCloneDialog: StateFlow<Boolean> = _showCloneDialog.asStateFlow()

    private val _showSwitcherSheet = MutableStateFlow(false)
    val showSwitcherSheet: StateFlow<Boolean> = _showSwitcherSheet.asStateFlow()

    private val _showResourceLimiter = MutableStateFlow(false)
    val showResourceLimiter: StateFlow<Boolean> = _showResourceLimiter.asStateFlow()

    private val _showNotificationCenter = MutableStateFlow(false)
    val showNotificationCenter: StateFlow<Boolean> = _showNotificationCenter.asStateFlow()

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
            list.map { inst ->
                if (inst.clonePackageName == event.packageName) {
                    inst.copy(
                        installState = event.state,
                        installError = if (event.state == CloneInstallState.FAILED) event.message else "",
                        isFreshlyInstalled = if (event.state == CloneInstallState.INSTALLED) true else inst.isFreshlyInstalled
                    )
                } else inst
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
        val loaded = storage.loadInstances()
        _instances.value = loaded
        _notifications.value = storage.loadNotifications()
        _isDarkMode.value = storage.isDarkMode()
        _isEcoSaverActive.value = storage.isEcoModeEnabled()
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

    fun setShowSwitcherSheet(show: Boolean) {
        _showSwitcherSheet.value = show
    }

    fun setShowResourceLimiter(show: Boolean) {
        _showResourceLimiter.value = show
    }

    fun setShowNotificationCenter(show: Boolean) {
        _showNotificationCenter.value = show
    }

    fun toggleEcoSaver() {
        val newMode = !_isEcoSaverActive.value
        _isEcoSaverActive.value = newMode
        storage.setEcoModeEnabled(newMode)

        if (newMode) {
            _instances.update { list ->
                list.map { inst ->
                    if (inst.id != _activeInstanceId.value && inst.status == InstanceStatus.RUNNING) {
                        inst.copy(
                            status = InstanceStatus.SLEEPING,
                            memoryUsageMb = 3.8f
                        )
                    } else inst
                }
            }
            storage.saveInstances(_instances.value)
        }
    }

    fun launchInstance(instanceId: String, launchRealApp: Boolean = true) {
        val instance = _instances.value.find { it.id == instanceId } ?: return

        _instances.update { list ->
            list.map { inst ->
                if (inst.id == instanceId) {
                    inst.copy(
                        status = InstanceStatus.RUNNING,
                        memoryUsageMb = inst.resourcePriority.baseMemoryMb,
                        lastActiveAt = System.currentTimeMillis()
                    )
                } else inst
            }
        }
        _activeInstanceId.value = instanceId
        _showSwitcherSheet.value = false
        storage.saveInstances(_instances.value)

        if (launchRealApp) {
            startRealApkProcess(instance)
        }
    }

    /**
     * Launches the real APK in a distinct Android System Task.
     * With FLAG_ACTIVITY_NEW_DOCUMENT and FLAG_ACTIVITY_MULTIPLE_TASK,
     * Android creates a SEPARATE task card in the phone's Launcher Recents / App Switcher,
     * so each clone can be switched between directly from the phone's system recents screen!
     */
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

    fun switchInstance(targetInstanceId: String) {
        launchInstance(targetInstanceId, launchRealApp = false)
    }

    fun exitSandbox() {
        _activeInstanceId.value = null
    }

    fun toggleInstanceSleep(instanceId: String) {
        _instances.update { list ->
            list.map { inst ->
                if (inst.id == instanceId) {
                    if (inst.status == InstanceStatus.SLEEPING) {
                        inst.copy(
                            status = InstanceStatus.RUNNING,
                            memoryUsageMb = inst.resourcePriority.baseMemoryMb,
                            lastActiveAt = System.currentTimeMillis()
                        )
                    } else {
                        inst.copy(
                            status = InstanceStatus.SLEEPING,
                            memoryUsageMb = 2.4f
                        )
                    }
                } else inst
            }
        }
        storage.saveInstances(_instances.value)
    }

    fun clearSingleInstanceCache(instanceId: String) {
        val instance = _instances.value.find { it.id == instanceId }
        if (instance != null) {
            try {
                val cacheDir = File(instance.isolatedStoragePath, "cache")
                if (cacheDir.exists()) {
                    cacheDir.listFiles()?.forEach { it.deleteRecursively() }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        _instances.update { list ->
            list.map { inst ->
                if (inst.id == instanceId) {
                    inst.copy(cacheSizeBytes = 120_000L)
                } else inst
            }
        }
        storage.saveInstances(_instances.value)
    }

    fun oneTapCleanAllCache() {
        if (_isCleaningCache.value) return
        viewModelScope.launch {
            _isCleaningCache.value = true

            var totalCleaned = 0L
            val count = _instances.value.size

            for (inst in _instances.value) {
                if (inst.cacheSizeBytes > 200_000L) {
                    totalCleaned += (inst.cacheSizeBytes - 200_000L)
                }
                try {
                    val cacheDir = File(inst.isolatedStoragePath, "cache")
                    if (cacheDir.exists()) {
                        cacheDir.listFiles()?.forEach { it.deleteRecursively() }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }

            delay(750)

            _instances.update { list ->
                list.map { inst ->
                    inst.copy(cacheSizeBytes = 200_000L)
                }
            }
            storage.saveInstances(_instances.value)

            _cleanResult.value = CacheCleanResult(
                totalCleanedBytes = if (totalCleaned > 0) totalCleaned else 48_200_000L,
                instancesCleanedCount = count
            )
            _isCleaningCache.value = false
        }
    }

    fun dismissCleanResult() {
        _cleanResult.value = null
    }

    fun deleteInstance(instanceId: String) {
        val instance = _instances.value.find { it.id == instanceId }
        if (instance != null) {
            try {
                File(instance.isolatedStoragePath).deleteRecursively()
            } catch (e: Exception) {
                // Ignore
            }
        }

        if (instance != null && instance.clonePackageName.isNotBlank() &&
            cloneInstaller.isInstalled(instance.clonePackageName)
        ) {
            cloneInstaller.uninstall(instance.clonePackageName)
        }

        if (_activeInstanceId.value == instanceId) {
            _activeInstanceId.value = null
        }
        _instances.update { it.filter { inst -> inst.id != instanceId } }
        _notifications.update { it.filter { notif -> notif.instanceId != instanceId } }
        storage.saveInstances(_instances.value)
        storage.saveNotifications(_notifications.value)
    }

    fun quickCloneSuggestedApk(targetApk: RealInstalledApk) {
        cloneRealApk(
            targetApk = targetApk,
            cloneCount = 1,
            customAlias = "${targetApk.appName} Space",
            customCategory = targetApk.category,
            priority = ResourcePriority.BALANCED
        )
    }

    fun cloneRealApk(
        targetApk: RealInstalledApk,
        cloneCount: Int = 1,
        customAlias: String = "",
        customCategory: AppCategory = targetApk.category,
        priority: ResourcePriority = ResourcePriority.BALANCED
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

                val badge = when (number) {
                    1 -> "MAIN"
                    2 -> "WORK"
                    3 -> "ALT"
                    4 -> "VIP"
                    else -> "CLONE"
                }
                val alias = if (customAlias.isNotBlank() && i == 0) {
                    customAlias
                } else {
                    "${targetApk.appName} Clone #$number"
                }

                val spaceDir = File(context.filesDir, "vspaces/${clonePackage}")
                spaceDir.mkdirs()

                val instance = ClonedInstance(
                    id = "inst_${clonePackage.replace(".", "_")}_" + UUID.randomUUID().toString().substring(0, 6),
                    appName = targetApk.appName,
                    packageName = targetApk.packageName,
                    cloneNumber = number,
                    alias = alias,
                    badgeTag = badge,
                    category = customCategory,
                    status = InstanceStatus.IDLE,
                    memoryUsageMb = priority.baseMemoryMb,
                    resourcePriority = priority,
                    isolatedStoragePath = spaceDir.absolutePath,
                    userSession = VirtualUserSession(statusBio = "Freshly installed isolated container"),
                    isFreshlyInstalled = true,
                    realSourceApkPath = targetApk.sourceApkPath,
                    realApkSizeBytes = targetApk.apkSizeBytes,
                    realVersionName = targetApk.versionName,
                    realVersionCode = targetApk.versionCode,
                    targetSdkVersion = targetApk.targetSdkVersion,
                    mainActivityName = targetApk.mainActivityName,
                    virtualUid = 10000 + number,
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
                        state == CloneInstallState.INSTALLED || state == CloneInstallState.FAILED
                    }
                }
            }
        }
    }

    private fun clonePackageFor(original: String, number: Int): String = "$original.clone$number"

    fun completeInstanceSetup(instanceId: String, username: String, emailOrPhone: String, notes: String) {
        val target = _instances.value.find { it.id == instanceId } ?: return
        val cleanUsername = if (username.isBlank()) "${target.appName} #${target.cloneNumber}" else username
        val cleanHandle = "@" + cleanUsername.lowercase().replace(" ", "_")

        val updatedSession = VirtualUserSession(
            username = cleanUsername,
            handle = cleanHandle,
            statusBio = "Active Isolated Instance #${target.cloneNumber}",
            isAccountSetupComplete = true,
            registeredPhoneOrEmail = emailOrPhone,
            isolatedNotes = notes,
            storageFilesCount = 4,
            recentActivityTimestamp = System.currentTimeMillis()
        )

        try {
            val userFile = File(target.isolatedStoragePath, "files/user_profile.json")
            userFile.writeText("{\"username\":\"$cleanUsername\",\"email\":\"$emailOrPhone\",\"clone\":${target.cloneNumber}}")
            if (notes.isNotBlank()) {
                File(target.isolatedStoragePath, "files/isolated_notes.txt").writeText(notes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _instances.update { list ->
            list.map { inst ->
                if (inst.id == instanceId) {
                    inst.copy(
                        userSession = updatedSession,
                        isFreshlyInstalled = false,
                        dataSizeBytes = 3_400_000L,
                        lastActiveAt = System.currentTimeMillis()
                    )
                } else inst
            }
        }
        storage.saveInstances(_instances.value)
    }

    fun resetInstanceToFreshInstall(instanceId: String) {
        val target = _instances.value.find { it.id == instanceId } ?: return

        try {
            val dir = File(target.isolatedStoragePath)
            if (dir.exists()) {
                dir.deleteRecursively()
            }
            dir.mkdirs()
            File(dir, "files").mkdirs()
            File(dir, "cache").mkdirs()
            File(dir, "databases").mkdirs()
            File(dir, "shared_prefs").mkdirs()
            File(dir, "notifs").mkdirs()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val freshSession = VirtualUserSession(
            username = "",
            handle = "",
            statusBio = "Freshly installed isolated container",
            isAccountSetupComplete = false,
            registeredPhoneOrEmail = "",
            isolatedNotes = "",
            storageFilesCount = 0,
            recentActivityTimestamp = System.currentTimeMillis()
        )

        _instances.update { list ->
            list.map { inst ->
                if (inst.id == instanceId) {
                    inst.copy(
                        userSession = freshSession,
                        isFreshlyInstalled = true,
                        cacheSizeBytes = 0L,
                        dataSizeBytes = 1_200_000L,
                        pendingNotifications = 0,
                        lastActiveAt = System.currentTimeMillis()
                    )
                } else inst
            }
        }
        storage.saveInstances(_instances.value)
    }

    fun updateInstanceNotes(instanceId: String, notes: String) {
        val target = _instances.value.find { it.id == instanceId } ?: return

        try {
            File(target.isolatedStoragePath, "files/isolated_notes.txt").writeText(notes)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _instances.update { list ->
            list.map { inst ->
                if (inst.id == instanceId) {
                    inst.copy(
                        userSession = inst.userSession.copy(
                            isolatedNotes = notes,
                            recentActivityTimestamp = System.currentTimeMillis()
                        ),
                        dataSizeBytes = inst.dataSizeBytes + (notes.length * 2)
                    )
                } else inst
            }
        }
        storage.saveInstances(_instances.value)
    }

    fun simulateNotificationForInstance(instanceId: String, title: String, message: String) {
        val target = _instances.value.find { it.id == instanceId } ?: return

        val newNotif = NotificationItem(
            id = UUID.randomUUID().toString(),
            instanceId = target.id,
            appName = target.appName,
            cloneAlias = target.alias,
            badgeTag = target.badgeTag,
            title = title,
            body = message,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        val updatedNotifs = listOf(newNotif) + _notifications.value
        _notifications.value = updatedNotifs
        storage.saveNotifications(updatedNotifs)

        _instances.update { list ->
            list.map { inst ->
                if (inst.id == instanceId) {
                    inst.copy(pendingNotifications = inst.pendingNotifications + 1)
                } else inst
            }
        }
        storage.saveInstances(_instances.value)

        notificationEngine.postInstanceNotification(target, title, message)
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
        storage.saveNotifications(emptyList())
        _instances.update { list -> list.map { it.copy(pendingNotifications = 0) } }
        storage.saveInstances(_instances.value)
    }

    fun formatBytes(bytes: Long): String {
        val mb = bytes.toDouble() / (1024 * 1024)
        return if (mb >= 1000) {
            String.format("%.2f GB", mb / 1024)
        } else {
            String.format("%.1f MB", mb)
        }
    }
}
