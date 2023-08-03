package com.union.hora.home.contract

import com.union.hora.base.IModel
import com.union.hora.base.IPresenter
import com.union.hora.base.IView

interface MessageContract {

    interface View : IView {
        fun scrollToTop()
    }

    interface Presenter : IPresenter<View> {

    }

    interface Model : IModel {

    }

}