package com.union.hora.http

import com.union.hora.BuildConfig
import com.union.hora.app.App
import com.union.hora.app.constant.Constant
import com.union.hora.app.constant.HttpConstant
import com.union.hora.http.api.ApiService
import com.union.hora.http.factory.CommonGsonFactory
import com.union.hora.http.interceptor.CacheInterceptor
import com.union.hora.http.interceptor.HeaderInterceptor
import com.union.hora.http.interceptor.SaveCookieInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.io.File
import java.util.concurrent.TimeUnit


object CommonRetrofit {

    private var retrofit: Retrofit? = null

    val service: ApiService by lazy { getRetrofit()!!.create(ApiService::class.java) }

    private fun getRetrofit(): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder().baseUrl(Constant.BASE_URL)  // baseUrl
                .client(getOkHttpClient())
                .addConverterFactory(CommonGsonFactory.create())
//                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create()).build()
        }
        return retrofit
    }

    var headerInterceptor: Interceptor = Interceptor { chain ->
        val originalRequest: Request = chain.request()
        val requestBuilder: Request.Builder =
            originalRequest.newBuilder()
                .addHeader("Accept-Encoding", "gzip")
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .method(originalRequest.method, originalRequest.body)
        requestBuilder.addHeader("satoken",  "9617b4a5-d337-439c-beed-35a069b5e739") //添加请求头信息，服务器进行token有效性验证
        val request: Request = requestBuilder.build()
        chain.proceed(request)
    }


    /**
     * 获取 OkHttpClient
     */
    private fun getOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        //设置 请求的缓存的大小跟位置
        val cacheFile = File(App.context.cacheDir, "cache")
        val cache = Cache(cacheFile, HttpConstant.MAX_CACHE_SIZE)
        builder.run {
            addInterceptor(headerInterceptor)
            addInterceptor(httpLoggingInterceptor)
            addInterceptor(HeaderInterceptor())
            addInterceptor(SaveCookieInterceptor())
            addInterceptor(CacheInterceptor())
            cache(cache)  //添加缓存
            connectTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            retryOnConnectionFailure(true) // 错误重连
            // cookieJar(CookieManager())
        }
        return builder.build()
    }

}