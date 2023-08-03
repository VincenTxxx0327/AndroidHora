package com.union.hora.business.user.presenter

import com.union.hora.app.ext.withSuccessOnly
import com.union.hora.base.BasePresenter
import com.union.hora.business.user.contract.UserContract
import com.union.hora.business.user.model.UserModel

class UserPresenter : BasePresenter<UserContract.Model, UserContract.View>(), UserContract.Presenter {

    override fun createModel(): UserContract.Model? = UserModel()

    override fun register(username: String, password: String) {
        mModel?.registerMember(username, password)?.withSuccessOnly(mModel, mView) {
            mView?.registerSuccess(it.data)
        }
    }

    override fun login(username: String, password: String) {
        mModel?.loginMember(username, password)?.withSuccessOnly(mModel, mView) {
            mView?.loginSuccess(it.data)
        }
    }
}