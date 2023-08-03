package com.union.hora.http.bean

import com.chad.library.adapter.base.entity.SectionEntity

class TodoDataBean(var headerName: String = "", var todoBean: TodoBean? = null) : SectionEntity {

    override val isHeader: Boolean
        get() = headerName.isNotEmpty()

}