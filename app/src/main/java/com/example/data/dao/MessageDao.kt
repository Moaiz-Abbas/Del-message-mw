package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.ConversationSummary
import com.example.model.MessageRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query(
        """
        SELECT 
            conversationId, 
            conversationTitle, 
            sourcePackage, 
            sourceAppName, 
            COUNT(*) as messageCount, 
            SUM(CASE WHEN isDeleted = 1 THEN 1 ELSE 0 END) as deletedCount, 
            (SELECT messageText FROM message_records m2 WHERE m2.conversationId = m1.conversationId ORDER BY notificationTimestamp DESC LIMIT 1) as lastMessageText, 
            MAX(notificationTimestamp) as lastTimestamp 
        FROM message_records m1 
        GROUP BY conversationId, conversationTitle, sourcePackage, sourceAppName 
        ORDER BY lastTimestamp DESC
        """
    )
    fun getConversations(): Flow<List<ConversationSummary>>

    @Query("SELECT * FROM message_records WHERE conversationId = :convId ORDER BY notificationTimestamp ASC")
    fun getMessagesForConversation(convId: String): Flow<List<MessageRecord>>

    @Query("SELECT * FROM message_records WHERE isDeleted = 1 ORDER BY notificationTimestamp DESC")
    fun getDeletedMessages(): Flow<List<MessageRecord>>

    @Query("SELECT * FROM message_records WHERE category != 'TEXT' OR isMediaAccessible = 1 OR mediaUri IS NOT NULL ORDER BY notificationTimestamp DESC")
    fun getMediaMessages(): Flow<List<MessageRecord>>

    @Query("SELECT * FROM message_records ORDER BY notificationTimestamp DESC LIMIT :limit")
    fun getRecentMessages(limit: Int = 10): Flow<List<MessageRecord>>

    @Query(
        """
        SELECT * FROM message_records 
        WHERE (messageText LIKE '%' || :query || '%' 
           OR senderName LIKE '%' || :query || '%' 
           OR conversationTitle LIKE '%' || :query || '%')
          AND (:pkg IS NULL OR sourcePackage = :pkg)
        ORDER BY notificationTimestamp DESC
        """
    )
    fun searchMessages(query: String, pkg: String?): Flow<List<MessageRecord>>

    @Query("SELECT * FROM message_records WHERE dedupKey = :key LIMIT 1")
    suspend fun findByDedupKey(key: String): MessageRecord?

    @Query("SELECT * FROM message_records WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): MessageRecord?

    @Query(
        """
        SELECT * FROM message_records 
        WHERE conversationId = :convId 
          AND (:sender = '' OR senderName = :sender) 
          AND isDeleted = 0 
        ORDER BY notificationTimestamp DESC 
        LIMIT 1
        """
    )
    suspend fun findLatestCandidateForDeletion(convId: String, sender: String): MessageRecord?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: MessageRecord): Long

    @Update
    suspend fun update(message: MessageRecord)

    @Query("DELETE FROM message_records WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM message_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("DELETE FROM message_records WHERE conversationId = :convId")
    suspend fun deleteByConversation(convId: String): Int

    @Query("DELETE FROM message_records WHERE notificationTimestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM message_records")
    suspend fun clearAll(): Int

    @Query("SELECT COUNT(*) FROM message_records")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM message_records WHERE isDeleted = 1")
    fun getDeletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM message_records WHERE isDeleted = 1 AND captureStatus = 'DELETION_CONFIRMED'")
    fun getConfirmedDeletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM message_records WHERE isDeleted = 1 AND captureStatus = 'DELETION_POSSIBLE_UNCONFIRMED'")
    fun getUnconfirmedDeletedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM message_records WHERE sourcePackage LIKE '%whatsapp%'")
    fun getWhatsAppCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM message_records WHERE sourcePackage LIKE '%facebook%' OR sourcePackage LIKE '%orca%'")
    fun getMessengerCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM message_records WHERE isMediaAccessible = 1")
    fun getAccessibleMediaCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM message_records WHERE category != 'TEXT' AND isMediaAccessible = 0")
    fun getReferenceOnlyMediaCount(): Flow<Int>

    @Query("SELECT * FROM message_records WHERE mediaUri IS NOT NULL")
    suspend fun getAllMessagesWithMedia(): List<MessageRecord>

    @Query("SELECT * FROM message_records WHERE id IN (:ids) AND mediaUri IS NOT NULL")
    suspend fun getMessagesWithMediaByIds(ids: List<Long>): List<MessageRecord>

    @Query("SELECT * FROM message_records WHERE conversationId = :convId AND mediaUri IS NOT NULL")
    suspend fun getMessagesWithMediaByConversation(convId: String): List<MessageRecord>

    @Query("SELECT * FROM message_records ORDER BY notificationTimestamp ASC")
    suspend fun getAllMessagesDirect(): List<MessageRecord>

    @Query("SELECT * FROM message_records WHERE id IN (:ids) ORDER BY notificationTimestamp ASC")
    suspend fun getMessagesByIdsDirect(ids: List<Long>): List<MessageRecord>

    @Query("SELECT * FROM message_records WHERE conversationId = :convId ORDER BY notificationTimestamp ASC")
    suspend fun getMessagesForConversationDirect(convId: String): List<MessageRecord>
}
