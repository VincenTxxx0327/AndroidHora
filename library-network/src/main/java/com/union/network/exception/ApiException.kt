@file:Suppress("DEPRECATION")

package com.union.network.exception

import android.net.ParseException
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import com.union.network.model.ApiResult
import org.apache.http.conn.ConnectTimeoutException
import org.json.JSONException
import retrofit2.HttpException
import java.io.NotSerializableException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

/**
 * 统一处理了API异常错误
 * @Author： VincenT
 * @Time： 2023/8/15 20:42
 */
class ApiException(throwable: Throwable, code: Int) : Exception(throwable) {
    private val code: Int
    private var displayMessage: String? = null
    private var apiMessage: String? = null

    init {
        this.code = code
        apiMessage = throwable.message
    }

    fun getCode(): Int {
        return code
    }

    fun getDisplayMessage(): String? {
        return displayMessage
    }

    fun setDisplayMessage(msg: String) {
        displayMessage = "$msg(code:$code)"
    }

    fun getApiMessage(): String? {
        return apiMessage
    }

    /**
     * 约定异常
     */
    object ERROR {
        /**
         * 未知错误
         */
        const val UNKNOWN = 1000

        /**
         * 解析错误
         */
        const val PARSE_ERROR = UNKNOWN + 1

        /**
         * 网络错误
         */
        const val NETWORD_ERROR = PARSE_ERROR + 1

        /**
         * 协议出错
         */
        const val HTTP_ERROR = NETWORD_ERROR + 1

        /**
         * 证书出错
         */
        const val SSL_ERROR = HTTP_ERROR + 1

        /**
         * 连接超时
         */
        const val TIMEOUT_ERROR = SSL_ERROR + 1

        /**
         * 调用错误
         */
        const val INVOKE_ERROR = TIMEOUT_ERROR + 1

        /**
         * 类转换错误
         */
        const val CAST_ERROR = INVOKE_ERROR + 1

        /**
         * 请求取消
         */
        const val REQUEST_CANCEL = CAST_ERROR + 1

        /**
         * 未知主机错误
         */
        const val UNKNOWNHOST_ERROR = REQUEST_CANCEL + 1

        /**
         * 空指针错误
         */
        const val NULLPOINTER_EXCEPTION = UNKNOWNHOST_ERROR + 1
    }

    companion object {
        //对应HTTP的状态码
        private const val BADREQUEST = 400
        private const val UNAUTHORIZED = 401
        private const val FORBIDDEN = 403
        private const val NOT_FOUND = 404
        private const val METHOD_NOT_ALLOWED = 405
        private const val REQUEST_TIMEOUT = 408
        private const val INTERNAL_SERVER_ERROR = 500
        private const val BAD_GATEWAY = 502
        private const val SERVICE_UNAVAILABLE = 503
        private const val GATEWAY_TIMEOUT = 504
        const val UNKNOWN = 1000
        const val PARSE_ERROR = 1001
        fun <T> isOk(apiResult: ApiResult<T>?): Boolean {
            if (apiResult == null) return false
            return apiResult.isOk()
        }

        @Suppress("KotlinConstantConditions")
        fun handleException(e: Throwable): ApiException {
            val ex: ApiException
            return when (e) {
                is HttpException -> {
                    ex = ApiException(e, e.code())
                    /*switch (httpException.code()) {
                                   case BADREQUEST:
                                   case UNAUTHORIZED:
                                   case FORBIDDEN:
                                   case NOT_FOUND:
                                   case REQUEST_TIMEOUT:
                                   case GATEWAY_TIMEOUT:
                                   case INTERNAL_SERVER_ERROR:
                                   case BAD_GATEWAY:
                                   case SERVICE_UNAVAILABLE:
                                   default:
                                       ex.message = "网络错误,Code:"+httpException.code()+" ,err:"+httpException.getMessage();
                                       break;
                               }*/
                    ex.apiMessage = e.message
                    ex
                }

                is ServerException -> {
                    val resultException: ServerException = e
                    ex = ApiException(resultException, resultException.getErrCode())
                    ex.apiMessage = resultException.getApiMessage()
                    ex
                }

                is JsonParseException, is JSONException, is JsonSyntaxException, is JsonSerializer<*>, is NotSerializableException, is ParseException -> {
                    ex = ApiException(e, ERROR.PARSE_ERROR)
                    ex.apiMessage = "解析错误"
                    ex
                }

                is ClassCastException -> {
                    ex = ApiException(e, ERROR.CAST_ERROR)
                    ex.apiMessage = "类型转换错误"
                    ex
                }

                is ConnectException -> {
                    ex = ApiException(e, ERROR.NETWORD_ERROR)
                    ex.apiMessage = "连接失败"
                    ex
                }

                is SSLHandshakeException -> {
                    ex = ApiException(e, ERROR.SSL_ERROR)
                    ex.apiMessage = "证书验证失败"
                    ex
                }

                is ConnectTimeoutException -> {
                    ex = ApiException(e, ERROR.TIMEOUT_ERROR)
                    ex.apiMessage = "连接超时"
                    ex
                }

                is SocketTimeoutException -> {
                    ex = ApiException(e, ERROR.TIMEOUT_ERROR)
                    ex.apiMessage = "连接超时"
                    ex
                }

                is UnknownHostException -> {
                    ex = ApiException(e, ERROR.UNKNOWNHOST_ERROR)
                    ex.apiMessage = "无法解析该域名"
                    ex
                }

                is NullPointerException -> {
                    ex = ApiException(e, ERROR.NULLPOINTER_EXCEPTION)
                    ex.apiMessage = "NullPointerException"
                    ex
                }

                else -> {
                    ex = ApiException(e, ERROR.UNKNOWN)
                    ex.apiMessage = "未知错误"
                    ex
                }
            }
        }
    }
}