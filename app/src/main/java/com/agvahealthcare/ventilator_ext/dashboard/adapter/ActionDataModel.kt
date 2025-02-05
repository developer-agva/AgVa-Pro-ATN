package com.agvahealthcare.ventilator_ext.dashboard.adapter

sealed class ActionDataModel{
    data class ActionModelText(val text:String) : ActionDataModel()
    data class ActionModel(val image: Int) : ActionDataModel()
}
