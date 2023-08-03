package com.union.hora.business.data

import android.os.Bundle
import android.view.View
import com.union.hora.R
import com.union.hora.base.BaseFragment

class SettingFragment : BaseFragment() {

    companion object {
        fun getInstance(bundle: Bundle): SettingFragment {
            val fragment = SettingFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun initLayoutRes(): Int = R.layout.fragment_setting
    override fun initToolbar(view: View) {
        TODO("Not yet implemented")
    }

    override fun initView(view: View) {
    }

    override fun initData(view: View) {
        TODO("Not yet implemented")
    }

    override fun initListener(view: View) {
        TODO("Not yet implemented")
    }

    override fun lazyLoad() {
    }
}