package com.union.hora.model

enum class RefreshStrategy {
    INITIAL,
    MANUAL,
    SILENT
}

data class PageResult(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val isForceRefreshing: Boolean = false,
    val strategy: RefreshStrategy = RefreshStrategy.INITIAL
) {
    val showLoading: Boolean
        get() = isLoading && chats.isEmpty()

    val showEmpty: Boolean
        get() = chats.isEmpty() && !isLoading

    val showForceRefreshing: Boolean
        get() = isForceRefreshing

    fun withLoading(strategy: RefreshStrategy = this.strategy): PageResult {
        return when (strategy) {
            RefreshStrategy.INITIAL -> copy(isLoading = true, isForceRefreshing = false, strategy = strategy)
            RefreshStrategy.MANUAL -> copy(isForceRefreshing = true, isLoading = false, strategy = strategy)
            RefreshStrategy.SILENT -> copy(isLoading = false, isForceRefreshing = false, strategy = strategy)
        }
    }

    fun initData(chats: List<Chat>): PageResult {
        return copy(chats = chats, isLoading = false, isForceRefreshing = false)
    }

    /**
     * 仅更新 chats 数据，保留当前 loading/forceRefreshing 状态。
     * 用于局部操作（置顶/删除）避免意外清除刷新状态。
     */
    fun updateChats(newChats: List<Chat>): PageResult {
        return copy(chats = newChats)
    }

    /**
     * 更新数据并清除所有 loading 状态。用于数据加载完成。
     */
    fun updateData(chats: List<Chat>): PageResult {
        return copy(chats = chats, isLoading = false, isForceRefreshing = false)
    }
}

sealed class Action(open val chatId: String, open val timestamp: Long) {
    data class Pin(override val chatId: String, override val timestamp: Long) : Action(chatId, timestamp)
    data class Unpin(override val chatId: String, override val timestamp: Long) : Action(chatId, timestamp)
    data class Delete(override val chatId: String, override val timestamp: Long) : Action(chatId, timestamp)
}
