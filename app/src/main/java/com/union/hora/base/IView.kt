package com.union.hora.base

import android.content.Context

interface IView {

    fun getContext(): Context?

    fun showLoading()

    fun hideLoading()

    fun showToastMsg(msg: String)

    fun showErrorMsg(msg: String)

}