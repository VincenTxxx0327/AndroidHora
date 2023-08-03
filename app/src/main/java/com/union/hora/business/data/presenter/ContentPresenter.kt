package com.union.hora.business.data.presenter

import com.union.hora.business.data.contract.ContentContract
import com.union.hora.business.data.model.ContentModel
import com.union.hora.base.CommonPresenter

class ContentPresenter : CommonPresenter<ContentContract.Model, ContentContract.View>(), ContentContract.Presenter {

    override fun createModel(): ContentContract.Model = ContentModel()

}