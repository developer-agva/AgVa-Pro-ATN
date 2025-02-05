package com.agvahealthcare.ventilator_ext.api.model.serviceDataModel

import com.google.gson.annotations.SerializedName

class VentiDetailsResponseModel(
    @SerializedName("data") val data: VentiDetailsData,
    @SerializedName("message") val message: String,
    @SerializedName("statusCode") val statusCode: Int,
    @SerializedName("statusValue") val statusValue: String,
)

class VentiDetailsData(
    @SerializedName("_id") val _id: String,
    @SerializedName("Alias_Name") val Alias_Name: String,
    @SerializedName("Bio_Med") val Bio_Med: String,
    @SerializedName("Department_Name") val Department_Name: String,
    @SerializedName("DeviceId") val ServiceRequestDeviceId: String,
    @SerializedName("Doctor_Name") val Doctor_Name: String,
    @SerializedName("Hospital_Name") val Hospital_Name: String,
    @SerializedName("IMEI_NO") val IMEI_NO: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("Ward_No") val Ward_No: String,
    @SerializedName("isAssigned") val isAssigned: String,
    @SerializedName("address") val address: String,
    @SerializedName("health") val health: String,
    @SerializedName("last_hours") val last_hours: String,
    @SerializedName("total_hours") val total_hours : String,
    @SerializedName("isPaymentDone") val isPaymentDone : Boolean,
    @SerializedName("isLocked") val isLocked : Boolean
)