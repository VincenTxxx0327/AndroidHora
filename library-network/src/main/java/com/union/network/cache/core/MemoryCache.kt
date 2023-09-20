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

import java.lang.reflect.Type

/**
 * 内存缓存
 * 内存缓存针对缓存的时间不好处理，暂时没有写内存缓存，等后面有思路了，再加上该部分
 * @Author： VincenT
 * @Time： 2023/8/15 17:52
 */
@Deprecated("")
class MemoryCache : BaseCache() {
    override fun doContainsKey(key: String): Boolean {
        return false
    }

    override fun isExpiry(key: String, existTime: Long): Boolean {
        return false
    }

    override fun <T> doLoad(type: Type, key: String): T? {
        return null
    }

    override fun <T> doSave(key: String, value: T): Boolean {
        return false
    }

    override fun doRemove(key: String): Boolean {
        return false
    }

    override fun doClear(): Boolean {
        return false
    }
}