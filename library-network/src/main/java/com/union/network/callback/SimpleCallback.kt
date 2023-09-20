package com.union.network.callback

/**
 * 简单的回调,默认可以使用该回调，不用关注其他回调方法
 * 使用该回调默认只需要处理onError，onSuccess两个方法既成功失败
 * @Author： VincenT
 * @Time： 2023/8/15 19:26
 */
@Deprecated("")
abstract class SimpleCallback<T> : AbsCallback<T>() {
    override fun onStart() {}
    override fun onComplete() {}
}