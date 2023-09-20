package com.union.network.transformer

import com.union.network.func.HttpResponseFunc
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.ObservableTransformer

/**
 * 错误转换Transformer
 * @Author： VincenT
 * @Time： 2023/8/15 21:45
 */
class HandleErrTransformer<T : Any> : ObservableTransformer<T, T> {
    override fun apply(upstream: Observable<T>): ObservableSource<T> {
        return upstream.onErrorResumeNext(HttpResponseFunc<T>())
    }
}