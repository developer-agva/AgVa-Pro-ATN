package com.agvahealthcare.ventilator_ext.api.model.locationDataModel

import android.provider.Settings
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.google.gson.annotations.SerializedName

data class LocationRequestModel(
    @SerializedName("deviceId") var did: String? = "",
    @SerializedName("street") var street: String? = "",
    @SerializedName("city") var city: String? = "",
    @SerializedName("country") var country: String? = "",
    @SerializedName("pincode") var pincode: String? = "",
    @SerializedName("state") var state: String? = ""
)
