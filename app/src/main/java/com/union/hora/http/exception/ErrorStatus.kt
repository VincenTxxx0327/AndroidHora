package com.union.hora.http.exception

object ErrorStatus {

    const val SUCCESS = 200//响应成功

    const val TOKEN_INVALID = 401//Token 过期

    const val UNKNOWN_ERROR = 1002//未知错误

    const val SERVER_ERROR = 1003//服务器内部错误

    const val NETWORK_ERROR = 1004//网络连接超时

    const val API_ERROR = 1005//API解析异常（或者第三方数据结构更改）等其他异常
}