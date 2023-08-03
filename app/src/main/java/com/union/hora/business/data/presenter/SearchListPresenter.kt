package com.union.hora.business.data.presenter

import com.union.hora.app.ext.withSuccessOnly
import com.union.hora.business.data.contract.SearchListContract
import com.union.hora.business.data.model.SearchListModel
import com.union.hora.base.CommonPresenter

class SearchListPresenter : CommonPresenter<SearchListContract.Model, SearchListContract.View>(), SearchListContract.Presenter {

    override fun createModel(): SearchListContract.Model = SearchListModel()

    override fun queryBySearchKey(page: Int, key: String) {
        mModel?.queryBySearchKey(page, key)?.withSuccessOnly(mModel, mView, page == 0) {
            mView?.showArticles(it.data)
        }
    }

}