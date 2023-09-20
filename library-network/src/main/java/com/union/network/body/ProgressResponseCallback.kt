package com.union.network.body

import com.union.network.model.Progress

/**
 * 上传进度回调接口
 * @Author： VincenT
 * @Time： 2023/8/15 1:27
 */
interface ProgressResponseCallback {

    /**
     * 回调进度
     * @param progress  进度值
     */
    fun onResponseProgress(progress: Progress)

    /**
     * 回调进度
     * @param bytesWritten  当前读取响应体字节长度
     * @param contentLength 总长度
     * @param done          是否读取完成
     */
    @Deprecated("加入了Progress文件，弃用此方法")
    fun onResponseProgress(bytesWritten: Long, contentLength: Long, done: Boolean)
}