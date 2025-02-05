package com.agvahealthcare.ventilator_ext.api.model

import com.agvahealthcare.ventilator_ext.api.model.datamodel.Device
import com.google.gson.annotations.SerializedName

class LogRequestModel(
    @SerializedName("type") var type: String? = "001",
    @SerializedName("log") var log: RequestLog? = RequestLog(),
    @SerializedName("device") var device: Device? = Device(),
    @SerializedName("version") var version: String? = "1.0.0"

)


