
package com.agvahealthcare.ventilator_ext

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    val ventBatteryLevel = MutableLiveData<Int>()
    fun setVentBatteryLevel(batteryLevel: Int) {
        ventBatteryLevel.value = batteryLevel
    }

    val connectivityStatus = MutableLiveData<Boolean>()

    //Operational hours in hours and minutes.
    val OPHours= MutableLiveData<String>()
    //Service hours in hours and minutes.
    val serviceHours = MutableLiveData<String>()


    val twoTileResponse = MutableLiveData<Int>()

    val batterySystemFailure = MutableLiveData<Boolean>()

    val ventBatteryHealth = MutableLiveData<Int>()
    fun setVentBatteryHealth(ventBattHealth: Int) {
        ventBatteryHealth.value = ventBattHealth
    }
    val isVentilationInitiatedFromExisting = MutableLiveData<Boolean>()
    val ventBatteryRemainingTime = MutableLiveData<Int>()
    fun setVentBatteryRemainingTime(ventBR: Int) {
        ventBatteryRemainingTime.value = ventBR
    }

    val isBatteryConnected=MutableLiveData<Boolean>()
    fun setBAtteryConnectedFlag(isConnected:Boolean){
        isBatteryConnected.value=isConnected
    }

    val isNeoNatalSensorConnected = MutableLiveData<Boolean>()
    fun setNeoNatalSensorConnectedFlag(isConnect:Boolean){
        isNeoNatalSensorConnected.value = isConnect
    }
    val calibrationerrorpressure=MutableLiveData<String>()
    val calibrationerrorflow=MutableLiveData<String>()
    val calibrationerrordutycycle=MutableLiveData<String>()
    val isConnected = MutableLiveData<Boolean>()
}