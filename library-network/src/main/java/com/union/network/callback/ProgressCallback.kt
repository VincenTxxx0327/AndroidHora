package com.union.network.callback

/**
 * 下载进度回调（主线程，可以直接操作UI）
 * @Author： VincenT
 * @Time： 2023/8/15 19:25
 */
abstract class ProgressCallback<T> : AbsCallback<T>() {

    abstract fun update(bytesRead: Long, contentLength: Long, done: Boolean)
    abstract fun onComplete(path: String?)
}