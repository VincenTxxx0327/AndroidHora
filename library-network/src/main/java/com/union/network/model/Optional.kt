package com.union.network.model

import io.reactivex.Observable

/**
 * 为了使Rxjava2 onNext 返回null,使用了此包装类，进行过渡
 * @Author： VincenT
 * @Time： 2023/8/15 21:23
 */
@Deprecated("")
class Optional<T>(obs: Observable<T>) {
    var obs: Observable<T>

    init {
        this.obs = obs
    }

    fun get(): T {
        return obs.blockingSingle()
    }

    fun orElse(defaultValue: T): T {
        return obs.defaultIfEmpty(defaultValue).blockingSingle()
    }

    companion object {
        fun <T> of(value: T?): Optional<T> {
            return if (value == null) {
                throw NullPointerException()
            } else {
                Optional(Observable.just(value))
            }
        }

        fun <T> ofNullable(value: T?): Optional<T> {
            return if (value == null) {
                Optional(Observable.empty())
            } else {
                Optional(Observable.just(value))
            }
        }
    }
}