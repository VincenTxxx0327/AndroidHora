package com.union.hora.home.contract

import com.union.hora.base.IModel
import com.union.hora.base.IPresenter
import com.union.hora.base.IView
import com.union.hora.http.bean.CommonResponse
import com.union.hora.http.bean.ProjectTreeBean
import com.union.hora.http.bean.UserIconBean
import io.reactivex.rxjava3.core.Observable

interface MineContract {

    interface View : IView {

        fun showUserUsefulList(list: ArrayList<UserIconBean>)
        fun showUserExtraList(list: ArrayList<UserIconBean>)

    }

    interface Presenter : IPresenter<View> {

        fun loadMinePageData()

    }

    interface Model : IModel {
        fun requestProjectTree(): Observable<CommonResponse<List<ProjectTreeBean>>>
    }

}