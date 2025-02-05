package com.agvahealthcare.ventilator_ext.api.model

data class OTAResponse(
    val data: ArrayList<DataX>,
    val message: String,
    val statusCode: Int,
    val statusValue: String
)

data class DataX(
    val __v: Int,
    val _id: String,
    val app_url: String,
    val createdAt: String,
    val dateTime: String,
    val project_code: String,
    val updatedAt: String,
    val version: String
)