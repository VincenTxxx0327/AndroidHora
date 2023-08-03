package com.union.hora.home.presenter

import com.union.hora.base.BasePresenter
import com.union.hora.home.contract.MessageContract
import com.union.hora.home.model.MessageModel

class MessagePresenter : BasePresenter<MessageModel, MessageContract.View>(),
    MessageContract.Presenter {

    override fun createModel(): MessageModel? = MessageModel()

}