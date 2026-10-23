package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.ArchiveStats
import com.example.model.MessageRecord
import com.example.security.SecurityUtils
import com.example.ui.components.AppBadge
import com.example.ui.components.StatusChip
import com.example.ui.components.formatFileSize
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.DeletionConfirmedRed
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.VaultBorderDark
import com.example.ui.theme.VaultSurfaceDark
import com.example.ui.theme.VaultSurfaceVariantDark

@Composable
fun DashboardScreen(
    stats: ArchiveStats,
    isListenerActive: Boolean,
    recentMessages: List<MessageRecord>,
    whatsAppActive: Boolean,
    messengerActive: Boolean,
    onOpenConversation: (String) -> Unit,
    onNavigateArchive: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero Visual Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, VaultBorderDark, RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.privacy_hero_banner_1792777162532),
                    contentDescription = "Zero Cloud Privacy Vault",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        item {
            // Privacy Guarantee Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(VaultSurfaceVariantDark)
                    .border(1.dp, VaultBorderDark, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "100% Device-Local Architecture",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Zero internet permission • Excluded from cloud backups",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Notification Access Card
        item {
            if (!isListenerActive) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF261D0C)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD97706))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Notification Access Required",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To preserve incoming messages before they are deleted by senders, Android requires you to grant Notification Access. All data remains exclusively on your device.",
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { SecurityUtils.openNotificationAccessSettings(context) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD97706),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Notification Settings", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(VaultSurfaceDark)
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notification Listener Active",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Monitoring WhatsApp and Messenger locally",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Metrics Grid (2 x 2)
        item {
            Text(
                text = "Archive Statistics",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Captured Messages",
                    value = stats.totalMessages.toString(),
                    subtitle = "${stats.whatsappCount} WA • ${stats.messengerCount} FB",
                    icon = Icons.Default.Archive,
                    iconTint = EmeraldPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Deletion Records",
                    value = stats.deletedCount.toString(),
                    subtitle = "${stats.confirmedDeletionsCount} confirmed",
                    icon = Icons.Default.Delete,
                    iconTint = DeletionConfirmedRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Media Files",
                    value = stats.mediaCount.toString(),
                    subtitle = "${stats.accessibleMediaCount} saved • ${stats.referenceOnlyMediaCount} refs",
                    icon = Icons.Default.Image,
                    iconTint = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Storage Used",
                    value = formatFileSize(stats.storageBytes),
                    subtitle = "App-private disk",
                    icon = Icons.Default.Folder,
                    iconTint = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Recent Activity Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Message Activity",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (recentMessages.isNotEmpty()) {
                    Text(
                        text = "View Archive",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary,
                        modifier = Modifier.clickable { onNavigateArchive() }
                    )
                }
            }
        }

        if (recentMessages.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Archive is Empty",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When incoming WhatsApp or Messenger notifications arrive on your phone, they will automatically be preserved in this local vault.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(recentMessages) { msg ->
                RecentMessageItem(
                    message = msg,
                    onClick = { onOpenConversation(msg.conversationId) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentMessageItem(
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
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppBadge(
                        sourcePackage = message.sourcePackage,
                        sourceAppName = message.sourceAppName
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = message.conversationTitle,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = formatTimestamp(message.notificationTimestamp),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${message.senderName}: ${message.messageText}",
                fontSize = 12.sp,
                color = if (message.isDeleted) DeletionConfirmedRed else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            StatusChip(
                captureStatus = message.captureStatus,
                isDeleted = message.isDeleted
            )
        }
    }
}
