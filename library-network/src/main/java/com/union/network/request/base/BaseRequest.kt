package com.union.network.request.base

import android.annotation.SuppressLint
import com.union.common.utils.getAppComponent
import com.union.network.EasyHttp
import com.union.network.EasyHttp.Companion.getRetrofitBuilder
import com.union.network.api.ApiService
import com.union.network.cache.RxCache
import com.union.network.cache.converter.IDiskConverter
import com.union.network.cache.model.CacheMode
import com.union.network.cache.model.CacheMode.CACHEANDREMOTE
import com.union.network.cache.model.CacheMode.CACHEANDREMOTEDISTINCT
import com.union.network.cache.model.CacheMode.DEFAULT
import com.union.network.cache.model.CacheMode.FIRSTCACHE
import com.union.network.cache.model.CacheMode.FIRSTREMOTE
import com.union.network.cache.model.CacheMode.NO_CACHE
import com.union.network.cache.model.CacheMode.ONLYCACHE
import com.union.network.cache.model.CacheMode.ONLYREMOTE
import com.union.network.callback.AbsCallback
import com.union.network.https.HttpsUtils
import com.union.network.https.HttpsUtils.getSslSocketFactory
import com.union.network.interceptor.BaseDynamicInterceptor
import com.union.network.interceptor.CacheInterceptor
import com.union.network.interceptor.CacheInterceptorOffline
import com.union.network.interceptor.HeadersInterceptor
import com.union.network.interceptor.NoCacheInterceptor
import com.union.network.model.HttpHeaders
import com.union.network.model.HttpParams
import com.union.network.request.RequestCall
import com.union.network.utils.HttpLog
import com.union.network.utils.RxUtil
import io.reactivex.rxjava3.core.Observable
import okhttp3.Cache
import okhttp3.Cookie
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.File
import java.io.InputStream
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import kotlin.math.max

/**
 * 所有请求的基类
 * @Author： VincenT
 * @Time： 2023/8/15 22:02
 */
@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
abstract class BaseRequest<R : BaseRequest<R>>(url: String) {

    internal var readTimeOut: Long = 0                          //读超时
    internal var writeTimeOut: Long = 0                         //写超时
    internal var connectTimeout: Long = 0                       //链接超时
    internal var proxy: Proxy? = null                           //代理
    internal var hostnameVerifier: HostnameVerifier? = null     //使用 verify 函数效验服务器主机名的合法性
    internal var sslParams: HttpsUtils.SSLParams? = null        //获取 SSLSocketFactory 和 X509TrustManager

    internal var sign = false                                   //是否需要签名
    internal var timeStamp = false                              //是否需要追加时间戳
    internal var accessToken = false                            //是否需要追加token
    internal var cookies: MutableList<Cookie> = ArrayList()     //用户手动添加的Cookie
    internal val interceptors: MutableList<Interceptor> = ArrayList()
    internal val networkInterceptors: MutableList<Interceptor> = ArrayList()

    internal var url: String? = null
    internal var baseUrl: String? = null
    internal var httpUrl: HttpUrl? = null
    internal var retryCount: Int = 3                            //重试次数默认3次
    internal var retryDelay: Long = 0                           //延迟500ms重试
    internal var retryIncreaseDelay: Long = 0                   //叠加延迟
    internal var isSyncRequest = false                          //是否是同步请求
    internal var isGlobalErrorHandle = false                    //是否进行全局错误统一处理
    internal var converterFactories: MutableList<Converter.Factory> = mutableListOf()
    internal var adapterFactories: MutableList<CallAdapter.Factory> = mutableListOf()

    internal var cache: Cache? = null
    internal var cacheMode: CacheMode? = NO_CACHE               //默认无缓存
    internal var cacheTime: Long? = -1L                         //缓存时间
    internal var cacheKey: String? = null                       //缓存Key
    internal var diskConverter: IDiskConverter? = null          //设置RxCache磁盘转换器

    internal var callback: AbsCallback<*>? = null

    internal var isBreakpointDownload = false                           //是否进行断点下载
    internal var httpLoggingInterceptor: HttpLoggingInterceptor? = null //数据请求打印拦截器

    internal val httpHeaders by lazy { HttpHeaders() }                  //添加的 header
    internal val httpParams by lazy { HttpParams() }                    //添加的 param

    internal val okHttpClient: OkHttpClient by lazy {
        generateOkClient().apply {
            if (cacheMode === DEFAULT && cache != null) {
                this.cache(cache)
            }
        }.build()
    }
    internal val retrofit: Retrofit by lazy {
        generateRetrofit().apply {
            client(okHttpClient)
        }.build()
    }
    internal val apiService: ApiService? by lazy {
        getAppComponent().repositoryManager()
            .obtainRetrofitService<ApiService>(retrofit)
    }
    internal val rxCache: RxCache by lazy {
        generateRxCache().build()
    }

    init {
        this.url = url
        val easyHttp: EasyHttp? = EasyHttp.instance
        baseUrl = easyHttp?.baseUrl
        baseUrl?.let {
            httpUrl = it.toHttpUrlOrNull()
        }
        if (baseUrl?.toHttpUrlOrNull() == null && (url.startsWith("http://") || url.startsWith("https://"))) {
            httpUrl = url.toHttpUrl()
            httpUrl?.let {
                baseUrl = "${it.toUrl().protocol}://${it.toUrl().host}/"    //显示为 http://www.xxx.xxx 或 https://www.xxx.xxx
            }
        }
        cacheMode = easyHttp?.mCacheMode                        //添加缓存模式
        cacheTime = easyHttp?.cacheTime                         //缓存时间
        retryCount = easyHttp?.retryCount ?: 3                  //超时重试次数
        retryDelay = easyHttp?.retryDelay ?: 500                //超时重试延时
        retryIncreaseDelay = easyHttp?.retryIncreaseDelay ?: 0  //超时重试叠加延时
        cache = easyHttp?.httpCache
//        isGlobalErrorHandle = easyHttp?.isGlobalErrorHandle()
//        httpLoggingInterceptor = easyHttp?.getHttpLoggingInterceptor()

        HttpHeaders.acceptLanguage?.let { headers(HttpHeaders.HEAD_KEY_ACCEPT_LANGUAGE, it) }   //默认添加 Accept-Language
        HttpHeaders.userAgent?.let { headers(HttpHeaders.HEAD_KEY_USER_AGENT, it) }             //默认添加 User-Agent
        easyHttp?.getCommonParams()?.let(httpParams::put)                                       //添加公共请求参数
        easyHttp?.getCommonHeaders()?.let(httpHeaders::put)                                     //添加公共请求头
    }

//    fun getParams(): HttpParams {
//        return params
//    }

    fun readTimeOut(readTimeOut: Long): R {
        this.readTimeOut = readTimeOut
        return this as R
    }

    fun writeTimeOut(writeTimeOut: Long): R {
        this.writeTimeOut = writeTimeOut
        return this as R
    }

    fun connectTimeout(connectTimeout: Long): R {
        this.connectTimeout = connectTimeout
        return this as R
    }

    fun timeOut(timeOut: Long): R {
        this.readTimeOut = timeOut
        this.writeTimeOut = timeOut
        this.connectTimeout = timeOut
        return this as R
    }

    /**
     * 设置代理
     */
    fun okHttpProxy(proxy: Proxy): R {
        this.proxy = proxy
        return this as R
    }

    /**
     * https的全局访问规则
     */
    fun hostnameVerifier(hostnameVerifier: HostnameVerifier): R {
        this.hostnameVerifier = hostnameVerifier
        return this as R
    }

    /**
     * https的全局自签名证书
     */
    fun certificates(vararg certificates: InputStream): R {
        sslParams = getSslSocketFactory(certificates)
        return this as R
    }

    /**
     * https双向认证证书
     */
    fun certificates(bksFile: InputStream, password: String, vararg certificates: InputStream): R {
        sslParams = getSslSocketFactory(bksFile, password, certificates)
        return this as R
    }

    fun addInterceptor(interceptor: Interceptor): R {
        interceptor.let(interceptors::add)
        return this as R
    }

    fun addNetworkInterceptor(networkInterceptor: Interceptor): R {
        networkInterceptor.let(networkInterceptors::add)
        return this as R
    }

    fun sign(sign: Boolean): R {
        this.sign = sign
        return this as R
    }

    fun timeStamp(timeStamp: Boolean): R {
        this.timeStamp = timeStamp
        return this as R
    }

    fun accessToken(accessToken: Boolean): R {
        this.accessToken = accessToken
        return this as R
    }

    fun addCookie(name: String, value: String): R {
        val builder = Cookie.Builder()
        httpUrl?.let {
            val cookie: Cookie = builder.name(name).value(value).domain(it.host).build()
            cookies.add(cookie)
        }
        return this as R
    }

    fun addCookie(cookie: Cookie): R {
        cookies.add(cookie)
        return this as R
    }

    fun addCookies(cookies: List<Cookie>): R {
        this.cookies.addAll(cookies)
        return this as R
    }

    fun baseUrl(baseUrl: String): R {
        this.baseUrl = baseUrl
        this.baseUrl?.let {
            httpUrl = it.toHttpUrl()
        }
        return this as R
    }

    fun retryCount(retryCount: Int): R {
        require(retryCount >= 0) { "retryCount must > 0" }
        this.retryCount = retryCount
        return this as R
    }

    fun retryDelay(retryDelay: Long): R {
        require(retryDelay >= 0) { "retryDelay must > 0" }
        this.retryDelay = retryDelay
        return this as R
    }

    fun retryIncreaseDelay(retryIncreaseDelay: Long): R {
        require(retryIncreaseDelay >= 0) { "retryIncreaseDelay must > 0" }
        this.retryIncreaseDelay = retryIncreaseDelay
        return this as R
    }

    fun syncRequest(syncRequest: Boolean): R {
        isSyncRequest = syncRequest
        return this as R
    }

    /**
     * 设置Converter.Factory,默认GsonConverterFactory.create()
     */
    fun addConverterFactory(factory: Converter.Factory): R {
        converterFactories.add(factory)
        return this as R
    }

    /**
     * 设置CallAdapter.Factory,默认RxJavaCallAdapterFactory.create()
     */
    fun addCallAdapterFactory(factory: CallAdapter.Factory): R {
        adapterFactories.add(factory)
        return this as R
    }

    /**
     * 添加头信息
     */
    fun headers(headers: HttpHeaders): R {
        httpHeaders.put(headers)
        return this as R
    }

    /**
     * 添加头信息
     */
    fun headers(key: String, value: String): R {
        httpHeaders.put(key, value)
        return this as R
    }

    /**
     * 移除头信息
     */
    fun removeHeader(key: String): R {
        httpHeaders.remove(key)
        return this as R
    }

    /**
     * 移除所有头信息
     */
    fun removeAllHeaders(): R {
        httpHeaders.clear()
        return this as R
    }

    /**
     * 设置参数
     */
    fun params(params: HttpParams): R {
        httpParams.put(params)
        return this as R
    }

    fun params(key: String, value: String): R {
        httpParams.put(key, value)
        return this as R
    }

    fun params(keyValues: Map<String, String>): R {
        httpParams.put(keyValues)
        return this as R
    }

    fun removeParam(key: String): R {
        httpParams.remove(key)
        return this as R
    }

    fun removeAllParams(): R {
        httpParams.clear()
        return this as R
    }

    /**
     * 添加 okhttp 缓存
     */
    fun okCache(cache: Cache): R {
        this.cache = cache
        return this as R
    }

    /**
     * 缓存模式
     */
    fun cacheMode(cacheMode: CacheMode): R {
        this.cacheMode = cacheMode
        return this as R
    }

    /**
     * 缓存的时间 单位:秒
     */
    fun cacheTime(cacheTime: Long): R {
        var retCacheTime = cacheTime
        if (retCacheTime <= -1) retCacheTime = EasyHttp.DEFAULT_CACHE_NEVER_EXPIRE
        this.cacheTime = retCacheTime
        return this as R
    }

    /**
     * 缓存 Key
     */
    fun cacheKey(cacheKey: String): R {
        this.cacheKey = cacheKey
        return this as R
    }

    /**
     * 设置缓存的转换器
     */
    fun cacheDiskConverter(converter: IDiskConverter): R {
        diskConverter = converter
        return this as R
    }

    /**
     * 移除缓存（key）
     */
    @SuppressLint("CheckResult")
    fun removeCache(key: String) {
        rxCache.remove(key).compose(RxUtil.io_main<Boolean>())
            ?.subscribe({ HttpLog.i("removeCache success!!!") }, { throwable -> HttpLog.i("removeCache err!!!$throwable") })
    }

    /**
     * 根据当前的请求参数，生成对应的OkClient
     */
    private fun generateOkClient(): OkHttpClient.Builder {
        return if (readTimeOut <= 0 && writeTimeOut <= 0 && connectTimeout <= 0 && sslParams == null && cookies.size == 0 && hostnameVerifier == null && proxy == null && httpHeaders.isEmpty()) {
            val existBuilder: OkHttpClient.Builder = EasyHttp.getOkHttpClientBuilder()
            existBuilder.interceptors().forEach { interceptor ->
                if (interceptor is BaseDynamicInterceptor<*>) {
                    interceptor.sign(sign).timeStamp(timeStamp).accessToken(accessToken)
                }
            }
            existBuilder
        } else {
            val newBuilder: OkHttpClient.Builder = EasyHttp.okHttpClient.newBuilder()
            if (readTimeOut > 0) newBuilder.readTimeout(readTimeOut, TimeUnit.MILLISECONDS)
            if (writeTimeOut > 0) newBuilder.writeTimeout(writeTimeOut, TimeUnit.MILLISECONDS)
            if (connectTimeout > 0) newBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
            if (cookies.size > 0) EasyHttp.getCookieJar()?.addCookies(cookies)
            hostnameVerifier?.let(newBuilder::hostnameVerifier)
            sslParams?.let { newBuilder.sslSocketFactory(it.sSLSocketFactory!!, it.trustManager!!) }
            proxy?.let(newBuilder::proxy)

            //添加头  头添加放在最前面方便其他拦截器可能会用到
            newBuilder.addInterceptor(HeadersInterceptor(httpHeaders))
            interceptors.forEach { interceptor ->
                if (interceptor is BaseDynamicInterceptor<*>) {
                    interceptor.sign(sign).timeStamp(timeStamp).accessToken(accessToken)
                }
                newBuilder.addInterceptor(interceptor)
            }
            networkInterceptors.forEach { interceptor ->
                newBuilder.addNetworkInterceptor(interceptor)
            }
//            httpLoggingInterceptor?.let(newBuilder::addInterceptor)
            newBuilder
        }
    }

    /**
     * 根据当前的请求参数，生成对应的Retrofit
     */
    private fun generateRetrofit(): Retrofit.Builder {
        return if (converterFactories.isEmpty() && adapterFactories.isEmpty()) {
            val builder: Retrofit.Builder = getRetrofitBuilder()
            baseUrl?.let {
                builder.baseUrl(it)
            }
            builder
        } else {
            val newBuilder = Retrofit.Builder()
            val existBuilder: Retrofit.Builder = getRetrofitBuilder()
            baseUrl?.let {
                newBuilder.baseUrl(it)
                existBuilder.baseUrl(it)
            }
            if (converterFactories.isNotEmpty()) {
                converterFactories.forEach { converterFactory ->
                    newBuilder.addConverterFactory(converterFactory)
                }
            } else {
                existBuilder.build().converterFactories().forEach { factory ->
                    newBuilder.addConverterFactory(factory)
                }
            }
            if (adapterFactories.isNotEmpty()) {
                adapterFactories.forEach { adapterFactory ->
                    newBuilder.addCallAdapterFactory(adapterFactory)
                }
            } else {
                existBuilder.build().callAdapterFactories().forEach { factory ->
                    newBuilder.addCallAdapterFactory(factory)
                }
            }
            newBuilder
        }
    }

    /**
     * 根据当前的请求参数，生成对应的RxCache和Cache
     */
    private fun generateRxCache(): RxCache.Builder {
        val rxCacheBuilder: RxCache.Builder = EasyHttp.getRxCacheBuilder()
        when (cacheMode) {
            NO_CACHE -> {
                val noCacheInterceptor = NoCacheInterceptor()
                interceptors.add(noCacheInterceptor)
                networkInterceptors.add(noCacheInterceptor)
            }

            DEFAULT -> {
                if (cache == null) {
                    val cacheDirectory: File = EasyHttp.instance?.cacheDirectory ?: File(EasyHttp.context?.cacheDir, "okhttp-cache")
                    cache = cacheDirectory.let {
                        if (it.isDirectory && !it.exists()) it.mkdirs()
                        Cache(it, max(5 * 1024 * 1024, EasyHttp.instance?.cacheMaxSize ?: 0))
                    }
                }
                val cacheControlValue = String.format("max-age=%d", max(-1, cacheTime ?: -1))
                val cacheInterceptor = CacheInterceptor(cacheControlValue)
                val cacheInterceptorOffline = CacheInterceptorOffline(cacheControlValue)
                networkInterceptors.add(cacheInterceptor)
                networkInterceptors.add(cacheInterceptorOffline)
                interceptors.add(cacheInterceptorOffline)
            }

            FIRSTREMOTE, FIRSTCACHE, ONLYREMOTE, ONLYCACHE, CACHEANDREMOTE, CACHEANDREMOTEDISTINCT -> {
                interceptors.add(NoCacheInterceptor())
                return if (diskConverter == null) {
                    rxCacheBuilder.also {
                        it.cacheKey(checkNotNull(cacheKey) { "cacheKey == null" })
                            .cacheTime(cacheTime ?: -1)
                    }
                } else {
                    EasyHttp.rxCache.newBuilder().also {
                        it.diskConverter(diskConverter)
                            .cacheKey(checkNotNull(cacheKey) { "cacheKey == null" })
                            .cacheTime(cacheTime ?: -1)
                    }
                }
            }

            else -> {}
        }
        return rxCacheBuilder
    }

    fun build(): RequestCall {
        return RequestCall(this)
    }

//    open fun build(): R {
//        val rxCacheBuilder: RxCache.Builder = generateRxCache()
//        val okHttpClientBuilder: OkHttpClient.Builder = generateOkClient()
//        if (cacheMode === DEFAULT) { //okhttp缓存
//            okHttpClientBuilder.cache(cache)
//        }
//        val retrofitBuilder = generateRetrofit()
//        okHttpClient = okHttpClientBuilder.build()
//        retrofitBuilder.client(okHttpClient!!)
//        retrofit = retrofitBuilder.build()
//        rxCache = rxCacheBuilder?.build()
//        apiManager = retrofit?.create(ApiService::class.java)
//        return this as R
//    }

    abstract fun generateRequest(): Observable<ResponseBody>?
}