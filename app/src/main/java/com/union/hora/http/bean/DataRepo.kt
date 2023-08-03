package com.union.hora.http.bean

import com.squareup.moshi.Json
import org.litepal.crud.LitePalSupport
import java.io.Serializable
import java.sql.Date

data class CommonResponse<T>(
    @Json(name = "data") val data: T
) : BaseResponse()

// 通用的带有列表数据的实体
data class Page<T>(
    @Json(name = "current") val current: Int,
    @Json(name = "pages") val pages: Int,
    @Json(name = "records") val records: MutableList<T>,
    @Json(name = "searchCount") val searchCount: Boolean,
    @Json(name = "size") val size: Int,
    @Json(name = "total") val total: Int
)

//文章
data class MomentResponseBody(
    @Json(name = "curPage") val curPage: Int,
    @Json(name = "datas") var datas: MutableList<Moment>,
    @Json(name = "offset") val offset: Int,
    @Json(name = "over") val over: Boolean,
    @Json(name = "pageCount") val pageCount: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "total") val total: Int
)

//文章
data class Moment(
    @Json(name = "memberId") val memberId: Long,
    @Json(name = "codeTypeId") val codeTypeId: Long,
    @Json(name = "publishType") val publishType: Int,
    @Json(name = "contentType") val contentType: Int,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "contentImages") val contentImages: String,
    @Json(name = "publishTime") val publishTime: Date,
    @Json(name = "likes") val likes: Int,
    @Json(name = "forwards") val forwards: Int,
    @Json(name = "comments") val comments: Int,
    @Json(name = "cityId") val cityId: Long,
    @Json(name = "location") val location: String,
    @Json(name = "longitude") val longitude: String,
    @Json(name = "latitude") val latitude: String,
    @Json(name = "report") val report: Int,

    @Json(name = "memberName") val memberName: String,
    @Json(name = "memberImg") val memberImg: String,
    @Json(name = "hasLike") val hasLike: Boolean,
    @Json(name = "hasForward") val hasForward: Boolean,
    @Json(name = "hasComment") val hasComment: Boolean
) {
    override fun toString(): String {
        return "Moment(memberId=$memberId, codeTypeId=$codeTypeId, publishType=$publishType, contentType=$contentType, content='$content', contentImages='$contentImages', publishTime=$publishTime, likes=$likes, forwards=$forwards, comments=$comments, cityId=$cityId, location='$location', longitude='$longitude', latitude='$latitude', report=$report)"
    }
}

data class Tag(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)

//轮播图
data class Banner(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "coverImage") val coverImage: String,
    @Json(name = "createMemberId") val createMemberId: Long,
    @Json(name = "createTime") val createTime: Date,
    @Json(name = "roleId") val roleId: Long
) {
    override fun toString(): String {
        return "Banner(id=$id, title='$title', description='$description', coverImage='$coverImage', createMemberId=$createMemberId, createTime=$createTime, roleId=$roleId)"
    }

}

data class HotKey(
    @Json(name = "id") val id: Int,
    @Json(name = "link") val link: String,
    @Json(name = "name") val name: String,
    @Json(name = "order") val order: Int,
    @Json(name = "visible") val visible: Int
)

//常用网站
data class Friend(
    @Json(name = "icon") val icon: String,
    @Json(name = "id") val id: Int,
    @Json(name = "link") val link: String,
    @Json(name = "name") val name: String,
    @Json(name = "order") val order: Int,
    @Json(name = "visible") val visible: Int
)

//知识体系
data class KnowledgeTreeBody(
    @Json(name = "children") val children: MutableList<Knowledge>,
    @Json(name = "courseId") val courseId: Int,
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "order") val order: Int,
    @Json(name = "parentChapterId") val parentChapterId: Int,
    @Json(name = "visible") val visible: Int
) : Serializable

data class Knowledge(
    @Json(name = "children") val children: List<Any>,
    @Json(name = "courseId") val courseId: Int,
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "order") val order: Int,
    @Json(name = "parentChapterId") val parentChapterId: Int,
    @Json(name = "visible") val visible: Int
) : Serializable

// 登录数据
data class LoginData(
    @Json(name = "member") val member: MemberBean,
    @Json(name = "info") val info: MemberInfoBean
)

//收藏网站
data class CollectionWebsite(
    @Json(name = "desc") val desc: String,
    @Json(name = "icon") val icon: String,
    @Json(name = "id") val id: Int,
    @Json(name = "link") var link: String,
    @Json(name = "name") var name: String,
    @Json(name = "order") val order: Int,
    @Json(name = "userId") val userId: Int,
    @Json(name = "visible") val visible: Int
)

data class CollectionArticle(
    @Json(name = "author") val author: String,
    @Json(name = "chapterId") val chapterId: Int,
    @Json(name = "chapterName") val chapterName: String,
    @Json(name = "courseId") val courseId: Int,
    @Json(name = "desc") val desc: String,
    @Json(name = "envelopePic") val envelopePic: String,
    @Json(name = "id") val id: Int,
    @Json(name = "link") val link: String,
    @Json(name = "niceDate") val niceDate: String,
    @Json(name = "origin") val origin: String,
    @Json(name = "originId") val originId: Int,
    @Json(name = "publishTime") val publishTime: Long,
    @Json(name = "title") val title: String,
    @Json(name = "userId") val userId: Int,
    @Json(name = "visible") val visible: Int,
    @Json(name = "zan") val zan: Int
)

// 导航
data class NavigationBean(
    val moments: MutableList<Moment>,
    val cid: Int,
    val name: String
)

// 项目
data class ProjectTreeBean(
    @Json(name = "children") val children: List<Any>,
    @Json(name = "courseId") val courseId: Int,
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "order") val order: Int,
    @Json(name = "parentChapterId") val parentChapterId: Int,
    @Json(name = "visible") val visible: Int
)

// 热门搜索
data class HotSearchBean(
    @Json(name = "id") val id: Int,
    @Json(name = "link") val link: String,
    @Json(name = "name") val name: String,
    @Json(name = "order") val order: Int,
    @Json(name = "visible") val visible: Int
)

// 搜索历史
data class SearchHistoryBean(val key: String) : LitePalSupport() {
    val id: Long = 0
}

// TODO工具 类型
data class TodoTypeBean(
    val type: Int,
    val name: String,
    var isSelected: Boolean
)

// TODO实体类
data class TodoBean(
    @Json(name = "id") val id: Int,
    @Json(name = "completeDate") val completeDate: String,
    @Json(name = "completeDateStr") val completeDateStr: String,
    @Json(name = "content") val content: String,
    @Json(name = "date") val date: Long,
    @Json(name = "dateStr") val dateStr: String,
    @Json(name = "status") val status: Int,
    @Json(name = "title") val title: String,
    @Json(name = "type") val type: Int,
    @Json(name = "userId") val userId: Int,
    @Json(name = "priority") val priority: Int
) : Serializable

data class TodoListBean(
    @Json(name = "date") val date: Long,
    @Json(name = "todoList") val todoList: MutableList<TodoBean>
)

// 所有TODO，包括待办和已完成
data class AllTodoResponseBody(
    @Json(name = "type") val type: Int,
    @Json(name = "doneList") val doneList: MutableList<TodoListBean>,
    @Json(name = "todoList") val todoList: MutableList<TodoListBean>
)

data class TodoResponseBody(
    @Json(name = "curPage") val curPage: Int,
    @Json(name = "datas") val datas: MutableList<TodoBean>,
    @Json(name = "offset") val offset: Int,
    @Json(name = "over") val over: Boolean,
    @Json(name = "pageCount") val pageCount: Int,
    @Json(name = "size") val size: Int,
    @Json(name = "total") val total: Int
)

// 新增TODO的实体
data class AddTodoBean(
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "date") val date: String,
    @Json(name = "type") val type: Int
)

// 更新TODO的实体
data class UpdateTodoBean(
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "date") val date: String,
    @Json(name = "status") val status: Int,
    @Json(name = "type") val type: Int
)

// 公众号列表实体
data class WXChapterBean(
    @Json(name = "children") val children: MutableList<String>,
    @Json(name = "courseId") val courseId: Int,
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "order") val order: Int,
    @Json(name = "parentChapterId") val parentChapterId: Int,
    @Json(name = "userControlSetTop") val userControlSetTop: Boolean,
    @Json(name = "visible") val visible: Int
)

// 用户个人信息
data class UserInfoBody(
    @Json(name = "coinCount") val coinCount: Int, // 总积分
    @Json(name = "rank") val rank: Int, // 当前排名
    @Json(name = "userId") val userId: Int,
    @Json(name = "username") val username: String
)

// 个人积分实体
data class UserScoreBean(
    @Json(name = "coinCount") val coinCount: Int,
    @Json(name = "date") val date: Long,
    @Json(name = "desc") val desc: String,
    @Json(name = "id") val id: Int,
    @Json(name = "reason") val reason: String,
    @Json(name = "type") val type: Int,
    @Json(name = "userId") val userId: Int,
    @Json(name = "userName") val userName: String
)

// 个人积分实体
data class UserIconBean(
    @Json(name = "title") val title: String,
    @Json(name = "desc") val desc: String,
    @Json(name = "iconPath") val iconPath: String,
    @Json(name = "hasDot") val hasDot: Boolean = false,
    @Json(name = "readNum") val readNum: Int = 0,
    @Json(name = "showIcon") val showIcon: Boolean = true
)

// 排行榜实体
data class CoinInfoBean(
    @Json(name = "coinCount") val coinCount: Int,
    @Json(name = "level") val level: Int,
    @Json(name = "rank") val rank: Int,
    @Json(name = "userId") val userId: Int,
    @Json(name = "username") val username: String
)

// 我的分享
data class ShareResponseBody(
    val coinInfo: CoinInfoBean,
    val shareArticles: MomentResponseBody
)

data class MemberBean(
    @Json(name = "id") val id: Long,
    @Json(name = "username") val username: String,
    @Json(name = "role_ids") val role_ids: String
)

data class MemberInfoBean(
    @Json(name = "id") val id: Long,
    @Json(name = "avatar_url") val avatar_url: String
)