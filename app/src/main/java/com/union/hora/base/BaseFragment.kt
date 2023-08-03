package com.union.hora.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.union.hora.app.constant.Constant
import com.union.hora.utils.CommonUtil
import com.union.hora.utils.Preference
import com.union.hora.widget.MultipleStatusView
import leakcanary.AppWatcher
import org.greenrobot.eventbus.EventBus

abstract class BaseFragment : Fragment(), FragmentAction {

    protected var hasLogin: Boolean by Preference(Constant.LOGIN_KEY, false)
    protected var hasNetwork: Boolean by Preference(Constant.HAS_NETWORK_KEY, true)

    private var isViewPrepare = false
    private var hasLoadData = false
    protected var mLayoutStatusView: MultipleStatusView? = null

    open fun enableEventBus(): Boolean = true

    protected abstract fun initLayoutRes(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(initLayoutRes(), null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (enableEventBus()) {
            EventBus.getDefault().register(this)
        }
        isViewPrepare = true
        initToolbar(view)
        initView(view)
        initData(view)
        initListener(view)
        lazyLoadDataIfPrepared()
    }

    private fun lazyLoadDataIfPrepared() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isViewPrepare && !hasLoadData) {
            lazyLoad()
            hasLoadData = true
        }
    }

    override fun onResume() {
        super.onResume()
        lazyLoadDataIfPrepared()
    }
    override fun onDestroy() {
        if (enableEventBus()) {
            EventBus.getDefault().unregister(this)
        }
//        CommonUtil.fixInputMethodManagerLeak(requireContext())
        activity?.let { AppWatcher.objectWatcher.expectWeaklyReachable(this, "baseFragment") }
        super.onDestroy()
    }

}