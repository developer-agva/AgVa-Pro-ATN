package com.agvahealthcare.ventilator_ext.logs.event

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agvahealthcare.ventilator_ext.api.ServerLogger
import com.agvahealthcare.ventilator_ext.database.entities.EventDataModel
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//This class is for the data to be sent to the server for the events from the ventilator.
class EventViewModel(application: Application) : AndroidViewModel(application) {

    fun addEvent(eventDataModel: EventDataModel) {

        CoroutineScope(Dispatchers.IO).launch {
            val eventDataString = "${eventDataModel.event},${eventDataModel.timeStamp},${eventDataModel.uhid}|"
            FileLogger.writeEventFile(getApplication(),"event", eventDataString)
            ServerLogger.sendEvent(getApplication(), eventDataModel.event)
        }
    }

    fun addEventForDevelopers(eventDataModel: EventDataModel) {

        CoroutineScope(Dispatchers.IO).launch {
            val eventDataString = "${eventDataModel.event},${eventDataModel.timeStamp},${eventDataModel.uhid}|"
            FileLogger.writeEventFileDevelopers(getApplication(),"eventForDevelopers", eventDataString)
            if (!ServerLogger.sendEventForDevelopers(getApplication(), eventDataModel.event)) ServerLogger.sendEventForDevelopers(getApplication(), eventDataModel.event)
        }
    }

}
