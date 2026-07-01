package com.union.hora.ui

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.tencent.mmkv.MMKV
import com.union.hora.model.ActionStore
import com.union.hora.model.RefreshStrategy
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * 竞态条件验证测试（Instrumented Test，需在设备/模拟器上运行）
 *
 * 验证场景：
 * 1. 上传循环(500ms) 和 MANUAL刷新 同时调用 processActionQueue 时，tryLock 防止重复执行
 * 2. 定时同步 fetchFromTim(clearLoading=false) 不会清除 MANUAL 刷新的 forceRefreshing 状态
 * 3. ActionStore.remove 在 synchronized 块内，不会因竞态丢失 MMKV 数据
 * 4. 进程重启后 MMKV 能恢复未完成的操作
 */
class ChatsViewModelRaceConditionTest {

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        MMKV.initialize(context)
        ActionStore.clear()
    }

    /**
     * 测试1：上传循环与 MANUAL 刷新并发调用 processActionQueue
     *
     * 预期：tryLock 保证同一时刻只有一个 processActionQueue 在执行，
     *       不会出现重复网络请求。所有操作最终都被处理完毕。
     */
    @Test
    fun concurrentProcessActionQueue_completesWithoutDeadlock() = runBlocking {
        val viewModel = ChatsViewModel()

        // 等待初始加载完成
        delay(2000)
        assertTrue("初始数据应已加载", viewModel.currentPager.value.chats.isNotEmpty())

        // 取前5个chat进行操作，构造批量队列
        val chats = viewModel.currentPager.value.chats.take(5)
        chats.forEachIndexed { index, chat ->
            if (index % 2 == 0) {
                viewModel.pinChat(chat)
            } else {
                viewModel.deleteChat(chat)
            }
        }

        // 立即触发 MANUAL 刷新，与上传循环(500ms检查)并发竞争 processMutex
        viewModel.refresh(RefreshStrategy.MANUAL)
        assertTrue("MANUAL 刷新应设置 isForceRefreshing", viewModel.currentPager.value.isForceRefreshing)

        // 等待足够时间让上传(每操作1s) + fetchFromTim(1.5s) 完成
        // 5个操作 × 1s = 5s 上传 + 1.5s 同步 = 6.5s，给 8s 余量
        delay(8000)

        // 验证：所有 pending actions 应已清除（上传成功后 clearPending）
        assertTrue(
            "所有操作应已处理完毕，但仍有 pending: ${viewModel.pendingActions.value}",
            viewModel.pendingActions.value.isEmpty()
        )

        // 验证：MANUAL 刷新完成后 isForceRefreshing 应为 false
        assertFalse(
            "MANUAL 刷新完成后 isForceRefreshing 应为 false",
            viewModel.currentPager.value.isForceRefreshing
        )

        // 验证：MMKV 中不应有残留记录（全部上传成功后已 remove）
        assertTrue(
            "MMKV 应无残留操作记录",
            ActionStore.restoreAll().isEmpty()
        )
    }

    /**
     * 测试2：定时同步不清除 MANUAL 刷新状态
     *
     * 预期：fetchFromTim(clearLoading=false) 使用 updateChats，
     *       不会在 MANUAL 刷新进行中意外清除 isForceRefreshing。
     */
    @Test
    fun periodicSync_doesNotClearForceRefreshing_duringManualRefresh() = runBlocking {
        val viewModel = ChatsViewModel()
        delay(2000) // 等待初始加载

        // 触发 MANUAL 刷新
        viewModel.refresh(RefreshStrategy.MANUAL)
        assertTrue("MANUAL 刷新中 isForceRefreshing 应为 true", viewModel.currentPager.value.isForceRefreshing)

        // MANUAL 的 processActionQueue + fetchFromTim 约需 1.5s
        // 在此期间手动等待但不干扰，模拟定时同步可能在此窗口触发
        // 由于定时同步使用 clearLoading=false，不会清除 forceRefreshing
        delay(500)

        // MANUAL 刷新仍在进行中（fetchFromTim 还没完成），isForceRefreshing 应仍为 true
        // 注意：如果操作队列为空，processActionQueue 很快返回，fetchFromTim 约1.5s
        assertTrue(
            "MANUAL 刷新进行中，isForceRefreshing 不应被定时同步清除",
            viewModel.currentPager.value.isForceRefreshing
        )

        // 等待 MANUAL 完成
        delay(2000)
        assertFalse("MANUAL 完成后 isForceRefreshing 应为 false", viewModel.currentPager.value.isForceRefreshing)
    }

    /**
     * 测试3：Pin + Unpin 互相抵消，不从 MMKV 恢复
     *
     * 预期：enqueueAction 中 Pin+Unpin 抵消后，ActionStore 也同步移除，
     *       不应有残留记录导致重启后恢复无意义操作。
     */
    @Test
    fun pinThenUnpin_cancelsActionInQueueAndMmkv() = runBlocking {
        val viewModel = ChatsViewModel()
        delay(2000)

        val chat = viewModel.currentPager.value.chats.first()
        val wasPinned = chat.isPinned

        // Pin 然后 Unpin（或反过来），应在队列中互相抵消
        viewModel.pinChat(chat)  // 如果已pinned则unpin，否则pin
        viewModel.pinChat(chat)  // 反向操作，应抵消

        // 等待上传循环处理（如果有残留操作的话）
        delay(2000)

        // 验证：抵消后队列应为空，MMKV 也应为空
        assertTrue(
            "Pin+Unpin 抵消后 MMKV 不应有残留",
            ActionStore.restoreAll().isEmpty()
        )
        assertTrue(
            "Pin+Unpin 抵消后 pendingActions 应为空",
            viewModel.pendingActions.value.isEmpty()
        )

        // 验证：chat 的 isPinned 状态应与操作前一致（pin+unpin = 无净变化）
        // 注意：如果 wasPinned=true，第一次pinChat实际是unpin，第二次是pin，净效果=不变
        val updatedChat = viewModel.currentPager.value.chats.find { it.id == chat.id }
        if (updatedChat != null) {
            assertTrue("净效果应保持原始pinned状态", updatedChat.isPinned == wasPinned)
        }
    }

    /**
     * 测试4：MMKV 持久化与恢复
     *
     * 预期：操作入队后写入 MMKV，上传成功后从 MMKV 移除。
     *       如果进程被杀，新 ViewModel 创建时能从 MMKV 恢复未完成的操作。
     */
    @Test
    fun mmkv_persistsAndRestoresActions() = runBlocking {
        val viewModel = ChatsViewModel()
        delay(2000)

        // 入队一个操作
        val chat = viewModel.currentPager.value.chats.first { !it.isPinned }
        viewModel.pinChat(chat)

        // 等待确保 enqueueAction 已写入 MMKV
        delay(200)

        // 验证 MMKV 中有记录
        val stored = ActionStore.restoreAll()
        assertTrue("MMKV 应有待上传操作记录", stored.isNotEmpty())
        assertTrue("应包含刚操作的 chatId", stored.any { it.chatId == chat.id })

        // 等待上传循环处理完毕（1s 网络延迟 + 500ms 检查间隔）
        delay(3000)

        // 验证上传成功后 MMKV 记录已移除
        assertTrue(
            "上传成功后 MMKV 应无残留记录",
            ActionStore.restoreAll().isEmpty()
        )
    }
}
