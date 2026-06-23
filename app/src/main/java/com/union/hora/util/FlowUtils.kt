package com.union.hora.util

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class FlowUtils {

    fun AppCompatActivity.countDown(time: Int = 60, onStart: () -> Unit = {}, onTick: (String) -> Unit = {}, onFinish: () -> Unit = {}) {
        lifecycleScope.launch {
            flow {
                (time downTo 0L).forEach { num ->
                    delay(1000)
                    emit(num)
                }
            }.onStart {
                onStart()
            }.onCompletion {
                onFinish()
            }.catch {
                onFinish()
            }.collect {
                onTick(it.toString())
            }
        }
    }
}