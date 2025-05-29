package com.agvahealthcare.ventilator_ext

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.*
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.ack756Visibility
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.defaultOfExhaleValveRanges
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.defaultOfOxygenValveRanges
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.defaultOfTurbineRanges
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.globalCount
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.hidData
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.isLiveDataRequest
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.isLiveGraphDataRequest
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.latitude
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.logitude
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.startupCheckDialogFrag
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.ventiData
import com.agvahealthcare.ventilator_ext.alarm.limit_one.EncoderValue
import com.agvahealthcare.ventilator_ext.alarm.limit_one.KnobParameterModel
import com.agvahealthcare.ventilator_ext.api.ServerLogger
import com.agvahealthcare.ventilator_ext.api.model.calibrationDataModel.CalibrationRequestModel
import com.agvahealthcare.ventilator_ext.api.model.serviceDataModel.PaymentStatusRequestModel
import com.agvahealthcare.ventilator_ext.api.model.statusDataModel.StatusRequestModel
import com.agvahealthcare.ventilator_ext.callback.*
import com.agvahealthcare.ventilator_ext.connection.parser.RaspiParser
import com.agvahealthcare.ventilator_ext.connection.support_threads.HandshakingTask
import com.agvahealthcare.ventilator_ext.connection.support_threads.PingingTask
import com.agvahealthcare.ventilator_ext.control.AutoVentilationFragment
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterAdapter
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterClickListener
import com.agvahealthcare.ventilator_ext.dashboard.BaseLockActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.database.entities.EventDataModel
import com.agvahealthcare.ventilator_ext.databinding.ActivityMainBinding
import com.agvahealthcare.ventilator_ext.location.DefaultLocationClient
import com.agvahealthcare.ventilator_ext.location.LocationClient
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.logging.FileLogger.Companion.dataNotFound
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.model.SensorCalibration
import com.agvahealthcare.ventilator_ext.modes.GeneralGraphicalToolTipFragment
import com.agvahealthcare.ventilator_ext.modes.GraphicTooltipFragment
import com.agvahealthcare.ventilator_ext.modes.ModeDialogFragment
import com.agvahealthcare.ventilator_ext.modes.OnModeConfirmListener
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.service.UsbService
import com.agvahealthcare.ventilator_ext.standby.StandbyControlDialogFragment
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.system.debug.DebugViewModel
import com.agvahealthcare.ventilator_ext.system.diagnosticCheck.DiagnosticCheckViewModel
import com.agvahealthcare.ventilator_ext.system.o2Regulation.O2RegulationCheckViewModel
import com.agvahealthcare.ventilator_ext.system.test_calib.TestCalibrationFragment
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.*
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*
import com.example.demoremoteupdateapp.checkSelfPermissionCompat
import com.example.demoremoteupdateapp.requestPermissionsCompat
import com.example.demoremoteupdateapp.shouldShowRequestPermissionRationaleCompat
import com.example.demoremoteupdateapp.showSnackbar
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URISyntaxException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

interface ActivateVentilatorListener {
    fun activateVentilatorListener()
}


class MainActivity : BaseLockActivity(), OnCalibrationOxygen, UpdateHelper.OnUpdateCheckListener,
    UpdateHelper.OnUpdateBaseUrlListener,
    View.OnClickListener,
    ControlParameterClickListener,
    OnLoudnessAdjustmentListener, ActivateVentilatorListener {

    private lateinit var binding: ActivityMainBinding
    private val ctx = this@MainActivity
    private val TAG = MainActivity::class.java.simpleName
    private var handshakingTask: HandshakingTask? = null
    private var countKnobData = 0
    private var countBatteryData = 0
    private var isForKnob: Boolean? = null
    private var isKnobPressedForControlTile = false
    var tempPrefMapForExistingVentilationInteger = hashMapOf<String, Int>()
    var tempPrefMapForExistingVentilationFloat = hashMapOf<String, Float>()
    private var parseMap: Map<String, Map<String, String>>? = null
    private var raspiParser: RaspiParser? = RaspiParser()

    var lastUhidEvents = ""
    var lastUhid = ""
    var powerConnected = false
    private var shutDownProgress: ProgressDialog? = null
    private var ackVisibilities: BooleanArray = BooleanArray(6000)

    lateinit var downloadController: DownloadController

    private val TOOLTIP_TAG = "Main"

    private var heightSize: Int? = null
    private var widthSize: Int? = null

    private var serviceIntent: Intent? = null

    private var isfromStandby: Boolean = false
    private var activityCount: Int = 0

    private var ack5006SendFlg: Boolean = false
    private var ack5005SendFlg: Boolean = false

    private var selectedBasicPosition: Int? = null
    private var selectAdvancedPosition: Int? = null

    private var selectSmartFio2Position: Int? = null
    private var selectVtasPosition: Int? = null
    private var selectEtCuffPosition: Int? = null

    private var selectedBackupPosition: Int? = null
    private var communicationService: CommunicationService? = null

    private var prefManager: PreferenceManager? = null
    private var dataStoreManager: DataStoreManager? = null

    private var basicControlParameterList: List<ControlParameterModel>? = null
    private var backupControlParameterList: List<ControlParameterModel>? = null

    private lateinit var locationClient: LocationClient
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    private var advancedControlParameterList: List<ControlParameterModel>? = null
    private var smartFio2ControlParameterList: List<ControlParameterModel>? = null

    private var vTasControlParameterList: List<ControlParameterModel>? = null
    private var etCuffControlParameterList: List<ControlParameterModel>? = null

    private var isReadingFromConnection = false
    private var pingingTask: PingingTask? = null

    private var bodyParamsType: BodyParamsType? = null

    var isServiceBound = false
    private var progressDialog: KnobDialog? = null

    private var gender: Gender? = null
    private var height: Float? = null

    private var sizeOfEvents = 1

    private var age: Float? = null
    private val locationPermissionCode = 2
    private var weight: Float? = null

    private var graphicTooltipFragment: GraphicTooltipFragment? = null
    private var testCalibrationFragment: TestCalibrationFragment? = null

    internal var requestedModeCode: Int = 0
    private var tempModeType: Int = 0

    private var controlParameterAdapter: ControlParameterAdapter? = null

    private lateinit var mEventViewModel: EventViewModel
    private lateinit var mDebugViewModel: DebugViewModel
    private var customCountDownTimer: CustomCountDownTimer? = null

    private var modeDialogFragment: ModeDialogFragment? = null
    private var currentButtonID: View? = null

    private var calibrationConfirmDialog: AlertDialog? = null
    private var calibrationProgress: ProgressDialog? = null

    private var standbyControlFragment: StandbyControlDialogFragment? = null
    private var autoVentilationFragment: AutoVentilationFragment? = null

    private var systemDialogFragment: SystemDialogFragment? = null

    private var mediaPlayer: CustomMediaPlayer? = null
    private var isExistingVentilation: Boolean? = null

    private lateinit var ackTimer: CountDownTimer


    //variable for the mainactivity viewmodel for scoping the value in the child fragments as well
    private lateinit var mMainActivityViewModel: MainActivityViewModel
    private lateinit var mDiagnosticCheckViewModel: DiagnosticCheckViewModel
    private lateinit var mO2RegulationCheckViewModel: O2RegulationCheckViewModel
    private val settingsCountDownTimer = SettingsCountDownTimer(2500, 700)

    private val knobTimeoutListener = object : OnDismissDialogListener {
        override fun handleDialogClose() {
            selectedBasicPosition = null
            selectAdvancedPosition = null
            selectedBackupPosition = null
            selectSmartFio2Position = null
            selectVtasPosition = null
            selectEtCuffPosition = null

            //  progressDialog?.takeIf { it.isVisible }?.dismiss()
            Log.i("adawd", "1")
            normaliseParameterTiles()
            hideGraphicTooltip()
            renderControlParameterTilesViaPreference()
        }
    }

    private var currentPatientType: String = "";

    // Service connection for bound services
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, ibinder: IBinder) {
            isServiceBound = true
            communicationService = (ibinder as CommunicationService.LocalBinder).service

            communicationService?.makeLog(MainActivity::class.java.simpleName)
            communicationService?.let {
                updateUsbCheckView(it.isHIDConnected, it.isVentilatorConnected)
            }
            CoroutineScope(Dispatchers.Main).launch {
                if (dataStoreManager?.getCurrentActivity()?.first() == "Main") {
                    addEventsForDevelopers(
                        "Re-Initiate Handshake Due to App Crash during standby",
                        prefManager?.readUHID().toString()
                    )
                    onDeviceConnect()
                } else {
                    Log.i("handshake_check", "onServiceConnect Second")
                    communicationService?.takeIf { it.isPortsConnected }?.apply {
                        if (!isReadingFromConnection) {
                            Log.i(
                                "SERVICE_CHECK",
                                "connection status = $isPortsConnected during DEVICE_CONNECTED check"
                            )
                            isReadingFromConnection = true
                            startReading()
                            startPinging()
                            CoroutineScope(Dispatchers.IO).launch {
                                Log.i("CHECK_LOG_HERE", "HERE1")
                                delay(1000L)
                                send("CM+NEO")
                            }
                        }
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isServiceBound = false
        }
    }

    private val modeConfirmationListener = object : OnModeConfirmListener {

        override fun onConfirm(modeCode: Int) {
            binding.progressIndicator.visibility = View.VISIBLE
            normaliseButtons()
            modeDialogFragment?.dismiss()
            highlightButton(binding.buttonControls)
        }

        override fun onCancel() {

        }

    }

    // remove 38 & 39 mode functionality code as per embedded team concern
    //MODE ACK REPEAT
    private val startNewVentilationListener = object : OnStartVentilationListener {
        override fun onStart() {

            //Timer starting for receiving ACK after mode send...
//            startAckTimer()

            Log.i(
                "new_ventilation",
                "startNewVentilationListener in mainactivity ${isServiceBound.toString()}"
            )
            communicationService?.takeIf { it.isPortsConnected }?.apply {
                requestedModeCode.takeIf { isValidVentilatorMode(this@MainActivity, it) }?.let {
                    Log.i("CHECK_HERE_MODE", it.toString())
                    if (isExistingVentilation == true) {
//                        if (prefManager?.readSelectedOptions() == SELECTED_OPTIONS.NON_INVASIVE_NAME && (it == MODE_NIV_CPAP)) send(Configs.INV_CPAP.toString())
//                        else if (prefManager?.readSelectedOptions() == SELECTED_OPTIONS.NON_INVASIVE_NAME && (it == MODE_NIV_BPAP)) send(Configs.INV_BPAP.toString())
                        if (prefManager?.readSelectedOptions() == SELECTED_OPTIONS.PRONGS_NAME && (it != MODE_NC_CPAP && it != MODE_HFNC)) send(
                            MODE_NC_IPPV.toString()
                        )
                        else if (it == MODE_PC_PRVC) {
                            send(Configs.MODE_PC_SIMV.toString())
                        } else {
                            send(it.toString())
                        }
                    } else {
//                        if (prefManager?.readSelectedOptions() == SELECTED_OPTIONS.NON_INVASIVE_NAME && (it == MODE_NIV_CPAP)) send(
//                            Configs.INV_CPAP.toString()
//                        )
//                        else if (prefManager?.readSelectedOptions() == SELECTED_OPTIONS.NON_INVASIVE_NAME && (it == MODE_NIV_BPAP)) send(
//                            Configs.INV_BPAP.toString()
//                        )
                        if (prefManager?.readSelectedOptions() == SELECTED_OPTIONS.PRONGS_NAME && (it != MODE_NC_CPAP && it != MODE_HFNC)) send(
                            MODE_NC_IPPV.toString()
                        )
                        else if (it == MODE_PC_PRVC) {
                            send(Configs.MODE_PC_SIMV.toString())
                        } else {
                            send(it.toString())
                        }
                    }
                    requestedModeCode = it
                }
            }
            addEvents(
                "Ventilation started with new patient profile ${
                    Configs.getPatientType(
                        prefManager
                    )
                } with ${
                    getVentilatorModeByCode(
                        this@MainActivity,
                        requestedModeCode
                    )?.let { it.modeType }
                }", prefManager?.readUHID().toString()
            )

//            CoroutineScope(Dispatchers.IO).launch{
//                delay(2000L)
//                withContext(Dispatchers.Main){

//                }
//            }
        }
    }


    fun addEvents(eventMsg: String, uhid: String) {
        val eventDataModel = EventDataModel(
            eventMsg,
            uhid
        )
        mEventViewModel.addEvent(eventDataModel)
    }

    fun addEventsForDevelopers(eventMsg: String, uhid: String) {
        val eventDataModel = EventDataModel(
            eventMsg,
            uhid
        )
        mEventViewModel.addEventForDevelopers(eventDataModel)
    }

    private val fragmentDismissListener = object : OnDismissDialogListener {
        override fun handleDialogClose() {
            //normaliseButtons()
            binding.progressIndicator?.visibility = View.INVISIBLE
            normaliseButtons()
            disablePresence()
//            if (isVentiLocked) {
//                try {
//                    DialogBoxFactory.dismissDialogs()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                DialogBoxFactory.showServicePaymentDialog(ctx, this@MainActivity)
//            }
        }
    }

    private val standbyControlFragmentDismissListener = object : OnDismissDialogListener {
        override fun handleDialogClose() {
            disablePresence()
            VentilatorApp.globalModeType = null
            VentilatorApp.selectedOptions = null

            prefManager?.setSmartFiO2StatusTemp(false)
            prefManager?.setIRVStatusTemp(false)
            prefManager?.setVGVStatusTemp(false)
            prefManager?.setDeflashedStatusTemp(false)
            prefManager?.setEtCuffStatusTemp(false)
            prefManager?.setApneaSettingsStatusTemp(false)

            fragmentDismissListener.handleDialogClose()
            prefManager?.readVentilationMode()?.apply { requestedModeCode = this }
        }
    }

    private val onBodyParamsKnobPressListener = object : OnKnobPressListener {
        override fun onKnobPress(previousValue: Float, newValue: Float) {


            when (currentButtonID) {

                binding.includeProgressWeight.paramProgressBar -> {

                    if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                        currentButtonID = binding.includeProgressWeight.paramProgressBar
                        binding.includeProgressWeight.textView.text = String.format(
                            "%.1f",
                            newValue
                        )
                        binding.includeProgressWeight.paramProgressBar.setCurrentProgress(newValue.toDouble())
                        prefManager?.setBodyWeight(newValue.toDouble().toFloat())
                        weight = newValue
                        normalizeProgressBars()
                    } else {
                        currentButtonID = binding.includeProgressWeight.paramProgressBar
                        binding.includeProgressWeight.textView.text =
                            newValue.toDouble().toInt().toString()
                        binding.includeProgressWeight.paramProgressBar.setCurrentProgress(
                            newValue.toInt().toDouble()
                        )
                        prefManager?.setBodyWeight(newValue.toDouble().toFloat())
                        weight = newValue
                        normalizeProgressBars()
                    }
                }


                binding.includeProgressAge.paramProgressBar -> {

                    currentButtonID = binding.includeProgressAge.paramProgressBar
                    binding.includeProgressAge.textView.text =
                        newValue.toDouble().toInt().toString()
                    binding.includeProgressAge.paramProgressBar.setCurrentProgress(
                        newValue.toInt().toDouble()
                    )
                    prefManager?.setAge(newValue.toDouble().toFloat())
                    age = newValue
                    normalizeProgressBars()

                }

                binding.includeProgressHeight.paramProgressBar -> {

                    currentButtonID = binding.includeProgressHeight.paramProgressBar
                    binding.includeProgressHeight.textView.text =
                        newValue.toDouble().toInt().toString()
                    binding.includeProgressHeight.paramProgressBar.setCurrentProgress(
                        newValue.toInt().toDouble()
                    )
                    prefManager?.setBodyHeight(newValue.toDouble().toFloat())
                    height = newValue
                    normalizeProgressBars()

                }
            }

            progressDialog?.takeIf { it.isVisible }?.apply {
                this.dismiss()
            }
            progressDialog = null

        }
    }

    private val onBodyParamsLimitChangeListener = fun(view: View): OnLimitChangeListener {
        return object : OnLimitChangeListener {
            override fun onLimitChange(previousValue: Float, newValue: Float) {

                Log.i("dataHandling123", view.toString())

                when (currentButtonID) {

                    binding.includeProgressWeight.paramProgressBar -> {
                        if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                            binding.includeProgressWeight.paramProgressBar.setCurrentProgress(
                                newValue.toDouble()
                            )
                            binding.includeProgressWeight.textView.text =
                                String.format("%.1f", newValue)
                        } else {
                            view.let {
                                binding.includeProgressWeight.paramProgressBar.setCurrentProgress(
                                    newValue.toDouble()
                                )
                                binding.includeProgressWeight.textView.text =
                                    newValue.toInt().toString()
                            }
                        }
                    }

                    binding.includeProgressHeight.paramProgressBar -> {
                        binding.includeProgressHeight.paramProgressBar.setCurrentProgress(
                            newValue.toInt().toDouble()
                        )
                        binding.includeProgressHeight.textView.text = newValue.toInt().toString()
                    }

                    binding.includeProgressAge.paramProgressBar -> {
                        binding.includeProgressAge.paramProgressBar.setCurrentProgress(
                            newValue.toInt().toDouble()
                        )
                        binding.includeProgressAge.textView.text = newValue.toInt().toString()
                    }

                }
            }
        }
    }

    private val onBodyParamsCloseListener = object : OnDismissDialogListener {
        override fun handleDialogClose() {
            progressDialog?.takeIf { it.isVisible }?.apply {
                this.dismiss()
            }
            normalizeProgressBars()
            initBodyParamsViaPreferences()
        }
    }

    private val onBodyParamsTimeoutListener = object : OnDismissDialogListener {
        override fun handleDialogClose() {
            progressDialog?.takeIf { it.isVisible }?.dismiss()
            normalizeProgressBars()
            initBodyParamsViaPreferences()
        }
    }

    /*
     * This provides intent filter for the Gatt Data Receiver
     */

//    private fun updateServiceLayout(image:Int, color: Int){
//        serviceLayout.setBackgroundResource(color)
//        serviceIcon.setImageResource(image)
//    }

    private fun getIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(IntentFactory.ACTION_HANDSHAKE_TIMEOUT)
        intentFilter.addAction(IntentFactory.ACTION_DEVICE_CONNECTED)
        intentFilter.addAction(IntentFactory.ACTION_O2_REGILATION_TOOL_CHECK)
        intentFilter.addAction(IntentFactory.ACTION_DIAGNOSTIC_TOOL_CHECK)
        intentFilter.addAction(IntentFactory.ACTION_DEVICE_DISCONNECTED)
        intentFilter.addAction(IntentFactory.ACTION_DOWNLOADED_ALREADY_LETS_INSTALL)
        intentFilter.addAction(IntentFactory.ACTION_KNOB_CHANGE)
        intentFilter.addAction(IntentFactory.ACTION_LOCK)
        intentFilter.addAction(IntentFactory.ACTION_ACK_AVAILABLE)
        intentFilter.addAction(IntentFactory.ACTION_MODE_SET)
        intentFilter.addAction(IntentFactory.ACTION_SENSOR_AVAILABILITY_RESPONSE)
        intentFilter.addAction(IntentFactory.ACTION_SENSOR_CALIBRATION_RESPONSE)
        intentFilter.addAction(IntentFactory.ACTION_BATTERY_STATUS_AVAILABLE)
        intentFilter.addAction(IntentFactory.ACTION_MUTE_UNMUTE)
        intentFilter.addAction(IntentFactory.ACTION_POWER_OFF)
        intentFilter.addAction(IntentFactory.ACTION_BATTERY_CONNECTED)
        intentFilter.addAction(IntentFactory.ACTION_BATTERY_DISCONNECTED)
        intentFilter.addAction(IntentFactory.ACTION_POWER_ON)
        intentFilter.addAction(IntentFactory.ACTION_NEO_SENSOR_CONNECT)
        intentFilter.addAction(IntentFactory.ACTION_NEO_SENSOR_DISCONNECT)
        intentFilter.addAction(IntentFactory.ACTION_CALIBRATION_ERROR_DATA)
        intentFilter.addAction(IntentFactory.ACTION_STARTUP_CHECK)
        intentFilter.addAction(IntentFactory.ACTION_RESISTANCE_CALIBRATION_RESPONSE)
        intentFilter.addAction(IntentFactory.ACTION_COMPLIANCE_CALIBRATION_RESPONSE)
        intentFilter.addAction(IntentFactory.ACTION_CHECK_USB_CONNECTIONS)
        return intentFilter
    }

    private fun updateDiagData(map: Map<String, String>) {

        // first line
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_INSP_PRESSURE_RAW
        )?.let { mDiagnosticCheckViewModel.inspPressureRawData.value = "$it V" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_EXP_PRESSURE_RAW
        )?.let { mDiagnosticCheckViewModel.expPressureRawData.value = "$it V" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_OXP_PRESSURE_RAW
        )?.let { mDiagnosticCheckViewModel.oxyPressureRawData.value = "$it V" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_INSP_PRESSURE
        )?.let { mDiagnosticCheckViewModel.inspPressureData.value = "$it cmH₂O" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_EXP_PRESSURE
        )?.let { mDiagnosticCheckViewModel.expPressureData.value = "$it cmH₂O" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_OXP_PRESSURE
        )?.let { mDiagnosticCheckViewModel.oxyPressureData.value = "$it bar" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_INSP_FLOW_VOLTAGE
        )?.let { mDiagnosticCheckViewModel.inspFlowVoltageData.value = "$it V" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_INSP_FLOW
        )?.let { mDiagnosticCheckViewModel.inspFlowData.value = "$it LPM" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_EXP_DP_RAW
        )?.let { mDiagnosticCheckViewModel.expDPRawData.value = "$it pa" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_EXP_FLOW
        )?.let { mDiagnosticCheckViewModel.expFlowData.value = "$it LPM" }

        // mid line
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_BTRY_CURRENT
        )?.let { mDiagnosticCheckViewModel.batteryCurrentData.value = "$it Amp" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_BTRY_VOLTAGE
        )?.let { mDiagnosticCheckViewModel.batteryVoltageData.value = "$it V" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_BTRY_SOC
        )?.let { mDiagnosticCheckViewModel.batterySOCData.value = it }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_BTRY_STATE
        )?.let {
            mDiagnosticCheckViewModel.batteryStateData.value =
                if (it == "0") "DISCONNECTED" else "CONNECTED"
        }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_BTRY_REMAINING_TIME
        )?.let { mDiagnosticCheckViewModel.batteryRemainingTimeData.value = "$it min" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_POWER_CONNECTION
        )?.let {
            mDiagnosticCheckViewModel.powerConnectionData.value =
                if (it == "0") "DISCONNECTED" else "CONNECTED"
        }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_MAIN_SWITCH
        )?.let {
            mDiagnosticCheckViewModel.mainSwitchData.value =
                if (it == "0") "DISCONNECTED" else "CONNECTED"
        }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_SPO2_STATUS
        )?.let {
            mDiagnosticCheckViewModel.spo2StatusData.value =
                if (it == "0") "DISCONNECTED" else "CONNECTED"
        }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_HR
        )?.let { mDiagnosticCheckViewModel.hrData.value = "$it bpm" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_Spo2
        )?.let { mDiagnosticCheckViewModel.spo2Data.value = "$it %" }

        // last line
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_OXYGEN_SENSOR_VOLTAGE
        )?.let { mDiagnosticCheckViewModel.o2SensorVoltageData.value = "$it V" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_HARDWARE_VERSION
        )?.let { mDiagnosticCheckViewModel.hardwareVersionData.value = it }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_PI_TEMP
        )?.let { mDiagnosticCheckViewModel.piTempData.value = "$it ℃" }
        getMapValueFromLabel(
            map,
            RaspiParser.DATA_PI_CPU_LOAD
        )?.let { mDiagnosticCheckViewModel.piCpuLoadData.value = it }

        // static line
        try {
            val pInfo =
                packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            mDiagnosticCheckViewModel.softwareVersionData.postValue(version)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        try {
            val process = Runtime.getRuntime().exec("cat sys/class/thermal/thermal_zone0/temp")
            process.waitFor()
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line: String = reader.readLine()
            if (line != null) {
                val temp = line.toFloat()
                mDiagnosticCheckViewModel.screenCPUTempData.postValue("${(temp / 1000.0f).toInt()} ℃")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mDiagnosticCheckViewModel.knobPcbTypeData.postValue("USB/Legacy")
        mDiagnosticCheckViewModel.knobPcbVersionData.postValue("1.5.0")

        // sending data to live window
        mDiagnosticCheckViewModel.apply {
            var dataDiagnostic = deviceId
            dataDiagnostic += "^${inspPressureRawData.value},${inspPressureData.value},${inspFlowData.value},${inspFlowVoltageData.value},${expPressureRawData.value},${expPressureData.value},${expFlowData.value},${expDPRawData.value},${oxyPressureRawData.value},${oxyPressureData.value},${o2SensorVoltageData.value},${batteryCurrentData.value},${batteryVoltageData.value},${batterySOCData.value},${batteryRemainingTimeData.value},${batteryStateData.value},${powerConnectionData.value},${mainSwitchData.value},${spo2StatusData.value},${hrData.value},${spo2Data.value},${piTempData.value},${screenCPUTempData.value},${piCpuLoadData.value},${knobPcbTypeData.value},${knobPcbVersionData.value},${hardwareVersionData.value}"
            if (isLiveDataRequest) mSocket?.emit("DataSendingAndroidDiagnostic", dataDiagnostic)
        }
    }

    private fun getMapValueFromLabel(map: Map<String, String>, lbl: String): String? {
        try {
            if (map.containsKey(lbl)) return (map[lbl])
        } catch (exception: NumberFormatException) {
            exception.printStackTrace()
            return null
        }
        return null
    }

    // this is the broadcast receiver for the app
    fun updateUsbCheckView(hid: Boolean, venti: Boolean) {
        if (!hid) binding.cvHideConnectionMain.setImageResource(R.color.red)
        else binding.cvHideConnectionMain.setImageResource(R.color.ack_green)

        if (!venti) binding.cvVentilatorConnectionMain.setImageResource(R.color.red)
        else binding.cvVentilatorConnectionMain.setImageResource(R.color.ack_green)
    }

    private val connReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.action?.apply {
                when (this) {

                    IntentFactory.ACTION_HANDSHAKE_TIMEOUT -> {
                        // case for knob data knob available
                        if (isForKnob == true) {
                            addEventsForDevelopers(
                                "Handshake Timeout For Knob Data Not Available During Standby Case",
                                prefManager?.readUHID().toString()
                            )
                        }
                        // case for spo2 data knob available
                        else if (isForKnob == false) {
                            addEventsForDevelopers(
                                "Handshake Timeout For Spo2 Data Not Available During Standby Case",
                                prefManager?.readUHID().toString()
                            )
                        } else if (isForKnob == null) {
                            addEventsForDevelopers(
                                "Handshake Timeout For Crash During Standby Case",
                                prefManager?.readUHID().toString()
                            )
                        }
                        stopHandshaking()
                    }

                    IntentFactory.ACTION_CHECK_USB_CONNECTIONS -> {
                        val hid = intent.getBooleanExtra(USB_HID_DATA, false)
                        val venti = intent.getBooleanExtra(USB_VENTILATOR_DATA, false)

                        if (!hid) binding.cvHideConnectionMain.setImageResource(R.color.red)
                        else binding.cvHideConnectionMain.setImageResource(R.color.ack_green)

                        if (!venti) binding.cvVentilatorConnectionMain.setImageResource(R.color.red)
                        else binding.cvVentilatorConnectionMain.setImageResource(R.color.ack_green)

                        Log.i("check_connections", "$hid -- $venti")
                    }


                    IntentFactory.ACTION_O2_REGILATION_TOOL_CHECK -> {

                        val data = intent.getStringExtra(OR_TOOL_CHECK)
                        data?.takeIf { it.isNotEmpty() }?.apply {
                            mO2RegulationCheckViewModel.o2PressureData.postValue(this)
                        }
                    }

                    IntentFactory.ACTION_DOWNLOADED_ALREADY_LETS_INSTALL -> {
                        val destination = intent.getStringExtra(FILE_DESTINATION)
                        val isAuto = intent.getBooleanExtra(FILE_IS_AUTO_UPDATE, false)
                        Log.i("testing_ota", "broadcast hit")
                        if (destination != null) {
                            if (isAuto) createAlertBox(destination, "Updates Available!")
                            else createAlertBox(destination, "File Is Ready To Install")
                        }
                    }

                    IntentFactory.ACTION_DIAGNOSTIC_TOOL_CHECK -> {
                        val data = intent.getStringExtra(DIAG_TOOL_CHECK)

                        data?.let {
                            parseMap = raspiParser?.diaParser(it)
                            parseMap?.apply {
                                this.takeIf { it.isNotEmpty() }?.apply {
                                    val lbl = this.keys.iterator().next()
                                    this[lbl]?.apply {
                                        updateDiagData(this)
                                    }
                                }
                            }
                        }
                    }

                    IntentFactory.ACTION_BATTERY_STATUS_AVAILABLE -> {

                        CoroutineScope(Dispatchers.Main).launch {
                            if (countKnobData == 9 && dataStoreManager?.getKnobRebootStatusFlag()
                                    ?.first() == true
                            ) {
                                dataStoreManager?.saveKnobRebootStatusFlag(false)
                                addEventsForDevelopers(
                                    "Re-Initiate Handshake Due to Knob Data Not Available during Standby",
                                    prefManager?.readUHID().toString()
                                )
                                isForKnob = true
                                onDeviceConnect()
                            } else if (countKnobData == 9 && dataStoreManager?.getKnobRebootStatusFlag()
                                    ?.first() == false
                            ) {
                                addEventsForDevelopers(
                                    "Showing Software Knob Due to Knob Data Not Available in StandBy",
                                    prefManager?.readUHID().toString()
                                )
                                prefManager?.setKnobStatus(true)
                            }
                            countKnobData++
                        }

                        countBatteryData = 0
                        CoroutineScope(Dispatchers.Main).launch {
                            dataStoreManager?.saveDashRebootStatusFlag(true)
                        }

                        val batteryLevel = intent.getIntExtra(VENTILATOR_BATTERY_LEVEL, -1)
                        val batteryHealth = intent.getIntExtra(VENTILATOR_BATTERY_HEALTH, -1)
                        val batteryRemainingTime =
                            intent.getIntExtra(VENTILATOR_BATTERY_TTE, -1).let {
                                if (it > 30) it
                                else -1
                            }
                        //setbatteryLevelImage(batteryLevel)

                        // update battery level on view
                        /* if (batteryLevel in 0..100 && batteryHealth in 0..100 && batteryRemainingTime >= 0) {
                             updateBatteryLevel(batteryLevel, batteryHealth, batteryRemainingTime)
                         }*/

                        var tempBatteryLevel = mMainActivityViewModel.ventBatteryLevel.value
                        var tempBatteryHealth = mMainActivityViewModel.ventBatteryHealth.value
                        var tempBatteryRemainingTime =
                            mMainActivityViewModel.ventBatteryRemainingTime.value
                        if (tempBatteryHealth == null || tempBatteryHealth != batteryHealth) {
                            mMainActivityViewModel.setVentBatteryHealth(batteryHealth)
                        }
                        if (tempBatteryLevel != null || tempBatteryLevel != batteryLevel) {
                            mMainActivityViewModel.setVentBatteryLevel(batteryLevel)
                        }

                        mMainActivityViewModel.setVentBatteryRemainingTime(batteryRemainingTime)
                    }

                    IntentFactory.ACTION_COMPLIANCE_CALIBRATION_RESPONSE -> {
                        val data = intent.getStringExtra(TUBE_COMPLIANCE_CALIBRATION)

                        data?.takeIf { it.isNotEmpty() }?.apply {
                            Log.i("testingCompliance_Data", "compliance : $this")

                            prefManager?.apply {
                                if (data.contains("0.00")) {

                                    callCalibrationApi(
                                        Configs.CALIBRATION_TUBE_COMPLIANCE,
                                        CALIBRATION_FAILED
                                    )
                                    prefManager?.setComplianceTubeCalibrationStatus(false)

                                    addEvents(
                                        "Tube Compliance Calibration Failed",
                                        prefManager?.readUHID().toString()
                                    )
                                } else {
                                    callCalibrationApi(
                                        Configs.CALIBRATION_TUBE_COMPLIANCE,
                                        CALIBRATION_SUCCESS
                                    )
                                    prefManager?.setComplianceTubeCalibrationStatus(true)

                                    addEvents(
                                        "Tube Compliance Calibration Successful",
                                        prefManager?.readUHID().toString()
                                    )
                                }

                                prefManager?.setComplianceTubeCalibration(data)
                                systemDialogFragment?.takeIf { it.isVisible }
                                    ?.updateSensorTubeComplianceCalibrationViaPreference()
                            }
                        }
                    }

                    IntentFactory.ACTION_RESISTANCE_CALIBRATION_RESPONSE -> {
                        val data = intent.getStringExtra(TUBE_RESISTANCE_CALIBRATION)

                        data?.takeIf { it.isNotEmpty() }?.apply {
                            Log.i("testingResistance_data", "resistance : $this")
                            prefManager?.apply {
                                if (data.contains("0.00")) {
                                    setResistanceTubeCalibrationStatus(false)
                                    callCalibrationApi(
                                        Configs.CALIBRATION_TUBE_RESISTANCE,
                                        CALIBRATION_FAILED
                                    )
                                    addEvents(
                                        "Tube Resistance Calibration Failed",
                                        prefManager?.readUHID().toString()
                                    )
                                } else {
                                    setResistanceTubeCalibrationStatus(true)
                                    callCalibrationApi(
                                        Configs.CALIBRATION_TUBE_RESISTANCE,
                                        CALIBRATION_SUCCESS
                                    )
                                    addEvents(
                                        "Tube Resistance Calibration Successful",
                                        prefManager?.readUHID().toString()
                                    )
                                }

                                prefManager?.setResistanceTubeCalibration(data)
                                systemDialogFragment?.takeIf { it.isVisible }
                                    ?.updateSensorTubeResistanceCalibrationViaPreference()
                            }
                        }

                    }

                    IntentFactory.ACTION_MUTE_UNMUTE -> {
//                        Runtime.getRuntime().exec("reboot -p");
                    }

                    IntentFactory.ACTION_SENSOR_AVAILABILITY_RESPONSE -> {

                        intent.getStringArrayListExtra(SENSOR_ANALYSIS)
                            ?.takeIf { it.isNotEmpty() }
                            ?.apply {
                                Log.i("SENSOR_ANALYSE_CHECK", this.toString())
                                prefManager?.setSensorExhaleFlow(this[0].toInt())
                                prefManager?.setSensorInhaleFlow(this[1].toInt())
                                //  prefManager?.setSensorLowFlowO2(this[2].toInt())
                                prefManager?.setSensorHighFlowO2(this[3].toInt())
                                prefManager?.setSensorSPO2(this[5].toInt())
                                prefManager?.setSensorTemp(this[6].toInt())
                                prefManager?.setSensorHighPressureO2Line(this[7].toInt())
                                prefManager?.setSensorInspPressure(this[10].toInt())
                                prefManager?.setSensorExpPressure(this[11].toInt())
                            }
                    }

                    IntentFactory.ACTION_SENSOR_CALIBRATION_RESPONSE -> {
                        val status = intent.getStringExtra(VENTILATOR_SENSOR_CALIBRATION_TAG)
                        val calibResult =
                            intent.getIntExtra(VENTILATOR_SENSOR_CALIBRATION_RESULT, -1)

                        try {
                            status?.apply {
                                if (calibResult == -1) {
                                    Log.i(
                                        "CALIBCHECK",
                                        "Sensor calibration status not available"
                                    )
                                    return
                                }

                                val calibration = SensorCalibration(calibResult)

                                Log.i(
                                    "CALIBCHECK",
                                    "Sensor = $status calibrated with value = $calibResult"
                                )

                                when (this) {
                                    TAG_SENSOR_INSP_FLOW -> {
                                        prefManager?.setInspFlowCalibration(calibration)
                                    }

                                    TAG_SENSOR_EXP_FLOW -> {
                                        prefManager?.setExpFlowCalibration(calibration)
                                    }

                                    TAG_SENSOR_OXYGEN -> {
                                        prefManager?.setOxygenCalibration(calibration)
                                    }

                                    TAG_SENSOR_PRESSURE -> {
                                        prefManager?.setPresserCalibration(calibration)
                                    }

                                    TAG_SENSOR_TURBINE -> {
                                        prefManager?.setTurbineCalibration(calibration)
                                    }

                                    TAG_SENSOR_EXHALE_VALVE -> {
                                        prefManager?.setExhaleValveCalibration(calibration)
                                    }

                                    else -> {
                                        Log.i(
                                            "CALIBCHECK",
                                            "Invalid sensor tag for calibration"
                                        )
                                        /*  ServerLogger.w(
                                              context,
                                              "Invalid sensor tag for calibration"
                                          )*/
                                    }
                                }
                                Log.i("CALIBCHECK", "out of when")

                                systemDialogFragment?.getTestCalibFragment()
                                    ?.updateSensorCalibrationStatus()
                                systemDialogFragment?.getaAdvancedFragment()
                                    ?.updateSensorCalibrationStatus()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    IntentFactory.ACTION_KNOB_CHANGE -> {
                        val data = intent.getStringExtra(VENTILATOR_CONTROL_KNOB)
                        data?.takeIf { it.isNotEmpty() }?.apply {
                            Log.i("KNOB_DATADASH", this)

                            countKnobData = 0
                            CoroutineScope(Dispatchers.Main).launch {
                                dataStoreManager?.saveKnobRebootStatusFlag(true)
                            }

                            if (data != "X") {
                                if (DialogBoxFactory.dialogView != null && DialogBoxFactory.dialogView.isShowing) {
                                    Log.i("KNOB_DATADASH", DialogBoxFactory.dialogView.toString())
                                } else if (progressDialog?.isVisible == true) {
                                    progressDialog?.updateWithTimeoutDebounce(data)
                                } else if (systemDialogFragment?.isVisible == true) {
                                    systemDialogFragment?.highlightViewWithFocus(data)
                                } else if (standbyControlFragment?.isVisible == true) {
                                    standbyControlFragment?.highlightViewWithFocus(data)
                                } else if (modeDialogFragment?.isVisible == true) {
                                    // modeDialogFragment?.highlightViewWithFocus(data)
                                } else {
                                    highlightViewWithFocus(data)
                                }
                            } else {
                                if (DialogBoxFactory.dialogView != null && DialogBoxFactory.dialogView.isShowing) {
                                    Log.i("KNOB_DATADASH", DialogBoxFactory.dialogView.toString())
                                } else if (systemDialogFragment?.isVisible == true) {
                                    systemDialogFragment?.updateKnob(data)
                                }
                            }
                        }
                    }

                    IntentFactory.ACTION_DEVICE_CONNECTED -> {
                        onDeviceConnect()
                    }

                    IntentFactory.ACTION_DEVICE_DISCONNECTED -> {
                        onDeviceDisconnect()
                    }

                    IntentFactory.ACTION_CALIBRATION_ERROR_DATA -> {
                        val pressure = intent.getStringExtra("pressure")
                        val flow = intent.getStringExtra("flow")
                        val duty_cycle = intent.getStringExtra("dutycycle")
                        mMainActivityViewModel.calibrationerrorpressure.value = pressure
                        mMainActivityViewModel.calibrationerrorflow.value = flow
                        mMainActivityViewModel.calibrationerrordutycycle.value = duty_cycle
                        Log.i("VALUE_LOG_ERROR", "$pressure,$flow,$duty_cycle")
                    }

                    IntentFactory.ACTION_ACK_AVAILABLE -> {
                        val ackValue = intent.getStringExtra(VENTILATOR_ACK)
                        Log.i("ACK_CHECK", "ACK received @$ackValue")
                        val runningModeFromIntent = intent.getIntExtra(VENTILATOR_MODES, -1)
                        //code for the action ack 00

                        if (ackValue != null && ackValue.isNotEmpty()
//                            && isAcknowledgementAcceptable(
//                                runningModeFromIntent,
//                                ackValue
//                            )
                        )
                            handleAcknowledgements(ackValue)
                    }

                    IntentFactory.ACTION_MODE_SET -> {

                        Log.i("new_ventilation", "ACTION_MODE_SET")
                        requestedModeCode = intent.getIntExtra(VENTILATOR_MODES, -1)
                        Log.i("MODE_CODE", "MODE  $requestedModeCode")
                        if (Configs.isValidVentilatorMode(
                                this@MainActivity,
                                requestedModeCode
                            )
                        ) {
                            if (requestedModeCode == Configs.MODE_AUTO_VENTILATION) {
                                sendControlModeToVentilator(requestedModeCode)
                            } else {

                                Log.i(
                                    "new_ventilation",
                                    "ACtion_mode_Set in condition of AI vent else "
                                )
                                showStandbyControlFragment()
                            }
                        }
                    }

                    IntentFactory.ACTION_MODE_TYPE -> {
                        tempModeType = intent.getIntExtra(VENTILATOR_MODE_TYPE, 1)
                        Log.i("MODE_TYPE_CHECK", "mode_type $tempModeType")

                    }


                    //Intent for the shutdown procedure initiation
                    IntentFactory.ACTION_POWER_OFF -> {
                        //showShutDownConfirmation()
                        sendShutDownCommandToVentilator()
                    }

                    //Intent will only be fired when the power button will be pressed again
                    IntentFactory.ACTION_POWER_ON -> {
                        //  sendShutDownCommandToVentilator()
                    }


                    IntentFactory.ACTION_NEO_SENSOR_CONNECT -> {
                        Log.i("HERE", "CHECK SENSOR VALUE RECEIVED")
                        mMainActivityViewModel.setNeoNatalSensorConnectedFlag(true)
                        prefManager?.setNeoNateActiveStatus(true)
                        binding.neonateIconLayout.visibility = View.VISIBLE
                    }

                    IntentFactory.ACTION_NEO_SENSOR_DISCONNECT -> {
                        prefManager?.setNeoNateActiveStatus(false)
                        mMainActivityViewModel.setNeoNatalSensorConnectedFlag(false)
                        binding.neonateIconLayout.visibility = View.INVISIBLE
                    }


                    IntentFactory.ACTION_BATTERY_CONNECTED -> {
                        mMainActivityViewModel.setBAtteryConnectedFlag(true)
                    }

                    IntentFactory.ACTION_BATTERY_DISCONNECTED -> {
                        mMainActivityViewModel.setBAtteryConnectedFlag(false)
                    }
                }
            }
        }
    }


    private fun hideAllDialogFragment() {
        standbyControlFragment?.takeIf { it.isVisible }?.apply {
            dismiss()

        }

        systemDialogFragment?.takeIf { it.isVisible }?.apply {
            dismiss()

        }

        modeDialogFragment?.takeIf { it.isVisible }?.apply {
            dismiss()
        }
    }

    private fun callCalibrationApi(title: String, msg: String) {
        val request = CalibrationRequestModel()
        request.apply {
            did = Settings.Secure.getString(
                this@MainActivity.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            message = msg
            date = AppUtils.getCurrentDateTime()
            name = title
        }
        CoroutineScope(Dispatchers.IO).launch {
            if (!ServerLogger.sendCalibrationRequest(request)) ServerLogger.sendCalibrationRequest(
                request
            )
        }
    }

    private fun handleAcknowledgements(ackValue: String) {

        when (ackValue) {

            ACK_CODE_5301 -> {
                isOSReboot = false
                systemDialogFragment?.getUpdateDeviceFragment()
                    ?.showInfo("Checking For Internet...")
            }

            ACK_CODE_5302 -> {
                isOSReboot = false
                prefManager?.downloadType = "No Internet"
                prefManager?.downloadStatus = false
                prefManager?.downloadTime = AppUtils.getCurrentDateTime()
                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }

            ACK_CODE_5303 -> {
                isOSReboot = false
                systemDialogFragment?.getUpdateDeviceFragment()?.showInfo("Start Downloading...")
            }

            ACK_CODE_5304 -> {
                isOSReboot = false
                prefManager?.downloadType = "Download Failed"
                prefManager?.downloadStatus = false
                prefManager?.downloadTime = AppUtils.getCurrentDateTime()
                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }

            ACK_CODE_5305 -> {
                isOSReboot = false
                prefManager?.downloadType = "Download Successfull"
                prefManager?.downloadStatus = true
                prefManager?.downloadTime = AppUtils.getCurrentDateTime()
                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }

            ACK_CODE_5306 -> {
                isOSReboot = false
                prefManager?.downloadType = "Already Updated"
                prefManager?.downloadStatus = false
                prefManager?.downloadTime = AppUtils.getCurrentDateTime()
                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }

            ACK_CODE_5307 -> {
                isOSReboot = false
                prefManager?.downloadType = "Download Timeout"
                prefManager?.downloadStatus = false
                prefManager?.downloadTime = AppUtils.getCurrentDateTime()
                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }

            ACK_CODE_5308 -> {
                isOSReboot = true
                systemDialogFragment?.getUpdateDeviceFragment()?.showInfo("Installing...")
            }

            ACK_CODE_5309 -> {
                isOSReboot = false
                prefManager?.updateType = "Installation Failed"
                prefManager?.updateStatus = false
                prefManager?.updateTime = AppUtils.getCurrentDateTime()

                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }

            ACK_CODE_5310 -> {
                isOSReboot = false
                prefManager?.updateType = "Successfully Installed"
                prefManager?.updateStatus = true
                prefManager?.updateTime = AppUtils.getCurrentDateTime()

                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }


            ACK_CODE_5311 -> {
                isOSReboot = false
                prefManager?.downloadType = "Slow Internet"
                prefManager?.downloadStatus = false
                prefManager?.downloadTime = AppUtils.getCurrentDateTime()
                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }

            ACK_CODE_5312 -> {
                isOSReboot = true
                systemDialogFragment?.getUpdateDeviceFragment()?.showInfo("Start Restoring...")
            }

            ACK_CODE_5313 -> {
                isOSReboot = false
                prefManager?.restoreType = "Successfully Restored"
                prefManager?.restoreStatus = true
                prefManager?.restoreTime = AppUtils.getCurrentDateTime()
                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }

            ACK_CODE_5314 -> {
                isOSReboot = false
                prefManager?.restoreType = "Restore Failed"
                prefManager?.restoreStatus = false
                prefManager?.restoreTime = AppUtils.getCurrentDateTime()
                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }

            ACK_CODE_746 -> {
                isOSReboot = false

                prefManager?.updateType = "Update Completed"
                prefManager?.updateStatus = true
                prefManager?.updateTime = AppUtils.getCurrentDateTime()

                systemDialogFragment?.getUpdateDeviceFragment()?.updateSensorCalibrationStatus()
            }

            ACK_CODE_824 -> {
                mMainActivityViewModel.batterySystemFailure.postValue(true)
            }

            ACK_CODE_834 -> {
                mMainActivityViewModel.batterySystemFailure.postValue(false)
            }

            // leak test
            ACK_CODE_4020 -> {
                showCalibrationDialog("Ventilator is under Leak Test")
            }

            ACK_CODE_4021 -> {
                callCalibrationApi(Configs.CALIBRATION_LEAK_TEST, Configs.CALIBRATION_FAILED)

                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                prefManager?.setLeakTestCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()

                addEvents(
                    "Leak test failed due to flow greater than 4 LPM",
                    prefManager?.readUHID().toString()
                )

                showCalibrationErrorDialog("Leak test failed due to flow greater than 4 LPM")
            }

            ACK_CODE_4022 -> {
                callCalibrationApi(Configs.CALIBRATION_LEAK_TEST, Configs.CALIBRATION_SUCCESS)
                calibrationConfirmDialog?.dismiss()

                prefManager?.setLeakTestCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_SUCCESS
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()

                addEvents("Leak test success", prefManager?.readUHID().toString())
            }

            // neonate zeroing
            ACK_CODE_4023 -> {
                showCalibrationDialog("Zeroing under process")
            }

            ACK_CODE_4024 -> {
                callCalibrationApi(Configs.CALIBRATION_NEO_ZERO, Configs.CALIBRATION_FAILED)

                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                prefManager?.setNeoZeroCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()

                addEvents(
                    "Neonate sensor zeroing failed",
                    prefManager?.readUHID().toString()
                )

                showCalibrationErrorDialog("Neonate sensor zeroing failed")
            }

            ACK_CODE_4025 -> {
                callCalibrationApi(Configs.CALIBRATION_NEO_ZERO, Configs.CALIBRATION_FAILED)

                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                prefManager?.setNeoZeroCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()

                addEvents(
                    "Neonate sensor zeroing failed",
                    prefManager?.readUHID().toString()
                )

                showCalibrationErrorDialog("Neonate sensor zeroing failed")
            }

            ACK_CODE_4026 -> {
                callCalibrationApi(Configs.CALIBRATION_NEO_ZERO, Configs.CALIBRATION_FAILED)

                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                prefManager?.setNeoZeroCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()

                addEvents(
                    "Neonate sensor zeroing failed",
                    prefManager?.readUHID().toString()
                )

                showCalibrationErrorDialog("Neonate sensor zeroing failed")
            }

            ACK_CODE_4027 -> {
                callCalibrationApi(Configs.CALIBRATION_NEO_ZERO, Configs.CALIBRATION_FAILED)

                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                prefManager?.setNeoZeroCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()

                addEvents(
                    "Neonate sensor zeroing failed",
                    prefManager?.readUHID().toString()
                )

                showCalibrationErrorDialog("Neonate sensor zeroing failed")
            }

            ACK_CODE_4028 -> {
                callCalibrationApi(Configs.CALIBRATION_NEO_ZERO, Configs.CALIBRATION_SUCCESS)
                calibrationConfirmDialog?.dismiss()

                prefManager?.setNeoZeroCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_SUCCESS
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()

                addEvents("Neonate Sensor Zeroing Successfully", prefManager?.readUHID().toString())
            }

            // ACK FOR INSP FLOW
            ACK_CODE_5120 -> {
                showCalibrationDialog("Ventilator is under Inspiratory Flow Calibration Process")
            }

            ACK_CODE_4010 -> {
//                showStartupCheckDialog("Startup check result")
            }

            ACK_CODE_4011 -> {
                showCalibrationErrorDialog("Error in StartupCheck")
            }

            ACK_CODE_5133 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("External Flow Calibrator not found")
            }

            ACK_CODE_5134 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("External Flow Calibrator not found")
            }

            ACK_CODE_5135 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Issue in Inspiratory flow Calibration")
            }

            ACK_CODE_5136 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Issue in Inspiratory flow Calibration")
            }

            ACK_CODE_5121 -> {
                callCalibrationApi(Configs.CALIBRATION_INSP_FLOW, Configs.CALIBRATION_FAILED)

                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }

                prefManager?.setInspFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()

                showCalibrationErrorDialog("Calibration Stopped due to Error")
            }

            ACK_CODE_5122 -> {
                callCalibrationApi(Configs.CALIBRATION_INSP_FLOW, Configs.CALIBRATION_SUCCESS)
                calibrationConfirmDialog?.dismiss()

                prefManager?.setInspFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_SUCCESS
                    )
                )
                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5126 -> {

                callCalibrationApi(Configs.CALIBRATION_EXP_FLOW, Configs.CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }

                showCalibrationErrorDialog("Calibration Stopped due to some issue in calibrator")

                prefManager?.setExpFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            // ACK FOR EXP FLOW
            ACK_CODE_5123 -> {
                showCalibrationDialog("Ventilator is under Expiratory Flow Calibration Process")
            }


            ACK_CODE_5124 -> {

                callCalibrationApi(Configs.CALIBRATION_EXP_FLOW, Configs.CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }

                showCalibrationErrorDialog("Calibration Stopped due to Error")

                prefManager?.setExpFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )


                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5125 -> {

                Log.i("in_erroadadwr", "in error")

                callCalibrationApi(Configs.CALIBRATION_EXP_FLOW, Configs.CALIBRATION_SUCCESS)
                calibrationConfirmDialog?.dismiss()

                prefManager?.setExpFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_SUCCESS
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            // handshake completed
            ACK_CODE_5004 -> {
                if (isForKnob == true) {
                    addEventsForDevelopers(
                        "Handshake Completed For Knob Data Not Available During Standby Case",
                        prefManager?.readUHID().toString()
                    )
                }
                // case for spo2 data knob available
                else if (isForKnob == false) {
                    addEventsForDevelopers(
                        "Handshake Completed For Battery Data Not Available During Standby Case",
                        prefManager?.readUHID().toString()
                    )
                } else if (isForKnob == null) {
                    addEventsForDevelopers(
                        "Handshake Completed For Crash During Standby Case",
                        prefManager?.readUHID().toString()
                    )
                }
                stopHandshaking()
            }

            ACK_CODE_5006 -> {
                //Another Cancels here after ACK 5005
//                ackTimer.cancel()

                VentilatorApp.globalModeType?.let {
                    prefManager?.setModeType(it)
                }

                VentilatorApp.globalModeType = null
//                ack5006SendFlg = !ack5006SendFlg;

                prefManager?.apply {
                    setSmartFiO2Status(false)
                    setEtCuffStatusTemp(false)
                    setIRVStatus(readIRVStatusTemp())
                    setVGVStatus(readVGVStatusTemp())
                    setDeflashedStatus(readDeflashedStatusTemp())
                    setApneaSettingsStatus(readApneaSettingsStatusTemp())
                }

                if (isExistingVentilation == true) {

                    if (tempPrefMapForExistingVentilationInteger.isNotEmpty() && tempPrefMapForExistingVentilationFloat.isNotEmpty()) {
                        saveTempPrefForExistingVentilationToMainPrefs()
                        tempPrefMapForExistingVentilationInteger.clear()
                        tempPrefMapForExistingVentilationFloat.clear()
                    }
                    sendConfigurationToVentilatorWithWatchDog()
                } else {

                    prefManager?.setAdmitDate(
                        prefManager?.readUHID(),
                        AppUtils.getCurrentDateTime()
                    )
                    // set selected options into preference
                    when (VentilatorApp.selectedOptions) {
                        SELECTED_OPTIONS.PRONGS_NAME -> {
                            prefManager?.setSelectedOptions(SELECTED_OPTIONS.PRONGS_NAME)
                        }

                        SELECTED_OPTIONS.INVASIVE_NAME -> {
                            prefManager?.setSelectedOptions(SELECTED_OPTIONS.INVASIVE_NAME)
                        }

                        SELECTED_OPTIONS.NON_INVASIVE_NAME -> {
                            prefManager?.setSelectedOptions(SELECTED_OPTIONS.NON_INVASIVE_NAME)
                        }
                    }

                    VentilatorApp.selectedOptions = null
                    updateModeAndSendParametersToVentilator(requestedModeCode)
                }
                //sendConfigurationToVentilatorWithWatchDog()
                lastUhid = prefManager?.readUHID().toString()


            }


            ACK_CODE_5601 -> {
                DialogBoxFactory.showNeoSensorFailureWarning(this@MainActivity)
            }

            //setting saved

            ACK_CODE_5005 -> {


                //Acknowledgement Timer cancels here..

                activityCount++
//                ack5005SendFlg = !ack5005SendFlg
                prefManager?.setLastUid(prefManager?.readCurrentUid())
                binding.progressIndicator.visibility = View.VISIBLE
                settingsCountDownTimer.safeStop()

                try {
                    standbyControlFragment?.takeIf { it.isVisible }?.dismiss()
                } catch (e: Error) {
                    Log.i(
                        "STANBYCONTROL_CHECK",
                        "Failed during standby control fragment dismiss"
                    )
                }
                if (isExistingVentilation == true && isfromStandby == true) {
                    //Code to be inserted here for the tiles to be rendered from the exisiting ventilation when the values get flushed.
                } else {
                    renderControlParameterTilesViaPreference()
                }

                if (mMainActivityViewModel.isVentilationInitiatedFromExisting.value == true) {
                    val intent = Intent(this@MainActivity, DashBoardActivity::class.java)
                    intent.putExtra("fromButton", "existingVentilation")
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    finish()
                } else {
                    val intent = Intent(this@MainActivity, DashBoardActivity::class.java)
                    intent.putExtra("fromButton", "startNewVentilation")
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    finish()
                }


            }

            ACK_CODE_5011 -> {
                showCalibrationDialog("Ventilator is under Turbine Calibration Process")
            }

            ACK_CODE_5012 -> {
                callCalibrationApi(Configs.CALIBRATION_TURBINE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Check for the Inner Functionality")
                // save time stamp in preference
                prefManager?.setTurbineCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5013 -> {
                callCalibrationApi(Configs.CALIBRATION_TURBINE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Pressure Sensor Not Found")
                // save time stamp in preference
                prefManager?.setTurbineCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5014 -> {
                callCalibrationApi(Configs.CALIBRATION_TURBINE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Transfer Function Error")
                // save time stamp in preference
                prefManager?.setTurbineCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5015 -> {
                callCalibrationApi(Configs.CALIBRATION_TURBINE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Fatal Error !! Turbine not working")

                // save time stamp in preference
                prefManager?.setTurbineCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5016 -> {
                callCalibrationApi(Configs.CALIBRATION_TURBINE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Flow is greater than 5 Ltr/Min")
                // save time stamp in preference
                prefManager?.setTurbineCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5017 -> {
                callCalibrationApi(Configs.CALIBRATION_TURBINE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Turbine not started")
                // save time stamp in preference
                prefManager?.setTurbineCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5019 -> {

                callCalibrationApi(Configs.CALIBRATION_TURBINE, CALIBRATION_SUCCESS)
                calibrationConfirmDialog?.dismiss()
                // save time stamp in preference
                prefManager?.setTurbineCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_SUCCESS
                    )
                )

                // check if concerned view is visible , if yes then refresh to render it from preference
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()

            }

            ACK_CODE_5021 -> {

            }

            ACK_CODE_5022 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped due to Error !! Fluke not found")
                prefManager?.setExpFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5023 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped due to Error !! Turbine not working")
                prefManager?.setExpFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5024 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped due to Error !! ")
                prefManager?.setExpFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5025 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped due to Error !! Fatal Error")
                prefManager?.setExpFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5026 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped . Fluke not Connected,Please re-connect it !!")
                prefManager?.setExpFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5028 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped due to Error !! ADC not found")
                prefManager?.setExpFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5029 -> {
                calibrationConfirmDialog?.dismiss()
                prefManager?.setExpFlowCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_SUCCESS
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
                calibrationConfirmDialog?.dismiss()
            }

            ACK_CODE_5081 -> {

                showCalibrationDialog("Ventilator is under Oxygen Calibration Process")

            }

            ACK_CODE_5082 -> {

                Log.i("CHECK_CALIB", "CaLLED 1")
                callCalibrationApi(Configs.CALIBRATION_OXYGEN, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped !! Pressure sensor not found")
                prefManager?.setOxygenCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()

            }

            ACK_CODE_5083 -> {
                Log.i("CHECK_CALIB", "CaLLED 2")
                callCalibrationApi(Configs.CALIBRATION_OXYGEN, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped !! No O2 Supply Connected")
                prefManager?.setOxygenCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5084 -> {
                Log.i("CHECK_CALIB", "CaLLED 3")
                callCalibrationApi(Configs.CALIBRATION_OXYGEN, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped due to low line pressure")

                prefManager?.setOxygenCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5140 -> {
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showO2RegulationDialog("Ventilator under O₂ regulator calibration")
            }

            ACK_CODE_5141 -> {

                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
            }

            ACK_CODE_5089 -> {
                callCalibrationApi(Configs.CALIBRATION_OXYGEN, CALIBRATION_SUCCESS)
                prefManager?.setOxygenCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_SUCCESS
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
                calibrationConfirmDialog?.dismiss()
            }

            ACK_CODE_5051 -> {

            }

            ACK_CODE_5052 -> {

            }

            ACK_CODE_5053 -> {

            }

            ACK_CODE_5054 -> {

            }

            ACK_CODE_5055 -> {

            }

            ACK_CODE_5056 -> {

            }

            ACK_CODE_5059 -> {
                prefManager?.setOxygenCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_SUCCESS
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
                calibrationConfirmDialog?.dismiss()
            }

            ACK_CODE_5071 -> {
                showCalibrationDialog("Ventilator is under Exhale Valve Calibration Process")
            }

            ACK_CODE_5072 -> {

                callCalibrationApi(Configs.CALIBRATION_EXHALE_VALVE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog(
                    "Calibration Stopped due to Error : flow  ${mMainActivityViewModel.calibrationerrorflow.value}  " +
                            " Pressure : ${mMainActivityViewModel.calibrationerrorpressure.value}  Dutycycle : ${mMainActivityViewModel.calibrationerrordutycycle.value}"
                )
                prefManager?.setExhaleValveCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5073 -> {
                callCalibrationApi(Configs.CALIBRATION_EXHALE_VALVE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped due to Error !!")

                prefManager?.setExhaleValveCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5074 -> {
                callCalibrationApi(Configs.CALIBRATION_EXHALE_VALVE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped due to Error !! Fatal Error")

                prefManager?.setExhaleValveCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }


            ACK_CODE_5075 -> {
                callCalibrationApi(Configs.CALIBRATION_EXHALE_VALVE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped due to Error !! Diaphragm not found")

                prefManager?.setExhaleValveCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5078 -> {
                callCalibrationApi(Configs.CALIBRATION_EXHALE_VALVE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog("Calibration Stopped due to Error !! ADC not Connected")

                prefManager?.setExhaleValveCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_FAILURE
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
            }

            ACK_CODE_5079 -> {
                callCalibrationApi(Configs.CALIBRATION_EXHALE_VALVE, CALIBRATION_SUCCESS)
                prefManager?.setExhaleValveCalibration(
                    SensorCalibration(
                        AppUtils.getCurrentDateTime(),
                        Configs.SENSOR_CALIBRATION_SUCCESS
                    )
                )
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorsCalibrationStatusViaPreference()
                calibrationConfirmDialog?.dismiss()
            }

            ACK_CODE_5094 -> {
                showTubeCalibrationDialog("Ventilator is under circuit resistance calibration process")
            }

            ACK_CODE_5095 -> {
                Log.i("valuadadwad", "in success")
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
            }

            ACK_CODE_5096 -> {
                Log.i("valuadadwad", "in ack")
                callCalibrationApi(Configs.CALIBRATION_TUBE_RESISTANCE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog(
                    "Tube Resistance calibration failed"
                )

                addEvents(
                    "Tube Resistance Calibration Failed",
                    prefManager?.readUHID().toString()
                )
                prefManager?.setResistanceTubeCalibrationStatus(false)
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorTubeResistanceCalibrationViaPreference()
            }

            ACK_CODE_5101 -> {
                showTubeCalibrationDialog("Ventilator is under circuit compliance calibration process")
            }

            ACK_CODE_5102 -> {

                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
            }

            ACK_CODE_5103 -> {
                callCalibrationApi(Configs.CALIBRATION_TUBE_COMPLIANCE, CALIBRATION_FAILED)
                calibrationConfirmDialog?.takeIf { it.isShowing }?.apply {
                    this.dismiss()
                }
                showCalibrationErrorDialog(
                    "Tube Compliance calibration failed"
                    /*"Calibration Stopped due to Error : flow  ${mMainActivityViewModel.calibrationerrorflow.value}  " +
                            " Pressure : ${mMainActivityViewModel.calibrationerrorpressure.value}  Dutycycle : ${mMainActivityViewModel.calibrationerrordutycycle.value}"*/
                )

                prefManager?.setComplianceTubeCalibrationStatus(false)
                systemDialogFragment?.takeIf { it.isVisible }
                    ?.updateSensorTubeComplianceCalibrationViaPreference()

                addEvents(
                    "Tube Compliance Calibration Failed",
                    prefManager?.readUHID().toString()
                )
            }

            ACK_CODE_5003 -> {
                val inte = Intent(this@MainActivity, ShutDownActivity::class.java)
                inte.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(inte)
                finish()
            }

            ACK_CODE_16 -> {
                Log.d(
                    "ackvaluemessage", "$ACK_CODE_16   ${
                        MessageFactory.getAckMessage(
                            this@MainActivity,
                            ACK_CODE_16,
                            prefManager?.readLastVentMode()
                        )
                    }"
                )
                mMainActivityViewModel.setBAtteryConnectedFlag(false)
            }


            ACK_CODE_6 -> {
                Log.d(
                    "ackvaluemessage", "$ACK_CODE_6   ${
                        MessageFactory.getAckMessage(
                            this@MainActivity,
                            ACK_CODE_6,
                            prefManager?.readLastVentMode()
                        )
                    }"
                )
                mMainActivityViewModel.setBAtteryConnectedFlag(true)
            }
        }
    }

    private fun onDeviceConnect() {
        Log.i("connection_state", "ondeviceconnect in main")
        Log.i("handshake_check", "ondevice connect")
        // FOR ALLOWING SCREEN AUTO LOCK
        AppUtils.keepScreenAlive(this@MainActivity, true)
        Log.i("handshake_check", "onServiceConnect Second")
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            validateConnectionState()
            if (!isReadingFromConnection) {
                Log.i(
                    "SERVICE_CHECK",
                    "connection status = $isPortsConnected during DEVICE_CONNECTED check"
                )
                isReadingFromConnection = true
                startReading()
                startPinging()
                CoroutineScope(Dispatchers.IO).launch {
                    Log.i("CHECK_LOG_HERE", "HERE1")
                    delay(1000L)
                    send("CM+NEO")
                }
            }
        }
    }

    private fun onDeviceDisconnect() {
        Log.i("connection_state", "ondevicedisconnect in main")
        Log.i("handshake_check", "ondevice disconnect")
        // FOR ALLOWING SCREEN AUTO LOCK
        AppUtils.keepScreenAlive(this@MainActivity, false)
        stopHandshaking()
        if (isReadingFromConnection) {
            communicationService?.apply {
                stopReading()
            }
            isReadingFromConnection = false
        }
    }

    private fun validateConnectionState() {

        communicationService?.takeIf { it.isPortsConnected }?.apply {
            startHandshakingWithThreadSafety()
        } ?: kotlin.run {
            AppUtils.keepScreenAlive(this@MainActivity, false)
        }
    }

    /*
  * This method will start handshaking countdown thread
  */
    private fun startHandshakingWithThreadSafety() {
        Log.i("handshake_check", "in start handshaking thread safety")
        Handler(Looper.getMainLooper()).post {
            try {
                startHandshaking()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun startHandshaking() {
        Log.i("handshake_check", "in start handshaking")
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            if (handshakingTask == null) handshakingTask = HandshakingTask(this)
            if (handshakingTask?.isRunning == true) return

            // case for knob data knob available
            if (isForKnob == true) {
                addEventsForDevelopers(
                    "Start Handshaking For Knob Data Not Available During Standby Case",
                    prefManager?.readUHID().toString()
                )
            }
            // case for spo2 data knob available
            else if (isForKnob == false) {
                addEventsForDevelopers(
                    "Start Handshaking For Battery Data Not Available During Standby Case",
                    prefManager?.readUHID().toString()
                )
            } else if (isForKnob == null) {
                addEventsForDevelopers(
                    "Start Handshaking For Crash During Standby Case",
                    prefManager?.readUHID().toString()
                )
            }

            // starting handshaking
            Handler(Looper.getMainLooper()).postDelayed({
                handshakingTask?.apply {
                    start()
                }
            }, 100)
        }
    }

    /*
     * This method will stop handshaking thread
     */
    private fun stopHandshaking() {
        Log.i("handshake_check", "in stop handshaking")
        isForKnob = null
        handshakingTask?.stop()
    }


    private fun saveTempPrefForExistingVentilation() {
        prefManager?.apply {
            val userid = readCurrentUid().toString()
            tempPrefMapForExistingVentilationInteger["$userid.pref_ventilation_mode"] =
                readVentilationMode()
            tempPrefMapForExistingVentilationFloat["$userid.pref_pip"] = readPip()
            tempPrefMapForExistingVentilationFloat["$userid.pref_vti"] = readVti()
            tempPrefMapForExistingVentilationFloat["$userid.pref_rr"] = readRR()
            tempPrefMapForExistingVentilationFloat["$userid.pref_trig_flow"] = readTrigFlow()
            tempPrefMapForExistingVentilationFloat["$userid.pref_pplat"] = readPplat()
            tempPrefMapForExistingVentilationFloat["$userid.pref_peep"] = readPEEP()
            tempPrefMapForExistingVentilationFloat["$userid.pref_tisnp"] = readTinsp()
            tempPrefMapForExistingVentilationFloat["$userid.pref_support_pressure"] =
                readSupportPressure()
            tempPrefMapForExistingVentilationFloat["$userid.pref_peak_flow"] = readPeakFlow()
            tempPrefMapForExistingVentilationFloat["$userid.pref_fio2"] = readFiO2()
            tempPrefMapForExistingVentilationFloat["$userid.pref_insp_pause"] =
                readInspiratoryPause()
            tempPrefMapForExistingVentilationFloat["$userid.pref_peep_valve"] = readPeepValve()
            tempPrefMapForExistingVentilationFloat["$userid.pref_target_volume"] =
                readTargetVolume()

            tempPrefMapForExistingVentilationFloat["$userid.pref_frequency"] = readFrequency()
            tempPrefMapForExistingVentilationFloat["$userid.pref_flow"] = readFlow()
            tempPrefMapForExistingVentilationFloat["$userid.pref_slope"] = readSlope()
            tempPrefMapForExistingVentilationFloat["$userid.pref_tlow"] = readTlow()
            tempPrefMapForExistingVentilationFloat["$userid.pref_texp"] = readTexp()

            tempPrefMapForExistingVentilationFloat["$userid.pref_apnea_vti"] = readVtApnea()
            tempPrefMapForExistingVentilationFloat["$userid.pref_apnea_rr"] = readRRApnea()
            tempPrefMapForExistingVentilationFloat["$userid.pref_apnea_trig_flow"] =
                readTrigFlowApnea()

        }
    }

    private fun saveTempPrefForExistingVentilationToMainPrefs() {

        prefManager?.apply {
            val userid = readCurrentUid().toString()
            tempPrefMapForExistingVentilationInteger["$userid.pref_ventilation_mode"]?.let {
                setVentilationMode(
                    it
                )
            }
            setPip(tempPrefMapForExistingVentilationFloat["$userid.pref_pip"])
            setVti(tempPrefMapForExistingVentilationFloat["$userid.pref_vti"])
            setRR(tempPrefMapForExistingVentilationFloat["$userid.pref_rr"])
            setTrigFlow(tempPrefMapForExistingVentilationFloat["$userid.pref_trig_flow"])
            setPplat(tempPrefMapForExistingVentilationFloat["$userid.pref_pplat"])
            setPEEP(tempPrefMapForExistingVentilationFloat["$userid.pref_peep"])
            setTinsp(tempPrefMapForExistingVentilationFloat["$userid.pref_tisnp"])
            setSupportPressure(tempPrefMapForExistingVentilationFloat["$userid.pref_support_pressure"])
            setPeakFlow(tempPrefMapForExistingVentilationFloat["$userid.pref_peak_flow"])
            setFiO2(tempPrefMapForExistingVentilationFloat["$userid.pref_fio2"])
            setInspiratoryPause(tempPrefMapForExistingVentilationFloat["$userid.pref_insp_pause"])
            setPeepValve(tempPrefMapForExistingVentilationFloat["$userid.pref_peep_valve"])
            setTargetVolume(tempPrefMapForExistingVentilationFloat["$userid.pref_target_volume"])
            setFrequency(tempPrefMapForExistingVentilationFloat["$userid.pref_frequency"])
            setFlow(tempPrefMapForExistingVentilationFloat["$userid.pref_flow"])
            setSlope(tempPrefMapForExistingVentilationFloat["$userid.pref_slope"])
            setTlow(tempPrefMapForExistingVentilationFloat["$userid.pref_tlow"])
            setTexp(tempPrefMapForExistingVentilationFloat["$userid.pref_texp"])

            setVtApnea(tempPrefMapForExistingVentilationFloat["$userid.pref_apnea_vti"])
            setRRApnea(tempPrefMapForExistingVentilationFloat["$userid.pref_apnea_rr"])
            setTrigFlowApnea(tempPrefMapForExistingVentilationFloat["$userid.pref_apnea_trig_flow"])
            setEtPressure(tempPrefMapForExistingVentilationFloat["$userid.pref_et_pressure"])

        }
    }

    private fun checkSesnsor() {
        binding.layouterrorsensor.visibility = View.GONE
        var i = 0
        CoroutineScope(Dispatchers.Main).launch {
            val list = dataStoreManager?.getStartUpCheckValue()?.first().toString().split(",")

            if (list.size >= 12) {

                try {
                    if (list[1].toFloat() < 3.0f && list[5].toFloat() > 3.0f) {

                    } else if (list[1].toFloat() > 3.0f && list[5].toFloat() < 3.0f) {

                    } else if (list[1].toFloat() > 3.0f && list[5].toFloat() > 3.0f) {

                    } else {
                        ++i
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    Log.i("startupTurbine", "turbine error")
                }


                if (list.get(8) == "1") {

                } else {
                    binding.layouterrorsensor.visibility = View.VISIBLE
                    ++i
                    binding.tvsensor.setText("Expiratory Flow Sensor Failed")
                }
                if (list.get(7) == "1") {
//                tvsensor.setText("Expiratory Flow Sensor Pass")
                } else {
                    binding.layouterrorsensor.visibility = View.VISIBLE
                    ++i
                    binding.tvsensor.setText("Inspiratory Flow Sensor Failed")
                }
                if (list.get(9) == "1") {
//                tvsensor.setText("Inspiratory Pressure Sensor Pass")
                } else {
                    binding.layouterrorsensor.visibility = View.VISIBLE
                    ++i
                    binding.tvsensor.setText("Inspiratory Pressure Sensor Failed")
                }
                if (list.get(10) == "1") {
//                tvsensor.setText("O2 Pressure Sensor Pass")
                } else {
                    binding.layouterrorsensor.visibility = View.VISIBLE
                    ++i
                    binding.tvsensor.setText("O2 Pressure Sensor Failed")
                }

                if (list.get(11) == "1") {

//                tvsensor.setText("O2 Sensor Pass")
                } else {
                    binding.layouterrorsensor.visibility = View.VISIBLE
                    ++i
                    binding.tvsensor.setText("O2 Sensor Failed")
                }
//                if (list.get(12) == "1") {
//
////                tvsensor.setText("Neo Sensor Pass")
//                } else {
//                    layouterrorsensor.visibility = View.VISIBLE
//                    ++i
//                    tvsensor.setText("Neo Sensor Disconnected")
//                }

//                if (list.get(13) == "1") {
//
////                tvsensor.setText("Neo Sensor Pass")
//                } else {
//                    layouterrorsensor.visibility = View.VISIBLE
//                    ++i
//                    tvsensor.setText("Neo Sensor Flow Check Failed")
//                }

                if (i > 1) {
                    binding.tvsensor.text = "${binding.tvsensor.text} + ${--i}"
                }
            }
        }
    }

    private fun showStandbyControlFragment() {

        basicControlParameterList = null
        advancedControlParameterList = null
        backupControlParameterList = null
        smartFio2ControlParameterList = null
        vTasControlParameterList = null
        etCuffControlParameterList = null
        isfromStandby = true

        saveTempPrefForExistingVentilation()

        prefManager?.apply {
            clearProfilePreferences(readCurrentUid())
        }

        VentilatorApp.IERatio =
            prefManager?.readRR()
                ?.let { Configs.calculateIERatio(it.toInt(), prefManager?.readTinsp()) }
                .toString()

        val controlParameters = filterControlParameterViaMode(
            this@MainActivity,
            requestedModeCode,
            getAllControlParameterLists(
                this@MainActivity,
                requestedModeCode
            ).flatten()
        )

        controlParameters[ControlSettingType.BASIC]?.let {
            basicControlParameterList = it
        }
        controlParameters[ControlSettingType.BACKUP]?.let {
            backupControlParameterList = it
        }
        controlParameters[ControlSettingType.ADVANCED]?.let {
            advancedControlParameterList = it
        }
        controlParameters[ControlSettingType.SmartFio2]?.let {
            smartFio2ControlParameterList = it
        }
        controlParameters[ControlSettingType.VTas]?.let {
            vTasControlParameterList = it
        }
        controlParameters[ControlSettingType.EtCuff]?.let {
            etCuffControlParameterList = it
        }
//Inspiratory Termination Tile change
        if (VentilatorApp.selectedOptions == SELECTED_OPTIONS.NON_INVASIVE_NAME) {
            if (requestedModeCode == MODE_NIV_BPAP || requestedModeCode == MODE_NIV_CPAP) {
                advancedControlParameterList?.filter {
                    it.ventKey == LBL_TEXP
                }?.apply {
                    this[0].reading = 75.0f.toInt().toString()
                    prefManager?.setTexp(75.0f)
                }
            } else {
                advancedControlParameterList?.filter {
                    it.ventKey == LBL_TEXP
                }?.apply {
                    if (this.size != 0) {
                        this[0].reading = 75.0f.toInt().toString()
                        prefManager?.setTexp(75.0f)
                    }
                }
            }

        } else {
            if (requestedModeCode == MODE_NIV_BPAP || requestedModeCode == MODE_NIV_CPAP) {
                advancedControlParameterList?.filter {
                    it.ventKey == LBL_TEXP
                }?.apply {
                    this[0].reading = 50.0f.toInt().toString()
                    prefManager?.setTexp(50.0f)
                }
            } else {
                advancedControlParameterList?.filter {
                    it.ventKey == LBL_TEXP
                }?.apply {
                    if (this.size != 0) {
                        this[0].reading = 50.0f.toInt().toString()
                        prefManager?.setTexp(50.0f)
                    }
                }
            }

        }


        prefManager?.apply {
            if (this@MainActivity.lastUhid != readUHID() && (backupControlParameterList == null || backupControlParameterList?.isEmpty() == true)) {
                Log.i("backup_value", "11")
                setIRVStatusTemp(true)
                setApneaSettingsStatusTemp(false)
            } else if (this@MainActivity.lastUhid == readUHID() && (backupControlParameterList == null || backupControlParameterList?.isEmpty() == true)) {
                Log.i("backup_value", "12")
                setIRVStatusTemp(readIRVStatus())
                setApneaSettingsStatusTemp(false)
            } else if (this@MainActivity.lastUhid != readUHID() && (backupControlParameterList != null || backupControlParameterList?.isEmpty() == false)) {
                setIRVStatusTemp(true)
                Log.i("backup_value", "13")
                setApneaSettingsStatusTemp(true)
            } else if (this@MainActivity.lastUhid == readUHID() && (backupControlParameterList != null || backupControlParameterList?.isEmpty() == false)) {
                setIRVStatusTemp(readIRVStatus())
                Log.i("backup_value", "14")
                setApneaSettingsStatusTemp(readApneaSettingsStatusTemp())
            }
        }

        Log.d("List", advancedControlParameterList?.get(0)?.reading.toString())

        standbyControlFragment = basicControlParameterList?.let {
            StandbyControlDialogFragment.newInstance(
                heightSize,
                widthSize,
                false,
                it.toMutableList(),
                advancedControlParameterList?.toMutableList(),
                backupControlParameterList?.toMutableList(),
                smartFio2ControlParameterList?.toMutableList(),
                vTasControlParameterList?.toMutableList(),
                etCuffControlParameterList?.toMutableList(),
                standbyControlFragmentDismissListener,
                this@MainActivity,
                onAdvanceControlParameterClickListener,
                onBackupControlParameterClickListener,
                onSmartFio2ControlParameterClickListener,
                onVTasControlParameterClickListener,
                onEtCuffControlParameterClickListener,
                startNewVentilationListener
            )
        }

        progressDialog?.isCancelable = false
        standbyControlFragment?.isCancelable = false
        standbyControlFragment?.show(
            supportFragmentManager,
            StandbyControlDialogFragment.TAG
        )
    }

    private fun sendNeoSensorCheckCommand() {
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            send(resources.getString(R.string.cmd_neo_sensor_check))
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        binding.etUhid.isCursorVisible = false
        binding.etUhid.setBackgroundColor(resources.getColor(R.color.uhid_grey, null))
        try {
            if (event?.y!! >= 72) {
                binding.etUhid.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
            }

        } catch (e: Error) {
            Log.e("error", "Keyboard issue $e")
        }
        if (prefManager?.readUHID() != FIRST_FILTER_NAME) binding.etUhid.setText(prefManager?.readUHID())

        return true

    }

    private fun qrCode(inputValue: String): Bitmap? {
        val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(inputValue, BarcodeFormat.QR_CODE, 512, 512, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) 0xFF5C5C5C.toInt() else 0xFFFFFFFF.toInt()
                    )
                }
            }
            return bmp
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        return null
    }

    // knob highlight logic starts here

    private var highlightedIndex = 0
    private var visibilityTimeout: CountDownTimer? = null

    @SuppressLint("NotifyDataSetChanged")
    fun highlightViewWithFocus(data: String) {

        startTimeoutWithDebounce()
        Log.i("value_check_tiles", "$highlightedIndex")

        when (data) {
            PREFIX_PLUS -> {
                if (highlightedIndex < 16) highlightedIndex++
                else highlightedIndex = 1

                getViewForFocus(false)?.let { changeConstraintsOfFocusLayout(it) }
            }

            PREFIX_MINUS -> {
                if (highlightedIndex > 1) highlightedIndex--
                else highlightedIndex = 16

                getViewForFocus(true)?.let { changeConstraintsOfFocusLayout(it) }
            }

            PREFIX_AND -> {

                if (highlightedIndex == 6) {
                    binding.includeMale.layoutPanelMale.callOnClick()
                } else if (highlightedIndex == 7) {
                    binding.includeFemale.layoutPanelFemale.callOnClick()
                } else {
                    getViewForFocus(null)?.callOnClick()
                }
            }
        }
    }

    private fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.mainViewPanelMain)
            constraintSet.clear(binding.focusLayoutMain.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutMain.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutMain.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutMain.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelMain)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelMain)
        constraintSet.connect(
            binding.focusLayoutMain.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutMain.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutMain.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutMain.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelMain)
    }

    private fun getViewForFocus(isMinus: Boolean?): View? {


        return when (highlightedIndex) {
            1 -> binding.buttonModes
            2 -> binding.buttonControls
            3 -> binding.buttonPreopCheck
            4 -> binding.buttonServiceCheck
            5 ->
                if (binding.layouterrorsensor.isVisible) {
                    binding.layouterrorsensor
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }

            6 -> binding.includeMale.layoutPanelMale
            7 -> binding.includeFemale.layoutPanelFemale
            8 -> binding.layoutPanelPatientHeightMain
            9 -> binding.layoutPanelPatientAgeMain
            10 -> binding.layoutPanelPatientWeightMain
            11 -> {
                if (binding.buttonStartExistingVentilation.isVisible) {
                    binding.buttonStartExistingVentilation
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            12 -> binding.buttonStartNewVentilation
            13 -> binding.batteryLayout
            14 -> binding.buttonAdult
            15 -> binding.buttonPediatric
            16 -> binding.buttonNeonatal

            else -> null
        }
    }

    fun startTimeoutWithDebounce() {

        cancelTimeout()

        visibilityTimeout = object : CountDownTimer(10000, 2000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                clearPreviousConstraints()
                cancelTimeout()
            }
        }
        visibilityTimeout?.start()
    }

    fun cancelTimeout() {
        if (visibilityTimeout != null) {
            visibilityTimeout?.cancel()
            visibilityTimeout = null
        }
    }

    // knob highlight logic ends here
    private var connectedDevicesLive = 0
    private var mSocket: Socket? = null
    var deviceId = ""
    var isSocketStop = false


    fun returnCommandsToSocket(command: String) {
        CoroutineScope(Dispatchers.IO).launch {
            mSocket?.emit("AndroidSendingCommand", "$deviceId^$command")
        }
    }

    fun sendRangesToSocket(command: String) {
        CoroutineScope(Dispatchers.IO).launch {
            mSocket?.emit("AndroidSendingRange", "$deviceId^$command")
        }
    }

    fun returnDebugCommandsToSocket(debugCommands: String) {
        CoroutineScope(Dispatchers.IO).launch {
            mSocket?.emit("AndroidSendingDebugCommand", "$deviceId^$debugCommands")
        }
    }

    fun sendDebugDataToLiveWindow(debugData: String) {
        CoroutineScope(Dispatchers.IO).launch {
            if (isLiveDataRequest) mSocket?.emit("DataSendingAndroidDebug", "$deviceId^$debugData")
        }
    }

    private fun sendDiagnosticDataToSocket() {

        deviceId = Settings.Secure.getString(
            this@MainActivity.contentResolver, Settings.Secure.ANDROID_ID
        )

        try {
            mSocket = IO.socket(FileLogger.readBaseUrl())
            mSocket?.connect()

            Log.i("Payment_Status", "${mSocket?.connected()}")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        // connect hote hi device id bhejo backend ko
        mSocket?.emit("AndroidStartUp", deviceId)

        mSocket?.on("AndroidReceivingPaymentStatus") {
            CoroutineScope(Dispatchers.Main).launch {
                if (deviceId == it[0].toString().split("^")[0]) {
                    if (it[0].toString().split("^")[1] == "false") {
                        Log.i("Payment_Status", "if ${it[0]}")

                        if (prefManager?.readLockedStatus() == "Unlocked") DialogBoxFactory.showServicePaymentDialog(
                            this@MainActivity,
                            this@MainActivity
                        )

                        isVentiLocked = true
                        prefManager?.saveLockedStatus("Locked")
                        val lockedStatusData = "$deviceId,false,true,${prefManager?.readLockedStatus()}"
                        mSocket?.emit("NodeReceivingLockedStatus", lockedStatusData)

                        if (it[0].toString().split("^")[2].trim().toInt() >= 100) prefManager?.setVentilatorNeedToLock(false) else prefManager?.setVentilatorNeedToLock(true)

                    } else {
                        Log.i("Payment_Status", "else ${it[0]}")

                        if (isVentiLocked) {
                            isVentiLocked = false
                            val calendar = Calendar.getInstance()
                            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
                            FileLogger.writeDispatchDate(this@MainActivity, dayOfYear.toString())
                        }
                        try {
                            DialogBoxFactory.dismissDialogs()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        prefManager?.saveLockedStatus("Unlocked")
                        if (it[0].toString().split("^")[2].toInt() >= 100) prefManager?.setVentilatorNeedToLock(false) else prefManager?.setVentilatorNeedToLock(true)

                        val lockedStatusData = "$deviceId,true,false,${prefManager?.readLockedStatus()}"
                        mSocket?.emit("NodeReceivingLockedStatus", lockedStatusData)
                    }
                }
            }
        }

        mSocket?.on("AndroidReceivingRange") { it1 ->
            if (deviceId == it1[0].toString().split("^")[0]) {
                val ranges = it1[0].toString().split("^")[1]

                CoroutineScope(Dispatchers.Main).launch {
                    systemDialogFragment?.takeIf { it.isVisible }?.apply {
                        sendRangesInDiagnostic(ranges)
                    }
                }
            }
        }

        mSocket?.on("AndroidReceivingDebugCommand") { it1 ->
            if (deviceId == it1[0].toString().split("^")[0]) {
                val debugCommand = it1[0].toString().split("^")[1]

                CoroutineScope(Dispatchers.Main).launch {

                    when (debugCommand) {

                        "Start Debug" -> {

                            systemDialogFragment?.let {

                                if (it.isVisible) {
                                    it.switchBetweenDebugAndDiagnosticWindow("Debug")
                                } else {
                                    highlightButton(binding.buttonPreopCheck)

                                    systemDialogFragment = SystemDialogFragment.newInstance(
                                        heightSize,
                                        widthSize,
                                        false,
                                        fragmentDismissListener,
                                        this@MainActivity,
                                        this@MainActivity,
                                        communicationService
                                    ).apply { show(supportFragmentManager, "Debug") }

                                    systemDialogFragment?.isCancelable = false
                                }

                            } ?: kotlin.run {
                                highlightButton(binding.buttonPreopCheck)

                                systemDialogFragment = SystemDialogFragment.newInstance(
                                    heightSize,
                                    widthSize,
                                    false,
                                    fragmentDismissListener,
                                    this@MainActivity,
                                    this@MainActivity,
                                    communicationService
                                ).apply { show(supportFragmentManager, "Debug") }

                                systemDialogFragment?.isCancelable = false
                            }
                        }

                        "Stop Debug" -> {
                            systemDialogFragment?.takeIf { it.isVisible }?.apply {
                                closeFragment()
                            }
                        }

                        else -> {
                            systemDialogFragment?.takeIf { it.isVisible }?.apply {
                                sendDebugCommandInDebug(debugCommand)
                            }
                        }

                    }
                }
            }
        }

        mSocket?.on("AndroidNodeStart") {

            if (deviceId == it[0].toString().split(",")[0]) {
                connectedDevicesLive += 1
                isSocketStop = false
                isLiveDataRequest = true
            }
        }

        mSocket?.on("AndroidReceiveStop") {
            if (deviceId == it[0].toString()) {
                connectedDevicesLive -= 1
                isSocketStop = true
                isLiveDataRequest = connectedDevicesLive > 0
            }
        }

        // listeners for handle UI online
        mSocket?.on("AndroidReceiveCommand") { it1 ->

            Log.i("Receive", it1[0].toString().split("^")[1])

            CoroutineScope(Dispatchers.Main).launch {
                if (deviceId == it1[0].toString().split("^")[0]) {

                    when (val command = it1[0].toString().split("^")[1]) {

                        "Start Diagnostic" -> {

                            systemDialogFragment?.let {

                                if (it.isVisible) {
                                    it.switchBetweenDebugAndDiagnosticWindow("Diagnostic")
                                } else {
                                    highlightButton(binding.buttonPreopCheck)

                                    systemDialogFragment = SystemDialogFragment.newInstance(

                                        heightSize,
                                        widthSize,
                                        false,
                                        fragmentDismissListener,
                                        this@MainActivity,
                                        this@MainActivity,
                                        communicationService
                                    ).apply { show(supportFragmentManager, "Diagnostic") }

                                    systemDialogFragment?.isCancelable = false
                                }

                            } ?: kotlin.run {
                                highlightButton(binding.buttonPreopCheck)

                                systemDialogFragment = SystemDialogFragment.newInstance(
                                    heightSize,
                                    widthSize,
                                    false,
                                    fragmentDismissListener,
                                    this@MainActivity,
                                    this@MainActivity,
                                    communicationService
                                ).apply { show(supportFragmentManager, "Diagnostic") }

                                systemDialogFragment?.isCancelable = false
                            }
                        }

                        "Stop Diagnostic" -> {

                            systemDialogFragment?.takeIf { it.isVisible }?.apply {
                                closeFragment()
                            }
                        }

                        else -> {
                            systemDialogFragment?.takeIf { it.isVisible }?.apply {
                                sendCommandInDiagnostic(command)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun activateVentilatorListener() {
        DialogBoxFactory.dismissDialogs()

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000L)
            systemDialogFragment = SystemDialogFragment.newInstance(
                heightSize,
                widthSize,
                false,
                fragmentDismissListener,
                this@MainActivity,
                this@MainActivity,
                communicationService
            ).apply { show(supportFragmentManager, "Activate_Ventilator") }

            systemDialogFragment?.isCancelable = false
        }
    }

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        // init preference manager
        binding = ActivityMainBinding.inflate(layoutInflater)
        hideSystemUI()
        setContentView(binding.root)

        prefManager = PreferenceManager(this@MainActivity)
        dataStoreManager = DataStoreManager(this@MainActivity)
        mEventViewModel = ViewModelProvider(this)[EventViewModel::class.java]
        mDebugViewModel = ViewModelProvider(this)[DebugViewModel::class.java]
        mMainActivityViewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        prefManager?.setRebootStatusForHandshake(false)

        VentilatorApp.currentActivityName = "Main"
        CoroutineScope(Dispatchers.Main).launch {
            dataStoreManager?.saveCurrentActivity("Main")
        }

        addEvents("Standby process success", prefManager?.readUHID().toString())
        mMainActivityViewModel.isSocketConnected.distinctUntilChanged()
            .observe(this, Observer { it ->
                it?.let {
                    if (it) mSocket?.emit("DeviceRequestForPaymentStatus", deviceId)
                }
            })
        // status api call
        callRunningStatusApi(RUNNING_STATUS_INACTIVE, ACTIVITY_STANDBY)
        getVentilatorDetailsApi()
        CoroutineScope(Dispatchers.IO).launch {

            FileLogger.readCrashFile().let {
                if (it != dataNotFound) {

                    val result = ServerLogger.d(
                        this@MainActivity,
                        it,
                        "AgvaPro"
                    )
                    if (result) FileLogger.deleteCrashFile()
                }
            }
        }

        sendDiagnosticDataToSocket()
        VentilatorApp.isNebuliserActive = true
        mDiagnosticCheckViewModel = ViewModelProvider(this)[DiagnosticCheckViewModel::class.java]
        mO2RegulationCheckViewModel = ViewModelProvider(this)[O2RegulationCheckViewModel::class.java]

        mMainActivityViewModel.setBAtteryConnectedFlag(false)

        mMainActivityViewModel.batterySystemFailure.distinctUntilChanged()
            .observe(this, Observer { it1 ->

                it1?.let {
                    if (it) {
                        binding.layouterrorsensor.visibility = View.VISIBLE
                        binding.tvsensor.text = "BATTERY SYSTEM FAILURE"
                    } else {
                        //  checkSensor()
                    }
                }
            })

        mMainActivityViewModel.isBatteryConnected.distinctUntilChanged()
            .observe(this, Observer { it ->
                globalCount = 0
                if (it == true) {
                    powerConnected = true
                    binding.powerStatus.setImageDrawable(getDrawable(R.drawable.ic_plug_out))
                    mMainActivityViewModel.ventBatteryLevel.value?.let { it1 ->
                        updateBatteryImage(
                            it1
                        )
                    }
                } else {
                    powerConnected = false
                    binding.powerStatus.setImageDrawable(getDrawable(R.drawable.ic_plug_in))
                    binding.batteryStatus.setImageDrawable(getDrawable(R.drawable.ic_charging))
                }
            })

        mMainActivityViewModel.ventBatteryLevel.distinctUntilChanged()
            .observe(this, androidx.lifecycle.Observer {
                if (mMainActivityViewModel.isBatteryConnected.value == true) {
                    updateBatteryImage(it)
                }
            })

        lastUhidEvents = prefManager?.readUHID().toString()
        lastUhid = prefManager?.readUHID().toString()

        neoSensorObserve()
        initView()
        setNeoButtonHighlight()
        setOnClickListener()
        doBindService()
        checkPatientTypeAndHightlightSEV()
        normaliseButtons()
        disablePresence()

        val deviceId = Settings.Secure.getString(this@MainActivity.contentResolver, Settings.Secure.ANDROID_ID)
        val input = "https://wa.me/7330405060?text=Hi, i need support for this ventilator id - +${deviceId}"

        val qrCodeBitmap = qrCode(input)
        binding.qrCodeStandby.setImageBitmap(qrCodeBitmap)

        //Location Update
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(
                applicationContext
            )
        )

        locationClient.getLocationUpdates(5000L)
            .catch { e ->
                {
                    e.printStackTrace()
                    Log.i("check_location", e.toString())
                }
            }
            .onEach { location ->
                location.extras

                latitude = location.latitude
                logitude = location.longitude
                Log.i("check_location", "$latitude $logitude")
                serviceScope.cancel()
                CoroutineScope(Dispatchers.IO).launch {

                    if (ServerLogger.sendLocationRequest(
                            LocationFilter(this@MainActivity).getFullAddress(
                                latitude,
                                logitude
                            )
                        )
                    ) ServerLogger.sendLocationRequest(
                        LocationFilter(this@MainActivity).getFullAddress(
                            latitude,
                            logitude
                        )
                    )
                }
            }
            .launchIn(serviceScope)

        // set op hours in file
        setOpHoursInFile()
        setServiceHoursInFile()

        val updateHelper = UpdateHelper(this, this, this)
        updateHelper.checkForUpdates(this)

        if (!prefManager?.readUHID().equals(FIRST_FILTER_NAME)) binding.etUhid.setText(prefManager?.readUHID())

        binding.etUhid.setOnClickListener { it ->
            binding.etUhid.isCursorVisible = true
            binding.etUhid.setBackgroundColor(resources.getColor(R.color.white, null))
        }

        binding.etUhid.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_DONE) {
                    if (binding.etUhid.text.toString().length <= 10) {
                        prefManager?.setUHID(binding.etUhid.text.toString())
                        binding.etUhid.isCursorVisible = false
                        binding.etUhid.setBackgroundColor(
                            resources.getColor(
                                R.color.uhid_grey,
                                null
                            )
                        )
                    } else {
                        if (prefManager?.readUHID() != null) {
                        } else {
                            ToastFactory.custom(
                                this@MainActivity,
                                "Invalid UHID. Please Re-enter"
                            )
                        }
                        binding.etUhid.isCursorVisible = false
                        binding.etUhid.setBackgroundColor(
                            resources.getColor(
                                R.color.uhid_grey,
                                null
                            )
                        )
                    }
                    AppUtils.hideKeyBoard(this@MainActivity, binding.etUhid)
                    if (prefManager?.readUHID() != FIRST_FILTER_NAME) binding.etUhid.setText(
                        prefManager?.readUHID()
                    )
                    return true
                }
                return false
            }
        })

        normalizeProgressBars()

        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        am.setStreamVolume(
            AudioManager.STREAM_ALARM,
            am.getStreamMaxVolume(AudioManager.STREAM_ALARM),
            0
        )
        mMainActivityViewModel.OPHours.value = calculateOperationalHourInTime()
        mMainActivityViewModel.serviceHours.value = calculateServiceHourInTime()
    }

    private fun setOpHoursInFile() {
        try {
            // second installation time conditions
            if (prefManager?.readDashBoardRunningTime() == 0L) {
                val data = FileLogger.readOPFile()
                if (data != dataNotFound) {
                    prefManager?.setDashBoardRunningTime(data.toLong())
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                FileLogger.writeOPFile(prefManager?.readDashBoardRunningTime().toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setServiceHoursInFile() {
        try {
            // second installation time conditions
            if (prefManager?.readDashBoardRunningTimeForService() == 0L) {
                val data = FileLogger.readServiceFile()
                if (data != dataNotFound) {
                    prefManager?.setDashBoardRunningTimeForService(data.toLong())
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                FileLogger.writeServiceFile(
                    prefManager?.readDashBoardRunningTimeForService().toString()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("HardwareIds")
    private fun getVentilatorDetailsApi() {
        CoroutineScope(Dispatchers.IO).launch {

            val deviceId = Settings.Secure.getString(
                this@MainActivity.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            val response = ServerLogger.getVentiDetailsRequest(deviceId)
            response?.let {
                prefManager?.saveVentiDetails("${it.data.Ward_No},${it.data.Hospital_Name},${it.data.Department_Name}")
            }
        }

        if (prefManager?.readLockedStatus() != "Unlocked") {
            DialogBoxFactory.showServicePaymentDialog(this@MainActivity, this@MainActivity)

            isVentiLocked = true
            val lockedStatusData = "$deviceId,false,true,${prefManager?.readLockedStatus()}"
            mSocket?.emit("NodeReceivingLockedStatus", lockedStatusData)

            prefManager?.saveLockedStatus(prefManager?.readLockedStatus())
        }
    }

    private fun updateBatteryImage(batteryLevel: Int) {

        if (batteryLevel in 76..100) {
            binding.batteryStatus.setImageResource(R.drawable.ic_battery_full)
        } else if (batteryLevel in 51..75) {
            binding.batteryStatus.setImageResource(R.drawable.ic_threefourth)
        } else if (batteryLevel in 26..50) {
            binding.batteryStatus.setImageResource(R.drawable.ic_battery_half)
        } else if (batteryLevel in 0..25) {
            binding.batteryStatus.setImageResource(R.drawable.ic_battery_low)
            addEvents("Battery Critically Low", prefManager?.readUHID().toString())
            DialogBoxFactory.showBatteryCriticallyLowStatusDialog(
                "Ventilator will shutdown anytime,For patient's safety please connect the ventilator to AC source.",
                ctx
            )
        }
    }

    private fun callRunningStatusApi(status: String, deviceStatus: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val request = StatusRequestModel()
            dataStoreManager?.apply {
                request.apply {
                    this.did = Settings.Secure.getString(
                        this@MainActivity.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    this.deviceStatus = deviceStatus
                    this.message = status
                    this.last_hours = calculateTotalAndLastHours(getLastHours().first().toLong())
                    this.total_hours = calculateTotalAndLastHours(getTotalHours().first().toLong())
                    this.health = " Good"
                    this.address = LocationFilter(this@MainActivity).getAddress(
                        latitude,
                        logitude
                    )
                }
                Log.i("value_check_hours", "INVENTILATION $request")
                if (!ServerLogger.sendStatusRequest(request)) ServerLogger.sendStatusRequest(
                    request
                )
            }
        }
    }

    override fun onUpdateCheckListener(urlApp: String) {
        downloadController = DownloadController(this, urlApp, true)
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000L)
            checkStoragePermission()
        }
    }

    private fun checkStoragePermission() {
        // Check if the storage permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // start downloading
            Log.i("testing_ota", "in check storage")
            downloadController.enqueueDownload()
        } else {
            // Permission is missing and must be requested.
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

        } else {
            requestPermissionsCompat(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                SplashActivity.PERMISSION_REQUEST_STORAGE
            )
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun createAlertBox(destination: String, msg: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)

        builder.setMessage("Due you want to install the updated file..")
        builder.setTitle(msg)

        builder.setIcon(R.drawable.ic_info_param)
        builder.setCancelable(false)

        builder.setPositiveButton(
            "Install",
            DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                // When the user click yes button dialog box also be cancelled
                // check storage permission granted if yes then start downloading file
                DownloadController(this@MainActivity, "", false).installApk(destination)
                dialog.cancel()
            } as DialogInterface.OnClickListener)

        builder.setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                // If user click no then dialog box is cancelled.
                dialog.cancel()
            } as DialogInterface.OnClickListener)

        val alertDialog: AlertDialog = builder.create()

        // Show the Alert Dialog box
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setTextColor(resources.getColor(R.color.white))
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            .setTextColor(resources.getColor(R.color.white))
        alertDialog.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SplashActivity.PERMISSION_REQUEST_STORAGE || requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // start downloading
                ToastFactory.custom(ctx, "Permission Granted")
                downloadController.enqueueDownload()
            } else {
                ToastFactory.custom(ctx, "Permission Denied")
            }
        }
    }

    //Service hours in hours and minutes.
    @SuppressLint("DefaultLocale")
    private fun calculateServiceHourInTime(): String {
        val totalRunningTime = prefManager?.readDashBoardRunningTimeForService()?.toLong() ?: 0L
        val hr = TimeUnit.MILLISECONDS.toHours(totalRunningTime)
        val min = TimeUnit.MILLISECONDS.toMinutes(totalRunningTime) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(totalRunningTime)
        )
        return String.format(
            "%d hr, %d min",
            hr, min
        )
    }

    //Operational hours in hours and minutes.
    @SuppressLint("DefaultLocale")
    private fun calculateOperationalHourInTime(): String {
        val totalRunningTime = prefManager?.readDashBoardRunningTime()?.toLong() ?: 0L
        val hr = TimeUnit.MILLISECONDS.toHours(totalRunningTime)
        val min = TimeUnit.MILLISECONDS.toMinutes(totalRunningTime) - TimeUnit.HOURS.toMinutes(
            TimeUnit.MILLISECONDS.toHours(totalRunningTime)
        )

        Log.i("value_calculated", min.toString())
        return String.format(
            "%d hr, %d min",
            hr, min
        )
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

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        registerReceiver(connReceiver, getIntentFilter())
        doBindService()
        mMainActivityViewModel.isVentilationInitiatedFromExisting.value = false
    }

    private fun disableNeoButton() {
        binding.buttonNeonatal.apply {
            this.setBackgroundResource(R.drawable.background_light_grey_disable)
            this.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black))
            setPaddingOnButtons()
        }
    }

    private fun disablePresence() {
        listOf<AppCompatButton>(
            binding.buttonControls,

            ).forEach {
            it.setBackgroundResource(R.drawable.background_light_grey_disable)
            it.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
        setPaddingOnButtons()
    }

    private fun isVentilatorInStandby(): Boolean = intent.getBooleanExtra(IS_STAND_BY, false)

    private fun isExistingVentilationModeAvailable(): Boolean {
        val ventMode = prefManager?.readLastVentMode()
        return ventMode != null && isValidVentilatorMode(this@MainActivity, ventMode)
    }

    private fun showCalibrationErrorDialog(dialogDisplayMessage: String) {

        if (calibrationConfirmDialog?.isShowing == true) return

        //     dialogDisplayMessage = this.getString(R.string.calibration_error)
        calibrationConfirmDialog =
            DialogBoxFactory.showCalibrationErrorDialog(
                dialogDisplayMessage,
                this@MainActivity
            ) { ->
                showCalibrationProgress("Error Occured")
                addEvents("Sensor Calibration requested", prefManager?.readUHID().toString())
            }
    }

    private fun showTubeCalibrationDialog(dialogDisplayMessage: String) {

        if (calibrationConfirmDialog?.isShowing() == true) return;
        calibrationConfirmDialog =
            DialogBoxFactory.showTubeDialog(dialogDisplayMessage, this@MainActivity) { ->
                addEvents("Sensor Calibration requested", prefManager?.readUHID().toString())
            }
    }

    private fun showO2RegulationDialog(dialogDisplayMessage: String) {

        if (calibrationConfirmDialog?.isShowing() == true) return;
        calibrationConfirmDialog =
            DialogBoxFactory.showO2RegulationStatusDialog(
                dialogDisplayMessage,
                this@MainActivity
            ) { ->
                addEvents("Sensor Calibration requested", prefManager?.readUHID().toString())
            }
    }

    private fun showStartupCheckDialog(dialogDisplayMessage: String) {

        if (calibrationConfirmDialog?.isShowing() == true) return;
        CoroutineScope(Dispatchers.Main).launch {
            calibrationConfirmDialog = DialogBoxFactory.showStartupCheckDialog(
                dialogDisplayMessage,
                this@MainActivity,
                dataStoreManager?.getStartUpCheckValue()?.first().toString()
            )
        }
        addEvents("Startup Check Calibration requested", prefManager?.readUHID().toString())
        startupCheckDialogFrag = false
    }

    private fun showCalibrationDialog(dialogDisplayMessage: String) {
        if (calibrationConfirmDialog?.isShowing() == true) return;
        calibrationConfirmDialog =
            DialogBoxFactory.showCalibrationStatusDialog(
                dialogDisplayMessage,
                this@MainActivity
            ) { ->
                addEvents("Sensor Calibration requested", prefManager?.readUHID().toString())
            }
    }

    private fun showCalibrationProgress(txt: String) {
        if (calibrationProgress == null) {
            calibrationProgress = ProgressDialog(this@MainActivity)
            calibrationProgress?.setCancelable(false)
        }
        calibrationProgress?.setMessage(txt)
        calibrationProgress?.show()
    }

    private fun initView() {

        binding.includeButtonCalibrate.buttonView.text = "Here"
        binding.includeProgressHeight.paramProgressBar.background =
            AppCompatResources.getDrawable(this, R.drawable.progresscircle)
        binding.includeProgressWeight.paramProgressBar.background =
            AppCompatResources.getDrawable(this, R.drawable.progresscircle)
        binding.includeProgressAge.paramProgressBar.background =
            AppCompatResources.getDrawable(this, R.drawable.progresscircle)

        initViewViaPreferences()

        Log.i("STAND_BY_STATUS", "Status" + isVentilatorInStandby())

        if (isVentilatorInStandby()) {
            binding.textStandBy.visibility = View.VISIBLE
            binding.textTimer.visibility = View.VISIBLE
            binding.textNoVentDelivered.visibility = View.VISIBLE
            binding.layoutPanelMode.visibility = View.VISIBLE
        } else {
            binding.layoutPanelMode.visibility = View.VISIBLE
            binding.textStandBy.visibility = View.VISIBLE
            binding.textNoVentDelivered.visibility = View.VISIBLE
            binding.textTimer.visibility = View.VISIBLE
        }

        binding.includeProgressHeight.textView.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )

        binding.includeProgressWeight.textView.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )

        binding.includeProgressAge.textView.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )

        initViewStandByTime()

    }

    private fun initViewStandByTime() {
        startTime()
    }

    private var isOSReboot = false
    private var isVentiLocked = false


    private fun startTime() {

        val liveData: MutableLiveData<String> = MutableLiveData()
        customCountDownTimer = CustomCountDownTimer(liveData)
        customCountDownTimer?.start(100) //Epoch timestamp
        customCountDownTimer?.mutableLiveData?.observe(this, Observer { counterState ->

            mMainActivityViewModel.isSocketConnected.postValue(mSocket?.connected())

            // lock ventilator manually as per 10 days policy
            val dispatchData = FileLogger.readDispatchDate()

            if (dispatchData != dataNotFound) {
                val calendar = Calendar.getInstance()
                val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

                androidx.media3.common.util.Log.i(
                    "data_Date",
                    "continues check - $isVentiLocked ,${prefManager?.readVentilatorNeedToLock()}" +
                            ",$dayOfYear, $dispatchData"
                )
                if (( (dayOfYear - dispatchData.toInt()) >= 10 && !isVentiLocked) && prefManager?.readVentilatorNeedToLock() == true) {

                    prefManager?.saveLockedStatus("Auto Locked")
                    isVentiLocked = true

                    if (mSocket?.connected() == true) {
                        mSocket?.emit(
                            "DeviceRequestForPaymentStatus",
                            deviceId
                        )
                    } else {
                        try {
                            DialogBoxFactory.dismissDialogs()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        DialogBoxFactory.showServicePaymentDialog(
                            this@MainActivity,
                            this@MainActivity
                        )
                    }
                }
            }

            mDebugViewModel.ackOccurenceLiveData.postValue(ack756Visibility)

            mDebugViewModel.ventiLiveData.value?.let {
                val dataList = it
                if (dataList.size < 17) {
                    dataList.add(ventiData)
                    mDebugViewModel.ventiLiveData.postValue(dataList)
                } else {
                    val newDataList = dataList.subList(1, dataList.size)
                    newDataList.add(ventiData)
                    mDebugViewModel.ventiLiveData.postValue(newDataList)
                }
            } ?: kotlin.run {
                val newDataList = ArrayList<String>()
                newDataList.add(ventiData)
                mDebugViewModel.ventiLiveData.postValue(newDataList)
            }

            mDebugViewModel.hidLiveData.value?.let {

                val dataList = it
                if (dataList.size < 17) {
                    dataList.add(hidData)
                    mDebugViewModel.hidLiveData.postValue(dataList)
                } else {
                    val newDataList = dataList.subList(1, dataList.size)
                    newDataList.add(hidData)
                    mDebugViewModel.hidLiveData.postValue(newDataList)
                }
            } ?: kotlin.run {
                val newDataList = ArrayList<String>()
                newDataList.add(hidData)
                mDebugViewModel.hidLiveData.postValue(newDataList)
            }

            CoroutineScope(Dispatchers.Main).launch {

                if (!isOSReboot) {
                    if (countBatteryData == 10 && dataStoreManager?.getDashRebootStatusFlag()
                            ?.first() == true
                    ) {
                        dataStoreManager?.saveDashRebootStatusFlag(false)
                        addEventsForDevelopers(
                            "Re-Initiate Handshake Due to Battery Data Not Available during ventilation",
                            prefManager?.readUHID().toString()
                        )
                        isForKnob = false
                        onDeviceConnect()

                    } else if (countBatteryData == 10 && dataStoreManager?.getDashRebootStatusFlag()
                            ?.first() == false
                    ) {
                        addEventsForDevelopers(
                            "Showing Alarm Due to Battery Data Not Available during ventilation",
                            prefManager?.readUHID().toString()
                        )
                    }
                    countBatteryData++
                }
            }

            counterState?.let {
                binding.textTimer.text = counterState
                globalCount++
            }
        })
    }

    private fun checkPatientTypeAndHightlightSEV() {

        binding.buttonStartExistingVentilation.visibility =
            if (isExistingVentilationModeAvailable() && currentPatientType == prefManager?.readLastUid()
                    .toString()
            ) View.VISIBLE else View.INVISIBLE


        binding.buttonModes.isEnabled =
            isExistingVentilationModeAvailable() && currentPatientType == prefManager?.readLastUid()
                .toString()

        if (isExistingVentilationModeAvailable() && currentPatientType == prefManager?.readLastUid()
                .toString()
        ) {
            binding.buttonModes.apply {
                setBackgroundResource(R.drawable.background_medium_grey)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black))
                setPaddingOnButtons()
            }
        } else {
            binding.buttonModes.apply {
                setBackgroundResource(R.drawable.background_light_grey_disable)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.disable_grey))
                setPaddingOnButtons()
            }
        }
    }

    private fun initViewViaPreferences() {

        prefManager?.apply {
            readCurrentUid()?.let {
                when (it) {

                    PatientProfile.TYPE_ADULT -> {
                        highlightProfiles(binding.buttonAdult)
                        currentPatientType = PatientProfile.TYPE_ADULT.toString()
                        checkPatientTypeAndHightlightSEV()
                        if (isExistingVentilationModeAvailable() && currentPatientType == prefManager?.readCurrentUid()
                                .toString()
                        ) View.VISIBLE else View.INVISIBLE
                        binding.includeProgressHeight.paramProgressBar.maxProgress =
                            PATIENT_ADULT_HEIGHT_UPPER.toDouble()
                        binding.includeProgressAge.paramProgressBar.maxProgress =
                            PATIENT_AGE_UPPER.toDouble()
                        binding.includeProgressWeight.paramProgressBar.maxProgress =
                            PATIENT_ADULT_WEIGHT_UPPER.toDouble()
                    }

                    PatientProfile.TYPE_PED -> {
                        highlightProfiles(binding.buttonPediatric)
                        currentPatientType = PatientProfile.TYPE_PED.toString()
                        checkPatientTypeAndHightlightSEV()
                        binding.includeProgressHeight.paramProgressBar.maxProgress =
                            PED_HEIGHT_UPPER.toDouble()
                        binding.includeProgressAge.paramProgressBar.maxProgress =
                            PATIENT_AGE_UPPER.toDouble()
                        binding.includeProgressWeight.paramProgressBar.maxProgress =
                            PED_WEIGHT_UPPER.toDouble()
                    }

                    PatientProfile.TYPE_NEONAT -> {
                        if (readNeoNateActiveStatus()) highlightProfiles(binding.buttonNeonatal)
                        currentPatientType = PatientProfile.TYPE_NEONAT.toString()
                        checkPatientTypeAndHightlightSEV()
                        binding.includeProgressHeight.paramProgressBar.maxProgress =
                            NEO_HEIGHT_UPPER.toDouble()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) binding.includeProgressAge.paramProgressBar.maxProgress =
                            NEO_AGE_UPPER.toDouble()
                        binding.includeProgressAge.paramProgressBar.maxProgress =
                            NEO_AGE_UPPER.toDouble()
                        binding.includeProgressWeight.paramProgressBar.maxProgress =
                            NEO_WEIGHT_UPPER.toDouble()
                    }
                }
            }

            initBodyParamsViaPreferences()

            if (Gender.TYPE_MALE == readGender()) setDataMale() else setDataFemale()
            if (isExistingVentilationModeAvailable()) setExistingVentilationMode(readLastVentMode())

            binding.checkMode.visibility =
                if (isExistingVentilationModeAvailable()) View.VISIBLE else View.INVISIBLE
            binding.existinglabel.visibility =
                if (isExistingVentilationModeAvailable()) View.VISIBLE else View.INVISIBLE
            if (isExistingVentilationModeAvailable() && currentPatientType == prefManager?.readCurrentUid()
                    .toString()
            ) View.VISIBLE else View.INVISIBLE
            binding.layoutPanelMode.visibility =
                if (isExistingVentilationModeAvailable()) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun initBodyParamsViaPreferences() {

        val textV = findViewById<TextView>(R.id.age)
        if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
            textV.text = "Days"
        } else {
            textV.text = "Years"
        }

        prefManager?.apply {

            if (readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                readBodyWeight()?.toDouble()?.let {
                    binding.includeProgressWeight.paramProgressBar.setCurrentProgress(it)
                    binding.includeProgressWeight.textView.text =
                        String.format("%.1f", it.toFloat())
                }
            } else {
                readBodyWeight()?.toDouble()?.toInt()?.let {
                    binding.includeProgressWeight.paramProgressBar.setCurrentProgress(it.toDouble())
                    binding.includeProgressWeight.textView.text = it.toString()
                }
            }

            readBodyHeight()?.toDouble()?.toInt()?.let {
                binding.includeProgressHeight.paramProgressBar.setCurrentProgress(it.toDouble())
                binding.includeProgressHeight.textView.text = it.toString()
            }

            readAge()?.toDouble()?.toInt()?.let {
                binding.includeProgressAge.paramProgressBar.setCurrentProgress(it.toDouble())
                binding.includeProgressAge.textView.text = it.toString()
            }
        }
    }

    private fun setNeoButtonHighlight() {
        if (prefManager?.readNeoNateActiveStatus() == true && prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
            normalizeNeoBtn()
            highlightButton(binding.buttonNeonatal)
        }
    }

    private fun neoSensorObserve() {
        mMainActivityViewModel.isNeoNatalSensorConnected.observe(this)
        {
            if (it == true || prefManager?.readNeoNateActiveStatus() == true) {
                normalizeNeoBtn()
                if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    highlightButton(binding.buttonNeonatal)
                }
            } else {
                disableNeoButton()
            }
        }
    }

    private fun setOnClickListener() {

        prefManager?.readVentilationMode()?.apply {
            binding.buttonModes.isEnabled = isValidVentilatorMode(this@MainActivity, this)
        }
        binding.includeButtonCalibrate.buttonView.setOnClickListener(this)
        binding.buttonControls.isClickable = true
        binding.buttonAdult.isClickable = true
        binding.buttonPediatric.isClickable = true
        binding.batteryLayout.setOnClickListener(this)
        binding.layouterrorsensor.setOnClickListener(this)
        binding.layoutPanelPatientHeightMain.setOnClickListener(this)
        binding.layoutPanelPatientWeightMain.setOnClickListener(this)
        binding.layoutPanelPatientAgeMain.setOnClickListener(this)
        binding.includeMale.layoutPanelMale.setOnClickListener(this)
        binding.includeFemale.layoutPanelFemale.setOnClickListener(this)
        binding.buttonModes.setOnClickListener(this)
        binding.buttonControls.setOnClickListener(this)
        binding.buttonPreopCheck.setOnClickListener(this)
        binding.buttonServiceCheck.setOnClickListener(this)
        binding.buttonAdult.setOnClickListener(this)
        binding.buttonPediatric.setOnClickListener(this)
        binding.buttonNeonatal.setOnClickListener(this)
        binding.buttonStartExistingVentilation.setOnClickListener(this)
        binding.buttonStartNewVentilation.setOnClickListener(this)

//        serviceLayout.setOnClickListener {
//
//            try {
//                DialogBoxFactory.dismissDialogs()
//            }catch (e:Exception){
//                e.printStackTrace()
//            }
//
//            prefManager?.apply {
//                if (readDashBoardRunningTimeForService() > SERVICE_HOUR_LIMIT){
//                    DialogBoxFactory.showBatteryFailureStatusDialog(this@MainActivity, "Please Contact Customer Support.")
//                }
//                else if (readDashBoardRunningTimeForService() in (SERVICE_HOUR_LIMIT * 0.90).toLong()..SERVICE_HOUR_LIMIT){
//                    DialogBoxFactory.showBatteryFailureStatusDialog(this@MainActivity, "Please Contact Customer Support.")
//                }
//            }
//        }
    }

    private fun setPaddingOnButtons() {

        binding.buttonModes.setPadding(0, 25, 0, 25)
        binding.buttonControls.setPadding(0, 25, 0, 25)
        binding.buttonPreopCheck.setPadding(0, 25, 0, 25)
        binding.buttonServiceCheck.setPadding(0, 25, 0, 25)
        binding.buttonNeonatal.setPadding(0, 25, 0, 25)
        binding.buttonAdult.setPadding(0, 25, 0, 25)
        binding.buttonPediatric.setPadding(0, 25, 0, 25)

        // put all this in xml
        binding.buttonStartExistingVentilation.setPadding(108, 25, 108, 25)
        binding.buttonStartNewVentilation.setPadding(130, 25, 130, 25)
    }

    private fun normalizeProgressBars() {
        binding.includeProgressHeight.paramProgressBar.background =
            ContextCompat.getDrawable(this, R.drawable.progresscircle)
        binding.includeProgressHeight.paramProgressBar.progressColor =
            ContextCompat.getColor(this, R.color.racing_green)
        binding.includeProgressHeight.paramProgressBar.dotColor =
            ContextCompat.getColor(this, R.color.racing_green)
        binding.includeProgressHeight.textView.setTextColor(Color.WHITE)

        binding.includeProgressWeight.paramProgressBar.background =
            ContextCompat.getDrawable(this, R.drawable.progresscircle)
        binding.includeProgressWeight.paramProgressBar.progressColor =
            ContextCompat.getColor(this, R.color.racing_green)
        binding.includeProgressWeight.paramProgressBar.dotColor =
            ContextCompat.getColor(this, R.color.racing_green)
        binding.includeProgressWeight.textView.setTextColor(Color.WHITE)

        binding.includeProgressAge.paramProgressBar.background =
            ContextCompat.getDrawable(this, R.drawable.progresscircle)
        binding.includeProgressAge.paramProgressBar.progressColor =
            ContextCompat.getColor(this, R.color.racing_green)
        binding.includeProgressAge.paramProgressBar.dotColor =
            ContextCompat.getColor(this, R.color.racing_green)
        binding.includeProgressAge.textView.setTextColor(Color.WHITE)
    }

    private fun highlightProgressBar(view: View?) {
        normalizeProgressBars()
        view.let {
            when (it) {
                binding.layoutPanelPatientAgeMain -> {
                    binding.includeProgressAge.paramProgressBar.background =
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.progresscircle_with_selection
                        )
                    binding.includeProgressAge.textView.setTextColor(Color.BLACK)
                }

                binding.layoutPanelPatientWeightMain -> {
                    binding.includeProgressWeight.paramProgressBar.background =
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.progresscircle_with_selection
                        )
                    binding.includeProgressWeight.textView.setTextColor(Color.BLACK)
                }

                binding.layoutPanelPatientHeightMain -> {
                    binding.includeProgressHeight.paramProgressBar.background =
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.progresscircle_with_selection
                        )
                    binding.includeProgressHeight.textView.setTextColor(Color.BLACK)
                }

                else -> {}
            }
        }
    }

    private fun normalizeNeoBtn() {
        binding.buttonNeonatal.apply {
            this.setBackgroundResource(R.drawable.background_medium_grey)
            this.setTextColor(ContextCompat.getColor(ctx, R.color.black))
        }
        setPaddingOnButtons()
    }

    private fun normalizeProfiles() {

        listOf<AppCompatButton>(
            binding.buttonAdult,
            binding.buttonPediatric
        ).forEach {
            it.setBackgroundResource(R.drawable.background_medium_grey)
            it.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
        if (prefManager?.readNeoNateActiveStatus() == true) {
            binding.buttonNeonatal.setBackgroundResource(R.drawable.background_medium_grey)
            binding.buttonNeonatal.setTextColor(ContextCompat.getColor(this, R.color.black))
        }
        setPaddingOnButtons()
    }

    private fun highlightProfiles(btn_profile: AppCompatButton) {
        normalizeProfiles()
        btn_profile.apply {
            setBackgroundResource(R.drawable.background_green_border)
            setTextColor(ContextCompat.getColor(ctx, R.color.white))
            setPaddingOnButtons()
        }
    }

    private fun normaliseButtons() {
        listOf<AppCompatButton>(
            binding.buttonStartExistingVentilation,
            binding.buttonStartNewVentilation,
            binding.buttonPreopCheck
        ).forEach {
            it.setBackgroundResource(R.drawable.background_medium_grey)
            it.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        if (isExistingVentilationModeAvailable() == false && currentPatientType == prefManager?.readLastUid()
                .toString()
        ) {
            binding.buttonModes.apply {
                setBackgroundResource(R.drawable.background_light_grey_disable)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.disable_grey))
                setPaddingOnButtons()
            }
        } else {
            binding.buttonModes.apply {
                setBackgroundResource(R.drawable.background_medium_grey)
                setTextColor(ContextCompat.getColor(this@MainActivity, R.color.black))
                setPaddingOnButtons()
            }

        }
        setPaddingOnButtons()
    }

    private fun setDataMale() {
        binding.includeMale.imageViewMale.setImageResource(R.drawable.ic_male_select)
        binding.includeMale.buttonMale.setBackgroundResource(R.drawable.background_green_border)
        binding.includeMale.buttonMale.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.includeFemale.imageViewFemale.setImageResource(R.drawable.ic_female_unselect)
        binding.includeFemale.buttonFemale.setBackgroundResource(R.drawable.background_medium_grey)
        binding.includeFemale.buttonFemale.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.black
            )
        )
        prefManager?.setGender(Gender.TYPE_MALE)
        prefManager?.setPatientGender(prefManager?.readUHID(), Gender.TYPE_MALE)
        gender = Gender.TYPE_MALE
    }

    private fun setDataFemale() {
        binding.includeMale.imageViewMale.setImageResource(R.drawable.ic_male_unselect)
        binding.includeMale.buttonMale.setBackgroundResource(R.drawable.background_medium_grey)
        binding.includeMale.buttonMale.setTextColor(ContextCompat.getColor(this, R.color.black))
        binding.includeFemale.imageViewFemale.setImageResource(R.drawable.ic_female_select)
        binding.includeFemale.buttonFemale.setBackgroundResource(R.drawable.background_green_border)
        binding.includeFemale.buttonFemale.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.white
            )
        )
        prefManager?.setGender(Gender.TYPE_FEMALE)
        prefManager?.setPatientGender(prefManager?.readUHID(), Gender.TYPE_FEMALE)
        gender = Gender.TYPE_FEMALE
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        heightSize = 800
        widthSize = 1150
        hideSystemUI()
    }

    override fun onStop() {
        unregisterReceiver(connReceiver)
        super.onStop()
    }

    override fun onDestroy() {
        VentilatorApp.isShutDown = false
        VentilatorApp.selectedOptions = null
        binding.progressIndicator.visibility = View.INVISIBLE
        settingsCountDownTimer.safeStop()
        customCountDownTimer?.stop()

        if (!isSocketStop) {
            VentilatorApp.isLiveDataRequest = false
            mSocket?.emit("AndroidStopAuto", deviceId)
        }

        // disconnect krne ke liye bhi command bhejo
        mSocket?.emit("AndroidDisconnect", deviceId)
        mSocket?.disconnect()

        stopPinging()
        doUnbindService()
        super.onDestroy()
    }

    private fun doBindService() {
        if (!isServiceBound) {
            serviceIntent = Intent(this, UsbService::class.java)
            bindService(serviceIntent, mServiceConnection, BIND_AUTO_CREATE)
        }
        startService(serviceIntent)
    }

    private fun doUnbindService() {
        if (isServiceBound) {
            try {
                unbindService(mServiceConnection)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }

        serviceIntent?.let {
            stopService(it)
        }
    }

    override fun onCalibration() {}

    private fun sendConfigurationToVentilatorWithWatchDog() {

        communicationService?.takeIf { it.isPortsConnected }?.apply {
            sendConfigurationToVentilator()
            settingsCountDownTimer.startRunning()
        }
    }

    // remove 38 & 39 mode functionality code as per embedded team concern
    fun sendControlModeToVentilator(mode: Int) {

        communicationService?.takeIf { it.isPortsConnected }?.apply {
            mode.takeIf { isValidVentilatorMode(this@MainActivity, it) }?.let {
                Log.i("CHECK_HERE_MODE", VentilatorApp.selectedOptions.toString())

                if (isExistingVentilation == true) {
                    if (VentilatorApp.selectedOptions == SELECTED_OPTIONS.PRONGS_NAME && (mode != MODE_NC_CPAP && mode != MODE_HFNC)) send(
                        MODE_NC_IPPV.toString()
                    )
                    else if (mode == MODE_PC_PRVC) {
                        send(Configs.MODE_PC_SIMV.toString())
                    } else {
                        send(mode.toString())
                    }
                } else {
                    if (VentilatorApp.selectedOptions == SELECTED_OPTIONS.PRONGS_NAME && (mode != MODE_NC_CPAP && mode != MODE_HFNC)) send(
                        MODE_NC_IPPV.toString()
                    )
                    else if (mode == MODE_PC_PRVC) {
                        send(Configs.MODE_PC_SIMV.toString())
                    } else {
                        send(mode.toString())
                    }
                }
                requestedModeCode = it
            }
        }
    }

    private fun startPinging() {
        communicationService?.apply {
            if (pingingTask == null) pingingTask = PingingTask(this)
            if (pingingTask?.isRunning == false) pingingTask?.start()
        }
    }

    private fun stopPinging() {
        pingingTask?.apply {
            if (isRunning) stop()
        }
    }

    private fun setExistingVentilationMode(readVentilationMode: Int) {
        when (readVentilationMode) {

            MODE_VCV_CMV -> {
                binding.textModeType.text = getString(R.string.hint_vc_cmv)
                binding.checkMode.text = getString(R.string.hint_vc_cmv)
            }

            MODE_VCV_SIMV -> {
                binding.textModeType.text = getString(R.string.hint_vc_simv)
                binding.checkMode.text = getString(R.string.hint_vc_simv)
            }

            MODE_VCV_ACV -> {
                binding.textModeType.text = getString(R.string.hint_vc_cv)
                binding.checkMode.text = getString(R.string.hint_vc_cv)
            }

            MODE_VCV_PRVC -> {
                binding.textModeType.text = getString(R.string.hint_prvc)
                binding.checkMode.text = getString(R.string.hint_prvc)
            }

            MODE_PC_AC -> {
                binding.textModeType.text = getString(R.string.hint_spont)
                binding.checkMode.text = getString(R.string.hint_spont)
            }

            MODE_PC_CMV -> {
                binding.textModeType.text = getString(R.string.hint_pc_cmv)
                binding.checkMode.text = getString(R.string.hint_pc_cmv)
            }

            MODE_PC_ARPV -> {
                binding.textModeType.text = getString(R.string.hint_pc_aprv)
                binding.checkMode.text = getString(R.string.hint_pc_aprv)
            }

            MODE_PC_PRVC -> {
                binding.textModeType.text = getString(R.string.hint_prvc)
                binding.checkMode.text = getString(R.string.hint_prvc)
            }

            MODE_HFNC -> {
                if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    binding.textModeType.text = getString(R.string.NeoNatehfnc)
                    binding.checkMode.text = getString(R.string.NeoNatehfnc)
                } else {
                    binding.textModeType.text = getString(R.string.hfnc)
                    binding.checkMode.text = getString(R.string.hfnc)
                }
            }

            MODE_PC_SIMV -> {
                binding.textModeType.text = getString(R.string.hint_pc_imv)
                binding.checkMode.text = getString(R.string.hint_pc_imv)
            }

            MODE_PC_SPONTANEOUS -> {
                binding.textModeType.text = getString(R.string.hint_spont)
                binding.checkMode.text = getString(R.string.hint_spont)
            }

            MODE_PC_PSV -> {
                binding.textModeType.text = getString(R.string.hint_psv)
                binding.checkMode.text = getString(R.string.hint_psv)
            }

            MODE_AUTO_VENTILATION -> {
                binding.textModeType.text = getString(R.string.hint_ai_vent)
                binding.checkMode.text = getString(R.string.hint_ai_vent)
            }

            MODE_NIV_BPAP -> {
                if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    binding.textModeType.text = getString(R.string.hint_nbpap)
                    binding.checkMode.text = getString(R.string.hint_nbpap)
                } else {
                    binding.textModeType.text = getString(R.string.hint_bpap)
                    binding.checkMode.text = getString(R.string.hint_bpap)
                }
            }

            MODE_NIV_CPAP -> {
                if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    binding.textModeType.text = getString(R.string.hint_ncpap)
                    binding.checkMode.text = getString(R.string.hint_ncpap)
                } else {
                    binding.textModeType.text = getString(R.string.hint_cpap)
                    binding.checkMode.text = getString(R.string.hint_cpap)
                }
            }

            MODE_NIV_NBPAP -> {
                binding.textModeType.text = getString(R.string.hint_nbpap)
                binding.checkMode.text = getString(R.string.hint_nbpap)
            }

            MODE_NC_IPPV -> {
                binding.textModeType.text = getString(R.string.hint_nc_cpap)
                binding.checkMode.text = getString(R.string.hint_nc_cpap)
            }

            MODE_NC_CPAP -> {
                binding.textModeType.text = getString(R.string.hint_ncpap)
                binding.checkMode.text = getString(R.string.hint_ncpap)
            }
        }
    }

    override fun onClick(clickedView: View?) {
        AppUtils.hideKeyBoard(this@MainActivity, binding.etUhid)

        binding.etUhid.isCursorVisible = false
        binding.etUhid.setBackgroundColor(resources.getColor(R.color.uhid_grey, null))

        clickedView?.also { view ->

            when (view) {

                binding.batteryLayout -> {
                    systemDialogFragment = SystemDialogFragment.newInstance(
                        heightSize,
                        widthSize,
                        false,
                        fragmentDismissListener,
                        this,
                        this,
                        communicationService
                    ).apply { show(supportFragmentManager, TAG) }

                    systemDialogFragment?.isCancelable = false
                }

                binding.layoutPanelPatientHeightMain -> {

                    currentButtonID = binding.includeProgressHeight.paramProgressBar

                    var encoder: EncoderValue? = null
                    var param: KnobParameterModel? = null

                    prefManager?.readCurrentUid()?.apply {
                        encoder = when (this) {
                            PatientProfile.TYPE_ADULT -> {
                                EncoderValue(
                                    PATIENT_ADULT_HEIGHT_LOWER.toFloat(),
                                    PATIENT_ADULT_HEIGHT_UPPER.toFloat(),
                                    1.0f
                                )
                            }

                            PatientProfile.TYPE_PED -> {
                                EncoderValue(
                                    PED_HEIGHT_LOWER.toFloat(),
                                    PED_HEIGHT_UPPER.toFloat(),
                                    1.0f
                                )
                            }

                            PatientProfile.TYPE_NEONAT -> {
                                EncoderValue(
                                    NEO_HEIGHT_LOWER.toFloat(),
                                    NEO_HEIGHT_UPPER.toFloat(),
                                    1.0f
                                )
                            }
                        }

                        param = prefManager?.readBodyHeight()?.let { it1 ->
                            KnobParameterModel(
                                Configs.LBL_HEIGHT_KEY,
                                Configs.LBL_HEIGHT_KEY,
                                1,
                                it1,
                                ""
                            )
                        }
                    }
                    highlightProgressBar(view)
                    showKnobForBodyParams(view, param, encoder)
                }

                binding.layoutPanelPatientWeightMain -> {
                    // highlightProgressBar(view)
                    currentButtonID = binding.includeProgressWeight.paramProgressBar

                    var encoder: EncoderValue? = null
                    var param: KnobParameterModel? = null

                    prefManager?.readCurrentUid()?.apply {
                        encoder = when (this) {
                            PatientProfile.TYPE_ADULT -> {
                                EncoderValue(
                                    PATIENT_ADULT_WEIGHT_LOWER.toFloat(),
                                    PATIENT_ADULT_WEIGHT_UPPER.toFloat(),
                                    1.0f
                                )
                            }

                            PatientProfile.TYPE_PED -> {
                                EncoderValue(
                                    PED_WEIGHT_LOWER.toFloat(),
                                    PED_WEIGHT_UPPER.toFloat(),
                                    1.0f
                                )
                            }

                            PatientProfile.TYPE_NEONAT -> {
                                EncoderValue(
                                    NEO_WEIGHT_LOWER,
                                    NEO_WEIGHT_UPPER,
                                    0.1f
                                )
                            }
                        }

                        param = prefManager?.readBodyWeight()?.let { it1 ->
                            KnobParameterModel(
                                Configs.LBL_WEIGHT_KEY,
                                Configs.LBL_WEIGHT_KEY,
                                1,
                                it1,
                                ""
                            )
                        }
                    }

                    highlightProgressBar(view)

                    showKnobForBodyParams(view, param, encoder)
                }

                binding.layoutPanelPatientAgeMain -> {

                    currentButtonID = binding.includeProgressAge.paramProgressBar
                    var encoder: EncoderValue? = null
                    var param: KnobParameterModel? = null

                    prefManager?.readCurrentUid()?.apply {
                        encoder = when (this) {
                            PatientProfile.TYPE_ADULT -> {
                                EncoderValue(
                                    PATIENT_AGE_LOWER.toFloat(),
                                    PATIENT_AGE_UPPER.toFloat(),
                                    1.0f
                                )
                            }

                            PatientProfile.TYPE_PED -> {
                                EncoderValue(
                                    PED_AGE_LOWER.toFloat(),
                                    PED_AGE_UPPER.toFloat(),
                                    1.0f
                                )
                            }

                            PatientProfile.TYPE_NEONAT -> {
                                EncoderValue(
                                    NEO_AGE_LOWER.toFloat(),
                                    NEO_AGE_UPPER.toFloat(),
                                    1.0f
                                )
                            }
                        }

                        param = prefManager?.readAge()?.let { it1 ->
                            KnobParameterModel(
                                Configs.LBL_AGE_KEY,
                                Configs.LBL_AGE_KEY,
                                1,
                                it1,
                                ""
                            )
                        }
                    }

                    highlightProgressBar(view)
                    showKnobForBodyParams(view, param, encoder)
                }

                binding.includeMale.layoutPanelMale -> {
                    setDataMale()
                }

                binding.includeFemale.layoutPanelFemale -> {
                    setDataFemale()
                }

                binding.includeButtonCalibrate.buttonView -> {

                    highlightButton(binding.buttonPreopCheck)
                    communicationService?.takeIf { it.isPortsConnected }
                        ?.apply {
                            send(resources.getString(R.string.cmd_preop))
                        }

                    systemDialogFragment = SystemDialogFragment.newInstance(
                        heightSize,
                        widthSize,
                        false,
                        fragmentDismissListener,
                        this,
                        this,
                        communicationService
                    ).apply {
                        arguments?.putString("CALIBRATE_CIRCUIT", "TouchHere")
                        show(supportFragmentManager, TAG)
                    }

                    systemDialogFragment?.isCancelable = false
                }

                binding.buttonServiceCheck -> {
                    highlightButton(binding.buttonPreopCheck)

                    systemDialogFragment = SystemDialogFragment.newInstance(
                        heightSize,
                        widthSize,
                        false,
                        fragmentDismissListener,
                        this,
                        this,
                        communicationService
                    ).apply { show(supportFragmentManager, "FromSplash") }

                    systemDialogFragment?.isCancelable = false
                }

                binding.buttonPreopCheck -> {
                    highlightButton(binding.buttonPreopCheck)

                    systemDialogFragment = SystemDialogFragment.newInstance(
                        heightSize,
                        widthSize,
                        false,
                        fragmentDismissListener,
                        this,
                        this,
                        communicationService
                    ).apply { show(supportFragmentManager, TAG) }

                    systemDialogFragment?.isCancelable = false
                }

                //modified progressbar code  by masoom on 05 jan 2023
                binding.buttonNeonatal -> {

                    binding.progressIndicator.visibility = View.INVISIBLE
                    if (mMainActivityViewModel.isNeoNatalSensorConnected.value == false || mMainActivityViewModel.isNeoNatalSensorConnected.value == null) {
                        DialogBoxFactory.dismissDialogs()
                        DialogBoxFactory.showNeonateSensorDialog(
                            this@MainActivity,
                            resources.getString(R.string.neonate_sensor_not_connected)
                        )
                    } else {
                        highlightProfiles(binding.buttonNeonatal)

                        val textV = findViewById<TextView>(R.id.age)

                        textV.text = "Days"
                        prefManager?.apply {
                            setCurrentUid(PatientProfile.TYPE_NEONAT)
                            setNeoNateActiveStatus(true)
                            currentPatientType = PatientProfile.TYPE_NEONAT.toString()
                            checkPatientTypeAndHightlightSEV()

                            readBodyHeight()?.toDouble()?.toInt()?.let {
//                            includeProgressHeight.param_progress_bar.setCurrentProgress(it.toDouble())
                                binding.includeProgressHeight.paramProgressBar.setProgress(
                                    it.toDouble(),
                                    NEO_HEIGHT_UPPER.toDouble()
                                )
                                binding.includeProgressHeight.textView.text = it.toString()
                            }
                            readAge()?.toDouble()?.toInt()?.let {
                                binding.includeProgressAge.paramProgressBar.setProgress(
                                    it.toDouble(),
                                    NEO_AGE_UPPER.toDouble()
                                )
                                binding.includeProgressAge.textView.text = it.toString()
                            }
                            readBodyWeight()?.toDouble()?.toFloat()?.let {
                                binding.includeProgressWeight.paramProgressBar.setProgress(
                                    it.toDouble(),
                                    NEO_WEIGHT_UPPER.toDouble()
                                )
                                binding.includeProgressWeight.textView.text =
                                    String.format("%.1f", it)
                            }

                            binding.includeProgressHeight.paramProgressBar.maxProgress =
                                NEO_HEIGHT_UPPER.toDouble()

                            binding.includeProgressAge.paramProgressBar.maxProgress =
                                NEO_AGE_UPPER.toDouble()

                            binding.includeProgressWeight.paramProgressBar.maxProgress =
                                NEO_WEIGHT_UPPER.toDouble()
                        }
                    }
                }

                //modified progressbar code  by masoom on 05 jan 2023
                binding.buttonPediatric -> {
                    disablePresence()
                    highlightProfiles(binding.buttonPediatric)
                    normalizeProgressBars()
                    binding.progressIndicator.visibility = View.INVISIBLE

                    val textV: TextView = findViewById(R.id.age) as TextView
                    textV.setText("Years")
                    prefManager?.apply {
                        setPediatricStatus(true)
                        setNeoNateActiveStatus(false)
                        setCurrentUid(PatientProfile.TYPE_PED)
                        currentPatientType = PatientProfile.TYPE_PED.toString()
                        checkPatientTypeAndHightlightSEV()
                        Log.i("PedStatus", prefManager?.readPediatricStatus().toString())

                        readBodyHeight()?.toDouble()?.toInt()?.let {
                            binding.includeProgressHeight.paramProgressBar.setProgress(
                                it.toDouble(),
                                PED_HEIGHT_UPPER.toDouble()
                            )
                            binding.includeProgressHeight.textView.text = it.toString()
                        }

                        readAge()?.toDouble()?.toInt()?.let {
                            binding.includeProgressAge.paramProgressBar.setProgress(
                                it.toDouble(),
                                PED_AGE_UPPER.toDouble()
                            )
                            binding.includeProgressAge.textView.text = it.toString()
                        }
                        readBodyWeight()?.toDouble()?.toInt()?.let {
                            binding.includeProgressWeight.paramProgressBar.setProgress(
                                it.toDouble(),
                                PED_WEIGHT_UPPER.toDouble()
                            )
                            binding.includeProgressWeight.textView.text = it.toString()
                        }

                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) includeProgressHeight.param_progress_bar.min =
                            PED_HEIGHT_LOWER*/
                        binding.includeProgressHeight.paramProgressBar.maxProgress =
                            PED_HEIGHT_UPPER.toDouble()

                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) includeProgressAge.param_progress_bar.min =
                            PED_AGE_LOWER*/
                        binding.includeProgressAge.paramProgressBar.maxProgress =
                            PED_AGE_UPPER.toDouble()

                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) includeProgressWeight.param_progress_bar.min =
                            PED_WEIGHT_LOWER*/
                        binding.includeProgressWeight.paramProgressBar.maxProgress =
                            PED_WEIGHT_UPPER.toDouble()

                    }
                }

                //modified progressbar code  by masoom on 05 jan 2023
                binding.buttonAdult -> {

                    normalizeProgressBars()
                    disablePresence()
                    highlightProfiles(binding.buttonAdult)
                    binding.progressIndicator.visibility = View.INVISIBLE
                    val textV: TextView = findViewById(R.id.age) as TextView
                    textV.text = "Years"
                    prefManager?.apply {
                        setCurrentUid(PatientProfile.TYPE_ADULT)
                        setPediatricStatus(false)
                        setNeoNateActiveStatus(false)
                        currentPatientType = PatientProfile.TYPE_ADULT.toString()
                        checkPatientTypeAndHightlightSEV()
                        readBodyHeight()?.toDouble()?.toInt()?.let {
                            binding.includeProgressHeight.paramProgressBar.setProgress(
                                it.toDouble(),
                                PATIENT_ADULT_HEIGHT_UPPER.toDouble()
                            )
                            binding.includeProgressHeight.textView.text = it.toString()
                        }
                        readAge()?.toDouble()?.toInt()?.let {
                            binding.includeProgressAge.paramProgressBar.setProgress(
                                it.toDouble(),
                                PATIENT_AGE_UPPER.toDouble()
                            )
                            binding.includeProgressAge.textView.text = it.toString()
                        }

                        readBodyWeight()?.toDouble()?.toInt()?.let {

                            binding.includeProgressWeight.paramProgressBar.setProgress(
                                it.toDouble(),
                                PATIENT_ADULT_WEIGHT_UPPER.toDouble()
                            )
                            binding.includeProgressWeight.textView.text = it.toString()
                        }

                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) includeProgressHeight.param_progress_bar.min =
                            PATIENT_ADULT_HEIGHT_LOWER*/
                        binding.includeProgressHeight.paramProgressBar.maxProgress =
                            PATIENT_ADULT_HEIGHT_UPPER.toDouble()

                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) includeProgressAge.param_progress_bar.min =
                            PATIENT_AGE_LOWER*/
                        binding.includeProgressAge.paramProgressBar.maxProgress =
                            PATIENT_AGE_UPPER.toDouble()

                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) includeProgressWeight.param_progress_bar.min =
                            PATIENT_ADULT_WEIGHT_LOWER*/
                        binding.includeProgressWeight.paramProgressBar.maxProgress =
                            PATIENT_ADULT_WEIGHT_UPPER.toDouble()

                    }

                }


                binding.buttonStartExistingVentilation -> {
                    isExistingVentilation = true

                    VentilatorApp.IERatio =
                        prefManager?.readRR()
                            ?.let {
                                Configs.calculateIERatio(
                                    it.toInt(),
                                    prefManager?.readTinsp()
                                )
                            }.toString()

                    if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                        if (prefManager?.readNeoNateActiveStatus() == true) {
                            binding.progressIndicator.visibility = View.VISIBLE
                            prefManager?.readLastVentMode()?.apply {
                                sendControlModeToVentilator(this)

                                addEvents(
                                    "Ventilation started with existing patient profile ${
                                        Configs.getPatientType(
                                            prefManager
                                        )
                                    } with ${
                                        getVentilatorModeByCode(
                                            this@MainActivity,
                                            this
                                        )?.let { it.modeType }
                                    }", prefManager?.readUHID().toString()
                                )
                                mMainActivityViewModel.isVentilationInitiatedFromExisting.value =
                                    true
                            }
                        } else {
                            ToastFactory.custom(ctx, "Neo Sensor not connected")
                        }
                    } else {
                        binding.progressIndicator.visibility = View.VISIBLE
                        prefManager?.readLastVentMode()?.apply {

                            sendControlModeToVentilator(this)

                            addEvents(
                                "Ventilation started with existing patient profile ${
                                    Configs.getPatientType(
                                        prefManager
                                    )
                                } with ${
                                    getVentilatorModeByCode(
                                        this@MainActivity,
                                        this
                                    )?.let { it.modeType }
                                }",
                                prefManager?.readUHID().toString()
                            )
                            mMainActivityViewModel.isVentilationInitiatedFromExisting.value =
                                true
                        }
                    }
                }

                binding.buttonControls -> {

                    DialogBoxFactory.dismissDialogs()
                    DialogBoxFactory.showNeonateSensorDialog(
                        ctx,
                        resources.getString(R.string.controls_are_desable)
                    )
                }

                binding.layouterrorsensor -> {
                    showStartupCheckDialog("Startup Check Result")
                }

                binding.buttonModes -> {

                    highlightButton(binding.buttonModes)

                    modeDialogFragment = ModeDialogFragment.newInstance(
                        heightSize,
                        widthSize,
                        false,
                        closeListener = fragmentDismissListener,
                        onModeConfirmListener = modeConfirmationListener
                    )

                    modeDialogFragment?.show(supportFragmentManager, "MainActivity")
                    modeDialogFragment?.isCancelable = false

                }


                binding.buttonStartNewVentilation -> {

                    isExistingVentilation = false
                    if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                        if (prefManager?.readNeoNateActiveStatus() == true) {
                            highlightButton(binding.buttonModes)
                            Log.d("currentsModes", currentPatientType)

                            if (isVentilatorInStandby()) {

                                Log.i("new_ventilation", "buttonStartNewVentilation click")
                                communicationService?.takeIf { it.isPortsConnected }
                                    ?.apply {
                                        Log.i(
                                            "new_ventilation",
                                            "buttonStartNewVentilation click inside condition"
                                        )
                                        send(resources.getString(R.string.cmd_vent_wakeup))
                                    }
                            }

                            modeDialogFragment = ModeDialogFragment.newInstance(
                                heightSize,
                                widthSize,
                                false,
                                closeListener = fragmentDismissListener,
                                onModeConfirmListener = modeConfirmationListener
                            )

                            modeDialogFragment?.show(supportFragmentManager, "MainActivity")
                            modeDialogFragment?.isCancelable = false

                        } else {
                            addEvents(
                                "Neo Sensor missing ventilation can't be started",
                                prefManager?.readUHID().toString()
                            )
                            ToastFactory.custom(ctx, "Neo Sensor not connected")
                        }
                    } else {

                        highlightButton(binding.buttonModes)
                        Log.d("currentsModes", currentPatientType)
                        if (isVentilatorInStandby()) {

                            Log.i("new_ventilation", "buttonStartNewVentilation click")
                            communicationService?.takeIf { it.isPortsConnected }
                                ?.apply {
                                    Log.i(
                                        "new_ventilation",
                                        "buttonStartNewVentilation click inside condition"
                                    )
                                    send(resources.getString(R.string.cmd_vent_wakeup))
                                }
                        }

                        modeDialogFragment = ModeDialogFragment.newInstance(
                            heightSize,
                            widthSize,
                            false,
                            closeListener = fragmentDismissListener,
                            onModeConfirmListener = modeConfirmationListener
                        )

                        modeDialogFragment?.show(supportFragmentManager, "MainActivity")
                        modeDialogFragment?.isCancelable = false


                    }


                }

            }


        }
    }


    private fun sendShutDownCommandToVentilator() {
        // ToastFactory.custom(ctx, communicationService.toString())
        communicationService?.send(getString(R.string.cmd_vent_shutdown))
    }

    /*
   * Customized countdown timer for Control settings
   */

    open inner class SettingsCountDownTimer(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {
        private var isSafeStop = false
        private var isRunning = false
        private var isFirstCallElapsed = false
        fun startRunning() {
            if (!isRunning) {
                isSafeStop = false
                isRunning = true
                start()
            }
        }

        fun safeStop() {
            isSafeStop = true
            if (isRunning) cancel()
        }

        override fun onTick(millis: Long) {

            communicationService?.takeIf { it.isPortsConnected }?.apply {
                sendConfigurationToVentilator()
            }
        }

        override fun onFinish() {
            if (!isFirstCallElapsed) true
            isRunning = false
        }
    }


    private fun hideKnob() {
        progressDialog?.dismiss()
        progressDialog = null
        hideGraphicTooltip()
    }

    private fun showGraphicTooltip() {

        hideGraphicTooltip()
        graphicTooltipFragment = GeneralGraphicalToolTipFragment.newInstance(
            heightSize,
            widthSize,
            requestedModeCode,
            false,
            knobTimeoutListener
        )
        graphicTooltipFragment?.show(
            supportFragmentManager,
            GeneralGraphicalToolTipFragment.GGTTTAG
        )
        graphicTooltipFragment?.startTimeoutWithDebounce()
    }

    private fun updateGraphictoolTip(controlParameterModel: ControlParameterModel) {
        graphicTooltipFragment?.takeIf { it.isVisible }?.updateDataOnView(controlParameterModel)
    }

    private fun hideGraphicTooltip() {
        if (graphicTooltipFragment?.isVisible == true) {
            graphicTooltipFragment?.dismiss()
            graphicTooltipFragment = null
        }
    }

    // change here 8 feb
    private fun showKnob(param: KnobParameterModel?, encoder: EncoderValue?) {
        showGraphicTooltip()
        if (param == null || encoder == null) return

        progressDialog = KnobDialog.newInstance(
            onKnobPressListener = object : OnKnobPressListener {

                override fun onKnobPress(previousValue: Float, newValue: Float) {
                    isKnobPressedForControlTile = true
                    basicControlParameterList?.let {
                        selectedBasicPosition?.apply {
                            var lbl = it[this].ventKey
                            val unit = it[this].units

                            updateParameter(lbl, newValue.toString())

                            addEvents(
                                "Set $lbl from ${
                                    String.format(
                                        "%.1f",
                                        previousValue
                                    )
                                }  $unit to $newValue $unit", prefManager?.readUHID().toString()
                            )

                            selectedBasicPosition = null
                            prefManager?.updateParameterViaName(lbl, newValue)
                        }
                    }

                    advancedControlParameterList?.let {
                        selectAdvancedPosition?.apply {
                            var lbl = it[this].ventKey
                            val unit = it[this].units

                            updateParameter(lbl, newValue.toString())

                            addEvents(
                                "Set $lbl from ${
                                    String.format(
                                        "%.1f",
                                        previousValue
                                    )
                                }  $unit to $newValue $unit", prefManager?.readUHID().toString()
                            )

                            selectAdvancedPosition = null
                            prefManager?.updateParameterViaName(lbl, newValue)
                        }
                    }
                    backupControlParameterList?.let {
                        selectedBackupPosition?.apply {
                            val lbl = it[this].ventKey
                            val unit = it[this].units

                            updateParameter(lbl, newValue.toString())


                            addEvents(
                                "Set $lbl from ${
                                    String.format(
                                        "%.1f",
                                        previousValue
                                    )
                                }  $unit to $newValue $unit", prefManager?.readUHID().toString()
                            )

                            selectedBackupPosition = null

                        }
                    }

                    // change here 8 feb
                    smartFio2ControlParameterList?.let {
                        selectSmartFio2Position?.apply {
                            val lbl = it[this].ventKey
                            val unit = it[this].units

                            updateParameter(lbl, newValue.toString())


                            addEvents(
                                "Set $lbl from ${
                                    String.format(
                                        "%.1f",
                                        previousValue
                                    )
                                }  $unit to $newValue $unit", prefManager?.readUHID().toString()
                            )

                            selectSmartFio2Position = null

                        }
                    }

                    // change here 8 feb
                    vTasControlParameterList?.let {
                        selectVtasPosition?.apply {
                            val lbl = it[this].ventKey
                            val unit = it[this].units

                            updateParameter(lbl, newValue.toString())

                            addEvents(
                                "Set $lbl from ${
                                    String.format(
                                        "%.1f",
                                        previousValue
                                    )
                                }  $unit to $newValue $unit", prefManager?.readUHID().toString()
                            )

                            selectVtasPosition = null

                        }
                    }
                    // change here 9 feb 2023
                    etCuffControlParameterList?.let {
                        selectEtCuffPosition?.apply {
                            val lbl = it[this].ventKey
                            val unit = it[this].units

                            updateParameter(lbl, newValue.toString())

                            addEvents(
                                "Set $lbl from ${
                                    String.format(
                                        "%.1f",
                                        previousValue
                                    )
                                }  $unit to $newValue $unit", prefManager?.readUHID().toString()
                            )

                            selectEtCuffPosition = null

                        }
                    }
                    Log.i("adawd", "2")
                    normaliseParameterTiles()
                    hideGraphicTooltip()
                    progressDialog?.takeIf { it.isVisible }?.apply {
                        this.dismiss()
                    }
                    progressDialog = null
                }


            },
            onLimitChangeListener = object : OnLimitChangeListener {
                override fun onLimitChange(previousValue: Float, newValue: Float) {
                    selectedBasicPosition?.let { pos ->
                        basicControlParameterList?.getOrNull(pos)?.apply {
                            updateParameter(ventKey, newValue.toString())
                            updateGraphictoolTip(this)
                        }

                    }

                    selectAdvancedPosition?.let { pos ->
                        advancedControlParameterList?.getOrNull(pos)?.apply {
                            updateParameter(ventKey, newValue.toString())
                        }
                    }
                    selectedBackupPosition?.let { pos ->
                        backupControlParameterList?.getOrNull(pos)?.apply {
                            updateParameter(ventKey, newValue.toString())
                        }
                    }
                    // change here 8 feb
                    selectSmartFio2Position?.let { pos ->
                        smartFio2ControlParameterList?.getOrNull(pos)?.apply {
                            updateParameter(ventKey, newValue.toString())
                        }
                    }

                    selectVtasPosition?.let { pos ->
                        vTasControlParameterList?.getOrNull(pos)?.apply {
                            updateParameter(ventKey, newValue.toString())
                        }
                    }
                    selectEtCuffPosition?.let { pos ->
                        etCuffControlParameterList?.getOrNull(pos)?.apply {
                            updateParameter(ventKey, newValue.toString())
                        }
                    }

                }
            },
            onCloseListener = object : OnDismissDialogListener {
                override fun handleDialogClose() {
                    selectedBasicPosition = null
                    selectAdvancedPosition = null
                    selectedBackupPosition = null
                    selectSmartFio2Position = null
                    selectVtasPosition = null
                    selectEtCuffPosition = null
                    progressDialog?.takeIf { it.isVisible }?.apply {
                        this.dismiss()
                    }
                    normaliseParameterTiles()
                    hideGraphicTooltip()
                    renderControlParameterTilesViaPreference()
                }
            },
            onTimeoutListener = object : OnDismissDialogListener {
                override fun handleDialogClose() {
                    progressDialog?.takeIf { it.isVisible }?.dismiss()
                    normaliseParameterTiles()
                    hideGraphicTooltip()
                    renderControlParameterTilesViaPreference()
                }
            },
            parameterModel = param,
            encoderValue = encoder,
        )

        progressDialog?.apply {
            show(supportFragmentManager, "FromMainActivity")
            startTimeoutWithDebounce()
        }
    }


    private fun showKnobForBodyParams(

        view: View,
        param: KnobParameterModel?,
        encoder: EncoderValue?,


        ) {
        if (param != null && encoder != null) {

            hideKnob()

            progressDialog = KnobDialog.newInstance(
                onKnobPressListener = onBodyParamsKnobPressListener,
                onLimitChangeListener = onBodyParamsLimitChangeListener(view),
                onCloseListener = onBodyParamsCloseListener,
                onTimeoutListener = onBodyParamsTimeoutListener,
                parameterModel = param,
                encoderValue = encoder,
            )
            progressDialog?.apply {
                show(supportFragmentManager, "FromMainActivity")
                startTimeoutWithDebounce()
            }
        }
    }

    //invoked on knob value change
    private fun updateParameter(key: String, value: String) {

        val isDecimalSupported = Configs.supportPrecision(key, value)

        Log.i("valueNuasdasd", "1yg2")

        basicControlParameterList
            ?.filter { it.ventKey == key }
            ?.takeIf { it.isNotEmpty() }
            ?.getOrNull(0)
            ?.apply {
                updateGraphictoolTip(this)
                this.reading = supportPrecision(key, value)
            }

        advancedControlParameterList
            ?.filter { it.ventKey == key }
            ?.takeIf { it.isNotEmpty() }
            ?.getOrNull(0)
            ?.apply {
                this.reading = supportPrecision(key, value)
            }

        backupControlParameterList
            ?.filter { it.ventKey == key }
            ?.takeIf { it.isNotEmpty() }
            ?.getOrNull(0)
            ?.apply {
                this.reading = supportPrecision(key, value)
            }

        // change here 8 feb
        smartFio2ControlParameterList
            ?.filter { it.ventKey == key }
            ?.takeIf { it.isNotEmpty() }
            ?.getOrNull(0)
            ?.apply {
                this.reading = supportPrecision(key, value)
            }

        vTasControlParameterList
            ?.filter { it.ventKey == key }
            ?.takeIf { it.isNotEmpty() }
            ?.getOrNull(0)
            ?.apply {
                this.reading = supportPrecision(key, value)
            }

        etCuffControlParameterList
            ?.filter { it.ventKey == key }
            ?.takeIf { it.isNotEmpty() }
            ?.getOrNull(0)
            ?.apply {
                this.reading = supportPrecision(key, value)
            }

        standbyControlFragment?.notifyParameterAdapter()
    }

    private fun renderControlParameterTilesViaPreference() {
        prefManager?.apply {
            updateParameter(LBL_PEEP, readPEEP().toInt().toString())
            updateParameter(LBL_TRIG_FLOW, readTrigFlow().toString())
            updateParameter(LBL_PPLAT, readPplat().toInt().toString())
            updateParameter(LBL_VTI, readVti().toInt().toString())
            updateParameter(LBL_PIP, readPip().toInt().toString())
            updateParameter(LBL_RR, readRR().toInt().toString())
            updateParameter(LBL_TINSP, readTinsp().toString())
            updateParameter(LBL_FIO2, readFiO2().toInt().toString())
            updateParameter(LBL_SUPPORT_PRESSURE, readSupportPressure().toInt().toString())
            updateParameter(LBL_SLOPE, readSlope().toInt().toString())
            updateParameter(LBL_INSP_PAUSE, readInspiratoryPause().toInt().toString())
            updateParameter(LBL_PEEP_VALVE, readPeepValve().toInt().toString())
            updateParameter(LBL_TARGET_SPO2, readTargetSpo2().toInt().toString())
            updateParameter(LBL_HR_LIMIT, readHrLimit().toInt().toString())
            updateParameter(LBL_TARGET_VOLUME, readTargetVolume().toInt().toString())

            //Frequency
            updateParameter(LBL_FREQUENCY, readFrequency().toString())
            updateParameter(LBL_FLOW, readFlow().toInt().toString())
            updateParameter(LBL_FIO2_DEV, readFiO2Dev().toInt().toString())
            updateParameter(LBL_TLOW, readTlow().toString())
            updateParameter(LBL_APNEA_RR, readRRApnea().toInt().toString())
            updateParameter(LBL_APNEA_VT, readVtApnea().toInt().toString())
            updateParameter(LBL_TAPNEA, readTApnea().toString())
            updateParameter(LBL_APNEA_TRIG_FLOW, readTrigFlowApnea().toInt().toString())
            updateParameter(LBL_ET_PRESSURE, readEtPressure().toInt().toString())
//            updateParameter(LBL_TEXP,readTexp().toInt().toString())
        }

        //Inspiratory Termination tile Change
        prefManager?.apply {
            if (VentilatorApp.selectedOptions == SELECTED_OPTIONS.NON_INVASIVE_NAME) {
                if (requestedModeCode == MODE_NIV_CPAP || requestedModeCode == MODE_NIV_BPAP) {
                    // note : we only need to handle first tym value because every time it receives value from preferences
                    if (isKnobPressedForControlTile) updateParameter(
                        LBL_TEXP,
                        readTexp().toInt().toString()
                    )
                    else updateParameter(
                        LBL_TEXP,
                        75.0f.toInt().toString()
                    )
                } else {
                    if (isKnobPressedForControlTile) updateParameter(
                        LBL_TEXP,
                        readTexp().toInt().toString()
                    )
                    else updateParameter(
                        LBL_TEXP,
                        50.0f.toInt().toString()
                    )
                }
            } else {
                if (requestedModeCode == MODE_NIV_CPAP || requestedModeCode == MODE_NIV_BPAP) {
                    // note : we only need to handle first tym value because every time it receives value from preferences
                    if (isKnobPressedForControlTile) updateParameter(
                        LBL_TEXP,
                        readTexp().toInt().toString()
                    )
                    else updateParameter(
                        LBL_TEXP,
                        75.0f.toInt().toString()
                    )
                } else {
                    if (isKnobPressedForControlTile) updateParameter(
                        LBL_TEXP,
                        readTexp().toInt().toString()
                    )
                    else updateParameter(
                        LBL_TEXP,
                        50.0f.toInt().toString()
                    )
                }
            }
        }
    }

    private fun highlightButton(btn: AppCompatButton) {
        normaliseButtons()
        btn.apply {
            setBackgroundResource(R.drawable.background_green_border)
            setTextColor(ContextCompat.getColor(ctx, R.color.white))
            setPaddingOnButtons()
        }
    }

    override fun onAdjustLoudness(volumeLevel: Float) {
        Log.i("LOUDCHECK", "Adjusted loudness to $volumeLevel")
        mediaPlayer?.apply {
            setVolume(
                volumeLevel / VOLUME_MAX_VALUE,
                volumeLevel / VOLUME_MAX_VALUE
            )
        }
    }

    override fun onCheckLoudness() {
        mediaPlayer?.takeIf { it.isRunning }?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        mediaPlayer = CustomMediaPlayer()

        mediaPlayer?.apply {
            setDataSource(this@MainActivity, Configs.URI_ALARM_HIGH_LEVEL)
            val attrib: AudioAttributes = AudioAttributes.Builder()
                .setLegacyStreamType(AudioManager.STREAM_ALARM)
                .build()
            setAudioAttributes(attrib)
            setAudioAttributes(attrib)
            prepare()
            isLooping = true
            prefManager?.let {
                setVolume(
                    it.readVolume() / VOLUME_MAX_VALUE,
                    it.readVolume() / VOLUME_MAX_VALUE
                )
                start()
            }
        }

        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                mediaPlayer?.release()
                mediaPlayer = null;
            }
        }.start()

    }

    private fun updateModeAndSendParametersToVentilator(modeCode: Int) {

        //Timer starting for receiving ACK after settings send...
//        startAckTimer()
        if (modeCode == MODE_AUTO_VENTILATION) {
            Log.i("CHECK_LOG_HERE", "HERE2")
            prefManager?.apply {
                clearProfilePreferences(readCurrentUid())
                setVentilationMode(modeCode)
                setLastVentMode(modeCode)
                CoroutineScope(Dispatchers.IO).launch {
                    FileLogger.writeModeFile(this@MainActivity, modeCode.toString())
                }
            }
            Configs.getAllControlParameterLists(this@MainActivity, modeCode)
                ?.flatten()
                ?.let {
                    prefManager?.readApneaSettingsStatus()
                        ?.let { it1 ->
                            sendParametersToVentilator(it, it1)
                        }
                }
        } else {
            Log.i("CHECK_LOG_HERE", "HERE3")
            prefManager?.apply {
                setVentilationMode(modeCode)
                setLastVentMode(modeCode)
                CoroutineScope(Dispatchers.IO).launch {
                    FileLogger.writeModeFile(this@MainActivity, modeCode.toString())
                }
            }
            Log.i(
                "check_vent_mode",
                "updateModeAndSendParametersToVentilator else block  ${modeCode.toString()}"
            )

            standbyControlFragment?.apply {
                Log.i("MODE_CHECK", "Calling getAllCOntrolParameters")
                getAllControlParameters().let {
                    prefManager?.readApneaSettingsStatus()
                        ?.let { it1 ->
                            sendParametersToVentilator(it, it1)
                        }
                }
            }
        }
    }


    private fun sendParametersToVentilator(
        parameters: List<ControlParameterModel>,
        apneaStatus: Boolean
    ) {

        parameters.forEach {
            try {
                Log.i(
                    "ONSTARTVENTILATION",
                    "${it.ventKey} updated with value = ${it.reading}"
                )
                prefManager?.updateParameterViaName(
                    it.ventKey,
                    it.reading.toFloat()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i(
                    "ONSTARTVENTILATION",
                    "Unable to parse some parameters to Float"
                )
            }
        }
        prefManager?.setApneaSettingsStatus(apneaStatus)
        sendConfigurationToVentilatorWithWatchDog()
    }


    private fun highlightParameterTile(params: List<ControlParameterModel>, at: Int) {
        if (at < params.size) {
            Log.i("adawd", "6")
            normaliseParameterTiles()
            params.get(at).isIsselected = true

        }
    }

    private val onAdvanceControlParameterClickListener =
        object : ControlParameterClickListener {
            override fun onClick(position: Int, model: ControlParameterModel) {
                advancedControlParameterList?.let {
                    selectedBasicPosition = null
                    selectAdvancedPosition = position
                    selectedBackupPosition = null
                    selectSmartFio2Position = null
                    selectVtasPosition = null
                    selectEtCuffPosition = null
                    val knobModel = KnobParameterModel.fromControlParameter(model)
                    highlightParameterTile(it, position)
                    val encoderValue = EncoderValue(
                        model.lowerLimit.toFloat(),
                        model.upperLimit.toFloat(),
                        model.step.toFloat()
                    )
                    //  showGraphicTooltip()
                    showKnob(knobModel, encoderValue)
                }
                standbyControlFragment?.notifyItemParameterAdapter(position)
            }

            override fun onStateChange(
                isActive: Boolean,
                type: ControlSettingType,
                position: Int
            ) {

                if (type == ControlSettingType.ADVANCED) {
                    if (position == 0) {
                        prefManager?.setIRVStatusTemp(isActive)
                    }
                }
            }
        }

    private val onBackupControlParameterClickListener = object : ControlParameterClickListener {
        override fun onClick(position: Int, model: ControlParameterModel) {
            backupControlParameterList?.let {
                selectedBasicPosition = null
                selectedBackupPosition = position
                selectAdvancedPosition = null
                selectSmartFio2Position = null
                selectVtasPosition = null
                selectEtCuffPosition = null
                val knobModel = KnobParameterModel.fromControlParameter(model)
                highlightParameterTile(it, position)
                val encoderValue = EncoderValue(
                    model.lowerLimit.toFloat(),
                    model.upperLimit.toFloat(),
                    model.step.toFloat()
                )
                // showGraphicTooltip()
                showKnob(knobModel, encoderValue)

            }
            standbyControlFragment?.notifyItemParameterAdapter(position)
        }

        override fun onStateChange(isActive: Boolean, type: ControlSettingType, position: Int) {


            if (type == ControlSettingType.BACKUP) prefManager?.setApneaSettingsStatusTemp(
                isActive
            )

        }

    }

    // change here 8 feb
    private val onSmartFio2ControlParameterClickListener =
        object : ControlParameterClickListener {
            override fun onClick(position: Int, model: ControlParameterModel) {
                smartFio2ControlParameterList?.let {
                    selectedBasicPosition = null
                    selectAdvancedPosition = null
                    selectedBackupPosition = null
                    selectVtasPosition = null
                    selectEtCuffPosition = null
                    selectSmartFio2Position = position
                    val knobModel = KnobParameterModel.fromControlParameter(model)
                    highlightParameterTile(it, position)
                    val encoderValue = EncoderValue(
                        model.lowerLimit.toFloat(),
                        model.upperLimit.toFloat(),
                        model.step.toFloat()
                    )
                    //  showGraphicTooltip()
                    showKnob(knobModel, encoderValue)

                }
                standbyControlFragment?.notifyItemParameterAdapter(position)
            }

            override fun onStateChange(
                isActive: Boolean,
                type: ControlSettingType,
                position: Int
            ) {
                if (type == ControlSettingType.SmartFio2) prefManager?.setSmartFiO2StatusTemp(
                    isActive
                )
            }
        }

    // change here 8 feb
    private val onVTasControlParameterClickListener = object : ControlParameterClickListener {
        override fun onClick(position: Int, model: ControlParameterModel) {
            vTasControlParameterList?.let {
                selectedBasicPosition = null
                selectAdvancedPosition = null
                selectedBackupPosition = null
                selectVtasPosition = position
                selectSmartFio2Position = null
                selectEtCuffPosition = null
                val knobModel = KnobParameterModel.fromControlParameter(model)
                highlightParameterTile(it, position)
                val encoderValue = EncoderValue(
                    model.lowerLimit.toFloat(),
                    model.upperLimit.toFloat(),
                    model.step.toFloat()
                )
                //  showGraphicTooltip()
                showKnob(knobModel, encoderValue)

            }
            standbyControlFragment?.notifyItemParameterAdapter(position)
        }

        override fun onStateChange(isActive: Boolean, type: ControlSettingType, position: Int) {
            if (type == ControlSettingType.VTas) prefManager?.setVGVStatusTemp(isActive)
        }
    }

    // change here 8 feb
    private val onEtCuffControlParameterClickListener = object : ControlParameterClickListener {
        override fun onClick(position: Int, model: ControlParameterModel) {
            etCuffControlParameterList?.let {
                selectedBasicPosition = null
                selectAdvancedPosition = null
                selectedBackupPosition = null
                selectVtasPosition = null
                selectEtCuffPosition = position
                selectSmartFio2Position = null
                val knobModel = KnobParameterModel.fromControlParameter(model)
                highlightParameterTile(it, position)
                val encoderValue = EncoderValue(
                    model.lowerLimit.toFloat(),
                    model.upperLimit.toFloat(),
                    model.step.toFloat()
                )
                //  showGraphicTooltip()
                showKnob(knobModel, encoderValue)

            }
            standbyControlFragment?.notifyItemParameterAdapter(position)
        }

        override fun onStateChange(isActive: Boolean, type: ControlSettingType, position: Int) {
            if (type == ControlSettingType.EtCuff) {
                if (position == 0) prefManager?.setEtCuffStatusTemp(isActive)
                else prefManager?.setDeflashedStatusTemp(isActive)
            }
        }
    }

    // change here 8 feb
    private fun normaliseParameterTiles() {
        basicControlParameterList?.forEach {
            it.isIsselected = false
        }
        backupControlParameterList?.forEach {
            it.isIsselected = false
        }
        advancedControlParameterList?.forEach {
            it.isIsselected = false
        }
        smartFio2ControlParameterList?.forEach {
            it.isIsselected = false
        }
        vTasControlParameterList?.forEach {
            it.isIsselected = false
        }
        etCuffControlParameterList?.forEach {
            it.isIsselected = false
        }
        standbyControlFragment?.notifyParameterAdapter()
    }

    //This method is being invoked by knob for body parameters only
    override fun onClick(position: Int, model: ControlParameterModel) {

        basicControlParameterList?.let {
            selectedBasicPosition = position
            val knobModel = KnobParameterModel.fromControlParameter(model)
            highlightParameterTile(it, position)
            val encoderValue = EncoderValue(
                model.lowerLimit.toFloat(),
                model.upperLimit.toFloat(),
                model.step.toFloat()
            )
            //   showGraphicTooltip()
            showKnob(knobModel, encoderValue)

            //showKnobViews(knobModel,encoderValue)
            //standbyControlFragment?.notifyParameterAdapter()
        }
        //standbyControlFragment?.notifyParameterAdapter()
        standbyControlFragment?.notifyItemParameterAdapter(position)
        //  ToastFactory.custom(this@MainActivity,"The clicked position is $position")
    }

    override fun onStateChange(isActive: Boolean, type: ControlSettingType, position: Int) {


        if (type == ControlSettingType.BACKUP) prefManager?.setApneaSettingsStatus(isActive)


    }


    override fun onUpdateBaseUrlListener(baseUrl: String) {
//        CoroutineScope(Dispatchers.IO).launch {
//            FileLogger.writeBaseUrl(this@MainActivity,baseUrl)
//        }
    }
}

