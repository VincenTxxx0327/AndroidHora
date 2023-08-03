package com.union.hora.home

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.union.hora.R
import com.union.hora.base.BaseMvpFragment
import com.union.hora.event.ColorEvent
import com.union.hora.home.contract.MessageContract
import com.union.hora.home.presenter.MessagePresenter
import com.union.hora.utils.SettingUtil
import kotlinx.android.synthetic.main.fragment_system.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MessageFragment : BaseMvpFragment<MessageContract.View, MessagePresenter>(), MessageContract.View {

    companion object {
        fun getInstance(): MessageFragment = MessageFragment()
    }

    private val titleList = mutableListOf<String>()
    private val fragmentList = mutableListOf<Fragment>()
    private val systemPagerAdapter: SystemPagerAdapter by lazy {
        SystemPagerAdapter(childFragmentManager, titleList, fragmentList)
    }

    override fun createPresenter(): MessagePresenter = MessagePresenter()
    override fun enableEventBus(): Boolean = false
    override fun initLayoutRes(): Int = R.layout.fragment_system
    override fun initToolbar(view: View) {
    }

    override fun initData(view: View) {
    }

    override fun initListener(view: View) {
    }

    override fun initView(view: View) {
        titleList.add(getString(R.string.knowledge_system))
        titleList.add(getString(R.string.navigation))
//        fragmentList.add(KnowledgeTreeFragment.getInstance())
//        fragmentList.add(NavigationFragment.getInstance())

        viewPager.run {
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
            adapter = systemPagerAdapter
        }

        tabLayout.run {
            setupWithViewPager(viewPager)
            addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
            addOnTabSelectedListener(onTabSelectedListener)
        }

        refreshColor(ColorEvent(true))

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshColor(event: ColorEvent) {
        if (event.isRefresh) {
            if (!SettingUtil.getIsNightMode()) {
                tabLayout.setBackgroundColor(SettingUtil.getColor())
            }
        }
    }

    override fun lazyLoad() {
    }

    override fun scrollToTop() {
    }

    /**
     * onTabSelectedListener
     */
    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            // 默认切换的时候，会有一个过渡动画，设为false后，取消动画，直接显示
            tab?.let {
                viewPager.setCurrentItem(it.position, false)
            }
        }
    }

    class SystemPagerAdapter(
        fm: FragmentManager,
        private val titleList: MutableList<String>,
        private val fragmentList: MutableList<Fragment>
    ) : FragmentPagerAdapter(fm) {

        override fun getItem(i: Int): Fragment = fragmentList[i]

        override fun getCount(): Int = fragmentList.size

        override fun getPageTitle(position: Int): CharSequence? = titleList[position]

    }

}