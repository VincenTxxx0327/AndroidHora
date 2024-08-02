package com.union.hora.base

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.listener.OnLoadMoreListener
import com.widget.decoration.SpaceItemDecoration
import kotlinx.android.synthetic.main.fragment_refresh_layout.*

abstract class BaseMvpListFragment<in V : IView, P : IPresenter<V>> : BaseMvpFragment<V, P>() {

    /**
     * 每页数据的个数
     */
    protected var pageSize = 20

    /**
     * PageNum
     */
    protected var pageNum = 0

    /**
     * LinearLayoutManager
     */
    protected val linearLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(activity)
    }

    /**
     * RecyclerView Divider
     */
    private val recyclerViewItemDecoration by lazy {
        activity?.let {
            SpaceItemDecoration(it)
        }
    }

    /**
     * RefreshListener
     */
    protected val onRefreshListener = SwipeRefreshLayout.OnRefreshListener {
        pageNum = 0
        onRefreshList()
    }

    /**
     * LoadMoreListener
     */
    protected val onRequestLoadMoreListener = OnLoadMoreListener {
        pageNum++
        swipeRefreshLayout.isRefreshing = false
        onLoadMoreList()
    }

    /**
     * 下拉刷新
     */
    abstract fun onRefreshList()

    /**
     * 上拉加载更多
     */
    abstract fun onLoadMoreList()

    override fun initView(view: View) {
        mLayoutStatusView = msv_statusView
        swipeRefreshLayout.run {
            setOnRefreshListener(onRefreshListener)
        }
        rv_mine_extra.run {
            layoutManager = linearLayoutManager
            itemAnimator = DefaultItemAnimator()
            recyclerViewItemDecoration?.let { addItemDecoration(it) }
        }
    }

    override fun showLoading() {
        // swipeRefreshLayout.isRefreshing = isRefresh
    }

    override fun hideLoading() {
        swipeRefreshLayout?.isRefreshing = false
    }

    override fun showErrorMsg(msg: String) {
        super.showErrorMsg(msg)
        mLayoutStatusView?.showError()
    }
}