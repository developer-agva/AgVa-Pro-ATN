package com.agvahealthcare.ventilator_ext.api.model

import com.google.gson.annotations.SerializedName

data class Os(
    @SerializedName("name" ) var name : String? = "Ubuntu 20.04",
    @SerializedName("type" ) var type : String? = "linux"
)