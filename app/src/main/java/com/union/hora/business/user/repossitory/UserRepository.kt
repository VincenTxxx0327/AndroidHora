package com.union.hora.business.user.repossitory

import com.union.hora.app.constant.Constant
import com.union.hora.http.bean.UserIconBean
import com.union.hora.utils.Preference


class UserRepository {
    companion object {
        private const val TAG = "UserRepository"
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance() = instance ?: synchronized(this) {
            instance ?: UserRepository().also { instance = it }
        }
    }

    val hasLogin: Boolean by Preference(Constant.LOGIN_KEY, false)

    fun loadUserUsefulList(onSuccess: (ArrayList<UserIconBean>) -> Unit) {
        val list = ArrayList<UserIconBean>()
        if (hasLogin) {
            list.add(UserIconBean("789", "好友数", ""))
            list.add(UserIconBean("12", "驴友点", ""))
            list.add(UserIconBean("8888", "访客数", "", false, 15))
            list.add(UserIconBean("7", "我的内容", "", true))
        } else {
            list.add(UserIconBean("--", "好友数", ""))
            list.add(UserIconBean("--", "驴友点", ""))
            list.add(UserIconBean("--", "访客数", ""))
            list.add(UserIconBean("--", "我的内容", ""))
        }
        onSuccess.invoke(list)
    }

    fun loadUserExtraList(onSuccess: (ArrayList<UserIconBean>) -> Unit) {
        val list = ArrayList<UserIconBean>()
        list.add(UserIconBean("我的喜欢", "", "", readNum = 12))
        list.add(UserIconBean("我的收藏", "", ""))
        list.add(UserIconBean("使用指南", "", ""))
        list.add(UserIconBean("放心借", "", "", true))
        list.add(UserIconBean("我的公益", "", ""))
        list.add(UserIconBean("反馈与帮助", "", "", true))
        onSuccess.invoke(list)
    }

}