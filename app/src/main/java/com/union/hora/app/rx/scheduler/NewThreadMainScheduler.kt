package com.union.hora.app.rx.scheduler

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class NewThreadMainScheduler<T : Any> private constructor() : BaseScheduler<T>(Schedulers.newThread(), AndroidSchedulers.mainThread())
