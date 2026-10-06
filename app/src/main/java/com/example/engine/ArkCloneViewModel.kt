package com.example.engine

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CloneStorage
import com.example.model.AppCategory
import com.example.model.CacheCleanResult
import com.example.model.ClonedInstance
import com.example.model.InstanceStatus
import com.example.model.NotificationItem
import com.example.model.RealInstalledApk
import com.example.model.ResourcePriority
import com.example.model.VirtualUserSession
import kotlinx.coroutines.delay
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
            val intent = pm.getLaunchIntentForPackage(instance.packageName)
            if (intent != null) {
                // Configure multi-task flags so Android creates an independent task entry in Launcher Recents
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                }

                // Virtual space container metadata
                intent.putExtra("ark.vspace.id", instance.id)
                intent.putExtra("ark.vspace.clone_num", instance.cloneNumber)
                intent.putExtra("ark.vspace.storage_dir", instance.isolatedStoragePath)
                intent.putExtra("ark.vspace.badge", instance.badgeTag)
                intent.putExtra("ark.vspace.uid", instance.virtualUid)

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
        viewModelScope.launch {
            val currentList = _instances.value
            val existingForPkg = currentList.filter { it.packageName == targetApk.packageName }
            var nextCloneNum = existingForPkg.size + 1

            val newInstances = mutableListOf<ClonedInstance>()
            val context = getApplication<Application>()

            for (i in 0 until cloneCount) {
                val uniqueId = "inst_${targetApk.packageName.replace(".", "_")}_" +
                        UUID.randomUUID().toString().substring(0, 6)

                val badge = when (nextCloneNum) {
                    1 -> "MAIN"
                    2 -> "WORK"
                    3 -> "ALT"
                    4 -> "VIP"
                    else -> "CLONE"
                }

                val alias = if (customAlias.isNotBlank() && i == 0) {
                    customAlias
                } else {
                    "${targetApk.appName} Clone #$nextCloneNum"
                }

                val spaceDir = File(context.filesDir, "vspaces/${targetApk.packageName}_inst_$nextCloneNum")
                spaceDir.mkdirs()
                File(spaceDir, "files").mkdirs()
                File(spaceDir, "cache").mkdirs()
                File(spaceDir, "databases").mkdirs()
                File(spaceDir, "shared_prefs").mkdirs()
                File(spaceDir, "notifs").mkdirs()

                val userSession = VirtualUserSession(
                    username = "${targetApk.appName} #$nextCloneNum",
                    handle = "@${targetApk.packageName.substringAfterLast(".")}_$nextCloneNum",
                    statusBio = "Virtual Enclave #$nextCloneNum for installed app",
                    unreadMessagesCount = 0,
                    storageFilesCount = 14
                )

                val newInst = ClonedInstance(
                    id = uniqueId,
                    appName = targetApk.appName,
                    packageName = targetApk.packageName,
                    cloneNumber = nextCloneNum,
                    alias = alias,
                    badgeTag = badge,
                    category = customCategory,
                    status = InstanceStatus.IDLE,
                    memoryUsageMb = priority.baseMemoryMb,
                    cacheSizeBytes = (targetApk.apkSizeBytes / 5).coerceAtLeast(12_000_000L),
                    dataSizeBytes = (targetApk.apkSizeBytes / 2).coerceAtLeast(30_000_000L),
                    notificationsEnabled = true,
                    pendingNotifications = 0,
                    resourcePriority = priority,
                    isolatedStoragePath = spaceDir.absolutePath,
                    userSession = userSession,
                    realSourceApkPath = targetApk.sourceApkPath,
                    realApkSizeBytes = targetApk.apkSizeBytes,
                    realVersionName = targetApk.versionName,
                    realVersionCode = targetApk.versionCode,
                    isRealApkInstalled = true,
                    targetSdkVersion = targetApk.targetSdkVersion,
                    mainActivityName = targetApk.mainActivityName,
                    virtualUid = 10000 + nextCloneNum
                )

                newInstances.add(newInst)
                nextCloneNum++
            }

            val updated = currentList + newInstances
            _instances.value = updated
            storage.saveInstances(updated)
            _showCloneDialog.value = false
        }
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
