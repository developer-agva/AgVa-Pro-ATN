package com.agvahealthcare.ventilator_ext.api.model.statusDataModel

import com.google.gson.annotations.SerializedName

data class StatusRequestModel(
    @SerializedName("deviceId"     ) var did     : String? = null,
    @SerializedName("message" ) var message : String? = null,
    @SerializedName("last_hours" ) var last_hours : String? = null,
    @SerializedName("total_hours" ) var total_hours : String? = null,
    @SerializedName("health" ) var health : String? = null,
    @SerializedName("address" ) var address : String? = null,
)

data class PatientDetailsRequestModel(
    @SerializedName("UHID"     ) var UHID     : String? = "",
    @SerializedName("age" ) var age : String? = null,
    @SerializedName("patientProfile" ) var patient_Profile : String? = "",
    @SerializedName("weight" ) var weight : String? = null,
    @SerializedName("height" ) var height : String? = null,
    @SerializedName("deviceId" ) var deviceId : String? = null,
    @SerializedName("ward_no" ) var ward_no : String? = "",
    @SerializedName("alias_name" ) var alias_name : String? = "",
)