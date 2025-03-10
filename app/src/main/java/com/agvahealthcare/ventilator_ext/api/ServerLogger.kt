package com.agvahealthcare.ventilator_ext.api

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.provider.Settings.Secure
import android.util.Log
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.work.*
import androidx.work.Data
import com.agvahealthcare.ventilator_ext.api.model.CrashRequestBody
import com.agvahealthcare.ventilator_ext.api.model.DataX
import com.agvahealthcare.ventilator_ext.api.model.LogResponseModel
import com.agvahealthcare.ventilator_ext.api.model.Sensors
import com.agvahealthcare.ventilator_ext.api.model.TrendsDataModel.TrendsRequestBodyModel
import com.agvahealthcare.ventilator_ext.api.model.alarmDataModel.AlarmRequestBodyModel
import com.agvahealthcare.ventilator_ext.api.model.calibrationDataModel.CalibrationRequestModel
import com.agvahealthcare.ventilator_ext.api.model.eventDataModel.EventRequestBodyModel
import com.agvahealthcare.ventilator_ext.api.model.locationDataModel.LocationRequestModel
import com.agvahealthcare.ventilator_ext.api.model.maneuversDataModel.ManeuversRequestBodyModel
import com.agvahealthcare.ventilator_ext.api.model.serviceDataModel.*
import com.agvahealthcare.ventilator_ext.api.model.statusDataModel.PatientDetailsRequestModel
import com.agvahealthcare.ventilator_ext.api.model.statusDataModel.StatusRequestModel
import com.agvahealthcare.ventilator_ext.api.services.*
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.model.AlarmModel
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.internal.wait
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ServerLogger {

    companion object {

        // done
        fun sendPatientDetailsRequest(patientDetailsRequestModel: PatientDetailsRequestModel): Boolean {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            val response = retrofit.create(LoggerApiService::class.java)
            try {
                val responseEvent =
                    response.updateServerWithPatientDetails(patientDetailsRequestModel).execute()
                // this log will represent the successful Hit to the server

                Log.d("responsePatientDetailsPass", responseEvent.message())
                responseEvent.body()?.let {
                    return it.status == 201 || it.statusCodeNew == 201
                } ?: kotlin.run {
                    return false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }

        fun sendAlarm(ctx: Context, ackList: ArrayList<AlarmModel>): Boolean {

            return try {
                val list = ackList
                val alarmRequestBodyModel = AlarmRequestBodyModel()
                alarmRequestBodyModel.did = Secure.getString(ctx.contentResolver, Secure.ANDROID_ID)
                alarmRequestBodyModel.type = "008"
                alarmRequestBodyModel.ack = ArrayList(list.map { it.toAckModel() })

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(FileLogger.readBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()

                val response = retrofit.create(LoggerApiService::class.java)
                val responseEvent = response.updateServerWithAlarms(alarmRequestBodyModel).execute()
                responseEvent.body()?.let {
                    it.status == 201 || it.statusCodeNew == 201
                } ?: kotlin.run {
                    false
                }
            } catch (e: Exception) {
                Log.d("responseAlarmPass", e.toString())
                e.printStackTrace()
                false
            }
        }

//        private fun runEventWorkManager(context:Context,eventRequestBodyModel: EventRequestBodyModel) {
//            Log.d("responseEventPass", "3")
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.UNMETERED)
//                .build()
//
//            val dataBody = Gson().toJson(eventRequestBodyModel)
//            val data = Data.Builder()
//                .putString("event_request", dataBody)
//                .build()
//
//            val myWorkRequest = OneTimeWorkRequestBuilder<ScheduleEventUpload>()
//                .setConstraints(constraints)
//                .setInitialDelay(10, TimeUnit.SECONDS)
//                .setInputData(data)
//                .build()
//
//            WorkManager.getInstance(context).enqueue(myWorkRequest)
//        }

        // done
        @SuppressLint("HardwareIds")
        fun sendEventForDevelopers(ctx: Context, message: String): Boolean {
            val eventRequestBodyModel = EventRequestBodyModel()
            eventRequestBodyModel.did = Secure.getString(ctx.contentResolver, Secure.ANDROID_ID)
            eventRequestBodyModel.type = "008"
            eventRequestBodyModel.message = message
            eventRequestBodyModel.date = AppUtils.getCurrentDateReverse()
            Log.i("event_api_developer", "api called $message")
            return sendEventRequestForDevelopers(eventRequestBodyModel)
        }

        // done
        fun d(ctx: Context, err: Throwable, filename: String) =
            d(ctx, err.stackTraceToString(), filename)

        // done
        @SuppressLint("HardwareIds")
        fun d(ctx: Context, data: String, filename: String): Boolean {
            val requestBodyModel = CrashRequestBody()
            requestBodyModel.apply {
                did = Secure.getString(ctx.contentResolver, Secure.ANDROID_ID)
                msg = data
                file = filename
                date = AppUtils.getCurrentDateReverse()
            }
            return apiRequest(ctx, requestBodyModel)
        }

        private fun apiRequest(ctx: Context, logRequestBodyModel: CrashRequestBody): Boolean {
            try {
                Log.i("SERVER_CHECK", "Request initiated")
                return sendCrashRequest(logRequestBodyModel)
            } catch (e: Exception) {
                FileLogger.d(ctx, e)
            }
            return false
        }

        // done
        fun sendEvent(
            ctx: Context,
            message: String
        ) {

            val eventRequestBodyModel = EventRequestBodyModel()
            eventRequestBodyModel.did = Secure.getString(ctx.contentResolver, Secure.ANDROID_ID)
            eventRequestBodyModel.type = "008"
            eventRequestBodyModel.message = message
            eventRequestBodyModel.date = AppUtils.getCurrentDateReverse()

            try {

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(FileLogger.readBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()

                val response = retrofit.create(LoggerApiService::class.java)
                response.updateServerWithEvents(eventRequestBodyModel).execute()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // done
        private fun sendEventRequestForDevelopers(eventRequestBodyModel: EventRequestBodyModel): Boolean {
            try {

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(FileLogger.readBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()
                Log.i("event_api_developer", "api on way $eventRequestBodyModel")
                val response = retrofit.create(LoggerApiService::class.java)
                val responseEvent =
                    response.updateServerWithEventsForDevelopers(eventRequestBodyModel).execute()
                // this log will represent the successful Hit to the server
                Log.i("event_api_developer", "api do something $responseEvent")
                responseEvent.body()?.let {
                    return it.status == 201 || it.statusCodeNew == 201
                } ?: kotlin.run {
                    return false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("event_api_developer", "api do something ${e.message}")
                return false
            }
        }

        // done
        fun sendLocationRequest(locationRequestModel: LocationRequestModel): Boolean {
            try {

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(FileLogger.readBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()

                val response = retrofit.create(LoggerApiService::class.java)
                val responseEvent =
                    response.updateServerWithLocation(locationRequestModel).execute()

                // this log will represent the successful Hit to the server

                Log.d("responseLocationPass", responseEvent.message())
                responseEvent.body()?.let {
                    return it.status == 201 || it.statusCodeNew == 201
                } ?: kotlin.run {
                    return false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }

        //done
        fun sendCalibrationRequest(calibrationRequestModel: CalibrationRequestModel): Boolean {
            try {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(FileLogger.readBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()

                val response = retrofit.create(LoggerApiService::class.java)

                val responseEvent =
                    response.updateServerWithCalibration(calibrationRequestModel).execute()
                // this log will represent the successful Hit to the server
                Log.d("responseCalibrationPass", responseEvent.toString())
                responseEvent.body()?.let {
                    Log.d("responseCalibrationPass", "pass")
                    return it.status == 201 || it.statusCodeNew == 201
                } ?: kotlin.run {
                    Log.d("responseCalibrationPass", "pass 1")
                    return false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.d("responseCalibrationPass", "fail")
                Log.d("responseCalibrationPass", e.toString())
                return false
            }
        }


        fun sendTrendsRequest(ctx: Context, trendsRequestBodyModel: TrendsRequestBodyModel) {
            try {

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(FileLogger.readBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()

                val response = retrofit.create(TrendsApiInterface::class.java)
                response.sendTrendsApi(trendsRequestBodyModel).execute()

                Log.d("responseTrendPass", trendsRequestBodyModel.toString())

//                if (responseEvent.code() == 201) {
//                    sendTrendsRequestFromFile(ctx)
//                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun sendPaymentStatus(paymentStatusRequestModel: PaymentStatusRequestModel): Response<LogResponseModel>? {

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            val response = retrofit.create(LoggerApiService::class.java)
            return try {
                var response = response.sendPaymentStatus(paymentStatusRequestModel).execute()
                Log.i("CHECK_RESPONSE", response.toString())
                response

            } catch (e: Exception) {
                Log.i("dataSuccessfully", e.message.toString())
                null
            }
        }

        fun sendServiceRequest(serviceRequestModel: ServiceRequestModel): Response<LogResponseModel>? {

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            val response = retrofit.create(LoggerApiService::class.java)
            return try {
                val response = response.updateServerWithService(serviceRequestModel).execute()
                Log.i("value_Repsinse", response.toString())

                response
            } catch (e: Exception) {
                Log.i("dataSuccessfully", e.message.toString())
                null
            }
        }

        fun sendManeuversRequest(maneuversRequestBodyModel: ManeuversRequestBodyModel): Boolean {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            val response = retrofit.create(LoggerApiService::class.java)
            return try {
                val responseManuevers =
                    response.updateServerWithManeuvers(maneuversRequestBodyModel).execute()
                responseManuevers.body()?.let {
                    return it.status == 201 || it.statusCodeNew == 201
                } ?: kotlin.run {
                    return false
                }
            } catch (e: Exception) {
                Log.i("dataSuccessfully", e.message.toString())
                false
            }
        }

        fun getVentiDetailsRequest(deviceId: String): VentiDetailsResponseModel? {

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()


            return try {
                val response =
                    retrofit.create(LoggerApiService::class.java).getVentiDetailsRequest(deviceId)
                        .execute()
                Log.i("asds", response.toString())

                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.i("valiueRead", e.toString())
                e.printStackTrace()
                null
            }

        }

        fun getVentiConfigs(): Sensors? {

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            return try {
                val response =
                    retrofit.create(LoggerApiService::class.java).getVentiConfigs().execute()
                Log.i("venti_configs_check", response.toString())

                response.body()?.let {
                    if (it.statusCode == 200) return it.data.sensors
                }
                return null

            } catch (e: Exception) {
                Log.i("venti_configs_check", e.toString())
                e.printStackTrace()
                null
            }

        }

        fun getAppHistory(): ArrayList<DataX>? {

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            return try {
                val response =
                    retrofit.create(LoggerApiService::class.java).getAppHistory().execute()
                Log.i("app_history_check", response.toString())

                response.body()?.let {
                    if (it.statusCode == 200) return it.data
                }
                return null

            } catch (e: Exception) {
                Log.i("app_history_check", e.toString())
                e.printStackTrace()
                null
            }

        }

        fun getServiceRequests(deviceId: String): ResponseModel? {

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()


            return try {
                val response =
                    retrofit.create(LoggerApiService::class.java).getServiceRequests(deviceId)
                        .execute()
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.i("valiueRead", e.message.toString())
                e.printStackTrace()
                null
            }
        }

        fun sendOtpVerifyForTicketClose(serviceOtpVerifyModel: ServiceOtpVerifyModel): Boolean {

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            val response = retrofit.create(LoggerApiService::class.java)
            return try {
                val response =
                    response.updateServerWithOtpVerifyForTicketClose(serviceOtpVerifyModel)
                        .execute()
                Log.i("dataSuccessfully", "Successfully")
                response.isSuccessful
            } catch (e: Exception) {
                Log.i("dataSuccessfully", e.message.toString())
                false
            }
        }

        // done
        fun sendStatusRequest(statusRequestModel: StatusRequestModel): Boolean {

            try {
                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(FileLogger.readBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()

                val response = retrofit.create(LoggerApiService::class.java)
                val responseEvent = response.updateServerWithStatus(statusRequestModel).execute()
                // this log will represent the successful Hit to the server

                responseEvent.body()?.let {
                    return it.status == 201 || it.statusCodeNew == 201
                } ?: kotlin.run {
                    return false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }

        fun sendServiceCloseRequest(serviceCloseRequestModel: ServiceCloseRequestModel): Boolean {

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            val response = retrofit.create(LoggerApiService::class.java)
            return try {
                val response = response.sendServiceCloseRequest(serviceCloseRequestModel).execute()
                Log.i("dataSuccessfully", "Successfully with $serviceCloseRequestModel")
                response.isSuccessful
            } catch (e: Exception) {
                Log.i("dataSuccessfully", e.message.toString())
                false
            }
        }

        fun sendOtpVerifyRequest(serviceOtpVerifyModel: ServiceOtpVerifyModel): Boolean {

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(FileLogger.readBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()

            val response = retrofit.create(LoggerApiService::class.java)
            return try {
                val response = response.updateServerWithOtpVerify(serviceOtpVerifyModel).execute()
                Log.i("dataSuccessfully", "Successfully")
                response.isSuccessful
            } catch (e: Exception) {
                Log.i("dataSuccessfully", e.message.toString())
                false
            }
        }


        // done
        private fun sendCrashRequest(requestBodyModel: CrashRequestBody): Boolean {
            try {

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(FileLogger.readBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build()

                val response = retrofit.create(LoggerApiService::class.java)
                val responseEvent = response.updateServerWithCrash(requestBodyModel).execute()
                // this log will represent the successful Hit to the server

                responseEvent.body()?.let {
                    Log.i("valueCHeckError", it.toString())
                    return it.status == 201 || it.statusCodeNew == 201
                } ?: kotlin.run {
                    return false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
    }
}