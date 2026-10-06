package com.arkeos.arkclone.model

enum class InstanceStatus(val label: String) {
    RUNNING("Active"),
    SLEEPING("Sleeping"),
    IDLE("Idle"),
    FROZEN("Frozen")
}

enum class CloneInstallState(val label: String) {
    PREPARING("Preparing"),
    AWAITING_USER("Waiting for confirmation"),
    INSTALLED("Installed"),
    FAILED("Failed"),
    NOT_INSTALLED("Not installed")
}

enum class ResourcePriority(val label: String, val baseMemoryMb: Float) {
    LOW_POWER("Eco Saver", 14.5f),
    BALANCED("Balanced", 26.0f),
    HIGH_PERFORMANCE("Full Speed", 42.0f)
}

enum class AppCategory(val title: String) {
    ALL("All"),
    SOCIAL("Social"),
    MESSAGING("Messaging"),
    TOOLS("Tools"),
    GAMES("Games")
}

data class IsolatedMessageItem(
    val id: String,
    val sender: String,
    val text: String,
    val timestamp: String,
    val isFromUser: Boolean
)

data class VirtualUserSession(
    val username: String = "",
    val handle: String = "",
    val statusBio: String = "",
    val isAccountSetupComplete: Boolean = false,
    val registeredPhoneOrEmail: String = "",
    val isolatedNotes: String = "",
    val storageFilesCount: Int = 0,
    val recentActivityTimestamp: Long = System.currentTimeMillis()
)

data class ClonedInstance(
    val id: String,
    val appName: String,
    val packageName: String,
    val cloneNumber: Int,
    val alias: String,
    val badgeTag: String, // e.g. WORK, PERSONAL, ALT, VIP, GAME
    val category: AppCategory,
    val status: InstanceStatus = InstanceStatus.IDLE,
    val memoryUsageMb: Float = 14.0f,
    val cacheSizeBytes: Long = 0L, // Fresh install starts with 0 cache
    val dataSizeBytes: Long = 1_200_000L, // Initial clean sandbox baseline
    val notificationsEnabled: Boolean = true,
    val pendingNotifications: Int = 0,
    val resourcePriority: ResourcePriority = ResourcePriority.BALANCED,
    val isolatedStoragePath: String,
    val userSession: VirtualUserSession = VirtualUserSession(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val isFreshlyInstalled: Boolean = true,
    // Installed APK metadata
    val realSourceApkPath: String = "",
    val realApkSizeBytes: Long = 0L,
    val realVersionName: String = "1.0",
    val realVersionCode: Long = 1L,
    val isRealApkInstalled: Boolean = true,
    val targetSdkVersion: Int = 34,
    val mainActivityName: String = "",
    val virtualUid: Int = 10001,
    val clonePackageName: String = "",
    val installState: CloneInstallState = CloneInstallState.NOT_INSTALLED,
    val installError: String = ""
)

data class RealInstalledApk(
    val appName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val sourceApkPath: String,
    val apkSizeBytes: Long,
    val isSystemApp: Boolean,
    val targetSdkVersion: Int,
    val category: AppCategory,
    val defaultBadge: String,
    val mainActivityName: String = "",
    val hasLaunchIntent: Boolean = true,
    val splitSourcePaths: List<String> = emptyList()
)

data class NotificationItem(
    val id: String,
    val instanceId: String,
    val appName: String,
    val cloneAlias: String,
    val badgeTag: String,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class CacheCleanResult(
    val totalCleanedBytes: Long,
    val instancesCleanedCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
