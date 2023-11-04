package com.union.network.request

import com.union.network.request.base.BaseBodyRequest
import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

/**
 * post请求
 * @Author： VincenT
 * @Time： 2023/8/15 22:05
 */
class PostRequest(url: String) : BaseBodyRequest<PostRequest>(url) {

    override fun generateRequest(): Observable<ResponseBody>? {
        when {
            requestBody != null -> {
                return apiService?.postBody(getNewUrl(), requestBody!!)
            }
            json != null -> {
                val body = json!!.toRequestBody(mediaType)
                return apiService?.postJson(getNewUrl(), body)
            }
            content != null -> {
                val body = content!!.toRequestBody(mediaType)
                return apiService?.postBody(getNewUrl(), body)
            }
            bytes != null -> {
                val body = bytes!!.toRequestBody(mediaType, 0, bytes!!.size)
                return apiService?.postBody(getNewUrl(), body)
            }
            any != null -> {
                return apiService?.postBody(getNewUrl(), any!!)
            }
            else -> return apiService?.post(url!!, httpParams.urlParamsMap)
        }
    }
}