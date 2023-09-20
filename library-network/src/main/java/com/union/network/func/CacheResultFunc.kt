package com.union.network.func

import com.union.network.cache.model.CacheResult
import io.reactivex.functions.Function

/**
 * 缓存结果转换
 * @Author： VincenT
 * @Time： 2023/8/15 20:55
 */
@Deprecated("")
class CacheResultFunc<T> : Function<CacheResult<T>, T> {
    @Throws(Exception::class)
    override fun apply(cacheResult: CacheResult<T>): T? {
        return cacheResult.data
    }
}