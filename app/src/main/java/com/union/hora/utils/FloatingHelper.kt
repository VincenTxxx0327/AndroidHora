package com.union.hora.utils

import android.app.Service
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout


/**
 *
 * @Author： VincenT
 * @Time： 2023/11/6 18:27
 */
class FloatingHelper(context: Context) {
    private var mWindowManager: WindowManager?
    private var mChildViewMap: MutableMap<View, WindowManager.LayoutParams>? = null

    init {
        mWindowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager?
    }

    /**
     * 创建模板 WindowManager.LayoutParams 对象
     */
    private fun createLayoutParams(): WindowManager.LayoutParams {
        val layoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams.format = PixelFormat.RGBA_8888
        layoutParams.gravity = Gravity.START or Gravity.TOP
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        return layoutParams
    }

    /**
     * 添加并显示悬浮View
     *
     * @param view 要悬浮的View
     */
    fun addView(view: View) {
        if (mWindowManager == null) {
            return
        }
        if (mChildViewMap == null) {
            mChildViewMap = HashMap<View, WindowManager.LayoutParams>()
        }
        if (!mChildViewMap!!.containsKey(view)) {
            val layoutParams = createLayoutParams()
            mChildViewMap!![view] = layoutParams
            mWindowManager!!.addView(view, layoutParams)
        }
    }

    /**
     * 添加并（在指定坐标位置）显示悬浮View
     *
     * @param view 要悬浮的View
     */
    fun addView(view: View, x: Int, y: Int) {
        if (mWindowManager == null) {
            return
        }
        if (mChildViewMap == null) {
            mChildViewMap = HashMap<View, WindowManager.LayoutParams>()
        }
        if (!mChildViewMap!!.containsKey(view)) {
            val layoutParams = createLayoutParams()
            layoutParams.x = x
            layoutParams.y = y
            mChildViewMap!![view] = layoutParams
            mWindowManager!!.addView(view, layoutParams)
        }
    }

    /**
     * 添加并显示悬浮View
     *
     * @param view    要悬浮的View
     * @param canMove 是否可以拖动
     */
    fun addView(view: View, canMove: Boolean) {
        if (mWindowManager == null) {
            return
        }
        if (mChildViewMap == null) {
            mChildViewMap = HashMap<View, WindowManager.LayoutParams>()
        }
        if (!mChildViewMap!!.containsKey(view)) {
            val layoutParams = createLayoutParams()
            mChildViewMap!![view] = layoutParams
            mWindowManager!!.addView(view, layoutParams)
            if (canMove) {
                view.setOnTouchListener(FloatingOnTouchListener())
            }
        }
    }

    /**
     * 添加并（在指定坐标位置）显示悬浮View
     *
     * @param view    要悬浮的View
     * @param canMove 是否可以拖动
     */
    fun addView(view: View, x: Int, y: Int, canMove: Boolean) {
        if (mWindowManager == null) {
            return
        }
        if (mChildViewMap == null) {
            mChildViewMap = HashMap<View, WindowManager.LayoutParams>()
        }
        if (!mChildViewMap!!.containsKey(view)) {
            val layoutParams = createLayoutParams()
            layoutParams.x = x
            layoutParams.y = y
            mChildViewMap!![view] = layoutParams
            mWindowManager!!.addView(view, layoutParams)
            if (canMove) {
                view.setOnTouchListener(FloatingOnTouchListener())
            }
        }
    }

    /**
     * 添加并显示悬浮View，自行提供WindowManager.LayoutParams对象
     *
     * @param view         要悬浮的View
     * @param layoutParams WindowManager.LayoutParams 对象
     */
    fun addView(view: View, layoutParams: WindowManager.LayoutParams) {
        if (mWindowManager == null) {
            return
        }
        if (mChildViewMap == null) {
            mChildViewMap = HashMap<View, WindowManager.LayoutParams>()
        }
        if (!mChildViewMap!!.containsKey(view)) {
            mChildViewMap!![view] = layoutParams
            mWindowManager!!.addView(view, layoutParams)
        }
    }

    /**
     * 判断是否存在该悬浮View
     */
    operator fun contains(view: View): Boolean {
        return if (mChildViewMap != null) {
            mChildViewMap!!.containsKey(view)
        } else false
    }

    /**
     * 移除指定悬浮View
     */
    fun removeView(view: View) {
        if (mWindowManager == null) {
            return
        }
        if (mChildViewMap != null) {
            mChildViewMap!!.remove(view)
        }
        mWindowManager!!.removeView(view)
    }

    /**
     * 根据 LayoutParams 更新悬浮 View 布局
     */
    fun updateViewLayout(view: View, layoutParams: WindowManager.LayoutParams) {
        if (mWindowManager == null) {
            return
        }
        if (mChildViewMap != null && mChildViewMap!!.containsKey(view)) {
            mChildViewMap!![view] = layoutParams
            mWindowManager!!.updateViewLayout(view, layoutParams)
        }
    }

    /**
     * 获取指定悬浮View的 LayoutParams
     */
    fun getLayoutParams(view: View): WindowManager.LayoutParams? {
        return if (mChildViewMap!!.containsKey(view)) {
            mChildViewMap!![view]
        } else null
    }

    fun clear() {
        if (mWindowManager == null) {
            return
        }
        for (view in mChildViewMap!!.keys) {
            mWindowManager!!.removeView(view)
        }
        mChildViewMap!!.clear()
    }

    fun destroy() {
        if (mWindowManager == null) {
            return
        }
        for (view in mChildViewMap!!.keys) {
            mWindowManager!!.removeView(view)
        }
        mChildViewMap!!.clear()
        mChildViewMap = null
        mWindowManager = null
    }

    /**
     * 处理触摸事件实现悬浮View拖动效果
     */
    inner class FloatingOnTouchListener : View.OnTouchListener {
        private var x = 0
        private var y = 0
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (mChildViewMap!!.containsKey(view)) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        x = event.rawX.toInt()
                        y = event.rawY.toInt()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val nowX = event.rawX.toInt()
                        val nowY = event.rawY.toInt()
                        val movedX = nowX - x
                        val movedY = nowY - y
                        x = nowX
                        y = nowY
                        val layoutParams = mChildViewMap!![view]
                        layoutParams!!.x = layoutParams.x + movedX
                        layoutParams.y = layoutParams.y + movedY
                        if (layoutParams.x < 0) {
                            layoutParams.x = 0
                        }
                        if (layoutParams.y < 0) {
                            layoutParams.y = 0
                        }
                        mWindowManager!!.updateViewLayout(view, layoutParams)
                    }

                    else -> {}
                }
            }
            return view.onTouchEvent(event)
        }
    }

    companion object {
        /**
         * 判断是否拥有悬浮窗权限
         *
         * @param isApplyAuthorization 是否申请权限
         */
        fun canDrawOverlays(context: Context, isApplyAuthorization: Boolean): Boolean {
            //Android 6.0 以下无需申请权限
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //判断是否拥有悬浮窗权限，无则跳转悬浮窗权限授权页面
                if (Settings.canDrawOverlays(context)) {
                    true
                } else {
                    if (isApplyAuthorization) {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName))
                        if (context is Service) {
                            intent.flags = FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                        false
                    } else {
                        false
                    }
                }
            } else {
                true
            }
        }
    }
}