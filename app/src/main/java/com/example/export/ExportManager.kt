package com.example.export

import android.content.Context
import android.net.Uri
import com.example.model.MessageRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    fun generateTxtTranscript(messages: List<MessageRecord>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val builder = StringBuilder()

        builder.append("====================================================\n")
        builder.append("DELETED MESSAGE VIEWER LITE - LOCAL ARCHIVE EXPORT\n")
        builder.append("Export Date: ").append(dateFormat.format(Date())).append("\n")
        builder.append("Total Messages: ").append(messages.size).append("\n")
        builder.append("Privacy Notice: This export was generated upon explicit user request.\n")
        builder.append("This file resides in external storage and will NOT be deleted if\n")
        builder.append("the application is uninstalled.\n")
        builder.append("====================================================\n\n")

        for (msg in messages) {
            val dateStr = dateFormat.format(Date(msg.notificationTimestamp))
            builder.append("----------------------------------------------------\n")
            builder.append("Time: ").append(dateStr).append(" | App: ").append(msg.sourceAppName).append("\n")
            builder.append("Conversation: ").append(msg.conversationTitle).append("\n")
            builder.append("Sender: ").append(msg.senderName).append("\n")
            builder.append("Status: ").append(msg.captureStatus).append("\n")

            if (msg.isDeleted) {
                builder.append("[DELETION SIGNAL DETECTED]\n")
                if (msg.deletionDetectedTimestamp != null) {
                    builder.append("Detected At: ").append(dateFormat.format(Date(msg.deletionDetectedTimestamp))).append("\n")
                }
                if (!msg.deletionEvidence.isNullOrBlank()) {
                    builder.append("Evidence: ").append(msg.deletionEvidence).append("\n")
                }
            }

            builder.append("Message Content:\n").append(msg.messageText).append("\n")

            if (msg.isMediaAccessible && !msg.mediaUri.isNullOrBlank()) {
                builder.append("[Media file preserved in local storage: ").append(msg.mediaUri).append("]\n")
            } else if (msg.category != "TEXT") {
                builder.append("[Media reference placeholder: ").append(msg.category).append("]\n")
            }
            builder.append("\n")
        }

        return builder.toString()
    }

    fun generateJsonArchive(messages: List<MessageRecord>): String {
        val root = JSONObject()
        root.put("application", "Deleted Message Viewer LITE + Private")
        root.put("version", "1.0")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("recordCount", messages.size)
        root.put(
            "notice",
            "Exported upon explicit user action. Exists outside app-private sandbox."
        )

        val array = JSONArray()
        for (msg in messages) {
            val item = JSONObject()
            item.put("id", msg.id)
            item.put("sourcePackage", msg.sourcePackage)
            item.put("sourceAppName", msg.sourceAppName)
            item.put("conversationId", msg.conversationId)
            item.put("conversationTitle", msg.conversationTitle)
            item.put("senderName", msg.senderName)
            item.put("messageText", msg.messageText)
            item.put("notificationTimestamp", msg.notificationTimestamp)
            item.put("capturedTimestamp", msg.capturedTimestamp)
            item.put("category", msg.category)
            item.put("captureStatus", msg.captureStatus)
            item.put("isDeleted", msg.isDeleted)
            item.put("deletionDetectedTimestamp", msg.deletionDetectedTimestamp ?: JSONObject.NULL)
            item.put("deletionEvidence", msg.deletionEvidence ?: JSONObject.NULL)
            item.put("isMediaAccessible", msg.isMediaAccessible)
            item.put("mediaMimeType", msg.mediaMimeType ?: JSONObject.NULL)
            item.put("mediaSize", msg.mediaSize)
            array.put(item)
        }

        root.put("records", array)
        return root.toString(2)
    }

    suspend fun writeContentToUri(context: Context, uri: Uri, content: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    OutputStreamWriter(stream).use { writer ->
                        writer.write(content)
                        writer.flush()
                    }
                }
                true
            } catch (_: Exception) {
                false
            }
        }
}
