package com.union.network.cache.strategy

import com.union.network.cache.RxCache
import com.union.network.cache.model.CacheResult
import io.reactivex.rxjava3.core.Observable
import java.lang.reflect.Type

/**
 * 实现缓存策略的接口，可以自定义缓存实现方式，只要实现该接口就可以
 * @Author： VincenT
 * @Time： 2023/8/15 18:33
 */
interface IStrategy {
    /**
     * 执行缓存
     *
     * @param rxCache   缓存管理对象
     * @param cacheKey  缓存key
     * @param cacheTime 缓存时间
     * @param source    网络请求对象
     * @param type     要转换的目标对象
     * @return 返回带缓存的Observable流对象
     */
    fun <T : Any> execute(rxCache: RxCache, cacheKey: String? = "", cacheTime: Long, source: Observable<T>, type: Type): Observable<CacheResult<T>>
}