package com.union.network.request.base

import com.union.network.body.ProgressResponseCallback
import com.union.network.model.HttpParams
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/18 13:37
 */
interface BaseBody<R> {

    fun requestBody(requestBody: RequestBody): R

    fun params(key: String, file: File, responseCallBack: ProgressResponseCallback): R

    fun params(key: String, stream: InputStream, fileName: String, responseCallBack: ProgressResponseCallback): R

    fun params(key: String, bytes: ByteArray, fileName: String, responseCallBack: ProgressResponseCallback): R

    fun params(key: String, file: File, fileName: String, responseCallBack: ProgressResponseCallback): R

    fun params(key: String, file: File, fileName: String, contentType: MediaType, responseCallBack: ProgressResponseCallback): R

    fun <T> params(key: String, file: T, fileName: String, contentType: MediaType, responseCallBack: ProgressResponseCallback): R

    fun addFileParams(key: String, files: List<File>, responseCallBack: ProgressResponseCallback): R

    fun addFileWrapperParams(key: String, fileWrappers: List<HttpParams.FileWrapper<R>>): R

    fun upString(content: String): R

    fun upString(content: String, mediaType: MediaType): R

    fun upJson(json: String): R

    fun upJson(jsonObject: JSONObject): R

    fun upJson(jsonArray: JSONArray): R

    fun upBytes(bs: ByteArray): R

    fun upBytes(bs: ByteArray, mediaType: MediaType): R

    fun upObject(any: Any): R

    fun addParamsToUrl(isAddParamsToUrl: Boolean): R
}