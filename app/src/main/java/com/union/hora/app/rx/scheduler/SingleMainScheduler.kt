package com.union.hora.app.rx.scheduler

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class SingleMainScheduler<T : Any> private constructor() : BaseScheduler<T>(Schedulers.single(), AndroidSchedulers.mainThread())
