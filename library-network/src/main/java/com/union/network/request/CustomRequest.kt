package com.union.network.request

import com.union.common.utils.getAppComponent
import com.union.network.callback.AbsCallback
import com.union.network.request.base.BaseRequest
import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody

/**
 * 自定义请求，例如你有自己的ApiService
 * @Author： VincenT
 * @Time： 2023/8/15 22:03
 */
class CustomRequest(url: String) : BaseRequest<CustomRequest>(url) {

    private var observable: Observable<ResponseBody>? = null

    inline fun <reified R> create(): R? {
        return create(R::class.java)
    }

    fun <R> create(serviceClass: Class<R>): R? {
        return getAppComponent().repositoryManager()
            .obtainRetrofitService(serviceClass, retrofit)
    }

    fun <T> apiCall(observable: Observable<ResponseBody>?, callback: AbsCallback<T>) {
        this.observable = observable
        build().execute(callback)
    }

    override fun generateRequest(): Observable<ResponseBody>? {
        return observable
    }
}