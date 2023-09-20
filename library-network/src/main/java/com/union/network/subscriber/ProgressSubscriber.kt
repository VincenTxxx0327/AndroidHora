package com.union.network.subscriber

import android.app.Dialog
import android.content.Context
import com.union.network.callback.listener.IProgressDialog
import com.union.network.exception.ApiException

/**
 * 实现带有进度的订阅
 * 1.支持自定义加载进度框
 * 2.支持对话框取消时可以自动终止本次请求，取消订阅
 * @Author： VincenT
 * @Time： 2023/8/15 22:00
 */
@Deprecated("")
abstract class ProgressSubscriber<T : Any> : BaseSubscriber<T>, IProgressDialog {
    private var progressDialog: IProgressDialog? = null
    private var mDialog: Dialog? = null
    private var isShowProgress = true

    /**
     * 默认不显示弹出框，不可以取消
     *
     * @param context  上下文
     */
    constructor(context: Context?) : super(context) {
        init(false)
    }

    /**
     * 自定义加载进度框
     *
     * @param context 上下文
     * @param progressDialog 自定义对话框
     */
    constructor(context: Context?, progressDialog: IProgressDialog?) : super(context) {
        this.progressDialog = progressDialog
        init(false)
    }

    /**
     * 自定义加载进度框,可以设置是否显示弹出框，是否可以取消
     *
     * @param context 上下文
     * @param progressDialog 对话框
     * @param isShowProgress  是否显示对话框
     * @param isCancel  对话框是否可以取消
     */
    constructor(context: Context?, progressDialog: IProgressDialog?, isShowProgress: Boolean, isCancel: Boolean) : super(context) {
        this.progressDialog = progressDialog
        this.isShowProgress = isShowProgress
        init(isCancel)
    }

    /**
     * 初始化
     *
     * @param isCancel 对话框是否可以取消
     */
    private fun init(isCancel: Boolean) {
        if (progressDialog == null) return
        mDialog = progressDialog!!.getDialog()
        if (mDialog == null) return
        mDialog!!.setCancelable(isCancel)
        if (isCancel) {
            mDialog!!.setOnCancelListener { onCancelProgress() }
        }
    }

    /**
     * 展示进度框
     */
    private fun showProgress() {
        if (!isShowProgress) {
            return
        }
        if (mDialog != null) {
            if (!mDialog!!.isShowing) {
                mDialog!!.show()
            }
        }
    }

    /**
     * 取消进度框
     */
    private fun dismissProgress() {
        if (!isShowProgress) {
            return
        }
        if (mDialog != null) {
            if (mDialog!!.isShowing) {
                mDialog!!.dismiss()
            }
        }
    }

    override fun onStart() {
        showProgress()
    }

    override fun onComplete() {
        dismissProgress()
    }

    override fun onError(e: ApiException) {
        dismissProgress()
        //int errCode = e.getCode();
        /*if (errCode == ApiException.ERROR.TIMEOUT_ERROR) {
            ToastUtil.showToast(contextWeakReference.get(), "网络中断，请检查您的网络状态");
        } else if (errCode == ApiException.ERROR.NETWORD_ERROR) {
            ToastUtil.showToast(contextWeakReference.get(), "请检查您的网络状态");
        } else {
            ToastUtil.showToast(contextWeakReference.get(), "error:" + e.getMessage());
        }*/
    }

    override fun onCancelProgress() {
        if (!isDisposed) {
            dispose()
        }
    }
}