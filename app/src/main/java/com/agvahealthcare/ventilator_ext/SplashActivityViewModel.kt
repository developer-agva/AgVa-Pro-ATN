
package com.agvahealthcare.ventilator_ext

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class SplashActivityViewModel(application: Application) : AndroidViewModel(application) {

    val batterySystemFailure = MutableLiveData<Boolean>()

}