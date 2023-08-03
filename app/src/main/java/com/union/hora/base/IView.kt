package com.union.hora.base

interface IView {

    fun showLoading()

    fun hideLoading()

    fun showToastMsg(msg: String)

    fun showErrorMsg(msg: String)

}