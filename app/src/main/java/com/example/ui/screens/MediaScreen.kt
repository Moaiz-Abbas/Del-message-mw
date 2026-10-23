package com.example.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MessageCategory
import com.example.model.MessageRecord
import com.example.ui.components.AppBadge
import com.example.ui.components.StatusChip
import com.example.ui.components.formatFileSize
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.VaultBorderDark
import com.example.ui.theme.VaultSurfaceDark
import com.example.ui.theme.VaultSurfaceVariantDark
import java.io.File

@Composable
fun MediaScreen(
    mediaMessages: List<MessageRecord>,
    onOpenConversation: (String) -> Unit,
    onRequestDeleteMessage: (Long) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = remember(mediaMessages, selectedFilter) {
        when (selectedFilter) {
            "ACCESSIBLE" -> mediaMessages.filter { it.isMediaAccessible }
            "REFERENCE" -> mediaMessages.filter { !it.isMediaAccessible }
            else -> mediaMessages
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Honest Media Architecture Banner
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
                    text = "Media Delivery Disclosure",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Only image previews delivered in notification headers can be saved as actual files. Voice clips, videos, and documents are preserved as metadata references because Android's notification API does not receive full media downloads.",
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
                label = { Text("All Media (${mediaMessages.size})", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldContainer,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == "ACCESSIBLE",
                onClick = { selectedFilter = "ACCESSIBLE" },
                label = {
                    Text(
                        "Saved Files (${mediaMessages.count { it.isMediaAccessible }})",
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldContainer,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = selectedFilter == "REFERENCE",
                onClick = { selectedFilter = "REFERENCE" },
                label = {
                    Text(
                        "References (${mediaMessages.count { !it.isMediaAccessible }})",
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldContainer,
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
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Media Captured",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Photos and media placeholders from WhatsApp or Messenger will appear here when captured.",
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
                    MediaItemCard(
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
fun MediaItemCard(
    message: MessageRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
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
                        contentDescription = "Delete media record",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Local media image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "File Size: ${formatFileSize(message.mediaSize)} • Saved in Private App Storage",
                        fontSize = 11.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                val icon: ImageVector = when (message.category) {
                    MessageCategory.AUDIO -> Icons.Default.Mic
                    MessageCategory.VIDEO -> Icons.Default.Movie
                    MessageCategory.DOCUMENT -> Icons.Default.InsertDriveFile
                    else -> Icons.Default.Image
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(VaultSurfaceVariantDark)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Reference Only: ${message.messageText}",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Raw payload was not transmitted in notification",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(
                    captureStatus = message.captureStatus,
                    isDeleted = message.isDeleted
                )
                Text(
                    text = formatTimestamp(message.notificationTimestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
