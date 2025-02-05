package com.agvahealthcare.ventilator_ext.api.model

import com.google.gson.annotations.SerializedName

data class RequestLog(
    @SerializedName("file" ) var file : String? = "SplashActivity.kt",
    @SerializedName("date" ) var date : String? = "2022-02-21",
    @SerializedName("msg"  ) var msg  : String? = "Testing from Mohit's system",
    @SerializedName("type" ) var type : String? = "error"
)
