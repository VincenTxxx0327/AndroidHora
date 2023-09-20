package com.union.network.cache

import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.union.network.cache.converter.IDiskConverter
import com.union.network.cache.converter.SerializableDiskConverter
import com.union.network.cache.core.CacheCore
import com.union.network.cache.core.LruDiskCache
import com.union.network.cache.model.CacheMode
import com.union.network.cache.model.CacheResult
import com.union.network.cache.strategy.IStrategy
import com.union.network.utils.HttpLog
import com.union.network.utils.Utils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.exceptions.Exceptions
import java.io.File
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.math.max
import kotlin.math.min

/**
 * 缓存统一入口类
 * 主要实现技术：Rxjava+DiskLruCache(jakewharton大神开源的LRU库)
 * 主要功能：
 * 1.可以独立使用，单独用RxCache来存储数据
 * 2.采用transformer与网络请求结合，可以实现网络缓存功能,本地硬缓存
 * 3.可以保存缓存 （异步）
 * 4.可以读取缓存（异步）
 * 5.可以判断缓存是否存在
 * 6.根据key删除缓存
 * 7.清空缓存（异步）
 * 8.缓存Key会自动进行MD5加密
 * 9.其它参数设置：缓存磁盘大小、缓存key、缓存时间、缓存存储的转换器、缓存目录、缓存Version
 * 使用说明：
 * RxCache rxCache = new RxCache.Builder(this)
 * .appVersion(1)//不设置，默认为1
 * .diskDir(new File(getCacheDir().getPath() + File.separator + "data-cache"))//不设置，默认使用缓存路径
 * .diskConverter(new SerializableDiskConverter())//目前只支持Serializable缓存
 * .diskMax(20*1024*1024)//不设置， 默为认50MB
 * .build();
 * @Author： VincenT
 * @Time： 2023/8/15 17:49
 */
class RxCache private constructor(builder: Builder) {
    private val context: Context?
    private var cacheCore: CacheCore? = null    //缓存的核心管理类
    private val cacheKey: String?               //缓存的key
    private val cacheTime: Long                 //缓存的时间 单位:秒
    private val diskDir: File?                  //缓存的磁盘目录，默认是缓存目录
    private val diskConverter: IDiskConverter?  //缓存的转换器
    private val diskMaxSize: Long               //缓存的磁盘大小
    private val appVersion: Int                 //缓存的版本

    constructor() : this(Builder())

    init {
        context = builder.context
        cacheKey = builder.cacheKey
        cacheTime = builder.cacheTime
        diskDir = builder.diskDir
        diskMaxSize = builder.diskMaxSize
        diskConverter = builder.diskConverter
        appVersion = builder.appVersion
        if (diskConverter != null && diskDir != null) {
            cacheCore = CacheCore(LruDiskCache(diskConverter, diskDir, appVersion, diskMaxSize))
        }
    }

    fun newBuilder(): Builder {
        return Builder(this)
    }

    /**
     * 缓存transformer
     *
     * @param cacheMode 缓存类型
     * @param type      缓存clazz
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> transformer(cacheMode: CacheMode, type: Type): ObservableTransformer<T, CacheResult<T>> {
        val strategy: IStrategy = loadStrategy(cacheMode) //获取缓存策略
        return ObservableTransformer<T, CacheResult<T>> { upstream ->
            HttpLog.i("cacheKey=$cacheKey")
            var tempType = type
            if (type is ParameterizedType) { //自定义ApiResult
                val cls: Class<T> = type.rawType as Class<T>
                if (CacheResult::class.java.isAssignableFrom(cls)) {
                    tempType = Utils.getParameterizedType<T>(type, 0)
                }
            }
            strategy.execute(this@RxCache, cacheKey, cacheTime, upstream, tempType)
        }
    }

    private abstract class SimpleSubscribe<T : Any> : ObservableOnSubscribe<T> {
        @Throws(Exception::class)
        override fun subscribe(subscriber: ObservableEmitter<T>) {
            try {
                val data = execute()
                if (!subscriber.isDisposed && data != null) {
                    subscriber.onNext(data)
                }
            } catch (e: Throwable) {
                HttpLog.e(e.message)
                if (!subscriber.isDisposed) {
                    subscriber.onError(e)
                }
                Exceptions.throwIfFatal(e)
                //RxJavaPlugins.onError(e);
                return
            }
            if (!subscriber.isDisposed) {
                subscriber.onComplete()
            }
        }

        @Throws(Throwable::class)
        abstract fun execute(): T?
    }

    /**
     * 获取缓存
     * @param type 保存的类型
     * @param key 缓存key
     */
    fun <T : Any> load(type: Type, key: String): Observable<T> {
        return load(type, key, -1)
    }

    /**
     * 根据时间读取缓存
     *
     * @param type 保存的类型
     * @param key  缓存key
     * @param time 保存时间
     */
    fun <T : Any> load(type: Type, key: String, time: Long): Observable<T> {
        return Observable.create(object : SimpleSubscribe<T>() {
            override fun execute(): T? {
                return cacheCore?.load(type, key, time)
            }
        })
    }

    /**
     * 保存
     *
     * @param key   缓存key
     * @param value 缓存Value
     */
    fun <T> save(key: String, value: T): Observable<Boolean> {
        return Observable.create(object : SimpleSubscribe<Boolean>() {
            @Throws(Throwable::class)
            override fun execute(): Boolean {
                cacheCore?.save(key, value)
                return true
            }
        })
    }

    /**
     * 是否包含
     */
    fun containsKey(key: String): Observable<Boolean> {
        return Observable.create(object : SimpleSubscribe<Boolean>() {
            @Throws(Throwable::class)
            override fun execute(): Boolean {
                return cacheCore?.containsKey(key) ?: false
            }
        })
    }

    /**
     * 删除缓存
     */
    fun remove(key: String): Observable<Boolean> {
        return Observable.create(object : SimpleSubscribe<Boolean>() {
            @Throws(Throwable::class)
            override fun execute(): Boolean {
                return cacheCore?.remove(key) ?: false
            }
        })
    }

    /**
     * 清空缓存
     */
    fun clear(): Observable<Boolean> {
        return Observable.create(object : SimpleSubscribe<Boolean>() {
            @Throws(Throwable::class)
            override fun execute(): Boolean {
                return cacheCore?.clear() ?: false
            }
        })
    }

    /**
     * 利用反射，加载缓存策略模型
     */
    private fun loadStrategy(cacheMode: CacheMode): IStrategy {
        return try {
            val pkName: String = IStrategy::class.java.getPackage()?.name ?: ""
            Class.forName(pkName + "." + cacheMode.getClassName()).newInstance() as IStrategy
        } catch (e: Exception) {
            throw RuntimeException("loadStrategy(" + cacheMode + ") err!!" + e.message)
        }
    }

    @Suppress("DEPRECATION")
    class Builder {
        internal var context: Context? = null
        internal var cacheKey: String? = null
        internal var cacheTime: Long
        internal var diskDir: File? = null
        internal var diskMaxSize: Long = 0
        internal var diskConverter: IDiskConverter?
        internal var appVersion: Int

        constructor() {
            diskConverter = SerializableDiskConverter()
            cacheTime = CACHE_NEVER_EXPIRE
            appVersion = 1
        }

        constructor(rxCache: RxCache) {
            context = rxCache.context
            cacheKey = rxCache.cacheKey
            cacheTime = rxCache.cacheTime
            diskDir = rxCache.diskDir
            diskMaxSize = rxCache.diskMaxSize
            diskConverter = rxCache.diskConverter
            appVersion = rxCache.appVersion
        }

        fun init(context: Context?): Builder {
            this.context = context
            return this
        }

        /**
         * 不设置，默认为1
         */
        fun appVersion(appVersion: Int): Builder {
            this.appVersion = appVersion
            return this
        }

        /**
         * 默认为缓存路径
         *
         * @param directory
         * @return
         */
        fun diskDir(directory: File?): Builder {
            diskDir = directory
            return this
        }

        fun diskConverter(converter: IDiskConverter?): Builder {
            diskConverter = converter
            return this
        }

        /**
         * 不设置， 默为认50MB
         */
        fun diskMax(maxSize: Long): Builder {
            diskMaxSize = maxSize
            return this
        }

        fun cacheKey(cacheKey: String?): Builder {
            this.cacheKey = cacheKey
            return this
        }

        fun cacheTime(cacheTime: Long): Builder {
            this.cacheTime = cacheTime
            return this
        }

        fun build(): RxCache {
            diskDir = diskDir ?: getDiskCacheDir(context, "data-cache")
            diskConverter = diskConverter ?: SerializableDiskConverter()
            diskDir?.let {
                if (!it.exists()) it.mkdirs()
                if (diskMaxSize <= 0) diskMaxSize = calculateDiskCacheSize(it)
            }
            cacheTime = max(CACHE_NEVER_EXPIRE, cacheTime)
            appVersion = max(1, appVersion)
            return RxCache(this)
        }

        /**
         * 应用程序缓存原理：
         * 1.当SD卡存在或者SD卡不可被移除的时候，就调用getExternalCacheDir()方法来获取缓存路径，否则就调用getCacheDir()方法来获取缓存路径<br></br>
         * 2.前者是/sdcard/Android/data/<application package>/cache 这个路径<br></br>
         * 3.后者获取到的是 /data/data/<application package>/cache 这个路径<br></br>
         *
         * @param uniqueName 缓存目录
        </application></application> */
        @Suppress("SameParameterValue")
        private fun getDiskCacheDir(context: Context?, uniqueName: String): File {
            var cacheDir: File?
            cacheDir = if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                context!!.externalCacheDir
            } else {
                context!!.cacheDir
            }
            if (cacheDir == null) { // if cacheDir is null throws NullPointerException
                cacheDir = context.cacheDir
            }
            return File(cacheDir!!.path + File.separator + uniqueName)
        }

        companion object {
            private const val MIN_DISK_CACHE_SIZE = 5 * 1024 * 1024     // 5MB
            private const val MAX_DISK_CACHE_SIZE = 50 * 1024 * 1024    // 50MB
            private const val CACHE_NEVER_EXPIRE: Long = -1             //永久不过期
            private fun calculateDiskCacheSize(dir: File): Long {
                var size: Long = 0
                try {
                    val statFs = StatFs(dir.absolutePath)
                    val available = statFs.blockCount.toLong() * statFs.blockSize
                    size = available / 50
                } catch (ignored: IllegalArgumentException) {
                }
                return max(min(size, MAX_DISK_CACHE_SIZE.toLong()), MIN_DISK_CACHE_SIZE.toLong())
            }
        }
    }
}