package com.union.network.callback

import com.google.gson.internal.`$Gson$Types`
import com.union.network.cache.model.CacheResult
import com.union.network.callback.listener.CallType
import com.union.network.model.ApiResult
import com.union.network.utils.Utils
import okhttp3.ResponseBody
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 提供回调代理
 * @Author： VincenT
 * @Time： 2023/8/15 18:38
 */
@Deprecated("")
abstract class CallBackProxy<T : ApiResult<R>, R>(absCallBack: AbsCallback<R>) : CallType<T> {
    var mAbsCallback: AbsCallback<R>?

    init {
        mAbsCallback = absCallBack
    }

    fun getCallBack(): AbsCallback<R>? {
        return mAbsCallback
    }

    override fun getType(): Type { //CallBack代理方式，获取需要解析的Type
        var typeArguments: Type? = null
        if (mAbsCallback != null) {
            val rawType = mAbsCallback!!.getRawType() //如果用户的信息是返回List需单独处理
            typeArguments =
                if (MutableList::class.java.isAssignableFrom(Utils.getClass<T>(rawType, 0)) || MutableMap::class.java.isAssignableFrom(Utils.getClass<T>(rawType, 0))) {
                    mAbsCallback!!.getType()
                } else if (CacheResult::class.java.isAssignableFrom(Utils.getClass<T>(rawType, 0))) {
                    val type = mAbsCallback!!.getType()
                    Utils.getParameterizedType<T>(type, 0)
                } else {
                    val type = mAbsCallback!!.getType()
                    Utils.getClass<T>(type, 0)
                }
        }
        if (typeArguments == null) {
            typeArguments = ResponseBody::class.java
        }
        var rawType: Type? = Utils.findNeedType(javaClass)
        if (rawType is ParameterizedType) {
            rawType = rawType.rawType
        }
        return `$Gson$Types`.newParameterizedTypeWithOwner(null, rawType, typeArguments)
    }
}