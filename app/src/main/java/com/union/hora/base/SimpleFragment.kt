package com.union.hora.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

abstract class SimpleFragment<VB : ViewBinding> : BaseFragment(),
    IView {
    private var _binding: VB? = null
    protected val binding: VB
        get() = requireNotNull(_binding) { "The property of binding has been destroyed." }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ViewBindingCreator.createViewBinding(javaClass, layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun initLayoutRes(): Int {
        return -1
    }

    override fun enableEventBus(): Boolean {
        return false
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