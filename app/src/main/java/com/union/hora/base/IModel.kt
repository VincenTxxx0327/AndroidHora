package com.union.hora.base

import io.reactivex.rxjava3.disposables.Disposable

interface IModel {

    fun addDisposable(disposable: Disposable?)

    fun onDetach()

}