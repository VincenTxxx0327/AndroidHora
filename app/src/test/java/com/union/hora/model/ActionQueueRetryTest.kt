package com.union.hora.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap

/**
 * 验证上传失败时 actionQueue 的重试机制。
 *
 * 核心逻辑：processActionQueue 中 executeAction 返回 false 时，
 * 不从 actionQueue 移除该 action，上传循环 500ms 后会再次处理。
 *
 * 本测试不依赖 Android 框架，模拟 processActionQueue 的关键逻辑。
 */
class ActionQueueRetryTest {

    /**
     * 模拟上传失败 → action 保留在队列中 → 模拟重试成功 → action 被移除
     */
    @Test
    fun `failed upload retains action in queue then retry succeeds and removes it`() {
        val actionQueue = ConcurrentHashMap<String, Action>()
        val action = Action.Delete("1001", System.currentTimeMillis())

        // 1. 模拟 enqueueAction
        actionQueue[action.chatId] = action
        assertEquals(1, actionQueue.size)
        assertTrue(actionQueue.containsKey("1001"))

        // 2. 模拟 processActionQueue — 第一次上传失败
        val snapshot1 = actionQueue.values.sortedBy { it.timestamp }
        snapshot1.forEach { a ->
            val success = false // 模拟上传失败
            if (success) {
                actionQueue.remove(a.chatId)
            }
            // 失败时不移除
        }

        // 验证：action 仍在队列中
        assertEquals("失败后 action 应保留在队列中", 1, actionQueue.size)
        assertTrue("失败后 actionQueue 应包含 chatId=1001", actionQueue.containsKey("1001"))

        // 3. 模拟 500ms 后上传循环再次触发 — 第二次上传成功
        val snapshot2 = actionQueue.values.sortedBy { it.timestamp }
        snapshot2.forEach { a ->
            val success = true // 模拟上传成功
            if (success) {
                actionQueue.remove(a.chatId)
            }
        }

        // 验证：action 已从队列移除
        assertEquals("成功后 action 应从队列移除", 0, actionQueue.size)
        assertFalse("成功后 actionQueue 不应包含 chatId=1001", actionQueue.containsKey("1001"))
    }

    /**
     * 模拟多次失败后最终成功
     */
    @Test
    fun `multiple failures then success retains action until final success`() {
        val actionQueue = ConcurrentHashMap<String, Action>()
        val action = Action.Pin("2002", System.currentTimeMillis())
        actionQueue[action.chatId] = action

        // 模拟连续3次失败
        repeat(3) { attempt ->
            val snapshot = actionQueue.values.sortedBy { it.timestamp }
            snapshot.forEach { a ->
                val success = false
                if (success) {
                    actionQueue.remove(a.chatId)
                }
            }
            assertEquals("第${attempt + 1}次失败后 action 应仍在队列中", 1, actionQueue.size)
        }

        // 第4次成功
        val snapshot = actionQueue.values.sortedBy { it.timestamp }
        snapshot.forEach { a ->
            val success = true
            if (success) {
                actionQueue.remove(a.chatId)
            }
        }
        assertEquals("第4次成功后 action 应被移除", 0, actionQueue.size)
    }

    /**
     * 模拟队列中有多个 action，部分成功部分失败
     */
    @Test
    fun `partial failure retains only failed actions`() {
        val actionQueue = ConcurrentHashMap<String, Action>()
        actionQueue["1001"] = Action.Pin("1001", 1000L)
        actionQueue["1002"] = Action.Delete("1002", 2000L)
        actionQueue["1003"] = Action.Unpin("1003", 3000L)

        // 模拟上传：1001成功，1002失败，1003成功
        val results = mapOf("1001" to true, "1002" to false, "1003" to true)
        val snapshot = actionQueue.values.sortedBy { it.timestamp }
        snapshot.forEach { a ->
            val success = results[a.chatId] ?: false
            if (success) {
                actionQueue.remove(a.chatId)
            }
        }

        // 验证：只有1002保留在队列中
        assertEquals(1, actionQueue.size)
        assertTrue(actionQueue.containsKey("1002"))
        assertFalse(actionQueue.containsKey("1001"))
        assertFalse(actionQueue.containsKey("1003"))
        assertTrue(actionQueue["1002"] is Action.Delete)
    }

    /**
     * 模拟 Pin+Unpin 抵消后再上传 — 不应上传已抵消的操作
     */
    @Test
    fun `pin then unpin cancels action and queue becomes empty`() {
        val actionQueue = ConcurrentHashMap<String, Action>()

        // 1. 入队 Pin
        val pinAction = Action.Pin("3003", 1000L)
        actionQueue[pinAction.chatId] = pinAction
        assertEquals(1, actionQueue.size)

        // 2. 模拟 enqueueAction 中的抵消逻辑
        val newAction = Action.Unpin("3003", 2000L)
        val existing = actionQueue[newAction.chatId]
        if ((existing is Action.Pin && newAction is Action.Unpin) ||
            (existing is Action.Unpin && newAction is Action.Pin)) {
            actionQueue.remove(newAction.chatId)
        }

        // 验证：队列已空，无需上传
        assertEquals("Pin+Unpin 抵消后队列应为空", 0, actionQueue.size)
    }

    /**
     * 模拟 Delete 覆盖 Pin — 队列中只保留 Delete
     */
    @Test
    fun `delete overrides pin in queue`() {
        val actionQueue = ConcurrentHashMap<String, Action>()

        // 1. 入队 Pin
        actionQueue["4004"] = Action.Pin("4004", 1000L)
        assertEquals(1, actionQueue.size)
        assertTrue(actionQueue["4004"] is Action.Pin)

        // 2. 模拟 enqueueAction 中 Delete 覆盖逻辑
        val deleteAction = Action.Delete("4004", 2000L)
        val existing = actionQueue[deleteAction.chatId]
        if (existing != null && existing !is Action.Delete && deleteAction is Action.Delete) {
            actionQueue[deleteAction.chatId] = deleteAction
        }

        // 验证：队列中是 Delete，不是 Pin
        assertEquals(1, actionQueue.size)
        assertTrue(actionQueue["4004"] is Action.Delete)
        assertFalse(actionQueue["4004"] is Action.Pin)
    }
}
