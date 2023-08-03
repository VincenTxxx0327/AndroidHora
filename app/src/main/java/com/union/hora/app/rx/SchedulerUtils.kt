package com.union.hora.app.rx

import com.union.hora.app.rx.scheduler.IoMainScheduler

object SchedulerUtils {

    fun <T> ioToMain(): IoMainScheduler<T> {
        return IoMainScheduler()
    }

}