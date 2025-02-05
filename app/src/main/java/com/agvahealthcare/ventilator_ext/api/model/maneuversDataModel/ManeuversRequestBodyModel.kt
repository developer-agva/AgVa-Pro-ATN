package com.agvahealthcare.ventilator_ext.api.model.maneuversDataModel

import com.google.gson.annotations.SerializedName

data class ManeuversRequestBodyModel(
    @SerializedName("status") var status : String? = null,
    @SerializedName("value") var value : String? = null,
    @SerializedName("date_time") var dateTime : String? = null,
    @SerializedName("flag") var flag : String? = null
)
