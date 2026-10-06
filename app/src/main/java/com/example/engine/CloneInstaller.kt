package com.example.engine

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.model.CloneInstallState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File

data class CloneInstallEvent(
    val packageName: String,
    val state: CloneInstallState,
    val message: String = ""
)

object CloneInstallEvents {
    private val mutable = MutableSharedFlow<CloneInstallEvent>(extraBufferCapacity = 32)
    val flow: SharedFlow<CloneInstallEvent> = mutable.asSharedFlow()

    fun emit(event: CloneInstallEvent) {
        mutable.tryEmit(event)
    }
}

class CloneInstaller(private val context: Context) {

    fun canInstall(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun install(clonePackage: String, apks: List<File>) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(clonePackage)
        val sessionId = installer.createSession(params)

        installer.openSession(sessionId).use { session ->
            apks.forEachIndexed { index, apk ->
                val name = if (index == 0) "base.apk" else "split_$index.apk"
                apk.inputStream().use { input ->
                    session.openWrite(name, 0, apk.length()).use { out ->
                        input.copyTo(out)
                        session.fsync(out)
                    }
                }
            }
            val intent = Intent(context, CloneInstallReceiver::class.java)
                .setAction(ACTION_RESULT)
                .putExtra(EXTRA_CLONE_PACKAGE, clonePackage)
            var flags = PendingIntent.FLAG_UPDATE_CURRENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags = flags or PendingIntent.FLAG_MUTABLE
            val pending = PendingIntent.getBroadcast(context, sessionId, intent, flags)
            session.commit(pending.intentSender)
        }
    }

    fun uninstall(clonePackage: String) {
        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:$clonePackage")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun isInstalled(packageName: String): Boolean = try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: Exception) {
        false
    }

    companion object {
        const val ACTION_RESULT = "com.example.engine.CLONE_INSTALL_RESULT"
        const val EXTRA_CLONE_PACKAGE = "ark.clone.package"
    }
}
