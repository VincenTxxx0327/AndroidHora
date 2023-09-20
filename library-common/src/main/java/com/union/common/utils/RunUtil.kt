package com.union.common.utils

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.DownloadManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.app.SearchManager
import android.app.UiModeManager
import android.app.admin.DevicePolicyManager
import android.app.job.JobScheduler
import android.app.usage.UsageStatsManager
import android.appwidget.AppWidgetManager
import android.content.ClipboardManager
import android.hardware.SensorManager
import android.location.LocationManager
import android.media.AudioManager
import android.media.MediaRouter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.PowerManager
import android.os.Vibrator
import android.os.storage.StorageManager
import android.telecom.TelecomManager
import android.telephony.CarrierConfigManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.Utils

/**
 * 提供各种系统服务
 * @Author： VincenT
 * @Time： 2023/9/20 2:30
 */
inline fun <reified T> getSystemService(): T? =
    ContextCompat.getSystemService(Utils.getApp(), T::class.java)

val windowManager get() = getSystemService<WindowManager>()
val clipboardManager get() = getSystemService<ClipboardManager>()
val layoutInflater get() = getSystemService<LayoutInflater>()
val activityManager get() = getSystemService<ActivityManager>()
val powerManager get() = getSystemService<PowerManager>()
val alarmManager get() = getSystemService<AlarmManager>()
val notificationManager get() = getSystemService<NotificationManager>()
val keyguardManager get() = getSystemService<KeyguardManager>()
val locationManager get() = getSystemService<LocationManager>()
val searchManager get() = getSystemService<SearchManager>()
val storageManager get() = getSystemService<StorageManager>()
val vibrator get() = getSystemService<Vibrator>()
val connectivityManager get() = getSystemService<ConnectivityManager>()
val wifiManager get() = getSystemService<WifiManager>()
val audioManager get() = getSystemService<AudioManager>()
val mediaRouter get() = getSystemService<MediaRouter>()
val telephonyManager get() = getSystemService<TelephonyManager>()
val sensorManager get() = getSystemService<SensorManager>()
val subscriptionManager get() = getSystemService<SubscriptionManager>()
val carrierConfigManager get() = getSystemService<CarrierConfigManager>()
val inputMethodManager get() = getSystemService<InputMethodManager>()
val uiModeManager get() = getSystemService<UiModeManager>()
val downloadManager get() = getSystemService<DownloadManager>()
val batteryManager get() = getSystemService<BatteryManager>()
val jobScheduler get() = getSystemService<JobScheduler>()
val accessibilityManager get() = getSystemService<AccessibilityManager>()
val appWidgetManager get() = getSystemService<AppWidgetManager>()
val telecomManager get() = getSystemService<TelecomManager>()
val devicePolicyManager get() = getSystemService<DevicePolicyManager>()
val usageStatsManager get() = getSystemService<UsageStatsManager>()