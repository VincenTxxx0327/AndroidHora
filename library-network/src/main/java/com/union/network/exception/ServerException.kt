package com.union.network.exception

/**
 * 处理服务器异常
 * @Author： VincenT
 * @Time： 2023/8/15 20:50
 */
class ServerException(errCode: Int, msg: String) : RuntimeException(msg) {
    private val errCode: Int
    override var message: String? = null

    init {
        this.errCode = errCode
        message = msg
    }

    fun getErrCode(): Int {
        return errCode
    }

    fun getMessage(): String {
        return message ?: ""
    }
}