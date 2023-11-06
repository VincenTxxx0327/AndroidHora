package com.union.network

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.union.network.cache.RxCache
import com.union.network.cache.converter.IDiskConverter
import com.union.network.cache.converter.SerializableDiskConverter
import com.union.network.cache.model.CacheMode
import com.union.network.cookie.CookieManger
import com.union.network.https.HttpsUtils.getSslSocketFactory
import com.union.network.interceptor.HttpLoggingInterceptor
import com.union.network.model.HttpHeaders
import com.union.network.model.HttpParams
import com.union.network.request.CustomRequest
import com.union.network.request.DeleteRequest
import com.union.network.request.DownloadRequest
import com.union.network.request.GetRequest
import com.union.network.request.PostRequest
import com.union.network.request.PutRequest
import com.union.network.utils.HttpLog
import com.union.network.utils.RxUtil.io_main
import com.union.network.utils.Utils
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.io.File
import java.io.InputStream
import java.net.Proxy
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

/**
 * 网络请求入口类
 * 主要功能：
 * 1.全局设置超时时间
 * 2.支持请求错误重试相关参数，包括重试次数、重试延时时间
 * 3.支持缓存支持6种缓存模式、时间、大小、缓存目录
 * 4.支持支持GET、post、delete、put请求
 * 5.支持支持自定义请求
 * 6.支持文件上传、下载
 * 7.支持全局公共请求头
 * 8.支持全局公共参数
 * 9.支持okhttp相关参数，包括拦截器
 * 10.支持Retrofit相关参数
 * 11.支持Cookie管理
 * @Author： VincenT
 * @Time： 2023/8/15 22:09
 */
@Suppress("unused")
class EasyHttp private constructor() {
    /**
     * 获取OkHttp的缓存<br></br>
     */
    var httpCache: Cache? = null //Okhttp缓存对象
        get() = instance?.httpCache
        private set
    var mCacheMode: CacheMode = CacheMode.NO_CACHE //缓存类型

    /**
     * 获取全局的缓存过期时间
     */
    var cacheTime: Long = -1 //缓存时间
        get() = instance?.cacheTime ?: -1
        private set

    /**
     * 获取缓存的路劲
     */
    var cacheDirectory //缓存目录
            : File? = null
        get() = instance?.cacheDirectory
        private set

    /**
     * 获取全局的缓存大小
     */
    var cacheMaxSize //缓存大小
            : Long = 0
        get() = instance?.cacheMaxSize ?: 0
        private set

    /**
     * 获取全局baseurl
     */
    var baseUrl //全局BaseUrl
            : String? = null
        get() = instance?.baseUrl
        private set

    /**
     * 超时重试次数
     */
    var retryCount = DEFAULT_RETRY_COUNT //重试次数默认3次
        get() = instance?.retryCount ?: DEFAULT_RETRY_COUNT
        private set

    /**
     * 超时重试延迟时间
     */
    var retryDelay = DEFAULT_RETRY_DELAY //延迟xxms重试
        get() = instance?.retryDelay ?: DEFAULT_RETRY_DELAY
        private set

    /**
     * 超时重试延迟叠加时间
     */
    var retryIncreaseDelay = DEFAULT_RETRY_INCREASE_DELAY //叠加延迟
        get() = instance?.retryIncreaseDelay ?: DEFAULT_RETRY_INCREASE_DELAY
        private set
    private var mCommonHeaders //全局公共请求头
            : HttpHeaders? = null
    private var mCommonParams //全局公共请求参数
            : HttpParams? = null
    private val okHttpClientBuilder //okhttp请求的客户端
            : OkHttpClient.Builder = OkHttpClient.Builder()
    private val retrofitBuilder //Retrofit请求Builder
            : Retrofit.Builder
    private val rxCacheBuilder //RxCache请求的Builder
            : RxCache.Builder
    private var cookieJar //Cookie管理
            : CookieManger? = null

    init {
        okHttpClientBuilder.hostnameVerifier(DefaultHostnameVerifier())
        okHttpClientBuilder.connectTimeout(DEFAULT_MILLISECONDS.toLong(), TimeUnit.MILLISECONDS)
        okHttpClientBuilder.readTimeout(DEFAULT_MILLISECONDS.toLong(), TimeUnit.MILLISECONDS)
        okHttpClientBuilder.writeTimeout(DEFAULT_MILLISECONDS.toLong(), TimeUnit.MILLISECONDS)
        retrofitBuilder = Retrofit.Builder()
        retrofitBuilder.addCallAdapterFactory(RxJava3CallAdapterFactory.create()) //增加RxJava2CallAdapterFactory
        rxCacheBuilder = RxCache.Builder().init(sContext)
            .diskConverter(SerializableDiskConverter()) //目前只支持Serializable和Gson缓存其它可以自己扩展
    }

    /**
     * 调试模式,默认打开所有的异常调试
     */
    fun debug(tag: String?): EasyHttp {
        debug(tag, true)
        return this
    }

    /**
     * 调试模式,第二个参数表示所有catch住的log是否需要打印<br></br>
     * 一般来说,这些异常是由于不标准的数据格式,或者特殊需要主动产生的,
     * 并不是框架错误,如果不想每次打印,这里可以关闭异常显示
     */
    fun debug(tag: String?, isPrintException: Boolean): EasyHttp {
        val tempTag = if (TextUtils.isEmpty(tag)) "RxEasyHttp_" else tag!!
        if (isPrintException) {
            val loggingInterceptor = HttpLoggingInterceptor(tempTag, true)
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
        }
        HttpLog.customTagPrefix = tempTag
        HttpLog.allowE = isPrintException
        HttpLog.allowD = isPrintException
        HttpLog.allowI = isPrintException
        HttpLog.allowV = isPrintException
        return this
    }

    /**
     * 此类是用于主机名验证的基接口。 在握手期间，如果 URL 的主机名和服务器的标识主机名不匹配，
     * 则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。策略可以是基于证书的或依赖于其他验证方案。
     * 当验证 URL 主机名使用的默认规则失败时使用这些回调。如果主机名是可接受的，则返回 true
     */
    inner class DefaultHostnameVerifier : HostnameVerifier {
        @SuppressLint("BadHostnameVerifier")
        override fun verify(hostname: String, session: SSLSession): Boolean {
            return true
        }
    }

    /**
     * https的全局访问规则
     */
    fun setHostnameVerifier(hostnameVerifier: HostnameVerifier): EasyHttp {
        okHttpClientBuilder.hostnameVerifier(hostnameVerifier)
        return this
    }

    /**
     * https的全局自签名证书
     */
    fun setCertificates(vararg certificates: InputStream): EasyHttp {
        val sslParams = getSslSocketFactory(certificates)
        okHttpClientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
        return this
    }

    /**
     * https双向认证证书
     */
    fun setCertificates(bksFile: InputStream?, password: String?, vararg certificates: InputStream): EasyHttp {
        val sslParams = getSslSocketFactory(bksFile, password, certificates)
        okHttpClientBuilder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
        return this
    }

    /**
     * 全局cookie存取规则
     */
    fun setCookieStore(cookieManager: CookieManger?): EasyHttp {
        cookieJar = cookieManager
        cookieJar?.let { okHttpClientBuilder.cookieJar(it) }
        return this
    }

    /**
     * 全局读取超时时间
     */
    fun setReadTimeOut(readTimeOut: Long): EasyHttp {
        okHttpClientBuilder.readTimeout(readTimeOut, TimeUnit.MILLISECONDS)
        return this
    }

    /**
     * 全局写入超时时间
     */
    fun setWriteTimeOut(writeTimeout: Long): EasyHttp {
        okHttpClientBuilder.writeTimeout(writeTimeout, TimeUnit.MILLISECONDS)
        return this
    }

    /**
     * 全局连接超时时间
     */
    fun setConnectTimeout(connectTimeout: Long): EasyHttp {
        okHttpClientBuilder.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
        return this
    }

    /**
     * 超时重试次数
     */
    fun setRetryCount(retryCount: Int): EasyHttp {
        require(retryCount >= 0) { "retryCount must > 0" }
        this.retryCount = retryCount
        return this
    }

    /**
     * 超时重试延迟时间
     */
    fun setRetryDelay(retryDelay: Long): EasyHttp {
        require(retryDelay >= 0) { "retryDelay must > 0" }
        this.retryDelay = retryDelay
        return this
    }

    /**
     * 超时重试延迟叠加时间
     */
    fun setRetryIncreaseDelay(retryIncreaseDelay: Long): EasyHttp {
        require(retryIncreaseDelay >= 0) { "retryIncreaseDelay must > 0" }
        this.retryIncreaseDelay = retryIncreaseDelay
        return this
    }

    /**
     * 全局的缓存模式
     */
    fun setCacheMode(cacheMode: CacheMode): EasyHttp {
        mCacheMode = cacheMode
        return this
    }

    /**
     * 全局的缓存过期时间
     */
    fun setCacheTime(cacheTime: Long): EasyHttp {
        var retCacheTime = cacheTime
        if (retCacheTime <= -1) retCacheTime = DEFAULT_CACHE_NEVER_EXPIRE
        this.cacheTime = retCacheTime
        return this
    }

    /**
     * 全局的缓存大小,默认50M
     */
    fun setCacheMaxSize(maxSize: Long): EasyHttp {
        cacheMaxSize = maxSize
        return this
    }

    /**
     * 全局设置缓存的版本，默认为1，缓存的版本号
     */
    fun setCacheVersion(cacheVersion: Int): EasyHttp {
        require(cacheVersion >= 0) { "cacheVersion must > 0" }
        rxCacheBuilder.appVersion(cacheVersion)
        return this
    }

    /**
     * 全局设置缓存的路径，默认是应用包下面的缓存
     */
    fun setCacheDirectory(directory: File?): EasyHttp {
        cacheDirectory = Utils.checkNotNull(directory, "directory == null")
        rxCacheBuilder.diskDir(directory)
        return this
    }

    /**
     * 全局设置缓存的转换器
     */
    fun setCacheDiskConverter(converter: IDiskConverter?): EasyHttp {
        rxCacheBuilder.diskConverter(Utils.checkNotNull(converter, "converter == null"))
        return this
    }

    /**
     * 全局设置OkHttp的缓存,默认是3天
     */
    fun setHttpCache(cache: Cache?): EasyHttp {
        httpCache = cache
        return this
    }

    /**
     * 添加全局公共请求参数
     */
    fun addCommonParams(commonParams: HttpParams?): EasyHttp {
        if (mCommonParams == null) mCommonParams = HttpParams()
        mCommonParams?.put(commonParams)
        return this
    }

    /**
     * 获取全局公共请求参数
     */
    fun getCommonParams(): HttpParams? {
        return mCommonParams
    }

    /**
     * 获取全局公共请求头
     */
    fun getCommonHeaders(): HttpHeaders? {
        return mCommonHeaders
    }

    /**
     * 添加全局公共请求参数
     */
    fun addCommonHeaders(commonHeaders: HttpHeaders?): EasyHttp {
        if (mCommonHeaders == null) mCommonHeaders = HttpHeaders()
        mCommonHeaders?.put(commonHeaders)
        return this
    }

    /**
     * 添加全局拦截器
     */
    fun addInterceptor(interceptor: Interceptor?): EasyHttp {
        okHttpClientBuilder.addInterceptor(Utils.checkNotNull(interceptor, "interceptor == null"))
        return this
    }

    /**
     * 添加全局网络拦截器
     */
    fun addNetworkInterceptor(interceptor: Interceptor?): EasyHttp {
        okHttpClientBuilder.addNetworkInterceptor(Utils.checkNotNull(interceptor, "interceptor == null"))
        return this
    }

    /**
     * 全局设置代理
     */
    fun setOkProxy(proxy: Proxy?): EasyHttp {
        okHttpClientBuilder.proxy(Utils.checkNotNull(proxy, "proxy == null"))
        return this
    }

    /**
     * 全局设置请求的连接池
     */
    fun setOkConnectionPool(connectionPool: ConnectionPool?): EasyHttp {
        okHttpClientBuilder.connectionPool(Utils.checkNotNull(connectionPool, "connectionPool == null"))
        return this
    }

    /**
     * 全局为Retrofit设置自定义的OkHttpClient
     */
    fun setOkClient(client: OkHttpClient?): EasyHttp {
        retrofitBuilder.client(Utils.checkNotNull(client, "client == null"))
        return this
    }

    /**
     * 全局设置Converter.Factory,默认GsonConverterFactory.create()
     */
    fun addConverterFactory(factory: Converter.Factory?): EasyHttp {
        retrofitBuilder.addConverterFactory(Utils.checkNotNull(factory, "factory == null"))
        return this
    }

    /**
     * 全局设置CallAdapter.Factory,默认RxJavaCallAdapterFactory.create()
     */
    fun addCallAdapterFactory(factory: CallAdapter.Factory?): EasyHttp {
        retrofitBuilder.addCallAdapterFactory(Utils.checkNotNull(factory, "factory == null"))
        return this
    }

    /**
     * 全局设置Retrofit callbackExecutor
     */
    fun setCallbackExecutor(executor: Executor?): EasyHttp {
        retrofitBuilder.callbackExecutor(Utils.checkNotNull(executor, "executor == null"))
        return this
    }

    /**
     * 全局设置Retrofit对象Factory
     */
    fun setCallFactory(factory: okhttp3.Call.Factory?): EasyHttp {
        retrofitBuilder.callFactory(Utils.checkNotNull(factory, "factory == null"))
        return this
    }

    /**
     * 全局设置baseurl
     */
    fun setBaseUrl(baseUrl: String?): EasyHttp {
        this.baseUrl = Utils.checkNotNull(baseUrl, "baseUrl == null")
        return this
    }

    companion object {
        private var sContext: Application? = null
        const val DEFAULT_MILLISECONDS = 60000 //默认的超时时间
        private const val DEFAULT_RETRY_COUNT = 3 //默认重试次数
        private const val DEFAULT_RETRY_DELAY: Long = 500 //默认重试延时
        private const val DEFAULT_RETRY_INCREASE_DELAY: Long = 0 //默认重试叠加时间
        const val DEFAULT_CACHE_NEVER_EXPIRE = -1L //缓存过期时间，默认永久缓存

        @Volatile
        private var singleton: EasyHttp? = null
        val instance: EasyHttp?
            get() {
                testInitialize()
                if (singleton == null) {
                    synchronized(EasyHttp::class.java) {
                        if (singleton == null) {
                            singleton = EasyHttp()
                        }
                    }
                }
                return singleton
            }

        /**
         * 必须在全局Application先调用，获取context上下文，否则缓存无法使用
         */
        fun init(app: Application?) {
            sContext = app
        }

        /**
         * 获取全局上下文
         */
        val context: Context?
            get() {
                testInitialize()
                return sContext
            }

        private fun testInitialize() {
            if (sContext == null) throw ExceptionInInitializerError("请先在全局Application中调用 EasyHttp.init() 初始化！")
        }

        val okHttpClient: OkHttpClient
            get() = instance!!.okHttpClientBuilder.build()
        val retrofit: Retrofit
            get() = instance!!.retrofitBuilder.build()
        val rxCache: RxCache
            get() = instance!!.rxCacheBuilder.build()

        /**
         * 对外暴露 OkHttpClient,方便自定义
         */
        fun getOkHttpClientBuilder(): OkHttpClient.Builder {
            return instance!!.okHttpClientBuilder
        }

        /**
         * 对外暴露 Retrofit,方便自定义
         */
        fun getRetrofitBuilder(): Retrofit.Builder {
            return instance!!.retrofitBuilder
        }

        /**
         * 对外暴露 RxCache,方便自定义
         */
        fun getRxCacheBuilder(): RxCache.Builder {
            return instance!!.rxCacheBuilder
        }

        /**
         * 获取全局的cookie实例
         */
        fun getCookieJar(): CookieManger? {
            return instance!!.cookieJar
        }

        /**
         * 获取全局的缓存模式
         */
        val cacheMode: CacheMode
            get() = instance!!.mCacheMode

        /**
         * get请求
         */
        operator fun get(url: String): GetRequest {
            return GetRequest(url)
        }

        /**
         * post请求
         */
        fun post(url: String): PostRequest {
            return PostRequest(url)
        }

        /**
         * delete请求
         */
        fun delete(url: String): DeleteRequest {
            return DeleteRequest(url)
        }

        /**
         * 自定义请求
         */
        fun custom(url: String): CustomRequest {
            return CustomRequest(url)
        }

        fun downLoad(url: String): DownloadRequest {
            return DownloadRequest(url)
        }

        fun put(url: String): PutRequest {
            return PutRequest(url)
        }

        /**
         * 取消订阅
         */
        fun cancelSubscription(disposable: Disposable?) {
            if (disposable != null && !disposable.isDisposed) {
                disposable.dispose()
            }
        }

        /**
         * 清空缓存
         */
        @SuppressLint("CheckResult")
        fun clearCache() {
            rxCache.clear().compose(io_main<Boolean>())
                .subscribe({ HttpLog.i("clearCache success!!!") }, { HttpLog.i("clearCache err!!!") })
        }

        /**
         * 移除缓存（key）
         */
        @SuppressLint("CheckResult")
        fun removeCache(key: String) {
            rxCache.remove(key).compose(io_main<Boolean>())
                .subscribe({ HttpLog.i("removeCache success!!!") }, { HttpLog.i("removeCache err!!!") })
        }
    }
}