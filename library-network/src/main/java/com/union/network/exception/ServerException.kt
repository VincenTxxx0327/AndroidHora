package com.union.network.exception

/**
 * 处理服务器异常
 * @Author： VincenT
 * @Time： 2023/8/15 20:50
 */
class ServerException(errCode: Int, msg: String) : RuntimeException(msg) {
    private val errCode: Int
    private var apiMessage: String? = null

    init {
        this.errCode = errCode
        apiMessage = msg
    }

    fun getErrCode(): Int {
        return errCode
    }

    fun getApiMessage(): String {
        return apiMessage ?: ""
    }
}