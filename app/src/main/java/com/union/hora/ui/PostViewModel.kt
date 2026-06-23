package com.union.hora.ui

import androidx.lifecycle.ViewModel
import com.union.hora.model.Post
import com.union.hora.presenter.PostPresenter
import com.union.hora.contract.PostContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PostViewModel : ViewModel(), PostContract.View {
    private val presenter = PostPresenter()

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var page = 1
    private val pageSize = 5

    init {
        presenter.attachView(this)
        getPosts()
    }

    fun getPosts(isRefresh: Boolean = true) {
        if (isRefresh) {
            page = 1
            _isLoading.value = true
        } else {
            _isLoadingMore.value = true
        }
        presenter.getPosts(page, pageSize, isRefresh)
    }

    override fun showPosts(posts: List<Post>, isRefresh: Boolean) {
        if (isRefresh) {
            _posts.value = posts
        } else {
            _posts.value = _posts.value + posts
        }
        page++
        hideLoading()
    }

    override fun showError(errorMsg: String) {
        _error.value = errorMsg
        hideLoading()
    }

    override fun showLoading() {
        _isLoading.value = true
    }

    override fun hideLoading() {
        _isLoading.value = false
        _isLoadingMore.value = false
    }

    override fun getContext() = null

    override fun showToastMsg(msg: String) {
        // 可以在这里实现Toast显示逻辑
    }

    override fun showErrorMsg(msg: String) {
        // 可以在这里实现错误消息显示逻辑
        _error.value = msg
    }

    override fun onCleared() {
        presenter.detachView()
        super.onCleared()
    }
}