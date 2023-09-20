package com.union.network.subscriber

import com.union.common.utils.launchMain
import com.union.network.cache.model.CacheResult
import com.union.network.callback.CacheResultCallback
import com.union.network.exception.ApiException
import com.union.network.request.base.BaseRequest
import com.union.network.utils.HttpLog
import io.reactivex.rxjava3.disposables.Disposable

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/23 18:02
 */
class CacheCallbackSubscriber<T>(val request: BaseRequest<*>) : BaseSubscriber<CacheResult<String>>() {

    @Suppress("UNCHECKED_CAST")
    private val callback by lazy { request.callback as? CacheResultCallback<T> }

    override fun onStart() {
        launchMain {
            callback?.onStart()
        }
    }

    override fun onNext(t: CacheResult<String>) {
        try {
            val body = callback?.convertResponse(t.data)
            launchMain {
                val response = CacheResult.success(t.isFromCache, body)
                callback?.onSuccess(response)
            }
        } catch (throwable: Throwable) {
            error(t.isFromCache, throwable)
        }
    }

    override fun onError(t: Throwable) {
//        if (request.isGlobalErrorHandle) {
//            super.onError(t)
//        }
        error(false, t)
    }

    override fun onError(e: ApiException) {
        error(false, e.cause)
    }

    override fun onComplete() {
        launchMain {
            callback?.onComplete()
        }
    }

    private fun error(isFromCache: Boolean, throwable: Throwable?) {
        launchMain {
            val response = CacheResult.error<T>(isFromCache, throwable)
            callback?.onError(response)
            callback?.onComplete()
        }
    }
}