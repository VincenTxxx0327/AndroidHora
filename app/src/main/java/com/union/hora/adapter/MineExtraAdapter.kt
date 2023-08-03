package com.union.hora.adapter

import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.UpFetchModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.union.hora.R
import com.union.hora.http.bean.UserIconBean
import com.union.hora.utils.GlideUtil

class MineExtraAdapter : BaseQuickAdapter<UserIconBean, BaseViewHolder>(R.layout.item_mine_extra), UpFetchModule {

    override fun convert(holder: BaseViewHolder, item: UserIconBean) {
        holder.setText(R.id.tv_mine_desc, item.title)
            .setText(R.id.tv_mine_read, "${item.readNum}+")
        val tvRead = holder.getView<TextView>(R.id.tv_mine_read)
        tvRead.visibility = if (tvRead.text.isNullOrEmpty() || item.readNum == 0) View.INVISIBLE else View.VISIBLE
        val tvTips = holder.getView<TextView>(R.id.tv_mine_tips)
        tvTips.visibility = if (item.hasDot) View.VISIBLE else View.INVISIBLE
        GlideUtil.load(context, item.iconPath, holder.getView(R.id.tv_mine_title))
    }

}