package com.union.network.cache.strategy

import com.union.network.cache.RxCache
import com.union.network.cache.model.CacheResult
import io.reactivex.rxjava3.core.Observable
import java.lang.reflect.Type

/**
 * 只读缓存</p>
 * 此类加载用的是反射 所以类名是灰色的 没有直接引用  不要误删
 * @Author： VincenT
 * @Time： 2023/8/15 18:34
 */
@Deprecated("")
class OnlyCacheStrategy : BaseStrategy() {

    override fun <T : Any> execute(rxCache: RxCache, cacheKey: String?, cacheTime: Long, source: Observable<T>, type: Type): Observable<CacheResult<T>> {
        return loadCache(rxCache, type, cacheKey ?: "", cacheTime, false)
    }
}