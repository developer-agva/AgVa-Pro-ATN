package com.agvahealthcare.ventilator_ext.connection.internetConnection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    fun observe(): MutableLiveData<Boolean>
}