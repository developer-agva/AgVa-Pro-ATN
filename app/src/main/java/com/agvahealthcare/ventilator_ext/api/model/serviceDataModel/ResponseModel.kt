package com.agvahealthcare.ventilator_ext.api.model.serviceDataModel

import com.google.gson.annotations.SerializedName

class ResponseModel(
    @SerializedName("currentPage") val ServiceRequestCurrentPage: Int,
    @SerializedName("data") val ServiceRequestData: ArrayList<Data>,
    @SerializedName("message") val ServiceRequestMessage: String,
    @SerializedName("statusCode") val ServiceRequestStatusCode: Int,
    @SerializedName("statusValue") val ServiceRequestStatusValue: String,
    @SerializedName("totalDataCount") val ServiceRequestTotalDataCount: Int,
    @SerializedName("totalPages") val ServiceRequestTotalPages: Int
)

class Data(
    @SerializedName("_id") val ServiceRequest_id: String,
    @SerializedName("contactNo") val ServiceRequestContactNo: String,
    @SerializedName("date") val ServiceRequestDate: String,
    @SerializedName("department") val ServiceRequestDepartment: String,
    @SerializedName("deviceId") val ServiceRequestDeviceId: String,
    @SerializedName("email") val ServiceRequestEmail: String,
    @SerializedName("hospitalName") val ServiceRequestHospitalName: String,
    @SerializedName("isVerified") val ServiceRequestIsVerified: Boolean,
    @SerializedName("message") val ServiceRequestMessage: String,
    @SerializedName("name") val ServiceRequestName: String,
    @SerializedName("otp") val ServiceRequestOtp: String,
    @SerializedName("serialNo") val ServiceRequestSerialNo: String,
    @SerializedName("currentPage") val ServiceRequestTicketStatus: String,
    @SerializedName("wardNo") val ServiceRequestWardNo: String,
    @SerializedName("ticketStatus") val ticketStatus : String,
    @SerializedName("serviceEngName") val serviceEngName : String
)