package com.union.hora.business.data

import com.union.hora.R
import com.union.hora.app.constant.Constant
import com.union.hora.base.BaseSwipeBackActivity
import com.union.hora.event.ColorEvent
import kotlinx.android.synthetic.main.toolbar.*
import org.greenrobot.eventbus.EventBus

class CommonActivity : BaseSwipeBackActivity() {

    private var mType = ""

    override fun initLayoutRes(): Int = R.layout.activity_common

    override fun initToolbar() {

    }

    override fun initView() {
        val extras = intent.extras ?: return
        mType = extras.getString(Constant.TYPE_KEY, "")
        toolbar.run {
            title = getString(R.string.app_name)
            setSupportActionBar(this)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        val fragment = when (mType) {
            Constant.Type.COLLECT_TYPE_KEY -> {
                toolbar.title = getString(R.string.collect)
                AboutFragment.getInstance(extras)
            }
            Constant.Type.ABOUT_US_TYPE_KEY -> {
                toolbar.title = getString(R.string.about_us)
                AboutFragment.getInstance(extras)
            }
            Constant.Type.SETTING_TYPE_KEY -> {
                toolbar.title = getString(R.string.setting)
                SettingFragment.getInstance(extras)
            }
            Constant.Type.SEARCH_TYPE_KEY -> {
                toolbar.title = extras.getString(Constant.SEARCH_KEY, "")
                SearchListFragment.getInstance(extras)
            }
            Constant.Type.SCAN_QR_CODE_TYPE_KEY -> {
                toolbar.title = getString(R.string.scan_code_download)
                QrCodeFragment.getInstance()
            }
            else -> {
                null
            }
        }
        fragment ?: return
        supportFragmentManager.beginTransaction()
            .replace(R.id.common_frame_layout, fragment, Constant.Type.COLLECT_TYPE_KEY)
            .commit()

    }

    override fun initData() {

    }

    override fun initListener() {
    }

    override fun initColor() {
        super.initColor()
        EventBus.getDefault().post(ColorEvent(true, mThemeColor))
    }

}
