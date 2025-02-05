package com.agvahealthcare.ventilator_ext.utility

import android.os.Build
import android.os.CountDownTimer
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class CustomCountDownTimer(var mutableLiveData: MutableLiveData<String>) {

    private lateinit var timer: CountDownTimer
    var count = 0L
    var day = 0
    fun start(endOn: Long) {
        if (this::timer.isInitialized) {
            return
        }
        timer = object : CountDownTimer(endOn * 1000 * 60 * 60, 1000) {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onTick(millisUntilFinished: Long) {
                val stringBuilder = StringBuilder()
                val minutesInMilli = 60
                val hoursInMilli = 60 * 60
                //val daysInMilli = 24 * 60* 60

               // val days = count / daysInMilli
                val hour = count / hoursInMilli
                val hMod = count % hoursInMilli
                val minutes = hMod / minutesInMilli
                val second = hMod % minutesInMilli

               /* val daysmod = days / 10

                val dayString = if (days<1){
                    "0$days"
                }
                else {
                  days.toString()
                }
                stringBuilder.append("$dayString:")*/

                val hourMod = hour / 10
                val  hourString = if (hourMod < 1) {
                    "0$hour"
                } else {
                    hour.toString()
                }
                stringBuilder.append("$hourString:")


                val minuteMode = minutes / 10
                val minuteString = if (minuteMode < 1) {
                    "0$minutes"
                } else {
                    minutes.toString()
                }

                stringBuilder.append("$minuteString:")

                val secondMode = second / 10
                val secondString = if (secondMode < 1) {
                    "0$second"
                } else {
                    second.toString()
                }

                stringBuilder.append(secondString)

                mutableLiveData.postValue(stringBuilder.toString())
                count++
                //Log.d("CustomCountDownTimer", stringBuilder.toString())
            }

            override fun onFinish() {

            }
        }

        timer.start()
    }


    fun stop() {
        timer.cancel()

    }

    fun getTimerState(): LiveData<String> {
        return mutableLiveData
    }
}


/*

                val stringBuilder = StringBuilder()

                val endDateTime: ZonedDateTime =
                    Instant.ofEpochMilli(millisUntilFinished).atZone(ZoneId.systemDefault())
                        .toLocalDateTime().atZone(zone)

                var diff: Duration = Duration.between(startDateTime, endDateTime)


                if (diff.isZero || diff.isNegative) {
                    stringBuilder.append("Already ended!")
                } else {

                    val hours: Long = diff.toHours()
                    stringBuilder.append("${hours}: ")
                    diff = diff.minusHours(hours)

                    val minutes: Long = diff.toMinutes()
                    stringBuilder.append("${minutes} : ")
                    diff = diff.minusMinutes(minutes)

                    val seconds: Long = diff.seconds

                    stringBuilder.append("${seconds}")

                }*/
