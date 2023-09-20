package com.union.network.request.base

import com.union.network.body.ProgressResponseCallback
import com.union.network.model.HttpParams
import com.union.network.utils.createUrlFromParams
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.http.Body
import java.io.File
import java.io.InputStream

/**
 * body请求的基类
 * @Author： VincenT
 * @Time： 2023/8/15 22:01
 */
@Suppress("UNCHECKED_CAST", "unused")
abstract class BaseBodyRequest<R : BaseBodyRequest<R>>(url: String) : BaseRequest<R>(url), BaseBody<R> {

    internal var mediaType: MediaType? = null           //上传的文本内容
    internal var content: String? = null                //上传的文本内容
    internal var json: String? = null                   //上传的Json
    internal var bytes: ByteArray? = null               //上传的字节数据
    internal var any: Any? = null                       //上传的对象
    internal var requestBody: RequestBody? = null       //自定义的请求体
    internal var isAddParamsToUrl = true                //是否把 Params 拼接到 Url

//    enum class UploadType {
//        /**
//         * MultipartBody.Part方式上传
//         */
//        PART,
//
//        /**
//         * Map RequestBody方式上传
//         */
//        BODY
//    }

    //    private var currentUploadType = UploadType.PART

    /**
     * 用于调用 upXxx() 相关函数，并且有传入 urlParams 时，把 urlParams 拼接到 url 上
     */
    protected fun getNewUrl(): String {
        if (isAddParamsToUrl && httpParams.urlParamsMap.isNotEmpty()) {
            return createUrlFromParams(url!!, httpParams.urlParamsMap)
        }
        return url ?: ""
    }

    override fun requestBody(requestBody: RequestBody): R {
        this.requestBody = requestBody
        return this as R
    }

    override fun params(key: String, file: File, responseCallBack: ProgressResponseCallback): R {
        httpParams.put(key, file, responseCallBack)
        return this as R
    }

    override fun params(key: String, stream: InputStream, fileName: String, responseCallBack: ProgressResponseCallback): R {
        httpParams.put(key, stream, fileName, responseCallBack)
        return this as R
    }

    override fun params(key: String, bytes: ByteArray, fileName: String, responseCallBack: ProgressResponseCallback): R {
        httpParams.put(key, bytes, fileName, responseCallBack)
        return this as R
    }

    override fun params(key: String, file: File, fileName: String, responseCallBack: ProgressResponseCallback): R {
        httpParams.put(key, file, fileName, responseCallBack)
        return this as R
    }

    override fun params(key: String, file: File, fileName: String, contentType: MediaType, responseCallBack: ProgressResponseCallback): R {
        httpParams.put(key, file, fileName, contentType, responseCallBack)
        return this as R
    }

    override fun <T> params(key: String, file: T, fileName: String, contentType: MediaType, responseCallBack: ProgressResponseCallback): R {
        httpParams.put(key, file, fileName, contentType, responseCallBack)
        return this as R
    }

    override fun addFileParams(key: String, files: List<File>, responseCallBack: ProgressResponseCallback): R {
        httpParams.putFileParams(key, files, responseCallBack)
        return this as R
    }

    override fun addFileWrapperParams(key: String, fileWrappers: List<HttpParams.FileWrapper<R>>): R {
        httpParams.putFileWrapperParams(key, fileWrappers)
        return this as R
    }

    override fun upString(content: String): R {
        this.content = content
        mediaType = HttpParams.MEDIA_TYPE_PLAIN
        return this as R
    }

    override fun upString(content: String, mediaType: MediaType): R {
        this.content = content
        this.mediaType = mediaType
        return this as R
    }

    override fun upJson(json: String): R {
        this.json = json
        return this as R
    }

    override fun upJson(jsonObject: JSONObject): R {
        this.json = jsonObject.toString()
        this.mediaType = HttpParams.MEDIA_TYPE_JSON
        return this as R
    }

    override fun upJson(jsonArray: JSONArray): R {
        this.json = jsonArray.toString()
        this.mediaType = HttpParams.MEDIA_TYPE_JSON
        return this as R
    }

    override fun upBytes(bs: ByteArray): R {
        this.bytes = bs
        return this as R
    }

    override fun upBytes(bs: ByteArray, mediaType: MediaType): R {
        this.bytes = bs
        this.mediaType = mediaType
        return this as R
    }

    override fun upObject(@Body any: Any): R {
        this.any = any
        return this as R
    }

    override fun addParamsToUrl(isAddParamsToUrl: Boolean): R {
        this.isAddParamsToUrl = isAddParamsToUrl
        return this as R
    }


//    /**
//     * 上传文件的方式，默认part方式上传
//     */
//    fun <T> uploadType(uploadType: UploadType): R {
//        currentUploadType = uploadType
//        return this as R
//    }
//
//    @Suppress("SENSELESS_COMPARISON")
//    override fun generateRequest(): Observable<ResponseBody>? {
//        if (requestBody != null) { //自定义的请求体
//            return apiManager!!.postBody(url, requestBody!!)
//        } else if (json != null) { //上传的Json
//            val body = json!!.toRequestBody("application/json; charset=utf-8".toMediaType())
//            return apiManager!!.postJson(url, body)
//        } else if (any != null) { //自定义的请求object
//            return apiManager!!.postBody(url, any!!)
//        } else if (content != null) { //上传的文本内容
//            val body = RequestBody.create(mediaType, content!!)
//            return apiManager!!.postBody(url, body)
//        } else if (bytes != null) { //上传的字节数据
//            val body = RequestBody.create("application/octet-stream".toMediaType(), bytes!!)
//            return apiManager!!.postBody(url, body)
//        }
//        return if (params.fileParamsMap!!.isEmpty()) {
//            apiManager!!.post(url, params.urlParamsMap!!)
//        } else {
//            if (currentUploadType == UploadType.PART) { //part方式上传
//                uploadFilesWithParts()
//            } else { //body方式上传
//                uploadFilesWithBodys()
//            }
//        }
//    }

//    protected fun uploadFilesWithParts(): Observable<ResponseBody> {
//        val parts: MutableList<MultipartBody.Part> = ArrayList()
//        //拼接参数键值对
//        for (entry: Map.Entry<String, String> in params.urlParamsMap!!.entries) {
//            parts.add(MultipartBody.Part.createFormData(entry.key, entry.value))
//        }
//        //拼接文件
//        for (entry: Map.Entry<String, List<HttpParams.FileWrapper<*>>> in params.fileParamsMap!!.entries) {
//            val fileValues: List<HttpParams.FileWrapper<*>> = entry.value
//            for (fileWrapper in fileValues) {
//                val part: MultipartBody.Part = addFile(entry.key, fileWrapper)
//                parts.add(part)
//            }
//        }
//        return apiManager!!.uploadFiles(url, parts)
//    }
//
//    protected fun uploadFilesWithBodys(): Observable<ResponseBody> {
//        val mBodyMap: MutableMap<String, RequestBody> = HashMap()
//        //拼接参数键值对
//        for (entry: Map.Entry<String, String?> in params.urlParamsMap!!.entries) {
//            val body = entry.value!!.toRequestBody("text/plain".toMediaType())
//            mBodyMap[entry.key] = body
//        }
//        //拼接文件
//        for (entry: Map.Entry<String, List<HttpParams.FileWrapper<*>>> in params.fileParamsMap!!.entries) {
//            val fileValues: List<HttpParams.FileWrapper<*>> = entry.value
//            for (fileWrapper in fileValues) {
//                mBodyMap[entry.key] = UploadProgressRequestBody(getRequestBody(fileWrapper), fileWrapper.responseCallBack!!)
//            }
//        }
//        return apiManager!!.uploadFiles(url, mBodyMap)
//    }
//
//    //文件方式
//    private fun addFile(key: String, fileWrapper: HttpParams.FileWrapper<*>): MultipartBody.Part {
//        //MediaType.parse("application/octet-stream", file)
//        val requestBody = getRequestBody(fileWrapper)
//        Utils.checkNotNull(requestBody, "requestBody==null fileWrapper.file must is File/InputStream/byte[]")
//        //包装RequestBody，在其内部实现上传进度监听
//        return if (fileWrapper.responseCallBack != null) {
//            MultipartBody.Part.createFormData(key, fileWrapper.fileName, UploadProgressRequestBody(requestBody, fileWrapper.responseCallBack!!))
//        } else {
//            MultipartBody.Part.createFormData(key, fileWrapper.fileName, requestBody!!)
//        }
//    }
//
//    private fun getRequestBody(fileWrapper: HttpParams.FileWrapper<*>): RequestBody? {
//        var requestBody: RequestBody? = null
//        if (fileWrapper.file is File) {
//            requestBody = (fileWrapper.file as File).asRequestBody(fileWrapper.contentType)
//        } else if (fileWrapper.file is InputStream) {
//            requestBody = RequestBodyUtils.create(fileWrapper.contentType, fileWrapper.file as InputStream)
//        } else if (fileWrapper.file is ByteArray) {
//            requestBody = (fileWrapper.file as ByteArray).toRequestBody(fileWrapper.contentType)
//        }
//        return requestBody
//    }
}