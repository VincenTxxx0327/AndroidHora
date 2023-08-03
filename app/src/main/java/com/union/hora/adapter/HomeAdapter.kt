package com.union.hora.adapter

import android.text.Html
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.module.UpFetchModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.union.hora.R
import com.union.hora.http.bean.Moment
import com.union.hora.utils.GlideUtil

class HomeAdapter : BaseQuickAdapter<Moment, BaseViewHolder>(R.layout.item_common_grid), LoadMoreModule, UpFetchModule {

    init {
        addChildClickViewIds(R.id.iv_moment_likeImg)
    }

    override fun convert(holder: BaseViewHolder, item: Moment) {
        holder.setText(R.id.tv_moment_title, Html.fromHtml(item.content))
            .setText(R.id.tv_moment_author, item.memberName)
            .setText(R.id.tv_moment_likeNum, item.likes.toString())
        GlideUtil.load(context, item.contentImages, holder.getView(R.id.iv_moment_image))
        GlideUtil.loadCircle(context, item.memberImg, holder.getView(R.id.iv_moment_authorImg))
    }
}
