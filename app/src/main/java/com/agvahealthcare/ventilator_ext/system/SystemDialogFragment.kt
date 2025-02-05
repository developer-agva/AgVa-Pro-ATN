package com.agvahealthcare.ventilator_ext.system

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.*
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.advanced.AdvancedCalibrationFragment
import com.agvahealthcare.ventilator_ext.system.debug.DebugFragment
import com.agvahealthcare.ventilator_ext.system.device_update.DeviceUpdateFragment
import com.agvahealthcare.ventilator_ext.system.diagnosticCheck.DiagnosticCheckFragment
import com.agvahealthcare.ventilator_ext.system.info.InfoFragment
import com.agvahealthcare.ventilator_ext.system.network.NetworkFragment
import com.agvahealthcare.ventilator_ext.system.o2Regulation.O2RegulationFragment
import com.agvahealthcare.ventilator_ext.system.ota.OTAFragment
import com.agvahealthcare.ventilator_ext.system.services.ServiceFragment
import com.agvahealthcare.ventilator_ext.system.settings.SettingFragment
import com.agvahealthcare.ventilator_ext.system.test_calib.TestCalibrationFragment
import com.agvahealthcare.ventilator_ext.system.tube.TubeDiaFragment
import com.agvahealthcare.ventilator_ext.system.wifi.WiFiFragment
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.replaceFragment
import com.agvahealthcare.ventilator_ext.utility.setHeightWidthPercent
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_AND
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_MINUS
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_PLUS
import kotlinx.android.synthetic.main.content_button_layout.view.buttonView
import kotlinx.android.synthetic.main.fragment_system_dialog.*

class SystemDialogFragment : DialogFragment(), PasswordCallbackListener, SimpleCallbackListener {

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

    private var passWord = ""

    private var closeListener: OnDismissDialogListener? = null
    private var communicationService: CommunicationService? = null
    private var calibrationOxygen: OnCalibrationOxygen? = null
    var onLoudnessAdjustmentListener: OnLoudnessAdjustmentListener? = null
    private var infoFragment: InfoFragment? = null
    private var testCalibrationFragment: TestCalibrationFragment? = null
    private var advancedCalibrationFragment: AdvancedCalibrationFragment? = null
    private var startupCheckFragment: StartupCheckFragment? = null
    private var networkFragment: NetworkFragment? = null
    private var tubeDiaFragment: TubeDiaFragment? = null
    private var debugFragment: DebugFragment? = null
    private var settingFragment: SettingFragment? = null
    private var diagnosticCheckFragment: DiagnosticCheckFragment? = null
    private var o2RegulationFragment: O2RegulationFragment? = null
    private var serviceFragment: ServiceFragment? = null
    private var updateDeviceFragment: DeviceUpdateFragment? = null
    private var otaFragment: OTAFragment? = null
    private var wifiFragment: WiFiFragment? = null

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
    }

    @SuppressLint("NotifyDataSetChanged")
    fun highlightViewWithFocus(data: String) {

        Log.i("value_check_bonds", "index : $highlightedIndex ,size : $sizeOfCurrentArray")

        if (diagnosticCheckFragment?.buttonState != null) {
            diagnosticCheckFragment?.updateValueOnKnobChange(data)
        } else if (o2RegulationFragment?.customProgressDialog?.isVisible == true) {
            o2RegulationFragment?.updateValueOnKnobChange(data)
        } else if (settingFragment?.customProgressDialog != null) {
            settingFragment?.updateKnobSetting(data)
        } else if (settingFragment?.clickedTile != null) {
            settingFragment?.updateKnobSetting(data)
        } else {

            clearPreviousConstraints()
            startTimeoutWithDebounce()

            when (data) {
                PREFIX_PLUS -> {
                    if (highlightedIndex < (sizeOfCurrentArray + 14)) highlightedIndex++
                    else {
                        highlightedIndex = 0
                    }

                    getViewForFocus(false)?.let {
                        highlightAdapters(-1, data)
                        changeConstraintsOfFocusLayout(it)
                    } ?: kotlin.run {
                        highlightAdapters(highlightedIndex, data)
                    }
                }

                PREFIX_MINUS -> {

                    if (highlightedIndex > 0) highlightedIndex--
                    else {
                        highlightedIndex = (sizeOfCurrentArray + 14)
                    }

                    getViewForFocus(true)?.let {
                        highlightAdapters(-1, data)
                        changeConstraintsOfFocusLayout(it)
                    } ?: kotlin.run {
                        highlightAdapters(highlightedIndex, data)
                    }
                }

                PREFIX_AND -> {
                    getViewForFocus(null)?.let {
                        if (highlightedIndex == sizeOfCurrentArray + 1) {
                            it.callOnClick()
                        } else {
                            it.buttonView.callOnClick()

                            // reset highlight index to starting position after clicking on any fragments
                            highlightedIndex = -1
                        }
                    } ?: kotlin.run {
                        handleAdaptersClick(highlightedIndex)
                    }

                    clearPreviousConstraints()
                }
            }
        }
    }

    private fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainViewPanelSystem)
            constraintSet.clear(focusLayoutSystem.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutSystem.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutSystem.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutSystem.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelSystem)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelSystem)
        constraintSet.connect(
            focusLayoutSystem.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutSystem.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutSystem.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutSystem.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelSystem)
    }

    private fun getViewForFocus(isMinus: Boolean?): View? {

        return when (highlightedIndex) {

            in 0..sizeOfCurrentArray -> {
                if (sizeOfCurrentArray == 0) {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                } else null
            }

            sizeOfCurrentArray + 1 -> imageViewCrossSystem
            sizeOfCurrentArray + 2 -> includeButtonInfo
            sizeOfCurrentArray + 3 -> includeButtonStartup
            sizeOfCurrentArray + 4 -> includeButtonTestCalib
            sizeOfCurrentArray + 5 -> includeButtonSettings
            sizeOfCurrentArray + 6 -> includeButtonTube
            sizeOfCurrentArray + 7 -> includeButtonService
            sizeOfCurrentArray + 8 -> {
                if (includeButtonDiagchk.isVisible) {
                    includeButtonDiagchk
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 9 -> {
                if (includeButtonO2Regulate.isVisible) {
                    includeButtonO2Regulate
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 10 -> {
                if (includeButtonAdvancedCalibration.isVisible) {
                    includeButtonAdvancedCalibration
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 11 -> {
                if (includeButtondeviceUpdate.isVisible) {
                    includeButtondeviceUpdate
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 12 -> {
                if (includeButtonNetworkInfo.isVisible) {
                    includeButtonNetworkInfo
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 13 -> {
                if (includeButtonDebug.isVisible) {
                    includeButtonDebug
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 14 -> {
                if (includeButtonOTA.isVisible) {
                    includeButtonOTA
                } else {
                    isMinus?.let {
                        if (isMinus) highlightViewWithFocus("-") else highlightViewWithFocus("+")
                        null
                    }
                }
            }

            else -> null
        }
    }

    fun startTimeoutWithDebounce() {

        cancelTimeout()

        visibilityTimeout = object : CountDownTimer(10000, 2000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                highlightAdapters(-1, null)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_system_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        TAG = tag.toString()
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)

        setUpNavigation()

        includeButtonService.visibility = View.VISIBLE

        includeButtonDebug.buttonView.setOnClickListener {
            setupDebugFragment()
        }

        includeButtonOTA.buttonView.setOnClickListener {
            setupOtaFragment()
        }

        includeButtonStartup.buttonView.setOnLongClickListener {
            passWord = "8000"
            DialogBoxFactory.dismissDialogs()
            DialogBoxFactory.showNetworkInfoDialog(
                requireContext(),
                this,
                this,
                passWord,
                "Enter password to unlock system configuration window"
            )
            return@setOnLongClickListener true
        }

        includeButtonInfo.buttonView.setOnLongClickListener {
            if (tag == "FromDashboard") {
//                includeButtonService.visibility = View.VISIBLE
            } else {
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
            return@setOnLongClickListener true
        }
    }

    fun switchBetweenDebugAndDiagnosticWindow(tag : String){
        if (tag == "Diagnostic"){
            showDiagnosticFragment()

            includeButtonDiagchk.visibility = View.VISIBLE
            includeButtonO2Regulate.visibility = View.VISIBLE
            includeButtondeviceUpdate.visibility = View.VISIBLE
            includeButtonAdvancedCalibration.visibility = View.VISIBLE
            includeButtonNetworkInfo.visibility = View.GONE
            includeButtonDebug.visibility = View.GONE
            includeButtonWifi.visibility = View.GONE
            includeButtonOTA.visibility = View.GONE
            enableAllTabs(true)
        }

        else{
            setupDebugFragment()
            includeButtonDiagchk.visibility = View.GONE
            includeButtonO2Regulate.visibility = View.GONE
            includeButtondeviceUpdate.visibility = View.GONE
            includeButtonAdvancedCalibration.visibility = View.GONE
            includeButtonNetworkInfo.visibility = View.VISIBLE
            includeButtonDebug.visibility = View.VISIBLE
            includeButtonWifi.visibility = View.VISIBLE
            includeButtonOTA.visibility = View.VISIBLE
            enableAllTabs(true)
        }
    }

    private fun setUpNavigation() {

        if (tag == "FromSplash") {
            showServiceFragment()

            enableAllTabs(false)

        }
        else if (tag == "Diagnostic"){
            showDiagnosticFragment()

            includeButtonDiagchk.visibility = View.VISIBLE
            includeButtonO2Regulate.visibility = View.VISIBLE
            includeButtondeviceUpdate.visibility = View.VISIBLE
            includeButtonAdvancedCalibration.visibility = View.VISIBLE
            enableAllTabs(true)
        }

        else if (tag == "Debug"){
            setupDebugFragment()

            includeButtonNetworkInfo.visibility = View.VISIBLE
            includeButtonDebug.visibility = View.VISIBLE
            includeButtonWifi.visibility = View.VISIBLE
            includeButtonOTA.visibility = View.VISIBLE
            enableAllTabs(true)
        }

        else {
            if (arguments?.getString("CALIBRATE_CIRCUIT") == "TouchHere") setupTubeFragment()
            else showInfoFragment(communicationService)

            enableAllTabs(true)
        }
        setupClickListener()
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

    fun enableAllTabs(value: Boolean) {
        includeButtonStartup.buttonView.isEnabled = value
        includeButtonTube.buttonView.isEnabled = value
        includeButtonAdvancedCalibration.buttonView.isEnabled = value
        includeButtonSettings.buttonView.isEnabled = value
        includeButtonO2Regulate.buttonView.isEnabled = value
        includeButtonAdvancedCalibration.buttonView.isEnabled = value
        includeButtonTestCalib.buttonView.isEnabled = value
        includeButtonInfo.buttonView.isEnabled = value
        includeButtonNetworkInfo.buttonView.isEnabled = value
        includeButtonService.buttonView.isEnabled = value
        includeButtonDebug.buttonView.isEnabled = value
        includeButtonOTA.buttonView.isEnabled = value
        includeButtondeviceUpdate.buttonView.isEnabled = value
        includeButtonWifi.buttonView.isEnabled = value
    }

    // showing fragments
    private fun setupClickListener() {

        includeButtonInfo.buttonView.text = getString(R.string.hint_info)
        includeButtonTestCalib.buttonView.text = getString(R.string.hint_test_calib)
        includeButtonStartup.buttonView.text = getString(R.string.hint_sensors)
        includeButtonSettings.buttonView.text = getString(R.string.hint_settings)
        includeButtonTube.buttonView.text = getString(R.string.hint_tube)
        includeButtonDebug.buttonView.text = getString(R.string.hint_debug)
        includeButtonOTA.buttonView.text = getString(R.string.hint_ota)
        includeButtonDiagchk.buttonView.text = getString(R.string.hint_diagnos)
        includeButtonO2Regulate.buttonView.text = getString(R.string.hint_reg_o2)
        includeButtonAdvancedCalibration.buttonView.text = getString(R.string.hint_advanced_calib)
        includeButtonService.buttonView.text = getString(R.string.hint_service)
        includeButtonStartup.buttonView.text = getString(R.string.startup)
        includeButtondeviceUpdate.buttonView.text = getString(R.string.hint_update)
        includeButtonNetworkInfo.buttonView.text = getString(R.string.network_info)
        includeButtonWifi.buttonView.text = getString(R.string.wifi)

        imageViewCrossSystem.setOnClickListener {

            closeFragment()
        }

        includeButtonInfo.buttonView.setOnClickListener {
            showInfoFragment(communicationService)
        }

        includeButtonNetworkInfo.buttonView.setOnClickListener {
            showNetworkInfoFragment()
        }

        includeButtonTube.buttonView.setOnClickListener {
            setupTubeFragment()
        }
        includeButtondeviceUpdate.buttonView.setOnClickListener {
            showDeviceUpdateFragment()
        }

        includeButtonTestCalib.buttonView.setOnClickListener {
            setupTestCalibFragment()
        }

        includeButtonSettings.buttonView.setOnClickListener {
            setupSettingsFragment()
        }

        includeButtonDiagchk.buttonView.setOnClickListener {
            showDiagnosticFragment()
        }
        includeButtonO2Regulate.buttonView.setOnClickListener {
            showO2RegulationFragment()
        }

        includeButtonAdvancedCalibration.buttonView.setOnClickListener {
            setupAdvancedFragment()
        }

        includeButtonStartup.buttonView.setOnClickListener {
            setupStartupFragment()
        }

        includeButtonService.buttonView.setOnClickListener {
            showServiceFragment()
        }
        includeButtonWifi.buttonView.setOnClickListener {
            showWifiFragment()
        }
    }

    private fun setupTestCalibFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 2
        if (testCalibrationFragment == null)
            testCalibrationFragment =
                TestCalibrationFragment(communicationService)
        testCalibrationFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }

        highlightButton(includeButtonTestCalib)
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

        highlightButton(includeButtonStartup)
    }

    private fun setupAdvancedFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 2
        if (advancedCalibrationFragment == null)
            advancedCalibrationFragment = AdvancedCalibrationFragment(communicationService)
        advancedCalibrationFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
        highlightButton(includeButtonAdvancedCalibration)
    }

    private fun setupSettingsFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 4
        if (settingFragment == null)
            settingFragment = SettingFragment.newInstance(onLoudnessAdjustmentListener)
        settingFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
        highlightButton(includeButtonSettings)
    }

    private fun setupOtaFragment() {
        sizeOfCurrentArray = 0
        makeAllFragmentsNull()
        if (otaFragment == null) otaFragment = OTAFragment()
        otaFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
        highlightButton(includeButtonOTA)
    }

    private fun setupDebugFragment() {
        sizeOfCurrentArray = 0
        makeAllFragmentsNull()
        if (debugFragment == null) debugFragment = DebugFragment()
        debugFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
        highlightButton(includeButtonDebug)
    }

    private fun setupTubeFragment() {
        sizeOfCurrentArray = 1
        makeAllFragmentsNull()
        if (tubeDiaFragment == null) tubeDiaFragment = TubeDiaFragment(communicationService)
        tubeDiaFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
        highlightButton(includeButtonTube)
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

        highlightButton(includeButtonNetworkInfo)
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
        highlightButton(includeButtonO2Regulate)
    }


    fun closeFragment(){
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
        highlightButton(includeButtonDiagchk)
    }

    fun sendCommandInDiagnostic(command:String){
        diagnosticCheckFragment?.takeIf { it.isVisible }?.apply {
            getCommandsFromLiveWindow(command)
        }
    }

    fun sendDebugCommandInDebug(command:String){
        debugFragment?.takeIf { it.isVisible }?.apply {
            getCommandsFromLiveWindow(command)
        }
    }

    fun sendRangesInDiagnostic(ranges:String){
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
        highlightButton(includeButtonService)
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
        highlightButton(includeButtondeviceUpdate)
    }

    private fun showInfoFragment(communicationService: CommunicationService?) {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (infoFragment == null)
            infoFragment = InfoFragment(communicationService)
        infoFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }

        highlightButton(includeButtonInfo)
    }
    private fun showWifiFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (wifiFragment == null)
            wifiFragment = WiFiFragment()
        wifiFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }

        highlightButton(includeButtonWifi)
    }

    // handling fragments selections
    private fun highlightButton(view: View) {

        // adding bg colors
        includeButtonInfo.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonTestCalib.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonStartup.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonTube.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonSettings.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonDiagchk.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonO2Regulate.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtondeviceUpdate.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonNetworkInfo.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonAdvancedCalibration.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonService.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonDebug.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonOTA.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        includeButtonWifi.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)

        // adding text colors
        includeButtonInfo.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonOTA.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtondeviceUpdate.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonNetworkInfo.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonTestCalib.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonStartup.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonSettings.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonTube.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonDiagchk.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonO2Regulate.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonAdvancedCalibration.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonService.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonDebug.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        includeButtonWifi.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )

        // added on current fragment
        view.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
        view.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

    }

    override fun onStart() {
        super.onStart()
        val heightDialog = arguments?.getInt(KEY_HEIGHT)
        val widthDialog = arguments?.getInt(KEY_WIDTH)
        val isCheck = arguments?.getBoolean(KEY_STATUS)

        setHeightWidthPercent(heightDialog, widthDialog, isCheck)
    }

    fun updateSensorsCalibrationStatusViaPreference() {
        testCalibrationFragment?.takeIf { it.isVisible }
            ?.apply {
                updateSensorCalibrationStatus()
            }
        advancedCalibrationFragment?.takeIf { it.isVisible }
            ?.apply {
                updateSensorCalibrationStatus()
            }
    }

    fun updateOxygenCalibrateProgressStatus(progress: Int, msg: String, textAlignment: Int) {

        testCalibrationFragment?.takeIf { it.isVisible }?.apply {
            updateOxygenCalibrateProgressStatus(
                progress,
                msg,
                textAlignment
            )
        }
    }

    fun setSoftWareUpdate(softwareUpdate: String?) {
        infoFragment?.takeIf { it.isVisible }?.apply {
            setSoftWareUpdate(softwareUpdate)
        }
    }

    fun updateKnob(data: String?) {
        settingFragment?.takeIf { it.isVisible }?.apply {
            updateKnobSetting(data.toString())
        }

        networkFragment?.takeIf { it.isVisible }?.apply {
            updateKnobRawData(data.toString())
        }

        diagnosticCheckFragment?.takeIf { it.isVisible }?.apply {
            updateValueOnKnobChange(data)
        }

        advancedCalibrationFragment?.takeIf { it.isVisible }?.apply {
            updateValueOnKnobChange(data)
        }

        o2RegulationFragment?.takeIf { it.isVisible }?.apply {
            updateValueOnKnobChange(data)
        }

        serviceFragment?.takeIf { it.isVisible }?.apply {
            updateValueOnKnobChange(data)
        }
    }

    override fun closeDialog() {

        if (passWord == "8000") {
            includeButtonStartup.buttonView.apply {
                setBackgroundResource(R.drawable.background_grey_border_white)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        } else {
            includeButtonInfo.buttonView.apply {
                setBackgroundResource(R.drawable.background_grey_border_white)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }

    override fun doAction() {
        if (passWord == "8000") {
            includeButtonNetworkInfo.visibility = View.VISIBLE
            includeButtonDebug.visibility = View.VISIBLE

            if (tag == "FromDashboard") includeButtonOTA.visibility = View.GONE
            else {
                includeButtonWifi.visibility = View.VISIBLE
                includeButtonOTA.visibility = View.VISIBLE
            }
        } else {
            includeButtonDiagchk.visibility = View.VISIBLE
            includeButtonO2Regulate.visibility = View.VISIBLE
            includeButtondeviceUpdate.visibility = View.VISIBLE
            includeButtonAdvancedCalibration.visibility = View.VISIBLE
        }
    }

}