package com.union.hora.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dylanc.longan.logInfo
import com.union.hora.HoraApp
import com.union.hora.model.Action
import com.union.hora.model.ActionStore
import com.union.hora.model.Chat
import com.union.hora.model.ChatModel
import com.union.hora.model.PageResult
import com.union.hora.model.RefreshStrategy
import com.union.hora.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
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
    private val processMutex = Mutex()
    private var uploadJob: Job? = null
    private var syncJob: Job? = null

    init {
        restorePendingActions()
        refresh(RefreshStrategy.INITIAL)
        startUploadLoop()
        startPeriodicSync()
    }

    // ==================== MMKV 持久化 ====================

    private fun restorePendingActions() {
        val restored = ActionStore.restoreAll()
        restored.forEach { action ->
            actionQueue[action.chatId] = action
            markPending(action.chatId)
        }
        if (restored.isNotEmpty()) {
            logInfo("从MMKV恢复 ${restored.size} 条待上传操作")
        }
    }

    // ==================== 线程安全的 enqueueAction ====================

    private fun enqueueAction(action: Action) {
        synchronized(actionQueue) {
            val existing = actionQueue[action.chatId]
            val actionType = action::class.simpleName
            val existingType = existing?.let { it::class.simpleName }
            when {
                existing == null -> {
                    logInfo("[enqueue] chatId=${action.chatId} 新操作=$actionType 入队，当前队列大小=${actionQueue.size + 1}")
                    actionQueue[action.chatId] = action
                    ActionStore.save(action)
                }
                existing is Action.Delete -> {
                    logInfo("[enqueue] chatId=${action.chatId} 忽略 $actionType，已有Delete在队列中")
                }
                action is Action.Delete -> {
                    logInfo("[enqueue] chatId=${action.chatId} 覆盖 $existingType → Delete，当前队列大小=${actionQueue.size}")
                    actionQueue[action.chatId] = action
                    ActionStore.save(action)
                }
                (existing is Action.Pin && action is Action.Unpin) ||
                (existing is Action.Unpin && action is Action.Pin) -> {
                    logInfo("[enqueue] chatId=${action.chatId} $existingType + $actionType 互相抵消，移出队列，当前队列大小=${actionQueue.size - 1}")
                    actionQueue.remove(action.chatId)
                    ActionStore.remove(action.chatId)
                    clearPending(action.chatId)
                }
                else -> {
                    logInfo("[enqueue] chatId=${action.chatId} 更新 $existingType → $actionType（取最新），当前队列大小=${actionQueue.size}")
                    actionQueue[action.chatId] = action
                    ActionStore.save(action)
                }
            }
        }
    }

    // ==================== 上传循环 ====================

    private fun startUploadLoop() {
        uploadJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                if (actionQueue.isNotEmpty()) {
                    logInfo("[upload-loop] 检测到队列非空，大小=${actionQueue.size}，触发上传")
                    processActionQueue()
                }
                delay(500)
            }
        }
    }

    /**
     * 使用 tryLock 防止并发执行：
     * - 上传循环调用时：tryLock 失败则跳过，500ms 后重试
     * - MANUAL 刷新调用时：tryLock 失败则跳过，直接 fetchFromTim
     *
     * ActionStore.remove 移入 synchronized 块内，防止与 enqueueAction 竞态导致 MMKV 数据丢失。
     */
    private suspend fun processActionQueue() {
        if (!processMutex.tryLock()) {
            logInfo("[process] tryLock 失败，已有其他协程在执行 processActionQueue，跳过本次")
            return
        }
        try {
            val actionsToProcess = synchronized(actionQueue) {
                actionQueue.values.sortedBy { it.timestamp }
            }
            logInfo("[process] tryLock 成功，待处理操作数=${actionsToProcess.size}，详情=${actionsToProcess.map { "${it.chatId}:${it::class.simpleName}" }}")

            actionsToProcess.forEach { action ->
                val actionType = action::class.simpleName
                logInfo("[process] 开始上传 chatId=${action.chatId} action=$actionType")
                val success = executeAction(action)
                if (success) {
                    synchronized(actionQueue) {
                        actionQueue.remove(action.chatId)
                        ActionStore.remove(action.chatId)
                    }
                    clearPending(action.chatId)
                    logInfo("[process] chatId=${action.chatId} action=$actionType 上传成功，已从队列和MMKV移除，剩余队列大小=${actionQueue.size}")
                    _toast.value = when (action) {
                        is Action.Pin -> "已置顶"
                        is Action.Unpin -> "已取消置顶"
                        is Action.Delete -> "已删除会话"
                    }
                } else {
                    logInfo("[process] chatId=${action.chatId} action=$actionType 上传失败！保留在队列中，500ms后重试")
                }
            }
            logInfo("[process] 处理完毕，最终队列大小=${actionQueue.size}")
        } finally {
            processMutex.unlock()
        }
    }

    private suspend fun executeAction(action: Action): Boolean {
        return when (action) {
            is Action.Pin -> chatModel.pinChatOnTim(action.chatId)
            is Action.Unpin -> chatModel.unpinChatOnTim(action.chatId)
            is Action.Delete -> chatModel.deleteChatOnTim(action.chatId)
        }
    }

    // ==================== 10秒定时同步 ====================

    private fun startPeriodicSync() {
        syncJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(SYNC_INTERVAL_MS)
                logInfo("[periodic-sync] 10秒定时同步触发，clearLoading=false")
                fetchFromTim(clearLoading = false)
            }
        }
    }

    /**
     * @param clearLoading true: 清除 loading/forceRefreshing（MANUAL 刷新完成时）
     *                     false: 仅更新数据，保留状态（定时同步，避免清除 MANUAL 刷新的转圈状态）
     */
    private suspend fun fetchFromTim(clearLoading: Boolean) {
        logInfo("[sync] fetchFromTim 开始，clearLoading=$clearLoading")
        val oldChats = _currentPager.value.chats.associateBy { it.id }
        val updatedChats = chatModel.fetchChatsFromTim()
        _currentPager.update {
            if (clearLoading) {
                logInfo("[sync] fetchFromTim 完成，使用 initData 清除 loading 状态")
                it.initData(updatedChats)
            } else {
                logInfo("[sync] fetchFromTim 完成，使用 updateChats 保留 loading 状态")
                it.updateChats(updatedChats)
            }
        }
        // 检测新消息并发送通知
        notifyNewMessages(oldChats, updatedChats)
    }

    /**
     * 比较新旧 chats，对有新消息的会话发送通知。
     * 判定条件：lastMessage 变化 或 timestamp 变大（新消息到达）
     */
    private suspend fun notifyNewMessages(
        oldChats: Map<String, Chat>,
        newChats: List<Chat>
    ) {
        val context = HoraApp.context
        newChats.forEach { newChat ->
            val oldChat = oldChats[newChat.id]
            val isNewMessage = oldChat == null ||
                    (newChat.lastMessage != oldChat.lastMessage && newChat.timestamp > oldChat.timestamp)
            if (isNewMessage) {
                NotificationHelper.showNotification(
                    context = context,
                    title = newChat.name,
                    subtitle = newChat.lastMessage,
                    avatar = newChat.avatar,
                    notifId = newChat.id.hashCode()
                )
                logInfo("[notif] 新消息通知: ${newChat.name} - ${newChat.lastMessage}")
            }
        }
    }

    // ==================== refresh ====================

    fun refresh(strategy: RefreshStrategy) {
        val pager = _currentPager.value
        logInfo("[refresh] strategy=$strategy, 当前状态 isLoading=${pager.isLoading} isForceRefreshing=${pager.isForceRefreshing}")
        when (strategy) {
            RefreshStrategy.INITIAL -> {
                if (pager.isLoading) {
                    logInfo("[refresh] INITIAL 跳过：已在 loading 中")
                    return
                }
                _currentPager.update { it.withLoading(strategy) }
                viewModelScope.launch {
                    val chats = chatModel.getChatsFromLocal()
                    _currentPager.update { it.initData(chats) }
                    logInfo("[refresh] INITIAL 完成，数据量=${chats.size}")
                }
            }
            RefreshStrategy.MANUAL -> {
                if (pager.isForceRefreshing) {
                    logInfo("[refresh] MANUAL 跳过：已在 forceRefreshing 中")
                    return
                }
                _currentPager.update { it.withLoading(strategy) }
                logInfo("[refresh] MANUAL 开始：先处理队列，再 fetchFromTim(clearLoading=true)")
                viewModelScope.launch(Dispatchers.IO) {
                    processActionQueue()
                    fetchFromTim(clearLoading = true)
                    logInfo("[refresh] MANUAL 全部完成")
                }
            }
            RefreshStrategy.SILENT -> {
                _currentPager.update { it.withLoading(strategy) }
                viewModelScope.launch {
                    val chats = chatModel.getChatsFromLocal()
                    _currentPager.update { it.updateData(chats) }
                    logInfo("[refresh] SILENT 完成，数据量=${chats.size}")
                }
            }
        }
    }

    // ==================== 局部操作 ====================

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
        _currentPager.update { it.updateChats(updatedChats) }
        enqueueAction(Action.Pin(chat.id, System.currentTimeMillis()))
        markPending(chat.id)
    }

    private fun unpinChat(chat: Chat) {
        val updatedChats = chatModel.unpinLocally(chat.id)
        _currentPager.update { it.updateChats(updatedChats) }
        enqueueAction(Action.Unpin(chat.id, System.currentTimeMillis()))
        markPending(chat.id)
    }

    fun deleteChat(chat: Chat) {
        val updatedChats = chatModel.deleteLocally(chat.id)
        _currentPager.update { it.updateChats(updatedChats) }
        enqueueAction(Action.Delete(chat.id, System.currentTimeMillis()))
        markPending(chat.id)
    }

    private fun markPending(chatId: String) {
        _pendingActions.update { it + chatId }
    }

    private fun clearPending(chatId: String) {
        _pendingActions.update { it - chatId }
    }

    override fun onCleared() {
        super.onCleared()
        uploadJob?.cancel()
        syncJob?.cancel()
    }

    companion object {
        private const val SYNC_INTERVAL_MS = 10_000L
    }
}
