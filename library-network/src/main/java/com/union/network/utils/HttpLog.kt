package com.union.network.utils

import android.text.TextUtils
import android.util.Log

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/15 21:10
 */
object HttpLog {
    var customTagPrefix = "RxEasyHttp_"
    var allowD = true
    var allowE = true
    var allowI = true
    var allowV = true
    var allowW = true
    var allowWtf = true
    private fun generateTag(caller: StackTraceElement): String {
        var tag = "%s.%s(L:%d)"
        var callerClazzName = caller.className
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1)
        tag = String.format(tag, callerClazzName, caller.methodName, caller.lineNumber)
        tag = if (TextUtils.isEmpty(customTagPrefix)) tag else "$customTagPrefix:$tag"
        return tag
    }

    var customLogger: CustomLogger? = null
    fun d(content: String?) {
        if (!allowD) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.d(tag, content)
        } else {
            Log.d(tag, content!!)
        }
    }

    fun getCallerStackTraceElement(): StackTraceElement {
        return Thread.currentThread().stackTrace[4]
    }

    fun d(content: String?, tr: Throwable?) {
        if (!allowD) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.d(tag, content, tr)
        } else {
            Log.d(tag, content, tr)
        }
    }

    fun e(content: String?) {
        if (!allowE) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.e(tag, content)
        } else {
            Log.e(tag, content!!)
        }
    }

    fun e(e: Exception) {
        if (!allowE) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.e(tag, e.message, e)
        } else {
            Log.e(tag, e.message, e)
        }
    }

    fun e(content: String?, tr: Throwable?) {
        if (!allowE) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.e(tag, content, tr)
        } else {
            Log.e(tag, content, tr)
        }
    }

    fun i(content: String?) {
        if (!allowI) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.i(tag, content)
        } else {
            Log.i(tag, content!!)
        }
    }

    fun i(content: String?, tr: Throwable?) {
        if (!allowI) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.i(tag, content, tr)
        } else {
            Log.i(tag, content, tr)
        }
    }

    fun v(content: String?) {
        if (!allowV) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.v(tag, content)
        } else {
            Log.v(tag, content!!)
        }
    }

    fun v(content: String?, tr: Throwable?) {
        if (!allowV) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.v(tag, content, tr)
        } else {
            Log.v(tag, content, tr)
        }
    }

    fun w(content: String?) {
        if (!allowW) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.w(tag, content)
        } else {
            Log.w(tag, content!!)
        }
    }

    fun w(content: String?, tr: Throwable?) {
        if (!allowW) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.w(tag, content, tr)
        } else {
            Log.w(tag, content, tr)
        }
    }

    fun w(tr: Throwable?) {
        if (!allowW) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.w(tag, tr)
        } else {
            Log.w(tag, tr)
        }
    }

    fun wtf(content: String?) {
        if (!allowWtf) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.wtf(tag, content)
        } else {
            Log.wtf(tag, content)
        }
    }

    fun wtf(content: String?, tr: Throwable?) {
        if (!allowWtf) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.wtf(tag, content, tr)
        } else {
            Log.wtf(tag, content, tr)
        }
    }

    fun wtf(tr: Throwable?) {
        if (!allowWtf) return
        val caller = getCallerStackTraceElement()
        val tag = generateTag(caller)
        if (customLogger != null) {
            customLogger!!.wtf(tag, tr)
        } else {
            Log.wtf(tag, tr!!)
        }
    }

    interface CustomLogger {
        fun d(tag: String?, content: String?)
        fun d(tag: String?, content: String?, tr: Throwable?)
        fun e(tag: String?, content: String?)
        fun e(tag: String?, content: String?, tr: Throwable?)
        fun i(tag: String?, content: String?)
        fun i(tag: String?, content: String?, tr: Throwable?)
        fun v(tag: String?, content: String?)
        fun v(tag: String?, content: String?, tr: Throwable?)
        fun w(tag: String?, content: String?)
        fun w(tag: String?, content: String?, tr: Throwable?)
        fun w(tag: String?, tr: Throwable?)
        fun wtf(tag: String?, content: String?)
        fun wtf(tag: String?, content: String?, tr: Throwable?)
        fun wtf(tag: String?, tr: Throwable?)
    }
}