package com.union.hora.business.data.contract

import com.union.hora.base.CommonContract
import com.union.hora.http.bean.MomentResponseBody
import com.union.hora.http.bean.CommonResponse
import io.reactivex.rxjava3.core.Observable

interface SearchListContract {

    interface View : CommonContract.View {

        fun showArticles(articles: MomentResponseBody)

        fun scrollToTop()

    }

    interface Presenter : CommonContract.Presenter<View> {

        fun queryBySearchKey(page: Int, key: String)

    }

    interface Model : CommonContract.Model {

        fun queryBySearchKey(page: Int, key: String): Observable<CommonResponse<MomentResponseBody>>

    }

}