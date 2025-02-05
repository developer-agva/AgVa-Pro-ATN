package com.agvahealthcare.ventilator_ext.api.model

import com.google.gson.annotations.SerializedName

data class DeviceIdResponseModel(
    @SerializedName("statusCode") var statusCode : Int? = 0,
    @SerializedName("deviceId"     ) var deviceId     : String? = ""
)
