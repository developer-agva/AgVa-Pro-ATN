package com.agvahealthcare.ventilator_ext.system.diagnosticCheck

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class DiagnosticCheckViewModel(application:Application) :AndroidViewModel(application) {

    //  first line
    val inspPressureRawData = MutableLiveData<String?>()
    val expPressureRawData = MutableLiveData<String?>()
    val oxyPressureRawData = MutableLiveData<String?>()
    val inspPressureData = MutableLiveData<String?>()
    val expPressureData = MutableLiveData<String?>()
    val oxyPressureData = MutableLiveData<String?>()
    val inspFlowVoltageData = MutableLiveData<String?>()
    val inspFlowData = MutableLiveData<String?>()
    val expDPRawData = MutableLiveData<String?>()
    val expFlowData = MutableLiveData<String?>()

    //  mid line
    val batteryCurrentData = MutableLiveData<String?>()
    val batteryVoltageData = MutableLiveData<String?>()
    val batterySOCData = MutableLiveData<String?>()
    val batteryRemainingTimeData = MutableLiveData<String?>()
    val batteryStateData = MutableLiveData<String?>()
    val powerConnectionData = MutableLiveData<String?>()
    val mainSwitchData = MutableLiveData<String?>()
    val spo2StatusData = MutableLiveData<String?>()
    val spo2Data = MutableLiveData<String?>()
    val hrData = MutableLiveData<String?>()

    //  last line
    val piTempData = MutableLiveData<String?>()
    val screenCPUTempData = MutableLiveData<String?>()
    val softwareVersionData = MutableLiveData<String?>()
    val knobPcbTypeData = MutableLiveData<String?>()
    val knobPcbVersionData = MutableLiveData<String?>()
    val piCpuLoadData = MutableLiveData<String?>()
    val o2SensorVoltageData = MutableLiveData<String?>()
    val hardwareVersionData = MutableLiveData<String?>()


}