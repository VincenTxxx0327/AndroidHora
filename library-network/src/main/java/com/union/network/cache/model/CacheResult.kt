package com.union.network.cache.model

import java.io.Serializable

/**
 * 缓存对象
 * @Author： VincenT
 * @Time： 2023/8/15 18:02
 */
class CacheResult<T> : Serializable {
    var isFromCache = false
    var exception: Throwable? = null
    var data: T? = null
    constructor()

    constructor(isFromCache: Boolean) {
        this.isFromCache = isFromCache
    }

    constructor(isFromCache: Boolean, data: T?) {
        this.isFromCache = isFromCache
        this.data = data
    }
    companion object {

        fun <T> success(isFromCache: Boolean, data: T?): CacheResult<T> {
            val cacheResult = CacheResult<T>()
            cacheResult.isFromCache = isFromCache
            cacheResult.data = data
            return cacheResult
        }

        fun <T> error(isFromCache: Boolean, throwable: Throwable?): CacheResult<T> {
            val cacheResult = CacheResult<T>()
            cacheResult.isFromCache = isFromCache
            cacheResult.exception = throwable
            return cacheResult
        }
    }

    override fun toString(): String {
        return "CacheResult(isFromCache=$isFromCache, exception=$exception, data=$data)"
    }
}