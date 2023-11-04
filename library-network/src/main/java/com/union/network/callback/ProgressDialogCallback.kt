package com.union.network.callback

import android.app.Dialog
import com.union.network.callback.listener.IProgressDialog
import com.union.network.exception.ApiException
import io.reactivex.rxjava3.disposables.Disposable

/**
 * 可以自定义带有加载进度框的回调
 * 1.可以自定义带有加载进度框的回调,是否需要显示，是否可以取消
 * 2.取消对话框会自动取消掉网络请求
 * @Author： VincenT
 * @Time： 2023/8/15 19:25
 */
abstract class ProgressDialogCallback<T>(progressDialog: IProgressDialog? = null, private val isShowProgress: Boolean = true, private val isCancel: Boolean = true) : AbsCallback<T>(),
    IProgressDialog {
    private var mDialog: Dialog? = null
    private var disposed: Disposable? = null

    init {
        progressDialog?.let {
            mDialog = it.getDialog()
            mDialog?.let { dialog ->
                dialog.setCancelable(isCancel)
                if (isCancel) {
                    dialog.setOnCancelListener {
                        progressDialog.onCancelProgress()
                    }
                }
            }
        }
    }

    override fun onStart() {
        showProgress()
    }

    override fun onError(e: ApiException?) {
        onComplete()
    }

    override fun onComplete() {
        dismissProgress()
    }

    override fun onCancelProgress() {
        disposed?.let {
            if (!it.isDisposed) {
                it.dispose()
            }
        }
    }

    internal fun subscription(disposed: Disposable?) {
        this.disposed = disposed
    }

    private fun showProgress() {
        isShowProgress.let { ifShow ->
            if (ifShow) {
                mDialog?.let { dialog ->
                    if (!dialog.isShowing) {
                        dialog.show()
                    }
                }
            }
        }
    }

    private fun dismissProgress() {
        isShowProgress.let { ifShow ->
            if (ifShow) {
                mDialog?.let { dialog ->
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
            }
        }
    }
}