package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConversationSummary
import com.example.model.MessageRecord
import com.example.ui.components.AppBadge
import com.example.ui.components.StatusChip
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.DeletionConfirmedRed
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.VaultBorderDark
import com.example.ui.theme.VaultSurfaceDark
import com.example.ui.theme.VaultSurfaceVariantDark
import java.io.File

@Composable
fun ArchiveScreen(
    conversations: List<ConversationSummary>,
    activeConversationId: String?,
    activeConversationMessages: List<MessageRecord>,
    searchQuery: String,
    searchResults: List<MessageRecord>,
    selectedAppFilter: String?,
    selectedMessageIds: Set<Long>,
    onSearchQueryChange: (String) -> Unit,
    onAppFilterChange: (String?) -> Unit,
    onOpenConversation: (String) -> Unit,
    onCloseConversation: () -> Unit,
    onToggleSelectMessage: (Long) -> Unit,
    onClearSelection: () -> Unit,
    onRequestExportSelected: () -> Unit,
    onRequestDeleteSelected: () -> Unit,
    onRequestExportConversation: (String) -> Unit,
    onRequestDeleteConversation: (String) -> Unit,
    onRequestDeleteMessage: (Long) -> Unit
) {
    if (activeConversationId != null) {
        BackHandler {
            if (selectedMessageIds.isNotEmpty()) {
                onClearSelection()
            } else {
                onCloseConversation()
            }
        }
        ConversationDetailView(
            conversationId = activeConversationId,
            messages = activeConversationMessages,
            selectedMessageIds = selectedMessageIds,
            onBack = onCloseConversation,
            onToggleSelectMessage = onToggleSelectMessage,
            onClearSelection = onClearSelection,
            onRequestExportSelected = onRequestExportSelected,
            onRequestDeleteSelected = onRequestDeleteSelected,
            onRequestExportConversation = { onRequestExportConversation(activeConversationId) },
            onRequestDeleteConversation = { onRequestDeleteConversation(activeConversationId) },
            onRequestDeleteMessage = onRequestDeleteMessage
        )
    } else {
        ConversationListView(
            conversations = conversations,
            searchQuery = searchQuery,
            searchResults = searchResults,
            selectedAppFilter = selectedAppFilter,
            onSearchQueryChange = onSearchQueryChange,
            onAppFilterChange = onAppFilterChange,
            onOpenConversation = onOpenConversation,
            onRequestDeleteConversation = onRequestDeleteConversation
        )
    }
}

@Composable
fun ConversationListView(
    conversations: List<ConversationSummary>,
    searchQuery: String,
    searchResults: List<MessageRecord>,
    selectedAppFilter: String?,
    onSearchQueryChange: (String) -> Unit,
    onAppFilterChange: (String?) -> Unit,
    onOpenConversation: (String) -> Unit,
    onRequestDeleteConversation: (String) -> Unit
) {
    val isSearching = searchQuery.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("Search messages, contacts, groups...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VaultSurfaceDark,
                unfocusedContainerColor = VaultSurfaceDark,
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = VaultBorderDark
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedAppFilter == null,
                onClick = { onAppFilterChange(null) },
                label = { Text("All", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldContainer,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedAppFilter == "com.whatsapp",
                onClick = {
                    onAppFilterChange(if (selectedAppFilter == "com.whatsapp") null else "com.whatsapp")
                },
                label = { Text("WhatsApp", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldContainer,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedAppFilter == "com.facebook.orca",
                onClick = {
                    onAppFilterChange(if (selectedAppFilter == "com.facebook.orca") null else "com.facebook.orca")
                },
                label = { Text("Messenger", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldContainer,
                    selectedLabelColor = Color.White
                )
            )
        }

        if (isSearching) {
            Text(
                text = "Search Results (${searchResults.size})",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No messages found matching '$searchQuery'",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults, key = { it.id }) { msg ->
                        SearchResultItem(
                            message = msg,
                            onClick = { onOpenConversation(msg.conversationId) }
                        )
                    }
                }
            }
        } else {
            val filteredConversations = conversations.filter {
                selectedAppFilter == null || it.sourcePackage.contains(selectedAppFilter.replace("com.", ""))
            }

            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Conversations Yet",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Incoming message notifications will appear grouped here by contact or conversation title.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredConversations, key = { it.conversationId }) { conv ->
                        ConversationItem(
                            conversation = conv,
                            onClick = { onOpenConversation(conv.conversationId) },
                            onDelete = { onRequestDeleteConversation(conv.conversationId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ConversationSummary,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppBadge(
                        sourcePackage = conversation.sourcePackage,
                        sourceAppName = conversation.sourceAppName
                    )
                    Text(
                        text = conversation.conversationTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = conversation.lastMessageText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${conversation.messageCount} messages",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (conversation.deletedCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DeletionConfirmedRed.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${conversation.deletedCount} deleted",
                                color = DeletionConfirmedRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Text(
                        text = "• ${formatTimestamp(conversation.lastTimestamp)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete conversation",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SearchResultItem(
    message: MessageRecord,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppBadge(message.sourcePackage, message.sourceAppName)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = message.conversationTitle,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = formatTimestamp(message.notificationTimestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${message.senderName}: ${message.messageText}",
                fontSize = 12.sp,
                color = if (message.isDeleted) DeletionConfirmedRed else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ConversationDetailView(
    conversationId: String,
    messages: List<MessageRecord>,
    selectedMessageIds: Set<Long>,
    onBack: () -> Unit,
    onToggleSelectMessage: (Long) -> Unit,
    onClearSelection: () -> Unit,
    onRequestExportSelected: () -> Unit,
    onRequestDeleteSelected: () -> Unit,
    onRequestExportConversation: () -> Unit,
    onRequestDeleteConversation: () -> Unit,
    onRequestDeleteMessage: (Long) -> Unit
) {
    val isMultiSelect = selectedMessageIds.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Conversation Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VaultSurfaceDark)
                .border(1.dp, VaultBorderDark)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isMultiSelect) "${selectedMessageIds.size} Selected" else conversationId,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${messages.size} total captured",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isMultiSelect) {
                IconButton(onClick = onRequestExportSelected) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export Selected",
                        tint = EmeraldPrimary
                    )
                }
                IconButton(onClick = onRequestDeleteSelected) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Selected",
                        tint = DeletionConfirmedRed
                    )
                }
                IconButton(onClick = onClearSelection) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Selection",
                        tint = Color.White
                    )
                }
            } else {
                IconButton(onClick = onRequestExportConversation) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export Conversation",
                        tint = EmeraldPrimary
                    )
                }
                IconButton(onClick = onRequestDeleteConversation) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Conversation",
                        tint = DeletionConfirmedRed
                    )
                }
            }
        }

        // Message Thread
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No messages found in this conversation.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isSelected = selectedMessageIds.contains(msg.id)
                    MessageBubbleItem(
                        message = msg,
                        isSelected = isSelected,
                        onToggleSelect = { onToggleSelectMessage(msg.id) },
                        onDeleteSingle = { onRequestDeleteMessage(msg.id) }
                    )
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MessageBubbleItem(
    message: MessageRecord,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onDeleteSingle: () -> Unit
) {
    val bubbleBg = if (message.isDeleted) {
        Color(0xFF261214)
    } else if (isSelected) {
        EmeraldContainer.copy(alpha = 0.5f)
    } else {
        VaultSurfaceDark
    }

    val bubbleBorder = if (message.isDeleted) {
        DeletionConfirmedRed.copy(alpha = 0.5f)
    } else if (isSelected) {
        EmeraldPrimary
    } else {
        VaultBorderDark
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bubbleBg),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(bubbleBorder)),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onToggleSelect() },
                onLongClick = { onToggleSelect() }
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = message.senderName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = formatTimestamp(message.notificationTimestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body
            Text(
                text = message.messageText,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            // Local Media Preview if accessible
            if (message.isMediaAccessible && !message.mediaUri.isNullOrBlank()) {
                val bitmap = remember(message.mediaUri) {
                    try {
                        val file = File(message.mediaUri)
                        if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                    } catch (_: Exception) {
                        null
                    }
                }
                if (bitmap != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Saved media photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            // Deletion Evidence Highlight
            if (message.isDeleted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DeletionConfirmedRed.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.deletionEvidence ?: "Sender retracted this message.",
                        color = DeletionConfirmedRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Status chip and single delete action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(
                    captureStatus = message.captureStatus,
                    isDeleted = message.isDeleted
                )

                IconButton(
                    onClick = onDeleteSingle,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Message",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
