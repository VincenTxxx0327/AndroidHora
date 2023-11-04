package com.union.hora.home.model

import androidx.lifecycle.MutableLiveData
import com.union.hora.base.CommonModel
import com.union.hora.home.contract.MomentContract
import com.union.hora.http.CommonRetrofit
import com.union.hora.http.bean.Moment
import com.union.hora.http.bean.Banner
import com.union.hora.http.bean.Page
import com.union.hora.http.bean.CommonResponse
import io.reactivex.rxjava3.core.Observable

class MomentModel : CommonModel(), MomentContract.Model {

    val momentPageNum: MutableLiveData<Int>? by lazy {
        MutableLiveData<Int>()
    }

    val momentPageTotal: MutableLiveData<Int>? by lazy {
        MutableLiveData<Int>()
    }

    override fun loadAdvertData(): Observable<CommonResponse<Page<Banner>>> {
        return CommonRetrofit.service.loadAdvertList()
    }

    override fun requestTopArticles(): Observable<CommonResponse<Page<Moment>>> {
        return CommonRetrofit.service.getTopArticles(1)
    }

    override fun loadMomentData(pageNum: Int): Observable<CommonResponse<Page<Moment>>> {
        val queryMap = mutableMapOf<String, Any>()
        queryMap["memberId"] = "100001410053"
        queryMap["roleIds"] = "1,9527"
        queryMap["page"] = pageNum
        queryMap["size"] = 10
        return CommonRetrofit.service.loadMomentList(queryMap)
    }

}