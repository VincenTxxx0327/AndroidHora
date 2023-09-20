package com.union.network.utils

import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.Utils
import com.union.common.utils.getAppComponent
import java.io.File
import java.nio.charset.Charset

/**
 * http工具类
 * @Author： VincenT
 * @Time： 2023/8/15 21:11
 */
val UTF8: Charset = Charset.forName("UTF-8")

fun <V> escapeParams(map: Map<String?, V?>?): Map<String, V> {
    if (map.isNullOrEmpty()) {
        return mapOf()
    }
    val hashMap: LinkedHashMap<String, V> = linkedMapOf()
    for ((key, value) in map) {
        if (key != null && value != null) {
            hashMap[key] = value
        }
    }
    return hashMap
}

fun createUrlFromParams(url: String, params: Map<String, String>): String {
    try {
        val sb = StringBuilder()
        sb.append(url)
        if (url.indexOf('&') > 0 || url.indexOf('?') > 0) sb.append("&") else sb.append("?")
        for (entry: Map.Entry<String?, String> in params) {
            //对参数进行 utf-8 编码,防止头信息传中文
            //String urlValue = URLEncoder.encode(urlValues, UTF8.name());
            sb.append(entry.key).append("=").append(entry.value).append("&")
        }
        sb.deleteCharAt(sb.length - 1)
        return sb.toString()
    } catch (e: Exception) {
        HttpLog.e(e.message)
    }
    return url
}

/**
 * 根据文件目录和文件名创建文件
 */
fun createFile(fileDirName: String?, fileName: String): File {
    return File(createDir(fileDirName), fileName)
}

/**
 * 创建未存在的文件夹
 */
fun createDir(fileDirName: String?, type: String? = null): File? {
    val fileDirPath = Utils.getApp().getExternalFilesDir(type)?.absolutePath
        ?: Utils.getApp().cacheDir.absolutePath
    var dir = "$fileDirPath/"
    val folder = fileDirName ?: ""
    if (folder.isNotBlank()) {
        dir += "$folder/"
    }
    return createDir(File(dir))
}

/**
 * 创建未存在的文件夹
 */
fun createDir(file: File?): File? {
    return if (FileUtils.createOrExistsDir(file)) file else null
}

/**
 * 返回缓存文件夹
 * 应用在卸载后，会将 App-specific 目录下的数据删除。
 * Android Q 前提下，如果在 AndroidManifest.xml 中声明：android:hasFragileUserData="true" 用户可以选择是否保留。
 */
fun getCacheFile(): File {
    //获取应用程序内的外部缓存路劲
    var cacheFile = Utils.getApp().externalCacheDir
    //如果获取的文件为空,就使用自己定义的缓存文件夹做缓存路径
    cacheFile = cacheFile ?: createDir("")
    //如果获取的文件为空，就使用应用程序内的内部缓存路劲
    return cacheFile ?: Utils.getApp().cacheDir
}

/**
 * 获取Glide缓存目录文件
 */
fun getGlideCacheFile(): File? {
    return createDir(File(getAppComponent().cacheFile(), "glide"))
}

/**
 * 获取Glide缓存大小
 */
fun getGlideCacheSize(): Long {
    return FileUtils.getLength(getGlideCacheFile())
}

/**
 * 获取http数据缓存目录文件
 */
fun getHttpCacheFile(): File? {
    return createDir(File(getAppComponent().cacheFile(), "http"))
}

/**
 * 获取http数据缓存大小
 */
fun getHttpCacheSize(): Long {
    return FileUtils.getLength(getHttpCacheFile())
}