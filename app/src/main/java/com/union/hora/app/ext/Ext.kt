package com.union.hora.app.ext

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.WebView
import android.widget.Checkable
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commitNow
import com.dylanc.longan.navigationBarHeight
import com.dylanc.longan.packageName
import com.dylanc.longan.statusBarHeight
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.just.agentweb.AgentWeb
import com.just.agentweb.DefaultWebClient
import com.just.agentweb.WebChromeClient
import com.just.agentweb.WebViewClient
import com.union.hora.BuildConfig
import com.union.hora.HoraApp
import com.union.hora.R
import com.union.hora.app.widget.CustomToast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Any.loge(content: String?) {
    loge(this.javaClass.simpleName ?: HoraApp.TAG, content ?: "")
}

fun loge(tag: String, content: String?) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, content ?: "")
    }
}

fun showToast(content: String) {
    CustomToast(HoraApp.context, content).show()
}

fun Fragment.showDToast(content: String, withImg: Boolean = false) {
    if (BuildConfig.DEBUG) {
        if (withImg) {
            CustomToast(this.requireContext(), content).show()
        } else {
            Toast.makeText(this.requireContext(), content, Toast.LENGTH_SHORT).show()
        }
    }
}

fun Fragment.showToast(content: String, withImg: Boolean = false) {
    if (withImg) {
        CustomToast(this.requireContext(), content).show()
    } else {
        Toast.makeText(this.requireContext(), content, Toast.LENGTH_SHORT).show()
    }
}

fun Context.showToast(content: String, withImg: Boolean = false) {
    if (withImg) {
        CustomToast(this, content).show()
    } else {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
    }
}

fun Context.showDToast(content: String, withImg: Boolean = false) {
    if (BuildConfig.DEBUG) {
        if (withImg) {
            CustomToast(this, content).show()
        } else {
            Toast.makeText(this, content, Toast.LENGTH_SHORT).show()
        }
    }
}

fun AppCompatActivity.showSnackMsg(msg: String) {
    val snackbar = Snackbar.make(this.window.decorView, msg, Snackbar.LENGTH_SHORT)
    val view = snackbar.view
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        .setTextColor(ContextCompat.getColor(this, R.color.theme_white))
    snackbar.show()
}

fun Fragment.showSnackMsg(msg: String) {
    this.activity ?: return
    val snackbar =
        Snackbar.make(this.requireActivity().window.decorView, msg, Snackbar.LENGTH_SHORT)
    val view = snackbar.view
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        .setTextColor(ContextCompat.getColor(this.requireActivity(), R.color.theme_white))
    snackbar.show()
}

// 扩展点击事件属性(重复点击时长)
var <T : View> T.lastClickTime: Long
    set(value) = setTag(1766613352, value)
    get() = getTag(1766613352) as? Long ?: 0

// 重复点击事件绑定
inline fun <T : View> T.setSingleClickListener(time: Long = 1000, crossinline block: (T) -> Unit) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            block(this)
        }
    }
}

fun textChangeViews(vararg views: View, afterTextChanged: (view: View) -> Unit) {
    for (view in views) {
        if (view is EditText) {
            view.addTextChangedListener(onTextChanged = { char, _, _, _ ->

            }, afterTextChanged = {
                afterTextChanged.invoke(view)
            })
        }
    }
}

fun clearViewsFocus(vararg views: View) {
    for (view in views) {
        if (view is EditText) {
            view.clearFocus()
        }
    }
}

/**
 * fromJson2List
 */
inline fun <reified T> fromJson2List(json: String) = fromJson<List<T>>(json)

/**
 * fromJson
 */
inline fun <reified T> fromJson(json: String): T? {
    return try {
        val type = object : TypeToken<T>() {}.type
        return Gson().fromJson(json, type)
    } catch (e: Exception) {
        println("try exception,${e.message}")
        null
    }
}

/**
 * getAgentWeb
 */
fun String.getAgentWeb(
    activity: AppCompatActivity,
    webContent: ViewGroup,
    layoutParams: ViewGroup.LayoutParams,
    webView: WebView,
    webViewClient: WebViewClient?,
    webChromeClient: WebChromeClient?,
    indicatorColor: Int
): AgentWeb = AgentWeb.with(activity)//传入Activity or Fragment
    .setAgentWebParent(webContent, 1, layoutParams)//传入AgentWeb 的父控件
    .useDefaultIndicator(indicatorColor, 2)// 使用默认进度条
    .setWebView(webView)
    .setWebViewClient(webViewClient)
    .setWebChromeClient(webChromeClient)
    .setMainFrameErrorView(com.just.agentweb.R.layout.agentweb_error_page, -1)
    .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
    .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
    .interceptUnkownUrl()
    .createAgentWeb()//
    .ready()
    .go(this)

