package com.arkeos.arkclone.model

enum class CloneInstallState(val label: String) {
    PREPARING("Preparing"),
    AWAITING_USER("Waiting for confirmation"),
    INSTALLED("Installed"),
    FAILED("Failed"),
    NOT_INSTALLED("Not installed")
}

enum class ToastType { SUCCESS, ERROR, INFO }

data class ArkToast(
    val id: Long,
    val message: String,
    val type: ToastType
)

enum class AppCategory(val title: String) {
    ALL("All"),
    SOCIAL("Social"),
    MESSAGING("Messaging"),
    TOOLS("Tools"),
    GAMES("Games")
}

data class ClonedInstance(
    val id: String,
    val appName: String,
    val packageName: String,
    val cloneNumber: Int,
    val alias: String,
    val category: AppCategory,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val realSourceApkPath: String = "",
    val realApkSizeBytes: Long = 0L,
    val realVersionName: String = "1.0",
    val realVersionCode: Long = 1L,
    val targetSdkVersion: Int = 34,
    val mainActivityName: String = "",
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
    val mainActivityName: String = "",
    val hasLaunchIntent: Boolean = true,
    val splitSourcePaths: List<String> = emptyList()
)
