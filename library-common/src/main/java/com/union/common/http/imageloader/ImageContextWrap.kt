package com.union.common.http.imageloader

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 *
 * @Author： VincenT
 * @Time： 2023/9/19 16:48
 */
val View.imgCtxWrap: ImageContextWrap
    get() = ImageContextWrap(this)

val Context.imgCtxWrap: ImageContextWrap
    get() = ImageContextWrap(this)

val Fragment.imgCtxWrap: ImageContextWrap
    get() = ImageContextWrap(this)

val AppCompatActivity.imgCtxWrap: ImageContextWrap
    get() = ImageContextWrap(this)

val FragmentActivity.imgCtxWrap: ImageContextWrap
    get() = ImageContextWrap(this)

class ImageContextWrap {
    var view: View? = null
        private set
    var context: Context? = null
        private set
    var fragment: Fragment? = null
        private set
    var activity: AppCompatActivity? = null
        private set
    var fragmentActivity: FragmentActivity? = null
        private set

    constructor(view: View) {
        this.view = view
    }

    constructor(context: Context) {
        this.context = context
    }

    constructor(fragment: Fragment) {
        this.fragment = fragment
    }

    constructor(activity: AppCompatActivity) {
        this.activity = activity
    }

    constructor(fragmentActivity: FragmentActivity) {
        this.fragmentActivity = fragmentActivity
    }
}