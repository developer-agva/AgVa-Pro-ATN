package com.agvahealthcare.ventilator_ext.connection.internetConnection

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkConnectivityObserver(
    private val context: Context
) : ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): MutableLiveData<Boolean> {
        val state = MutableLiveData(false)
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("EthernetConnection", "NetworkCapabilities.TRANSPORT_ETHERNET")
                state.postValue(true)
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("EthernetConnection", "NetworkCapabilities.TRANSPORT_WIFI")
                state.postValue(false)
            }
        } else {
            Log.i("EthernetConnection", "Not connected ethernet")
            state.postValue(false)
        }
        return state
    }

}