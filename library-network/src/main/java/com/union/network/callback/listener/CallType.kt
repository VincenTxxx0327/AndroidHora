package com.union.network.callback.listener

import com.union.network.model.Progress
import com.union.network.cache.model.CacheResult
import io.reactivex.disposables.Disposable
import java.lang.reflect.Type

/**
 * 获取类型接口
 * @Author： VincenT
 * @Time： 2023/8/15 19:25
 */
interface CallType<T> {

    /**
     * 请求网络开始前，UI线程
     */
    fun onStart(disposable: Disposable)

    /**
     * 对返回数据进行操作的回调，UI线程
     */
    fun onSuccess(cacheResult: CacheResult<T>)

    /**
     * 请求失败，响应错误，数据解析错误等，都会回调该方法，UI线程
     */
    fun onError(cacheResult: CacheResult<T>)

    /**
     * 上传过程中的进度回调，UI线程
     */
    fun uploadProgress(progress: Progress)

    /**
     * 下载过程中的进度回调，UI线程
     */
    fun downloadProgress(progress: Progress)

    fun getType(): Type

    fun getRawType(): Type
}