package com.union.network.callback

import com.google.gson.internal.`$Gson$Types`
import com.union.network.callback.listener.CallType
import com.union.network.model.ApiResult
import com.union.network.utils.Utils
import okhttp3.ResponseBody
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 提供Clazz回调代理
 * 主要用于可以自定义ApiResult
 * @Author： VincenT
 * @Time： 2023/8/15 18:39
 */
@Deprecated("")
abstract class CallClazzProxy<T : ApiResult<R>, R>(type: Type) : CallType<T> {
    private val type: Type?

    init {
        this.type = type
    }

    fun getCallType(): Type? {
        return type
    }

    override fun getType(): Type { //CallClazz代理方式，获取需要解析的Type
        var typeArguments: Type? = null
        if (type != null) {
            typeArguments = type
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