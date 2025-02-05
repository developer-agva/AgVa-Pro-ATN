package com.agvahealthcare.ventilator_ext.callback

/*
Due to JVM Restrictions on the similar intefasce with different types
This class is prohibited from use
 */
@Deprecated(
    message = "Due to JVM Restrictions on the similar intefasce with different types. This class is prohibited from use",
)
interface OnParameterClickListener<T> {
    fun onClick(position: Int, model: T)
}