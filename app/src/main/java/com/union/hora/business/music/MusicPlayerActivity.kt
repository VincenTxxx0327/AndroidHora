package com.union.hora.business.music

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.union.hora.R
import com.union.hora.home.other.MusicFloatingService
import kotlinx.android.synthetic.main.activity_music_player.*

class MusicPlayerActivity : AppCompatActivity() {

    private var isStopService = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)
        iv_dismiss.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //判断是否拥有悬浮窗权限，无则跳转悬浮窗权限授权页面
                if (Settings.canDrawOverlays(this)) {
                    finish()
                } else {
                    //跳转悬浮窗权限授权页面
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                }
            } else {
                finish()
            }
        }
        iv_close.setOnClickListener {
            isStopService = true
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        dismissFloatingView()
    }

    override fun onPause() {
        super.onPause()
        if (isStopService) {
            MusicFloatingService.stopSelf()
        }else{
            showFloatingView()
        }
    }

    private fun dismissFloatingView(){
        if (MusicFloatingService.isStart) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(MusicFloatingService.ACTION_DISMISS_FLOATING))
        }
    }

    private fun showFloatingView() {
        if (MusicFloatingService.isStart) {
            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(MusicFloatingService.ACTION_SHOW_FLOATING))
        } else {
            startService(Intent(this, MusicFloatingService::class.java))
        }
    }
}