package com.agvahealthcare.ventilator_ext.api

import androidx.lifecycle.LiveData
import com.agvahealthcare.ventilator_ext.api.model.CrashRequestBody
import com.agvahealthcare.ventilator_ext.api.model.DeviceIdResponseModel
import com.agvahealthcare.ventilator_ext.api.model.LogResponseModel
import com.agvahealthcare.ventilator_ext.api.model.OTAResponse
import com.agvahealthcare.ventilator_ext.api.model.VentiConfigsResponse
import com.agvahealthcare.ventilator_ext.api.model.alarmDataModel.AlarmRequestBodyModel
import com.agvahealthcare.ventilator_ext.api.model.calibrationDataModel.CalibrationRequestModel

import com.agvahealthcare.ventilator_ext.api.model.datamodel.RequestBodyModel
import com.agvahealthcare.ventilator_ext.api.model.eventDataModel.EventRequestBodyModel
import com.agvahealthcare.ventilator_ext.api.model.locationDataModel.LocationRequestModel
import com.agvahealthcare.ventilator_ext.api.model.maneuversDataModel.ManeuversRequestBodyModel
import com.agvahealthcare.ventilator_ext.api.model.serviceDataModel.*
import com.agvahealthcare.ventilator_ext.api.model.statusDataModel.PatientDetailsRequestModel
import com.agvahealthcare.ventilator_ext.api.model.statusDataModel.StatusRequestModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface LoggerApiService {

    //interface for the retrofit to make a crash update on the server
    @Headers( "Content-Type: application/json" )
    @POST("api/logger/logs/v2/008")
    fun updateServerWithCrash(@Body requestBodyModel: CrashRequestBody):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/v2/alerts-new/008")
    fun updateServerWithAlarms(@Body alarmRequestBodyModel: AlarmRequestBodyModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/v2/events/008")
    fun updateServerWithEvents(@Body eventRequestBodyModel: EventRequestBodyModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/location/v2/008")
    fun updateServerWithLocation(@Body locationRequestModel: LocationRequestModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/calibration/v2/008")
    fun updateServerWithCalibration(@Body calibationRequestModel: CalibrationRequestModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/services/v2/008")
    fun updateServerWithService(@Body serviceRequestModel: ServiceRequestModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/v2/status/008")
    fun updateServerWithStatus(@Body statusRequestModel: StatusRequestModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("patient/save-uhid-details")
    fun updateServerWithPatientDetails(@Body patientDetailsRequestModel: PatientDetailsRequestModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/services/verify-sms-otp/SBXMH")
    fun updateServerWithOtpVerify(@Body serviceOtpVerifyModel: ServiceOtpVerifyModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/services/verify-otp-for-ticket-close/SBXMH")
    fun updateServerWithOtpVerifyForTicketClose(@Body serviceOtpVerifyModel: ServiceOtpVerifyModel):Call
    <LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/services/ticket-status/SBXMH")
    fun sendServiceCloseRequest(@Body serviceCloseRequestModel: ServiceCloseRequestModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @GET("api/logger/logs/services/get-by-deviceId")
    fun getServiceRequests(@Query("deviceId") deviceId: String, @Query("project_code") project_code: String = "SBXMH"):Call<ResponseModel>

    @Headers("Content-Type: application/json")
    @GET("devices/getdevice/{deviceId}")
    fun getVentiDetailsRequest(@Path("deviceId") deviceId: String):Call<VentiDetailsResponseModel>

    @Headers("Content-Type: application/json")
    @GET("api/s3/get-app-list/008")
    fun getAppHistory():Call<OTAResponse>

    @Headers("Content-Type: application/json")
    @GET("api/common/get-ventilator_conf_list/008")
    fun getVentiConfigs():Call<VentiConfigsResponse>

    @Headers("Content-Type: application/json")
    @PUT("devices/payment-update")
    fun sendPaymentStatus(@Body paymentStatusRequestModel: PaymentStatusRequestModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/debug-events/SBXMH")
    fun updateServerWithEventsForDevelopers(@Body eventRequestBodyModel: EventRequestBodyModel):Call<LogResponseModel>

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/manuevers/v2/008")
    fun updateServerWithManeuvers(@Body maneuversRequestBodyModel: ManeuversRequestBodyModel):Call<LogResponseModel>

}