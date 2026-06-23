package com.union.hora.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel: ViewModel() {
    val isRefreshing = MutableLiveData<Boolean>()
    val text = MutableLiveData<String>()
}