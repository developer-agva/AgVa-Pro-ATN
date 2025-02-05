package com.agvahealthcare.ventilator_ext.api.model.calibrationDataModel

import android.provider.Settings
import com.google.gson.annotations.SerializedName

data class CalibrationRequestModel(
    @SerializedName("deviceId"     ) var did     : String? = null,
    @SerializedName("message" ) var message : String? = null,
    @SerializedName("date"    ) var date    : String? = null,
    @SerializedName("name"    ) var name    : String? = null
)
