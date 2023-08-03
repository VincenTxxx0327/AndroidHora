package com.union.hora.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.union.hora.R
import com.union.hora.http.bean.Moment

class ProjectAdapter : BaseQuickAdapter<Moment, BaseViewHolder>(R.layout.item_project_list), LoadMoreModule {

    init {
        addChildClickViewIds(R.id.item_project_list_like_iv)
    }

    override fun convert(holder: BaseViewHolder, item: Moment) {
//        val authorStr = if (item.author.isNotEmpty()) item.author else item.shareUser
//        holder.setText(R.id.item_project_list_title_tv, Html.fromHtml(item.title))
//            .setText(R.id.item_project_list_content_tv, Html.fromHtml(item.desc))
//            .setText(R.id.item_project_list_time_tv, item.niceDate)
//            .setText(R.id.item_project_list_author_tv, authorStr)
//            .setImageResource(
//                R.id.item_project_list_like_iv,
//                if (item.collect) R.drawable.ic_like else R.drawable.ic_like_not
//            )
//        ImageLoader.load(context, item.envelopePic, holder.getView(R.id.item_project_list_iv))
    }
}