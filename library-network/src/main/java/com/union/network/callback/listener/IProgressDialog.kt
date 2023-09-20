package com.union.network.callback.listener

import android.app.Dialog

/**
 * 自定义对话框的dialog
 * @Author： VincenT
 * @Time： 2023/8/15 21:59
 */
interface IProgressDialog {

    fun getDialog(): Dialog?

    fun onCancelProgress()
}