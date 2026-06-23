package com.union.hora.contract

import com.union.hora.base.IView
import com.union.hora.base.IPresenter
import com.union.hora.model.Post

interface PostContract {
    interface View : IView {
        fun showPosts(posts: List<Post>, isRefresh: Boolean)
        fun showError(errorMsg: String)
    }

    interface Presenter : IPresenter<View> {
        fun getPosts(page: Int, pageSize: Int, isRefresh: Boolean)
    }
}