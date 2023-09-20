package com.union.network.model

import com.union.network.body.ProgressResponseCallback
import com.union.network.utils.escapeParams
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.InputStream
import java.io.Serializable
import java.net.URLConnection

/**
 * 普通参数
 * @Author： VincenT
 * @Time： 2023/8/15 21:22
 */
class HttpParams : Serializable {
    /**
     * 普通的键值对参数
     */
    val urlParamsMap: LinkedHashMap<String, String> = linkedMapOf()

    /**
     * 文件的键值对参数
     */
    val fileParamsMap: LinkedHashMap<String, MutableList<FileWrapper<*>>> = linkedMapOf()

    fun put(params: Map<String?, String?>?) {
        if (params?.isNotEmpty() == true) {
            urlParamsMap.putAll(escapeParams(params))
        }
    }

    fun put(params: HttpParams?) {
        if (params != null) {
            if (params.urlParamsMap.isNotEmpty()) {
                urlParamsMap.putAll(params.urlParamsMap)
            }
            if (params.fileParamsMap.isNotEmpty()) {
                fileParamsMap.putAll(params.fileParamsMap)
            }
        }
    }

    fun put(params: Map<String, String>?) {
        if (params.isNullOrEmpty()) return
        urlParamsMap.putAll(params)
    }

    fun put(key: String, value: String) {
        urlParamsMap[key] = value
    }

    fun <T : File?> put(key: String?, file: T, responseCallBack: ProgressResponseCallback?) {
        put(key, file, file!!.name, responseCallBack)
    }

    fun <T : File?> put(key: String?, file: T, fileName: String, responseCallBack: ProgressResponseCallback?) {
        put(key, file, fileName, guessMimeType(fileName), responseCallBack)
    }

    fun <T : InputStream?> put(key: String?, file: T, fileName: String, responseCallBack: ProgressResponseCallback?) {
        put(key, file, fileName, guessMimeType(fileName), responseCallBack)
    }

    fun put(key: String?, bytes: ByteArray, fileName: String, responseCallBack: ProgressResponseCallback?) {
        put(key, bytes, fileName, guessMimeType(fileName), responseCallBack)
    }

    fun put(key: String?, fileWrapper: FileWrapper<*>?) {
        if (key != null && fileWrapper != null) {
            put(key, fileWrapper.file, fileWrapper.fileName, fileWrapper.contentType, fileWrapper.responseCallBack)
        }
    }

    fun <T> put(key: String?, content: T, fileName: String, contentType: MediaType, responseCallBack: ProgressResponseCallback?) {
        if (key != null) {
            var fileWrappers = fileParamsMap[key]
            if (fileWrappers == null) {
                fileWrappers = mutableListOf()
                fileParamsMap[key] = fileWrappers
            }
            fileWrappers.add(FileWrapper<Any?>(content, fileName, contentType, responseCallBack))
        }
    }

    fun <T : File> putFileParams(key: String?, files: List<T>?, responseCallBack: ProgressResponseCallback?) {
        if (key != null && !files.isNullOrEmpty()) {
            for (file in files) {
                put<File>(key, file, responseCallBack)
            }
        }
    }

    fun putFileWrapperParams(key: String?, fileWrappers: List<FileWrapper<*>?>?) {
        if (key != null && !fileWrappers.isNullOrEmpty()) {
            for (fileWrapper in fileWrappers) {
                put(key, fileWrapper)
            }
        }
    }

    fun removeUrl(key: String?) {
        urlParamsMap.remove(key)
    }

    fun removeFile(key: String) {
        fileParamsMap.remove(key)
    }

    fun remove(key: String) {
        removeUrl(key)
        removeFile(key)
    }

    fun clear() {
        urlParamsMap.clear()
        fileParamsMap.clear()
    }

    private fun guessMimeType(path: String): MediaType {
        var retPath = path
        val fileNameMap = URLConnection.getFileNameMap()
        retPath = retPath.replace("#", "") //解决文件名中含有#号异常的问题
        var contentType = fileNameMap.getContentTypeFor(retPath)
        if (contentType == null) {
            contentType = "application/octet-stream"
        }
        return contentType.toMediaType()
    }

    /**
     * 文件类型的包装类
     */
    class FileWrapper<T>(var file: T, var fileName: String, var contentType: MediaType, responseCallBack: ProgressResponseCallback?) {
        var fileSize: Long = 0
        var responseCallBack: ProgressResponseCallback?

        init {
            if (file is File) {
                fileSize = (file as File).length()
            } else if (file is ByteArray) {
                fileSize = (file as ByteArray).size.toLong()
            }
            this.responseCallBack = responseCallBack
        }

        override fun toString(): String {
            return "FileWrapper{content=$file, fileName='$fileName, contentType=$contentType, fileSize=$fileSize}"
        }
    }

    override fun toString(): String {
        val result = StringBuilder()
        for (entry: Map.Entry<String?, String?> in urlParamsMap!!) {
            if (result.isNotEmpty()) result.append("&")
            result.append(entry.key).append("=").append(entry.value)
        }
        for (entry: Map.Entry<String, List<FileWrapper<*>>> in fileParamsMap!!) {
            if (result.isNotEmpty()) result.append("&")
            result.append(entry.key).append("=").append(entry.value)
        }
        return result.toString()
    }

    companion object {
        val MEDIA_TYPE_PLAIN = "text/plain; charset=utf-8".toMediaTypeOrNull()
        val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
        val MEDIA_TYPE_STREAM = "application/octet-stream".toMediaTypeOrNull()
    }
}