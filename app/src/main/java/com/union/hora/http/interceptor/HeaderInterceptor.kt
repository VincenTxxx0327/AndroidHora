package com.union.hora.http.interceptor

import com.union.hora.app.constant.Constant
import com.union.hora.app.constant.HttpConstant
import com.union.hora.utils.Preference
import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor : Interceptor {

    private var token: String by Preference(Constant.TOKEN_KEY, "")

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val builder = request.newBuilder()

        builder.addHeader("Content-type", "application/json; charset=utf-8")
        // .header("token", token)
        // .method(request.method(), request.body())

        val domain = request.url.host
        val url = request.url.toString()
        if (domain.isNotEmpty() && (url.contains(HttpConstant.COLLECTIONS_WEBSITE)
                        || url.contains(HttpConstant.UNCOLLECTIONS_WEBSITE)
                        || url.contains(HttpConstant.ARTICLE_WEBSITE)
                        || url.contains(HttpConstant.TODO_WEBSITE)
                        || url.contains(HttpConstant.COIN_WEBSITE))) {
            val spDomain: String by Preference(domain, "")
            val cookie: String = spDomain.ifEmpty { "" }
            if (cookie.isNotEmpty()) {
                // 将 Cookie 添加到请求头
                builder.addHeader(HttpConstant.COOKIE_NAME, cookie)
            }
        }

        return chain.proceed(builder.build())
    }

}