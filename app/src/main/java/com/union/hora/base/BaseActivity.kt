
package com.union.hora.base

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.union.hora.app.receiver.NetworkChangeReceiver
import com.union.hora.R
import com.union.hora.app.constant.Constant
import com.union.hora.app.ext.immersiveStatusBar
import com.union.hora.app.ext.setLightStatusBar
import com.union.hora.util.KeyBoardUtil
import com.union.hora.util.Preference

import org.greenrobot.eventbus.EventBus

abstract class BaseActivity : AppCompatActivity(), ActivityAction {

    protected var hasLogin: Boolean by Preference(Constant.KEY_LOGIN_OUT, false)
    protected var hasNetwork: Boolean by Preference(Constant.KEY_HAS_NETWORK, true)

    protected var mUseBinding: Boolean = false
    protected var mNetworkChangeReceiver: NetworkChangeReceiver? = null

    protected lateinit var mTipView: View
    protected lateinit var mWindowManager: WindowManager
    protected lateinit var mLayoutParams: WindowManager.LayoutParams

    open fun enableEventBus(): Boolean = true

    open fun enableNetworkTip(): Boolean = true

    abstract fun initLayoutRes(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // TODO 未做 安卓15及以上，需要适配全面屏 Fragment的fitSystemWindows需要设置为false并针对布局做padding处理
//            immersiveAbove15NavigationBar() // 沉浸式导航栏
        } else {
            immersiveStatusBar() // 沉浸式状态栏
            setLightStatusBar(true) // 设置浅色状态栏背景（文字为深色）
//            immersiveNavigationBar() // 沉浸式导航栏
//            setLightNavigationBar(true) // 设置浅色导航栏背景（文字为深色）
//            navigationBarHeightLiveData.observe(this) {
//                // 监听导航栏高度变化
//            }
        }
        super.onCreate(savedInstanceState)
        if (enableEventBus()) {
            EventBus.getDefault().register(this)
        }
        if (!mUseBinding) {
            setContentView(initLayoutRes())
        } else {
            initToolbar()
            initView()
            initData()
            initListener()
            initTipView()
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
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
//        mThemeColor = SettingUtil.getColor()
//        StatusBarUtil.setColor(this, mThemeColor, 0)
//        if (SettingUtil.getNavBar()) {
//            window.navigationBarColor = CircleView.shiftColorDown(mThemeColor)
//        } else {
//            window.statusBarColor = Color.argb(125, 255, 255, 255)
//            window.navigationBarColor = Color.argb(125, 255, 255, 255)
//        }
    }

    /**
     * 初始化 TipView
     */
    @SuppressLint("InflateParams")
    override fun initTipView() {
        mTipView = layoutInflater.inflate(R.layout.layout_network_tip, null)
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
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
            val send = findViewById<TextView>(R.id.tv_send)
            if (send == null) {
                // 如果不是落在EditText区域，则需要关闭输入法
                if (KeyBoardUtil.isHideKeyboard(v, ev)) {
                    KeyBoardUtil.hideKeyBoard(this, v)
                }
            } else {
                if (v != null && KeyBoardUtil.isHideKeyboard(send, ev) && KeyBoardUtil.isHideKeyboard(v, ev)) {
                    KeyBoardUtil.hideKeyBoard(this, v)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("GestureBackNavigation")
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
        if (::mTipView.isInitialized && mTipView?.parent != null) {
            mWindowManager.removeView(mTipView)
        }
        if (enableEventBus()) {
            EventBus.getDefault().unregister(this)
        }
//        CommonUtil.fixInputMethodManagerLeak(this)
//        AppWatcher.objectWatcher.expectWeaklyReachable(this, "baseActivity")
        super.onDestroy()

    }

    fun showRetryDialog(retryCount: Int, onGranted: () -> Unit, onDenied: () -> Unit = {}) {
        val message = when (retryCount) {
            1 -> "This is the second request for camera permission; the photo-taking function requires this permission."
            2 -> "This is the last request for camera permission. Please grant it to use full functionality."
            else -> "Camera permissions are required to use the photo-taking function."
        }

        AlertDialog.Builder(this)
            .setTitle("Camera permissions required")
            .setMessage(message)
            .setPositiveButton("Allow") { _, _ ->
                onGranted.invoke()
            }
            .setNegativeButton("Denied") { _, _ ->
                onDenied.invoke()
            }
            .show()
    }

    protected fun handlePermissionResult(isGranted: Boolean, callback: () -> Unit) {
        if (isGranted) {
            callback.invoke()
        }
    }

    private fun openCamera() {
        // 打开相机逻辑
    }

    /**
     * 显示第一次拒绝后的对话框
     */
    fun showFirstDeniedDialog(onPositive: () -> Unit, onNegative: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Camera permissions required")
            .setMessage("The camera function requires camera permissions to function properly. Please grant the permission request.")
            .setPositiveButton("Allow") { _, _ -> onPositive() }
            .setNegativeButton("Denied") { _, _ -> onNegative() }
            .setCancelable(false)
            .show()
    }

    /**
     * 显示第二次拒绝后的对话框
     */
    fun showSecondDeniedDialog(onPositive: () -> Unit, onNegative: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Camera permission denied")
            .setMessage("This is the last request for camera permission. Without this permission, you will not be able to use the photo-taking function.")
            .setPositiveButton("Allow") { _, _ -> onPositive() }
            .setNegativeButton("Denied") { _, _ -> onNegative() }
            .setCancelable(false)
            .show()
    }

    /**
     * 显示永久拒绝对话框
     */
    fun showPermanentlyDeniedDialog(onPositive: () -> Unit, onNegative: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Camera permissions permanently denied")
            .setMessage("You have permanently denied camera access. To use the photo function, please manually enable it in the settings.")
            .setPositiveButton("Settings") { _, _ -> onPositive() }
            .setNegativeButton("Cancel") { _, _ -> onNegative() }
            .setCancelable(false)
            .show()
    }

    /**
     * 显示自定义消息的权限对话框
     */
    fun showCustomRetryDialog(
        title: String,
        message: String,
        positiveText: String = "Allow",
        negativeText: String = "Denied",
        onPositive: () -> Unit,
        onNegative: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { _, _ -> onPositive() }
            .setNegativeButton(negativeText) { _, _ -> onNegative() }
            .setCancelable(false)
            .show()
    }
}