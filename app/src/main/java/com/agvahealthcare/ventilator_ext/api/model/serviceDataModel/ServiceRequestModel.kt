package com.agvahealthcare.ventilator_ext.api.model.serviceDataModel

import android.provider.Settings
import com.google.gson.annotations.SerializedName

data class ServiceRequestModel(
    @SerializedName("deviceId"     ) var did     : String? = null,
    @SerializedName("message" ) var message : String? = null,
    @SerializedName("date"    ) var date    : String? = null,
    @SerializedName("name"    ) var name    : String? = null,
    @SerializedName("contactNo"    ) var contactNo    : String? = null,
    @SerializedName("hospitalName"    ) var hospitalName    : String? = null,
    @SerializedName("wardNo"    ) var wardNo    : String? = null,
    @SerializedName("email"    ) var email    : String? = null,
    @SerializedName("department"    ) var department    : String? = null,
)
