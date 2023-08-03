package com.union.hora.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.FragmentTransaction
import com.tencent.bugly.beta.Beta
import com.union.hora.R
import com.union.hora.app.ext.showToast
import com.union.hora.base.BaseMvpActivity
import com.union.hora.home.contract.MainContract
import com.union.hora.home.presenter.MainPresenter
import com.union.hora.utils.Preference
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MainActivity : BaseMvpActivity<MainContract.View, MainContract.Presenter>(), MainContract.View, OnClickListener {

    private val BOTTOM_INDEX: String = "currentIndex"
    private val FRAGMENT_MOMENT = 0x01
    private val FRAGMENT_FOUND = 0x02
    private val FRAGMENT_MESSAGE = 0x03
    private val FRAGMENT_MINE = 0x04

    private var mIndex = FRAGMENT_MOMENT
    private var mPreIndex = FRAGMENT_MOMENT
    private var mMomentFragment: MomentFragment? = null
    private var mFoundFragment: FoundFragment? = null
    private var mMessageFragment: MessageFragment? = null
    private var mMineFragment: MineFragment? = null

    private var mExitTime: Long = 0
    private var mClickTime: Long = 0

    override fun initLayoutRes(): Int = R.layout.activity_main

    override fun createPresenter(): MainContract.Presenter = MainPresenter()

    override fun enableEventBus(): Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mIndex = savedInstanceState.getInt(BOTTOM_INDEX)
        }
        super.onCreate(savedInstanceState)
    }

    override fun initToolbar() {

    }

    override fun initView() {
        setCurrentViewIndex(mIndex, true)
    }

    override fun initData() {
//        Beta.checkUpgrade(false, false)
//        mPresenter?.loadUserInfo()
    }

    override fun initListener() {
        cl_moment.setOnClickListener(this)
        cl_found.setOnClickListener(this)
        cl_message.setOnClickListener(this)
        cl_mine.setOnClickListener(this)
    }

    private fun setCurrentViewIndex(index: Int, initData: Boolean) {
        mIndex = index
        if (index != mPreIndex || initData) {
            clearBottomStatus()
            val transaction = supportFragmentManager.beginTransaction()
            hideFragments(transaction)
            mClickTime = 0
            mPreIndex = index
            when (index) {
                FRAGMENT_MOMENT -> {
                    if (mMomentFragment == null) {
                        mMomentFragment = MomentFragment.getInstance()
                        transaction.add(R.id.container, mMomentFragment!!, "moment")
                    } else {
                        transaction.show(mMomentFragment!!)
                    }
                    cl_moment.isSelected = true
                }

                FRAGMENT_FOUND -> {
                    if (mFoundFragment == null) {
                        mFoundFragment = FoundFragment.getInstance()
                        transaction.add(R.id.container, mFoundFragment!!, "found")
                    } else {
                        transaction.show(mFoundFragment!!)
                    }
                    cl_found.isSelected = true
                }

                FRAGMENT_MESSAGE -> {
                    if (mMessageFragment == null) {
                        mMessageFragment = MessageFragment.getInstance()
                        transaction.add(R.id.container, mMessageFragment!!, "message")
                    } else {
                        transaction.show(mMessageFragment!!)
                    }
                    cl_message.isSelected = true
                }

                FRAGMENT_MINE -> {
                    if (mMineFragment == null) {
                        mMineFragment = MineFragment.getInstance()
                        transaction.add(R.id.container, mMineFragment!!, "mine")
                    } else {
                        transaction.show(mMineFragment!!)
                    }
                    cl_mine.isSelected = true
                }
            }
            transaction.commit()
        } else {
            if (System.currentTimeMillis().minus(mClickTime) <= 300) {
                showToast("触发了双击")
            } else {
                mClickTime = System.currentTimeMillis()
                showToast("再按一次为双击")
            }
        }
    }

    private fun clearBottomStatus() {
        cl_moment.isSelected = false
        cl_found.isSelected = false
        cl_message.isSelected = false
        cl_mine.isSelected = false
    }

    private fun hideFragments(transaction: FragmentTransaction) {
        mMomentFragment?.let { transaction.hide(it) }
        mFoundFragment?.let { transaction.hide(it) }
        mMessageFragment?.let { transaction.hide(it) }
        mMineFragment?.let { transaction.hide(it) }
    }

    override fun showLogoutSuccess(success: Boolean) {
        if (success) {
            doAsync {
                Preference.clearPreference()
                uiThread {
                    showToast(resources.getString(R.string.logout_success))
                }
            }
        }
    }

    override fun recreate() {
        try {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            mMomentFragment?.let { fragmentTransaction.remove(it) }
            mFoundFragment?.let { fragmentTransaction.remove(it) }
            mMessageFragment?.let { fragmentTransaction.remove(it) }
            mMineFragment?.let { fragmentTransaction.remove(it) }
            fragmentTransaction.commitAllowingStateLoss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.recreate()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        recreate()
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis().minus(mExitTime) <= 2000) {
                finish()
            } else {
                mExitTime = System.currentTimeMillis()
                showToast(getString(R.string.exit_tip))
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BOTTOM_INDEX, mIndex)
    }

    override fun onDestroy() {
        super.onDestroy()
        mMomentFragment = null
        mFoundFragment = null
        mMessageFragment = null
        mMineFragment = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cl_moment -> setCurrentViewIndex(FRAGMENT_MOMENT, false)
            R.id.cl_found -> setCurrentViewIndex(FRAGMENT_FOUND, false)
            R.id.cl_message -> setCurrentViewIndex(FRAGMENT_MESSAGE, false)
            R.id.cl_mine -> setCurrentViewIndex(FRAGMENT_MINE, false)
        }
    }

}
