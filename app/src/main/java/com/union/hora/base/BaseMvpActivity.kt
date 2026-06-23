package com.union.hora.base

import android.content.Context
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.union.hora.app.ext.showToast


@Suppress("UNCHECKED_CAST")
abstract class BaseMvpActivity<VB : ViewBinding, in V : IView, P : IPresenter<V>> : BaseActivity(),
    IView {

    protected var mPresenter: P? = null

    protected abstract fun createPresenter(): P
    private var _binding: VB? = null
    protected val binding: VB
        get() = requireNotNull(_binding) { "The property of binding has been destroyed." }

    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = ViewBindingCreator.createViewBinding(javaClass, layoutInflater)
        setContentView(binding.root)
        mPresenter = createPresenter()
        mPresenter?.attachView(this as V)
        mUseBinding = true
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.detachView()
        this.mPresenter = null
        _binding = null
    }

    override fun initLayoutRes(): Int {
        return -1
    }

    override fun enableEventBus(): Boolean {
        return false
    }

    override fun getContext(): Context? {
        return this
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