package com.union.hora.business.user.contract

import com.union.hora.base.IModel
import com.union.hora.base.IPresenter
import com.union.hora.base.IView
import com.union.hora.http.bean.CommonResponse
import com.union.hora.http.bean.LoginData
import com.union.hora.http.bean.UserIconBean
import io.reactivex.rxjava3.core.Observable

interface UserContract {

    interface View : IView {

        fun registerSuccess(data: LoginData)

        fun loginSuccess(data: LoginData)

    }

    interface Presenter : IPresenter<View> {

        fun register(username: String, password: String)

        fun login(username: String, password: String)

    }

    interface Model : IModel {

        fun registerMember(username: String, password: String): Observable<CommonResponse<LoginData>>

        fun loginMember(username: String, password: String): Observable<CommonResponse<LoginData>>

    }

}