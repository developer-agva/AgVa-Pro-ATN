package com.agvahealthcare.ventilator_ext.api.model.serviceDataModel

import android.provider.Settings
import com.google.gson.annotations.SerializedName

data class ServiceOtpVerifyModel(
    @SerializedName("deviceId"     ) var did     : String? = null,
    @SerializedName("otp"     ) var otp     : String? = null,
)

data class ServiceCloseRequestModel(
    @SerializedName("_id"     ) var _id     : String? = null,
    @SerializedName("contactNo"     ) var contactNo     : String? = null,
    @SerializedName("serviceEngName"     ) var serviceEngName     : String? = null,
    @SerializedName("UId"     ) var uid     : String? = "",
)

data class PaymentStatusRequestModel(
    @SerializedName("DeviceId"     ) var did: String? = null,
    @SerializedName("isPaymentDone"     ) var isPaymentDone     : String? = null,
    @SerializedName("isLocked"     ) var isLocked     : Boolean? = null
)