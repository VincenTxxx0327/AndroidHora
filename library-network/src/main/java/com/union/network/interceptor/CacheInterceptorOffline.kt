package com.union.network.interceptor

import com.blankj.utilcode.util.NetworkUtils
import com.union.network.model.HttpHeaders.Companion.HEAD_KEY_CACHE_CONTROL
import com.union.network.model.HttpHeaders.Companion.HEAD_KEY_PRAGMA
import com.union.network.utils.HttpLog
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * 支持离线缓存,使用OKHttp自带的缓存功能
 * 配置Okhttp的Cache<br>
 * 配置请求头中的cache-control或者统一处理所有请求的请求头
 * 云端配合设置响应头或者自己写拦截器修改响应头中cache-control
 * 列：
 * 在Retrofit中，我们可以通过@Headers来配置，如：
 *
 * @Headers("Cache-Control: public, max-age=3600)
 * @GET("merchants/{shopId}/icon")
 * Observable<ShopIconEntity> getShopIcon(@Path("shopId") long shopId);
 *
 * 如果你不想加入公共缓存，想单独对某个api进行缓存，可用Headers来实现
 *
 * 请参考网址：http://www.jianshu.com/p/9c3b4ea108a7
 * @Author： VincenT
 * @Time： 2023/8/15 21:14
 */
class CacheInterceptorOffline : CacheInterceptor {

    constructor() : super()
    constructor(cacheControlValue: String?) : super(cacheControlValue)
    constructor(cacheControlValue: String?, cacheOnlineControlValue: String?) : super(cacheControlValue, cacheOnlineControlValue)

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        if (!NetworkUtils.isConnected()) {
            HttpLog.i(" no network load cache:" + request.cacheControl.toString())
            request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
            val response: Response = chain.proceed(request)
            return response.newBuilder()
                .removeHeader(HEAD_KEY_PRAGMA)
                .removeHeader(HEAD_KEY_CACHE_CONTROL)
                .header(HEAD_KEY_CACHE_CONTROL, "public, only-if-cached, $cacheControlValueOffline")
                .build()
        }
        return chain.proceed(request)
    }
}