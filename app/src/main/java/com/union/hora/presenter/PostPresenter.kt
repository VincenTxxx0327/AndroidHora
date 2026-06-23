package com.union.hora.presenter

import com.union.hora.base.BasePresenter
import com.union.hora.contract.PostContract
import com.union.hora.model.PostModel

class PostPresenter : BasePresenter<PostModel, PostContract.View>(), PostContract.Presenter {

    override fun createModel(): PostModel? {
        return PostModel()
    }

    override fun getPosts(page: Int, pageSize: Int, isRefresh: Boolean) {
        mModel?.getPosts(page, pageSize, isRefresh,
            onSuccess = {
                mView?.showPosts(it, isRefresh)
            },
            onFailed = {
                mView?.showError(it)
            }
        )
    }
}