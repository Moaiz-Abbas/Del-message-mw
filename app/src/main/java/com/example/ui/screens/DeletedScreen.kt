package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptureStatus
import com.example.model.MessageRecord
import com.example.ui.components.AppBadge
import com.example.ui.components.StatusChip
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.DeletionConfirmedRed
import com.example.ui.theme.DeletionUnconfirmedAmber
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.VaultBorderDark
import com.example.ui.theme.VaultSurfaceDark
import com.example.ui.theme.VaultSurfaceVariantDark

@Composable
fun DeletedScreen(
    deletedMessages: List<MessageRecord>,
    onOpenConversation: (String) -> Unit,
    onRequestDeleteMessage: (Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = remember(deletedMessages, selectedFilter) {
        when (selectedFilter) {
            "CONFIRMED" -> deletedMessages.filter { it.captureStatus == CaptureStatus.DELETION_CONFIRMED }
            "UNCONFIRMED" -> deletedMessages.filter { it.captureStatus != CaptureStatus.DELETION_CONFIRMED }
            else -> deletedMessages
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Honest Uncertainty Disclaimer Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(VaultSurfaceVariantDark)
                .border(1.dp, VaultBorderDark, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF60A5FA),
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Honest Deletion Detection",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Confirmed deletions are recorded only when the messaging platform issues an explicit retraction broadcast (e.g. 'This message was deleted'). Regular notification dismissals are never fabricated as deletions.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${deletedMessages.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldContainer,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == "CONFIRMED",
                onClick = { selectedFilter = "CONFIRMED" },
                label = {
                    Text(
                        "Confirmed (${deletedMessages.count { it.captureStatus == CaptureStatus.DELETION_CONFIRMED }})",
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DeletionConfirmedRed.copy(alpha = 0.3f),
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == "UNCONFIRMED",
                onClick = { selectedFilter = "UNCONFIRMED" },
                label = {
                    Text(
                        "Unconfirmed (${deletedMessages.count { it.captureStatus != CaptureStatus.DELETION_CONFIRMED }})",
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DeletionUnconfirmedAmber.copy(alpha = 0.3f),
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Deletion Records",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "When a sender retracts a previously captured message, the original content and retraction evidence will appear here.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList, key = { it.id }) { msg ->
                    DeletedMessageCard(
                        message = msg,
                        onClick = { onOpenConversation(msg.conversationId) },
                        onDelete = { onRequestDeleteMessage(msg.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeletedMessageCard(
    message: MessageRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isConfirmed = message.captureStatus == CaptureStatus.DELETION_CONFIRMED
    val borderCol = if (isConfirmed) DeletionConfirmedRed.copy(alpha = 0.5f) else DeletionUnconfirmedAmber.copy(alpha = 0.5f)

    Card(
        colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(borderCol)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppBadge(message.sourcePackage, message.sourceAppName)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.conversationTitle,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete record",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Original Captured Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1F1214))
                    .border(1.dp, DeletionConfirmedRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "PRESERVED CONTENT (${message.senderName}):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeletionConfirmedRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.messageText,
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timeline & Evidence
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(VaultSurfaceVariantDark)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Captured: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTimestamp(message.notificationTimestamp),
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
                if (message.deletionDetectedTimestamp != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Retraction detected: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeletionConfirmedRed
                        )
                        Text(
                            text = formatTimestamp(message.deletionDetectedTimestamp),
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
                if (!message.deletionEvidence.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.deletionEvidence,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            StatusChip(
                captureStatus = message.captureStatus,
                isDeleted = message.isDeleted
            )
        }
    }
}
