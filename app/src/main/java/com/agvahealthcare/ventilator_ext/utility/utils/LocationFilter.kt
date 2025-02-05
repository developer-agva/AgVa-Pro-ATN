package com.agvahealthcare.ventilator_ext.utility.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.provider.Settings
import android.util.Log
import com.agvahealthcare.ventilator_ext.api.model.locationDataModel.LocationRequestModel
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class LocationFilter(val context: Context) {

    @SuppressLint("HardwareIds")
    fun getFullAddress(lat: Double, lng: Double): LocationRequestModel {
        val geocoder = Geocoder(context, Locale.getDefault())
        val result = LocationRequestModel()
        try {
            val data = geocoder.getFromLocation(lat, lng, 1)
            val add = data?.get(0)?.getAddressLine(0)?.toString().toString()

            result.apply {
                data?.let {
                    did = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                    country = it[0]?.countryName.toString()
                    pincode = it[0]?.postalCode.toString()
                    city = it[0]?.locality.toString()
                    state = it[0]?.adminArea.toString()
                    street = it[0]?.thoroughfare.toString()
                }
            }

            Log.i("valueasdwd",add)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (result)
    }

    @SuppressLint("HardwareIds")
    fun getAddress(lat: Double, lng: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        var result ="- - -"
        Log.i("valueasdadwad", "$lat $lng")
        try {
            val data = geocoder.getFromLocation(lat, lng, 1)
            result = data?.get(0)?.getAddressLine(0)?.toString().toString()

            Log.i("valueasdadwad",result.toString())
        } catch (e: Exception) {
            Log.i("valueasdadwad",e.message.toString())
            e.printStackTrace()
        }
        return (result)
    }
}