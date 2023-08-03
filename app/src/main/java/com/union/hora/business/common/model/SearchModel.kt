package com.union.hora.business.common.model

import com.union.hora.base.BaseModel
import com.union.hora.business.common.contract.SearchContract
import com.union.hora.http.CommonRetrofit
import com.union.hora.http.bean.HotSearchBean
import com.union.hora.http.bean.CommonResponse
import io.reactivex.Observable

class SearchModel : BaseModel(), SearchContract.Model {

    override fun getHotSearchData(): Observable<CommonResponse<MutableList<HotSearchBean>>> {
        return CommonRetrofit.service.getHotSearchData()
    }

}