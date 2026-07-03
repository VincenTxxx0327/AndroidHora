package com.union.hora.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.VectorDrawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Size
import com.dylanc.longan.logInfo
import com.union.hora.R
import com.union.hora.ui.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 聊天消息通知工具类
 *
 * 每次收到新消息时推送通知：
 * - title: 聊天名称
 * - subtitle: 消息内容
 * - icon: 优先使用聊天头像，无头像时从6个SVG VectorDrawable中随机选取
 */
object NotificationHelper {

    private const val CHANNEL_ID = "chat_message_channel"
    private const val CHANNEL_NAME = "聊天消息"
    private const val CHANNEL_DESC = "接收新聊天消息的通知"

    /**
     * 6个通知大图标（无头像时的随机备选），每次选取与上次不同的
     */
    private val largeIconResIds = intArrayOf(
        R.drawable.ic_notif_blue,
        R.drawable.ic_notif_green,
        R.drawable.ic_notif_orange,
        R.drawable.ic_notif_purple,
        R.drawable.ic_notif_red,
        R.drawable.ic_notif_teal
    )

    private var lastIconIndex = -1

    /**
     * 显示消息通知
     *
     * @param context  上下文
     * @param title    通知标题（聊天名称）
     * @param subtitle 通知副标题（消息内容）
     * @param avatar   聊天头像URL，有值则加载为通知图标，空则使用随机SVG图标
     * @param notifId  通知ID（使用 chatId.hashCode 保证同一会话覆盖）
     */
    suspend fun showNotification(
        context: Context,
        title: String,
        subtitle: String,
        avatar: String,
        notifId: Int
    ) = withContext(Dispatchers.IO) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        ensureChannel(manager)

        // 尝试加载头像 Bitmap
        val avatarBitmap = if (avatar.isNotEmpty()) loadBitmapFromUrl(context, avatar) else null

        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSubText(subtitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(subtitle))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        // 无头像或加载失败：smallIcon 用白色轮廓，largeIcon 用随机SVG
        if (avatarBitmap != null) {
            // 有头像：smallIcon 和 largeIcon 都使用头像
            builder
                .setSmallIcon(R.drawable.ic_notif_small)
                .setLargeIcon(avatarBitmap)
        } else {
            val largeBitmap = vectorDrawableToBitmap(context, nextRandomIcon())
            builder
                .setSmallIcon(R.drawable.ic_notif_small)
                .setLargeIcon(largeBitmap)
        }

        manager.notify(notifId, builder.build())
    }

    private fun ensureChannel(manager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * 随机选取下一个图标，确保与上次不同
     */
    private fun nextRandomIcon(): Int {
        var index: Int
        do {
            index = (0 until largeIconResIds.size).random()
        } while (index == lastIconIndex && largeIconResIds.size > 1)
        lastIconIndex = index
        return largeIconResIds[index]
    }

    /**
     * 将 VectorDrawable 转换为 Bitmap
     */
    private fun vectorDrawableToBitmap(context: Context, resId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, resId) as VectorDrawable
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth.coerceAtLeast(1),
            drawable.intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * 通过 Coil 单例从 URL 加载 Bitmap，失败返回 null
     *
     * 使用 context.imageLoader（Coil 单例），与 Compose UI 的 SubcomposeAsyncImage 共享
     * OkHttpClient、内存缓存和磁盘缓存，确保网络配置一致。
     *
     * 关键：通过 bitmapConfig(ARGB_8888) 强制解码为软件位图，
     * 避免 Coil 默认的 HARDWARE 位图无法在软件 Canvas 上绘制的问题
     * （"skip drawing hardware bitmap on S/W canvas" 警告）。
     */
    private suspend fun loadBitmapFromUrl(context: Context, url: String): Bitmap? {
        return try {
            val loader = context.imageLoader
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(Size(96, 96))
                .bitmapConfig(Bitmap.Config.ARGB_8888)
                .build()
            when (val result = loader.execute(request)) {
                is SuccessResult -> {
                    result.drawable?.let { drawable ->
                        Bitmap.createBitmap(
                            drawable.intrinsicWidth.coerceAtLeast(1),
                            drawable.intrinsicHeight.coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        ).also { bitmap ->
                            val canvas = Canvas(bitmap)
                            drawable.setBounds(0, 0, canvas.width, canvas.height)
                            drawable.draw(canvas)
                        }
                    }
                }

                is ErrorResult -> {
                    logInfo("[notif] 头像加载失败: url=$url, error=${result.throwable.message}")
                    null
                }

                else -> null
            }
        } catch (e: Exception) {
            logInfo("[notif] 头像加载异常: url=$url, exception=${e.message}")
            null
        }
    }
}
