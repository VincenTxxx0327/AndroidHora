package com.union.hora.business.user.model

import com.union.hora.base.BaseModel
import com.union.hora.business.user.contract.UserContract
import com.union.hora.business.user.repossitory.UserRepository
import com.union.hora.http.CommonRetrofit
import com.union.hora.http.bean.CommonResponse
import com.union.hora.http.bean.LoginData
import com.union.hora.http.bean.UserIconBean
import io.reactivex.Observable

class UserModel : BaseModel(), UserContract.Model {

    override fun registerMember(username: String, password: String): Observable<CommonResponse<LoginData>> {
        return CommonRetrofit.service.registerMember(username, password, "9527")
    }

    override fun loginMember(username: String, password: String): Observable<CommonResponse<LoginData>> {
        return CommonRetrofit.service.loginMember(username, password)
    }

}