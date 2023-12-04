package com.union.hora.home

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.union.hora.R
import com.union.hora.adapter.MineExtraAdapter
import com.union.hora.adapter.MineUsefulAdapter
import com.union.hora.app.ext.showToast
import com.union.hora.base.BaseMvpFragment
import com.union.hora.business.user.UserActivity
import com.union.hora.business.user.repossitory.UserRepository
import com.union.hora.home.contract.MineContract
import com.union.hora.home.presenter.MinePresenter
import com.union.hora.http.bean.UserIconBean
import com.union.hora.widget.decoration.GridViewItemDecoration
import kotlinx.android.synthetic.main.fragment_home_mine.*
import kotlinx.android.synthetic.main.toolbar_base.*

class MineFragment : BaseMvpFragment<MineContract.View, MineContract.Presenter>(), MineContract.View {

    companion object {
        fun getInstance(): MineFragment = MineFragment()
    }

    private val mineUsefulAdapter: MineUsefulAdapter by lazy {
        MineUsefulAdapter()
    }
    private val mineExtraAdapter: MineExtraAdapter by lazy {
        MineExtraAdapter()
    }

    override fun initLayoutRes(): Int = R.layout.fragment_home_mine
    override fun createPresenter() = MinePresenter(UserRepository())
    override fun enableEventBus(): Boolean = false
    override fun initToolbar(view: View) {
        iv_toolbarLeft.visibility = View.VISIBLE
        tv_toolbarTitle.text = getString(R.string.app_name)
        iv_toolbarRight.visibility = View.VISIBLE
    }

    override fun initView(view: View) {
        mLayoutStatusView = msv_statusView
        rv_mine_useful.run {
            adapter = mineUsefulAdapter
            layoutManager = GridLayoutManager(activity, 4, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(GridViewItemDecoration(4, 30, false))
        }
        rv_mine_extra.run {
            adapter = mineExtraAdapter
            layoutManager = GridLayoutManager(activity, 3, RecyclerView.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(GridViewItemDecoration(3, 30, false))
        }
    }

    override fun initData(view: View) {
    }

    override fun initListener(view: View) {
        btn_mine_login.setOnClickListener {
            startActivity(Intent(activity, UserActivity::class.java))
        }
        mineUsefulAdapter.run {
            setOnItemClickListener { adapter, _, position ->
                val item = adapter.data[position] as UserIconBean
                showToast("点击了列表，${item.title}，${item.desc}")
            }
        }
        mineExtraAdapter.run {
            setOnItemClickListener { adapter, _, position ->
                val item = adapter.data[position] as UserIconBean
                showToast("点击了列表，${item.title}")
            }
        }
    }

    override fun lazyLoad() {
        mPresenter?.loadMinePageData()
    }

    override fun showLoading() {
        mLayoutStatusView?.showLoading()
    }

    override fun showErrorMsg(msg: String) {
        mLayoutStatusView?.showError()
    }

    override fun showUserUsefulList(list: ArrayList<UserIconBean>) {
        swipeRefreshLayout.isEnabled = false
        mineUsefulAdapter.setList(list)
    }

    override fun showUserExtraList(list: ArrayList<UserIconBean>) {
        mineExtraAdapter.setList(list)
        if (mineExtraAdapter.data.isEmpty()) {
            mLayoutStatusView?.showEmpty()
        } else {
            mLayoutStatusView?.showContent()
        }
    }


}