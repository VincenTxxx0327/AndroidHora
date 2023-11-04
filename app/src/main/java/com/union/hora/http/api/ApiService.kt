package com.union.hora.http.api

import com.union.hora.http.bean.*
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.*

interface ApiService {

    @GET("advert")
    fun loadAdvertList(): Observable<CommonResponse<Page<Banner>>>

    @GET("moment/page")
    fun loadMomentList(@QueryMap map: MutableMap<String, Any>): Observable<CommonResponse<Page<Moment>>>

    @GET("moment/page/{order}")
    fun getTopArticles(@Path("order") orderType: Int): Observable<CommonResponse<Page<Moment>>>

    @GET("tree/json")
    fun getKnowledgeTree(): Observable<CommonResponse<List<KnowledgeTreeBody>>>

    @GET("article/list/{page}/json")
    fun getKnowledgeList(@Path("page") page: Int, @Query("cid") cid: Int): Observable<CommonResponse<MomentResponseBody>>

    @GET("navi/json")
    fun getNavigationList(): Observable<CommonResponse<List<NavigationBean>>>

    @GET("project/tree/json")
    fun getProjectTree(): Observable<CommonResponse<List<ProjectTreeBean>>>

    @GET("project/list/{page}/json")
    fun getProjectList(@Path("page") page: Int, @Query("cid") cid: Int): Observable<CommonResponse<MomentResponseBody>>

    @POST("login")
    @FormUrlEncoded
    fun loginMember(@Field("username") username: String,
                    @Field("password") password: String): Observable<CommonResponse<LoginData>>

    @POST("register")
    @FormUrlEncoded
    fun registerMember(@Field("username") username: String,
                       @Field("password") password: String,
                       @Field("role_ids") role_ids: String): Observable<CommonResponse<LoginData>>

    @GET("user/logout/json")
    fun logout(): Observable<CommonResponse<Any>>

    @GET("lg/collect/list/{page}/json")
    fun getCollectList(@Path("page") page: Int): Observable<CommonResponse<Page<CollectionArticle>>>

    @POST("lg/collect/{id}/json")
    fun addCollectArticle(@Path("id") id: Int): Observable<CommonResponse<Any>>

    @POST("lg/collect/add/json")
    @FormUrlEncoded
    fun addCoolectOutsideArticle(@Field("title") title: String,
                                 @Field("author") author: String,
                                 @Field("link") link: String): Observable<CommonResponse<Any>>

    @POST("lg/uncollect_originId/{id}/json")
    fun cancelCollectArticle(@Path("id") id: Int): Observable<CommonResponse<Any>>

    @POST("lg/uncollect/{id}/json")
    @FormUrlEncoded
    fun removeCollectArticle(@Path("id") id: Int,
                             @Field("originId") originId: Int = -1): Observable<CommonResponse<Any>>

    @GET("hotkey/json")
    fun getHotSearchData(): Observable<CommonResponse<MutableList<HotSearchBean>>>

    @POST("article/query/{page}/json")
    @FormUrlEncoded
    fun queryBySearchKey(@Path("page") page: Int,
                         @Field("k") key: String): Observable<CommonResponse<MomentResponseBody>>

    @POST("/lg/todo/list/{type}/json")
    fun getTodoList(@Path("type") type: Int): Observable<CommonResponse<AllTodoResponseBody>>

    @POST("/lg/todo/listnotdo/{type}/json/{page}")
    fun getNoTodoList(@Path("page") page: Int, @Path("type") type: Int): Observable<CommonResponse<TodoResponseBody>>

    @POST("/lg/todo/listdone/{type}/json/{page}")
    fun getDoneList(@Path("page") page: Int, @Path("type") type: Int): Observable<CommonResponse<TodoResponseBody>>

    @GET("/lg/todo/v2/list/{page}/json")
    fun getTodoList(@Path("page") page: Int, @QueryMap map: MutableMap<String, Any>): Observable<CommonResponse<AllTodoResponseBody>>

    @POST("/lg/todo/done/{id}/json")
    @FormUrlEncoded
    fun updateTodoById(@Path("id") id: Int, @Field("status") status: Int): Observable<CommonResponse<Any>>

    @POST("/lg/todo/delete/{id}/json")
    fun deleteTodoById(@Path("id") id: Int): Observable<CommonResponse<Any>>

    @POST("/lg/todo/add/json")
    @FormUrlEncoded
    fun addTodo(@FieldMap map: MutableMap<String, Any>): Observable<CommonResponse<Any>>

    @POST("/lg/todo/update/{id}/json")
    @FormUrlEncoded
    fun updateTodo(@Path("id") id: Int, @FieldMap map: MutableMap<String, Any>): Observable<CommonResponse<Any>>

    @GET("/wxarticle/chapters/json")
    fun getWXChapters(): Observable<CommonResponse<MutableList<WXChapterBean>>>

    @GET("/wxarticle/list/{id}/{page}/json")
    fun getWXArticles(@Path("id") id: Int,
                      @Path("page") page: Int): Observable<CommonResponse<MomentResponseBody>>

    @GET("/wxarticle/list/{id}/{page}/json")
    fun queryWXArticles(@Path("id") id: Int,
                        @Query("k") key: String,
                        @Path("page") page: Int): Observable<CommonResponse<MomentResponseBody>>

    @GET("/lg/coin/userinfo/json")
    fun getUserInfo(): Observable<CommonResponse<UserInfoBody>>

    @GET("/lg/coin/list/{page}/json")
    fun getUserScoreList(@Path("page") page: Int): Observable<CommonResponse<Page<UserScoreBean>>>

    @GET("/coin/rank/{page}/json")
    fun getRankList(@Path("page") page: Int): Observable<CommonResponse<Page<CoinInfoBean>>>

    @GET("user/lg/private_articles/{page}/json")
    fun getShareList(@Path("page") page: Int): Observable<CommonResponse<ShareResponseBody>>

    @POST("lg/user_article/add/json")
    @FormUrlEncoded
    fun shareArticle(@FieldMap map: MutableMap<String, Any>): Observable<CommonResponse<Any>>

    @POST("lg/user_article/delete/{id}/json")
    fun deleteShareArticle(@Path("id") id: Int): Observable<CommonResponse<Any>>

    @GET("user_article/list/{page}/json")
    fun getSquareList(@Path("page") page: Int): Observable<CommonResponse<MomentResponseBody>>

}