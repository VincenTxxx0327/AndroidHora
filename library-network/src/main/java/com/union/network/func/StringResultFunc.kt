package com.union.network.func

import okhttp3.ResponseBody
import io.reactivex.rxjava3.functions.Function

/**
 *
 * @Author： VincenT
 * @Time： 2023/11/3 18:15
 */
class StringResultFunc : Function<ResponseBody, String> {

    @Throws(Exception::class)
    override fun apply(responseBody: ResponseBody) = responseBody.string()
}