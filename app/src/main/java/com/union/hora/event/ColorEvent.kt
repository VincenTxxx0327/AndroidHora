package com.union.hora.event

import com.union.hora.utils.SettingUtil

class ColorEvent(var isRefresh: Boolean, var color: Int = SettingUtil.getColor())