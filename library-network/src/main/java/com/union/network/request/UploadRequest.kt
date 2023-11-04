package com.union.network.request


import com.union.network.body.ProgressResponseCallback
import com.union.network.request.base.BaseBodyRequest
import com.union.network.request.base.ProgressRequestBody
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/18 14:32
 */
class UploadRequest(url: String) : BaseBodyRequest<UploadRequest>(url) {
    private var progressCallBack: ProgressResponseCallback? = null//上传回调监听

    fun updateFileCallback(progressCallBack: ProgressResponseCallback?): UploadRequest {
        this.progressCallBack = progressCallBack
        return this
    }

    override fun generateRequest(): Observable<ResponseBody>? {
        return apiService?.uploadFiles(url ?: "", generateMultipartRequestBody())
    }

    private fun generateMultipartRequestBody(): RequestBody {
        //表单提交，有文件
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        //拼接键值对
        for ((key, value) in httpParams.urlParamsMap) {
            builder.addFormDataPart(key, value)
        }
        //拼接文件
        for ((key, files) in httpParams.fileParamsMap) {
            files.forEach {
                val fileBody = (it.file as File).asRequestBody(it.contentType)
                val body = ProgressRequestBody(fileBody, callback, progressCallBack)
                builder.addFormDataPart(key, it.fileName, body)
            }
        }
        return builder.build()
    }
}