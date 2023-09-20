package com.union.network.body

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Source
import okio.source
import java.io.IOException
import java.io.InputStream

/**
 * 请求体处理工具类
 * @Author： VincenT
 * @Time： 2023/8/15 1:27
 */
@Deprecated("")
object RequestBodyUtils {
    //public final static MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    fun create(mediaType: MediaType?, inputStream: InputStream): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return mediaType
            }

            override fun contentLength(): Long {
                return try {
                    inputStream.available().toLong()
                } catch (e: IOException) {
                    0
                }
            }

            @Throws(IOException::class)
            override fun writeTo(sink: BufferedSink) {
                var source: Source? = null
                try {
                    source = inputStream.source()
                    sink.writeAll(source)
                } finally {
//                    Util.closeQuietly(source)
                }
            }
        }
    }
}