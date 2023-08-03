package com.union.hora.business.data.contract

import com.union.hora.base.CommonContract

interface ContentContract {

    interface View : CommonContract.View {

    }

    interface Presenter : CommonContract.Presenter<View> {

    }

    interface Model : CommonContract.Model {

    }

}