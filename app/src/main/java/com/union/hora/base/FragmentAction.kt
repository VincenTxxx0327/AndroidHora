package com.union.hora.base

import android.view.View

interface FragmentAction {

    fun initToolbar(view: View)
    fun initView(view: View)
    fun initData(view: View)
    fun initListener(view: View)
    fun lazyLoad()
}