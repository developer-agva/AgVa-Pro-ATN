package com.agvahealthcare.ventilator_ext.system.selftest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class SelfTestViewModel(application: Application) : AndroidViewModel(application) {

    var selfTestData = MutableLiveData<String>()
}