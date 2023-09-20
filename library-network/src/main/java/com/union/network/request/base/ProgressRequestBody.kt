package com.union.network.request.base

import com.union.common.utils.launchMain
import com.union.network.body.ProgressResponseCallback
import com.union.network.model.Progress
import com.union.network.model.Progress.Companion.refreshProgress
import com.union.network.callback.listener.CallType
import com.union.network.utils.HttpLog
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**
 * 上传请求体
 * 1.具有上传进度回调通知功能
 * 2.防止频繁回调，上层无用的刷新
 * @Author： VincenT
 * @Time： 2023/8/18 14:40
 */
class ProgressRequestBody(
    private val requestBody: RequestBody,
    private val callback: CallType<*>?,
    private var progressCallBack: ProgressResponseCallback? = null
) : RequestBody() {

    override fun contentType(): MediaType? {
        return requestBody.contentType()
    }

    /**
     * 重写调用实际的响应体的contentLength
     */
    override fun contentLength(): Long {
        return try {
            requestBody.contentLength()
        } catch (e: IOException) {
            HttpLog.e(e.message)
            -1L
        }
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink: BufferedSink = countingSink.buffer()
        requestBody.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private inner class CountingSink(sink: Sink) : ForwardingSink(sink) {
        private var progress = Progress()

        init {
            progress.totalSize = contentLength()
        }

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            refreshProgress(progress, byteCount) { rspProgress ->
                launchMain {
                    callback?.uploadProgress(rspProgress)
                    progressCallBack?.onResponseProgress(rspProgress)
                }
            }
        }
    }

}