package com.union.hora.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dylanc.longan.logInfo
import com.union.hora.model.Action
import com.union.hora.model.Chat
import com.union.hora.model.ChatModel
import com.union.hora.model.PageResult
import com.union.hora.model.RefreshStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

class ChatsViewModel : ViewModel() {

    private val chatModel = ChatModel()

    private val _currentPager = MutableStateFlow(PageResult())
    val currentPager: StateFlow<PageResult> = _currentPager.asStateFlow()

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    private val _pendingActions = MutableStateFlow<Set<String>>(emptySet())
    val pendingActions: StateFlow<Set<String>> = _pendingActions.asStateFlow()

    private val actionQueue = ConcurrentHashMap<String, Action>()
    private val queueMutex = Mutex()
    private var uploadJob: Job? = null
    private var idleTimerJob: Job? = null

    init {
        refresh(RefreshStrategy.INITIAL)
        startUploadLoop()
        resetIdleTimer()
    }

    private fun startUploadLoop() {
        uploadJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (actionQueue.isNotEmpty()) {
                    logInfo("开始清理延迟线程..")
                    processActionQueue()
                    resetIdleTimer()
                }
                delay(500)
            }
        }
    }

    private fun resetIdleTimer() {
        idleTimerJob?.cancel()
        idleTimerJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(IDLE_TIMEOUT_MS)
                _toast.value = "同步消息.."
                fetchFromTim()
                _toast.value = "同步成功"
            }
        }
    }

    private suspend fun processActionQueue() {
        queueMutex.withLock {
            val actionsToProcess = actionQueue.values.sortedBy { it.timestamp }
            actionsToProcess.forEach { action ->
                val success = executeAction(action)
                if (success) {
                    actionQueue.remove(action.chatId)
                    clearPending(action.chatId)
                    _toast.value = when (action) {
                        is Action.Pin -> "已置顶"
                        is Action.Unpin -> "已取消置顶"
                        is Action.Delete -> "已删除会话"
                    }
                } else {
                    logInfo("${action.chatId} 操作失败，等待下次上传")
                }
            }
        }
    }

    private suspend fun executeAction(action: Action): Boolean {
        return when (action) {
            is Action.Pin -> chatModel.pinChatOnTim(action.chatId)
            is Action.Unpin -> chatModel.unpinChatOnTim(action.chatId)
            is Action.Delete -> chatModel.deleteChatOnTim(action.chatId)
        }
    }

    private suspend fun fetchFromTim() {
        val updatedChats = chatModel.fetchChatsFromTim()
        _currentPager.value.updateData { updatedChats }
    }

    private fun enqueueAction(action: Action) {
        val existing = actionQueue[action.chatId]
        when {
            existing == null -> actionQueue[action.chatId] = action
            existing is Action.Delete -> {}
            action is Action.Delete -> actionQueue[action.chatId] = action
            (existing is Action.Pin && action is Action.Unpin) ||
                    (existing is Action.Unpin && action is Action.Pin) -> {
                actionQueue.remove(action.chatId)
                clearPending(action.chatId)
            }

            else -> actionQueue[action.chatId] = action
        }
    }

    fun refresh(strategy: RefreshStrategy) {
        val pager = _currentPager.value
        when (strategy) {
            RefreshStrategy.INITIAL -> {
                if (pager.isLoading) return
                _currentPager.value = pager.withLoading(strategy)
                viewModelScope.launch {
                    val chats = chatModel.getChatsFromLocal()
                    _currentPager.value = _currentPager.value.initData(chats)
                }
            }

            RefreshStrategy.PARTIAL -> {
                _currentPager.value = pager.withLoading(strategy)
                viewModelScope.launch {
                    val chats = chatModel.getChatsFromLocal()
                    _currentPager.value.updateData { chats }
                }
            }

            RefreshStrategy.LOADING_REFRESH -> {
                if (pager.isForceRefreshing) return
                _currentPager.value = pager.withLoading(strategy)
                viewModelScope.launch {
                    val chats = chatModel.getChatsFromLocal()
                    _currentPager.value = _currentPager.value.initData(chats)
                }
            }

            RefreshStrategy.SILENT -> {
                _currentPager.value = pager.withLoading(strategy)
                viewModelScope.launch {
                    val chats = chatModel.getChatsFromLocal()
                    _currentPager.value.updateData { chats }
                }
            }

            RefreshStrategy.FORCE -> {
                if (pager.isForceRefreshing) return
                _currentPager.value = pager.withLoading(strategy)
                viewModelScope.launch(Dispatchers.IO) {
                    processActionQueue()
                    val updatedChats = chatModel.fetchChatsFromTim()
                    _currentPager.value = _currentPager.value.initData(updatedChats)
                    resetIdleTimer()
                }
            }
        }
    }

    fun consumeToast() {
        _toast.value = null
    }

    fun pinChat(chat: Chat) {
        if (chat.isPinned) {
            unpinChat(chat)
        } else {
            doPin(chat)
        }
    }

    private fun doPin(chat: Chat) {
        val updatedChats = chatModel.pinLocally(chat.id)
        _currentPager.value.updateData { updatedChats }
        enqueueAction(Action.Pin(chat.id, System.currentTimeMillis()))
        markPending(chat.id)
    }

    private fun unpinChat(chat: Chat) {
        val updatedChats = chatModel.unpinLocally(chat.id)
        _currentPager.value.updateData { updatedChats }
        enqueueAction(Action.Unpin(chat.id, System.currentTimeMillis()))
        markPending(chat.id)
    }

    fun deleteChat(chat: Chat) {
        val updatedChats = chatModel.deleteLocally(chat.id)
        _currentPager.value.updateData { updatedChats }
        enqueueAction(Action.Delete(chat.id, System.currentTimeMillis()))
        markPending(chat.id)
    }

    private fun markPending(chatId: String) {
        _pendingActions.value += chatId
    }

    private fun clearPending(chatId: String) {
        _pendingActions.value -= chatId
    }

    override fun onCleared() {
        super.onCleared()
        uploadJob?.cancel()
        idleTimerJob?.cancel()
    }

    companion object {
        private const val IDLE_TIMEOUT_MS = 10_000L
    }
}
