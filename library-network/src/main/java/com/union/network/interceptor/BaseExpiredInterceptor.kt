package com.union.network.interceptor

import com.union.network.utils.HttpLog
import com.union.network.utils.HttpUtil.UTF8
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.nio.charset.Charset

/**
 * 判断响应是否有效的处理
 * 继承后扩展各种无效响应处理：包括token过期、账号异地登录、时间戳过期、签名sign错误等
 * @Author： VincenT
 * @Time： 2023/8/15 20:59
 */
abstract class BaseExpiredInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val response: Response = chain.proceed(request)
        val responseBody = response.body
        val source = responseBody.source()
        source.request(Long.MAX_VALUE) // Buffer the entire body.
        val buffer = source.buffer
        var charset: Charset = UTF8
        val contentType = responseBody.contentType()
        if (contentType != null) {
            charset = contentType.charset(UTF8) ?: UTF8
        }
        val bodyString = buffer.clone().readString(charset)
        HttpLog.i("网络拦截器:" + bodyString + " host:" + request.url.toString())
        val isText = isText(contentType)
        if (!isText) {
            return response
        }
        //判断响应是否过期（无效）
        return if (isResponseExpired(response, bodyString)) {
            responseExpired(chain, bodyString)
        } else response
    }

    private fun isText(mediaType: MediaType?): Boolean {
        if (mediaType?.type == "text" || mediaType?.subtype == "json") {
            return true
        }
        return false
    }

    /**
     * 处理响应是否有效
     */
    abstract fun isResponseExpired(response: Response?, bodyString: String?): Boolean

    /**
     * 无效响应处理
     */
    abstract fun responseExpired(chain: Interceptor.Chain?, bodyString: String?): Response
}