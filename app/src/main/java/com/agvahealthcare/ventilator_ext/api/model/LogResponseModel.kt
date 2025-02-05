package com.agvahealthcare.ventilator_ext.api.model

import Data
import com.google.gson.annotations.SerializedName

data class LogResponseModel (
    @SerializedName("statusCode") val status : Int,
    @SerializedName("status") val statusCodeNew : Int,
    @SerializedName("message") val message : String,
    @SerializedName("data") val data : Data
)