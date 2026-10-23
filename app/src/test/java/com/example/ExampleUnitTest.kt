package com.example

import com.example.data.MessageRepository
import com.example.export.ExportManager
import com.example.model.CaptureStatus
import com.example.model.MessageCategory
import com.example.model.MessageRecord
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testDeletionNoticeClassification() {
        // WhatsApp standard retraction strings
        assertTrue(MessageRepository.isDeletionNotice("This message was deleted"))
        assertTrue(MessageRepository.isDeletionNotice("this message was deleted"))
        assertTrue(MessageRepository.isDeletionNotice("You deleted this message"))
        assertTrue(MessageRepository.isDeletionNotice("Ce message a été supprimé"))
        assertTrue(MessageRepository.isDeletionNotice("Este mensaje fue eliminado"))

        // Regular incoming messages
        assertFalse(MessageRepository.isDeletionNotice("Hey, are you free tonight?"))
        assertFalse(MessageRepository.isDeletionNotice("Photo"))
        assertFalse(MessageRepository.isDeletionNotice("Deleted file.pdf"))
    }

    @Test
    fun testMessageCategoryDetection() {
        assertEquals(
            MessageCategory.IMAGE,
            MessageRepository.detectCategory("Photo", hasPicture = false)
        )
        assertEquals(
            MessageCategory.IMAGE,
            MessageRepository.detectCategory("📷 Photo", hasPicture = false)
        )
        assertEquals(
            MessageCategory.AUDIO,
            MessageRepository.detectCategory("Voice message (0:14)", hasPicture = false)
        )
        assertEquals(
            MessageCategory.VIDEO,
            MessageRepository.detectCategory("Video (1:02)", hasPicture = false)
        )
        assertEquals(
            MessageCategory.DOCUMENT,
            MessageRepository.detectCategory("Document: contract.pdf", hasPicture = false)
        )
        assertEquals(
            MessageCategory.TEXT,
            MessageRepository.detectCategory("Regular meeting at 3pm", hasPicture = false)
        )
        assertEquals(
            MessageCategory.IMAGE,
            MessageRepository.detectCategory("Any caption", hasPicture = true)
        )
    }

    @Test
    fun testExportTxtGeneration() {
        val testMessage = MessageRecord(
            id = 1,
            sourcePackage = "com.whatsapp",
            sourceAppName = "WhatsApp",
            conversationId = "Alice",
            conversationTitle = "Alice",
            senderName = "Alice",
            messageText = "Secret message before deletion",
            notificationTimestamp = 1700000000000L,
            capturedTimestamp = 1700000001000L,
            category = MessageCategory.TEXT,
            captureStatus = CaptureStatus.DELETION_CONFIRMED,
            isDeleted = true,
            deletionDetectedTimestamp = 1700000050000L,
            deletionEvidence = "Sender retracted message with notice 'This message was deleted'",
            dedupKey = "test_key_1"
        )

        val txt = ExportManager.generateTxtTranscript(listOf(testMessage))
        assertTrue(txt.contains("DELETED MESSAGE VIEWER LITE"))
        assertTrue(txt.contains("Secret message before deletion"))
        assertTrue(txt.contains("DELETION SIGNAL DETECTED"))
        assertTrue(txt.contains("Sender retracted message"))
        assertTrue(txt.contains("WhatsApp"))
    }

}
