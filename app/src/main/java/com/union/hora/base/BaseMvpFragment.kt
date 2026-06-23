package com.union.hora.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.union.hora.app.ext.showToast


@Suppress("UNCHECKED_CAST")
abstract class BaseMvpFragment<VB : ViewBinding, in V : IView, P : IPresenter<V>> : BaseFragment(),
    IView {

    /**
     * Presenter
     */
    protected var mPresenter: P? = null

    protected abstract fun createPresenter(): P


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mPresenter = createPresenter()
        mPresenter?.attachView(this as V)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
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