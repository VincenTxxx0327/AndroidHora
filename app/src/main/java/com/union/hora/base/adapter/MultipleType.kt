package com.union.hora.base.adapter

@Deprecated("")
interface MultipleType<in T> {
    fun getLayoutId(item: T, position: Int): Int
}
