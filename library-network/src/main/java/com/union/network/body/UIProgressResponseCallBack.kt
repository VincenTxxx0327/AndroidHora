package com.union.network.body

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.io.Serializable
import java.lang.ref.WeakReference

/**
 * 可以直接更新UI的回调
 * @Author： VincenT
 * @Time： 2023/8/15 2:03
 */
@Deprecated("")
abstract class UIProgressResponseCallBack : ProgressResponseCallback {
    //处理UI层的Handler子类
    private class UIHandler(looper: Looper?, uiProgressResponseListener: UIProgressResponseCallBack) : Handler(looper!!) {
        //弱引用
        private val uiProgressResponseCallBackWeakReference: WeakReference<UIProgressResponseCallBack>

        init {
            uiProgressResponseCallBackWeakReference = WeakReference(uiProgressResponseListener)
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                RESPONSE_UPDATE -> {
                    val uiProgressResponseCallBack = uiProgressResponseCallBackWeakReference.get()
                    if (uiProgressResponseCallBack != null) {
                        val progressModel = msg.obj as ProgressModel
                        uiProgressResponseCallBack.onUIResponseProgress(progressModel.currentBytes, progressModel.contentLength, progressModel.isDone)
                    }
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    //主线程Handler
    private val mHandler: Handler = UIHandler(Looper.getMainLooper(), this)
    @Deprecated("加入了Progress文件，弃用此方法")
    override fun onResponseProgress(bytesWritten: Long, contentLength: Long, done: Boolean) {
        //通过Handler发送进度消息
        val message = Message.obtain()
        message.obj = ProgressModel(bytesWritten, contentLength, done)
        message.what = RESPONSE_UPDATE
        mHandler.sendMessage(message)
    }

    /**
     * UI层回调抽象方法
     *
     * @param bytesRead     当前读取响应体字节长度
     * @param contentLength 总字节长度
     * @param done          是否读取完成
     */
    abstract fun onUIResponseProgress(bytesRead: Long, contentLength: Long, done: Boolean)
    inner class ProgressModel(//当前读取字节长度
        var currentBytes: Long, //总字节长度
        var contentLength: Long, done: Boolean
    ) : Serializable {

        //是否读取完成
        var isDone: Boolean
            private set

        init {
            contentLength = contentLength
            isDone = done
        }

        fun setCurrentBytes(currentBytes: Long): ProgressModel {
            this.currentBytes = currentBytes
            return this
        }

        fun setContentLength(contentLength: Long): ProgressModel {
            this.contentLength = contentLength
            return this
        }

        fun setDone(done: Boolean): ProgressModel {
            isDone = done
            return this
        }

        override fun toString(): String {
            return "ProgressModel{" +
                    "currentBytes=" + currentBytes +
                    ", contentLength=" + contentLength +
                    ", done=" + isDone +
                    '}'
        }
    }

    companion object {
        private const val RESPONSE_UPDATE = 0x02
    }
}