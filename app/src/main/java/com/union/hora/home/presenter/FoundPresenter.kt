package com.union.hora.home.presenter

import com.union.hora.app.ext.withSuccessOnly
import com.union.hora.base.CommonPresenter
import com.union.hora.home.contract.FoundContract
import com.union.hora.home.model.FoundModel

class FoundPresenter : CommonPresenter<FoundModel, FoundContract.View>(), FoundContract.Presenter {

    override fun createModel(): FoundModel? = FoundModel()

    override fun getSquareList(page: Int) {
        mModel?.getSquareList(page)?.withSuccessOnly(mModel, mView, page == 0) {
            mView?.showSquareList(it.data)
        }
    }

}