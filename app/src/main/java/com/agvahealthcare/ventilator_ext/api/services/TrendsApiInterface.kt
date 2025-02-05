package com.agvahealthcare.ventilator_ext.api.services

import com.agvahealthcare.ventilator_ext.api.model.LogResponseModel
import com.agvahealthcare.ventilator_ext.api.model.TrendsDataModel.TrendsRequestBodyModel
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface TrendsApiInterface {

    @Headers("Content-Type: application/json")
    @POST("api/logger/logs/trends/v2/007")
    fun sendTrendsApi(@Body request: TrendsRequestBodyModel): Call<LogResponseModel>
}
