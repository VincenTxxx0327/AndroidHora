package com.union.network.interceptor

import com.union.network.model.HttpHeaders.Companion.HEAD_KEY_CACHE_CONTROL
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * 不加载缓存
 * 1.不适用Okhttp自带的缓存
 * @Author： VincenT
 * @Time： 2023/8/15 21:17
 */
class NoCacheInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        request = request.newBuilder().header(HEAD_KEY_CACHE_CONTROL, "no-cache").build()
        var originalResponse: Response = chain.proceed(request)
        originalResponse = originalResponse.newBuilder().header(HEAD_KEY_CACHE_CONTROL, "no-cache").build()
        return originalResponse
    }
}