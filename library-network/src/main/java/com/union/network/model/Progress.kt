package com.union.network.model

import android.os.SystemClock
import java.io.Serializable

/**
 * 进度计量转换
 * @Author： VincenT
 * @Time： 2023/8/18 14:17
 */
class Progress : Serializable {
    var filePath: String? = null                                //保存文件地址
    var fileName: String? = null                                //保存的文件名
    var fraction = 0F                                           //下载的进度，0-1 = 0f
    var totalSize = -1L                                         //总字节长度, byte
    var currentSize = 0L                                        //本次下载的大小, byte

    @Transient
    var speed = 0L                                              //网速，byte/s

    @Transient
    private var tempSize = 0L                                   //每一小段时间间隔的网络流量

    @Transient
    private var lastRefreshTime: Long = SystemClock.elapsedRealtime()                           //最后一次刷新的时间

    @Transient
    private val speedBuffer: MutableList<Long>                  //网速做平滑的缓存，避免抖动过快

    init {
        speedBuffer = ArrayList()
    }

    /**
     * 平滑网速，避免抖动过大
     */
    private fun bufferSpeed(speed: Long): Long {
        speedBuffer.add(speed)
        if (speedBuffer.size > 10) {
            speedBuffer.removeAt(0)
        }
        var sum: Long = 0
        for (speedTemp in speedBuffer) {
            sum += speedTemp
        }
        return sum / speedBuffer.size
    }

    fun interface CallbackAction {
        fun rspProgress(progress: Progress)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        return super.equals(other)
    }

    override fun toString(): String {
        return "Progress(filePath=$filePath, fileName=$fileName, fraction=$fraction, totalSize=$totalSize, currentSize=$currentSize, speed=$speed)"
    }

    override fun hashCode(): Int {
        return 0
    }

    companion object {

        fun refreshProgress(curProgress: Progress, writeSize: Long, rspProcess: CallbackAction?): Progress {
            return refreshProgress(curProgress, writeSize, curProgress.totalSize, rspProcess)
        }

        private fun refreshProgress(curProgress: Progress, writeSize: Long, totalSize: Long, callbackAction: CallbackAction?): Progress {
            curProgress.totalSize = totalSize
            curProgress.currentSize += writeSize
            curProgress.tempSize += writeSize
            val currentTime = SystemClock.elapsedRealtime()
            val isNotify = currentTime - curProgress.lastRefreshTime >= 100
            if (isNotify || curProgress.currentSize == totalSize) {
                var diffTime = currentTime - curProgress.lastRefreshTime
                if (diffTime == 0L) diffTime = 1
                curProgress.fraction = curProgress.currentSize * 1.0f / totalSize
                curProgress.speed = curProgress.bufferSpeed(curProgress.tempSize * 1000 / diffTime)
                curProgress.lastRefreshTime = currentTime
                curProgress.tempSize = 0
                callbackAction?.rspProgress(curProgress)
            }
            return curProgress
        }
    }
}