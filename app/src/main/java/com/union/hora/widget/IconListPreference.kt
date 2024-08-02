package com.widget

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.preference.ListPreference
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import com.union.hora.R
import com.union.hora.base.BaseActivity
import kotlinx.android.synthetic.main.item_icon_listpreference.view.*
import kotlinx.android.synthetic.main.item_icon_listpreference_preview.view.*
import java.util.*

class IconListPreference(context: Context, attrs: AttributeSet) : ListPreference(context, attrs) {

    private val drawableList = ArrayList<Drawable>()

    init {
        val ta = context.theme.obtainStyledAttributes(attrs, R.styleable.IconListPreference, 0, 0)
        val drawables: Array<CharSequence>
        try {
            drawables = ta.getTextArray(R.styleable.IconListPreference_iconsDrawables)
        } finally {
            ta.recycle()
        }
        for (drawable in drawables) {
            val resId = context.resources.getIdentifier(drawable.toString(), "mipmap", context.packageName)
            val d = context.resources.getDrawable(resId)
            drawableList.add(d)
        }

        widgetLayoutResource = R.layout.item_icon_listpreference_preview
    }

    private fun createListAdapter(): ListAdapter {
        val selectedValue = value
        val selectedIndex = findIndexOfValue(selectedValue)
        return IconArrayAdapter(
            context, R.layout.item_icon_listpreference,
            entries, drawableList, selectedIndex
        )
    }


    override fun onBindView(view: View) {
        super.onBindView(view)

        val selectedValue = value
        val selectedIndex = findIndexOfValue(selectedValue)

        val drawable = drawableList[selectedIndex]
        view.run {
            iv_preview.setImageDrawable(drawable)
        }
    }

    override fun onPrepareDialogBuilder(builder: android.app.AlertDialog.Builder?) {
        builder?.setAdapter(createListAdapter(), this)
        builder?.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        super.onPrepareDialogBuilder(builder)
    }

    private inner class IconArrayAdapter(
        context: Context, textViewResourceId: Int,
        objects: Array<CharSequence>, imageDrawables: List<Drawable>,
        selectedIndex: Int
    ) : ArrayAdapter<CharSequence>(context, textViewResourceId, objects) {

        private var list: List<Drawable>? = null
        private var selectedIndex = 0

        init {
            this.selectedIndex = selectedIndex
            this.list = imageDrawables
        }

        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val inflater = (context as BaseActivity).layoutInflater
            val view = inflater.inflate(R.layout.item_icon_listpreference, parent, false)

            view.run {
                label.text = getItem(position)
                label.isChecked = position == selectedIndex

                icon.setImageDrawable(list!![position])
            }
            return view
        }
    }
}