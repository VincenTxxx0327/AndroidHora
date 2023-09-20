package com.union.network.cache.strategy

import com.union.network.cache.RxCache
import com.union.network.cache.model.CacheResult
import io.reactivex.rxjava3.core.Observable
import java.lang.reflect.Type

/**
 * 先显示缓存，缓存不存在，再请求网络</p>
 * 此类加载用的是反射 所以类名是灰色的 没有直接引用  不要误删
 * @Author： VincenT
 * @Time： 2023/8/15 18:31
 */
@Deprecated("")
class FirstCacheStrategy : BaseStrategy() {

    override fun <T : Any> execute(rxCache: RxCache, cacheKey: String?, cacheTime: Long, source: Observable<T>, type: Type): Observable<CacheResult<T>> {
        val cache: Observable<CacheResult<T>> = loadCache(rxCache, type, cacheKey ?: "", cacheTime, true)
        val remote: Observable<CacheResult<T>> = loadRemote(rxCache, cacheKey ?: "", source, false)
        return cache.switchIfEmpty(remote)
    }
}