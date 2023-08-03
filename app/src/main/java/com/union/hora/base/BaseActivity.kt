@file:Suppress("DEPRECATION")

package com.union.hora.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.color.CircleView
import com.union.hora.R
import com.union.hora.app.constant.Constant
import com.union.hora.app.receiver.NetworkChangeReceiver
import com.union.hora.utils.*
import com.union.hora.widget.MultipleStatusView
import kotlinx.android.synthetic.main.toolbar_base.*
import leakcanary.AppWatcher
import org.greenrobot.eventbus.EventBus

abstract class BaseActivity : AppCompatActivity(), ActivityAction {

    protected var hasLogin: Boolean by Preference(Constant.LOGIN_KEY, false)
    protected var hasNetwork: Boolean by Preference(Constant.HAS_NETWORK_KEY, true)

    protected var mThemeColor: Int = SettingUtil.getColor()
    protected var mLayoutStatusView: MultipleStatusView? = null
    protected var mNetworkChangeReceiver: NetworkChangeReceiver? = null

    protected lateinit var mTipView: View
    protected lateinit var mWindowManager: WindowManager
    protected lateinit var mLayoutParams: WindowManager.LayoutParams

    open fun enableEventBus(): Boolean = true

    open fun enableNetworkTip(): Boolean = true

    abstract fun initLayoutRes(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        super.onCreate(savedInstanceState)
        if (enableEventBus()) {
            EventBus.getDefault().register(this)
        }
        setContentView(initLayoutRes())
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
//        StatusBarUtils.SetStatusBarByPureColor(this)
        initToolbar()
        initView()
        initData()
        initListener()
        initTipView()
    }

    override fun onResume() {
        val filter = IntentFilter()
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
        mNetworkChangeReceiver = NetworkChangeReceiver()
        registerReceiver(mNetworkChangeReceiver, filter)
        super.onResume()
        initColor()
    }

    override fun initColor() {
        mThemeColor = SettingUtil.getColor()
        StatusBarUtil.setColor(this, mThemeColor, 0)
        if (SettingUtil.getNavBar()) {
            window.navigationBarColor = CircleView.shiftColorDown(mThemeColor)
        } else {
            window.navigationBarColor = Color.argb(125, 255, 255, 255)
        }
    }

    /**
     * 初始化 TipView
     */
    @SuppressLint("InflateParams")
    override fun initTipView() {
        mTipView = layoutInflater.inflate(R.layout.layout_network_tip, null)
        mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mLayoutParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )
        mLayoutParams.gravity = Gravity.TOP
        mLayoutParams.x = 0
        mLayoutParams.y = 0
        mLayoutParams.windowAnimations = R.style.anim_float_view // add animations
    }

    override fun initNetwork(isConnected: Boolean) {
        if (enableNetworkTip()) {
            if (isConnected) {
                if (mTipView?.parent != null) {
                    mWindowManager.removeView(mTipView)
                }
            } else {
                if (mTipView?.parent == null) {
                    mWindowManager.addView(mTipView, mLayoutParams)
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP) {
            val v = currentFocus
            // 如果不是落在EditText区域，则需要关闭输入法
            if (KeyBoardUtil.isHideKeyboard(v, ev)) {
                KeyBoardUtil.hideKeyBoard(this, v)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val count = supportFragmentManager.backStackEntryCount
        if (count == 0) super.onBackPressed() else supportFragmentManager.popBackStack()
    }

    override fun onPause() {
        if (mNetworkChangeReceiver != null) {
            unregisterReceiver(mNetworkChangeReceiver)
            mNetworkChangeReceiver = null
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (mTipView?.parent != null) {
            mWindowManager.removeView(mTipView)
        }
        if (enableEventBus()) {
            EventBus.getDefault().unregister(this)
        }
//        CommonUtil.fixInputMethodManagerLeak(this)
        AppWatcher.objectWatcher.expectWeaklyReachable(this, "baseActivity")
        super.onDestroy()

    }

}