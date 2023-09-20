package com.union.network.cache.strategy

import android.annotation.SuppressLint
import com.union.network.cache.RxCache
import com.union.network.cache.model.CacheResult
import com.union.network.utils.HttpLog
import io.reactivex.rxjava3.core.Observable
import java.lang.reflect.Type

/**
 * 实现缓存策略的基类
 * @Author： VincenT
 * @Time： 2023/8/15 18:02
 */
abstract class BaseStrategy : IStrategy {
    fun <T : Any> loadCache(rxCache: RxCache, type: Type, key: String, time: Long, needEmpty: Boolean): Observable<CacheResult<T>> {
        var observable = rxCache.load<T>(type, key, time).flatMap { t ->
            Observable.just(CacheResult(true, t))
        }
        if (needEmpty) {
            observable = observable.onErrorResumeNext {
                Observable.empty()
            }
        }
        return observable
    }

    //请求成功后：同步保存
    fun <T : Any> loadRemote(rxCache: RxCache, key: String, source: Observable<T>, needEmpty: Boolean): Observable<CacheResult<T>> {
        var observable = source.flatMap { t ->
            rxCache.save(key, t).map { boolean ->
                HttpLog.i("save status => $boolean")
                CacheResult(false, t)
            }.onErrorReturn { throwable ->
                HttpLog.i("save status => $throwable")
                CacheResult(false, t)
            }
        }
        if (needEmpty) {
            observable = observable.onErrorResumeNext { Observable.empty() }
        }
        return observable
    }
}

