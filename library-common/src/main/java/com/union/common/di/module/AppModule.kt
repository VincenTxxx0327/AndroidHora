package com.union.common.di.module

import android.app.Application
import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.union.common.integration.IRepositoryManager
import com.union.common.integration.RepositoryManager
import com.union.common.integration.cache.Cache
import com.union.common.integration.cache.CacheType
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 *
 * @Author： VincenT
 * @Time： 2023/8/17 23:42
 */
@Suppress("unused")
@Module
abstract class AppModule {

    @Binds
    abstract fun bindRepositoryManager(repositoryManager: RepositoryManager): IRepositoryManager

    fun interface GsonConfiguration {
        fun configGson(context: Context, builder: GsonBuilder)
    }

    @Module
    companion object {

        @JvmStatic
        @Singleton
        @Provides
        fun provideGson(application: Application, configuration: GsonConfiguration?): Gson {
            val builder = GsonBuilder()
            configuration?.configGson(application, builder)
            return builder.create()
        }

        @JvmStatic
        @Singleton
        @Provides
        fun provideExtras(cacheFactory: Cache.Factory<String, Any>): Cache<String, Any> {
            return cacheFactory.build(CacheType.EXTRAS)
        }
    }
}