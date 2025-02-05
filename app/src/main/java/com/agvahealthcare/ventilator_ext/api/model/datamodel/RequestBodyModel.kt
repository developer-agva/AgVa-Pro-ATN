package com.agvahealthcare.ventilator_ext.api.model.datamodel

import com.agvahealthcare.ventilator_ext.BuildConfig
import com.google.gson.annotations.SerializedName

data class RequestBodyModel(
    @SerializedName("did"  ) var did: String? =Device().did,
    @SerializedName("version") var version: String?= BuildConfig.VERSION_NAME,
    @SerializedName("type") var type: String?= BuildConfig.PROJECT_TYPE,
    @SerializedName("log") var log: RequestLogModel?=RequestLogModel(),
    @SerializedName("device") var device: Device?=Device()
)