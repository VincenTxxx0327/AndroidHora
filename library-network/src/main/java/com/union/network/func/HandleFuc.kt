package com.union.network.func

import com.union.network.exception.ApiException
import com.union.network.exception.ServerException
import com.union.network.model.ApiResult
import io.reactivex.rxjava3.functions.Function
import java.util.*

/**
 * ApiResult<T>转换T
 * @Author： VincenT
 * @Time： 2023/8/15 20:55
 */
class HandleFuc<T : Any> : Function<ApiResult<T>, T> {
    @Throws(Exception::class)
    override fun apply(tApiResult: ApiResult<T>): T? {
        return if (ApiException.isOk(tApiResult)) {
            Optional.ofNullable(tApiResult.getData()).orElse(null)?: tApiResult.getData()
        } else {
            throw ServerException(tApiResult.getCode(), tApiResult.getMsg())
        }
    }
}