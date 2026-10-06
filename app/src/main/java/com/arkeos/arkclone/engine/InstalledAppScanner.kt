package com.arkeos.arkclone.engine

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.arkeos.arkclone.model.AppCategory
import com.arkeos.arkclone.model.RealInstalledApk
import java.io.File

class InstalledAppScanner(private val context: Context) {

    fun scanInstalledApps(): List<RealInstalledApk> {
        val result = mutableListOf<RealInstalledApk>()
        val pm = context.packageManager
        val seenPackages = mutableSetOf<String>()

        try {
            // First: scan launcher activities (the real user-facing apps on phone)
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedApps = pm.queryIntentActivities(mainIntent, 0)

            for (resolveInfo in resolvedApps) {
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == context.packageName || seenPackages.contains(pkgName)) continue
                seenPackages.add(pkgName)

                val appName = resolveInfo.loadLabel(pm).toString()
                val appInfo = resolveInfo.activityInfo.applicationInfo
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val sourceDir = appInfo.sourceDir ?: ""
                val apkSize = if (sourceDir.isNotBlank()) {
                    try { File(sourceDir).length() } catch (e: Exception) { 0L }
                } else 0L

                val packageInfo = try {
                    pm.getPackageInfo(pkgName, 0)
                } catch (e: Exception) {
                    null
                }

                val versionName = packageInfo?.versionName ?: "1.0"
                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo?.longVersionCode ?: 1L
                } else {
                    packageInfo?.versionCode?.toLong() ?: 1L
                }

                val category = categorize(appName, pkgName)
                val badge = if (isSystem) "SYS" else "APK"

                result.add(
                    RealInstalledApk(
                        appName = appName,
                        packageName = pkgName,
                        versionName = versionName,
                        versionCode = versionCode,
                        sourceApkPath = sourceDir,
                        apkSizeBytes = if (apkSize > 0) apkSize else 24_000_000L,
                        isSystemApp = isSystem,
                        targetSdkVersion = appInfo.targetSdkVersion,
                        category = category,
                        defaultBadge = badge,
                        mainActivityName = resolveInfo.activityInfo.name,
                        hasLaunchIntent = true,
                        splitSourcePaths = appInfo.splitSourceDirs?.toList() ?: emptyList()
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Second: if launcher returned few, scan all installed packages
        try {
            val allInstalled = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            for (appInfo in allInstalled) {
                val pkgName = appInfo.packageName
                if (pkgName == context.packageName || seenPackages.contains(pkgName)) continue

                val launchIntent = pm.getLaunchIntentForPackage(pkgName)
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                // If it has a launch intent or is user installed, include it
                if (launchIntent != null || !isSystem) {
                    seenPackages.add(pkgName)
                    val appName = pm.getApplicationLabel(appInfo).toString()
                    val sourceDir = appInfo.sourceDir ?: ""
                    val apkSize = if (sourceDir.isNotBlank()) {
                        try { File(sourceDir).length() } catch (e: Exception) { 0L }
                    } else 0L

                    val pInfo = try { pm.getPackageInfo(pkgName, 0) } catch (e: Exception) { null }
                    val vName = pInfo?.versionName ?: "1.0"
                    val vCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        pInfo?.longVersionCode ?: 1L
                    } else {
                        pInfo?.versionCode?.toLong() ?: 1L
                    }

                    result.add(
                        RealInstalledApk(
                            appName = appName,
                            packageName = pkgName,
                            versionName = vName,
                            versionCode = vCode,
                            sourceApkPath = sourceDir,
                            apkSizeBytes = if (apkSize > 0) apkSize else 18_000_000L,
                            isSystemApp = isSystem,
                            targetSdkVersion = appInfo.targetSdkVersion,
                            category = categorize(appName, pkgName),
                            defaultBadge = if (isSystem) "SYS" else "APK",
                            mainActivityName = "",
                            hasLaunchIntent = (launchIntent != null),
                            splitSourcePaths = appInfo.splitSourceDirs?.toList() ?: emptyList()
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Sort: User installed apps first, then alphabetically
        return result.sortedWith(
            compareByDescending<RealInstalledApk> { !it.isSystemApp }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun categorize(appName: String, pkgName: String): AppCategory {
        val lowerName = appName.lowercase()
        val lowerPkg = pkgName.lowercase()

        return when {
            lowerName.contains("chat") ||
                    lowerName.contains("messag") ||
                    lowerPkg.contains("whatsapp") ||
                    lowerPkg.contains("telegram") ||
                    lowerPkg.contains("signal") -> AppCategory.MESSAGING

            lowerName.contains("game") ||
                    lowerName.contains("play") ||
                    lowerPkg.contains("game") -> AppCategory.GAMES

            lowerName.contains("social") ||
                    lowerName.contains("gram") ||
                    lowerName.contains("tweet") ||
                    lowerName.contains("facebook") ||
                    lowerPkg.contains("discord") ||
                    lowerPkg.contains("twitter") ||
                    lowerPkg.contains("reddit") ||
                    lowerPkg.contains("tiktok") -> AppCategory.SOCIAL

            else -> AppCategory.TOOLS
        }
    }
}
