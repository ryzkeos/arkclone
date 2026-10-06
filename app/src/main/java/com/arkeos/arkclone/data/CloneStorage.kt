package com.arkeos.arkclone.data

import android.content.Context
import android.content.SharedPreferences
import com.arkeos.arkclone.model.AppCategory
import com.arkeos.arkclone.model.CloneInstallState
import com.arkeos.arkclone.model.ClonedInstance
import com.arkeos.arkclone.model.InstanceStatus
import com.arkeos.arkclone.model.NotificationItem
import com.arkeos.arkclone.model.ResourcePriority
import com.arkeos.arkclone.model.VirtualUserSession
import org.json.JSONArray
import org.json.JSONObject

class CloneStorage(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ark_clone_vspaces_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_INSTANCES = "saved_instances"
        private const val KEY_NOTIFICATIONS = "saved_notifications"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        private const val KEY_AUTO_CLEAN = "auto_clean_enabled"
    }

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)
    fun setDarkMode(enabled: Boolean) = prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()

    fun isAutoCleanEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_CLEAN, false)
    fun setAutoCleanEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTO_CLEAN, enabled).apply()

    fun loadInstances(): List<ClonedInstance> {
        val jsonString = prefs.getString(KEY_INSTANCES, null)
        // Per user request: do not auto-clone apps upon installation. Start empty.
        if (jsonString.isNullOrEmpty()) {
            return emptyList()
        }

        val list = mutableListOf<ClonedInstance>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val userSessionObj = obj.optJSONObject("userSession") ?: JSONObject()
                val session = VirtualUserSession(
                    username = userSessionObj.optString("username", ""),
                    handle = userSessionObj.optString("handle", ""),
                    statusBio = userSessionObj.optString("statusBio", "Fresh Isolated Space"),
                    isAccountSetupComplete = userSessionObj.optBoolean("isAccountSetupComplete", false),
                    registeredPhoneOrEmail = userSessionObj.optString("registeredPhoneOrEmail", ""),
                    isolatedNotes = userSessionObj.optString("isolatedNotes", ""),
                    storageFilesCount = userSessionObj.optInt("storageFilesCount", 0),
                    recentActivityTimestamp = userSessionObj.optLong("recentActivityTimestamp", System.currentTimeMillis())
                )

                val instance = ClonedInstance(
                    id = obj.getString("id"),
                    appName = obj.getString("appName"),
                    packageName = obj.getString("packageName"),
                    cloneNumber = obj.getInt("cloneNumber"),
                    alias = obj.getString("alias"),
                    badgeTag = obj.getString("badgeTag"),
                    category = AppCategory.valueOf(obj.optString("category", AppCategory.TOOLS.name)),
                    status = InstanceStatus.valueOf(obj.optString("status", InstanceStatus.IDLE.name)),
                    memoryUsageMb = obj.optDouble("memoryUsageMb", 14.0).toFloat(),
                    cacheSizeBytes = obj.optLong("cacheSizeBytes", 0L),
                    dataSizeBytes = obj.optLong("dataSizeBytes", 1_200_000L),
                    notificationsEnabled = obj.optBoolean("notificationsEnabled", true),
                    pendingNotifications = obj.optInt("pendingNotifications", 0),
                    resourcePriority = ResourcePriority.valueOf(
                        obj.optString("resourcePriority", ResourcePriority.BALANCED.name)
                    ),
                    isolatedStoragePath = obj.optString("isolatedStoragePath", "/vspaces/default"),
                    userSession = session,
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    lastActiveAt = obj.optLong("lastActiveAt", System.currentTimeMillis()),
                    isFreshlyInstalled = obj.optBoolean("isFreshlyInstalled", true),
                    realSourceApkPath = obj.optString("realSourceApkPath", ""),
                    realApkSizeBytes = obj.optLong("realApkSizeBytes", 24_000_000L),
                    realVersionName = obj.optString("realVersionName", "1.0"),
                    realVersionCode = obj.optLong("realVersionCode", 1L),
                    isRealApkInstalled = obj.optBoolean("isRealApkInstalled", true),
                    targetSdkVersion = obj.optInt("targetSdkVersion", 34),
                    mainActivityName = obj.optString("mainActivityName", ""),
                    virtualUid = obj.optInt("virtualUid", 10001),
                    clonePackageName = obj.optString("clonePackageName", ""),
                    installState = try {
                        CloneInstallState.valueOf(obj.optString("installState", CloneInstallState.NOT_INSTALLED.name))
                    } catch (e: IllegalArgumentException) {
                        CloneInstallState.NOT_INSTALLED
                    },
                    installError = obj.optString("installError", "")
                )
                list.add(instance)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
        return list
    }

    fun saveInstances(instances: List<ClonedInstance>) {
        val array = JSONArray()
        for (inst in instances) {
            val obj = JSONObject().apply {
                put("id", inst.id)
                put("appName", inst.appName)
                put("packageName", inst.packageName)
                put("cloneNumber", inst.cloneNumber)
                put("alias", inst.alias)
                put("badgeTag", inst.badgeTag)
                put("category", inst.category.name)
                put("status", inst.status.name)
                put("memoryUsageMb", inst.memoryUsageMb.toDouble())
                put("cacheSizeBytes", inst.cacheSizeBytes)
                put("dataSizeBytes", inst.dataSizeBytes)
                put("notificationsEnabled", inst.notificationsEnabled)
                put("pendingNotifications", inst.pendingNotifications)
                put("resourcePriority", inst.resourcePriority.name)
                put("isolatedStoragePath", inst.isolatedStoragePath)
                put("createdAt", inst.createdAt)
                put("lastActiveAt", inst.lastActiveAt)
                put("isFreshlyInstalled", inst.isFreshlyInstalled)
                put("realSourceApkPath", inst.realSourceApkPath)
                put("realApkSizeBytes", inst.realApkSizeBytes)
                put("realVersionName", inst.realVersionName)
                put("realVersionCode", inst.realVersionCode)
                put("isRealApkInstalled", inst.isRealApkInstalled)
                put("targetSdkVersion", inst.targetSdkVersion)
                put("mainActivityName", inst.mainActivityName)
                put("virtualUid", inst.virtualUid)
                put("clonePackageName", inst.clonePackageName)
                put("installState", inst.installState.name)
                put("installError", inst.installError)

                val sessionObj = JSONObject().apply {
                    put("username", inst.userSession.username)
                    put("handle", inst.userSession.handle)
                    put("statusBio", inst.userSession.statusBio)
                    put("isAccountSetupComplete", inst.userSession.isAccountSetupComplete)
                    put("registeredPhoneOrEmail", inst.userSession.registeredPhoneOrEmail)
                    put("isolatedNotes", inst.userSession.isolatedNotes)
                    put("storageFilesCount", inst.userSession.storageFilesCount)
                    put("recentActivityTimestamp", inst.userSession.recentActivityTimestamp)
                }
                put("userSession", sessionObj)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_INSTANCES, array.toString()).apply()
    }

    fun loadNotifications(): List<NotificationItem> {
        val jsonString = prefs.getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        val list = mutableListOf<NotificationItem>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    NotificationItem(
                        id = obj.getString("id"),
                        instanceId = obj.getString("instanceId"),
                        appName = obj.getString("appName"),
                        cloneAlias = obj.getString("cloneAlias"),
                        badgeTag = obj.getString("badgeTag"),
                        title = obj.getString("title"),
                        body = obj.getString("body"),
                        timestamp = obj.getLong("timestamp"),
                        isRead = obj.getBoolean("isRead")
                    )
                )
            }
        } catch (e: Exception) {
            return emptyList()
        }
        return list
    }

    fun saveNotifications(notifications: List<NotificationItem>) {
        val array = JSONArray()
        for (item in notifications) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("instanceId", item.instanceId)
                put("appName", item.appName)
                put("cloneAlias", item.cloneAlias)
                put("badgeTag", item.badgeTag)
                put("title", item.title)
                put("body", item.body)
                put("timestamp", item.timestamp)
                put("isRead", item.isRead)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_NOTIFICATIONS, array.toString()).apply()
    }
}
