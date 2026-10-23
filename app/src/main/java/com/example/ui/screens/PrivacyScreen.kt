package com.example.ui.screens

import android.os.Build
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ArchiveStats
import com.example.security.SecurityUtils
import com.example.ui.components.formatFileSize
import com.example.ui.theme.DeletionConfirmedRed
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.VaultBorderDark
import com.example.ui.theme.VaultSurfaceDark
import com.example.ui.theme.VaultSurfaceVariantDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    stats: ArchiveStats,
    isListenerActive: Boolean,
    whatsAppEnabled: Boolean,
    messengerEnabled: Boolean,
    appLockEnabled: Boolean,
    hidePreviewsEnabled: Boolean,
    retentionDays: Int,
    onToggleWhatsApp: (Boolean) -> Unit,
    onToggleMessenger: (Boolean) -> Unit,
    onToggleAppLock: (Boolean) -> Unit,
    onToggleHidePreviews: (Boolean) -> Unit,
    onSelectRetention: (Int) -> Unit,
    onRunRetentionCleanup: (Int) -> Unit,
    onRequestExportAll: (String) -> Unit,
    onRequestClearAll: () -> Unit
) {
    val context = LocalContext.current
    var retentionExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Privacy & Vault Configuration",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Strict local-only sandbox. No network calls. No telemetry.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Storage & Maintenance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Local Storage Usage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Vault Size:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatFileSize(stats.storageBytes),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Preserved Messages:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stats.totalMessages.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Preserved Media Items:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${stats.accessibleMediaCount} saved files",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Capture Source Toggles
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Source Applications",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "Capture WhatsApp & WA Business",
                        subtitle = "Monitor incoming message notifications from WhatsApp",
                        checked = whatsAppEnabled,
                        onCheckedChange = onToggleWhatsApp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Capture Messenger & Messenger Lite",
                        subtitle = "Monitor incoming message notifications from Facebook Messenger",
                        checked = messengerEnabled,
                        onCheckedChange = onToggleMessenger
                    )
                }
            }
        }

        // Security & Screen Privacy
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Security & Locking",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingToggleRow(
                        title = "Biometric / Device Credential Lock",
                        subtitle = "Require phone PIN, pattern, or fingerprint to open vault",
                        checked = appLockEnabled,
                        onCheckedChange = onToggleAppLock
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Hide Content in App Switcher",
                        subtitle = "Blocks preview screenshots in Android recent apps menu (FLAG_SECURE)",
                        checked = hidePreviewsEnabled,
                        onCheckedChange = onToggleHidePreviews
                    )
                }
            }
        }

        // Auto Retention Cleanup
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Data Retention Policy",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Automatically or manually purge records older than the selected timeframe.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val retentionLabel = when (retentionDays) {
                        7 -> "Purge records older than 7 days"
                        30 -> "Purge records older than 30 days"
                        90 -> "Purge records older than 90 days"
                        else -> "Keep all records forever (Default)"
                    }

                    ExposedDropdownMenuBox(
                        expanded = retentionExpanded,
                        onExpandedChange = { retentionExpanded = !retentionExpanded }
                    ) {
                        OutlinedTextField(
                            value = retentionLabel,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = retentionExpanded) },
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = VaultSurfaceVariantDark,
                                unfocusedContainerColor = VaultSurfaceVariantDark,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = VaultBorderDark
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = retentionExpanded,
                            onDismissRequest = { retentionExpanded = false },
                            modifier = Modifier.background(VaultSurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Keep forever", color = Color.White) },
                                onClick = {
                                    onSelectRetention(0)
                                    retentionExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Purge older than 7 days", color = Color.White) },
                                onClick = {
                                    onSelectRetention(7)
                                    retentionExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Purge older than 30 days", color = Color.White) },
                                onClick = {
                                    onSelectRetention(30)
                                    retentionExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Purge older than 90 days", color = Color.White) },
                                onClick = {
                                    onSelectRetention(90)
                                    retentionExpanded = false
                                }
                            )
                        }
                    }

                    if (retentionDays > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { onRunRetentionCleanup(retentionDays) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run Cleanup Now ($retentionDays days)")
                        }
                    }
                }
            }
        }

        // Export Actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Manual Export",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Export your private records into a standalone text transcript or JSON schema via Android's document picker.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRequestExportAll("TXT") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export TXT", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onRequestExportAll("JSON") },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Privacy Architecture Audit Checklist
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = VaultSurfaceDark),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(VaultBorderDark)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Privacy Architecture Verification",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PrivacyAuditItem(
                        title = "Zero Network Connectivity",
                        desc = "Manifest does NOT declare android.permission.INTERNET. App is physically incapable of making network calls."
                    )
                    PrivacyAuditItem(
                        title = "Cloud Backup Excluded",
                        desc = "android:allowBackup=\"false\" and strict data extraction rules prevent Google Drive or device migration transfer of archive records."
                    )
                    PrivacyAuditItem(
                        title = "App-Private Storage",
                        desc = "Database and media reside in internal context.filesDir, inaccessible to other non-root apps on device."
                    )
                    PrivacyAuditItem(
                        title = "Zero Tracking & Zero Telemetry",
                        desc = "No crash analytics SDKs, advertising libraries, or external endpoints."
                    )
                    PrivacyAuditItem(
                        title = "Uninstallation Lifecycle",
                        desc = "Uninstalling the app cleanly removes the database and internal media files. Exported files saved via SAF remain under user control in external storage."
                    )
                }
            }
        }

        // Danger Zone: Clear Archive
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1012)),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DeletionConfirmedRed.copy(alpha = 0.5f))),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Danger Zone",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DeletionConfirmedRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Permanently wipe all captured messages, deletion signals, and associated private media from this device.",
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onRequestClearAll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DeletionConfirmedRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Complete Archive", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Technical Diagnostics (zero sensitive data)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Deleted Message Viewer LITE v1.0",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Android SDK ${Build.VERSION.SDK_INT} • Room SQLite • Local Sandbox",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmeraldPrimary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = VaultSurfaceVariantDark
            )
        )
    }
}

@Composable
fun PrivacyAuditItem(
    title: String,
    desc: String
) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 15.sp,
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}
