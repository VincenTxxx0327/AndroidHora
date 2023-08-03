package com.union.hora.business.data

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import com.union.hora.R
import com.union.hora.base.BaseFragment
import com.union.hora.utils.SettingUtil
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : BaseFragment() {

    companion object {
        fun getInstance(bundle: Bundle): AboutFragment {
            val fragment = AboutFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun initView(view: View) {
        about_content.run {
            text = Html.fromHtml(getString(R.string.about_content))
            movementMethod = LinkMovementMethod.getInstance()
        }

        val versionName = activity?.packageManager?.getPackageInfo(activity?.packageName ?: "", 0)?.versionName
        val versionStr = "${getString(R.string.app_name)} V${versionName}"
        about_version.text = versionStr

        setLogoBg()

    }

    override fun initData(view: View) {
        TODO("Not yet implemented")
    }

    override fun initListener(view: View) {
        TODO("Not yet implemented")
    }

    private fun setLogoBg() {
        val drawable = iv_logo.background as GradientDrawable
        drawable.setColor(SettingUtil.getColor())
        iv_logo.setBackgroundDrawable(drawable)
    }

    override fun initLayoutRes(): Int = R.layout.fragment_about
    override fun initToolbar(view: View) {
        TODO("Not yet implemented")
    }

    override fun lazyLoad() {
    }
}