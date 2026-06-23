package com.union.hora.model

import com.union.hora.base.BaseModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class PostModel : BaseModel() {
    fun getPosts(
        page: Int,
        pageSize: Int,
        isRefresh: Boolean,
        onSuccess: (List<Post>) -> Unit,
        onFailed: (String) -> Unit
    ) {
        // 模拟网络请求，返回模拟数据
        GlobalScope.launch {
            delay(1000)
            val startIndex = (page - 1) * pageSize
            val endIndex = startIndex + pageSize
            val mockPosts = mutableListOf<Post>()
            
            for (i in startIndex until endIndex) {
                mockPosts.add(
                    Post(
                        id = (i + 1).toString(),
                        userId = "10${i + 1}",
                        userName = "用户${i + 1}",
                        userAvatar = "https://randomuser.me/api/portraits/${if (i % 2 == 0) "women" else "men"}/${i + 1}.jpg",
                        content = "这是第${i + 1}个测试帖子，分享我的生活点滴。",
                        imageUrl = "https://picsum.photos/id/${1000 + i + 1}/600/800",
                        likes = 100 + i * 10,
                        comments = 20 + i * 5,
                        shares = 10 + i * 3
                    )
                )
            }

            withContext(Dispatchers.Main) {
                onSuccess(mockPosts)
            }
        }
    }
}

data class Post(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val content: String,
    val imageUrl: String,
    val likes: Int,
    val comments: Int,
    val shares: Int
)