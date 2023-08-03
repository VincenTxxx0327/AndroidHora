package com.union.hora.home.presenter

import com.union.hora.base.BasePresenter
import com.union.hora.business.user.repossitory.UserRepository
import com.union.hora.home.contract.MineContract
import com.union.hora.home.model.MineModel

class MinePresenter(private val userRepo: UserRepository) : BasePresenter<MineContract.Model, MineContract.View>(), MineContract.Presenter {

    override fun createModel(): MineContract.Model = MineModel()

    override fun loadMinePageData() {
        userRepo.loadUserUsefulList {
            mView?.showUserUsefulList(it)
        }
        userRepo.loadUserExtraList {
            mView?.showUserExtraList(it)
        }
    }

}