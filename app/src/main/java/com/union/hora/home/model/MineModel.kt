package com.union.hora.home.model

import com.union.hora.base.BaseModel
import com.union.hora.home.contract.MineContract
import com.union.hora.http.CommonRetrofit
import com.union.hora.http.bean.CommonResponse
import com.union.hora.http.bean.ProjectTreeBean
import io.reactivex.Observable

class MineModel : BaseModel(), MineContract.Model {

    override fun requestProjectTree(): Observable<CommonResponse<List<ProjectTreeBean>>> {
        return CommonRetrofit.service.getProjectTree()
    }

}