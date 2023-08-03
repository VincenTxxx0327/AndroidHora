package com.union.hora.home.contract

import com.union.hora.base.IModel
import com.union.hora.base.IPresenter
import com.union.hora.base.IView
import com.union.hora.http.bean.CommonResponse
import com.union.hora.http.bean.UserInfoBody
import io.reactivex.Observable

interface MainContract {

    interface View : IView {
        fun showLogoutSuccess(success: Boolean)
    }

    interface Presenter : IPresenter<View> {
        fun logout()
        fun loadUserInfo()
    }

    interface Model : IModel {
        fun logout(): Observable<CommonResponse<Any>>
        fun getUserInfo(): Observable<CommonResponse<UserInfoBody>>
    }

}