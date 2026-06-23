package com.union.hora.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.union.hora.app.constant.Constant
import com.union.hora.app.event.NetworkChangeEvent
import com.union.hora.util.NetWorkUtil
import com.union.hora.util.Preference
import org.greenrobot.eventbus.EventBus

class NetworkChangeReceiver : BroadcastReceiver() {

    /**
     * 缓存上一次的网络状态
     */
    private var hasNetwork: Boolean by Preference(Constant.KEY_HAS_NETWORK, true)

    override fun onReceive(context: Context, intent: Intent) {
        val isConnected = NetWorkUtil.isNetworkConnected(context)
        if (isConnected) {
            if (isConnected != hasNetwork) {
                EventBus.getDefault().post(NetworkChangeEvent(isConnected))
            }
        } else {
            EventBus.getDefault().post(NetworkChangeEvent(isConnected))
        }
    }

}