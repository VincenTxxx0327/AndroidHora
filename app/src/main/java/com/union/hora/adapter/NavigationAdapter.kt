package com.union.hora.adapter

import android.app.ActivityOptions
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.union.hora.R
import com.union.hora.business.data.ContentActivity
import com.union.hora.http.bean.Moment
import com.union.hora.http.bean.NavigationBean
import com.union.hora.utils.CommonUtil
import com.union.hora.utils.DisplayManager
import com.zhy.view.flowlayout.FlowLayout
import com.zhy.view.flowlayout.TagAdapter
import com.zhy.view.flowlayout.TagFlowLayout

class NavigationAdapter : BaseQuickAdapter<NavigationBean, BaseViewHolder>(R.layout.item_navigation_list),
    LoadMoreModule {

    override fun convert(holder: BaseViewHolder, item: NavigationBean) {
        holder.setText(R.id.item_navigation_tv, item.name)
        val flowLayout: TagFlowLayout = holder.getView(R.id.item_navigation_flow_layout)
        val moments: List<Moment> = item.moments
        flowLayout.run {
            adapter = object : TagAdapter<Moment>(moments) {
                override fun getView(parent: FlowLayout?, position: Int, moment: Moment?): View? {

                    val tv: TextView = LayoutInflater.from(parent?.context).inflate(
                        R.layout.flow_layout_tv,
                        flowLayout, false
                    ) as TextView

                    moment ?: return null

                    val padding: Int = DisplayManager.dip2px(10F)
                    tv.setPadding(padding, padding, padding, padding)
                    tv.text = moment.title
                    tv.setTextColor(CommonUtil.randomColor())

                    setOnTagClickListener { view, position, _ ->
                        val options: ActivityOptions = ActivityOptions.makeScaleUpAnimation(
                            view,
                            view.width / 2,
                            view.height / 2,
                            0,
                            0
                        )
                        val data: Moment = moments[position]
//                        ContentActivity.start(context, data.id, data.title, data.link, options.toBundle())
                        true
                    }
                    return tv
                }
            }
        }
    }
}