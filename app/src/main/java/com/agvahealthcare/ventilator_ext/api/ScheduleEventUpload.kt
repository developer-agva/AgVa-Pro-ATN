package com.agvahealthcare.ventilator_ext.api

import android.content.Context
import android.util.Log
import androidx.work.*
import com.agvahealthcare.ventilator_ext.BuildConfig
import com.agvahealthcare.ventilator_ext.api.LoggerApiService
import com.agvahealthcare.ventilator_ext.api.model.eventDataModel.EventRequestBodyModel
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.utility.BASE_URL
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ScheduleEventUpload(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {

    override fun doWork(): Result {
        Log.d("responseEventPass", "7")
        val data = inputData.getString("event_request")
        val requestEventRequestBodyModel = Gson().fromJson(data, EventRequestBodyModel::class.java)
        return eventNetworkUpload(requestEventRequestBodyModel)
    }

    private fun eventNetworkUpload(eventRequestBodyModel: EventRequestBodyModel): Result {

        try {
            Log.d("responseEventPass", "8")
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
            val responseEvent = response.updateServerWithEvents(eventRequestBodyModel).execute()

            Log.d("responseEventPassWorkManager", responseEvent.message())
            responseEvent.body()?.let {
                if (it.status == 201 || it.statusCodeNew == 201) Result.success() else Result.failure()
            }
        } catch (e: Exception) {
            Log.d("responseEventPass", "9")
            e.printStackTrace()
        }

        return Result.failure()
    }

}