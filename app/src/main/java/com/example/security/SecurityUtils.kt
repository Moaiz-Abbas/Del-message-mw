package com.example.security

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.example.service.PrivateNotificationListenerService

object SecurityUtils {

    fun isNotificationAccessGranted(context: Context): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(context.packageName)
    }

    fun openNotificationAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun isDeviceSecure(context: Context): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return km?.isDeviceSecure == true
    }

    fun createDeviceUnlockIntent(context: Context): Intent? {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        return km?.createConfirmDeviceCredentialIntent(
            "Private Archive Vault",
            "Unlock using your device PIN, pattern, or biometrics to access your archive"
        )
    }
}
