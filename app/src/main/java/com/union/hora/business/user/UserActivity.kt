@file:Suppress("DEPRECATION")

package com.union.hora.business.user

import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.union.hora.R
import com.union.hora.app.constant.Constant
import com.union.hora.app.ext.showToast
import com.union.hora.base.BaseMvpActivity
import com.union.hora.business.user.contract.UserContract
import com.union.hora.business.user.presenter.UserPresenter
import com.union.hora.http.bean.LoginData
import com.union.hora.utils.DialogUtil
import com.union.hora.utils.Preference
import kotlinx.android.synthetic.main.activity_user.*
import kotlinx.android.synthetic.main.toolbar_base.*

class UserActivity : BaseMvpActivity<UserContract.View, UserContract.Presenter>(), UserContract.View, UserRegisterFragment.RegisterCallback, UserLoginFragment.LoginCallback {

    /**
     * local username
     */
    private var user: String by Preference(Constant.USERNAME_KEY, "")

    /**
     * local password
     */
    private var pwd: String by Preference(Constant.PASSWORD_KEY, "")

    /**
     * token
     */
    private var token: String by Preference(Constant.TOKEN_KEY, "")

    private var mAdapter: VPAdapter? = null
    private var mFragments: ArrayList<Fragment> = arrayListOf()

    override fun createPresenter(): UserContract.Presenter = UserPresenter()

    private val mDialog by lazy {
        DialogUtil.getWaitDialog(this, getString(R.string.register_ing))
    }

    override fun showLoading() {
        mDialog.show()
    }

    override fun hideLoading() {
        mDialog.dismiss()
    }

    override fun registerSuccess(data: LoginData) {
        showToast(getString(R.string.register_success))
        hasLogin = true
    }

    override fun loginSuccess(data: LoginData) {
        showToast(getString(R.string.login_success))
        hasLogin = true
    }

    override fun enableEventBus(): Boolean = false

    override fun enableNetworkTip(): Boolean = false

    override fun initLayoutRes(): Int = R.layout.activity_user

    override fun initToolbar() {
        iv_toolbarLeft.isInvisible = true
        iv_toolbarRight.isInvisible = true
        tv_toolbarTitle.text = "注册用户"
    }

    override fun initView() {
        tv_userThird.isInvisible = true
        cl_userThird.isInvisible = true
        mFragments.add(UserLoginFragment(this))
        mFragments.add(UserRegisterFragment(this))
        mAdapter = VPAdapter(supportFragmentManager, mFragments)
    }

    override fun initData() {
        vp_content.adapter = mAdapter
        vp_content.currentItem = 1
    }

    override fun initListener() {
        vp_content.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(position: Int, v: Float, i1: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        tv_userThird.isInvisible = false
                        cl_userThird.isInvisible = false
                        tv_toolbarTitle.text = "用户登录"
                        iv_headerBackground.setImageResource(R.drawable.night)
                    }

                    1 -> {
                        tv_userThird.isInvisible = true
                        cl_userThird.isInvisible = true
                        tv_toolbarTitle.text = "注册用户"
                        iv_headerBackground.setImageResource(R.drawable.day)
                    }
                }
            }

            override fun onPageScrollStateChanged(position: Int) {}
        })
    }

    class VPAdapter(fragmentManager: FragmentManager, private var fragments: ArrayList<Fragment> = arrayListOf()) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int {
            return fragments.size
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }


    }

    override fun onSignInClick() {
        vp_content.currentItem = 0
    }

    override fun onRegisterClick(username: String, password: String) {
        mPresenter?.register(username, password)
    }

    override fun onSignUpClick() {
        vp_content.currentItem = 1
    }

    override fun onLoginClick(username: String, password: String) {
        mPresenter?.login(username, password)
    }

}
