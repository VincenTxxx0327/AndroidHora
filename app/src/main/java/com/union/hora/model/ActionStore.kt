package com.union.hora.model

import com.google.gson.Gson
import com.tencent.mmkv.MMKV

/**
 * 使用 MMKV 持久化待上传的操作队列。
 * - save: 写入 MMKV
 * - remove: 从 MMKV 移除（上传成功后调用）
 * - restoreAll: App 重启时从 MMKV 恢复未完成的操作
 */
object ActionStore {

    private val mmkv: MMKV by lazy { MMKV.mmkvWithID("chat_action_queue") }
    private val gson = Gson()

    private data class ActionEntry(
        val type: String,
        val chatId: String,
        val timestamp: Long
    )

    private fun key(chatId: String) = "action_$chatId"

    fun save(action: Action) {
        val entry = ActionEntry(action::class.simpleName ?: "Unknown", action.chatId, action.timestamp)
        mmkv.encode(key(action.chatId), gson.toJson(entry))
    }

    fun remove(chatId: String) {
        mmkv.removeValueForKey(key(chatId))
    }

    fun restoreAll(): List<Action> {
        val keys = mmkv.allKeys() ?: return emptyList()
        return keys.mapNotNull { k ->
            mmkv.decodeString(k)?.let { json ->
                try {
                    val entry = gson.fromJson(json, ActionEntry::class.java)
                    when (entry.type) {
                        "Pin" -> Action.Pin(entry.chatId, entry.timestamp)
                        "Unpin" -> Action.Unpin(entry.chatId, entry.timestamp)
                        "Delete" -> Action.Delete(entry.chatId, entry.timestamp)
                        else -> null
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }.sortedBy { it.timestamp }
    }

    fun clear() {
        mmkv.clearAll()
    }
}
