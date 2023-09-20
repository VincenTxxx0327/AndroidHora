package com.union.network.subscriber

import android.content.Context
import com.union.network.exception.ApiException
import com.union.network.utils.HttpLog
import com.union.network.utils.NetworkUtil.isNetworkAvailable
import io.reactivex.observers.DisposableObserver
import java.lang.ref.WeakReference

/**
 * 订阅的基类
 * 1.可以防止内存泄露
 * 2.在onStart()没有网络时直接onCompleted()
 * 3.统一处理了异常
 * @Author： VincenT
 * @Time： 2023/8/15 21:46
 */
abstract class BaseSubscriber<T : Any>(val context: Context? = null) : DisposableObserver<T>() {

    private var contextWeakReference: WeakReference<Context?>? = null

    init {
        if (context != null) {
            contextWeakReference = WeakReference(context)
        }
    }

    override fun onStart() {
        HttpLog.e("-->DisposableObserver onStart")
        if (contextWeakReference != null && contextWeakReference?.get() != null && !isNetworkAvailable(contextWeakReference?.get()!!)) {
            //Toast.makeText(context, "无网络，读取缓存数据", Toast.LENGTH_SHORT).show();
            onComplete()
        }
    }

    override fun onNext(t: T) {
        HttpLog.e("-->DisposableObserver onNext")
    }

    override fun onError(e: Throwable) {
        HttpLog.e("-->DisposableObserver onError")
        if (e is ApiException) {
            HttpLog.e("-->onError apiException err:$e")
            onError(e)
        } else {
            HttpLog.e("-->onError elseException err:$e")
            onError(ApiException.handleException(e))
        }
    }

    override fun onComplete() {
        HttpLog.e("-->DisposableObserver onComplete")
    }

    abstract fun onError(e: ApiException)
}