package com.union.network.cache.converter

import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * 通用转换器接口
 * 1.实现该接口可以实现一大波的磁盘存储操作
 * 2.可以实现Serializable、Gson,Parcelable、fastjson、xml、kryo等等
 * @Author： VincenT
 * @Time： 2023/8/15 17:51
 */
interface IDiskConverter {
    /**
     * 读取
     *
     * @param source 输入流
     * @param type  读取数据后要转换的数据类型
     * 这里没有用泛型T或者Tyepe来做，是因为本框架决定的一些问题，泛型会丢失
     * @return
     */
    fun <T> load(source: InputStream, type: Type): T?

    /**
     * 写入
     *
     * @param sink
     * @param data 保存的数据
     * @return
     */
    fun writer(sink: OutputStream, data: Any?): Boolean
}