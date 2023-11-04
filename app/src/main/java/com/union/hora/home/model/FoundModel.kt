package com.union.hora.home.model

import com.union.hora.base.CommonModel
import com.union.hora.home.contract.FoundContract
import com.union.hora.http.CommonRetrofit
import com.union.hora.http.bean.MomentResponseBody
import com.union.hora.http.bean.CommonResponse
import io.reactivex.rxjava3.core.Observable

class FoundModel : CommonModel(), FoundContract.Model {
    override fun getSquareList(page: Int): Observable<CommonResponse<MomentResponseBody>> {
        return CommonRetrofit.service.getSquareList(page)
    }
}