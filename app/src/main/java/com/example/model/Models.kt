package com.example.model

enum class InstanceStatus(val label: String) {
    RUNNING("Active"),
    SLEEPING("Sleeping"),
    IDLE("Idle"),
    FROZEN("Frozen")
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

data class VirtualUserSession(
    val username: String = "User",
    val handle: String = "@user",
    val statusBio: String = "Virtual Space Active",
    val unreadMessagesCount: Int = 0,
    val storageFilesCount: Int = 12
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
    val memoryUsageMb: Float = 24.0f,
    val cacheSizeBytes: Long = 28_400_000L,
    val dataSizeBytes: Long = 64_200_000L,
    val notificationsEnabled: Boolean = true,
    val pendingNotifications: Int = 0,
    val resourcePriority: ResourcePriority = ResourcePriority.BALANCED,
    val isolatedStoragePath: String,
    val userSession: VirtualUserSession,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    // Real installed APK metadata
    val realSourceApkPath: String = "",
    val realApkSizeBytes: Long = 0L,
    val realVersionName: String = "1.0",
    val realVersionCode: Long = 1L,
    val isRealApkInstalled: Boolean = true,
    val targetSdkVersion: Int = 34,
    val mainActivityName: String = "",
    val virtualUid: Int = 10001
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
    val hasLaunchIntent: Boolean = true
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
