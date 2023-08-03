package com.union.hora.webclient

import android.webkit.WebViewClient

object WebClientFactory {

    val JIAN_SHU = "https://www.jianshu.com"

    fun create(url: String): WebViewClient {
        return when {
            url.startsWith(JIAN_SHU) -> JianShuWebClient()
            else -> BaseWebClient()
        }
    }

}