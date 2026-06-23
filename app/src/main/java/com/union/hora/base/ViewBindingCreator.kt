package com.union.hora.base

import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

object ViewBindingCreator {
    @Suppress("UNCHECKED_CAST")
    fun <VB : ViewBinding?> createViewBinding(
        targetClass: Class<*>,
        layoutInflater: LayoutInflater?
    ): VB? {
        val type: Type? = targetClass.genericSuperclass

        if (type is ParameterizedType) {
            try {
                val types: Array<Type> = type.actualTypeArguments

                for (type1 in types) {
                    if (type1.typeName.endsWith("Binding")) {
                        val method: Method = (type1 as Class<*>).getMethod("inflate", LayoutInflater::class.java)
                        return method.invoke(null, layoutInflater) as VB
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}