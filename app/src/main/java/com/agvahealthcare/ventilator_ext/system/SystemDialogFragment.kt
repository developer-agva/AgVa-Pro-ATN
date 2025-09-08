package com.agvahealthcare.ventilator_ext.system

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.*
import com.agvahealthcare.ventilator_ext.hl7comm.HL7CommunicationFragment
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.advanced.AdvancedCalibrationFragment
import com.agvahealthcare.ventilator_ext.system.configuration.ConfigFragment
import com.agvahealthcare.ventilator_ext.system.debug.DebugFragment
import com.agvahealthcare.ventilator_ext.system.device_update.DeviceUpdateFragment
import com.agvahealthcare.ventilator_ext.system.diagnosticCheck.DiagnosticCheckFragment
import com.agvahealthcare.ventilator_ext.system.info.InfoFragment
import com.agvahealthcare.ventilator_ext.system.network.NetworkFragment
import com.agvahealthcare.ventilator_ext.system.o2Regulation.O2RegulationFragment
import com.agvahealthcare.ventilator_ext.system.ota.OTAFragment
import com.agvahealthcare.ventilator_ext.system.selftest.SelfTestFragment
import com.agvahealthcare.ventilator_ext.system.services.ServiceFragment
import com.agvahealthcare.ventilator_ext.system.settings.SettingFragment
import com.agvahealthcare.ventilator_ext.system.test_calib.TestCalibrationFragment
import com.agvahealthcare.ventilator_ext.system.tube.TubeDiaFragment
import com.agvahealthcare.ventilator_ext.system.wifi.WiFiFragment
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.replaceFragment
import com.agvahealthcare.ventilator_ext.utility.setHeightWidthPercent
import kotlinx.android.synthetic.main.fragment_system_dialog.imageViewCrossSystem
import kotlinx.android.synthetic.main.fragment_system_dialog.recyclerViewSystem

enum class SystemFragmentButtonTypes {
    Info,
    Startup,
    Settings,
    Test_Calibrations,
    Tube,
    Service,
    HL7,
    Advanced_Calibrations,
    Device_Update,
    Ota,
    Wifi,
    Debug,
    Diagnos,
    O2_Reg,
    Network_Info,
    Config,
    Self_Test
}

data class SystemButtonModelClass(
    var title: String,
    var types: SystemFragmentButtonTypes
)

interface SystemFragmentButtonListener {
    fun doSystemButtonClick(buttonType: SystemFragmentButtonTypes)
    fun doSystemButtonLongClick(buttonType: SystemFragmentButtonTypes)
}

class SystemDialogFragment : DialogFragment(), PasswordCallbackListener, SimpleCallbackListener,
    SystemFragmentButtonListener {
    private var preferenceManager: PreferenceManager? = null

    companion object {
        var TAG = "SystemDialog"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"
        private const val KEY_STATUS = "KEY_STATUS"

        fun newInstance(
            height: Int?,
            width: Int?,
            status: Boolean?,
            closeListener: OnDismissDialogListener?,
            calibrationOxygen: OnCalibrationOxygen?,
            onLoudnessAdjustmentListener: OnLoudnessAdjustmentListener?,
            communicationService: CommunicationService?
        ): SystemDialogFragment {
            val args = Bundle()
            height?.let { args.putInt(KEY_HEIGHT, it) }
            width?.let { args.putInt(KEY_WIDTH, it) }
            status?.let { args.putBoolean(KEY_STATUS, it) }
            val fragment = SystemDialogFragment()
            fragment.arguments = args
            fragment.closeListener = closeListener
            fragment.communicationService = communicationService
            fragment.calibrationOxygen = calibrationOxygen
            fragment.onLoudnessAdjustmentListener = onLoudnessAdjustmentListener
            return fragment
        }
    }

    private var dataListSystemItems = ArrayList<SystemButtonModelClass>()
    private var passWord = ""
    private var closeListener: OnDismissDialogListener? = null
    private var communicationService: CommunicationService? = null
    private var calibrationOxygen: OnCalibrationOxygen? = null
    private var onLoudnessAdjustmentListener: OnLoudnessAdjustmentListener? = null
    private var infoFragment: InfoFragment? = null
    private var configFragment: ConfigFragment? = null
    private var testCalibrationFragment: TestCalibrationFragment? = null
    private var advancedCalibrationFragment: AdvancedCalibrationFragment? = null
    private var hL7CommunicationFragment: HL7CommunicationFragment? = null
    private var startupCheckFragment: StartupCheckFragment? = null
    private var networkFragment: NetworkFragment? = null
    private var tubeDiaFragment: TubeDiaFragment? = null
    private var debugFragment: DebugFragment? = null
    private var settingFragment: SettingFragment? = null
    private var selfTestFragment: SelfTestFragment? = null
    private var diagnosticCheckFragment: DiagnosticCheckFragment? = null
    private var o2RegulationFragment: O2RegulationFragment? = null
    private var serviceFragment: ServiceFragment? = null
    private var updateDeviceFragment: DeviceUpdateFragment? = null
    private var otaFragment: OTAFragment? = null
    private var wifiFragment: WiFiFragment? = null
    private var systemAdapter: SystemDesignAdapter? = null
    fun getTestCalibFragment(): TestCalibrationFragment? = testCalibrationFragment
    fun getaAdvancedFragment(): AdvancedCalibrationFragment? = advancedCalibrationFragment
    fun getUpdateDeviceFragment(): DeviceUpdateFragment? = updateDeviceFragment

    // knob highlight logic starts here
    var sizeOfCurrentArray = 0

    var highlightedIndex = -1
    private var visibilityTimeout: CountDownTimer? = null

    private fun highlightAdapters(highlightedIndex: Int, data: String?) {
        if (testCalibrationFragment != null) testCalibrationFragment?.highlightAdapterPosition(
            highlightedIndex,
            data
        )
        else if (tubeDiaFragment != null) tubeDiaFragment?.highlightAdapterPosition(
            highlightedIndex,
            data
        )
        else if (advancedCalibrationFragment != null) advancedCalibrationFragment?.highlightAdapterPosition(
            highlightedIndex,
            data
        )
        else if (diagnosticCheckFragment != null) diagnosticCheckFragment?.highlightAdapterPosition(
            highlightedIndex,
            data
        )
        else if (o2RegulationFragment != null) o2RegulationFragment?.highlightAdapterPosition(
            highlightedIndex,
            data
        )
        else if (settingFragment != null) settingFragment?.highlightAdapterPosition(
            highlightedIndex,
            data
        )
    }

    private fun handleAdaptersClick(highlightedIndex: Int) {
        if (testCalibrationFragment != null) testCalibrationFragment?.handleClick(highlightedIndex)
        else if (tubeDiaFragment != null) tubeDiaFragment?.handleClick(highlightedIndex)
        else if (advancedCalibrationFragment != null) advancedCalibrationFragment?.handleClick(
            highlightedIndex
        )
        else if (diagnosticCheckFragment != null) diagnosticCheckFragment?.handleClick(
            highlightedIndex
        )
        else if (o2RegulationFragment != null) o2RegulationFragment?.handleClick(highlightedIndex)
        else if (settingFragment != null) settingFragment?.handleClick(highlightedIndex)
    }

    private fun makeAllFragmentsNull() {
        infoFragment = null
        configFragment = null
        settingFragment = null
        startupCheckFragment = null
        testCalibrationFragment = null
        tubeDiaFragment = null
        diagnosticCheckFragment = null
        o2RegulationFragment = null
        advancedCalibrationFragment = null
        networkFragment = null
        serviceFragment = null
        updateDeviceFragment = null
        debugFragment = null
        otaFragment = null
        wifiFragment = null
        hL7CommunicationFragment = null
        selfTestFragment = null
    }

    private fun showhl7CommunicationFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (hL7CommunicationFragment == null)
            hL7CommunicationFragment = HL7CommunicationFragment()

        hL7CommunicationFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
    }

    private fun showSelfTestFragment(communicationService: CommunicationService?) {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (selfTestFragment == null)
            selfTestFragment = SelfTestFragment(communicationService)

        selfTestFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
    }

    // knob highlight logic ends here
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view =  inflater.inflate(R.layout.fragment_system_dialog, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        TAG = tag.toString()

        Log.i("given_tag",tag.toString())

        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        setUpNavigation()
    }

    // click events handling
    override fun doSystemButtonClick(buttonType: SystemFragmentButtonTypes) {
        when (buttonType) {
            SystemFragmentButtonTypes.Info -> {
                showInfoFragment(communicationService)
            }

            SystemFragmentButtonTypes.Config ->{
                showConfigFragment()
            }

            SystemFragmentButtonTypes.Self_Test -> {
                showSelfTestFragment(communicationService)
            }

            SystemFragmentButtonTypes.Startup -> {
                setupStartupFragment()
            }

            SystemFragmentButtonTypes.Settings -> {
                setupSettingsFragment()
            }

            SystemFragmentButtonTypes.Test_Calibrations -> {
                setupTestCalibFragment()
            }

            SystemFragmentButtonTypes.Tube -> {
                setupTubeFragment()
            }

            SystemFragmentButtonTypes.Service -> {
                showServiceFragment()
            }

            SystemFragmentButtonTypes.HL7 -> {
                showhl7CommunicationFragment()
            }

            SystemFragmentButtonTypes.Diagnos -> {
                showDiagnosticFragment()
            }

            SystemFragmentButtonTypes.O2_Reg -> {
                showO2RegulationFragment()
            }

            SystemFragmentButtonTypes.Advanced_Calibrations -> {
                setupAdvancedFragment()
            }

            SystemFragmentButtonTypes.Device_Update -> {
                showDeviceUpdateFragment()
            }

            SystemFragmentButtonTypes.Network_Info -> {
                showNetworkInfoFragment()
            }

            SystemFragmentButtonTypes.Ota -> {
                setupOtaFragment()
            }

            SystemFragmentButtonTypes.Wifi -> {
                showWifiFragment()
            }

            SystemFragmentButtonTypes.Debug -> {
                setupDebugFragment()
            }
        }
        systemAdapter?.notifyDataSetChanged()
    }

    override fun doSystemButtonLongClick(buttonType: SystemFragmentButtonTypes) {

        when (buttonType) {

            SystemFragmentButtonTypes.Startup -> {
                passWord = "8000"
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNetworkInfoDialog(
                    requireContext(),
                    this,
                    this,
                    passWord,
                    "Enter password to unlock system configuration window"
                )
            }

            SystemFragmentButtonTypes.Info -> {
                if (tag != "FromDashboard") {
                    passWord = "8085"
                    DialogBoxFactory.dismissDialogs()
                    DialogBoxFactory.showInfoResetDialog(
                        requireContext(),
                        this,
                        this,
                        passWord,
                        "Enter password to unlock advanced window"
                    )
                }
            }

            SystemFragmentButtonTypes.Settings -> {
                passWord = "2018"
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNetworkInfoDialog(
                    requireContext(),
                    this,
                    this,
                    passWord,
                    "Enter password to unlock system configuration window"
                )
            }

            else -> {}
        }
    }

    private fun setupAdapter() {
        systemAdapter = SystemDesignAdapter(requireContext(), dataListSystemItems, this)
        recyclerViewSystem.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewSystem.adapter = systemAdapter
    }

    fun switchBetweenDebugAndDiagnosticWindow(tag: String) {
        if (tag == "Diagnostic") {
            showDiagnosticFragment()

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_diagnos),
                        SystemFragmentButtonTypes.Diagnos
                    )
                )
            ) dataListSystemItems.contains(
                SystemButtonModelClass(
                    getString(R.string.hint_diagnos),
                    SystemFragmentButtonTypes.Diagnos
                )
            )

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_reg_o2),
                        SystemFragmentButtonTypes.O2_Reg
                    )
                )
            ) dataListSystemItems.contains(
                SystemButtonModelClass(
                    getString(R.string.hint_reg_o2),
                    SystemFragmentButtonTypes.O2_Reg
                )
            )

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_advanced_calib),
                        SystemFragmentButtonTypes.Advanced_Calibrations
                    )
                )
            ) dataListSystemItems.contains(
                SystemButtonModelClass(
                    getString(R.string.hint_advanced_calib),
                    SystemFragmentButtonTypes.Advanced_Calibrations
                )
            )

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_update),
                        SystemFragmentButtonTypes.Device_Update
                    )
                )
            ) dataListSystemItems.contains(
                SystemButtonModelClass(
                    getString(R.string.hint_update),
                    SystemFragmentButtonTypes.Device_Update
                )
            )

            systemAdapter?.isEnable = true
            systemAdapter?.selectedIndexType = SystemFragmentButtonTypes.Diagnos
            systemAdapter?.updateList(dataListSystemItems)
        }
        else {
            setupDebugFragment()

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.network_info),
                        SystemFragmentButtonTypes.Network_Info
                    )
                )
            ) dataListSystemItems.contains(
                SystemButtonModelClass(
                    getString(R.string.network_info),
                    SystemFragmentButtonTypes.Network_Info
                )
            )
            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_debug),
                        SystemFragmentButtonTypes.Debug
                    )
                )
            ) dataListSystemItems.contains(
                SystemButtonModelClass(
                    getString(R.string.hint_debug),
                    SystemFragmentButtonTypes.Debug
                )
            )
            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.wifi),
                        SystemFragmentButtonTypes.Wifi
                    )
                )
            ) dataListSystemItems.contains(
                SystemButtonModelClass(
                    getString(R.string.wifi),
                    SystemFragmentButtonTypes.Wifi
                )
            )
            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_ota),
                        SystemFragmentButtonTypes.Ota
                    )
                )
            ) dataListSystemItems.contains(
                SystemButtonModelClass(
                    getString(R.string.hint_ota),
                    SystemFragmentButtonTypes.Ota
                )
            )
            systemAdapter?.isEnable = true

            systemAdapter?.selectedIndexType = SystemFragmentButtonTypes.Debug
            systemAdapter?.updateList(dataListSystemItems)
        }
    }

    private fun setUpNavigation() {

        setupAdapter()
        dataListSystemItems.clear()
        dataListSystemItems.add(
            SystemButtonModelClass(
                getString(R.string.hint_info),
                SystemFragmentButtonTypes.Info
            )
        )
        dataListSystemItems.add(
            SystemButtonModelClass(
                getString(R.string.startup),
                SystemFragmentButtonTypes.Startup
            )
        )
        dataListSystemItems.add(
            SystemButtonModelClass(
                getString(R.string.hint_settings),
                SystemFragmentButtonTypes.Settings
            )
        )
        dataListSystemItems.add(
            SystemButtonModelClass(
                getString(R.string.hint_test_calib),
                SystemFragmentButtonTypes.Test_Calibrations
            )
        )
        dataListSystemItems.add(
            SystemButtonModelClass(
                getString(R.string.hint_tube),
                SystemFragmentButtonTypes.Tube
            )
        )
        dataListSystemItems.add(
            SystemButtonModelClass(
                getString(R.string.hint_service),
                SystemFragmentButtonTypes.Service
            )
        )
//        dataListSystemItems.add(
//            SystemButtonModelClass(
//                getString(R.string.hl7),
//                SystemFragmentButtonTypes.HL7
//            )
//        )

//        if (tag == "FromDashboard") dataListSystemItems.removeAt(dataListSystemItems.lastIndex)

        if (tag == "FromSplash") {
            systemAdapter?.selectedIndexType = SystemFragmentButtonTypes.Service
            showServiceFragment()
            systemAdapter?.isEnable = false

        } else if (tag == "Diagnostic") {
            systemAdapter?.selectedIndexType = SystemFragmentButtonTypes.Diagnos
            showDiagnosticFragment()
            dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_diagnos),
                    SystemFragmentButtonTypes.Diagnos
                )
            )
            dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_reg_o2),
                    SystemFragmentButtonTypes.O2_Reg
                )
            )
            dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_advanced_calib),
                    SystemFragmentButtonTypes.Advanced_Calibrations
                )
            )
            dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_update),
                    SystemFragmentButtonTypes.Device_Update
                )
            )
            systemAdapter?.isEnable = true

        } else if (tag == "Debug") {
            systemAdapter?.selectedIndexType = SystemFragmentButtonTypes.Debug
            setupDebugFragment()
            dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.network_info),
                    SystemFragmentButtonTypes.Network_Info
                )
            )
            dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_debug),
                    SystemFragmentButtonTypes.Debug
                )
            )
            dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.wifi),
                    SystemFragmentButtonTypes.Wifi
                )
            )
            dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_ota),
                    SystemFragmentButtonTypes.Ota
                )
            )
            systemAdapter?.isEnable = true
        }

        else if (tag == "Activate_Ventilator"){

            dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.wifi),
                    SystemFragmentButtonTypes.Wifi
                )
            )
            systemAdapter?.isEnable = false
            systemAdapter?.selectedIndexType = SystemFragmentButtonTypes.Wifi
            showWifiFragment()
        }
        else {
            if (arguments?.getString("CALIBRATE_CIRCUIT") == "TouchHere") {
                systemAdapter?.selectedIndexType = SystemFragmentButtonTypes.Tube
                setupTubeFragment()
            } else {
                systemAdapter?.selectedIndexType = SystemFragmentButtonTypes.Info
                showInfoFragment(communicationService)
            }
            systemAdapter?.isEnable = true
        }

        systemAdapter?.updateList(dataListSystemItems)

        imageViewCrossSystem.setOnClickListener {
            closeFragment()
        }
    }

    fun updateSensorTubeComplianceCalibrationViaPreference() {
        tubeDiaFragment?.takeIf { it.isVisible }
            ?.apply {
                this.updateTubeComplianceCalibrationStatus()
            }
    }

    fun updateSensorTubeResistanceCalibrationViaPreference() {
        tubeDiaFragment?.takeIf { it.isVisible }
            ?.apply {
                this.updateTubeResistanceCalibrationStatus()
            }
    }

    fun updateEnableStatus(status: Boolean) {
        systemAdapter?.isEnable = status
        systemAdapter?.notifyDataSetChanged()
    }

    private fun setupTestCalibFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 1
        if (testCalibrationFragment == null)
            testCalibrationFragment = TestCalibrationFragment(communicationService)
        testCalibrationFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
    }

    private fun setupStartupFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (startupCheckFragment == null)
            startupCheckFragment = StartupCheckFragment()
        startupCheckFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
    }

    private fun setupAdvancedFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 3
        if (advancedCalibrationFragment == null)
            advancedCalibrationFragment = AdvancedCalibrationFragment(communicationService)
        advancedCalibrationFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
    }

    private fun setupSettingsFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 4
        if (settingFragment == null)
            settingFragment =
                SettingFragment.newInstance(onLoudnessAdjustmentListener)
        settingFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
    }

    private fun setupOtaFragment() {
        sizeOfCurrentArray = 0
        makeAllFragmentsNull()
        if (otaFragment == null) otaFragment = OTAFragment()
        otaFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
    }

    private fun setupDebugFragment() {
        sizeOfCurrentArray = 0
        makeAllFragmentsNull()
        if (debugFragment == null) debugFragment = DebugFragment()
        debugFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
    }

    private fun setupTubeFragment() {
        sizeOfCurrentArray = 1
        makeAllFragmentsNull()
        if (tubeDiaFragment == null) tubeDiaFragment = TubeDiaFragment(communicationService)
        tubeDiaFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
    }

    private fun showNetworkInfoFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (networkFragment == null)
            networkFragment = NetworkFragment(communicationService)
        networkFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
    }

    private fun showO2RegulationFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 4
        if (o2RegulationFragment == null)
            o2RegulationFragment = O2RegulationFragment(communicationService)
        o2RegulationFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
    }

    fun closeFragment() {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .remove(this)
            .commitNow()
        closeListener?.handleDialogClose()
    }

    private fun showDiagnosticFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 10
        if (diagnosticCheckFragment == null)
            diagnosticCheckFragment = DiagnosticCheckFragment(communicationService)
        diagnosticCheckFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
    }

    fun sendCommandInDiagnostic(command: String) {
        diagnosticCheckFragment?.takeIf { it.isVisible }?.apply {
            getCommandsFromLiveWindow(command)
        }
    }

    fun sendDebugCommandInDebug(command: String) {
        debugFragment?.takeIf { it.isVisible }?.apply {
            getCommandsFromLiveWindow(command)
        }
    }

    fun sendRangesInDiagnostic(ranges: String) {
        diagnosticCheckFragment?.takeIf { it.isVisible }?.apply {
            getRangesFromLiveWindow(ranges)
        }
    }

    private fun showServiceFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (serviceFragment == null)
            serviceFragment = ServiceFragment()
        serviceFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
    }

    private fun showDeviceUpdateFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (updateDeviceFragment == null)
            updateDeviceFragment = DeviceUpdateFragment(communicationService)
        updateDeviceFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
    }

    private fun showInfoFragment(communicationService: CommunicationService?) {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (infoFragment == null)
            infoFragment = InfoFragment(communicationService)
        infoFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
    }

    private fun showConfigFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (configFragment == null)
            configFragment = ConfigFragment()
        configFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
    }

    private fun showWifiFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (wifiFragment == null)
            wifiFragment = WiFiFragment()
        wifiFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
    }

    override fun onStart() {
        super.onStart()
        val heightDialog = arguments?.getInt(KEY_HEIGHT)
        val widthDialog = arguments?.getInt(KEY_WIDTH)
        val isCheck = arguments?.getBoolean(KEY_STATUS)
        setHeightWidthPercent(heightDialog, widthDialog, isCheck)
    }

    fun updateSensorsCalibrationStatusViaPreference() {
        testCalibrationFragment?.takeIf { it.isVisible }?.apply { updateSensorCalibrationStatus() }
        advancedCalibrationFragment?.takeIf { it.isVisible }?.apply { updateSensorCalibrationStatus() }
    }

    fun updateOxygenCalibrateProgressStatus(
        progress: Int,
        msg: String,
        textAlignment: Int
    ) {
        testCalibrationFragment?.takeIf { it.isVisible }?.apply {
            updateOxygenCalibrateProgressStatus(
                progress,
                msg,
                textAlignment
            )
        }
    }

    fun setSoftWareUpdate(softwareUpdate: String?) {
        infoFragment?.takeIf { it.isVisible }?.apply { setSoftWareUpdate(softwareUpdate) }
    }

    fun updateXValue(data: String?) {
        networkFragment?.takeIf { it.isVisible }?.apply { updateKnobRawData(data.toString()) }
    }

    fun updateKnob(data: String?) {
        settingFragment?.takeIf { it.isVisible }?.apply { updateKnobSetting(data.toString()) }
        networkFragment?.takeIf { it.isVisible }?.apply { updateKnobRawData(data.toString()) }
        diagnosticCheckFragment?.takeIf { it.isVisible }?.apply { updateValueOnKnobChange(data) }
        advancedCalibrationFragment?.takeIf { it.isVisible }?.apply { updateValueOnKnobChange(data) }
        o2RegulationFragment?.takeIf { it.isVisible }?.apply { updateValueOnKnobChange(data) }
        serviceFragment?.takeIf { it.isVisible }?.apply { updateValueOnKnobChange(data) }
    }

    fun updateAck(){
        configFragment?.takeIf { it.isVisible }?.apply { initCalibrationFromPreferences() }
    }

    override fun closeDialog() {}

    override fun doAction() {
        if (passWord == "8000") {

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.network_info),
                        SystemFragmentButtonTypes.Network_Info
                    )
                )
            ) dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.network_info),
                    SystemFragmentButtonTypes.Network_Info
                )
            )

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_debug),
                        SystemFragmentButtonTypes.Debug
                    )
                )
            ) dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_debug),
                    SystemFragmentButtonTypes.Debug
                )
            )

            if (tag == "FromDashboard") {
                if (!dataListSystemItems.contains(
                        SystemButtonModelClass(
                            getString(R.string.hint_ota),
                            SystemFragmentButtonTypes.Ota
                        )
                    )
                ) dataListSystemItems.add(
                    SystemButtonModelClass(
                        getString(R.string.hint_ota),
                        SystemFragmentButtonTypes.Ota
                    )
                )
            } else {
                if (!dataListSystemItems.contains(
                        SystemButtonModelClass(
                            getString(R.string.wifi),
                            SystemFragmentButtonTypes.Wifi
                        )
                    )
                ) dataListSystemItems.add(
                    SystemButtonModelClass(
                        getString(R.string.wifi),
                        SystemFragmentButtonTypes.Wifi
                    )
                )

                if (!dataListSystemItems.contains(
                        SystemButtonModelClass(
                            getString(R.string.hint_ota),
                            SystemFragmentButtonTypes.Ota
                        )
                    )
                ) dataListSystemItems.add(
                    SystemButtonModelClass(
                        getString(R.string.hint_ota),
                        SystemFragmentButtonTypes.Ota
                    )
                )
            }
        }
        else if (passWord == "2018") {
            if (tag != "FromDashboard") {
                if (!dataListSystemItems.contains(
                        SystemButtonModelClass(
                            getString(R.string.config),
                            SystemFragmentButtonTypes.Config
                        )
                    )
                ) dataListSystemItems.add(
                    SystemButtonModelClass(
                        getString(R.string.config),
                        SystemFragmentButtonTypes.Config
                    )
                )
            }
        }

        else if (passWord == "8085"){

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_diagnos),
                        SystemFragmentButtonTypes.Diagnos
                    )
                )
            ) dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_diagnos),
                    SystemFragmentButtonTypes.Diagnos
                )
            )

//            if (!dataListSystemItems.contains(
//                    SystemButtonModelClass(
//                        getString(R.string.hint_self_test),
//                        SystemFragmentButtonTypes.Self_Test
//                    )
//                )
//            ) dataListSystemItems.add(
//                SystemButtonModelClass(
//                    getString(R.string.hint_self_test),
//                    SystemFragmentButtonTypes.Self_Test
//                )
//            )

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_reg_o2),
                        SystemFragmentButtonTypes.O2_Reg
                    )
                )
            ) dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_reg_o2),
                    SystemFragmentButtonTypes.O2_Reg
                )
            )

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_update),
                        SystemFragmentButtonTypes.Device_Update
                    )
                )
            ) dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_update),
                    SystemFragmentButtonTypes.Device_Update
                )
            )

            if (!dataListSystemItems.contains(
                    SystemButtonModelClass(
                        getString(R.string.hint_advanced_calib),
                        SystemFragmentButtonTypes.Advanced_Calibrations
                    )
                )
            ) dataListSystemItems.add(
                SystemButtonModelClass(
                    getString(R.string.hint_advanced_calib),
                    SystemFragmentButtonTypes.Advanced_Calibrations
                )
            )
        }
        systemAdapter?.updateList(dataListSystemItems)
    }
}

class SystemDesignAdapter(
    private var ctx: Context,
    private var dataList: ArrayList<SystemButtonModelClass>,
    private var onClick: SystemFragmentButtonListener
) : RecyclerView.Adapter<SystemDesignAdapter.SystemDesignViewHolder>() {

    var selectedIndexType: SystemFragmentButtonTypes? = null
    var highlightedIndex = -1
    var isEnable = true

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SystemDesignViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.system_design_single_layout, parent, false)
        return SystemDesignViewHolder(itemView)
    }

    fun updateList(newList: ArrayList<SystemButtonModelClass>) {
        dataList = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: SystemDesignViewHolder, position: Int) {
        val data = dataList[position]
        holder.buttonView?.text = data.title

        if (selectedIndexType == data.types) {
            if (highlightedIndex != -1 && highlightedIndex == position) {
                holder.buttonView?.setBackgroundResource(R.drawable.background_transparent_border_yellow)
                holder.buttonView?.setTextColor(ContextCompat.getColor(ctx, R.color.white))
            } else {
                holder.buttonView?.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
                holder.buttonView?.setTextColor(ContextCompat.getColor(ctx, R.color.white))
            }
        } else {
            if (highlightedIndex != -1 && highlightedIndex == position) {
                holder.buttonView?.setBackgroundResource(R.drawable.background_transparent_border_yellow)
                holder.buttonView?.setTextColor(ContextCompat.getColor(ctx, R.color.white))
            } else {
                holder.buttonView?.setBackgroundResource(R.drawable.background_grey_border_white)
                holder.buttonView?.setTextColor(ContextCompat.getColor(ctx, R.color.black))
            }
        }

        holder.buttonView?.setOnClickListener {
            selectedIndexType = data.types
            if (isEnable) onClick.doSystemButtonClick(data.types)
        }

        holder.buttonView?.setOnLongClickListener {
            if (isEnable) onClick.doSystemButtonLongClick(data.types)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class SystemDesignViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var buttonLayout: ConstraintLayout? = null
        var buttonView: AppCompatButton? = null

        init {
            buttonLayout = view.findViewById(R.id.buttonLayout)
            buttonView = view.findViewById(R.id.buttonView)
        }
    }
}