package com.union.network.interceptor

import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http.promisesBody
import okio.Buffer
import java.io.IOException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * 设置日志拦截器
 * 提供了详细、易懂的日志打印
 * @Author： VincenT
 * @Time： 2023/8/15 21:16
 */
class HttpLoggingInterceptor : Interceptor {
    @Volatile
    private var level = Level.NONE
    private var logger: Logger
    private var tag: String
    private var isLogEnable = false

    enum class Level {
        NONE,  //不打印log
        BASIC,  //只打印 请求首行 和 响应首行
        HEADERS,  //打印请求和响应的所有 Header
        BODY //所有数据全部打印
    }

    fun log(message: String?) {
        logger.log(java.util.logging.Level.INFO, message)
    }

    constructor(tag: String) {
        this.tag = tag
        logger = Logger.getLogger(tag)
    }

    constructor(tag: String, isLogEnable: Boolean) {
        this.tag = tag
        this.isLogEnable = isLogEnable
        logger = Logger.getLogger(tag)
    }

    fun setLevel(level: Level?): HttpLoggingInterceptor {
        if (level == null) throw NullPointerException("level == null. Use Level.NONE instead.")
        this.level = level
        return this
    }

    fun getLevel(): Level {
        return level
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        if (level == Level.NONE) {
            return chain.proceed(request)
        }

        //请求日志拦截
        logForRequest(request, chain.connection())

        //执行请求，计算请求时间
        val startNs = System.nanoTime()
        val response: Response = try {
            chain.proceed(request)
        } catch (e: Exception) {
            log("<-- HTTP FAILED: $e")
            throw e
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        //Logc.e(tag, "+++++++++++++++++++++++++++end+++++++++++耗时:" + tookMs + "毫秒");

        //响应日志拦截
        return logForResponse(response, tookMs)
    }

    @Throws(IOException::class)
    private fun logForRequest(request: Request, connection: Connection?) {
        log("-------------------------------request-------------------------------")
        val logBody = level == Level.BODY
        val logHeaders = level == Level.BODY || level == Level.HEADERS
        val requestBody = request.body
        val hasRequestBody = requestBody != null
        val protocol = connection?.protocol() ?: Protocol.HTTP_1_1
        try {
            val requestStartMessage = "--> " + request.method + ' ' + URLDecoder.decode(request.url.toUrl().toString(), UTF8.name()) + ' ' + protocol
            log(requestStartMessage)
            if (logHeaders) {
                val headers = request.headers
                var i = 0
                val count = headers.size
                while (i < count) {
                    log("\t" + headers.name(i) + ": " + headers.value(i))
                    i++
                }

                //log(" ");
                if (logBody && hasRequestBody) {
                    if (isPlaintext(requestBody!!.contentType())) {
                        bodyToString(request)
                    } else {
                        log("\tbody: maybe [file part] , too large too print , ignored!")
                    }
                }
            }
        } catch (e: Exception) {
            e(e)
        } finally {
            log("--> END " + request.method)
        }
    }

    private fun logForResponse(response: Response, tookMs: Long): Response {
        log("-------------------------------response-------------------------------")
        val builder: Response.Builder = response.newBuilder()
        val clone: Response = builder.build()
        var responseBody = clone.body
        val logBody = level == Level.BODY
        val logHeaders = level == Level.BODY || level == Level.HEADERS
        try {
            log("<-- " + clone.code + ' ' + clone.message + ' ' + URLDecoder.decode(clone.request.url.toUrl().toString(), UTF8.name()) + " (" + tookMs + "ms）")
            if (logHeaders) {
                log(" ")
                val headers = clone.headers
                var i = 0
                val count = headers.size
                while (i < count) {
                    log("\t" + headers.name(i) + ": " + headers.value(i))
                    i++
                }
                log(" ")
                if (logBody && clone.promisesBody()) {
                    if (isPlaintext(responseBody.contentType())) {
                        val body = responseBody.string()
                        log("\tbody:$body")
                        responseBody = body.toResponseBody(responseBody.contentType())
                        return response.newBuilder().body(responseBody).build()
                    } else {
                        log("\tbody: maybe [file part] , too large too print , ignored!")
                    }
                }
                log(" ")
            }
        } catch (e: Exception) {
            e(e)
        } finally {
            log("<-- END HTTP")
        }
        return response
    }

    private fun bodyToString(request: Request) {
        try {
            val copy = request.newBuilder().build()
            val buffer = Buffer()
            copy.body!!.writeTo(buffer)
            var charset = UTF8
            val contentType = copy.body!!.contentType()
            if (contentType != null) {
                charset = contentType.charset(UTF8)
            }
            val result = buffer.readString(charset!!)
            log("\tbody:" + URLDecoder.decode(replacer(result), UTF8.name()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun replacer(content: String): String {
        var data = content
        try {
            data = data.replace("%(?![0-9a-fA-F]{2})".toRegex(), "%25")
            data = data.replace("\\+".toRegex(), "%2B")
            data = URLDecoder.decode(data, "utf-8")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    fun e(t: Throwable) {
        if (isLogEnable) t.printStackTrace()
    }

    companion object {
        private val UTF8 = Charset.forName("UTF-8")

        /**
         * Returns true if the body in question probably contains human readable text. Uses a small sample
         * of code points to detect unicode control characters commonly used in binary file signatures.
         */
        @Suppress("SENSELESS_COMPARISON")
        fun isPlaintext(mediaType: MediaType?): Boolean {
            if (mediaType == null) return false
            if (mediaType.type != null && (mediaType.type == "text")) {
                return true
            }
            var subtype = mediaType.subtype
            if (subtype != null) {
                subtype = subtype.lowercase(Locale.getDefault())
                if (subtype.contains("x-www-form-urlencoded") ||
                    subtype.contains("json") ||
                    subtype.contains("xml") ||
                    subtype.contains("html")
                ) //
                    return true
            }
            return false
        }
    }
}