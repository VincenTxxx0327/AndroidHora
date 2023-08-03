package com.union.hora.business.data

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import com.union.hora.R
import com.union.hora.adapter.HomeAdapter
import com.union.hora.app.App
import com.union.hora.app.constant.Constant
import com.union.hora.app.ext.setNewOrAddData
import com.union.hora.app.ext.showSnackMsg
import com.union.hora.app.ext.showToast
import com.union.hora.base.BaseMvpListFragment
import com.union.hora.business.data.contract.SearchListContract
import com.union.hora.business.data.presenter.SearchListPresenter
import com.union.hora.business.user.UserActivity
import com.union.hora.event.ColorEvent
import com.union.hora.http.bean.Moment
import com.union.hora.http.bean.MomentResponseBody
import com.union.hora.utils.NetWorkUtil
import kotlinx.android.synthetic.main.fragment_refresh_layout.*
import kotlinx.android.synthetic.main.fragment_search_list.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class SearchListFragment : BaseMvpListFragment<SearchListContract.View, SearchListContract.Presenter>(),
    SearchListContract.View {

    companion object {
        fun getInstance(bundle: Bundle): SearchListFragment {
            val fragment = SearchListFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private var mKey = ""

    /**
     * Adapter
     */
    private val mAdapter: HomeAdapter by lazy {
        HomeAdapter()
    }

    override fun hideLoading() {
        super.hideLoading()
    }

    override fun showErrorMsg(msg: String) {
        super.showErrorMsg(msg)
    }

    override fun initLayoutRes(): Int = R.layout.fragment_search_list
    override fun initToolbar(view: View) {
        TODO("Not yet implemented")
    }

    override fun initData(view: View) {
        TODO("Not yet implemented")
    }

    override fun initListener(view: View) {
        TODO("Not yet implemented")
    }

    override fun createPresenter(): SearchListContract.Presenter = SearchListPresenter()

    override fun enableEventBus(): Boolean = true

    override fun initView(view: View) {
        super.initView(view)

        mKey = arguments?.getString(Constant.SEARCH_KEY, "") ?: ""

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

        floating_action_btn.setOnClickListener {
            scrollToTop()
        }
    }

    override fun lazyLoad() {
        mLayoutStatusView?.showLoading()
        mPresenter?.queryBySearchKey(0, mKey)
    }

    override fun onRefreshList() {
        mPresenter?.queryBySearchKey(0, mKey)
    }

    override fun onLoadMoreList() {
        mPresenter?.queryBySearchKey(pageNum, mKey)
    }

    override fun showArticles(articles: MomentResponseBody) {
        mAdapter.setNewOrAddData(pageNum == 0, articles.datas)
        if (mAdapter.data.isEmpty()) {
            mLayoutStatusView?.showEmpty()
        } else {
            mLayoutStatusView?.showContent()
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshColor(event: ColorEvent) {
        if (event.isRefresh) {
            floating_action_btn.backgroundTintList = ColorStateList.valueOf(event.color)
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