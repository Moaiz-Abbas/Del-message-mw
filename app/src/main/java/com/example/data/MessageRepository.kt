package com.example.data

import android.content.Context
import android.graphics.Bitmap
import com.example.data.dao.MediaDao
import com.example.data.dao.MessageDao
import com.example.model.ArchiveStats
import com.example.model.CaptureStatus
import com.example.model.ConversationSummary
import com.example.model.MediaRecord
import com.example.model.MessageCategory
import com.example.model.MessageRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MessageRepository(
    private val context: Context,
    private val messageDao: MessageDao,
    private val mediaDao: MediaDao
) {

    val conversations: Flow<List<ConversationSummary>> = messageDao.getConversations()
    val deletedMessages: Flow<List<MessageRecord>> = messageDao.getDeletedMessages()
    val mediaMessages: Flow<List<MessageRecord>> = messageDao.getMediaMessages()

    fun getMessagesForConversation(convId: String): Flow<List<MessageRecord>> =
        messageDao.getMessagesForConversation(convId)

    fun getRecentMessages(limit: Int = 10): Flow<List<MessageRecord>> =
        messageDao.getRecentMessages(limit)

    fun searchMessages(query: String, pkg: String?): Flow<List<MessageRecord>> =
        messageDao.searchMessages(query, pkg)

    val stats: Flow<ArchiveStats> = combine(
        listOf(
            messageDao.getTotalCount(),
            messageDao.getDeletedCount(),
            messageDao.getConfirmedDeletedCount(),
            messageDao.getUnconfirmedDeletedCount(),
            messageDao.getWhatsAppCount(),
            messageDao.getMessengerCount(),
            messageDao.getAccessibleMediaCount(),
            messageDao.getReferenceOnlyMediaCount()
        )
    ) { args: Array<Int> ->
        val total = args[0]
        val deleted = args[1]
        val confirmed = args[2]
        val unconfirmed = args[3]
        val wa = args[4]
        val mess = args[5]
        val accessibleMedia = args[6]
        val refMedia = args[7]
        ArchiveStats(
            totalMessages = total,
            deletedCount = deleted,
            mediaCount = accessibleMedia + refMedia,
            storageBytes = calculateLocalStorageBytes(),
            whatsappCount = wa,
            messengerCount = mess,
            confirmedDeletionsCount = confirmed,
            unconfirmedDeletionsCount = unconfirmed,
            accessibleMediaCount = accessibleMedia,
            referenceOnlyMediaCount = refMedia
        )
    }

    suspend fun processIncomingNotification(
        sourcePackage: String,
        sourceAppName: String,
        conversationId: String,
        conversationTitle: String,
        senderName: String,
        messageText: String,
        notificationTimestamp: Long,
        category: String,
        bitmap: Bitmap?
    ): Long = withContext(Dispatchers.IO) {
        val trimmedText = messageText.trim()
        val isDeletionMarker = isDeletionNotice(trimmedText)

        if (isDeletionMarker) {
            // Find candidate message in this conversation from this sender
            val candidate = messageDao.findLatestCandidateForDeletion(conversationId, senderName)
            if (candidate != null) {
                val updated = candidate.copy(
                    isDeleted = true,
                    captureStatus = CaptureStatus.DELETION_CONFIRMED,
                    deletionDetectedTimestamp = System.currentTimeMillis(),
                    deletionEvidence = "Sender retracted message with notice: '$trimmedText'"
                )
                messageDao.update(updated)
                return@withContext updated.id
            } else {
                // Standalone unconfirmed deletion notification
                val dedupKey = "${sourcePackage}_${conversationId}_${senderName}_${trimmedText.hashCode()}_${notificationTimestamp / 10000}"
                val existing = messageDao.findByDedupKey(dedupKey)
                if (existing != null) return@withContext existing.id

                val record = MessageRecord(
                    sourcePackage = sourcePackage,
                    sourceAppName = sourceAppName,
                    conversationId = conversationId,
                    conversationTitle = conversationTitle,
                    senderName = senderName,
                    messageText = trimmedText,
                    notificationTimestamp = notificationTimestamp,
                    capturedTimestamp = System.currentTimeMillis(),
                    category = MessageCategory.SYSTEM,
                    captureStatus = CaptureStatus.DELETION_POSSIBLE_UNCONFIRMED,
                    isDeleted = true,
                    deletionDetectedTimestamp = System.currentTimeMillis(),
                    deletionEvidence = "Deletion notice detected, but original message was not previously captured.",
                    dedupKey = dedupKey
                )
                return@withContext messageDao.insert(record)
            }
        }

        // Handle regular message capture
        var savedFilePath: String? = null
        var fileSize = 0L
        var isAccessible = false
        var captureStatus = CaptureStatus.TEXT_CAPTURED

        if (bitmap != null) {
            val mediaDir = getPrivateMediaDirectory()
            val fileName = "img_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val file = File(mediaDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            savedFilePath = file.absolutePath
            fileSize = file.length()
            isAccessible = true
            captureStatus = CaptureStatus.MEDIA_FILE_SAVED
        } else if (category != MessageCategory.TEXT) {
            captureStatus = CaptureStatus.MEDIA_REFERENCE_ONLY
        }

        val dedupKey = "${sourcePackage}_${conversationId}_${senderName}_${trimmedText.hashCode()}_${notificationTimestamp / 10000}"
        val existing = messageDao.findByDedupKey(dedupKey)
        if (existing != null) {
            return@withContext existing.id
        }

        val record = MessageRecord(
            sourcePackage = sourcePackage,
            sourceAppName = sourceAppName,
            conversationId = conversationId,
            conversationTitle = conversationTitle,
            senderName = senderName,
            messageText = trimmedText,
            notificationTimestamp = notificationTimestamp,
            capturedTimestamp = System.currentTimeMillis(),
            category = category,
            captureStatus = captureStatus,
            mediaUri = savedFilePath,
            mediaMimeType = if (isAccessible) "image/jpeg" else null,
            mediaSize = fileSize,
            isMediaAccessible = isAccessible,
            dedupKey = dedupKey
        )

        val insertedId = messageDao.insert(record)
        if (insertedId > 0 && isAccessible && savedFilePath != null) {
            mediaDao.insert(
                MediaRecord(
                    messageId = insertedId,
                    fileName = File(savedFilePath).name,
                    filePath = savedFilePath,
                    fileSize = fileSize,
                    mimeType = "image/jpeg",
                    mediaType = MessageCategory.IMAGE,
                    isAccessible = true,
                    referenceLabel = null,
                    timestamp = notificationTimestamp
                )
            )
        } else if (insertedId > 0 && category != MessageCategory.TEXT) {
            mediaDao.insert(
                MediaRecord(
                    messageId = insertedId,
                    fileName = category.lowercase(),
                    filePath = "",
                    fileSize = 0L,
                    mimeType = "application/octet-stream",
                    mediaType = category,
                    isAccessible = false,
                    referenceLabel = trimmedText,
                    timestamp = notificationTimestamp
                )
            )
        }

        insertedId
    }

    suspend fun deleteMessage(id: Long) = withContext(Dispatchers.IO) {
        val message = messageDao.getMessageById(id)
        message?.mediaUri?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }
        mediaDao.deleteByMessageId(id)
        messageDao.deleteById(id)
    }

    suspend fun deleteMessages(ids: List<Long>) = withContext(Dispatchers.IO) {
        val messages = messageDao.getMessagesWithMediaByIds(ids)
        messages.forEach { msg ->
            msg.mediaUri?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
        }
        mediaDao.deleteByMessageIds(ids)
        messageDao.deleteByIds(ids)
    }

    suspend fun deleteConversation(convId: String) = withContext(Dispatchers.IO) {
        val messages = messageDao.getMessagesWithMediaByConversation(convId)
        messages.forEach { msg ->
            msg.mediaUri?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
        }
        messageDao.deleteByConversation(convId)
    }

    suspend fun cleanupRetention(days: Int): Int = withContext(Dispatchers.IO) {
        if (days <= 0) return@withContext 0
        val cutoff = System.currentTimeMillis() - (days * 24L * 60L * 60L * 1000L)
        val count = messageDao.deleteOlderThan(cutoff)
        cleanOrphanedMediaFiles()
        count
    }

    suspend fun clearCompleteArchive() = withContext(Dispatchers.IO) {
        messageDao.clearAll()
        mediaDao.clearAll()
        val mediaDir = getPrivateMediaDirectory()
        mediaDir.listFiles()?.forEach { it.delete() }
    }

    suspend fun getExportMessages(scope: String, conversationId: String? = null, ids: List<Long>? = null): List<MessageRecord> =
        withContext(Dispatchers.IO) {
            when {
                !ids.isNullOrEmpty() -> messageDao.getMessagesByIdsDirect(ids)
                conversationId != null -> messageDao.getMessagesForConversationDirect(conversationId)
                else -> messageDao.getAllMessagesDirect()
            }
        }

    private fun getPrivateMediaDirectory(): File {
        val dir = File(context.filesDir, "private_media")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun cleanOrphanedMediaFiles() {
        val mediaDir = getPrivateMediaDirectory()
        mediaDir.listFiles()?.forEach { file ->
            // Delete if not accessible or aged
            if (file.isFile && System.currentTimeMillis() - file.lastModified() > 30L * 24 * 3600 * 1000) {
                file.delete()
            }
        }
    }

    fun calculateLocalStorageBytes(): Long {
        var total = 0L
        try {
            val dbFile = context.getDatabasePath("deleted_msg_vault.db")
            if (dbFile.exists()) total += dbFile.length()
            val shm = context.getDatabasePath("deleted_msg_vault.db-shm")
            if (shm.exists()) total += shm.length()
            val wal = context.getDatabasePath("deleted_msg_vault.db-wal")
            if (wal.exists()) total += wal.length()

            val mediaDir = File(context.filesDir, "private_media")
            if (mediaDir.exists()) {
                mediaDir.walkTopDown().filter { it.isFile }.forEach { total += it.length() }
            }
        } catch (_: Exception) {
        }
        return total
    }

    companion object {
        fun isDeletionNotice(text: String): Boolean {
            val lower = text.lowercase()
            return lower.contains("this message was deleted") ||
                    lower.contains("you deleted this message") ||
                    lower.contains("message was deleted by") ||
                    lower.contains("ce message a été supprimé") ||
                    lower.contains("este mensaje fue eliminado") ||
                    lower.contains("este mensaje se eliminó") ||
                    lower.contains("diese nachricht wurde gelöscht") ||
                    lower.contains("questo messaggio è stato eliminato") ||
                    lower.contains("bericht is verwijderd")
        }

        fun detectCategory(text: String, hasPicture: Boolean): String {
            if (hasPicture) return MessageCategory.IMAGE
            val lower = text.lowercase()
            return when {
                lower.startsWith("photo") || lower.contains("📷 photo") || lower.contains("image") -> MessageCategory.IMAGE
                lower.startsWith("voice message") || lower.contains("audio") || lower.contains("voice") -> MessageCategory.AUDIO
                lower.startsWith("video") || lower.contains("🎥 video") -> MessageCategory.VIDEO
                lower.startsWith("document") || lower.contains("📄 document") || lower.endsWith(".pdf") -> MessageCategory.DOCUMENT
                else -> MessageCategory.TEXT
            }
        }
    }
}
