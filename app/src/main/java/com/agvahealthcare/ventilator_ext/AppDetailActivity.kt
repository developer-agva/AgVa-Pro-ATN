package com.agvahealthcare.ventilator_ext

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AppDetailActivity:AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_WRITE_SETTINGS = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_app_detail)


        // Request necessary permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)
            != PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS)
        } else {
            manageHotspot()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED) {
                manageHotspot()
            } else {
                Toast.makeText(this, "Permission required", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun manageHotspot() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        try {
            val wifiConfig = WifiConfiguration().apply {
                SSID = "\"MyHotspotSSID\""
                preSharedKey = "\"MyHotspotPassword\""
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            }

            // Turn off Wi-Fi
            wifiManager.isWifiEnabled = false

            // Use reflection to access hidden methods
            val method = wifiManager.javaClass.getMethod(
                "setWifiApEnabled", WifiConfiguration::class.java, Boolean::class.javaPrimitiveType
            )
            method.invoke(wifiManager, wifiConfig, true)

            Toast.makeText(this, "Hotspot is enabled", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to enable hotspot", Toast.LENGTH_SHORT).show()
        }
    }
}