package com.union.network.callback

import okhttp3.ResponseBody

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/23 18:39
 */
abstract class NoCacheResultCallback<T> : AbsCallback<T>() {

    /**
     * 拿到响应后，将数据转换成需要的格式，子线程中执行，可以是耗时操作
     *
     * @param body 需要转换的对象
     * @return 转换后的结果
     * @throws Exception 转换过程发生的异常
     */
    @Throws(Throwable::class)
    abstract fun convertResponse(body: ResponseBody): T?
}

