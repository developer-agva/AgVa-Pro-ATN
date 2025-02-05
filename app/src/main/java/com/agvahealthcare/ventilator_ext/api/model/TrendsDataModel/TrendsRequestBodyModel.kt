package com.agvahealthcare.ventilator_ext.api.model.TrendsDataModel

import android.provider.Settings
import com.google.gson.annotations.SerializedName

data class TrendsRequestBodyModel(
    @SerializedName("time")var time :String? = "",
    @SerializedName("averageLeak")var averageLeak: String? = "",
    @SerializedName("did")var did: String? = "",
    @SerializedName("fio2")var fio2: String? = "",
    @SerializedName("ie")var ie: String? = "",
    @SerializedName("mean_Airway")var mean_Airway: String? = "",
    @SerializedName("mode")var mode: String? = "",
    @SerializedName("mve")var mve: String? = "",
    @SerializedName("mvi")var mvi: String? = "",
    @SerializedName("peep")var peep: String? = "",
    @SerializedName("pip")var pip: String? = "",
    @SerializedName("respiratory_Rate")var respiratory_Rate: String? = "",
    @SerializedName("texp")var texp: String? = "",
    @SerializedName("tinsp")var tinsp: String? = "",
    @SerializedName("type")var type: String? = "",
    @SerializedName("vti")var vti: String? = "",
    @SerializedName("vte")var vte: String? = "",
    @SerializedName("sPo2")var spo2: String? = "",
    @SerializedName("pr")var pr: String? = ""
)