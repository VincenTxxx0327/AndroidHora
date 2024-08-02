package com.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.union.hora.R


/**
 *
 * @Author： VincenT
 * @Time： 2023/11/6 16:40
 */
class MusicFloatingView(context: Context?) : View(context) {
    private val TAG = MusicFloatingView::class.java.simpleName

    /**
     * 默认宽高与当前View实际宽高
     */
    private var mDefaultWidth = 0
    private var mDefaultHeight = 0
    private var mWidth = 0
    private var mHeight = 0

    /**
     * 当前View绘制相关
     */
    private var mPaint: Paint? = null
    private var mBitmap: Bitmap? = null
    private var mPorterDuffXfermode: PorterDuffXfermode? = null
    private var mDirection = Direction.right
    private var mOrientation = 0
    private var mWidthPixels = 0

    /**
     * 悬浮窗管理相关
     */
    private var mWindowManager: WindowManager? = null
    private var mLayoutParams: WindowManager.LayoutParams? = null
    private var mIsShow = false

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun init() {
        //悬浮窗管理相关
        mWindowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager?
        mLayoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mLayoutParams!!.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        mLayoutParams!!.format = PixelFormat.RGBA_8888
        mLayoutParams!!.gravity = Gravity.START or Gravity.TOP
        mLayoutParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        mLayoutParams!!.width = LinearLayout.LayoutParams.WRAP_CONTENT
        mLayoutParams!!.height = LinearLayout.LayoutParams.WRAP_CONTENT

        //当前View绘制相关
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPorterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.DST_ATOP)
        mBitmap = (resources.getDrawable(R.drawable.ic_message_voice_call) as BitmapDrawable).bitmap
        mDefaultHeight = 210
        mDefaultWidth = 210

        //记录当前屏幕方向和屏幕宽度
        recordScreenWidth()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measureSize(mDefaultWidth, heightMeasureSpec)
        mHeight = measureSize(mDefaultHeight, widthMeasureSpec)
        setMeasuredDimension(mWidth, mHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        //间隔和圆角
        val d = 20
        val r = 30
        //画透明色圆角背景
        mPaint!!.color = Color.parseColor("#D9E1E1E1")
        canvas.drawRoundRect(0f, 0f, mWidth.toFloat(), mHeight.toFloat(), r.toFloat(), r.toFloat(), mPaint!!)
        when (mDirection) {
            Direction.right -> {
                mPaint!!.xfermode = mPorterDuffXfermode
                canvas.drawRoundRect((mWidth / 2).toFloat(), 0f, mWidth.toFloat(), mHeight.toFloat(), 0f, 0f, mPaint!!)
            }

            Direction.left -> {
                mPaint!!.xfermode = mPorterDuffXfermode
                canvas.drawRoundRect(0f, 0f, (mWidth / 2).toFloat(), mHeight.toFloat(), 0f, 0f, mPaint!!)
            }

            Direction.move -> {}
            else -> {
                mPaint!!.xfermode = mPorterDuffXfermode
                canvas.drawRoundRect((mWidth / 2).toFloat(), 0f, mWidth.toFloat(), mHeight.toFloat(), 0f, 0f, mPaint!!)
            }
        }
        mPaint!!.xfermode = null
        //画实色圆角矩形
        mPaint!!.color = Color.WHITE
        canvas.drawRoundRect(d.toFloat(), d.toFloat(), (mWidth - d).toFloat(), (mHeight - d).toFloat(), r.toFloat(), r.toFloat(), mPaint!!)
        //居中填充icon
        canvas.drawBitmap(mBitmap!!, ((mWidth - mBitmap!!.width) / 2).toFloat(), ((mHeight - mBitmap!!.height) / 2).toFloat(), mPaint)
    }

    private var x = 0
    private var y = 0

    init {
        init()
    }
    private var duration = 0L
    /**
     * 处理触摸事件，实现拖动、形状变更和粘边效果
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mWindowManager != null) {
            if (getResources().getConfiguration().orientation !== mOrientation) {
                //屏幕方向翻转了，重新获取并记录屏幕宽度
                recordScreenWidth()
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                    duration = System.currentTimeMillis()
                }

                MotionEvent.ACTION_MOVE -> {
                    val nowX = event.rawX.toInt()
                    val nowY = event.rawY.toInt()
                    val movedX = nowX - x
                    val movedY = nowY - y
                    x = nowX
                    y = nowY
                    mLayoutParams!!.x = mLayoutParams!!.x + movedX
                    mLayoutParams!!.y = mLayoutParams!!.y + movedY
                    if (mLayoutParams!!.x < 0) {
                        mLayoutParams!!.x = 0
                    }
                    if (mLayoutParams!!.y < 0) {
                        mLayoutParams!!.y = 0
                    }
                    if (mDirection != Direction.move) {
                        mDirection = Direction.move
                        invalidate()
                    }
                    mWindowManager!!.updateViewLayout(this, mLayoutParams)
                }

                MotionEvent.ACTION_UP -> {
                    handleDirection(event.rawX.toInt(), event.rawY.toInt())
                    invalidate()
                    mWindowManager!!.updateViewLayout(this, mLayoutParams)
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - duration > 250) {
                        duration = 0L
                        return true
                    }
                }

                else -> {}
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 计算宽高
     */
    private fun measureSize(defaultSize: Int, measureSpec: Int): Int {
        var result = defaultSize
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        //UNSPECIFIED	父容器没有对当前View有任何限制，当前View可以任意取尺寸
        //EXACTLY	当前的尺寸就是当前View应该取的尺寸
        //AT_MOST	当前尺寸是当前View能取的最大尺寸
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize)
        }
        return result
    }

    /**
     * 记录当前屏幕方向和屏幕宽度
     */
    private fun recordScreenWidth() {
        mOrientation = getResources().getConfiguration().orientation
        val outMetrics = DisplayMetrics()
        mWindowManager!!.defaultDisplay.getMetrics(outMetrics)
        mWidthPixels = outMetrics.widthPixels
    }

    /**
     * 判定所处方向
     */
    private fun handleDirection(x: Int, y: Int) {
        if (x > mWidthPixels.div(2)) {
            mDirection = Direction.right
            mLayoutParams!!.x = mWidthPixels - measuredWidth
        } else {
            mDirection = Direction.left
            mLayoutParams!!.x = 0
        }
    }

    /**
     * show
     */
    fun show() {
        if (!mIsShow) {
            if (mLayoutParams!!.x == 0 && mLayoutParams!!.y == 0 && mDirection == Direction.right) {
                mLayoutParams!!.x = mWidthPixels - mDefaultWidth
                mLayoutParams!!.y = 0
            }
            if (mDirection == Direction.move) {
                handleDirection(mLayoutParams!!.x, mLayoutParams!!.y)
            }
            mWindowManager!!.addView(this, mLayoutParams)
            mIsShow = true
        }
    }

    /**
     * 调整悬浮窗位置
     * 根据提供坐标自动判断粘边
     */
    fun updateViewLayout(x: Int, y: Int) {
        if (mIsShow) {
            handleDirection(x, y)
            invalidate()
            mLayoutParams!!.y = y
            mWindowManager!!.updateViewLayout(this, mLayoutParams)
        }
    }

    /**
     * dismiss
     */
    fun dismiss() {
        if (mIsShow) {
            mWindowManager!!.removeView(this)
            mIsShow = false
        }
    }

    /**
     * 方向
     */
    enum class Direction {
        /**
         * 左、右、移动
         */
        left, right, move
    }
}