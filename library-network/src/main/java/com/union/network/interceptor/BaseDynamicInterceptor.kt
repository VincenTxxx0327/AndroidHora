package com.union.network.interceptor

import com.union.network.utils.HttpLog
import com.union.network.utils.UTF8
import com.union.network.utils.createUrlFromParams
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.*

/**
 * 动态拦截器
 * 主要功能是针对参数：
 * 1.可以获取到全局公共参数和局部参数，统一进行签名sign
 * 2.可以自定义动态添加参数，类似时间戳timestamp是动态变化的，token（登录了才有），参数签名等
 * 3.参数值是经过UTF-8编码的
 * 4.默认提供询问是否动态签名（签名需要自定义），动态添加时间戳等
 * @Author： VincenT
 * @Time： 2023/8/15 20:58
 */
@Suppress("UNCHECKED_CAST")
abstract class BaseDynamicInterceptor<R : BaseDynamicInterceptor<R>> : Interceptor {
    private var httpUrl: HttpUrl? = null
    private var isSign = false          //是否需要签名
    private var timeStamp = false       //是否需要追加时间戳
    private var accessToken = false     //是否需要添加token

    fun sign(sign: Boolean): R {
        isSign = sign
        return this as R
    }

    fun timeStamp(timeStamp: Boolean): R {
        this.timeStamp = timeStamp
        return this as R
    }

    fun accessToken(accessToken: Boolean): R {
        this.accessToken = accessToken
        return this as R
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request: Request = chain.request()
        if (request.method == "GET") {
            httpUrl = parseUrl(request.url.toUrl().toString()).toHttpUrl()
            request = addGetParamsSign(request)
        } else if (request.method == "POST") {
            httpUrl = request.url
            request = addPostParamsSign(request)
        }
        return chain.proceed(request)
    }

    /**
     * @return GET添加签名和公共动态参数
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    private fun addGetParamsSign(req: Request): Request {
        var request = req
        var httpUrl = request.url
        val newBuilder = httpUrl.newBuilder()

        //获取原有的参数
        val nameSet = httpUrl.queryParameterNames
        val nameList = ArrayList<String>()
        nameList.addAll(nameSet)
        val oldParams = TreeMap<String, String>()
        for (i in nameList.indices) {
            val values = httpUrl.queryParameterValues(nameList[i])
            val value = if (values.isNotEmpty()) values[0] ?: "" else ""
            oldParams[nameList[i]] = value
        }
        val nameKeys = listOf(nameList).toString()
        //拼装新的参数
        val newParams = dynamic(oldParams)
        for ((key, value) in newParams) {
            val urlValue = URLEncoder.encode(value, UTF8.name())
            //原来的URl: https://xxx.xxx.xxx/app/chairdressing/skinAnalyzePower/skinTestResult?appId=10101
            if (!nameKeys.contains(key)) { //避免重复添加
                newBuilder.addQueryParameter(key, urlValue)
            }
        }
        httpUrl = newBuilder.build()
        request = request.newBuilder().url(httpUrl).build()
        return request
    }

    /**
     * @return POST添加签名和公共动态参数
     * @throws UnsupportedEncodingException
     */
    @Throws(UnsupportedEncodingException::class)
    private fun addPostParamsSign(req: Request): Request {
        var request = req
        if (request.body is FormBody) {
            val bodyBuilder = FormBody.Builder()
            var formBody = request.body as FormBody
            //原有的参数
            val oldParams = TreeMap<String, String>()
            for (i in 0 until formBody.size) {
                oldParams[formBody.encodedName(i)] = formBody.encodedValue(i)
            }
            //拼装新的参数
            val newParams = dynamic(oldParams)
            for ((key, value) in newParams) {
                val encValue = URLDecoder.decode(value, UTF8.name())
                bodyBuilder.addEncoded(key, encValue)
            }
            httpUrl?.let {
                val url = createUrlFromParams(it.toUrl().toString(), newParams)
                HttpLog.i(url)
            }
            formBody = bodyBuilder.build()
            request = request.newBuilder().post(formBody).build()
        } else if (request.body is MultipartBody) {
            var multipartBody = request.body as MultipartBody
            val bodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            val oldParts = multipartBody.parts

            //拼装新的参数
            val newParts = ArrayList<MultipartBody.Part>()
            newParts.addAll(oldParts)
            val oldParams = TreeMap<String, String>()
            val newParams = dynamic(oldParams)
            for ((key, value) in newParams) {
                val part = MultipartBody.Part.createFormData(key, value)
                newParts.add(part)
            }
            for (part in newParts) {
                bodyBuilder.addPart(part)
            }
            multipartBody = bodyBuilder.build()
            request = request.newBuilder().post(multipartBody).build()
        }
        return request
    }

    //解析前：https://xxx.xxx.xxx/app/chairdressing/skinAnalyzePower/skinTestResult?appId=10101
    //解析后：https://xxx.xxx.xxx/app/chairdressing/skinAnalyzePower/skinTestResult
    private fun parseUrl(url: String): String {
        var retUrl = url
        if ("" != retUrl && retUrl.contains("?")) { // 如果URL不是空字符串
            retUrl = retUrl.substring(0, retUrl.indexOf('?'))
        }
        return retUrl
    }

    /**
     * 动态处理参数
     *
     * @param dynamicMap
     * @return 返回新的参数集合
     */
    abstract fun dynamic(dynamicMap: TreeMap<String, String>?): TreeMap<String, String>
}