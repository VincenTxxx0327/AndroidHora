package com.union.hora.home.contract

import com.union.hora.base.CommonContract
import com.union.hora.http.bean.MomentResponseBody
import com.union.hora.http.bean.CommonResponse
import io.reactivex.rxjava3.core.Observable

interface FoundContract {

    interface View : CommonContract.View {
        fun scrollToTop()
        fun showSquareList(body: MomentResponseBody)
    }

    interface Presenter : CommonContract.Presenter<View> {
        fun getSquareList(page: Int)
    }

    interface Model : CommonContract.Model {
        fun getSquareList(page: Int): Observable<CommonResponse<MomentResponseBody>>
    }

}