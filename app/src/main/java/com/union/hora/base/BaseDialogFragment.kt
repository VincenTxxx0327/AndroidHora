package com.union.hora.base

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.dylanc.longan.getCompatDrawable
import com.union.hora.R

abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = requireNotNull(_binding) { "The property of binding has been destroyed." }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = ViewBindingCreator.createViewBinding(javaClass, layoutInflater)
        initView()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, setAnimationStyle())
    }

    override fun onStart() {
        super.onStart()
        setViewBackground()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    abstract fun initView()

    fun setAnimationStyle() = R.style.BottomSheetDialogBg

    fun setViewBackground() {
        //设置动画、位置、宽度等属性（注意一：必须放在onStart方法中）
        val window = dialog!!.window
        if (window != null) {
            // 注意二：一定要设置Background，如果不设置，window属性设置无效
            window.setBackgroundDrawable(getCompatDrawable(android.R.color.transparent))
            val layoutParams = window.attributes
            layoutParams.gravity = Gravity.BOTTOM // 位置
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT //宽度满屏
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT //高度
            window.attributes = layoutParams
        }
    }
}