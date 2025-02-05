package com.agvahealthcare.ventilator_ext.api.model

data class Log(
    val date: String,
    val `file`: String,
    val msg: String,
    val type: String
)