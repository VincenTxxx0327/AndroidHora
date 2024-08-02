package com.widget.decoration

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager


class GridViewItemDecoration @JvmOverloads constructor(spanCount: Int = 2, spacing: Int = 0, includeEdge: Boolean = true) : ItemDecoration() {
    /**
     * 间距
     */
    private var mSpacing = 0
    /**
     * 列数
     */
    private var mSpanCount = 2
    /**
     * 距屏幕周围是否也有间距
     */
    private val mIncludeEdge: Boolean
    /**
     * 头部 不显示间距的item个数
     */
    private var mStartFromSize = 0
    /**
     * 尾部 不显示间距的item个数
     */
    private var mEndFromSize = 0

    /**
     * @param spanCount   item 每行个数
     * @param spacing     item 间距
     * @param includeEdge item 距屏幕周围是否也有间距
     */
    init {
        mSpanCount = spanCount
        mSpacing = spacing
        mIncludeEdge = includeEdge
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val lastPosition = state.itemCount - 1
        var position = parent.getChildAdapterPosition(view)
        if (mStartFromSize <= position && position <= lastPosition - mEndFromSize) {

            // 减掉不设置间距的position
            position = position - mStartFromSize
            var column = position % mSpanCount

            // 瀑布流获取列方式不一样
            val layoutParams: ViewGroup.LayoutParams = view.getLayoutParams()
            if (layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                column = layoutParams.spanIndex
            }
            if (mIncludeEdge) {
                /*
                 *示例：
                 * spacing = 10 ；spanCount = 3
                 * ---------10--------
                 * 10   3+7   6+4    10
                 * ---------10--------
                 * 10   3+7   6+4    10
                 * ---------10--------
                 */
                outRect.left = mSpacing - column * mSpacing / mSpanCount
                outRect.right = (column + 1) * mSpacing / mSpanCount
                if (position < mSpanCount) {
                    outRect.top = mSpacing
                }
                outRect.bottom = mSpacing
            } else {
                /*
                 *示例：
                 * spacing = 10 ；spanCount = 3
                 * --------0--------
                 * 0   3+7   6+4    0
                 * -------10--------
                 * 0   3+7   6+4    0
                 * --------0--------
                 */
                outRect.left = column * mSpacing / mSpanCount
                outRect.right = mSpacing - (column + 1) * mSpacing / mSpanCount
                if (position >= mSpanCount) {
                    outRect.top = mSpacing
                }
            }
        }
    }

    /**
     * 设置从哪个位置 开始设置间距
     *
     * @param startFromSize 一般为HeaderView的个数 + 刷新布局(不一定设置)
     */
    fun setStartFrom(startFromSize: Int): GridViewItemDecoration {
        mStartFromSize = startFromSize
        return this
    }

    /**
     * 设置从哪个位置 结束设置间距。默认为1，默认用户设置了上拉加载
     *
     * @param endFromSize 一般为FooterView的个数 + 加载更多布局(不一定设置)
     */
    fun setEndFromSize(endFromSize: Int): GridViewItemDecoration {
        mEndFromSize = endFromSize
        return this
    }

    /**
     * 设置从哪个位置 结束设置间距
     *
     * @param startFromSize 一般为HeaderView的个数 + 刷新布局(不一定设置)
     * @param endFromSize   默认为1，一般为FooterView的个数 + 加载更多布局(不一定设置)
     */
    fun setNoShowSpace(startFromSize: Int, endFromSize: Int): GridViewItemDecoration {
        mStartFromSize = startFromSize
        mEndFromSize = endFromSize
        return this
    }
}