package com.agvahealthcare.ventilator_ext.api.model

import android.provider.Settings
import com.agvahealthcare.ventilator_ext.BuildConfig
import com.agvahealthcare.ventilator_ext.api.model.datamodel.Device
import com.agvahealthcare.ventilator_ext.api.model.datamodel.RequestLogModel
import com.google.gson.annotations.SerializedName

data class CrashRequestBody(
    @SerializedName("deviceId") var did: String? = Settings.Secure.ANDROID_ID,
    @SerializedName("version") var version: String?= BuildConfig.VERSION_NAME,
    @SerializedName("message") var msg: String?= "",
    @SerializedName("file") var file : String? = "",
    @SerializedName("date") var date:String? = ""
)
