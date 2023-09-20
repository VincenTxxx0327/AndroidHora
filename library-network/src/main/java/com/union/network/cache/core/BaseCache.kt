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

import com.union.network.utils.Utils
import java.lang.reflect.Type
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * 缓存的基类
 * 1.所有缓存处理都继承该基类
 * 2.增加了锁机制，防止频繁读取缓存造成的异常
 * 3.子类直接考虑具体的实现细节就可以了
 * @Author： VincenT
 * @Time： 2023/8/15 17:52
 */
abstract class BaseCache {
    private val mLock: ReadWriteLock = ReentrantReadWriteLock()

    /**
     * 读取缓存
     *
     * @param key       缓存key
     * @param existTime 缓存时间
     */
    fun <T> load(type: Type, key: String, existTime: Long): T? {
        //1.先检查key
        Utils.checkNotNull(key, "key == null")

        //2.判断key是否存在,key不存在去读缓存没意义
        if (!containsKey(key)) {
            return null
        }

        //3.判断是否过期，过期自动清理
        if (isExpiry(key, existTime)) {
            remove(key)
            return null
        }

        //4.开始真正的读取缓存
        mLock.readLock().lock()
        return try {
            // 读取缓存
            doLoad<T>(type, key)
        } finally {
            mLock.readLock().unlock()
        }
    }

    /**
     * 保存缓存
     *
     * @param key   缓存key
     * @param value 缓存内容
     * @return
     */
    fun <T> save(key: String, value: T): Boolean {
        //1.先检查key
        Utils.checkNotNull(key, "key == null")

        //2.如果要保存的值为空,则删除
        if (value == null) {
            return remove(key)
        }

        //3.写入缓存
        mLock.writeLock().lock()
        val status: Boolean = try {
            doSave<T>(key, value)
        } finally {
            mLock.writeLock().unlock()
        }
        return status
    }

    /**
     * 删除缓存
     */
    fun remove(key: String): Boolean {
        mLock.writeLock().lock()
        return try {
            doRemove(key)
        } finally {
            mLock.writeLock().unlock()
        }
    }

    /**
     * 清空缓存
     */
    fun clear(): Boolean {
        mLock.writeLock().lock()
        return try {
            doClear()
        } finally {
            mLock.writeLock().unlock()
        }
    }

    /**
     * 是否包含 加final 是让子类不能被重写，只能使用doContainsKey<br></br>
     * 这里加了锁处理，操作安全。<br></br>
     *
     * @param key 缓存key
     * @return 是否有缓存
     */
    fun containsKey(key: String): Boolean {
        mLock.readLock().lock()
        return try {
            doContainsKey(key)
        } finally {
            mLock.readLock().unlock()
        }
    }

    /**
     * 是否包含  采用protected修饰符  被子类修改
     */
    protected abstract fun doContainsKey(key: String): Boolean

    /**
     * 是否过期
     */
    protected abstract fun isExpiry(key: String, existTime: Long): Boolean

    /**
     * 读取缓存
     */
    protected abstract fun <T> doLoad(type: Type, key: String): T?

    /**
     * 保存
     */
    protected abstract fun <T> doSave(key: String, value: T): Boolean

    /**
     * 删除缓存
     */
    protected abstract fun doRemove(key: String): Boolean

    /**
     * 清空缓存
     */
    protected abstract fun doClear(): Boolean
}