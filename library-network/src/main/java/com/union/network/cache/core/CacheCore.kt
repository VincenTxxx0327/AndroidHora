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

import com.union.network.utils.HttpLog
import okio.ByteString
import java.lang.reflect.Type

/**
 * 缓存核心管理类
 * 1.采用LruDiskCache
 * 2.对Key进行MD5加密
 * 以后可以扩展 增加内存缓存，但是内存缓存的时间不好控制，暂未实现，后续可以添加
 * @Author： VincenT
 * @Time： 2023/8/15 17:52
 */
class CacheCore(private val disk: LruDiskCache?) {

    /**
     * 读取
     */
    @Synchronized
    fun <T> load(type: Type, key: String, time: Long): T? {
        val cacheKey: String = ByteString.of(*key.toByteArray()).md5().hex()
        HttpLog.d("loadCache  key=$cacheKey")
        if (disk != null) {
            val result = disk.load<T>(type, cacheKey, time)
            if (result != null) {
                return result
            }
        }
        return null
    }

    /**
     * 保存
     */
    @Synchronized
    fun <T> save(key: String, value: T): Boolean {
        val cacheKey: String = ByteString.of(*key.toByteArray()).md5().hex()
        HttpLog.d("saveCache  key=$cacheKey")
        return disk!!.save(cacheKey, value)
    }

    /**
     * 是否包含
     *
     * @param key
     * @return
     */
    @Synchronized
    fun containsKey(key: String): Boolean {
        val cacheKey: String = ByteString.of(*key.toByteArray()).md5().hex()
        HttpLog.d("containsCache  key=$cacheKey")
        if (disk != null) {
            if (disk.containsKey(cacheKey)) {
                return true
            }
        }
        return false
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    @Synchronized
    fun remove(key: String): Boolean {
        val cacheKey: String = ByteString.of(*key.toByteArray()).md5().hex()
        HttpLog.d("removeCache  key=$cacheKey")
        return disk?.remove(cacheKey) ?: true
    }

    /**
     * 清空缓存
     */
    @Synchronized
    fun clear(): Boolean {
        return disk?.clear() ?: false
    }
}