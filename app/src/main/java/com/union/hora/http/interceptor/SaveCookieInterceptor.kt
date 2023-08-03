package com.union.hora.http.interceptor

import com.union.hora.app.constant.HttpConstant
import okhttp3.Interceptor
import okhttp3.Response

class SaveCookieInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        val requestUrl = request.url.toString()
        val domain = request.url.host
        // set-cookie maybe has multi, login to save cookie
        if ((requestUrl.contains(HttpConstant.KEY_USER_LOGIN)
                        || requestUrl.contains(HttpConstant.KEY_USER_REGISTER))
                && !response.headers(HttpConstant.SET_COOKIE_KEY).isEmpty()) {
            val cookies = response.headers(HttpConstant.SET_COOKIE_KEY)
            val cookie = HttpConstant.encodeCookie(cookies)
            HttpConstant.saveCookie(requestUrl, domain, cookie)
        }
        return response
    }
}