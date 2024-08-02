package com.union.hora.home

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import cn.bingoogolapple.bgabanner.BGABanner
import cn.bingoogolapple.bgabanner.transformer.TransitionEffect
import com.union.hora.R
import com.union.hora.adapter.HomeAdapter
import com.union.hora.app.ext.setNewOrAddData
import com.union.hora.app.ext.showToast
import com.union.hora.base.BaseMvpFragment
import com.union.hora.business.user.UserActivity
import com.union.hora.home.contract.MomentContract
import com.union.hora.home.presenter.MomentPresenter
import com.union.hora.home.presenter.repository.MomentRepository
import com.union.hora.http.bean.Banner
import com.union.hora.http.bean.Moment
import com.union.hora.utils.GlideUtil
import com.widget.decoration.GridViewItemDecoration
import com.widget.manager.FullyStaggeredGridLayoutManager
import io.reactivex.rxjava3.core.Observable
import kotlinx.android.synthetic.main.fragment_refresh_layout.*
import kotlinx.android.synthetic.main.item_common_banner.view.*
import kotlinx.android.synthetic.main.toolbar_base.*

class MomentFragment : BaseMvpFragment<MomentContract.View, MomentContract.Presenter>(), MomentContract.View {

    companion object {
        fun getInstance(): MomentFragment = MomentFragment()
    }


    private var bannerList: ArrayList<Banner> = arrayListOf()
    private var bannerView: View? = null

    private val homeAdapter: HomeAdapter by lazy {
        HomeAdapter()
    }
    private val gridLayoutManager: FullyStaggeredGridLayoutManager by lazy {
        FullyStaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
    }

    private val bannerAdapter: BGABanner.Adapter<ImageView, String> by lazy {
        BGABanner.Adapter<ImageView, String> { _, imageView, feedImageUrl, _ ->
            GlideUtil.load(activity, feedImageUrl, imageView)
        }
    }

    override fun initLayoutRes(): Int = R.layout.fragment_refresh_layout
    override fun createPresenter() = MomentPresenter(MomentRepository())
    override fun enableEventBus(): Boolean = false

    override fun initToolbar(view: View) {
        iv_toolbarLeft.visibility = View.INVISIBLE
        tv_toolbarTitle.text = getString(R.string.app_name)
        iv_toolbarRight.visibility = View.VISIBLE
    }

    @SuppressLint("InflateParams")
    override fun initView(view: View) {
        val gridViewItemDecoration = GridViewItemDecoration(2, 30)
        mLayoutStatusView = msv_statusView
        rv_mine_extra.run {
            adapter = homeAdapter
            layoutManager = gridLayoutManager
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(gridViewItemDecoration)
        }
        bannerView = layoutInflater.inflate(R.layout.item_common_banner, null)
        bannerView?.banner?.run {
            setTransitionEffect(TransitionEffect.Depth)
        }
        homeAdapter.run {
            addHeaderView(bannerView!!)
            val start = if (hasHeaderLayout()) 1 else 0
            val end = when {
                (hasFooterLayout() && loadMoreModule.hasLoadMoreView()) -> 2
                (hasFooterLayout() || loadMoreModule.hasLoadMoreView()) -> 1
                else -> 0
            }
            gridViewItemDecoration.setNoShowSpace(start, end)
        }
    }

    override fun initData(view: View) {

    }

    override fun initListener(view: View) {
        swipeRefreshLayout.setOnRefreshListener {
            mPresenter?.requestHomeData()
        }
        mLayoutStatusView?.run {
            setOnClickListener {
                lazyLoad()
            }
        }
        homeAdapter.run {
            setOnItemClickListener { adapter, _, position ->
                val item = adapter.data[position] as Moment
                showToast("点击了列表，${item.content}")
            }
            setOnItemChildClickListener { adapter, view, position ->
                val item = adapter.data[position] as Moment
                when (view.id) {
                    R.id.iv_moment_likeImg -> {
                        if (hasLogin) {
                            showToast("点击了喜欢，${item.hasLike}，${item.likes}")
                        } else {
                            Intent(activity, UserActivity::class.java).run {
                                startActivity(this)
                            }
                            showToast(resources.getString(R.string.login_tint))
                        }
                    }
                }
            }
            loadMoreModule.setOnLoadMoreListener {
                swipeRefreshLayout.isRefreshing = false
                mPresenter?.loadMomentData(false)
            }
        }
        bannerView?.banner?.run {
            setDelegate { _, _, _, position ->
                if (bannerList.size > 0) {
                    val data = bannerList[position]
                    showToast("点击了广告，$data")
                }
            }
        }
    }

    override fun lazyLoad() {
        mLayoutStatusView?.showLoading()
        mPresenter?.requestHomeData()
    }

    override fun scrollToTop() {
        rv_mine_extra.run {
            if (gridLayoutManager.findLastCompletelyVisibleItemPositions(IntArray(20)).lastIndex != -1) {
                scrollToPosition(0)
            } else {
                smoothScrollToPosition(0)
            }
        }
    }

    override fun showLoading() {
    }

    override fun hideLoading() {
        swipeRefreshLayout?.isRefreshing = false
    }

    override fun showErrorMsg(msg: String) {
        mLayoutStatusView?.showError()
    }

    @SuppressLint("CheckResult")
    override fun showAdvertData(banners: MutableList<Banner>) {
        bannerList = banners as ArrayList<Banner>
        val bannerFeedList = ArrayList<String>()
        val bannerTitleList = ArrayList<String>()
        Observable.fromIterable(banners)
            .subscribe { list ->
                bannerFeedList.add(list.coverImage)
                bannerTitleList.add(list.title)
            }
        bannerView?.banner?.run {
            setAutoPlayAble(bannerFeedList.size > 1)
            setData(bannerFeedList, bannerTitleList)
            setAdapter(bannerAdapter)
        }
        mLayoutStatusView?.showContent()
    }

    override fun showMomentData(moments: MutableList<Moment>, firstPage: Boolean, lastPage: Boolean) {
        homeAdapter.setNewOrAddData(firstPage, moments)
        if (homeAdapter.data.isEmpty()) {
            mLayoutStatusView?.showEmpty()
        } else {
            mLayoutStatusView?.showContent()
        }
        homeAdapter.loadMoreModule.isEnableLoadMore = !lastPage
    }

}