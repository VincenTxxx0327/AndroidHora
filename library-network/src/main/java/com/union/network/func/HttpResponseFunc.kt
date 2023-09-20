package com.union.network.func

import com.union.network.exception.ApiException
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.functions.Function

/**
 * 异常转换处理
 * @Author： VincenT
 * @Time： 2023/8/15 20:56
 */
class HttpResponseFunc<T : Any> : Function<Throwable, Observable<T>> {
    @Throws(Exception::class)
    override fun apply(throwable: Throwable): Observable<T> {
        return Observable.error(ApiException.handleException(throwable))
    }
}