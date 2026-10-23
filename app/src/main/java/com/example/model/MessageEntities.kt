package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Capture status honest classification.
 * No fabrication, no assumptions.
 */
object CaptureStatus {
    const val TEXT_CAPTURED = "TEXT_CAPTURED"
    const val PARTIAL_NOTIFICATION = "PARTIAL_NOTIFICATION"
    const val MEDIA_REFERENCE_ONLY = "MEDIA_REFERENCE_ONLY"
    const val MEDIA_FILE_SAVED = "MEDIA_FILE_SAVED"
    const val DELETION_CONFIRMED = "DELETION_CONFIRMED"
    const val DELETION_POSSIBLE_UNCONFIRMED = "DELETION_POSSIBLE_UNCONFIRMED"
}

object MessageCategory {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
    const val AUDIO = "AUDIO"
    const val VIDEO = "VIDEO"
    const val DOCUMENT = "DOCUMENT"
    const val SYSTEM = "SYSTEM"
}

@Entity(
    tableName = "message_records",
    indices = [
        Index(value = ["dedupKey"], unique = true),
        Index(value = ["conversationId"]),
        Index(value = ["sourcePackage"]),
        Index(value = ["notificationTimestamp"]),
        Index(value = ["isDeleted"])
    ]
)
data class MessageRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourcePackage: String,
    val sourceAppName: String,
    val conversationId: String,
    val conversationTitle: String,
    val senderName: String,
    val messageText: String,
    val notificationTimestamp: Long,
    val capturedTimestamp: Long,
    val category: String = MessageCategory.TEXT,
    val captureStatus: String = CaptureStatus.TEXT_CAPTURED,
    val isDeleted: Boolean = false,
    val deletionDetectedTimestamp: Long? = null,
    val deletionEvidence: String? = null,
    val mediaUri: String? = null,
    val mediaMimeType: String? = null,
    val mediaSize: Long = 0L,
    val isMediaAccessible: Boolean = false,
    val dedupKey: String
)

@Entity(
    tableName = "media_records",
    indices = [
        Index(value = ["messageId"]),
        Index(value = ["filePath"])
    ]
)
data class MediaRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val mimeType: String,
    val mediaType: String,
    val isAccessible: Boolean,
    val referenceLabel: String?,
    val timestamp: Long
)

data class ConversationSummary(
    val conversationId: String,
    val conversationTitle: String,
    val sourcePackage: String,
    val sourceAppName: String,
    val messageCount: Int,
    val deletedCount: Int,
    val lastMessageText: String,
    val lastTimestamp: Long
)

data class ArchiveStats(
    val totalMessages: Int = 0,
    val deletedCount: Int = 0,
    val mediaCount: Int = 0,
    val storageBytes: Long = 0L,
    val whatsappCount: Int = 0,
    val messengerCount: Int = 0,
    val confirmedDeletionsCount: Int = 0,
    val unconfirmedDeletionsCount: Int = 0,
    val accessibleMediaCount: Int = 0,
    val referenceOnlyMediaCount: Int = 0
)
