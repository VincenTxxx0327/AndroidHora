package com.union.common.http

import okhttp3.HttpUrl

/**
 *
 * @Author： VincenT
 * @Time： 2023/9/19 16:49
 */
interface BaseUrl {

    /**
     * @return 在调用 Retrofit API 接口之前,使用 [okhttp3] 或其他方式,请求到正确的 [BaseUrl] 并通过此方法返回
     */
    fun url(): HttpUrl
}