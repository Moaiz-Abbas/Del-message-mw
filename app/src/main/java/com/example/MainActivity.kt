package com.example

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.security.SecurityUtils
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.ExportPreviewDialog
import com.example.ui.components.VaultLockOverlay
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeletedScreen
import com.example.ui.screens.MediaScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.theme.DeletionConfirmedRed
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.VaultBorderDark
import com.example.ui.theme.VaultSurfaceDark

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as PrivateArchiveApp
                return MainViewModel(app.repository, app.preferencesManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppScreen(
                    viewModel = viewModel,
                    onLaunchDeviceUnlock = { launchDeviceUnlock() },
                    onStartCreateDocument = { mimeType, fileName ->
                        createDocumentLauncher.launch(fileName)
                    }
                )
            }
        }
    }

    private var pendingExportMimeType = "text/plain"

    private val createDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            if (uri != null) {
                viewModel.finalizeExport(this, uri)
            }
        }

    private val deviceUnlockLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.unlockVault()
            }
        }

    private fun launchDeviceUnlock() {
        val intent = SecurityUtils.createDeviceUnlockIntent(this)
        if (intent != null) {
            deviceUnlockLauncher.launch(intent)
        } else {
            // Device does not have PIN/pattern set, unlock automatically
            viewModel.unlockVault()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshNotificationStatus(this)

        // Apply FLAG_SECURE if enabled in preferences
        val hidePreviews = viewModel.preferencesManager.hidePreviews.value
        if (hidePreviews) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    onLaunchDeviceUnlock: () -> Unit,
    onStartCreateDocument: (String, String) -> Unit
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val isListenerActive by viewModel.isNotificationAccessActive.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val activeConversationId by viewModel.activeConversationId.collectAsStateWithLifecycle()
    val activeMessages by viewModel.activeConversationMessages.collectAsStateWithLifecycle()
    val deletedMessages by viewModel.deletedMessages.collectAsStateWithLifecycle()
    val mediaMessages by viewModel.mediaMessages.collectAsStateWithLifecycle()
    val recentMessages by viewModel.recentMessages.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val selectedAppFilter by viewModel.selectedAppFilter.collectAsStateWithLifecycle()
    val selectedMessageIds by viewModel.selectedMessageIds.collectAsStateWithLifecycle()

    val whatsAppEnabled by viewModel.preferencesManager.whatsAppEnabled.collectAsStateWithLifecycle()
    val messengerEnabled by viewModel.preferencesManager.messengerEnabled.collectAsStateWithLifecycle()
    val appLockEnabled by viewModel.preferencesManager.appLockEnabled.collectAsStateWithLifecycle()
    val hidePreviewsEnabled by viewModel.preferencesManager.hidePreviews.collectAsStateWithLifecycle()
    val retentionDays by viewModel.preferencesManager.retentionDays.collectAsStateWithLifecycle()

    val isExportDialogOpen by viewModel.isExportDialogOpen.collectAsStateWithLifecycle()
    val exportPreview by viewModel.exportPreview.collectAsStateWithLifecycle()
    val exportFormat by viewModel.exportFormat.collectAsStateWithLifecycle()
    val confirmDialogData by viewModel.confirmDialogState.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.userFeedback.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissFeedback()
        }
    }

    if (isLocked) {
        VaultLockOverlay(onUnlockRequest = onLaunchDeviceUnlock)
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (activeConversationId == null) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (currentTab) {
                                MainTab.OVERVIEW -> "Private Vault"
                                MainTab.ARCHIVE -> "Message Archive"
                                MainTab.DELETED -> "Deletion Tracker"
                                MainTab.MEDIA -> "Media Vault"
                                MainTab.PRIVACY -> "Privacy Architecture"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = VaultSurfaceDark
                    ),
                    actions = {
                        if (appLockEnabled) {
                            IconButton(onClick = { viewModel.lockVault() }) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Lock Vault",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (activeConversationId == null) {
                NavigationBar(
                    containerColor = VaultSurfaceDark,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.OVERVIEW,
                        onClick = { viewModel.selectTab(MainTab.OVERVIEW) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                        label = { Text("Overview", fontSize = 10.sp) },
                        colors = navigationItemColors()
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.ARCHIVE,
                        onClick = { viewModel.selectTab(MainTab.ARCHIVE) },
                        icon = {
                            if (stats.totalMessages > 0) {
                                BadgedBox(badge = { Badge { Text(stats.totalMessages.toString()) } }) {
                                    Icon(Icons.Default.Archive, contentDescription = "Archive")
                                }
                            } else {
                                Icon(Icons.Default.Archive, contentDescription = "Archive")
                            }
                        },
                        label = { Text("Archive", fontSize = 10.sp) },
                        colors = navigationItemColors()
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.DELETED,
                        onClick = { viewModel.selectTab(MainTab.DELETED) },
                        icon = {
                            if (stats.deletedCount > 0) {
                                BadgedBox(badge = {
                                    Badge(containerColor = DeletionConfirmedRed) {
                                        Text(stats.deletedCount.toString())
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Deleted")
                                }
                            } else {
                                Icon(Icons.Default.Delete, contentDescription = "Deleted")
                            }
                        },
                        label = { Text("Deleted", fontSize = 10.sp) },
                        colors = navigationItemColors()
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.MEDIA,
                        onClick = { viewModel.selectTab(MainTab.MEDIA) },
                        icon = { Icon(Icons.Default.Image, contentDescription = "Media") },
                        label = { Text("Media", fontSize = 10.sp) },
                        colors = navigationItemColors()
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.PRIVACY,
                        onClick = { viewModel.selectTab(MainTab.PRIVACY) },
                        icon = { Icon(Icons.Default.Security, contentDescription = "Privacy") },
                        label = { Text("Privacy", fontSize = 10.sp) },
                        colors = navigationItemColors()
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                MainTab.OVERVIEW -> {
                    DashboardScreen(
                        stats = stats,
                        isListenerActive = isListenerActive,
                        recentMessages = recentMessages,
                        whatsAppActive = whatsAppEnabled,
                        messengerActive = messengerEnabled,
                        onOpenConversation = { convId ->
                            viewModel.selectTab(MainTab.ARCHIVE)
                            viewModel.openConversation(convId)
                        },
                        onNavigateArchive = { viewModel.selectTab(MainTab.ARCHIVE) }
                    )
                }
                MainTab.ARCHIVE -> {
                    ArchiveScreen(
                        conversations = conversations,
                        activeConversationId = activeConversationId,
                        activeConversationMessages = activeMessages,
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        selectedAppFilter = selectedAppFilter,
                        selectedMessageIds = selectedMessageIds,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onAppFilterChange = { viewModel.setAppFilter(it) },
                        onOpenConversation = { viewModel.openConversation(it) },
                        onCloseConversation = { viewModel.closeConversation() },
                        onToggleSelectMessage = { viewModel.toggleSelectMessage(it) },
                        onClearSelection = { viewModel.clearSelection() },
                        onRequestExportSelected = {
                            viewModel.startExport("TXT", ids = selectedMessageIds.toList())
                        },
                        onRequestDeleteSelected = { viewModel.requestDeleteSelected() },
                        onRequestExportConversation = { convId ->
                            viewModel.startExport("TXT", conversationId = convId)
                        },
                        onRequestDeleteConversation = { convId ->
                            viewModel.requestDeleteConversation(convId)
                        },
                        onRequestDeleteMessage = { viewModel.requestDeleteMessage(it) }
                    )
                }
                MainTab.DELETED -> {
                    DeletedScreen(
                        deletedMessages = deletedMessages,
                        onOpenConversation = { convId ->
                            viewModel.selectTab(MainTab.ARCHIVE)
                            viewModel.openConversation(convId)
                        },
                        onRequestDeleteMessage = { viewModel.requestDeleteMessage(it) }
                    )
                }
                MainTab.MEDIA -> {
                    MediaScreen(
                        mediaMessages = mediaMessages,
                        onOpenConversation = { convId ->
                            viewModel.selectTab(MainTab.ARCHIVE)
                            viewModel.openConversation(convId)
                        },
                        onRequestDeleteMessage = { viewModel.requestDeleteMessage(it) }
                    )
                }
                MainTab.PRIVACY -> {
                    PrivacyScreen(
                        stats = stats,
                        isListenerActive = isListenerActive,
                        whatsAppEnabled = whatsAppEnabled,
                        messengerEnabled = messengerEnabled,
                        appLockEnabled = appLockEnabled,
                        hidePreviewsEnabled = hidePreviewsEnabled,
                        retentionDays = retentionDays,
                        onToggleWhatsApp = { viewModel.preferencesManager.setWhatsAppEnabled(it) },
                        onToggleMessenger = { viewModel.preferencesManager.setMessengerEnabled(it) },
                        onToggleAppLock = { viewModel.preferencesManager.setAppLockEnabled(it) },
                        onToggleHidePreviews = { viewModel.preferencesManager.setHidePreviews(it) },
                        onSelectRetention = { viewModel.preferencesManager.setRetentionDays(it) },
                        onRunRetentionCleanup = { viewModel.runRetentionCleanup(it) },
                        onRequestExportAll = { fmt -> viewModel.startExport(fmt) },
                        onRequestClearAll = { viewModel.requestClearAllArchive() }
                    )
                }
            }
        }
    }

    // Dialogs
    if (isExportDialogOpen) {
        val extension = if (exportFormat == "JSON") "json" else "txt"
        val mime = if (exportFormat == "JSON") "application/json" else "text/plain"
        val fileName = "vault_export_${System.currentTimeMillis()}.$extension"

        ExportPreviewDialog(
            format = exportFormat,
            previewContent = exportPreview,
            onConfirmExport = {
                onStartCreateDocument(mime, fileName)
            },
            onDismiss = { viewModel.dismissExportDialog() }
        )
    }

    confirmDialogData?.let { data ->
        ConfirmationDialog(
            data = data,
            onDismiss = { viewModel.dismissConfirmDialog() }
        )
    }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = EmeraldPrimary,
    selectedTextColor = EmeraldPrimary,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    indicatorColor = EmeraldContainer
)
