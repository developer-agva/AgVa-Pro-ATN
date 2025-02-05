package com.agvahealthcare.ventilator_ext.api.model.datamodel

import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.google.gson.annotations.SerializedName


enum class LogType(val value: String) {
    VERBOSE("verbose"),
    DEBUG("debug"),
    INFO("info"),
    WARN("warn"),
    ERROR("error"),

}

data class RequestLogModel(

    @SerializedName("did"  ) var did  : String? = null,
    @SerializedName("file") var file : String?="SplashActivity.kt",
    @SerializedName("date") var date : String?=AppUtils.getCurrentDateReverse(),
    @SerializedName("msg") var msg : String?=null,
    @SerializedName("type") var type : String? = LogType.VERBOSE.value,

)