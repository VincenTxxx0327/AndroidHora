package com.union.network.cache.converter

import com.union.network.utils.HttpLog
import com.union.network.utils.Utils
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * 序列化对象的转换器
 * 1.使用改转换器，对象&对象中的其它所有对象都必须是要实现Serializable接口（序列化）
 * @Author： VincenT
 * @Time： 2023/8/15 17:52
 */
class SerializableDiskConverter : IDiskConverter {
    @Suppress("UNCHECKED_CAST")
    override fun <T> load(source: InputStream, type: Type): T? {
        //序列化的缓存不需要用到clazz
        var value: T? = null
        var oin: ObjectInputStream? = null
        try {
            oin = ObjectInputStream(source)
            value = oin.readObject() as T
        } catch (e: IOException) {
            HttpLog.e(e)
        } catch (e: ClassNotFoundException) {
            HttpLog.e(e)
        } finally {
            Utils.close(oin)
        }
        return value
    }

    override fun writer(sink: OutputStream, data: Any?): Boolean {
        var oos: ObjectOutputStream? = null
        try {
            oos = ObjectOutputStream(sink)
            oos.writeObject(data)
            oos.flush()
            return true
        } catch (e: IOException) {
            HttpLog.e(e)
        } finally {
            Utils.close(oos)
        }
        return false
    }
}