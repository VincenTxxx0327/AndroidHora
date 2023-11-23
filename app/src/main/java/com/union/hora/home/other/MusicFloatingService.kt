package com.union.hora.home.other

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.IntentFilter
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.union.hora.business.music.MusicPlayerActivity
import com.union.hora.widget.MusicFloatingView

/**
 *
 * @Author： VincenT
 * @Time： 2023/11/6 16:21
 */
class MusicFloatingService : Service() {

    companion object {
        private var mServiceVoice: MusicFloatingService? = null
        const val ACTION_SHOW_FLOATING = "action_show_floating"
        const val ACTION_DISMISS_FLOATING = "action_dismiss_floating"
        var isStart = false

        fun stopSelf() {
            mServiceVoice?.stopSelf()
            mServiceVoice = null
        }
    }

    private var mVoiceFloatingView: MusicFloatingView? = null

    /**
     * 监听本地广播显示或隐藏悬浮窗
     */
    private var mLocalBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (ACTION_SHOW_FLOATING == intent?.action) {
                mVoiceFloatingView?.show()
            } else if (ACTION_DISMISS_FLOATING == intent?.action) {
                mVoiceFloatingView?.dismiss()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        mServiceVoice = this
        isStart = true
        //初始化悬浮View
        mVoiceFloatingView = MusicFloatingView(this)
        //注册监听本地广播
        val intentFilter = IntentFilter(ACTION_SHOW_FLOATING)
        intentFilter.addAction(ACTION_DISMISS_FLOATING)
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mVoiceFloatingView?.show()
        mVoiceFloatingView?.setOnClickListener {
            val voiceActivityIntent = Intent(this@MusicFloatingService, MusicPlayerActivity::class.java)
            voiceActivityIntent.flags = FLAG_ACTIVITY_NEW_TASK
            startActivity(voiceActivityIntent)
            mVoiceFloatingView?.dismiss()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mVoiceFloatingView?.dismiss()
        mVoiceFloatingView = null
        isStart = false
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}