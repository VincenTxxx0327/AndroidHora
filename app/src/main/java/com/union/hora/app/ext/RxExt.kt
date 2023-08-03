package com.union.hora.app.ext

import com.union.hora.R
import com.union.hora.app.App
import com.union.hora.app.rx.SchedulerUtils
import com.union.hora.base.IModel
import com.union.hora.base.IView
import com.union.hora.http.bean.BaseResponse
import com.union.hora.http.exception.ErrorStatus
import com.union.hora.http.exception.ExceptionHandle
import com.union.hora.http.function.RetryWithDelay
import com.union.hora.utils.NetWorkUtil
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

fun <T : BaseResponse> Observable<T>.withSuccessOnly(
        model: IModel?,
        view: IView?,
        isShowLoading: Boolean = true,
        onSuccess: (T) -> Unit
) {
    this.compose(SchedulerUtils.ioToMain())
            .retryWhen(RetryWithDelay())
            .subscribe(object : Observer<T> {
                override fun onComplete() {
                    view?.hideLoading()
                }

                override fun onSubscribe(d: Disposable) {
                    if (isShowLoading) view?.showLoading()
                    model?.addDisposable(d)
                    if (!NetWorkUtil.isNetworkConnected(App.instance)) {
                        view?.showErrorMsg(App.instance.resources.getString(R.string.network_unavailable_tip))
                        onComplete()
                    }
                }

                override fun onNext(t: T) {
                    when {
                        t.code == ErrorStatus.SUCCESS -> onSuccess.invoke(t)
                        t.code == ErrorStatus.TOKEN_INVALID -> {
                            // Token 过期，重新登录
                        }
                        else -> view?.showToastMsg(t.message)
                    }
                }

                override fun onError(t: Throwable) {
                    view?.hideLoading()
                    view?.showErrorMsg(ExceptionHandle.handleException(t))
                }
            })
}

fun <T : BaseResponse> Observable<T>.withBoth(
        view: IView?,
        isShowLoading: Boolean = true,
        onSuccess: (T) -> Unit,
        onError: ((T) -> Unit)? = null
): Disposable {
    if (isShowLoading) view?.showLoading()
    return this.compose(SchedulerUtils.ioToMain())
            .retryWhen(RetryWithDelay())
            .subscribe({
                when {
                    it.code == ErrorStatus.SUCCESS -> onSuccess.invoke(it)
                    it.code == ErrorStatus.TOKEN_INVALID -> {
                        // Token 过期，重新登录
                    }
                    else -> {
                        if (onError != null) {
                            onError.invoke(it)
                        } else {
                            if (it.message.isNotEmpty())
                                view?.showErrorMsg(it.message)
                        }
                    }
                }
                view?.hideLoading()
            }, {
                view?.hideLoading()
                view?.showErrorMsg(ExceptionHandle.handleException(it))
            })
}

