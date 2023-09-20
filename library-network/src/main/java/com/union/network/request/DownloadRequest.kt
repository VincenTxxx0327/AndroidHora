package com.union.network.request

import com.union.network.callback.AbsCallback
import com.union.network.callback.custom.FileCallback
import com.union.network.func.RetryExceptionFunc
import com.union.network.request.base.BaseRequest
import com.union.network.subscriber.DownloadSubscriber
import com.union.network.transformer.HandleErrTransformer
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ResponseBody

/**
 * 下载请求
 * @Author： VincenT
 * @Time： 2023/8/15 22:05
 */
class DownloadRequest(url: String) : BaseRequest<DownloadRequest>(url) {
    private var savePath: String? = null
    private var saveName: String? = null

    override fun generateRequest(): Observable<ResponseBody>? {
        var range = 0L
        (callback as? FileCallback)?.apply {
            update(isBreakpointDownload)
            if (isBreakpointDownload) {
                range = downloadFile.length()
            }
        }
        return apiService?.downloadFile("bytes=${range}-", url)
    }

}