package com.union.hora.business.data

import android.view.View
import com.union.hora.R
import com.union.hora.base.BaseFragment

class QrCodeFragment : BaseFragment() {

    companion object {
        fun getInstance(): QrCodeFragment = QrCodeFragment()
    }

    override fun initLayoutRes(): Int = R.layout.fragment_qr_code
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