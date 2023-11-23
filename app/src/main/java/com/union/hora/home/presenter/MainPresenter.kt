package com.union.hora.home.presenter

import com.union.hora.app.ext.withBoth
import com.union.hora.app.ext.withSuccessOnly
import com.union.hora.base.BasePresenter
import com.union.hora.home.contract.MainContract
import com.union.hora.home.model.MainModel

class MainPresenter : BasePresenter<MainContract.Model, MainContract.View>(), MainContract.Presenter {

    override fun createModel(): MainContract.Model = MainModel()

    override fun logout() {
        mModel?.logout()?.withSuccessOnly(mModel, mView) {
            mView?.showLogoutSuccess(success = true)
        }
    }

    override fun loadUserInfo() {
        mModel?.getUserInfo()?.withBoth(mView, false, {

        }, {})
    }

    override fun loadFloatingView() {
        mView?.showFloatingView()
    }

}