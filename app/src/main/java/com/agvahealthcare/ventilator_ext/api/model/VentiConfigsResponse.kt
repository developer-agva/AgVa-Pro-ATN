package com.agvahealthcare.ventilator_ext.api.model


import com.google.gson.annotations.SerializedName

data class VentiConfigsResponse(
    @SerializedName("data")
    val data: VentiConfigsData,
    @SerializedName("message")
    val message: String,
    @SerializedName("statusCode")
    val statusCode: Int,
    @SerializedName("statusValue")
    val statusValue: String
)

data class VentiConfigsData(
    @SerializedName("_id")
    val id: String,
    @SerializedName("Product_Code")
    val productCode: String,
    @SerializedName("Product_ID")
    val productID: String,
    @SerializedName("Product_Name")
    val productName: String,
    @SerializedName("sensors")
    val sensors: Sensors
)

data class Sensors(
    @SerializedName("Flow_Sensor")
    val flowSensor: String,
    @SerializedName("KNOB_PCB_TYPE")
    val kNOBPCBTYPE: String,
    @SerializedName("Nebuliser_TYPE")
    val nebuliserTYPE: String,
    @SerializedName("NeoNate_Sensor")
    val neoNateSensor: String,
    @SerializedName("Oxygen_Sensor")
    val oxygenSensor: String,
    @SerializedName("Pressure_Sensor")
    val pressureSensor: String,
    @SerializedName("Proportional_Valve")
    val proportionalValve: String,
    @SerializedName("SpO2_Sensor")
    val spO2Sensor: String
)

