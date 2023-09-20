package com.union.network.interceptor

import android.text.TextUtils
import com.union.network.model.HttpHeaders.Companion.HEAD_KEY_CACHE_CONTROL
import com.union.network.model.HttpHeaders.Companion.HEAD_KEY_PRAGMA
import com.union.network.utils.HttpLog
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 设置缓存功能
 * @Author： VincenT
 * @Time： 2023/8/15 21:14
 */
open class CacheInterceptor(protected var cacheControlValueOffline: String?, protected var cacheControlValueOnline: String?) : Interceptor {

    @JvmOverloads
    constructor(cacheControlValue: String? = String.format("max-age=%d", maxStaleOnline)) : this(cacheControlValue, String.format("max-age=%d", maxStale))

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        val cacheControl = originalResponse.header(HEAD_KEY_CACHE_CONTROL)
        HttpLog.e(maxStaleOnline.toString() + "s load cache:" + cacheControl)
        return if (TextUtils.isEmpty(cacheControl) || cacheControl!!.contains("no-store")
            || cacheControl.contains("no-cache") || cacheControl.contains("must-revalidate")
            || cacheControl.contains("max-age") || cacheControl.contains("max-stale")
        ) {
            originalResponse.newBuilder()
                .removeHeader(HEAD_KEY_PRAGMA)
                .removeHeader(HEAD_KEY_CACHE_CONTROL)
                .header(HEAD_KEY_CACHE_CONTROL, "public, max-age=$maxStale")
                .build()
        } else {
            originalResponse
        }
    }

    companion object {
        //set cache times is 3 days
        protected const val maxStale = 60 * 60 * 24 * 3

        // read from cache for 60 s
        protected const val maxStaleOnline = 60
    }
}