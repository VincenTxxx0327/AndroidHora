package com.union.network.request

import com.union.network.callback.custom.FileCallback
import com.union.network.request.base.BaseRequest
import io.reactivex.rxjava3.core.Observable
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