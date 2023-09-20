package com.union.network.interceptor

import com.union.network.model.HttpHeaders
import com.union.network.utils.HttpLog
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * 配置公共头部
 * @Author： VincenT
 * @Time： 2023/8/15 21:16
 */
class HeadersInterceptor(headers: HttpHeaders) : Interceptor {
    private val headers: HttpHeaders

    init {
        this.headers = headers
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder: Request.Builder = chain.request().newBuilder()
        if (headers.headersMap?.isEmpty() == true) return chain.proceed(builder.build())
        try {
            for (entry: Map.Entry<String, String> in headers.headersMap?.entries!!) {
                //去除重复的header
                //builder.removeHeader(entry.getKey());
                //builder.addHeader(entry.getKey(), entry.getValue()).build();
                builder.header(entry.key, entry.value).build()
            }
        } catch (e: Exception) {
            HttpLog.e(e)
        }
        return chain.proceed(builder.build())
    }
}