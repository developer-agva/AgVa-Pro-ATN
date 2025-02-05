package com.agvahealthcare.ventilator_ext

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.remoteConfig
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateHelper(
    private val context: Context,
    private val onUpdateCheckListener: OnUpdateCheckListener?,
    private val onUpdateBaseUrlListener: OnUpdateBaseUrlListener?
) {
    interface OnUpdateCheckListener {
        fun onUpdateCheckListener(urlApp: String)
    }

    interface OnUpdateBaseUrlListener {
        fun onUpdateBaseUrlListener(baseUrl: String)
    }

    companion object {
        var KEY_UPDATE_ENABLE = "isUpdate"
        var KEY_UPDATE_ID = "update_id"
        var KEY_UPDATE_VERSION = "version"
        var KEY_UPDATE_URL = "update_url"
        var IS_PAYMENT_DONE = "isPaymentDone"
        var IS_UPDATE_BASE_URL = "update_baseURL"
        var KEY_BASE_URL = "base_url"
    }

    fun checkForUpdates(context: Context) {

        val currentVersion = remoteConfig?.getString(KEY_UPDATE_VERSION)
        val updateId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val appVersion = getAppVersion(context)
        val updateURL = remoteConfig?.getString(KEY_UPDATE_URL)
        val baseUrlFlag = remoteConfig?.getBoolean(IS_UPDATE_BASE_URL)
        val baseUrlString = remoteConfig?.getString(KEY_BASE_URL)
        val deviceId = remoteConfig?.getString(KEY_UPDATE_ID)
        if (remoteConfig?.getBoolean(KEY_UPDATE_ENABLE) == true) {
            if (!TextUtils.equals(currentVersion, appVersion) && TextUtils.equals(
                    updateId,
                    deviceId
                )
            ) {
                onUpdateCheckListener?.onUpdateCheckListener(updateURL!!)
            }
            Log.i(
                "CHECK_UPDATE_OTA",
                "baseurl flag ->$baseUrlFlag ,baseurlString -> $baseUrlString , device Id -> $deviceId , current device Id -> $updateId , version-> $appVersion, newversion -> $currentVersion updated apk"
            )
        }

        else if ((deviceId == "" || TextUtils.equals(deviceId, updateId)) && baseUrlFlag == true
        ) onUpdateBaseUrlListener?.onUpdateBaseUrlListener(baseUrlString!!)

        Log.i(
            "CHECK_UPDATE_OTA",
            "baseurl flag ->$baseUrlFlag ,baseurlString -> $baseUrlString , device Id -> $deviceId , current device Id -> $updateId"
        )
    }

    private fun getAppVersion(context: Context): String {
        var result = ""
        try {
            result = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            result = result.replace("[a-zA-Z] |-".toRegex(), "")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return result
    }
}