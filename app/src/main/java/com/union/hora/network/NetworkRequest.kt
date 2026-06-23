package com.union.hora.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response

class NetworkRequest {
    companion object {
        suspend fun <T> request(
            url: String,
            onSuccess: (T) -> Unit,
            onFailed: (String) -> Unit,
            parser: (Response) -> T?
        ) {
            withContext(Dispatchers.IO) {
                try {
                    val response = ApiService.getRequest(url)
                    if (response.isSuccessful) {
                        val result = parser(response)
                        if (result != null) {
                            withContext(Dispatchers.Main) {
                                onSuccess(result)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onFailed("解析失败")
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onFailed("请求失败: ${response.code}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        onFailed("网络错误: ${e.message}")
                    }
                }
            }
        }
    }
}