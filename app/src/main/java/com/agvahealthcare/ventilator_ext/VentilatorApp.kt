package com.agvahealthcare.ventilator_ext

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import com.agvahealthcare.ventilator_ext.connection.internetConnection.ConnectivityObserver
import com.agvahealthcare.ventilator_ext.connection.internetConnection.NetworkConnectivityObserver
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.exceptions.AppLevelExceptionHandler
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service_record.DeviceRecordManager
import com.agvahealthcare.ventilator_ext.utility.FIFOCAPACITY_CUSTOM_SIZE
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.ModeType
import com.github.anrwatchdog.ANRWatchDog
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.scichart.charting.visuals.SciChartSurface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


class VentilatorApp : Application() {

    companion object {

        // At the top level of your kotlin file:
        val android.content.Context.ventiPref: DataStore<Preferences> by preferencesDataStore(
            name = "Ventilator_Pro"
        )
        var choosedHours = "1 hour"
        var defaultOfTurbineRanges = "0"
        var defaultOfExhaleValveRanges = "0"
        var defaultOfOxygenValveRanges = "0"
        var defaultOfNeoRanges = "0"
        var isPatientDisconnected = false

        var currentActivityName = "Splash"
        var currentDownloadProgress = 0
        var downloadId = 0L
        var ack756Visibility = ""
        var ventiData = ""
        var hidData = ""
        var currentBreathCycle = ""
        var isLiveDataRequest = false
        var isLiveGraphDataRequest = false
        var isTrendsFirstTime = false
        var apneaActive:Boolean = false
        var isEtCo2Available:Boolean = false
        var dischargeDate:String? = null
        var isPatientDischarged:Boolean = false;

        var isSocketConnected:Boolean = false
        var flowPeakValuePositive : Float? = null
        var flowPeakValueNegative : Float? = null

        var hardwareSerialNumber = "---"
        // testing graph coloring
        var isPatientTrigger = false
        var isNebuliserActive = true
        var isManualBreath = false
        var selectedOptions :Configs.SELECTED_OPTIONS? = null
        var xValuePatientTriggerList = ArrayList<Double>()
        var xValueManualTriggerList = ArrayList<Double>()

        var isColoringStarted = false
        var isColoringended = false

        // RM scichart
        var xTestingPressure = IntArray(FIFOCAPACITY_CUSTOM_SIZE){i->0}
        var xTestingVolume = IntArray(FIFOCAPACITY_CUSTOM_SIZE){i->0}
        var xTestingFlow = IntArray(FIFOCAPACITY_CUSTOM_SIZE){i->0}
        var xTestingEtCo2 = IntArray(FIFOCAPACITY_CUSTOM_SIZE){i->0}

        var startupCheckDialogFrag : Boolean = false
        var socketId : String? = null
        var latitude = 0.0
        var logitude = 0.0
        var IERatio = ""
        var globalCount = 0
        var xMaxRangeGlobal : Double = 12.9

        var uhidDataListAlarm = ArrayList<String>()
        var uhidDataListEvent = ArrayList<String>()
        var testingConditonMap = HashMap<String,Float?>()
        var isFromControlFragment: Boolean? = null
        var fio2ChangeFlag : Boolean? = null
        var isShutDown : Boolean? = null
        var inDashboard:Boolean? = null

        var isExistingVentilationAvailable :Boolean = false
        var globalModeType : ModeType? = null
        var isTouchGraph : Boolean? = null
        var currentXValue = 0.0f
        var currentYValue = 0.0f
        var testingDashBoardViewModel : DashBoardViewModel? = null
        private var CHANNEL_ID = "ventilatorApp"
        private var sInstance: VentilatorApp? = null
        private var appVersion = "N/A"
        private var settingsContentObserver:SettingsContentObserver?= null

        var connectivityObserver : LiveData<ConnectivityObserver.Status>? = null
        var remoteConfig : FirebaseRemoteConfig? = null
        fun getInstance(): VentilatorApp? {
            return sInstance
        }

        fun getConnectivity(context:Context){
            connectivityObserver = NetworkConnectivityObserver(context)
        }

    }

    override fun onCreate() {
        super.onCreate()
        sInstance = this

//        createNotificationChannel()
        getConnectivity(applicationContext)
        ANRWatchDog().setANRListener { error -> // Handle the error. For example, log it to HockeyApp:

            CoroutineScope(Dispatchers.IO).launch {
                FileLogger.writeCrashFile(error.stackTraceToString())
            }
            when (currentActivityName) {

                "Splash" -> {
                    Intent(applicationContext, SplashActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        applicationContext.startActivity(it)
                    }
                }
                "Shutdown" -> {
                    Intent(applicationContext, ShutDownActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        applicationContext.startActivity(it)
                    }
                }

                "Main" -> {
                    Intent(applicationContext, MainActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        applicationContext.startActivity(it)
                    }
                }

                "Dash" -> {
                    Intent(applicationContext, DashBoardActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        applicationContext.startActivity(it)
                    }
                }
            }
            exitProcess(2)
        }.start()

        remoteConfig = FirebaseRemoteConfig.getInstance()
        val defaultValue: MutableMap<String, Any> = HashMap()
        defaultValue[UpdateHelper.KEY_UPDATE_ENABLE] = false
        defaultValue[UpdateHelper.IS_PAYMENT_DONE] = true
        defaultValue[UpdateHelper.IS_UPDATE_BASE_URL] = false
        defaultValue[UpdateHelper.KEY_BASE_URL] = "http://3.25.213.83:8000"
        defaultValue[UpdateHelper.KEY_UPDATE_ID] = ""
        defaultValue[UpdateHelper.KEY_UPDATE_VERSION] = "0.9"
        defaultValue[UpdateHelper.KEY_UPDATE_URL] = "https://www.google.com/url?sa=j&url=https%3A%2F%2Ffirebasestorage.googleapis.com%2Fv0%2Fb%2Ftest-ota-70cea.appspot.com%2Fo%2Fapp-debug.apk%3Falt%3Dmedia%26token%3Dcf15252f-6ee8-48fb-8c79-9a5877b9f000&uct=1676815994&usg=LXv7V3Jz5PtopUovLVVv2CrIllc.&source=chat"
        remoteConfig?.setDefaultsAsync(defaultValue)
        remoteConfig?.fetch(0)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                remoteConfig?.activate()
            }
        }

        if(DeviceRecordManager.readInceptionRecord() == null) DeviceRecordManager.createInceptionRecord()

        setupAppLevelExceptionHandler()

        if(isSystemSigned()) {
            Log.i("SYSCHECK", "App is signed by system")
            setUpKiosk(true)
        }

        setUpSciChartLicense()

        // init app version
        try {
            val pInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
            appVersion = pInfo.versionName
        } catch (e: NameNotFoundException) {
            Log.e("VERSION_CHECK", "Unable to fetch the version name from gradle file")
            e.printStackTrace()
        }
    }

//    private fun createNotificationChannel() {
//        val serviceChannel = NotificationChannel(
//            CHANNEL_ID,
//            "Example Service Channel",
//            NotificationManager.IMPORTANCE_DEFAULT
//        )
//        val manager = getSystemService(
//            NotificationManager::class.java
//        )
//        manager.createNotificationChannel(serviceChannel)
//    }

    private fun setUpSciChartLicense() {

        try {
            SciChartSurface.setRuntimeLicenseKey(BuildConfig.SCHICHART_API)
        } catch (e: Exception) {
            Log.e("SciChart", "Error when setting the license", e)
        }

    }

    override fun onTerminate() {
        super.onTerminate()
        settingsContentObserver?.let { this.contentResolver.unregisterContentObserver(it) }

    }

    /**
     * Requires system signature to
     * hide/unhide status bar
     */
    private fun setUpKiosk(isActive: Boolean){
        val action = if(isActive) "com.outform.hidebar" else "com.outform.unhidebar"
        sendBroadcast(Intent(action))
    }

    fun setupAppLevelExceptionHandler() {
        Log.i("APP_EXCEPTION_HANDLER", "Setting default error handler")
        Thread.setDefaultUncaughtExceptionHandler(AppLevelExceptionHandler(applicationContext))
    }

    fun getVersion(): String {
        return appVersion
    }

    private fun isSystemSigned(): Boolean {
        val pm = packageManager
        return try {
            val pi_app = pm.getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES)
            val pi_sys = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES)
            Log.i("SYSCHECK", "Found signatures")

            pi_app?.signatures != null && pi_sys.signatures[0] == pi_app.signatures[0]
        } catch (e: NameNotFoundException) {
            Log.i("SYSCHECK", "Unable to load the signatures")
            e.printStackTrace()
            false
        }
    }

    inner class SettingsContentObserver(handler: Handler?) : ContentObserver(handler) {

        override fun deliverSelfNotifications(): Boolean {
            return super.deliverSelfNotifications()
        }

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            AppUtils.setAlarmVolumeToMax(sInstance)
        }
    }
}