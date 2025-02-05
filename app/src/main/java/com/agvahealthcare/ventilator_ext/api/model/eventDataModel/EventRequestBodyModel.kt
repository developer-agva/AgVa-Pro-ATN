package com.agvahealthcare.ventilator_ext.api.model.eventDataModel

import android.provider.Settings
import com.google.gson.annotations.SerializedName


data class EventRequestBodyModel (
    @SerializedName("did"     ) var did     : String? = null,
    @SerializedName("type"    ) var type    : String? = null,
    @SerializedName("message" ) var message : String? = null,
    @SerializedName("date"    ) var date    : String? = null
)

