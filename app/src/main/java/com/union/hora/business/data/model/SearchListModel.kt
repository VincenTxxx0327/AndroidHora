package com.union.hora.business.data.model

import com.union.hora.business.data.contract.SearchListContract
import com.union.hora.base.CommonModel
import com.union.hora.http.CommonRetrofit
import com.union.hora.http.bean.MomentResponseBody
import com.union.hora.http.bean.CommonResponse
import io.reactivex.rxjava3.core.Observable

class SearchListModel : CommonModel(), SearchListContract.Model {

    override fun queryBySearchKey(page: Int, key: String): Observable<CommonResponse<MomentResponseBody>> {
        return CommonRetrofit.service.queryBySearchKey(page, key)
    }

}