package com.agvahealthcare.ventilator_ext.api.model.datamodel

import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils

import com.google.gson.annotations.SerializedName

data class Os (

    @SerializedName("name") var name : String?=AppUtils.getAndroidVersionName(), // ToDO : add to build config
    //there is a pre-defined enum in the backend
    @SerializedName("type") var type : String?="linux" // ToDO : add to build config
)