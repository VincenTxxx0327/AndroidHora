package com.union.hora.base

open class CommonPresenter<M : CommonContract.Model, V : CommonContract.View>
    : BasePresenter<M, V>(), CommonContract.Presenter<V> {


}