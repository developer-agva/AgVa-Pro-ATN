package com.agvahealthcare.ventilator_ext.system

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.agvahealthcare.ventilator_ext.MainActivityViewModel

class InternetConnection(private var appContext : Context,private val mainActivityViewModel: MainActivityViewModel) : LiveData<Boolean>(){

    private var connectivityManager : ConnectivityManager? = null

    init {
        connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        Log.i("value_check_connectivity","$connectivityManager , connecting")
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback(){

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d(ContentValues.TAG, "onAvailable: Network $network is Available")

            Log.i("value_check_connectivity","$mainActivityViewModel , available")
            mainActivityViewModel.connectivityStatus.postValue(true)
            postValue(true)
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            val isInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            Log.d(ContentValues.TAG, "networkCapabilities: $network $networkCapabilities")
            val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (isValidated){
                Log.d(ContentValues.TAG, "hasCapability: $network $networkCapabilities")
            } else{
                Log.d(ContentValues.TAG, "Network has No Connection Capability: $network $networkCapabilities")
            }
            mainActivityViewModel.connectivityStatus.postValue(isInternet && isValidated)
            postValue(isInternet && isValidated)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d(ContentValues.TAG, "onLost: $network Network Lost")

            Log.i("value_check_connectivity","$mainActivityViewModel , lost")
            mainActivityViewModel.connectivityStatus.postValue(false)
            postValue(false)

        }
    }

    override fun onActive() {
        super.onActive()

        val builder = NetworkRequest.Builder()
        connectivityManager?.registerNetworkCallback(builder
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build(), networkCallback)

        Log.i("value_check_connectivity","$builder , active")
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager?.unregisterNetworkCallback(networkCallback)
        Log.i("value_check_connectivity","inactive")
    }
}