package com.agvahealthcare.ventilator_ext.connection.internetConnection

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.LiveData

class NetworkConnectivityObserver(
    context: Context
) : LiveData<ConnectivityObserver.Status>(), ConnectivityObserver {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateStatus()
        }

        override fun onLost(network: Network) {
            updateStatus()
        }
    }

    override fun onActive() {
        super.onActive()
        connectivityManager.registerDefaultNetworkCallback(callback)
        updateStatus()
    }

    override fun onInactive() {
        super.onInactive()
        connectivityManager.unregisterNetworkCallback(callback)
    }

    override fun observe(): LiveData<ConnectivityObserver.Status> = this

    private fun updateStatus() {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        val status = when {
            capabilities == null -> {
                Log.i("Connectivity", "No active network")
                ConnectivityObserver.Status.Unavailable
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                Log.i("Connectivity", "Connected via Ethernet")
                ConnectivityObserver.Status.Available
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                Log.i("Connectivity", "Connected via WiFi")
                ConnectivityObserver.Status.Available
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                Log.i("Connectivity", "Connected via Mobile Data")
                ConnectivityObserver.Status.Available
            }
            else -> {
                Log.i("Connectivity", "Other/Unknown network")
                ConnectivityObserver.Status.Unavailable
            }
        }

        postValue(status)
    }
}
