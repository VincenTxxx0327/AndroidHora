@file:Suppress("unused", "DEPRECATION")

package com.union.network.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.telephony.TelephonyManager
import java.io.IOException
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.SocketException
import java.net.URL

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/15 21:11
 */
@SuppressLint("MissingPermission")
object NetworkUtil {
    var NET_CNNT_BAIDU_OK = 1 // NetworkAvailable
    var NET_CNNT_BAIDU_TIMEOUT = 2 // no NetworkAvailable
    var NET_NOT_PREPARE = 3 // Net no ready
    var NET_ERROR = 4 //net error
    private const val TIMEOUT = 3000 // TIMEOUT
    object DownloadStatus {
        const val STATUS_DEFAULT = 0
        const val STATUS_LOADING = 1
        const val STATUS_COMPLETE = 2
        const val STATUS_FAILED = 3
    }
    /**
     * check NetworkAvailable
     * @param context
     * @return
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val manager = context.applicationContext.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        val info = manager.activeNetworkInfo
        return !(null == info || !info.isAvailable)
    }

    /**
     * getLocalIpAddress
     * @return
     */
    fun getLocalIpAddress(): String {
        var ret = ""
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        ret = inetAddress.hostAddress ?: ""
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return ret
    }

    /**
     * 返回当前网络状态
     *
     * @param context
     * @return
     */
    @Suppress("SENSELESS_COMPARISON")
    fun getNetState(context: Context): Int {
        try {
            val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivity != null) {
                val networkInfo = connectivity.activeNetworkInfo
                if (networkInfo != null) {
                    return if (networkInfo.isAvailable && networkInfo.isConnected) {
                        if (!connectionNetwork()) NET_CNNT_BAIDU_TIMEOUT else NET_CNNT_BAIDU_OK
                    } else {
                        NET_NOT_PREPARE
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return NET_ERROR
    }

    /**
     * ping "http://www.baidu.com"
     * @return
     */
    private fun connectionNetwork(): Boolean {
        var result = false
        var httpUrl: HttpURLConnection? = null
        try {
            httpUrl = URL("http://www.baidu.com")
                .openConnection() as HttpURLConnection
            httpUrl.connectTimeout = TIMEOUT
            httpUrl.connect()
            result = true
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            httpUrl?.disconnect()
        }
        return result
    }

    /**
     * check is3G
     * @param context
     * @return boolean
     */
    fun is3G(context: Context): Boolean {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return (activeNetInfo != null
                && activeNetInfo.type == ConnectivityManager.TYPE_MOBILE)
    }

    /**
     * isWifi
     * @param context
     * @return boolean
     */
    fun isWifi(context: Context): Boolean {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return (activeNetInfo != null
                && activeNetInfo.type == ConnectivityManager.TYPE_WIFI)
    }

    /**
     * is2G
     * @param context
     * @return boolean
     */
    fun is2G(context: Context): Boolean {
        val connectivityManager = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return (activeNetInfo != null
                && (activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_EDGE || activeNetInfo.subtype == TelephonyManager.NETWORK_TYPE_GPRS || activeNetInfo
            .subtype == TelephonyManager.NETWORK_TYPE_CDMA))
    }

    /**
     * is wifi on
     */
    fun isWifiEnabled(context: Context): Boolean {
        val mgrConn = context
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val mgrTel = context
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return mgrConn.activeNetworkInfo != null && mgrConn
            .activeNetworkInfo!!.state == NetworkInfo.State.CONNECTED || mgrTel
            .networkType == TelephonyManager.NETWORK_TYPE_UMTS
    }
}