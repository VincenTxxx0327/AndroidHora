package com.union.hora.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageResultTest {

    @Test
    fun `withLoading INITIAL sets isLoading true`() {
        val result = PageResult().withLoading(RefreshStrategy.INITIAL)
        assertTrue(result.isLoading)
        assertFalse(result.isForceRefreshing)
    }

    @Test
    fun `withLoading MANUAL sets isForceRefreshing true`() {
        val result = PageResult().withLoading(RefreshStrategy.MANUAL)
        assertTrue(result.isForceRefreshing)
        assertFalse(result.isLoading)
    }

    @Test
    fun `withLoading SILENT clears both loading flags`() {
        val result = PageResult(isLoading = true, isForceRefreshing = true)
            .withLoading(RefreshStrategy.SILENT)
        assertFalse(result.isLoading)
        assertFalse(result.isForceRefreshing)
    }

    @Test
    fun `initData clears all loading states`() {
        val result = PageResult(isLoading = true, isForceRefreshing = true)
            .initData(listOf(sampleChat()))
        assertFalse(result.isLoading)
        assertFalse(result.isForceRefreshing)
        assertTrue(result.chats.isNotEmpty())
    }

    @Test
    fun `updateChats preserves loading states - key fix for race condition`() {
        // 模拟：MANUAL 刷新进行中 (isForceRefreshing=true)，
        // 此时用户执行 pin 操作，updateChats 不应清除 forceRefreshing
        val result = PageResult(
            chats = listOf(sampleChat()),
            isForceRefreshing = true,
            strategy = RefreshStrategy.MANUAL
        ).updateChats(listOf(sampleChat(id = "9999")))

        assertTrue(result.isForceRefreshing)
        assertTrue(result.chats.first().id == "9999")
    }

    @Test
    fun `updateData clears loading states - used by MANUAL refresh completion`() {
        val result = PageResult(isLoading = true, isForceRefreshing = true)
            .updateData(listOf(sampleChat()))

        assertFalse(result.isLoading)
        assertFalse(result.isForceRefreshing)
    }

    @Test
    fun `showLoading only true when isLoading and chats empty`() {
        assertTrue(PageResult(isLoading = true, chats = emptyList()).showLoading)
        assertFalse(PageResult(isLoading = true, chats = listOf(sampleChat())).showLoading)
        assertFalse(PageResult(isLoading = false, chats = emptyList()).showLoading)
    }

    @Test
    fun `showEmpty only true when chats empty and not loading`() {
        assertTrue(PageResult(isLoading = false, chats = emptyList()).showEmpty)
        assertFalse(PageResult(isLoading = true, chats = emptyList()).showEmpty)
        assertFalse(PageResult(isLoading = false, chats = listOf(sampleChat())).showEmpty)
    }

    private fun sampleChat(id: String = "1001") = Chat(
        id = id,
        name = "测试",
        avatar = "",
        lastMessage = "测试消息",
        timestamp = System.currentTimeMillis(),
        unreadCount = 0,
        isPinned = false
    )
}
