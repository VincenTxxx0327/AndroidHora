package com.union.hora.base

//import android.support.rastermill.FrameSequenceDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.union.hora.app.constant.Constant
import com.union.hora.util.Preference
import org.greenrobot.eventbus.EventBus


abstract class BaseFragment : Fragment(), FragmentAction {

    protected var hasLogin: Boolean by Preference(Constant.KEY_LOGIN_OUT, false)
    protected var loginType: Int by Preference(Constant.KEY_LOGIN_TYPE, -1)
    protected var hasNetwork: Boolean by Preference(Constant.KEY_HAS_NETWORK, true)

    protected var mUseBinding: Boolean = false
    private var isViewPrepare = false
    private var hasLoadData = false

    open fun enableEventBus(): Boolean = true

    protected abstract fun initLayoutRes(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (!mUseBinding) {
            inflater.inflate(initLayoutRes(), null)
        } else {
            null
        }
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
//        activity?.let { AppWatcher.objectWatcher.expectWeaklyReachable(this, "baseFragment") }
        super.onDestroy()
    }

//    fun ImageView.loadWebp(fileName:String) {
//        val drawable = FrameSequenceDrawable(resources.assets.open(fileName))
//        drawable.setLoopCount(-1)
//        drawable.setLoopBehavior(FrameSequenceDrawable.LOOP_FINITE)
//        drawable.setOnFinishedListener { }
//        setImageDrawable(drawable)
//    }
}