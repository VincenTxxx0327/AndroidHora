package com.union.hora.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Calendar

data class Chat(
    val id: String,
    val name: String,
    val avatar: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false
) {
    val time: String
        get() = formatTimestamp(timestamp)

    companion object {
        fun formatTimestamp(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            if (diff < 60_000L) return "刚刚"

            if (diff < 3_600_000L) {
                return "${(diff / 60_000L).toInt()}分钟前"
            }

            val nowCal = Calendar.getInstance()
            val msgCal = Calendar.getInstance().apply { timeInMillis = timestamp }

            val isToday = nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                    nowCal.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)
            if (isToday) {
                return String.format(
                    "%02d:%02d",
                    msgCal.get(Calendar.HOUR_OF_DAY),
                    msgCal.get(Calendar.MINUTE)
                )
            }

            if (diff < 7L * 24 * 3_600_000L) {
                val dayOfWeek = msgCal.get(Calendar.DAY_OF_WEEK)
                val dayNames = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
                return dayNames[dayOfWeek - 1]
            }

            return String.format(
                "%d/%d/%d年",
                msgCal.get(Calendar.DAY_OF_MONTH),
                msgCal.get(Calendar.MONTH) + 1,
                msgCal.get(Calendar.YEAR)
            )
        }
    }
}

class ChatModel {

    private fun timestampAt(daysAgo: Int, hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private val localChats = mutableListOf(
        Chat(id = "1001", name = "张三", avatar = "https://randomuser.me/api/portraits/men/11.jpg", lastMessage = "你好，最近怎么样？周末有空一起打球吗", timestamp = timestampAt(0, 10, 32), unreadCount = 3, isPinned = true),
        Chat(id = "1002", name = "李四", avatar = "https://randomuser.me/api/portraits/women/12.jpg", lastMessage = "项目文档我已经发到群里了，记得查收", timestamp = timestampAt(0, 9, 15), unreadCount = 0, isPinned = true),
        Chat(id = "1003", name = "产品交流群", avatar = "https://randomuser.me/api/portraits/men/13.jpg", lastMessage = "王五: 新版本明天上线，大家加把劲", timestamp = timestampAt(1, 14, 30), unreadCount = 12, isPinned = false),
        Chat(id = "1004", name = "妈妈", avatar = "https://randomuser.me/api/portraits/women/14.jpg", lastMessage = "记得按时吃饭，别老熬夜", timestamp = timestampAt(1, 18, 45), unreadCount = 1, isPinned = false),
        Chat(id = "1005", name = "赵六", avatar = "https://randomuser.me/api/portraits/men/15.jpg", lastMessage = "[图片]", timestamp = timestampAt(2, 10, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1006", name = "技术分享小组", avatar = "https://randomuser.me/api/portraits/women/16.jpg", lastMessage = "本周分享主题：Compose 性能优化实践", timestamp = timestampAt(2, 15, 30), unreadCount = 99, isPinned = false),
        Chat(id = "1007", name = "孙七", avatar = "https://randomuser.me/api/portraits/men/17.jpg", lastMessage = "好的，收到，我处理一下", timestamp = timestampAt(8, 11, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1008", name = "周八", avatar = "https://randomuser.me/api/portraits/women/18.jpg", lastMessage = "[语音] 00:12", timestamp = timestampAt(9, 16, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1009", name = "吴九", avatar = "https://randomuser.me/api/portraits/men/19.jpg", lastMessage = "今天下班一起吃饭吗？", timestamp = timestampAt(0, 10, 0), unreadCount = 2, isPinned = false),
        Chat(id = "1010", name = "郑十", avatar = "https://randomuser.me/api/portraits/women/20.jpg", lastMessage = "好的，没问题", timestamp = timestampAt(0, 9, 45), unreadCount = 0, isPinned = false),
        Chat(id = "1011", name = "前端开发组", avatar = "https://randomuser.me/api/portraits/men/21.jpg", lastMessage = "小李: PR已经提交，麻烦review一下", timestamp = timestampAt(1, 16, 20), unreadCount = 8, isPinned = false),
        Chat(id = "1012", name = "闺蜜群", avatar = "https://randomuser.me/api/portraits/women/22.jpg", lastMessage = "周末去看电影吧", timestamp = timestampAt(1, 20, 15), unreadCount = 128, isPinned = true),
        Chat(id = "1013", name = "钱十一", avatar = "https://randomuser.me/api/portraits/men/23.jpg", lastMessage = "[视频]", timestamp = timestampAt(3, 14, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1014", name = "刘十二", avatar = "https://randomuser.me/api/portraits/women/24.jpg", lastMessage = "会议改到下午3点了", timestamp = timestampAt(3, 9, 30), unreadCount = 3, isPinned = false),
        Chat(id = "1015", name = "陈十三", avatar = "https://randomuser.me/api/portraits/men/25.jpg", lastMessage = "收到，已处理", timestamp = timestampAt(10, 13, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1016", name = "销售团队", avatar = "https://randomuser.me/api/portraits/women/26.jpg", lastMessage = "本月目标还差一点，大家加油", timestamp = timestampAt(10, 17, 0), unreadCount = 7, isPinned = false),
        Chat(id = "1017", name = "冯十四", avatar = "https://randomuser.me/api/portraits/men/27.jpg", lastMessage = "资料已经整理好了", timestamp = timestampAt(0, 10, 20), unreadCount = 1, isPinned = false),
        Chat(id = "1018", name = "陈十五", avatar = "https://randomuser.me/api/portraits/women/28.jpg", lastMessage = "好的，我来安排", timestamp = timestampAt(0, 9, 30), unreadCount = 0, isPinned = false),
        Chat(id = "1019", name = "运维组", avatar = "https://randomuser.me/api/portraits/men/29.jpg", lastMessage = "服务器已重启，恢复正常", timestamp = timestampAt(1, 8, 0), unreadCount = 4, isPinned = false),
        Chat(id = "1020", name = "同学聚会群", avatar = "https://randomuser.me/api/portraits/women/30.jpg", lastMessage = "下个月同学聚会，大家有空吗", timestamp = timestampAt(1, 21, 0), unreadCount = 23, isPinned = false),
        Chat(id = "1021", name = "褚十六", avatar = "https://randomuser.me/api/portraits/men/31.jpg", lastMessage = "[位置]", timestamp = timestampAt(4, 11, 30), unreadCount = 0, isPinned = false),
        Chat(id = "1022", name = "卫十七", avatar = "https://randomuser.me/api/portraits/women/32.jpg", lastMessage = "周末去爬山怎么样", timestamp = timestampAt(4, 19, 0), unreadCount = 6, isPinned = false),
        Chat(id = "1023", name = "蒋十八", avatar = "https://randomuser.me/api/portraits/men/33.jpg", lastMessage = "收到通知了", timestamp = timestampAt(11, 10, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1024", name = "沈十九", avatar = "https://randomuser.me/api/portraits/women/34.jpg", lastMessage = "文档已更新，请查收", timestamp = timestampAt(11, 15, 0), unreadCount = 2, isPinned = false),
        Chat(id = "1025", name = "市场部", avatar = "https://randomuser.me/api/portraits/men/35.jpg", lastMessage = "新活动方案已发群里", timestamp = timestampAt(0, 10, 15), unreadCount = 9, isPinned = false),
        Chat(id = "1026", name = "韩二十", avatar = "https://randomuser.me/api/portraits/women/36.jpg", lastMessage = "好的，谢谢", timestamp = timestampAt(0, 9, 50), unreadCount = 0, isPinned = false),
        Chat(id = "1027", name = "杨二十一", avatar = "https://randomuser.me/api/portraits/men/37.jpg", lastMessage = "[文件] 周报.docx", timestamp = timestampAt(1, 17, 0), unreadCount = 1, isPinned = false),
        Chat(id = "1028", name = "朱二十二", avatar = "https://randomuser.me/api/portraits/women/38.jpg", lastMessage = "今天加班吗", timestamp = timestampAt(1, 19, 30), unreadCount = 3, isPinned = false),
        Chat(id = "1029", name = "胡二十三", avatar = "https://randomuser.me/api/portraits/men/39.jpg", lastMessage = "不加班，早点回家", timestamp = timestampAt(5, 10, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1030", name = "林二十四", avatar = "https://randomuser.me/api/portraits/women/40.jpg", lastMessage = "明天放假吗", timestamp = timestampAt(5, 14, 0), unreadCount = 5, isPinned = false),
        Chat(id = "1031", name = "何二十五", avatar = "https://randomuser.me/api/portraits/men/41.jpg", lastMessage = "正常上班", timestamp = timestampAt(12, 9, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1032", name = "研发部", avatar = "https://randomuser.me/api/portraits/women/42.jpg", lastMessage = "代码review会议下午2点", timestamp = timestampAt(12, 16, 0), unreadCount = 11, isPinned = false),
        Chat(id = "1033", name = "郭二十六", avatar = "https://randomuser.me/api/portraits/men/43.jpg", lastMessage = "收到，准时参加", timestamp = timestampAt(0, 10, 25), unreadCount = 0, isPinned = false),
        Chat(id = "1034", name = "马二十七", avatar = "https://randomuser.me/api/portraits/women/44.jpg", lastMessage = "[图片]", timestamp = timestampAt(0, 9, 40), unreadCount = 4, isPinned = false),
        Chat(id = "1035", name = "罗二十八", avatar = "https://randomuser.me/api/portraits/men/45.jpg", lastMessage = "周末愉快", timestamp = timestampAt(1, 22, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1036", name = "黄二十九", avatar = "https://randomuser.me/api/portraits/women/46.jpg", lastMessage = "你也是", timestamp = timestampAt(1, 20, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1037", name = "梁三十", avatar = "https://randomuser.me/api/portraits/men/47.jpg", lastMessage = "[语音] 00:35", timestamp = timestampAt(6, 11, 0), unreadCount = 2, isPinned = false),
        Chat(id = "1038", name = "唐三十一", avatar = "https://randomuser.me/api/portraits/women/48.jpg", lastMessage = "今天天气不错", timestamp = timestampAt(6, 15, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1039", name = "行政部", avatar = "https://randomuser.me/api/portraits/men/49.jpg", lastMessage = "下周体检，请大家安排时间", timestamp = timestampAt(13, 10, 0), unreadCount = 14, isPinned = false),
        Chat(id = "1040", name = "贾三十二", avatar = "https://randomuser.me/api/portraits/women/50.jpg", lastMessage = "知道了", timestamp = timestampAt(13, 14, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1041", name = "丁三十三", avatar = "https://randomuser.me/api/portraits/men/51.jpg", lastMessage = "晚上一起吃饭", timestamp = timestampAt(0, 10, 5), unreadCount = 1, isPinned = false),
        Chat(id = "1042", name = "宋三十四", avatar = "https://randomuser.me/api/portraits/women/52.jpg", lastMessage = "好的，哪里见", timestamp = timestampAt(0, 9, 55), unreadCount = 0, isPinned = false),
        Chat(id = "1043", name = "采购组", avatar = "https://randomuser.me/api/portraits/men/53.jpg", lastMessage = "办公用品已到货", timestamp = timestampAt(1, 15, 0), unreadCount = 6, isPinned = false),
        Chat(id = "1044", name = "袁三十五", avatar = "https://randomuser.me/api/portraits/women/54.jpg", lastMessage = "辛苦了", timestamp = timestampAt(1, 18, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1045", name = "许三十六", avatar = "https://randomuser.me/api/portraits/men/55.jpg", lastMessage = "[文件] 报价单.xlsx", timestamp = timestampAt(2, 16, 30), unreadCount = 3, isPinned = false),
        Chat(id = "1046", name = "何三十七", avatar = "https://randomuser.me/api/portraits/women/56.jpg", lastMessage = "收到，正在看", timestamp = timestampAt(3, 10, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1047", name = "财务部", avatar = "https://randomuser.me/api/portraits/men/57.jpg", lastMessage = "工资已发放，请查收", timestamp = timestampAt(8, 14, 0), unreadCount = 17, isPinned = false),
        Chat(id = "1048", name = "谢三十八", avatar = "https://randomuser.me/api/portraits/women/58.jpg", lastMessage = "收到，谢谢", timestamp = timestampAt(9, 11, 0), unreadCount = 0, isPinned = false),
        Chat(id = "1049", name = "王三十九", avatar = "https://randomuser.me/api/portraits/men/59.jpg", lastMessage = "明天开会", timestamp = timestampAt(0, 10, 10), unreadCount = 2, isPinned = false),
        Chat(id = "1050", name = "周四十", avatar = "https://randomuser.me/api/portraits/women/60.jpg", lastMessage = "几点？", timestamp = timestampAt(0, 10, 8), unreadCount = 0, isPinned = false)
    )

    suspend fun getChatsFromLocal(): List<Chat> = withContext(Dispatchers.IO) {
        delay(400)
        sortWithPinnedTop(localChats.toList())
    }

    fun pinLocally(chatId: String): List<Chat> {
        val index = localChats.indexOfFirst { it.id == chatId }
        if (index >= 0) {
            localChats[index] = localChats[index].copy(isPinned = true)
        }
        return sortWithPinnedTop(localChats.toList())
    }

    fun unpinLocally(chatId: String): List<Chat> {
        val index = localChats.indexOfFirst { it.id == chatId }
        if (index >= 0) {
            localChats[index] = localChats[index].copy(isPinned = false)
        }
        return sortWithPinnedTop(localChats.toList())
    }

    fun deleteLocally(chatId: String): List<Chat> {
        localChats.removeAll { it.id == chatId }
        return sortWithPinnedTop(localChats.toList())
    }

    fun restoreLocally(snapshot: List<Chat>): List<Chat> {
        localChats.clear()
        localChats.addAll(snapshot)
        return sortWithPinnedTop(localChats.toList())
    }

    suspend fun pinChatOnTim(chatId: String): Boolean = withContext(Dispatchers.IO) {
        delay(1000)
        (0..9).random() < 8
    }

    suspend fun unpinChatOnTim(chatId: String): Boolean = withContext(Dispatchers.IO) {
        delay(1000)
        (0..9).random() < 8
    }

    suspend fun deleteChatOnTim(chatId: String): Boolean = withContext(Dispatchers.IO) {
        delay(1000)
        (0..9).random() < 8
    }

    suspend fun fetchChatsFromTim(): List<Chat> = withContext(Dispatchers.IO) {
        delay(1500)
        val updatedChats = localChats.map { chat ->
            if ((0..9).random() < 3) {
                chat.copy(
                    lastMessage = when ((0..4).random()) {
                        0 -> "新消息来了"
                        1 -> "[图片]"
                        2 -> "[语音] 00:15"
                        3 -> "[文件]"
                        else -> "收到，好的"
                    },
                    timestamp = System.currentTimeMillis() - (0..3599).random() * 1000L,
                    unreadCount = chat.unreadCount + (0..5).random()
                )
            } else {
                chat
            }
        }
        localChats.clear()
        localChats.addAll(updatedChats)
        sortWithPinnedTop(localChats.toList())
    }

    private fun sortWithPinnedTop(chats: List<Chat>): List<Chat> {
        return chats.sortedWith(
            compareByDescending<Chat> { it.isPinned }
                .thenBy { it.id }
        )
    }
}