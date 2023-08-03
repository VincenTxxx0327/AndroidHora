package com.union.hora.http.factory

import androidx.annotation.NonNull
import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonParseException
import com.google.gson.JsonSyntaxException
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.tencent.bugly.proguard.T
import com.union.hora.http.bean.BaseResponse
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


class CommonGsonFactory private constructor(private val gson: Gson) : Converter.Factory() {

    companion object {
        @JvmOverloads  // Guarding public API nullability.
        fun create(gson: Gson? = Gson().newBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()): CommonGsonFactory {
            if (gson == null) throw NullPointerException("gson == null")
            return CommonGsonFactory(gson)
        }
    }

    override fun responseBodyConverter(type: Type, annotations: Array<out Annotation>, retrofit: Retrofit): Converter<ResponseBody, *> {
        val adapter: TypeAdapter<*> = gson.getAdapter(TypeToken.get(type))
        return GsonResponseBodyConverter(gson, adapter)
    }

    override fun requestBodyConverter(type: Type, parameterAnnotations: Array<out Annotation>, methodAnnotations: Array<out Annotation>, retrofit: Retrofit): Converter<*, RequestBody> {
        val adapter: TypeAdapter<*> = gson.getAdapter(TypeToken.get(type))
        return GsonRequestBodyConverter<Any>(gson, adapter)
    }

}


class GsonRequestBodyConverter<T>(private val gson: Gson, private val adapter: TypeAdapter<*>) : Converter<T, RequestBody> {
    @Throws(IOException::class)
    override fun convert(@NonNull value: T): RequestBody {
        val buffer = okio.Buffer()
        val writer = OutputStreamWriter(buffer.outputStream(), UTF_8)
        val jsonWriter: JsonWriter = gson.newJsonWriter(writer)
        adapter.write(jsonWriter, value as Nothing?)
        jsonWriter.close()
        return buffer.readByteString().toRequestBody(MEDIA_TYPE)
    }

    companion object {
        private val MEDIA_TYPE: MediaType = "application/json; charset=UTF-8".toMediaType()
        private val UTF_8: Charset = StandardCharsets.UTF_8
    }
}

class GsonResponseBodyConverter<T> internal constructor(private val gson: Gson, private val adapter: TypeAdapter<T>) : Converter<ResponseBody, T> {
    @Throws(IOException::class)
    override fun convert(value: ResponseBody): T {
        val jsonReader = gson.newJsonReader(value.charStream())
//        val map: BaseResponse = gson.fromJson(value.string(), BaseResponse::class.java)
//        if (map.code ==501) {
//            throw JsonParseException("Please log in first")
//        }
        return value.use { value ->
            val result = adapter.read(jsonReader)
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw JsonIOException("JSON document was not fully consumed.")
            }
            result
        }
    }
}

