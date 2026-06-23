package com.union.hora.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

class ApiService {
    companion object {
        private const val BASE_URL = "https://api.example.com/"
        private const val TIMEOUT = 30L

        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build()
        }

        suspend fun getRequest(url: String): Response {
            val request = Request.Builder()
                .url(BASE_URL + url)
                .get()
                .build()
            return okHttpClient.newCall(request).execute()
        }
    }
}