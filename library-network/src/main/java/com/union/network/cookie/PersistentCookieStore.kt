package com.union.network.cookie

import android.text.TextUtils
import com.blankj.utilcode.util.Utils
import com.union.network.utils.HttpLog
import okhttp3.Cookie
import okhttp3.HttpUrl
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * cookie存储器
 * @Author： VincenT
 * @Time： 2023/8/15 20:38
 */
class PersistentCookieStore {
    private val cookies by lazy { HashMap<String, ConcurrentHashMap<String, Cookie>>() }
    private val cookiePrefs by lazy { Utils.getApp().getSharedPreferences(COOKIE_PREFS, 0) }

    init {
        val prefsMap = cookiePrefs.all
        for (entry: Map.Entry<String, Any?> in prefsMap) {
            val cookieNames = TextUtils.split(entry.value as? String ?: "", ",")
            for (name in cookieNames) {
                val encodedCookie = cookiePrefs.getString(name, null)
                if (encodedCookie != null) {
                    val decodedCookie = decodeCookie(encodedCookie)
                    if (decodedCookie != null) {
                        if (!cookies.containsKey(entry.key)) {
                            cookies[entry.key] = ConcurrentHashMap<String, Cookie>()
                        }
                        cookies[entry.key]!![name!!] = decodedCookie
                    }
                }
            }
        }
    }

    protected fun getCookieToken(cookie: Cookie): String {
        return cookie.name + "@" + cookie.domain
    }

    fun add(url: HttpUrl, cookie: Cookie) {
        val name = getCookieToken(cookie)
        // 添加 host key. 否则有可能抛空.
        if (!cookies.containsKey(url.host)) {
            cookies[url.host] = ConcurrentHashMap()
        }
        // 删除已经有的.
        if (cookies.containsKey(url.host)) {
            cookies[url.host]!!.remove(name)
        }
        // 添加新的进去
        cookies[url.host]!![name] = cookie
        // 是否保存到 SP 中
        if (cookie.persistent) {
            val prefsWriter = cookiePrefs.edit()
            prefsWriter.putString(url.host, TextUtils.join(",", cookies[url.host]!!.keys))
            prefsWriter.putString(name, encodeCookie(SerializableOkHttpCookie(cookie)))
            prefsWriter.apply()
        } else {
            val prefsWriter = cookiePrefs.edit()
            prefsWriter.remove(url.host)
            prefsWriter.remove(name)
            prefsWriter.apply()
        }
    }

    fun addCookies(cookies: List<Cookie>) {
        for (cookie in cookies) {
            val domain = cookie.domain
            var domainCookies = this.cookies[domain]
            if (domainCookies == null) {
                domainCookies = ConcurrentHashMap()
                this.cookies[domain] = domainCookies
            }
        }
    }

    operator fun get(url: HttpUrl): List<Cookie> {
        val ret = ArrayList<Cookie>()
        if (cookies.containsKey(url.host)) ret.addAll(cookies[url.host]!!.values)
        return ret
    }

    fun removeAll(): Boolean {
        val prefsWriter = cookiePrefs.edit()
        prefsWriter.clear()
        prefsWriter.apply()
        cookies.clear()
        return true
    }

    fun remove(url: HttpUrl, cookie: Cookie): Boolean {
        val name = getCookieToken(cookie)
        return if (cookies.containsKey(url.host) && cookies[url.host]!!.containsKey(name)) {
            cookies[url.host]!!.remove(name)
            val prefsWriter = cookiePrefs.edit()
            if (cookiePrefs.contains(name)) {
                prefsWriter.remove(name)
            }
            prefsWriter.putString(url.host, TextUtils.join(",", cookies[url.host]!!.keys))
            prefsWriter.apply()
            true
        } else {
            false
        }
    }

    fun getCookies(): List<Cookie> {
        val ret = ArrayList<Cookie>()
        for (key in cookies.keys) ret.addAll(cookies[key]!!.values)
        return ret
    }

    /**
     * cookies to string
     */
    protected fun encodeCookie(cookie: SerializableOkHttpCookie?): String? {
        if (cookie == null) return null
        val os = ByteArrayOutputStream()
        try {
            val outputStream = ObjectOutputStream(os)
            outputStream.writeObject(cookie)
        } catch (e: IOException) {
            HttpLog.d("IOException in encodeCookie" + e.message)
            return null
        }
        return byteArrayToHexString(os.toByteArray())
    }

    /**
     * String to cookies
     */
    protected fun decodeCookie(cookieString: String): Cookie? {
        val bytes = hexStringToByteArray(cookieString)
        val byteArrayInputStream = ByteArrayInputStream(bytes)
        var cookie: Cookie? = null
        try {
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            cookie = (objectInputStream.readObject() as SerializableOkHttpCookie).getCookies()
        } catch (e: IOException) {
            HttpLog.d("IOException in decodeCookie" + e.message)
        } catch (e: ClassNotFoundException) {
            HttpLog.d("ClassNotFoundException in decodeCookie" + e.message)
        }
        return cookie
    }

    /**
     * byteArrayToHexString
     */
    protected fun byteArrayToHexString(bytes: ByteArray): String {
        val sb = StringBuilder(bytes.size * 2)
        for (element in bytes) {
            val v = element.toInt() and 0xff
            if (v < 16) {
                sb.append('0')
            }
            sb.append(Integer.toHexString(v))
        }
        return sb.toString().uppercase()
    }

    /**
     * hexStringToByteArray
     */
    protected fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((hexString[i].digitToIntOrNull(16) ?: (-1 shl 4)) + hexString[i + 1].digitToIntOrNull(16)!!).toByte()
            i += 2
        }
        return data
    }

    companion object {
        private const val COOKIE_PREFS = "Cookies_Prefs"
    }
}