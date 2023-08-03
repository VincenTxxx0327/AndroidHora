package com.union.hora.home.presenter.repository

import com.union.hora.app.ext.withSuccessOnly
import com.union.hora.home.contract.MomentContract
import com.union.hora.home.model.MomentModel
import com.union.hora.http.bean.Banner
import com.union.hora.http.bean.CommonResponse
import com.union.hora.http.bean.Moment
import com.union.hora.http.bean.MomentResponseBody
import com.union.hora.http.bean.Page


class MomentRepository {
    companion object {
        private const val TAG = "MomentRepository"
        var instance: MomentRepository? = null
            get() {
                if (field == null) {
                    field = MomentRepository()
                }
                return field
            }
            private set
    }

    fun loadAdvertData(model: MomentModel, view: MomentContract.View?, onSuccess: (CommonResponse<Page<Banner>>) -> Unit) {
        model.loadAdvertData().withSuccessOnly(model, view, false) {
            onSuccess.invoke(it)
        }
    }

    fun loadMomentData(model: MomentModel, view: MomentContract.View?, firstPage: Boolean, onSuccess: (CommonResponse<Page<Moment>>) -> Unit) {
        var pageNum = if (firstPage) 0 else model.momentPageNum?.value ?: 0
        pageNum++
        model.loadMomentData(pageNum).withSuccessOnly(model, view, firstPage) {
            model.momentPageNum?.value = it.data.current
            model.momentPageTotal?.value = it.data.total
            onSuccess.invoke(it)
        }
    }

    fun loadHomeData(model: MomentModel, view: MomentContract.View?, onSuccess: (CommonResponse<Page<Moment>>) -> Unit) {
        val pageNum = model.momentPageNum?.value ?: 0
        model.loadMomentData(pageNum).withSuccessOnly(model, view, pageNum == 0) {
            model.momentPageNum?.value = it.data.current
            model.momentPageTotal?.value = it.data.total
            onSuccess.invoke(it)
        }
    }

}