package com.union.common.utils

import com.blankj.utilcode.util.Utils
import com.union.common.base.IFlyApp
import com.union.common.di.component.AppComponent

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/17 22:06
 */

    fun getAppComponent(): AppComponent {
        return checkNotNull((Utils.getApp() as? IFlyApp)?.getAppComponent()) {
            "${Utils.getApp().javaClass.name} must be implements ${IFlyApp::class.java.name}"
        }
    }
