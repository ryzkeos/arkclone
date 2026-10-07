package com.arkeos.arkclone.data

import android.content.Context
import android.content.SharedPreferences
import com.arkeos.arkclone.model.AppCategory
import com.arkeos.arkclone.model.CloneInstallState
import com.arkeos.arkclone.model.ClonedInstance
import org.json.JSONArray
import org.json.JSONObject

class CloneStorage(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("ark_clone_vspaces_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_INSTANCES = "saved_instances"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        private const val KEY_GRID_VIEW = "grid_view_enabled"
    }

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, true)
    fun setDarkMode(enabled: Boolean) = prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()

    fun isGridView(): Boolean = prefs.getBoolean(KEY_GRID_VIEW, false)
    fun setGridView(enabled: Boolean) = prefs.edit().putBoolean(KEY_GRID_VIEW, enabled).apply()

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
                val instance = ClonedInstance(
                    id = obj.getString("id"),
                    appName = obj.getString("appName"),
                    packageName = obj.getString("packageName"),
                    cloneNumber = obj.getInt("cloneNumber"),
                    alias = obj.getString("alias"),
                    category = AppCategory.valueOf(obj.optString("category", AppCategory.TOOLS.name)),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    lastActiveAt = obj.optLong("lastActiveAt", System.currentTimeMillis()),
                    realSourceApkPath = obj.optString("realSourceApkPath", ""),
                    realApkSizeBytes = obj.optLong("realApkSizeBytes", 0L),
                    realVersionName = obj.optString("realVersionName", "1.0"),
                    realVersionCode = obj.optLong("realVersionCode", 1L),
                    targetSdkVersion = obj.optInt("targetSdkVersion", 34),
                    mainActivityName = obj.optString("mainActivityName", ""),
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
                put("category", inst.category.name)
                put("createdAt", inst.createdAt)
                put("lastActiveAt", inst.lastActiveAt)
                put("realSourceApkPath", inst.realSourceApkPath)
                put("realApkSizeBytes", inst.realApkSizeBytes)
                put("realVersionName", inst.realVersionName)
                put("realVersionCode", inst.realVersionCode)
                put("targetSdkVersion", inst.targetSdkVersion)
                put("mainActivityName", inst.mainActivityName)
                put("clonePackageName", inst.clonePackageName)
                put("installState", inst.installState.name)
                put("installError", inst.installError)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_INSTANCES, array.toString()).apply()
    }
}
