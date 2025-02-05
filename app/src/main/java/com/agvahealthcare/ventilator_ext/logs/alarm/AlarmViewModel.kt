package com.agvahealthcare.ventilator_ext.logs.alarm

import android.app.Application
import androidx.lifecycle.*
import com.agvahealthcare.ventilator_ext.database.entities.AlarmDBModel
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    fun addAlarm(alarmDBModel: AlarmDBModel) {
        CoroutineScope(Dispatchers.IO).launch {
            val alarmDataString = "${alarmDBModel.key},${alarmDBModel.message},${alarmDBModel.createdAt},${alarmDBModel.uhid}|"
            FileLogger.writeAlarmFile(getApplication(), "alarm", alarmDataString)
        }
    }

}