package com.union.hora.base

interface CommonContract {

    interface View : IView {

    }

    interface Presenter<in V : View> : IPresenter<V> {

    }

    interface Model : IModel {

    }

}