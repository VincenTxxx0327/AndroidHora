package com.union.network.cookie

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * cookie管理器
 * @Author： VincenT
 * @Time： 2023/8/15 20:37
 */
@Suppress("SENSELESS_COMPARISON")
class CookieManger : CookieJar {
    private val cookieStore: PersistentCookieStore by lazy { PersistentCookieStore() }

    fun addCookies(cookies: List<Cookie>) {
        cookieStore.addCookies(cookies)
    }

    fun saveFromResponse(url: HttpUrl, cookie: Cookie) {
        if (cookie != null) {
            cookieStore.add(url, cookie)
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies != null && cookies.isNotEmpty()) {
            for (item in cookies) {
                cookieStore.add(url, item)
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url]
    }

    fun remove(url: HttpUrl, cookie: Cookie) {
        cookieStore.remove(url, cookie)
    }

    fun removeAll() {
        cookieStore.removeAll()
    }
}