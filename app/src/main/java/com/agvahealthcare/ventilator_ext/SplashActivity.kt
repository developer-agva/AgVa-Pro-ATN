package com.agvahealthcare.ventilator_ext

import android.annotation.SuppressLint
import android.content.*
import android.content.Intent.ACTION_REBOOT
import android.content.Intent.ACTION_SHUTDOWN
import android.content.pm.PackageManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.latitude
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.logitude
import com.agvahealthcare.ventilator_ext.api.ServerLogger
import com.agvahealthcare.ventilator_ext.api.model.statusDataModel.StatusRequestModel
import com.agvahealthcare.ventilator_ext.callback.SimpleCallbackListener
import com.agvahealthcare.ventilator_ext.connection.parser.RaspiParser
import com.agvahealthcare.ventilator_ext.connection.parser.SpO2ParserExtension
import com.agvahealthcare.ventilator_ext.connection.support_threads.HandshakingTask
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.adapter.PrimaryObservedParameterClickListener
import com.agvahealthcare.ventilator_ext.database.entities.EventDataModel
import com.agvahealthcare.ventilator_ext.exceptions.AppLevelExceptionHandler
import com.agvahealthcare.ventilator_ext.location.DefaultLocationClient
import com.agvahealthcare.ventilator_ext.location.LocationClient
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.logging.FileLogger.Companion.dataNotFound
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.service.UsbService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*
import com.agvahealthcare.ventilator_ext.utility.utils.IntentFactory
import com.agvahealthcare.ventilator_ext.utility.utils.LocationFilter
import com.google.android.gms.location.LocationServices
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.InputStream
import java.io.OutputStream
import java.lang.Thread.setDefaultUncaughtExceptionHandler
import java.util.concurrent.TimeUnit


const val apkUrl =
    "https://raw.githubusercontent.com/feicien/android-auto-update/develop/extras/android-auto-update-v1.3.apk"

class SplashActivity : AppCompatActivity(), SimpleCallbackListener,
    PrimaryObservedParameterClickListener {

    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
    }

    private var mEventViewModel: EventViewModel? = null

    private var appLevelExceptionHandler: AppLevelExceptionHandler? = null

    private val REQUEST_STORAGE_PERMISSION_CODE = 111
    private val REQUEST_FINE_PERMISSION_CODE = 112
    private val REQUEST_COARSE_PERMISSION_CODE = 113
    private lateinit var mSplashActivityViewModel: SplashActivityViewModel

    private var communicationService: CommunicationService? = null
    private var isHandshakeAcknowledged = false
    private var handshakingTask: HandshakingTask? = null
    private var progressThread: Thread? = null
    private var preferenceManager: PreferenceManager? = null
    private var dataStoreManager: DataStoreManager? = null
    private var isSplashTimerRunning = false
    private var isHandshakingCompleted = false
    private var isServiceBound = false
    private var progressThreadState = true
    private var isReadingFromConnection = false
    private var startupCount: Int = 0
    private var hsCalibrationDialog: AlertDialog? = null
    private var hsFailureDialog: AlertDialog? = null
    private var commServiceIntent: Intent? = null
    private var threadTocheckActionDataAvailable: Thread? = null
    private var checkVentDataTimer: CountDownTimer? = null
    private var raspiParser: RaspiParser? =
        RaspiParser().addExtension(SpO2ParserExtension::class.java)// SpO2 extension added
    var isActivitySwitching: Boolean = false
    var isTimerRunning: Boolean = true

    private var countKnobData = 0

    private lateinit var socket: java.net.Socket
    private lateinit var outputStream: OutputStream
    private lateinit var inputStream: InputStream

    override fun onClick(position: Int, model: ObservedParameterModel) {
    }

    private lateinit var locationClient: LocationClient
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    // Service connection for bound services
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, ibinder: IBinder) {
            isServiceBound = true
            communicationService = (ibinder as CommunicationService.LocalBinder).service
            communicationService?.makeLog(SplashActivity::class.java.simpleName)
            Log.i("SERVICE_CHECK", "calling device connect from service")
            onDeviceConnect()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isReadingFromConnection = false
            isServiceBound = false
        }
    }

    private var timerToCheckBatteryData: CountDownTimer? = null

    @SuppressLint("HardwareIds")
    private fun getVentilatorDetailsApi() {
        CoroutineScope(Dispatchers.IO).launch {
            val response = ServerLogger.getVentiDetailsRequest(
                Settings.Secure.getString(
                    this@SplashActivity.contentResolver,
                    Settings.Secure.ANDROID_ID
                )
            )
            response?.let {
                if (it.statusCode == 200) {
                    preferenceManager?.saveVentiDetails("${it.data.Ward_No},${it.data.Hospital_Name},${it.data.Department_Name}")
                }
            }
        }
    }

    fun addEvents(eventMsg: String, uhid: String) {
        val eventDataModel = EventDataModel(
            eventMsg,
            uhid
        )
        mEventViewModel?.addEvent(eventDataModel)
    }

    fun addEventsForDevelopers(eventMsg: String, uhid: String) {
        val eventDataModel = EventDataModel(
            eventMsg,
            uhid
        )
        mEventViewModel?.addEventForDevelopers(eventDataModel)
    }


    /*
     * This provides intent filter for the Gatt Data Receiver
     */

    private fun getIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(IntentFactory.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(IntentFactory.ACTION_HARDWARE_SERIAL_NUMBER)
        intentFilter.addAction(IntentFactory.ACTION_DEVICE_CONNECTED)
        intentFilter.addAction(IntentFactory.ACTION_DEVICE_DISCONNECTED)
        intentFilter.addAction(IntentFactory.ACTION_KNOB_CHANGE)
        intentFilter.addAction(IntentFactory.ACTION_HANDSHAKE_COMPLETED)
        intentFilter.addAction(IntentFactory.ACTION_HANDSHAKE_TIMEOUT)
        intentFilter.addAction(IntentFactory.ACTION_ACK_AVAILABLE)
        intentFilter.addAction(IntentFactory.ACTION_STARTUP_CHECK)
        intentFilter.addAction(IntentFactory.ACTION_DOWNLOADED_ALREADY_LETS_INSTALL)
        intentFilter.addAction(IntentFactory.ACTION_BATTERY_STATUS_AVAILABLE)
        intentFilter.addAction(IntentFactory.ACTION_CHECK_USB_CONNECTIONS)
        intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED)

        return intentFilter
    }

    @SuppressLint("ResourceAsColor")
    private fun createAlertBox(destination: String,msg:String) {
        Log.i("testing_ota", "alertbox hit")
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@SplashActivity)

        builder.setMessage("Due you want to install the updated file..")

        builder.setTitle(msg)

        builder.setIcon(R.drawable.info)
        builder.setCancelable(false)

        builder.setPositiveButton("Install",
            DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                // When the user click yes button dialog box also be cancelled
                // check storage permission granted if yes then start downloading file
                DownloadController(this,"",false).installApk(destination)
                dialog.cancel()
            } as DialogInterface.OnClickListener)

        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                // If user click no then dialog box is cancelled.
                dialog.cancel()
            } as DialogInterface.OnClickListener)

        val alertDialog: AlertDialog = builder.create()

        // Show the Alert Dialog box
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.white))
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.white))
    }

    private var countBatteryData = 0
    private val connReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == null) return

            when (intent.action) {

                IntentFactory.ACTION_KNOB_CHANGE ->{
                    countKnobData = 0
                }

                IntentFactory.ACTION_DOWNLOADED_ALREADY_LETS_INSTALL -> {
                    val destination = intent.getStringExtra(FILE_DESTINATION)
                    Log.i("testing_ota", "broadcast hit")
                    if (destination != null) createAlertBox(destination,"File Is Ready To Install")
                }


                Intent.ACTION_BOOT_COMPLETED -> {
                    Log.i("CHECK_BOOT", "Here it is called")
                }

                IntentFactory.ACTION_CHECK_USB_CONNECTIONS -> {
                    val hid = intent.getBooleanExtra(USB_HID_DATA, false)
                    val venti = intent.getBooleanExtra(USB_VENTILATOR_DATA, false)

                    if (!hid) cvHideConnection.setImageResource(R.color.red)
                    else cvHideConnection.setImageResource(R.color.ack_green)

                    if (!venti) cvVentilatorConnection.setImageResource(R.color.red)
                    else cvVentilatorConnection.setImageResource(R.color.ack_green)

                    Log.i("check_connections", "$hid -- $venti")
                }

                IntentFactory.ACTION_HARDWARE_SERIAL_NUMBER -> {
                    val data = intent.getStringExtra(HARDWARE_SERIAL_NUMBER)
                    data?.takeIf { it.isNotEmpty() }?.apply {
                        CoroutineScope(Dispatchers.Main).launch {
//                            Log.i("check_serial_number_value",dataStoreManager.toString())
//                            dataStoreManager?.saveHardwareSerialNumber(data)
                        }
                    }
                }

                IntentFactory.ACTION_BATTERY_STATUS_AVAILABLE -> {
                    countBatteryData = 0
                    CoroutineScope(Dispatchers.Main).launch {
                        dataStoreManager?.saveDashRebootStatusFlag(true)
                    }
                }

                IntentFactory.ACTION_DATA_AVAILABLE -> {
                    val data = intent.getStringExtra(VENTILATOR_DATA)
                    var stringType: String? = null
                    try {
                        stringType = raspiParser!!.getDataType(data.toString())
                    } catch (e: Exception) {
                        stringType = null
                    }

                    threadTocheckActionDataAvailable = Thread {
                        runOnUiThread {
                            checkVentDataTimer = object : CountDownTimer(2000, 1000) {
                                override fun onTick(millisUntilFinished: Long) {
                                    Log.i("THevalueoferror", stringType.toString())
                                    if (stringType != null) {
                                        if (stringType == "A" || stringType == "B" || stringType == "C" || stringType == "D") {
                                            isActivitySwitching = true
                                            checkVentDataTimer?.onFinish()
                                            if (preferenceManager?.readLastVentMode() != -1) {
                                                tvUpdateMsg.text =
                                                    getString(R.string.hint_entering_ventilation)
                                                Intent(
                                                    this@SplashActivity,
                                                    DashBoardActivity::class.java
                                                ).also {
                                                    it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                                    startActivity(it)
                                                }
                                                finish()
                                            } else {
                                                isActivitySwitching = false
                                            }
                                        }
                                    }
                                }

                                override fun onFinish() {
                                    isTimerRunning = false
                                }
                            }.start()
                        }
                    }
                }

                IntentFactory.ACTION_HFNC_RESPONSE -> {
                    val data = intent.getStringArrayExtra(HFNC_MODE)
                    data?.takeIf { it.isNotEmpty() }?.apply {
//                          mDashBoardViewModel.setHFNCResponse(this)
                    }
                }

                IntentFactory.ACTION_DEVICE_CONNECTED -> {
                    Log.i("SPLASH_CHECK", "ventilator connected")
                    onDeviceConnect()
                }

                IntentFactory.ACTION_DEVICE_DISCONNECTED -> {
                    Log.i("SPLASH_CHECK", "ventilator disconnected")
                    onDeviceDisconnect()
                }

                IntentFactory.ACTION_HANDSHAKE_COMPLETED -> {

                    Log.i("swdadwa13123", "in handshake completed")

                    stopHandshaking()
                    isHandshakingCompleted = true

                    runOnUiThread {
                        tvUpdateMsg.text = getString(R.string.hint_handshake_completed)
                    }

                    if (preferenceManager?.readVentiConfigSetupStatus() == true) {
                        Thread {
                            runOnUiThread {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    // Dismiss progress bar after 2 seconds
                                    val intentData =
                                        Intent(this@SplashActivity, MainActivity::class.java)
                                    intentData.putExtra(IS_STAND_BY, false)
                                    startActivity(intentData)
                                    finish()
                                }, 250)
                            }
                        }.start()
                    }
                    else{
                        communicationService?.send(dataStingForConfig)
                    }
                }

                IntentFactory.ACTION_HANDSHAKE_TIMEOUT -> {

                    Log.i("CHECK_TIMEOUT", "sdsdsdsd")
                    isHandshakeAcknowledged = false
                    showTimeoutState()
                    stopHandshaking()
                    isHandshakingCompleted = false

                    Log.i("valueCheck", "${preferenceManager?.readRebootStatusForHandshake()}")
                    if (preferenceManager?.readRebootStatusForHandshake() == true) {
                        addEvents("Handshake Timeout", preferenceManager?.readUHID().toString())

                        hsFailureDialog = DialogBoxFactory.showDialog(
                            this@SplashActivity,
                            "CONNECTION FAILED",
                            "Unable to verify the connection with the ventilator",
                            "Try Again",
                            serviceListener
                        )
                        try {
                            validateConnectionState()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    } else {
                        layoutProgressCalib1.visibility = View.VISIBLE
                        tv_calib_UpdateMsg1.text =
                            "System will auto restart in 10 seconds to complete process successfully"
                        addEvents(
                            "Handshake Timeout, Initiating System Reboot Process",
                            preferenceManager?.readUHID().toString()
                        )
                        preferenceManager?.setRebootStatusForHandshake(true)

//                        CoroutineScope(Dispatchers.IO).launch {
//                            delay(10000L)
//                            Runtime.getRuntime().exec("reboot")
//                        }
                    }
                }

                IntentFactory.ACTION_STARTUP_CHECK -> {

                    val data = intent.getStringExtra("startupCheckAnalysis")

                    data?.let {
                        CoroutineScope(Dispatchers.Main).launch {

                            Log.i("listgetDataStore", it)
                            dataStoreManager?.saveStartUpCheckValue(it)

                            if (dataStoreManager?.getStartUpCheckFlag()?.first() == true) {
                                tvUpdateMsg.text = getString(R.string.hint_entering_standby)

                                dataStoreManager?.saveStartUpCheckFlag(false)

                                Log.i("STANDBY", "Sandby is false");

                                // Dismiss progress bar after 2 seconds
                                if (isActivitySwitching != true) {

                                    Log.i("CHECK_EXECUTION", "ENTERED MAINACTIVITY VIA STARTUP")
                                    val intentData = Intent(
                                        this@SplashActivity,
                                        MainActivity::class.java
                                    )
                                    intentData.putExtra(IS_STAND_BY, false)
                                    startActivity(intentData)
                                    finish()
                                }
                            }
                        }
                    }
                }

                IntentFactory.ACTION_ACK_AVAILABLE -> {
                    val ack = intent.getStringExtra(VENTILATOR_ACK)
                    Log.i("ACK_CHECK", "ACK received @$ack")

                    // FILTER FOR ACK 51 : Double handshake completed
                    if (ack != null && ack.isNotEmpty()) {
                        when (ack) {

                            // venti configs
                            ACK_CODE_5604 -> {
                                communicationService?.send("CM+CNF")
                                preferenceManager?.setVentiConfigSetupStatus(true)
                                if (threadTocheckActionDataAvailable != null || threadTocheckActionDataAvailable?.isAlive == false) {
                                    threadTocheckActionDataAvailable?.start()
                                }

                                threadTocheckActionDataAvailable?.join()
                                stopHandshaking()
                                isHandshakingCompleted = true

                                CoroutineScope(Dispatchers.IO).launch {

                                    if (dataStoreManager?.getStartUpCheckFlag()?.first() == true) {
                                        delay(500)
                                        communicationService?.takeIf { it.isPortsConnected }
                                            ?.apply {
                                                send("CM+STC")
                                            }
                                    }
                                }

                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(250L)

                                    if (dataStoreManager?.getStartUpCheckFlag()?.first() == false) {
                                        tvUpdateMsg.text = getString(R.string.hint_entering_standby)

                                        // Dismiss progress bar after 2 seconds
                                        if (isActivitySwitching != true) {
                                            val intentData = Intent(
                                                this@SplashActivity,
                                                MainActivity::class.java
                                            )
                                            intentData.putExtra(IS_STAND_BY, false)
                                            startActivity(intentData)
                                            finish()
                                        }
                                    }
                                }
                            }

                            // HANDSHAKE COMPLETED
                            ACK_CODE_5004 -> {

                                if (preferenceManager?.readVentiConfigSetupStatus() == true) {
                                    if (threadTocheckActionDataAvailable != null || threadTocheckActionDataAvailable?.isAlive == false) {
                                        threadTocheckActionDataAvailable?.start()
                                    }

                                    threadTocheckActionDataAvailable?.join()
                                    stopHandshaking()
                                    isHandshakingCompleted = true

                                    CoroutineScope(Dispatchers.IO).launch {

                                        if (dataStoreManager?.getStartUpCheckFlag()?.first() == true) {
                                            delay(500)
                                            communicationService?.takeIf { it.isPortsConnected }
                                                ?.apply {
                                                    send("CM+STC")
                                                }
                                        }
                                    }

                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(250L)

                                        if (dataStoreManager?.getStartUpCheckFlag()?.first() == false) {
                                            tvUpdateMsg.text = getString(R.string.hint_entering_standby)

                                            // Dismiss progress bar after 2 seconds
                                            if (isActivitySwitching != true) {
                                                Log.i(
                                                    "CHECK_EXECUTION",
                                                    "ENTERED MAINACTIVITY VIA 5004"
                                                )
                                                val intentData = Intent(
                                                    this@SplashActivity,
                                                    MainActivity::class.java
                                                )
                                                intentData.putExtra(IS_STAND_BY, false)
                                                startActivity(intentData)
                                                finish()
                                            }
                                        }
                                    }
                                }
                                else{
                                    communicationService?.send(dataStingForConfig)
                                }
                            }

                            ACK_CODE_824 -> {
                                mSplashActivityViewModel.batterySystemFailure.postValue(true)
                            }

                            ACK_CODE_834 -> {
                                mSplashActivityViewModel.batterySystemFailure.postValue(false)
                            }

                            ACK_CODE_4010 -> {
                                VentilatorApp.startupCheckDialogFrag = true
                            }

                            ACK_CODE_4011 -> {
                                tvUpdateMsg.text = getString(R.string.hint_entering_standby)
                                Log.i("STANDBY", "Standby is false");

                                // Dismiss progress bar after 2 seconds
                                if (!isActivitySwitching) {
                                    Log.i("CHECK_EXECUTION", "ENTERED MAIN ACTIVITY VIA 4011")
                                    val intentData = Intent(
                                        this@SplashActivity,
                                        MainActivity::class.java
                                    )
                                    intentData.putExtra(IS_STAND_BY, false)
                                    startActivity(intentData)
                                    finish()
                                }
                            }
                        }
                    }

                }


            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun calculateTotalAndLastHours(
        millis: Long
    ): String {
        return String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        )
    }

    private val serviceListener = object : SimpleCallbackListener {
        override fun doAction() {
            layoutService.visibility = View.VISIBLE
        }
    }

    private fun callRunningStatusApi(status: String,deviceStatus:String) {

        CoroutineScope(Dispatchers.IO).launch {
            val request = StatusRequestModel()
            dataStoreManager?.apply {
                request.apply {
                    this.did = Settings.Secure.getString(
                        this@SplashActivity.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    this.deviceStatus = deviceStatus
                    this.message = status
                    this.last_hours = calculateTotalAndLastHours(getLastHours().first().toLong())
                    this.total_hours = calculateTotalAndLastHours(getTotalHours().first().toLong())
                    this.health = " Good"
                    this.address = LocationFilter(this@SplashActivity).getAddress(
                        VentilatorApp.latitude,
                        VentilatorApp.logitude
                    )
                }
                Log.i("value_check_hours", "INVENTILATION $request")
                if (!ServerLogger.sendStatusRequest(request)) ServerLogger.sendStatusRequest(request)
            }
        }
    }

    private var mSocket: Socket? = null

    private var systemDialogFragment: SystemDialogFragment? = null

    private var heightSize: Int? = null
    private var widthSize: Int? = null
    private var dataStingForConfig = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // restart app after crashing

        mSplashActivityViewModel = ViewModelProvider(this).get(SplashActivityViewModel::class.java)
        dataStingForConfig = intent.getStringExtra(Configs.CONFIGS_STRING).toString()

        mEventViewModel = ViewModelProvider(this)[EventViewModel::class.java]

        Log.i("CHECK_COUNTER", startupCount.toString())
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(
                applicationContext
            )
        )

        //Below Code for the Location of the client and the data to be received for tracking of the ventilator..
        locationClient.getLocationUpdates(2000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location ->
                location.extras
                latitude = location.latitude
                logitude = location.longitude
                Log.i("value_location_error", "$latitude , $logitude splash")
                serviceScope.cancel()
                CoroutineScope(Dispatchers.IO).launch {

                    if (ServerLogger.sendLocationRequest(
                            LocationFilter(this@SplashActivity).getFullAddress(
                                latitude,
                                logitude
                            )
                        )
                    ) ServerLogger.sendLocationRequest(
                        LocationFilter(this@SplashActivity).getFullAddress(
                            latitude,
                            logitude
                        )
                    )
                }
            }
            .launchIn(serviceScope)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_splash)
        AppUtils.keepScreenAlive(this@SplashActivity, true)

        checkPermissions()
        preferenceManager = PreferenceManager(this)
        dataStoreManager = DataStoreManager(this)

        CoroutineScope(Dispatchers.Main).launch {
            if (dataStoreManager?.getCurrentActivity()?.first() == "Dash") {
                dashsplashSecondaryLayout.visibility = View.VISIBLE
                splashMainLayout.visibility = View.GONE
            } else {
                dashsplashSecondaryLayout.visibility = View.GONE
                splashMainLayout.visibility = View.VISIBLE
            }
        }

        VentilatorApp.currentActivityName = "Splash"
        CoroutineScope(Dispatchers.Main).launch {
            dataStoreManager?.saveCurrentActivity("Splash")
        }

        addEvents("Ventilator Started", preferenceManager?.readUHID().toString())
        callRunningStatusApi(RUNNING_STATUS_INACTIVE, ACTIVITY_SPLASH)
        CoroutineScope(Dispatchers.IO).launch {

            FileLogger.readCrashFile().let {
                if (it != dataNotFound) {

                    val result = ServerLogger.d(
                        this@SplashActivity,
                        it,
                        "AgvaPro"
                    )
                    if (result) FileLogger.deleteCrashFile()
                }
            }
        }
        getVentilatorDetailsApi()

        // setting app version
        tvVersion.text = "${getString(R.string.hint_version)}  ${VentilatorApp.getInstance()?.getVersion()}"
        btnService.setOnClickListener {
            systemDialogFragment = SystemDialogFragment.newInstance(
                heightSize,
                widthSize,
                false,
                null,
                null,
                null,
                communicationService
            ).apply { show(supportFragmentManager, "FromSplash") }

            systemDialogFragment?.isCancelable = false
        }

        registerReceiver(connReceiver, getIntentFilter())
        initServices()
        doBindService()

        VentilatorApp.IERatio = preferenceManager?.readRR()
            ?.let { Configs.calculateIERatio(it.toInt(), preferenceManager?.readTinsp()) }
            .toString()

        timerToCheckBatteryData = object : CountDownTimer(10000L, 1000L) {
            override fun onTick(milliSec: Long) {
                countBatteryData++
                countKnobData++
            }

            override fun onFinish() {
                CoroutineScope(Dispatchers.Main).launch {
                    dataStoreManager?.apply {

                        if (!getDashRebootStatusFlag().first() && countBatteryData >= 10) {
                            when (getCurrentActivity().first()) {

                                    "Dash" -> {
                                        addEventsForDevelopers(
                                            "Navigate From Splash to Ventilation Due to Battery Data Not Available",
                                            preferenceManager?.readUHID().toString()
                                        )
                                        Intent(
                                            this@SplashActivity,
                                            DashBoardActivity::class.java
                                        ).also {
                                            it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                            startActivity(it)
                                        }
                                        finish()
                                    }

                                    "Main" -> {
                                        addEventsForDevelopers(
                                            "Navigate From Splash to Standby Due to Battery Data Not Available",
                                            preferenceManager?.readUHID().toString()
                                        )
                                        val intentData =
                                            Intent(this@SplashActivity, MainActivity::class.java)
                                        intentData.putExtra(IS_STAND_BY, false)
                                        intentData.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                        startActivity(intentData)
                                        finish()
                                    }
                                }
                        }

                        else if (!getKnobRebootStatusFlag().first() && countKnobData >= 10){
                            when (getCurrentActivity().first()) {

                                "Dash" -> {
                                    addEventsForDevelopers(
                                        "Navigate From Splash to Ventilation Due to Knob Data Not Available",
                                        preferenceManager?.readUHID().toString()
                                    )
                                    Intent(
                                        this@SplashActivity,
                                        DashBoardActivity::class.java
                                    ).also {
                                        it.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                        startActivity(it)
                                    }
                                    finish()
                                }

                                "Main" -> {
                                    addEventsForDevelopers(
                                        "Navigate From Splash to Standby Due to Knob Data Not Available",
                                        preferenceManager?.readUHID().toString()
                                    )
                                    val intentData =
                                        Intent(this@SplashActivity, MainActivity::class.java)
                                    intentData.putExtra(IS_STAND_BY, false)
                                    intentData.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                    startActivity(intentData)
                                    finish()
                                }
                            }
                        }
                    }
                }
            }
        }.start()

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        heightSize = layoutPanelSplash.height
        widthSize = layoutPanelSplash.width
        hideSystemUI()
    }

    private fun checkPermissions() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i("APP_EXCEPTION_HANDLER", "Already has all permissions")
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onPause() {
        stopHandshaking()
        super.onPause()
    }

    override fun onRestart() {
        super.onRestart()
        if (threadTocheckActionDataAvailable != null || threadTocheckActionDataAvailable?.isAlive == false) {
            threadTocheckActionDataAvailable?.start()
        }
    }

    private fun onDeviceConnect() {
        Log.i(
            "USB_CHECK", "called device connect with com service " + communicationService
                    + " | ventConnected = " + communicationService?.isVentilatorConnected
                    + " | hidConnected = " + communicationService?.isHIDConnected
        )

        Log.i("connection_state","ondeviceconnect in splash")

        // FOR ALLOWING SCREEN AUTO LOCK
        AppUtils.keepScreenAlive(this@SplashActivity, true)
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            validateConnectionState()
            if (!isReadingFromConnection) {
                Log.i(
                    "USB_CHECK",
                    "connection status = $isPortsConnected during DEVICE_CONNECTED check"
                )

                isReadingFromConnection = true
                startReading()
            }
        } ?: kotlin.run {

        }
    }

    private fun onDeviceDisconnect() {
        Log.i("connection_state","ondevicedisconnect in splash")
        // FOR ALLOWING SCREEN AUTO LOCK
        AppUtils.keepScreenAlive(this@SplashActivity, false)
        showDisconnectState()
        stopHandshaking()
        if (isReadingFromConnection) {
            communicationService?.apply {
                stopReading()
            }
            isReadingFromConnection = false
        }

        hsCalibrationDialog?.takeIf { it.isShowing }?.apply {
            cancel()
        }

        hsFailureDialog?.takeIf { it.isShowing }?.apply {
            cancel()
        }

    }

//    private fun startSplashTimerWithState() {
//        if (isDeepSleep()) {
//            Log.i("SLEEP_CHECK", "Ventilator was in deep sleep in last session")
//            showDeepSleepState()
//        } else {
//            Log.i("SLEEP_CHECK", "Ventilator was normally shut down")
//            startSplashTimer()
//        }
//
//    }

//    private fun startSplashTimer() {
//        // Timer is required for the permission to resolve before validating the screen
//        if(!isSplashTimerRunning) {
//            isSplashTimerRunning = true
//            Handler(Looper.getMainLooper()).postDelayed({
//                validateConnectionState()
//                isSplashTimerRunning = false
//            }, SPLASH_SCREEN_LIFE)
//        }

//    }

    private fun validateConnectionState() {
//        Log.d("portconnected",communicationService?.isPortsConnected?.toString() ?: "False")
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            showConnectState()
            startHandshakingWithThreadSafety()
        } ?: kotlin.run {
            Log.i("CONNECTION_STATE_CHECK", "Comm service is null")
            showDisconnectState()
            // FOR ALLOWING SCREEN AUTO LOCK
            AppUtils.keepScreenAlive(this@SplashActivity, false)
        }
    }

    private fun broadcastHandshakeCompleted() {
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            sendBroadcastHandshakeCompleted()
        }
    }

    /*
  * This method will start handshaking countdown thread
  */
    private fun startHandshakingWithThreadSafety() {
        Handler(Looper.getMainLooper()).post {
            try {
                startHandshaking()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(connReceiver)
        doUnbindService()
        super.onDestroy()
    }

    private fun startHandshaking() {
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            if (handshakingTask == null) handshakingTask = HandshakingTask(this)
            if (handshakingTask?.isRunning == true) return

            Handler(Looper.getMainLooper()).postDelayed({
//                send(resources.getString(R.string.cmd_vent_wakeup))
            }, 1000)

            // starting handshaking
            Handler(Looper.getMainLooper()).postDelayed({
                handshakingTask?.apply {
                    Log.i("HS_CHECK", "HS started successfully inside 1000 ms looper")
                    start()
                }
            }, 100)

            // showing progress bar
            if (progressThread == null) {
                progressThread = Thread {
                    var i = 0
                    while (i < 70) {
                        if (progressThreadState) {
                            try {
                                Thread.sleep(30)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            val finalFixI = i
                            // runOnUiThread { progressBar.progress = finalFixI }
                        } else {
                            return@Thread
                        }
                        i += 1
                    }
                }
            }
            progressThreadState = true
            progressThread?.takeUnless { it.isAlive }?.start()
        }
    }

    /*
     * This method will stop handshaking thread
     */
    private fun stopHandshaking() {
        handshakingTask?.stop()
        progressThreadState = false
        progressThread = null
    }

    private fun showDisconnectState() {
        layoutProgress.visibility = View.GONE
        //  progressBar.progress = 0
        tvSwitchOffMsg.text = getString(R.string.press_switch_onn_manually)
        layoutVentiSwitchOff.visibility = View.VISIBLE
    }

    private fun showConnectState() {
        layoutVentiSwitchOff.visibility = View.GONE
        //   progressBar.progress = 0
        layoutProgress.visibility = View.VISIBLE
    }

    private fun showTimeoutState() {
        layoutVentiSwitchOff.visibility = View.GONE
        layoutProgress.visibility = View.GONE
        //  progressBar.progress = 0
    }

    private fun initServices() {
        if (commServiceIntent == null) {
            Log.i("SERVICE_CHECK", "Service intent created from init")
            commServiceIntent = Intent(this@SplashActivity, UsbService::class.java)
        }
    }

    private fun doBindService() {
        if (!isServiceBound && commServiceIntent != null) {
            bindService(commServiceIntent, mServiceConnection, BIND_AUTO_CREATE)
            Log.i("SERVICE_CHECK", "Service bound ")
            isServiceBound = true
        }
        startService(commServiceIntent)
    }

//        if (!UsbService.isServiceConnect) {
//            val startService = Intent(this, UsbService::class.java)
//            startService(startService)
//        }
//        val bindingIntent = Intent(this, UsbService::class.java)
//        bindService( , mServiceConnection, BIND_AUTO_CREATE)

    private fun doUnbindService() {
        if (isServiceBound) {
            try {
                unbindService(mServiceConnection)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                //ServerLogger.e(this@SplashActivity, e)
            }
            stopService(commServiceIntent)
        }
    }

    fun hideSystemUI(view: View) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, view).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        }
    }

    fun showSystemUI(view: View) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(
            window,
            view
        ).show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
    }

    override fun doAction() {
        layoutService.visibility = View.VISIBLE
    }
}

