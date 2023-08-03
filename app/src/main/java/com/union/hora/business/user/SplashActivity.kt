package com.union.hora.business.user

import android.annotation.SuppressLint
import android.content.Intent
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.union.hora.R
import com.union.hora.base.BaseActivity
import com.union.hora.home.MainActivity
import kotlinx.android.synthetic.main.activity_splash.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private var alphaAnimation: AlphaAnimation? = null

    override fun initLayoutRes(): Int = R.layout.activity_splash

    override fun enableEventBus(): Boolean = false

    override fun enableNetworkTip(): Boolean = false

    override fun initToolbar() {

    }

    override fun initView() {
        alphaAnimation = AlphaAnimation(0.3F, 1.0F)
        alphaAnimation?.run {
            duration = 3000
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    jumpToMain()
                }

                override fun onAnimationStart(p0: Animation?) {
                }
            })
        }
        layout_splash.startAnimation(alphaAnimation)
    }

    override fun initData() {
    }

    override fun initListener() {
    }

    override fun initColor() {
        super.initColor()
        layout_splash.setBackgroundColor(mThemeColor)
    }

    fun jumpToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

}
