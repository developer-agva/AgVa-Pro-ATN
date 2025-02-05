package com.agvahealthcare.ventilator_ext.api.model

import com.agvahealthcare.ventilator_ext.api.model.datamodel.Device

data class LogPostBodyModel(
    val device: Device,
    val log: Log,
    val type: String,
    val version: String
)