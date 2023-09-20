@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.union.network.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.Looper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.util.*

/**
 * 工具类
 * @Author： VincenT
 * @Time： 2023/8/15 21:12
 */
@Suppress("UNCHECKED_CAST")
object Utils {
    fun <T> checkNotNull(t: T?, message: String?): T {
        if (t == null) {
            throw NullPointerException(message)
        }
        return t
    }

    fun checkMain(): Boolean {
        return Thread.currentThread() === Looper.getMainLooper().thread
    }

    fun createJson(jsonString: String): RequestBody {
        checkNotNull(jsonString, "json not null!")
        return jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())
    }

    /**
     * @param name
     * @return
     */
    fun createFile(name: String): RequestBody {
        checkNotNull(name, "name not null!")
        return name.toRequestBody("multipart/form-data; charset=utf-8".toMediaType())
    }

    /**
     * @param file
     * @return
     */
    fun createFile(file: File): RequestBody {
        checkNotNull(file, "file not null!")
        return file.asRequestBody("multipart/form-data; charset=utf-8".toMediaType())
    }

    /**
     * @param file
     * @return
     */
    fun createImage(file: File): RequestBody {
        checkNotNull(file, "file not null!")
        return file.asRequestBody("image/jpg; charset=utf-8".toMediaType())
    }

    fun close(close: Closeable?) {
        if (close != null) {
            try {
                closeThrowException(close)
            } catch (ignored: IOException) {
            }
        }
    }

    @Throws(IOException::class)
    fun closeThrowException(close: Closeable?) {
        close?.close()
    }

    /**
     * find the type by interfaces
     *
     * @param cls
     * @param <R>
     * @return
    </R> */
    fun <R> findNeedType(cls: Class<R>): Type {
        val typeList = getMethodTypes(cls)
        return if (typeList.isNullOrEmpty()) {
            RequestBody::class.java
        } else typeList[0]
    }

    /**
     * MethodHandler
     */
    fun <T> getMethodTypes(cls: Class<T>): List<Type>? {
        val typeOri = cls.genericSuperclass
        var needTypes: MutableList<Type>? = null
        // if Type is T
        if (typeOri is ParameterizedType) {
            needTypes = ArrayList()
            val parentTypes = typeOri.actualTypeArguments
            for (childType in parentTypes) {
                needTypes.add(childType)
                if (childType is ParameterizedType) {
                    val childTypes = childType.actualTypeArguments
                    Collections.addAll(needTypes, *childTypes)
                }
            }
        }
        return needTypes
    }

    fun <T> getClass(type: Type, i: Int): Class<T> {
        return when (type) {
            is ParameterizedType -> { // 处理泛型类型
                getGenericClass(type, i)
            }

            is TypeVariable<*> -> {
                getClass(type.bounds[0], 0) // 处理泛型擦拭对象
            }

            else -> { // class本身也是type，强制转型
                type as Class<T>
            }
        }
    }

    fun <T> getType(type: Type, i: Int): Type {
        return when (type) {
            is ParameterizedType -> { // 处理泛型类型
                getGenericType<T>(type, i)
            }

            is TypeVariable<*> -> {
                getType<T>(type.bounds[0], 0) // 处理泛型擦拭对象
            }

            else -> { // class本身也是type，强制转型
                type
            }
        }
    }

    fun <T> getParameterizedType(type: Type, i: Int): Type {
        return when (type) {
            is ParameterizedType -> { // 处理泛型类型
                type.actualTypeArguments[i]
            }

            is TypeVariable<*> -> {
                getType<T>(type.bounds[0], 0) // 处理泛型擦拭对象
            }

            else -> { // class本身也是type，强制转型
                type
            }
        }
    }

    fun <T> getGenericClass(parameterizedType: ParameterizedType, i: Int): Class<T> {
        return when (val genericClass = parameterizedType.actualTypeArguments[i]) {
            is ParameterizedType -> { // 处理多级泛型
                genericClass.rawType as Class<T>
            }

            is GenericArrayType -> { // 处理数组泛型
                genericClass.genericComponentType as Class<T>
            }

            is TypeVariable<*> -> { // 处理泛型擦拭对象
                getClass(genericClass.bounds[0], 0)
            }

            else -> {
                genericClass as Class<T>
            }
        }
    }

    fun <T> getGenericType(parameterizedType: ParameterizedType, i: Int): Type {
        return when (val genericType = parameterizedType.actualTypeArguments[i]) {
            is ParameterizedType -> { // 处理多级泛型
                genericType.rawType
            }

            is GenericArrayType -> { // 处理数组泛型
                genericType.genericComponentType
            }

            is TypeVariable<*> -> { // 处理泛型擦拭对象
                getClass<T>(genericType.bounds[0], 0)
            }

            else -> {
                genericType
            }
        }
    }

    /**
     * 普通类反射获取泛型方式，获取需要实际解析的类型
     *
     * @param <T>
     * @return
    </T> */
    fun <T> findNeedClass(cls: Class<T>): Type {
        //以下代码是通过泛型解析实际参数,泛型必须传
        val genType = cls.genericSuperclass
        val params = (genType as ParameterizedType).actualTypeArguments
        val type = params[0]
        val finalNeedType: Type = if (params.size > 1) { //这个类似是：CacheResult<SkinTestResult> 2层
            check(type is ParameterizedType) { "没有填写泛型参数" }
            type.actualTypeArguments[0]
            //Type rawType = ((ParameterizedType) type).getRawType();
        } else { //这个类似是:SkinTestResult  1层
            type
        }
        return finalNeedType
    }

    /**
     * 普通类反射获取泛型方式，获取最顶层的类型
     */
    fun <T> findRawType(cls: Class<T>): Type {
        val genType = cls.genericSuperclass
        return getGenericType<T>(genType as ParameterizedType, 0)
    }
}