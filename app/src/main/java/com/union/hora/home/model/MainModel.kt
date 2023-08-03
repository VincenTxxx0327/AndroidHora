package com.union.hora.home.model

import com.union.hora.base.BaseModel
import com.union.hora.home.contract.MainContract
import com.union.hora.http.CommonRetrofit
import com.union.hora.http.bean.CommonResponse
import com.union.hora.http.bean.UserInfoBody
import io.reactivex.Observable

class MainModel : BaseModel(), MainContract.Model {

    override fun logout(): Observable<CommonResponse<Any>> {
        return CommonRetrofit.service.logout()
    }

    override fun getUserInfo(): Observable<CommonResponse<UserInfoBody>> {
        return CommonRetrofit.service.getUserInfo()
    }

}