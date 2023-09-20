/*
 * Copyright (C) 2017 zhouyou(478319399@qq.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.union.network.cache.core

import com.jakewharton.disklrucache.DiskLruCache
import com.union.network.cache.converter.IDiskConverter
import com.union.network.utils.Utils
import java.io.File
import java.io.IOException
import java.lang.reflect.Type

/**
 * 磁盘缓存实现类
 * @Author： VincenT
 * @Time： 2023/8/15 17:52
 */
class LruDiskCache(private val diskConverter: IDiskConverter, diskDir: File?, appVersion: Int, diskMaxSize: Long) : BaseCache() {

    private val mDiskLruCache by lazy { DiskLruCache.open(diskDir, appVersion, 1, diskMaxSize) }

    override fun <T> doLoad(type: Type, key: String): T? {
        mDiskLruCache?.let {
            try {
                val edit = it.edit(key) ?: return null
                val source = edit.newInputStream(0)
                val value: T?
                if (source != null) {
                    value = diskConverter.load(source, type)
                    Utils.close(source)
                    edit.commit()
                    return value
                }
                edit.abort()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
        return null
    }

    override fun <T> doSave(key: String, value: T): Boolean {
        mDiskLruCache?.let {
            try {
                val edit = it.edit(key) ?: return false
                val sink = edit.newOutputStream(0)
                if (sink != null) {
                    val result: Boolean = diskConverter.writer(sink, value)
                    Utils.close(sink)
                    edit.commit()
                    return result
                }
                edit.abort()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return false
        }
        return false
    }

    override fun doContainsKey(key: String): Boolean {
        mDiskLruCache?.let {
            try {
                return it.get(key) != null
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return false
        }
        return false
    }

    override fun doRemove(key: String): Boolean {
        mDiskLruCache?.let {
            try {
                return it.remove(key)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return false
        }
        return false
    }

    override fun doClear(): Boolean {
        mDiskLruCache?.let {
            try {
                it.delete()
                return true
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return false
        }
        return false
    }

    override fun isExpiry(key: String, existTime: Long): Boolean {
        mDiskLruCache?.let {
            if (existTime > -1) { //-1表示永久性存储 不用进行过期校验
                //为什么这么写，请了解DiskLruCache，看它的源码
                val file = File(it.directory, "$key.0")
                if (isCacheDataFailure(file, existTime)) { //没有获取到缓存,或者缓存已经过期!
                    return true
                }
            }
            return false
        }
        return false
    }

    /**
     * 判断缓存是否已经失效
     */
    private fun isCacheDataFailure(dataFile: File, time: Long): Boolean {
        if (!dataFile.exists()) {
            return false
        }
        val existTime = System.currentTimeMillis() - dataFile.lastModified()
        return existTime > time * 1000
    }
}