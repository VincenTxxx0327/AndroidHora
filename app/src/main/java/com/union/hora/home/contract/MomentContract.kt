package com.union.hora.home.contract


import com.union.hora.base.CommonContract
import com.union.hora.http.bean.Moment
import com.union.hora.http.bean.Banner
import com.union.hora.http.bean.Page
import com.union.hora.http.bean.CommonResponse
import io.reactivex.Observable

interface MomentContract {

    interface View : CommonContract.View {

        fun scrollToTop()

        fun showAdvertData(banners: MutableList<Banner>)

        fun showMomentData(moments: MutableList<Moment>, firstPage: Boolean, lastPage: Boolean)

    }

    interface Presenter : CommonContract.Presenter<View> {

        fun loadAdvertData()

        fun requestHomeData()

        fun loadMomentData(firstPage: Boolean)

    }

    interface Model : CommonContract.Model {

        fun loadAdvertData(): Observable<CommonResponse<Page<Banner>>>

        fun requestTopArticles(): Observable<CommonResponse<Page<Moment>>>

        fun loadMomentData(pageNum: Int): Observable<CommonResponse<Page<Moment>>>
    }

}