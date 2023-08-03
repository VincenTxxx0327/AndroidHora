package com.union.hora.app.rx

import com.union.hora.app.App
import com.union.hora.base.IView
import com.union.hora.http.bean.BaseResponse
import com.union.hora.http.exception.ErrorStatus
import com.union.hora.http.exception.ExceptionHandle
import com.union.hora.utils.NetWorkUtil
import io.reactivex.observers.ResourceObserver

abstract class BaseObserver<T : BaseResponse> : ResourceObserver<T> {

    private var mView: IView? = null
    private var mErrorMsg = ""
    private var bShowLoading = true

    constructor(view: IView) {
        this.mView = view
    }

    constructor(view: IView, bShowLoading: Boolean) {
        this.mView = view
        this.bShowLoading = bShowLoading
    }

    /**
     * 成功的回调
     */
    protected abstract fun onSuccess(t: T)

    /**
     * 错误的回调
     */
    protected fun onError(t: T) {}

    override fun onStart() {
        super.onStart()
        if (bShowLoading) mView?.showLoading()
        if (!NetWorkUtil.isNetworkConnected(App.instance)) {
            mView?.showErrorMsg("当前网络不可用，请检查网络设置")
            onComplete()
        }
    }

    override fun onNext(t: T) {
        mView?.hideLoading()
        when {
            t.code == ErrorStatus.SUCCESS -> onSuccess(t)
            t.code == ErrorStatus.TOKEN_INVALID -> {
                // TODO Token 过期，重新登录
            }
            else -> {
                onError(t)
                if (t.message.isNotEmpty())
                    mView?.showErrorMsg(t.message)
            }
        }
    }

    override fun onError(e: Throwable) {
        mView?.hideLoading()
        if (mView == null) {
            throw RuntimeException("mView can not be null")
        }
        if (mErrorMsg.isEmpty()) {
            mErrorMsg = ExceptionHandle.handleException(e)
        }
        mView?.showErrorMsg(mErrorMsg)
    }

    override fun onComplete() {
        mView?.hideLoading()
    }
}

