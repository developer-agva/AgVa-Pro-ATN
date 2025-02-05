package com.agvahealthcare.ventilator_ext.system.services

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agvahealthcare.ventilator_ext.api.ServerLogger
import com.agvahealthcare.ventilator_ext.api.model.serviceDataModel.Data
import com.agvahealthcare.ventilator_ext.database.entities.ServiceDataModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServiceViewModel(application: Application) : AndroidViewModel(application) {

    val serviceData = MutableLiveData<ArrayList<String>>()
    val serviceDataTemp = MutableLiveData<ArrayList<Data>>()


    @SuppressLint("HardwareIds")
    fun addService(ctx: Context, serviceDataModel: ServiceDataModel) {

    }

    fun  readAllService(deviceId :String){
        viewModelScope.launch(Dispatchers.IO) {
            val response = ServerLogger.getServiceRequests(deviceId)
            response?.let {
                if (it.ServiceRequestStatusCode == 200) serviceDataTemp.postValue(it.ServiceRequestData)
            }?: kotlin.run {
                serviceDataTemp.postValue(null)
            }
        }
    }

}
