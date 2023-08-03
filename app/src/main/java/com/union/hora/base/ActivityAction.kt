package com.union.hora.base

interface ActivityAction {

    fun initToolbar()
    fun initView()
    fun initData()
    fun initListener()
    fun initColor()
    fun initTipView()
    fun initNetwork(isConnected: Boolean = true)

}