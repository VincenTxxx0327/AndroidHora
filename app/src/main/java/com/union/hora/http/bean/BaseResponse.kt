package com.union.hora.http.bean

open class BaseResponse {
    var code: Int = 0
    var message: String = ""

    fun isSuccess() = code == 200
}