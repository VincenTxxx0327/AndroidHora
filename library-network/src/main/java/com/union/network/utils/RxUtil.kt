package com.union.network.utils

import com.union.network.func.HandleFuc
import com.union.network.func.HttpResponseFunc
import com.union.network.model.ApiResult
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * 线程调度工具
 * @Author： VincenT
 * @Time： 2023/8/15 21:12
 */
object RxUtil {
    fun <T> io(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe { disposable -> HttpLog.i("+++doOnSubscribe+++" + disposable.isDisposed) }
                .doFinally { HttpLog.i("+++doFinally+++") }
                .observeOn(Schedulers.io())
        }
    }

    fun <T> io_main(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe { disposable -> HttpLog.i("+++doOnSubscribe+++" + disposable.isDisposed) }
                .doFinally { HttpLog.i("+++doFinally+++") }
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T> _io_main(): ObservableTransformer<ApiResult<T>, T> {
        return ObservableTransformer<ApiResult<T>, T> { upstream ->
            upstream
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(HandleFuc())
                .doOnSubscribe { disposable -> HttpLog.i("+++doOnSubscribe+++" + disposable.isDisposed) }
                .doFinally { HttpLog.i("+++doFinally+++") }
                .onErrorResumeNext(HttpResponseFunc<T>())
        }
    }

    fun <T> _main(): ObservableTransformer<ApiResult<T>, T> {
        return ObservableTransformer<ApiResult<T>, T> { upstream ->
            upstream //.observeOn(AndroidSchedulers.mainThread())
                .map(HandleFuc())
                .doOnSubscribe { disposable -> HttpLog.i("+++doOnSubscribe+++" + disposable.isDisposed) }
                .doFinally { HttpLog.i("+++doFinally+++") }
                .onErrorResumeNext(HttpResponseFunc<T>())
        }
    }
}