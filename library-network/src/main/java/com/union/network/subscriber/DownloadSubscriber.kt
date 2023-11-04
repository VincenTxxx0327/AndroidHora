package com.union.network.subscriber

import android.annotation.SuppressLint
import android.text.TextUtils
import com.blankj.utilcode.util.Utils
import com.union.network.callback.AbsCallback
import com.union.network.callback.ProgressCallback
import com.union.network.exception.ApiException
import com.union.network.utils.HttpLog
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * 定义一个下载的订阅者
 * @Author： VincenT
 * @Time： 2023/8/15 21:58
 */
class DownloadSubscriber<ResponseBody : okhttp3.ResponseBody>(private val path: String, private val name: String, private val absCallBack: AbsCallback<*>) : BaseSubscriber<ResponseBody>() {

    private var lastRefreshUiTime = System.currentTimeMillis()

    override fun onStart() {
        super.onStart()
        absCallBack.onStart()
    }

    override fun onComplete() {
        super.onComplete()
        absCallBack.onComplete()
    }

    override fun onError(e: ApiException) {
        HttpLog.d("DownSubscriber:>>>> onError:" + e.getApiMessage())
        finalOnError(e)
    }

    override fun onNext(t: ResponseBody) {
        HttpLog.d("DownSubscriber:>>>> onNext")
        writeResponseBodyToDisk(path, name, t)
    }

    @SuppressLint("CheckResult")
    private fun writeResponseBodyToDisk(path: String, name: String, body: ResponseBody): Boolean {
        var retPath: String? = path
        var retName = name
        if (!TextUtils.isEmpty(retName)) {
            val type: String
            if (!retName.contains(".")) {
                type = body.contentType().toString()
                fileSuffix = when (type) {
                    APK_CONTENTTYPE -> ".apk"
                    PNG_CONTENTTYPE -> ".png"
                    JPG_CONTENTTYPE -> ".jpg"
                    else -> "." + (body.contentType()?.subtype ?: "")
                }
                retName += fileSuffix
            }
        } else {
            retName = System.currentTimeMillis().toString() + fileSuffix
        }
        if (retPath == null) {
            retPath = Utils.getApp().getExternalFilesDir(null).toString() + File.separator + retName
        } else {
            val file = File(retPath)
            if (!file.exists()) {
                file.mkdirs()
            }
            retPath = retPath + File.separator + retName
            retPath = retPath.replace("//".toRegex(), "/")
        }
        HttpLog.i("path:-->$retPath")
        return try {
            val futureStudioIconFile = File(retPath)
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null
            try {
                //byte[] fileReader = new byte[2048];
                val fileReader = ByteArray(1024 * 128)
                val fileSize: Long = body.contentLength()
                var fileSizeDownloaded: Long = 0
                HttpLog.d("file length: $fileSize")
                inputStream = body.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)
                val absCallBack = absCallBack
                while (true) {
                    val read = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    HttpLog.i("file download: $fileSizeDownloaded of $fileSize")
                    //下载进度
                    val progress = fileSizeDownloaded * 1.0f / fileSize
                    val curTime = System.currentTimeMillis()
                    //每200毫秒刷新一次数据,防止频繁更新进度
                    if (curTime - lastRefreshUiTime >= 200 || progress == 1.0f) {
                        val finalFileSizeDownloaded = fileSizeDownloaded
                        Observable.just(finalFileSizeDownloaded).observeOn(AndroidSchedulers.mainThread()).subscribe({
                            if (absCallBack is ProgressCallback<*>) {
                                (absCallBack as ProgressCallback<*>?)?.update(finalFileSizeDownloaded, fileSize, finalFileSizeDownloaded == fileSize)
                            }
                        }) { }
                        lastRefreshUiTime = System.currentTimeMillis()
                    }
                }
                outputStream.flush()
                HttpLog.i("file downloaded: $fileSizeDownloaded of $fileSize")
                //final String finalName = name;
                val finalPath: String = retPath
                Observable.just(finalPath).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    if (absCallBack is ProgressCallback<*>) {
                        (absCallBack as ProgressCallback<*>?)?.onComplete(finalPath)
                    }
                }) { }
                HttpLog.i("file downloaded: $fileSizeDownloaded of $fileSize")
                HttpLog.i("file downloaded: is sucess")
                true
            } catch (e: IOException) {
                finalOnError(e)
                false
            } finally {
                outputStream?.close()
                inputStream?.close()
            }
        } catch (e: IOException) {
            finalOnError(e)
            false
        }
    }

    @SuppressLint("CheckResult")
    private fun finalOnError(e: Exception) {
        Observable.just(ApiException(e, 100)).observeOn(AndroidSchedulers.mainThread()).subscribe({
            absCallBack.onError(it)
        }) { }
    }

    companion object {
        private const val APK_CONTENTTYPE = "application/vnd.android.package-archive"
        private const val PNG_CONTENTTYPE = "image/png"
        private const val JPG_CONTENTTYPE = "image/jpg"
        private var fileSuffix = ""
    }
}