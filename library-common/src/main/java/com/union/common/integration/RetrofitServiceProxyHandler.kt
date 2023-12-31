package com.union.common.integration

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 *
 * @Author： VincenT
 * @Time： 2023/9/19 20:06
 */
class RetrofitServiceProxyHandler(
    private val retrofit: Retrofit?,
    private val serviceClass: Class<*>
) : InvocationHandler {

    private val retrofitService by lazy { retrofit?.create(serviceClass) }

    @Throws(Throwable::class)
    override fun invoke(proxy: Any?, method: Method?, vararg args: Any?): Any? {
        // 根据 https://zhuanlan.zhihu.com/p/40097338 对 Retrofit 进行的优化
        if (method?.returnType == Observable::class.java) {
            // 如果方法返回值是 Observable 的话，则包一层再返回，
            // 只包一层 defer 由外部去控制耗时方法以及网络请求所处线程，
            // 如此对原项目的影响为 0，且更可控。
            return Observable.defer {
                // 执行真正的 Retrofit 动态代理的方法
                method.invoke(retrofitService, *args) as Observable<*>
            }
        }

        // 返回值不是 Observable 或 Single 的话不处理。
        else if (method?.returnType == Single::class.java) {
            // 如果方法返回值是 Single 的话，则包一层再返回。
            return Single.defer {
                // 执行真正的 Retrofit 动态代理的方法
                method.invoke(retrofitService, *args) as Single<*>
            }
        }

        return method?.invoke(retrofitService, *args)
    }
}