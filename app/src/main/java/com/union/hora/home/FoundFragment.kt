package com.union.hora.home

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.union.hora.R
import com.union.hora.adapter.HomeAdapter
import com.union.hora.app.App
import com.union.hora.app.constant.Constant
import com.union.hora.app.ext.setNewOrAddData
import com.union.hora.app.ext.showSnackMsg
import com.union.hora.app.ext.showToast
import com.union.hora.base.BaseMvpListFragment
import com.union.hora.business.data.CommonActivity
import com.union.hora.business.user.UserActivity
import com.union.hora.event.RefreshShareEvent
import com.union.hora.home.contract.FoundContract
import com.union.hora.home.presenter.FoundPresenter
import com.union.hora.http.bean.Moment
import com.union.hora.http.bean.MomentResponseBody
import com.union.hora.utils.NetWorkUtil
import kotlinx.android.synthetic.main.fragment_refresh_layout.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FoundFragment : BaseMvpListFragment<FoundContract.View, FoundPresenter>(), FoundContract.View {

    companion object {
        fun getInstance(): FoundFragment = FoundFragment()
    }

    private val mAdapter: HomeAdapter by lazy {
        HomeAdapter()
    }

    override fun createPresenter(): FoundPresenter = FoundPresenter()

    override fun enableEventBus(): Boolean = false
    override fun initLayoutRes(): Int = R.layout.fragment_home_found

    override fun hideLoading() {
        super.hideLoading()
    }

    override fun showErrorMsg(msg: String) {
        super.showErrorMsg(msg)
    }

    override fun initView(view: View) {
        setHasOptionsMenu(true)
        super.initView(view)

        rv_mine_extra.adapter = mAdapter

        mAdapter.run {
            setOnItemClickListener { adapter, view, position ->
                val item = adapter.data[position] as Moment
                itemClick(item)
            }
            setOnItemChildClickListener { adapter, view, position ->
                val item = adapter.data[position] as Moment
                itemChildClick(item, view, position)
            }
            loadMoreModule.setOnLoadMoreListener(onRequestLoadMoreListener)
        }
    }

    override fun lazyLoad() {
        mLayoutStatusView?.showLoading()
        mPresenter?.getSquareList(0)
    }

    override fun onRefreshList() {
        mPresenter?.getSquareList(0)
    }

    override fun onLoadMoreList() {
        mPresenter?.getSquareList(pageNum)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshShare(event: RefreshShareEvent) {
        if (event.isRefresh) {
            lazyLoad()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_square, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                Intent(activity, CommonActivity::class.java).run {
                    putExtra(Constant.TYPE_KEY, Constant.Type.SHARE_ARTICLE_TYPE_KEY)
                    startActivity(this)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initToolbar(view: View) {
    }

    override fun initData(view: View) {
    }

    override fun initListener(view: View) {
    }

    override fun scrollToTop() {
        rv_mine_extra.run {
            if (linearLayoutManager.findFirstVisibleItemPosition() > 20) {
                scrollToPosition(0)
            } else {
                smoothScrollToPosition(0)
            }
        }
    }

    override fun showSquareList(body: MomentResponseBody) {
        mAdapter.setNewOrAddData(pageNum == 0, body.datas)
        if (mAdapter.data.isEmpty()) {
            mLayoutStatusView?.showEmpty()
        } else {
            mLayoutStatusView?.showContent()
        }
    }

    /**
     * Item Click
     */
    private fun itemClick(item: Moment) {
//        ContentActivity.start(activity, item.id, item.title, item.link)
    }

    /**
     * Item Child Click
     * @param item Article
     * @param view View
     * @param position Int
     */
    private fun itemChildClick(item: Moment, view: View, position: Int) {
        when (view.id) {
            R.id.iv_moment_likeImg -> {
                if (hasLogin) {
                    if (!NetWorkUtil.isNetworkAvailable(App.context)) {
                        showSnackMsg(resources.getString(R.string.no_network))
                        return
                    }
//                    val collect = item.collect
//                    item.collect = !collect
//                    mAdapter.setData(position, item)

                } else {
                    Intent(activity, UserActivity::class.java).run {
                        startActivity(this)
                    }
                    showToast(resources.getString(R.string.login_tint))
                }
            }
        }
    }
}