package com.union.network.func

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.union.network.model.ApiResult
import com.union.network.utils.Utils
import io.reactivex.functions.Function
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 定义了ApiResult结果转换Func
 * @Author： VincenT
 * @Time： 2023/8/15 20:54
 */
@Suppress("UNCHECKED_CAST")
class ApiResultFunc<T>(type: Type) : Function<ResponseBody, ApiResult<T>> {
    protected var type: Type
    protected var gson: Gson

    init {
        gson = GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
            .serializeNulls()
            .create()
        this.type = type
    }

    @Throws(Exception::class)
    override fun apply(responseBody: ResponseBody): ApiResult<T> {
        var apiResult: ApiResult<T> = ApiResult()
        apiResult.setCode(-1)
        if (type is ParameterizedType) { //自定义ApiResult
            val cls: Class<T> = (type as ParameterizedType).rawType as Class<T>
            if (ApiResult::class.java.isAssignableFrom(cls)) {
                val params = (type as ParameterizedType).actualTypeArguments
                val clazz: Class<*> = Utils.getClass<T>(params[0], 0)
                val rawType: Class<*> = Utils.getClass<T>(type, 0)
                try {
                    val json = responseBody.string()
                    //增加是List<String>判断错误的问题
                    if (!MutableList::class.java.isAssignableFrom(rawType) && clazz == String::class.java) {
                        apiResult.setData(json as T)
                        apiResult.setCode(0)
                        /* final Type type = Utils.getType(cls, 0);
                        ApiResult result = gson.fromJson(json, type);
                        if (result != null) {
                            apiResult = result;
                            apiResult.setData((T) json);
                        } else {
                            apiResult.setMsg("json is null");
                        }*/
                    } else {
                        val result: ApiResult<T>? = gson.fromJson(json, type)
                        if (result != null) {
                            apiResult = result
                        } else {
                            apiResult.setMsg("json is null")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    apiResult.setMsg(e.message)
                } finally {
                    responseBody.close()
                }
            } else {
                apiResult.setMsg("ApiResult.class.isAssignableFrom(cls) err!!")
            }
        } else { //默认ApiResult
            try {
                val json = responseBody.string()
                val clazz: Class<T> = Utils.getClass(type, 0)
                if (clazz == String::class.java) {
                    //apiResult.setData((T) json);
                    //apiResult.setCode(0);
                    val result: ApiResult<T>? = parseApiResult(json, apiResult)
                    if (result != null) {
                        apiResult = result
                        apiResult.setData(json as T)
                    } else {
                        apiResult.setMsg("json is null")
                    }
                } else {
                    val result: ApiResult<T>? = parseApiResult(json, apiResult)
                    if (result != null) {
                        apiResult = result
                        if (apiResult.getData() != null) {
                            val data: T = gson.fromJson(apiResult.getData().toString(), clazz)
                            apiResult.setData(data)
                        } else {
                            apiResult.setMsg("ApiResult's data is null")
                        }
                    } else {
                        apiResult.setMsg("json is null")
                    }
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                apiResult.setMsg(e.message)
            } catch (e: IOException) {
                e.printStackTrace()
                apiResult.setMsg(e.message)
            } finally {
                responseBody.close()
            }
        }
        return apiResult
    }

    @Throws(JSONException::class)
    private fun parseApiResult(json: String, apiResult: ApiResult<T>?): ApiResult<T>? {
        if (TextUtils.isEmpty(json)) return null
        val jsonObject = JSONObject(json)
        if (jsonObject.has("code")) {
            apiResult?.setCode(jsonObject.getInt("code"))
        }
        if (jsonObject.has("data")) {
            apiResult?.setData(jsonObject.getString("data") as T)
        }
        if (jsonObject.has("msg")) {
            apiResult?.setMsg(jsonObject.getString("msg"))
        }
        return apiResult
    }
}