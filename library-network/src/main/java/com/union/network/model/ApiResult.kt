package com.union.network.model

/**
 * 提供的默认的标注返回api
 * @Author： VincenT
 * @Time： 2023/8/15 21:21
 */
class ApiResult<T> {
    private var code = 0
    private var msg: String? = null
    private var data: T? = null
    fun getCode(): Int {
        return code
    }

    fun setCode(code: Int) {
        this.code = code
    }

    fun getMsg(): String {
        return msg ?: ""
    }

    fun setMsg(msg: String?) {
        this.msg = msg
    }

    fun getData(): T? {
        return data
    }

    fun setData(data: T) {
        this.data = data
    }

    fun isOk(): Boolean {
        return code == 0
    }

    override fun toString(): String {
        return "ApiResult{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}'
    }
}