package com.agvahealthcare.ventilator_ext.system.o2Regulation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class O2RegulationCheckViewModel(application:Application) :AndroidViewModel(application) {

    val o2PressureData = MutableLiveData<String>()
}