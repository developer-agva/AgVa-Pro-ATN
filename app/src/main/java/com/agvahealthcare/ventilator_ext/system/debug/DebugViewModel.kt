package com.agvahealthcare.ventilator_ext.system.debug

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class DebugViewModel(application: Application) : AndroidViewModel(application) {


    var ventiLiveData = MutableLiveData<MutableList<String>>()
    var hidLiveData = MutableLiveData<MutableList<String>>()
    var ackOccurenceLiveData = MutableLiveData<String>()

}