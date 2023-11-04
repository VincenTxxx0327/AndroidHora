package com.union.network.utils

import com.union.network.func.HandleFuc
import com.union.network.model.ApiResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * 线程调度工具
 * @Author： VincenT
 * @Time： 2023/8/15 21:12
 */
object RxUtil {
    fun <T : Any> io(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe { disposable -> HttpLog.i("+++doOnSubscribe+++" + disposable.isDisposed) }
                .doOnComplete { HttpLog.i("+++doOnComplete+++") }
                .observeOn(Schedulers.io())
        }
    }

    fun <T : Any> io_main(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe { disposable -> HttpLog.i("+++doOnSubscribe+++" + disposable.isDisposed) }
                .doOnComplete { HttpLog.i("+++doOnComplete+++") }
                .observeOn(AndroidSchedulers.mainThread())
        }
    }

    fun <T : Any> _io_main(): ObservableTransformer<ApiResult<T>, T> {
        return ObservableTransformer<ApiResult<T>, T> { upstream ->
            upstream
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(HandleFuc())
                .doOnSubscribe { disposable -> HttpLog.i("+++doOnSubscribe+++" + disposable.isDisposed) }
                .doOnComplete { HttpLog.i("+++doOnComplete+++") }
//                .onErrorResumeNext(HttpResponseFunc<T>())
        }
    }

    fun <T : Any> _main(): ObservableTransformer<ApiResult<T>, T> {
        return ObservableTransformer<ApiResult<T>, T> { upstream ->
            upstream
                //.observeOn(AndroidSchedulers.mainThread())
                .map(HandleFuc())
                .doOnSubscribe { disposable -> HttpLog.i("+++doOnSubscribe+++" + disposable.isDisposed) }
                .doOnComplete { HttpLog.i("+++doOnComplete+++") }
//                .onErrorResumeNext(HttpResponseFunc<T>())
        }
    }
}