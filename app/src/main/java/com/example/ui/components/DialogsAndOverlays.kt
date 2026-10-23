package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ConfirmDialogData
import com.example.ui.theme.DeletionConfirmedRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.VaultBorderDark
import com.example.ui.theme.VaultSurfaceDark
import com.example.ui.theme.VaultSurfaceVariantDark

@Composable
fun ConfirmationDialog(
    data: ConfirmDialogData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VaultSurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = DeletionConfirmedRed,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = data.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Text(
                text = data.message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    data.confirmAction()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeletionConfirmedRed,
                    contentColor = Color.White
                )
            ) {
                Text("Confirm Delete", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ExportPreviewDialog(
    format: String,
    previewContent: String,
    onConfirmExport: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VaultSurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Export Records ($format)",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Notice: The exported file will be saved in your selected external folder via Android's document picker. Unlike the app's internal sandbox, exported external copies will remain intact if the app is uninstalled.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Preview Sample:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VaultSurfaceVariantDark)
                        .border(1.dp, VaultBorderDark, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = previewContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFFD1FAE5),
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmExport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black
                )
            ) {
                Text("Select Location & Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun VaultLockOverlay(
    onUnlockRequest: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VaultSurfaceDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(EmeraldPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Private Vault Locked",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Authentication required to access local archive records and private media.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onUnlockRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Unlock with Device PIN / Biometrics", fontWeight = FontWeight.Bold)
            }
        }
    }
}
