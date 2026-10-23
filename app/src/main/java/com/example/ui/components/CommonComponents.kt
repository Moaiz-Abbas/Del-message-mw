package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CaptureStatus
import com.example.ui.theme.DeletionConfirmedRed
import com.example.ui.theme.DeletionUnconfirmedAmber
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MessengerBrandColor
import com.example.ui.theme.WhatsAppBrandColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppBadge(
    sourcePackage: String,
    sourceAppName: String,
    modifier: Modifier = Modifier
) {
    val isWa = sourcePackage.contains("whatsapp", ignoreCase = true)
    val color = if (isWa) WhatsAppBrandColor else MessengerBrandColor
    val bg = color.copy(alpha = 0.15f)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = sourceAppName,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatusChip(
    captureStatus: String,
    isDeleted: Boolean,
    modifier: Modifier = Modifier
) {
    val (label, bg, fg, icon) = when {
        isDeleted && captureStatus == CaptureStatus.DELETION_CONFIRMED -> {
            Quad(
                "Confirmed Deletion",
                DeletionConfirmedRed.copy(alpha = 0.15f),
                DeletionConfirmedRed,
                Icons.Default.Warning
            )
        }
        isDeleted -> {
            Quad(
                "Possible Deletion",
                DeletionUnconfirmedAmber.copy(alpha = 0.15f),
                DeletionUnconfirmedAmber,
                Icons.Default.Info
            )
        }
        captureStatus == CaptureStatus.MEDIA_FILE_SAVED -> {
            Quad(
                "Media Saved",
                EmeraldPrimary.copy(alpha = 0.15f),
                EmeraldPrimary,
                Icons.Default.CheckCircle
            )
        }
        captureStatus == CaptureStatus.MEDIA_REFERENCE_ONLY -> {
            Quad(
                "Media Reference",
                Color(0xFF3B82F6).copy(alpha = 0.15f),
                Color(0xFF3B82F6),
                Icons.Default.Info
            )
        }
        else -> {
            Quad(
                "Text Captured",
                EmeraldPrimary.copy(alpha = 0.12f),
                EmeraldPrimary,
                Icons.Default.CheckCircle
            )
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

fun formatTimestamp(millis: Long): String {
    val format = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return format.format(Date(millis))
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
        bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes.toDouble() / 1024)
        else -> "$bytes B"
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
