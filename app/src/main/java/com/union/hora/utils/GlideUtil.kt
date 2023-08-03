package com.union.hora.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.union.hora.R
import com.union.hora.app.App
import java.security.MessageDigest


object GlideUtil {

    // 1.开启无图模式 2.非WiFi环境 不加载图片
    private val isLoadImage = !SettingUtil.getIsNoPhotoMode() || NetWorkUtil.isWifi(App.context)

    /**
     * 加载图片
     * @param context
     * @param url
     * @param iv
     */
    fun load(context: Context?, url: String?, iv: ImageView?) {
        if (isLoadImage) {
            iv?.apply {
                Glide.with(context!!).clear(iv)
                val options = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .placeholder(R.drawable.bg_placeholder)
                Glide.with(context!!)
                    .load(url)
                    .transition(DrawableTransitionOptions().crossFade())
                    .apply(options)
                    .into(iv)
            }
        }
    }

    fun loadCircle(context: Context?, url: String?, iv: ImageView?) {
        if (isLoadImage) {
            iv?.apply {
                Glide.with(context!!).clear(iv)
                val options = RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .placeholder(R.drawable.bg_placeholder_circle)
                    .transform(GlideCircleTransform())
                Glide.with(context!!)
                    .load(url)
                    .transition(DrawableTransitionOptions().crossFade())
                    .apply(options)
                    .into(iv)
            }
        }
    }

    class GlideCircleTransform : BitmapTransformation() {
        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        }

        override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
            return circleCrop(pool, toTransform)!!
        }

        companion object {
            private fun circleCrop(pool: BitmapPool, source: Bitmap?): Bitmap? {
                if (source == null) return null
                val size = source.width.coerceAtMost(source.height)
                val x = (source.width - size) / 2
                val y = (source.height - size) / 2

                // TODO this could be acquired from the pool too
                val squared = Bitmap.createBitmap(source, x, y, size, size)
                var result: Bitmap? = pool[size, size, Bitmap.Config.ARGB_8888]
                if (result == null) {
                    result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                }
                val canvas = Canvas(result!!)
                val paint = Paint()
                paint.shader = BitmapShader(squared, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
                paint.isAntiAlias = true
                val r = size / 2f
                canvas.drawCircle(r, r, r, paint)
                return result
            }
        }
    }
}