package com.union.network.callback

import com.union.network.cache.model.CacheResult
import com.union.network.callback.listener.CallType
import com.union.network.exception.ApiException
import com.union.network.model.Progress
import com.union.network.utils.Utils
import io.reactivex.disposables.Disposable
import java.lang.reflect.Type

/**
 * 网络请求回调
 * @Author： VincenT
 * @Time： 2023/8/15 18:38
 */
abstract class AbsCallback<T> : CallType<T> {
    abstract fun onStart()
    abstract fun onError(e: ApiException?)
    abstract fun onSuccess(t: T)
    abstract fun onComplete()

    override fun onStart(disposable: Disposable) {}

    override fun onSuccess(cacheResult: CacheResult<T>) {}

    override fun onError(cacheResult: CacheResult<T>) {}

    override fun uploadProgress(progress: Progress) {}

    override fun downloadProgress(progress: Progress) {}

    override fun getType(): Type { //获取需要解析的泛型T类型
        return Utils.findNeedClass(javaClass)
    }

    override fun getRawType(): Type { //获取需要解析的泛型T raw类型
        return Utils.findRawType(javaClass)
    }
}