package com.union.hora.base

import android.content.Context
import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class SimpleActivity<VB : ViewBinding> : BaseActivity(),
    IView {
    private var _binding: VB? = null
    protected val binding: VB
        get() = requireNotNull(_binding) { "The property of binding has been destroyed." }

    override fun onCreate(savedInstanceState: Bundle?) {
        _binding = ViewBindingCreator.createViewBinding(javaClass, layoutInflater)
        setContentView(binding.root)
        mUseBinding = true
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
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

    override fun showToastMsg(msg: String) {

    }

    override fun showErrorMsg(msg: String) {

    }
}