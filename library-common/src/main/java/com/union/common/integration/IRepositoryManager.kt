package com.union.common.integration

import androidx.annotation.Nullable
import retrofit2.Retrofit

/**
 * 用来管理网络请求层,以及数据缓存层,以后可能添加数据库请求层
 * @Author： VincenT
 * @Time： 2023/8/24 16:37
 */
interface IRepositoryManager {

    /**
     * 根据传入的 Class 获取对应的 Retrofit service
     *
     * @param serviceClass Retrofit service class
     * @param retrofit 传入新的 Retrofit
     * @return Retrofit service
     */
    fun <T> obtainRetrofitService(serviceClass: Class<T>, retrofit: Retrofit? = null): T?

    interface ObtainServiceDelegate {
        @Nullable
        fun <T> createRetrofitService(retrofit: Retrofit?, serviceClass: Class<T>?): T?
    }
}

inline fun <reified T> IRepositoryManager.obtainRetrofitService(retrofit: Retrofit?): T? {
    return obtainRetrofitService(T::class.java, retrofit)
}