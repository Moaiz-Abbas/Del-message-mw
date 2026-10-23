package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.MessageRepository
import com.example.model.CaptureStatus
import com.example.model.MessageCategory
import com.example.model.MessageRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: MessageRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = MessageRepository(context, db.messageDao(), db.mediaDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppNameString() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Deleted Message Viewer LITE + Private", appName)
    }

    @Test
    fun testIncomingMessageAndDeletionFlow() = runBlocking {
        // Step 1: User receives message "Hey let's meet at 5pm" from Alice
        val msgId = repository.processIncomingNotification(
            sourcePackage = "com.whatsapp",
            sourceAppName = "WhatsApp",
            conversationId = "Alice",
            conversationTitle = "Alice",
            senderName = "Alice",
            messageText = "Hey let's meet at 5pm",
            notificationTimestamp = 1700000000000L,
            category = MessageCategory.TEXT,
            bitmap = null
        )

        assertTrue(msgId > 0)
        val initialMsg = db.messageDao().getMessageById(msgId)
        assertNotNull(initialMsg)
        assertEquals("Hey let's meet at 5pm", initialMsg?.messageText)
        assertEquals(false, initialMsg?.isDeleted)
        assertEquals(CaptureStatus.TEXT_CAPTURED, initialMsg?.captureStatus)

        // Step 2: Alice deletes the message on WhatsApp, system sends "This message was deleted"
        val retractedId = repository.processIncomingNotification(
            sourcePackage = "com.whatsapp",
            sourceAppName = "WhatsApp",
            conversationId = "Alice",
            conversationTitle = "Alice",
            senderName = "Alice",
            messageText = "This message was deleted",
            notificationTimestamp = 1700000010000L,
            category = MessageCategory.SYSTEM,
            bitmap = null
        )

        // Verify the original preserved message has been marked as CONFIRMED DELETION
        val preservedMsg = db.messageDao().getMessageById(msgId)
        assertNotNull(preservedMsg)
        assertEquals("Hey let's meet at 5pm", preservedMsg?.messageText)
        assertEquals(true, preservedMsg?.isDeleted)
        assertTrue(preservedMsg?.deletionEvidence?.contains("Sender retracted message") == true)
    }

    @Test
    fun testSearchAndDeletion() = runBlocking {
        db.messageDao().insert(
            MessageRecord(
                sourcePackage = "com.whatsapp",
                sourceAppName = "WhatsApp",
                conversationId = "Project Group",
                conversationTitle = "Project Group",
                senderName = "Charlie",
                messageText = "Deployment is scheduled for tomorrow",
                notificationTimestamp = 1700000000000L,
                capturedTimestamp = 1700000001000L,
                dedupKey = "dedup_charlie_1"
            )
        )

        val searchResults = db.messageDao().searchMessages("Deployment", null).first()
        assertEquals(1, searchResults.size)
        assertEquals("Deployment is scheduled for tomorrow", searchResults[0].messageText)

        // Delete conversation
        repository.deleteConversation("Project Group")
        val afterDelete = db.messageDao().searchMessages("Deployment", null).first()
        assertEquals(0, afterDelete.size)
    }

    @Test
    fun testExportJsonArchiveGeneration() {
        val testMessage = MessageRecord(
            id = 10,
            sourcePackage = "com.facebook.orca",
            sourceAppName = "Messenger",
            conversationId = "Bob",
            conversationTitle = "Bob",
            senderName = "Bob",
            messageText = "Hello from Messenger",
            notificationTimestamp = 1700000000000L,
            capturedTimestamp = 1700000001000L,
            category = MessageCategory.TEXT,
            captureStatus = CaptureStatus.TEXT_CAPTURED,
            dedupKey = "test_key_bob"
        )

        val jsonString = com.example.export.ExportManager.generateJsonArchive(listOf(testMessage))
        assertTrue(jsonString.contains("Deleted Message Viewer LITE + Private"))
        assertTrue(jsonString.contains("Hello from Messenger"))
        assertTrue(jsonString.contains("Messenger"))
        assertTrue(jsonString.contains("recordCount"))
    }
}
