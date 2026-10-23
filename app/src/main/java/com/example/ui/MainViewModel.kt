package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MessageRepository
import com.example.data.PreferencesManager
import com.example.export.ExportManager
import com.example.model.ArchiveStats
import com.example.model.ConversationSummary
import com.example.model.MessageRecord
import com.example.security.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    OVERVIEW,
    ARCHIVE,
    DELETED,
    MEDIA,
    PRIVACY
}

class MainViewModel(
    private val repository: MessageRepository,
    val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _currentTab = MutableStateFlow(MainTab.OVERVIEW)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _selectedMessageIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedMessageIds: StateFlow<Set<Long>> = _selectedMessageIds.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedAppFilter = MutableStateFlow<String?>(null)
    val selectedAppFilter: StateFlow<String?> = _selectedAppFilter.asStateFlow()

    private val _isNotificationAccessActive = MutableStateFlow(false)
    val isNotificationAccessActive: StateFlow<Boolean> = _isNotificationAccessActive.asStateFlow()

    private val _isLocked = MutableStateFlow(preferencesManager.appLockEnabled.value)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    // Export State
    private val _isExportDialogOpen = MutableStateFlow(false)
    val isExportDialogOpen: StateFlow<Boolean> = _isExportDialogOpen.asStateFlow()

    private val _exportPreview = MutableStateFlow("")
    val exportPreview: StateFlow<String> = _exportPreview.asStateFlow()

    private val _exportFormat = MutableStateFlow("TXT")
    val exportFormat: StateFlow<String> = _exportFormat.asStateFlow()

    private var exportTargetMessages: List<MessageRecord> = emptyList()

    // Confirmation Dialog
    private val _confirmDialogState = MutableStateFlow<ConfirmDialogData?>(null)
    val confirmDialogState: StateFlow<ConfirmDialogData?> = _confirmDialogState.asStateFlow()

    // Media Filter ("ALL", "ACCESSIBLE", "REFERENCE")
    private val _mediaFilter = MutableStateFlow("ALL")
    val mediaFilter: StateFlow<String> = _mediaFilter.asStateFlow()

    // Snack / Info message
    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    val stats: StateFlow<ArchiveStats> = repository.stats
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ArchiveStats()
        )

    val conversations: StateFlow<List<ConversationSummary>> = repository.conversations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val deletedMessages: StateFlow<List<MessageRecord>> = repository.deletedMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val mediaMessages: StateFlow<List<MessageRecord>> = repository.mediaMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentMessages: StateFlow<List<MessageRecord>> = repository.getRecentMessages(6)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(kotlinx.coroutines.FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<MessageRecord>> = _searchQuery
        .debounce(250)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            repository.searchMessages(query.trim(), _selectedAppFilter.value)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeConversationMessages: StateFlow<List<MessageRecord>> = _activeConversationId
        .flatMapLatest { convId ->
            if (convId != null) {
                repository.getMessagesForConversation(convId)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        _activeConversationId.value = null
        clearSelection()
    }

    fun openConversation(convId: String) {
        _activeConversationId.value = convId
        clearSelection()
    }

    fun closeConversation() {
        _activeConversationId.value = null
        clearSelection()
    }

    fun toggleSelectMessage(id: Long) {
        val current = _selectedMessageIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedMessageIds.value = current
    }

    fun clearSelection() {
        _selectedMessageIds.value = emptySet()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAppFilter(filter: String?) {
        _selectedAppFilter.value = filter
    }

    fun setMediaFilter(filter: String) {
        _mediaFilter.value = filter
    }

    fun refreshNotificationStatus(context: Context) {
        _isNotificationAccessActive.value = SecurityUtils.isNotificationAccessGranted(context)
    }

    fun unlockVault() {
        _isLocked.value = false
    }

    fun lockVault() {
        if (preferencesManager.appLockEnabled.value) {
            _isLocked.value = true
        }
    }

    fun requestDeleteMessage(id: Long) {
        _confirmDialogState.value = ConfirmDialogData(
            title = "Delete Message",
            message = "This message and any associated local media file will be permanently erased from this device.",
            confirmAction = {
                viewModelScope.launch {
                    repository.deleteMessage(id)
                    clearSelection()
                    _userFeedback.value = "Message permanently deleted."
                }
            }
        )
    }

    fun requestDeleteSelected() {
        val ids = _selectedMessageIds.value.toList()
        if (ids.isEmpty()) return
        _confirmDialogState.value = ConfirmDialogData(
            title = "Delete ${ids.size} Messages",
            message = "All ${ids.size} selected records and associated media will be permanently removed.",
            confirmAction = {
                viewModelScope.launch {
                    repository.deleteMessages(ids)
                    clearSelection()
                    _userFeedback.value = "${ids.size} messages deleted."
                }
            }
        )
    }

    fun requestDeleteConversation(convId: String) {
        _confirmDialogState.value = ConfirmDialogData(
            title = "Delete Conversation",
            message = "All captured messages and media for '$convId' will be permanently deleted from the archive.",
            confirmAction = {
                viewModelScope.launch {
                    repository.deleteConversation(convId)
                    if (_activeConversationId.value == convId) {
                        _activeConversationId.value = null
                    }
                    _userFeedback.value = "Conversation deleted."
                }
            }
        )
    }

    fun requestClearAllArchive() {
        _confirmDialogState.value = ConfirmDialogData(
            title = "Wipe Complete Archive",
            message = "This will erase all captured messages, deletion signals, and private media files. This action cannot be undone.",
            confirmAction = {
                viewModelScope.launch {
                    repository.clearCompleteArchive()
                    _activeConversationId.value = null
                    clearSelection()
                    _userFeedback.value = "All archive data has been erased."
                }
            }
        )
    }

    fun dismissConfirmDialog() {
        _confirmDialogState.value = null
    }

    fun dismissFeedback() {
        _userFeedback.value = null
    }

    fun startExport(format: String, conversationId: String? = null, ids: List<Long>? = null) {
        viewModelScope.launch {
            _exportFormat.value = format
            exportTargetMessages = repository.getExportMessages(
                scope = if (!ids.isNullOrEmpty()) "SELECTED" else if (conversationId != null) "CONVERSATION" else "ALL",
                conversationId = conversationId,
                ids = ids
            )

            val preview = if (format == "JSON") {
                ExportManager.generateJsonArchive(exportTargetMessages.take(5))
            } else {
                ExportManager.generateTxtTranscript(exportTargetMessages.take(5))
            }
            _exportPreview.value = preview
            _isExportDialogOpen.value = true
        }
    }

    fun dismissExportDialog() {
        _isExportDialogOpen.value = false
    }

    fun finalizeExport(context: Context, uri: Uri) {
        viewModelScope.launch {
            val content = if (_exportFormat.value == "JSON") {
                ExportManager.generateJsonArchive(exportTargetMessages)
            } else {
                ExportManager.generateTxtTranscript(exportTargetMessages)
            }
            val success = ExportManager.writeContentToUri(context, uri, content)
            _isExportDialogOpen.value = false
            _userFeedback.value = if (success) {
                "Export saved successfully (${exportTargetMessages.size} records)."
            } else {
                "Failed to write export file."
            }
        }
    }

    fun runRetentionCleanup(days: Int) {
        viewModelScope.launch {
            val count = repository.cleanupRetention(days)
            _userFeedback.value = "Retention cleanup completed. $count records removed."
        }
    }
}

data class ConfirmDialogData(
    val title: String,
    val message: String,
    val confirmAction: () -> Unit
)
