package com.union.network.subscriber

import com.union.common.utils.launchMain
import com.union.network.cache.model.CacheResult
import com.union.network.callback.custom.NoCacheCustomCallback
import com.union.network.callback.NoCacheResultCallback
import com.union.network.exception.ApiException
import com.union.network.request.base.BaseRequest

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/23 18:11
 */
class NoCacheCallbackSubscriber<T>(val request: BaseRequest<*>) : BaseSubscriber<okhttp3.ResponseBody>() {

    @Suppress("UNCHECKED_CAST")
    private val callback by lazy { request.callback as? NoCacheResultCallback<T> }

    override fun onStart() {
        launchMain {
            callback?.onStart()
        }
    }

    override fun onNext(t: okhttp3.ResponseBody) {
        try {
            val body = callback?.convertResponse(t)
            if (callback is NoCacheCustomCallback<*>) {
                return
            } else if (callback is NoCacheResultCallback<*>) {
                launchMain {
                    val response = CacheResult.success(false, body)
                    callback?.onSuccess(response)
                }
            }
        } catch (throwable: Throwable) {
            error(throwable)
        }
    }

    override fun onError(t: Throwable) {
        if (request.isGlobalErrorHandle) {
            super.onError(t)
        }
        error(t)
    }

    override fun onError(e: ApiException) {
        error(false)
    }

    override fun onComplete() {
        if (callback !is NoCacheCustomCallback) {
            launchMain {
                callback?.onComplete()
            }
        }
    }

    private fun error(throwable: Throwable?) {
        launchMain {
            val response = CacheResult.error<T>(false, throwable)
            callback?.onError(response)
            callback?.onComplete()
        }
    }
}