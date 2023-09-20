package com.union.common.integration

import android.content.Context
import com.union.common.base.IFlyAppLifecycles
import com.union.common.di.module.GlobalConfigModule

/**
 * [ConfigModule] 可以给框架配置一些参数,需要实现 [ConfigModule] 后,在 AndroidManifest 中声明该实现类
 * @Author： VincenT
 * @Time： 2023/8/24 17:47
 */
interface ConfigModule {

    /**
     * 使用 [GlobalConfigModule.Builder] 给框架配置一些配置参数
     *
     * @param context [Context]
     * @param builder [GlobalConfigModule.Builder]
     */
    fun applyOptions(context: Context, builder: GlobalConfigModule.Builder)

    /**
     * 使用 [IFlyAppLifecycles] 在 [Application] 的生命周期中注入一些操作
     *
     * @param context    [Context]
     * @param lifecycles [Application] 的生命周期容器, 可向框架中添加多个 [Application] 的生命周期类
     */
    fun injectAppLifecycle(context: Context, lifecycles: MutableList<IFlyAppLifecycles>)
}