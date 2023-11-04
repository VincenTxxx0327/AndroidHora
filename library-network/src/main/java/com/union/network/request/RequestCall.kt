package com.union.network.request

import android.annotation.SuppressLint
import com.union.hora.http.function.RetryWithDelay
import com.union.network.callback.AbsCallback
import com.union.network.callback.CacheResultCallback
import com.union.network.callback.NoCacheResultCallback
import com.union.network.func.StringResultFunc
import com.union.network.request.base.BaseRequest
import com.union.network.subscriber.CacheCallbackSubscriber
import com.union.network.subscriber.NoCacheCallbackSubscriber
import com.union.network.utils.RxUtil.io
import com.union.network.utils.RxUtil.io_main
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.ResponseBody
import java.lang.reflect.ParameterizedType

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/17 20:57
 */
class RequestCall(private val request: BaseRequest<*>) {

    @SuppressLint("CheckResult")
    fun <T> execute(callback: AbsCallback<T>): Disposable? {
        request.callback = callback
        return when (callback) {
            is CacheResultCallback -> {
                request.generateRequest()
                    ?.map(StringResultFunc())
                    ?.compose(if (request.isSyncRequest) io_main<String>() else io<String>())
                    ?.compose(request.rxCache.transformer(request.cacheMode, String.Companion::class.java))
                    ?.retryWhen(RetryWithDelay(request.retryCount, request.retryDelay))
                    ?.subscribeWith(CacheCallbackSubscriber<T>(request))
            }
            is NoCacheResultCallback -> {
                request.generateRequest()
                    ?.compose(if (request.isSyncRequest) io_main<ResponseBody>() else io<ResponseBody>())
                    ?.retryWhen(RetryWithDelay(request.retryCount, request.retryDelay))
                    ?.subscribeWith(NoCacheCallbackSubscriber<T>(request))
            }
            else -> throw IllegalArgumentException("Callback is must be CacheResultCallback or NoCacheResultCallback!")
        }
    }
}