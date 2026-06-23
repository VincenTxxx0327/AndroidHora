package com.union.hora.model

enum class RefreshStrategy {
    INITIAL,
    PARTIAL,
    LOADING_REFRESH,
    SILENT,
    FORCE
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
            RefreshStrategy.INITIAL -> copy(isLoading = true, strategy = strategy)
            RefreshStrategy.PARTIAL -> copy(isLoading = true, strategy = strategy)
            RefreshStrategy.LOADING_REFRESH -> copy(isLoading = true, isForceRefreshing = true, strategy = strategy)
            RefreshStrategy.SILENT -> copy(strategy = strategy)
            RefreshStrategy.FORCE -> copy(isForceRefreshing = true, strategy = strategy)
        }
    }

    fun initData(chats: List<Chat>): PageResult {
        return copy(chats = chats, isLoading = false, isForceRefreshing = false)
    }

    fun updateData(transformer: (List<Chat>) -> List<Chat>): PageResult {
        return copy(chats = transformer(chats), isLoading = false, isForceRefreshing = false)
    }

    fun withError(): PageResult {
        return copy(isLoading = false, isForceRefreshing = false)
    }
}

sealed class Action(open val chatId: String, open val timestamp: Long) {
    data class Pin(override val chatId: String, override val timestamp: Long) : Action(chatId, timestamp)
    data class Unpin(override val chatId: String, override val timestamp: Long) : Action(chatId, timestamp)
    data class Delete(override val chatId: String, override val timestamp: Long) : Action(chatId, timestamp)
}
