package com.union.network.cache.strategy

import com.union.network.cache.RxCache
import com.union.network.cache.model.CacheResult
import io.reactivex.rxjava3.core.Observable
import java.lang.reflect.Type

/**
 * 网络加载，不缓存
 * @Author： VincenT
 * @Time： 2023/8/15 18:33
 */
@Deprecated("")
class NoStrategy : IStrategy {

    override fun <T : Any> execute(rxCache: RxCache, cacheKey: String?, cacheTime: Long, source: Observable<T>, type: Type): Observable<CacheResult<T>> {
        return source.map { t -> CacheResult(false, t) }
    }
}