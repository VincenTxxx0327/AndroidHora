package com.union.common.base

import android.app.Application
import android.content.Context
import android.content.res.Configuration

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/24 16:40
 */
interface IFlyAppLifecycles {

    fun attachBaseContext(context: Context)

    fun onCreate(application: Application)

    fun onTerminate(application: Application)

    fun onConfigurationChanged(configuration: Configuration)

    fun onLowMemory()

    fun onTrimMemory(level: Int)
}