package com.union.network.utils

import com.blankj.utilcode.util.SPUtils

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/24 11:39
 */
object FlyCacheUtils {
    private val SP_UTILS = SPUtils.getInstance("FlyHttp")

    private fun putInt(key: String, value: Int) {
        SP_UTILS.put(key, value)
    }

    private fun getInt(key: String, defaultValue: Int = 0): Int {
        return SP_UTILS.getInt(key, defaultValue)
    }

    fun setDownloadStatus(key: String, downloadStatus: Int) {
        putInt(key, downloadStatus)
    }

    fun getDownloadStatus(key: String): Int {
        return getInt(key)
    }
}