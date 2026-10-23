package com.example.service

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.example.PrivateArchiveApp
import com.example.data.MessageRepository
import com.example.model.MessageCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PrivateNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName ?: return
        val appName = when {
            pkg == PACKAGE_WHATSAPP -> "WhatsApp"
            pkg == PACKAGE_WHATSAPP_BUSINESS -> "WhatsApp Business"
            pkg == PACKAGE_MESSENGER -> "Messenger"
            pkg == PACKAGE_MESSENGER_LITE -> "Messenger Lite"
            else -> return // Ignore any unrelated applications!
        }

        val app = applicationContext as? PrivateArchiveApp ?: return
        val prefs = app.preferencesManager
        val isWa = pkg == PACKAGE_WHATSAPP || pkg == PACKAGE_WHATSAPP_BUSINESS
        val isMess = pkg == PACKAGE_MESSENGER || pkg == PACKAGE_MESSENGER_LITE

        if (isWa && !prefs.whatsAppEnabled.value) return
        if (isMess && !prefs.messengerEnabled.value) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Skip ongoing calls or system service notifications without message content
        if ((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) return

        serviceScope.launch {
            try {
                processNotification(app.repository, pkg, appName, sbn.postTime, extras)
            } catch (_: Exception) {
                // Safeguard listener thread against unexpected format crashes
            }
        }
    }

    private suspend fun processNotification(
        repository: MessageRepository,
        pkg: String,
        appName: String,
        postTime: Long,
        extras: Bundle
    ) {
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim() ?: ""
        val bigTitle = extras.getCharSequence(Notification.EXTRA_TITLE_BIG)?.toString()?.trim() ?: title
        val convTitle = if (bigTitle.isNotBlank()) bigTitle else title.ifBlank { appName }

        val pictureBitmap = extractBitmap(extras)

        // Try extracting MessagingStyle messages
        val messages = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(
            // Reconstruct minimal notification for extraction
            NotificationCompat.Builder(this, "channel")
                .addExtras(extras)
                .build()
        )

        if (messages != null && messages.messages.isNotEmpty()) {
            for (msg in messages.messages) {
                val sender = msg.person?.name?.toString()?.trim()
                    ?: if (messages.isGroupConversation) "Group Member" else convTitle
                val text = msg.text?.toString()?.trim() ?: ""
                val msgTimestamp = if (msg.timestamp > 0) msg.timestamp else postTime
                val category = MessageRepository.detectCategory(text, pictureBitmap != null)

                if (text.isNotBlank() || pictureBitmap != null) {
                    repository.processIncomingNotification(
                        sourcePackage = pkg,
                        sourceAppName = appName,
                        conversationId = convTitle,
                        conversationTitle = convTitle,
                        senderName = sender,
                        messageText = text.ifBlank { "[Image/Media]" },
                        notificationTimestamp = msgTimestamp,
                        category = category,
                        bitmap = pictureBitmap
                    )
                }
            }
            return
        }

        // Try extracting InboxStyle text lines if present (WhatsApp grouped summary)
        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (!textLines.isNullOrEmpty()) {
            for (line in textLines) {
                val lineStr = line?.toString()?.trim() ?: continue
                // Format often: "Sender: Message body"
                val colonIndex = lineStr.indexOf(':')
                val sender: String
                val body: String
                if (colonIndex in 1..40) {
                    sender = lineStr.substring(0, colonIndex).trim()
                    body = lineStr.substring(colonIndex + 1).trim()
                } else {
                    sender = convTitle
                    body = lineStr
                }
                val category = MessageRepository.detectCategory(body, false)
                repository.processIncomingNotification(
                    sourcePackage = pkg,
                    sourceAppName = appName,
                    conversationId = convTitle,
                    conversationTitle = convTitle,
                    senderName = sender,
                    messageText = body,
                    notificationTimestamp = postTime,
                    category = category,
                    bitmap = null
                )
            }
            return
        }

        // Fallback to standard EXTRA_TEXT / EXTRA_BIG_TEXT
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim()
        val messageBody = bigText ?: text ?: ""

        if (messageBody.isNotBlank() || pictureBitmap != null) {
            val sender = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()?.trim()
                ?: convTitle
            val category = MessageRepository.detectCategory(messageBody, pictureBitmap != null)

            repository.processIncomingNotification(
                sourcePackage = pkg,
                sourceAppName = appName,
                conversationId = convTitle,
                conversationTitle = convTitle,
                senderName = sender,
                messageText = messageBody.ifBlank { "[Media attachment]" },
                notificationTimestamp = postTime,
                category = category,
                bitmap = pictureBitmap
            )
        }
    }

    private fun extractBitmap(extras: Bundle): Bitmap? {
        val picture = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(Notification.EXTRA_PICTURE, Bitmap::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_PICTURE) as? Bitmap
        }
        if (picture != null) return picture

        // Check large icon
        val largeIcon = extras.get(Notification.EXTRA_LARGE_ICON)
        if (largeIcon is Bitmap) return largeIcon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && largeIcon is Icon) {
            try {
                val drawable = largeIcon.loadDrawable(this)
                if (drawable is android.graphics.drawable.BitmapDrawable) {
                    return drawable.bitmap
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Notification removal from system tray does NOT guarantee message deletion by sender.
        // It commonly represents user reading or clearing notifications.
        // As per privacy and honest certainty requirements, we do not mark this as a confirmed deletion.
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val PACKAGE_WHATSAPP = "com.whatsapp"
        const val PACKAGE_WHATSAPP_BUSINESS = "com.whatsapp.w4b"
        const val PACKAGE_MESSENGER = "com.facebook.orca"
        const val PACKAGE_MESSENGER_LITE = "com.facebook.mlite"
    }
}
