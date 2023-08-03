package com.union.hora.business.common.contract

import com.union.hora.base.IModel
import com.union.hora.base.IPresenter
import com.union.hora.base.IView
import com.union.hora.http.bean.HotSearchBean
import com.union.hora.http.bean.CommonResponse
import com.union.hora.http.bean.SearchHistoryBean
import io.reactivex.Observable

interface SearchContract {

    interface View : IView {

        fun showHistoryData(historyBeans: MutableList<SearchHistoryBean>)

        fun showHotSearchData(hotSearchDatas: MutableList<HotSearchBean>)

    }

    interface Presenter : IPresenter<View> {

        fun queryHistory()

        fun saveSearchKey(key: String)

        fun deleteById(id: Long)

        fun clearAllHistory()

        fun getHotSearchData()

    }

    interface Model : IModel {

        fun getHotSearchData(): Observable<CommonResponse<MutableList<HotSearchBean>>>

    }

}