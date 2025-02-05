package com.agvahealthcare.ventilator_ext.api.model.datamodel

import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.google.gson.annotations.SerializedName

data class Device (

    @SerializedName("did") var did : String?=AppUtils.getMacAddress(), // ToDO : add to build config
    @SerializedName("name") var name : String?=AppUtils.getDeviceName(), // ToDO : add to build config
    @SerializedName("manufacturer") var manufacturer : String?="Agva Healthcare", // ToDO : add to build config
    @SerializedName("battery") var battery : String?="",
    @SerializedName("os") var os : Os?=Os()

)