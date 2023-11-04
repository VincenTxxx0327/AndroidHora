package com.union.network.request

import com.union.network.request.base.BaseRequest
import io.reactivex.rxjava3.core.Observable
import okhttp3.ResponseBody

/**
 * get请求
 * @Author： VincenT
 * @Time： 2023/8/15 22:05
 */
class GetRequest(url: String) : BaseRequest<GetRequest>(url) {

    override fun generateRequest(): Observable<ResponseBody>? {
        return apiService?.get(url!!, httpParams.urlParamsMap)
    }
}