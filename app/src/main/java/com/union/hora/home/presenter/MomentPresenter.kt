package com.union.hora.home.presenter

import com.union.hora.base.CommonPresenter
import com.union.hora.home.contract.MomentContract
import com.union.hora.home.model.MomentModel
import com.union.hora.home.presenter.repository.MomentRepository

class MomentPresenter(private val momentRepo: MomentRepository) : CommonPresenter<MomentContract.Model, MomentContract.View>(), MomentContract.Presenter {

    override fun createModel(): MomentContract.Model = MomentModel()

    override fun loadAdvertData() {
        momentRepo.loadAdvertData(mModel as MomentModel, mView) {
            mView?.showAdvertData(it.data.records)
        }
    }

    override fun loadMomentData(firstPage: Boolean) {
        momentRepo.loadMomentData(mModel as MomentModel, mView, firstPage) {
            mView?.showMomentData(it.data.records, it.data.current == 1, it.data.current == it.data.pages)
        }
    }

    override fun requestHomeData() {
        loadAdvertData()
        loadMomentData(true)
    }

}