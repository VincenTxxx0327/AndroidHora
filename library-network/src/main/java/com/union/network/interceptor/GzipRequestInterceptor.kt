package com.union.network.interceptor

import com.union.network.model.HttpHeaders.Companion.HEAD_KEY_ACCEPT_ENCODING
import com.union.network.model.HttpHeaders.Companion.HEAD_KEY_CONTENT_ENCODING
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.GzipSink
import okio.buffer
import java.io.IOException

/**
 * post数据进行gzip后发送给服务器
 * okhttp内部默认启用了gzip,此选项是针对需要对post数据进行gzip后发送给服务器的,如服务器不支持,请勿开启
 * @Author： VincenT
 * @Time： 2023/8/15 21:15
 */
@Deprecated("")
class GzipRequestInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        if (originalRequest.body == null || originalRequest.header(HEAD_KEY_CONTENT_ENCODING) != null) {
            return chain.proceed(originalRequest)
        }
        val compressedRequest = originalRequest.newBuilder()
            .header(HEAD_KEY_ACCEPT_ENCODING, "gzip")
            .method(originalRequest.method, gzip(originalRequest.body))
            .build()
        return chain.proceed(compressedRequest)
    }

    private fun gzip(body: RequestBody?): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return body!!.contentType()
            }

            override fun contentLength(): Long {
                return -1
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                val gzipSink = GzipSink(sink).buffer()
                body!!.writeTo(gzipSink)
                gzipSink.close()
            }
        }
    }
}