package com.union.network.subscriber

import android.content.Context
import com.union.network.callback.AbsCallback
import com.union.network.callback.ProgressDialogCallback
import com.union.network.exception.ApiException

/**
 * 带有callBack的回调
 * 主要作用是不需要用户订阅，只要实现callback回调
 * @Author： VincenT
 * @Time： 2023/8/15 21:55
 */
@Deprecated("")
class CallBackSubscriber<T : Any>(context: Context?, private val absCallBack: AbsCallback<T>) : BaseSubscriber<T>(context) {

    init {
        if (absCallBack is ProgressDialogCallback<*>) {
            (absCallBack as ProgressDialogCallback<*>).subscription(this)
        }
    }

    override fun onStart() {
        super.onStart()
        absCallBack.onStart()
    }

    override fun onError(e: ApiException) {
        absCallBack.onError(e)
    }

    override fun onNext(t: T) {
        super.onNext(t)
        absCallBack.onSuccess(t)
    }

    override fun onComplete() {
        super.onComplete()
        absCallBack.onComplete()
    }
}