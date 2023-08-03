package com.union.hora.base

import android.os.Bundle
import com.union.hora.app.ext.showToast

@Suppress("UNCHECKED_CAST")
abstract class BaseMvpActivity<in V : IView, P : IPresenter<V>> : BaseActivity(), IView {

    protected var mPresenter: P? = null

    protected abstract fun createPresenter(): P

    override fun onCreate(savedInstanceState: Bundle?) {
        mPresenter = createPresenter()
        mPresenter?.attachView(this as V)
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.detachView()
        this.mPresenter = null
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
    }

    override fun showErrorMsg(msg: String) {
        showToast(msg)
    }

    override fun showToastMsg(msg: String) {
        showToast(msg)
    }


}