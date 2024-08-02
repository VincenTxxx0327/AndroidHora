package com.union.hora.app.rx.scheduler

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class IoMainScheduler<T : Any> : BaseScheduler<T>(Schedulers.io(), AndroidSchedulers.mainThread())
