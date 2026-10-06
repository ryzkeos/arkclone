package com.example.engine

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import androidx.core.content.IntentCompat
import com.example.model.CloneInstallState

class CloneInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pkg = intent.getStringExtra(CloneInstaller.EXTRA_CLONE_PACKAGE) ?: return
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
                if (confirm != null) {
                    confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirm)
                }
                CloneInstallEvents.emit(CloneInstallEvent(pkg, CloneInstallState.AWAITING_USER))
            }
            PackageInstaller.STATUS_SUCCESS ->
                CloneInstallEvents.emit(CloneInstallEvent(pkg, CloneInstallState.INSTALLED))
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE).orEmpty()
                CloneInstallEvents.emit(CloneInstallEvent(pkg, CloneInstallState.FAILED, message))
            }
        }
    }
}
