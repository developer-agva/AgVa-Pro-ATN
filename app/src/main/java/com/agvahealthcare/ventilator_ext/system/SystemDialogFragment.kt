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
import com.agvahealthcare.ventilator_ext.databinding.FragmentSystemDialogBinding
import com.agvahealthcare.ventilator_ext.hl7comm.HL7CommunicationFragment
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


// NOTE :  just handle highlight of buttons with root id
class SystemDialogFragment : DialogFragment(), PasswordCallbackListener, SimpleCallbackListener {
    private lateinit var binding: FragmentSystemDialogBinding
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
    private var hL7CommunicationFragment : HL7CommunicationFragment? = null
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
        hL7CommunicationFragment = null
    }

    private fun showhl7CommunicationFragment(tagHL7: String){
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if(hL7CommunicationFragment == null)
            hL7CommunicationFragment = HL7CommunicationFragment()

        hL7CommunicationFragment?.apply {
            replaceFragment(this,tagHL7,R.id.system_nav_container)
        }
        highlightButton(binding.includeButtonTransfer.buttonView)
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
                    if (highlightedIndex < (sizeOfCurrentArray + 15)) highlightedIndex++
                    else {
                        highlightedIndex = 0
                    }

                    getViewForFocus(false)?.let {
                        highlightAdapters(-1, data)
                        changeConstraintsOfFocusLayout(it.second)
                    } ?: kotlin.run {
                        highlightAdapters(highlightedIndex, data)
                    }
                }

                PREFIX_MINUS -> {
                    if (highlightedIndex > 0) highlightedIndex--
                    else {
                        highlightedIndex = (sizeOfCurrentArray + 15)
                    }

                    getViewForFocus(true)?.let {
                        highlightAdapters(-1, data)
                        changeConstraintsOfFocusLayout(it.second)
                    } ?: kotlin.run {
                        highlightAdapters(highlightedIndex, data)
                    }
                }

                PREFIX_AND -> {
                    getViewForFocus(null)?.let {
                        if (highlightedIndex == sizeOfCurrentArray + 1) {
                            it.first.callOnClick()
                        } else {
                            it.first.callOnClick()

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
            constraintSet.clone(binding.mainViewPanelSystem)
            constraintSet.clear(binding.focusLayoutSystem.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutSystem.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutSystem.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutSystem.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelSystem)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelSystem)
        constraintSet.connect(
            binding.focusLayoutSystem.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutSystem.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutSystem.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutSystem.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelSystem)
    }

    private fun getViewForFocus(isMinus: Boolean?): Pair<View, View>? {

        // NOTE : first value of pair is button view and second value of pair is root and is sometimes both are same
        return when (highlightedIndex) {

            in 0..sizeOfCurrentArray -> {
                if (sizeOfCurrentArray == 0) {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                } else null
            }

            sizeOfCurrentArray + 1 -> {
                val pair = Pair(binding.imageViewCrossSystem, binding.imageViewCrossSystem)
                pair
            }

            sizeOfCurrentArray + 2 -> {
                val pair = Pair(binding.includeButtonInfo.buttonView, binding.includeButtonInfo.root)
                pair
            }

            sizeOfCurrentArray + 3 -> {
                val pair =
                    Pair(binding.includeButtonStartup.buttonView, binding.includeButtonStartup.root)
                pair
            }

            sizeOfCurrentArray + 4 -> {
                val pair =
                    Pair(binding.includeButtonTestCalib.buttonView, binding.includeButtonTestCalib.root)
                pair
            }

            sizeOfCurrentArray + 5 -> {
                val pair =
                    Pair(binding.includeButtonSettings.buttonView, binding.includeButtonSettings.root)
                pair
            }

            sizeOfCurrentArray + 6 -> {
                val pair = Pair(binding.includeButtonTube.buttonView, binding.includeButtonTube.root)
                pair
            }
            sizeOfCurrentArray + 7 -> {
                val pair = Pair(binding.includeButtonService.buttonView, binding.includeButtonService.root)
                pair
            }

            sizeOfCurrentArray + 8 -> {
                if (binding.includeButtonDiagchk.root.isVisible) {
                    val pair = Pair(binding.includeButtonDiagchk.buttonView, binding.includeButtonDiagchk.root)
                    pair
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 9 -> {
                if (binding.includeButtonO2Regulate.root.isVisible) {
                    val pair = Pair(binding.includeButtonO2Regulate.buttonView, binding.includeButtonO2Regulate.root)
                    pair
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 10 -> {
                if (binding.includeButtonAdvancedCalibration.root.isVisible) {
                    val pair = Pair(binding.includeButtonAdvancedCalibration.buttonView, binding.includeButtonAdvancedCalibration.root)
                    pair
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 11 -> {
                if (binding.includeButtondeviceUpdate.root.isVisible) {
                    val pair = Pair(binding.includeButtondeviceUpdate.buttonView, binding.includeButtondeviceUpdate.root)
                    pair
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 12 -> {
                if (binding.includeButtonNetworkInfo.root.isVisible) {
                    val pair = Pair(binding.includeButtonNetworkInfo.buttonView, binding.includeButtonNetworkInfo.root)
                    pair
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 13 -> {
                if (binding.includeButtonDebug.root.isVisible) {
                    val pair = Pair(binding.includeButtonDebug.buttonView, binding.includeButtonDebug.root)
                    pair
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 14 -> {
                if (binding.includeButtonOTA.root.isVisible) {
                    val pair = Pair(binding.includeButtonOTA.buttonView, binding.includeButtonOTA.root)
                    pair
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
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSystemDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        TAG = tag.toString()
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)

        setUpNavigation()

        binding.includeButtonService.root.visibility = View.VISIBLE

        binding.includeButtonDebug.buttonView.setOnClickListener {
            setupDebugFragment()
        }

        binding.includeButtonOTA.buttonView.setOnClickListener {
            setupOtaFragment()
        }

        binding.includeButtonStartup.buttonView.setOnLongClickListener {
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

        binding.includeButtonInfo.buttonView.setOnLongClickListener {
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

    fun switchBetweenDebugAndDiagnosticWindow(tag: String) {
        if (tag == "Diagnostic") {
            showDiagnosticFragment()

            binding.includeButtonDiagchk.root.visibility = View.VISIBLE
            binding.includeButtonO2Regulate.root.visibility = View.VISIBLE
            binding.includeButtondeviceUpdate.root.visibility = View.VISIBLE
            binding.includeButtonAdvancedCalibration.root.visibility =
                View.VISIBLE
            binding.includeButtonNetworkInfo.root.visibility = View.GONE
            binding.includeButtonDebug.root.visibility = View.GONE
            binding.includeButtonWifi.root.visibility = View.GONE
            binding.includeButtonOTA.root.visibility = View.GONE
            enableAllTabs(true)
        } else {
            setupDebugFragment()
            binding.includeButtonDiagchk.root.visibility = View.GONE
            binding.includeButtonO2Regulate.root.visibility = View.GONE
            binding.includeButtondeviceUpdate.root.visibility = View.GONE
            binding.includeButtonAdvancedCalibration.root.visibility =
                View.GONE
            binding.includeButtonNetworkInfo.root.visibility = View.VISIBLE
            binding.includeButtonDebug.root.visibility = View.VISIBLE
            binding.includeButtonWifi.root.visibility = View.VISIBLE
            binding.includeButtonOTA.root.visibility = View.VISIBLE
            enableAllTabs(true)
        }
    }

    private fun setUpNavigation() {

        if (tag == "FromSplash") {
            showServiceFragment()
            enableAllTabs(false)

        } else if (tag == "Diagnostic") {
            showDiagnosticFragment()
            binding.includeButtonDiagchk.root.visibility = View.VISIBLE
            binding.includeButtonO2Regulate.root.visibility = View.VISIBLE
            binding.includeButtondeviceUpdate.root.visibility = View.VISIBLE
            binding.includeButtonAdvancedCalibration.root.visibility =
                View.VISIBLE
            enableAllTabs(true)

        } else if (tag == "Debug") {
            setupDebugFragment()
            binding.includeButtonNetworkInfo.root.visibility = View.VISIBLE
            binding.includeButtonDebug.root.visibility = View.VISIBLE
            binding.includeButtonWifi.root.visibility = View.VISIBLE
            binding.includeButtonOTA.root.visibility = View.VISIBLE
            enableAllTabs(true)
        }

        else if (tag == "Discharge"){
            showhl7CommunicationFragment("Discharge")
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
        binding.includeButtonStartup.buttonView.isEnabled = value
        binding.includeButtonTube.buttonView.isEnabled = value
        binding.includeButtonAdvancedCalibration.buttonView.isEnabled = value
        binding.includeButtonSettings.buttonView.isEnabled = value
        binding.includeButtonO2Regulate.buttonView.isEnabled = value
        binding.includeButtonAdvancedCalibration.buttonView.isEnabled = value
        binding.includeButtonTestCalib.buttonView.isEnabled = value
        binding.includeButtonInfo.buttonView.isEnabled = value
        binding.includeButtonNetworkInfo.buttonView.isEnabled = value
        binding.includeButtonService.buttonView.isEnabled = value
        binding.includeButtonDebug.buttonView.isEnabled = value
        binding.includeButtonOTA.buttonView.isEnabled = value
        binding.includeButtondeviceUpdate.buttonView.isEnabled = value
        binding.includeButtonWifi.buttonView.isEnabled = value
    }

    // showing fragments
    private fun setupClickListener() {

        binding.includeButtonInfo.buttonView.text = getString(R.string.hint_info)
        binding.includeButtonTestCalib.buttonView.text = getString(R.string.hint_test_calib)
        binding.includeButtonStartup.buttonView.text = getString(R.string.hint_sensors)
        binding.includeButtonSettings.buttonView.text = getString(R.string.hint_settings)
        binding.includeButtonTube.buttonView.text = getString(R.string.hint_tube)
        binding.includeButtonDebug.buttonView.text = getString(R.string.hint_debug)
        binding.includeButtonOTA.buttonView.text = getString(R.string.hint_ota)
        binding.includeButtonDiagchk.buttonView.text = getString(R.string.hint_diagnos)
        binding.includeButtonO2Regulate.buttonView.text = getString(R.string.hint_reg_o2)
        binding.includeButtonAdvancedCalibration.buttonView.text =
            getString(R.string.hint_advanced_calib)
        binding.includeButtonService.buttonView.text = getString(R.string.hint_service)
        binding.includeButtonStartup.buttonView.text = getString(R.string.startup)
        binding.includeButtondeviceUpdate.buttonView.text = getString(R.string.hint_update)
        binding.includeButtonNetworkInfo.buttonView.text = getString(R.string.network_info)
        binding.includeButtonWifi.buttonView.text = getString(R.string.wifi)
        binding.includeButtonTransfer.buttonView.text = getString(R.string.discharge)

        binding.imageViewCrossSystem.setOnClickListener {
            closeFragment()
        }

        binding.includeButtonInfo.buttonView.setOnClickListener {
            showInfoFragment(communicationService)
        }

        binding.includeButtonNetworkInfo.buttonView.setOnClickListener {
            showNetworkInfoFragment()
        }

        binding.includeButtonTube.buttonView.setOnClickListener {
            setupTubeFragment()
        }

        binding.includeButtondeviceUpdate.buttonView.setOnClickListener {
            showDeviceUpdateFragment()
        }

        binding.includeButtonTestCalib.buttonView.setOnClickListener {
            setupTestCalibFragment()
        }

        binding.includeButtonSettings.buttonView.setOnClickListener {
            setupSettingsFragment()
        }

        binding.includeButtonDiagchk.buttonView.setOnClickListener {
            showDiagnosticFragment()
        }

        binding.includeButtonO2Regulate.buttonView.setOnClickListener {
            showO2RegulationFragment()
        }

        binding.includeButtonAdvancedCalibration.buttonView.setOnClickListener {
            setupAdvancedFragment()
        }

        binding.includeButtonStartup.buttonView.setOnClickListener {
            setupStartupFragment()
        }

        binding.includeButtonService.buttonView.setOnClickListener {
            showServiceFragment()
        }

        binding.includeButtonWifi.buttonView.setOnClickListener {
            showWifiFragment()
        }
        binding.includeButtonTransfer.buttonView.setOnClickListener {
            showhl7CommunicationFragment("Discharge")
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
        highlightButton(binding.includeButtonTestCalib.buttonView)
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
        highlightButton(binding.includeButtonStartup.buttonView)
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
        highlightButton(binding.includeButtonAdvancedCalibration.buttonView)
    }

    private fun setupSettingsFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 4
        if (settingFragment == null)
            settingFragment = SettingFragment.newInstance(onLoudnessAdjustmentListener,tag!!)
        settingFragment?.apply {
            replaceFragment(
                this,
                this::class.java.javaClass.simpleName,
                R.id.system_nav_container
            )
        }
        highlightButton(binding.includeButtonSettings.buttonView)
    }

    private fun setupOtaFragment() {
        sizeOfCurrentArray = 0
        makeAllFragmentsNull()
        if (otaFragment == null) otaFragment = OTAFragment()
        otaFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
        highlightButton(binding.includeButtonOTA.buttonView)
    }

    private fun setupDebugFragment() {
        sizeOfCurrentArray = 0
        makeAllFragmentsNull()
        if (debugFragment == null) debugFragment = DebugFragment()
        debugFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
        highlightButton(binding.includeButtonDebug.buttonView)
    }

    private fun setupTubeFragment() {
        sizeOfCurrentArray = 1
        makeAllFragmentsNull()
        if (tubeDiaFragment == null) tubeDiaFragment = TubeDiaFragment(communicationService)
        tubeDiaFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
        highlightButton(binding.includeButtonTube.buttonView)
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
        highlightButton(binding.includeButtonNetworkInfo.buttonView)
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
        highlightButton(binding.includeButtonO2Regulate.buttonView)
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
        highlightButton(binding.includeButtonDiagchk.buttonView)
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
        highlightButton(binding.includeButtonService.buttonView)
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
        highlightButton(binding.includeButtondeviceUpdate.buttonView)
    }

    private fun showInfoFragment(communicationService: CommunicationService?) {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (infoFragment == null)
            infoFragment = InfoFragment(communicationService)
        infoFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
        highlightButton(binding.includeButtonInfo.buttonView)
    }

    private fun showWifiFragment() {
        makeAllFragmentsNull()
        sizeOfCurrentArray = 0
        if (wifiFragment == null)
            wifiFragment = WiFiFragment()
        wifiFragment?.apply {
            replaceFragment(this, TAG, R.id.system_nav_container)
        }
        highlightButton(binding.includeButtonWifi.buttonView)
    }

    // handling fragments selections
    private fun highlightButton(view: View) {

        // adding bg colors
        binding.includeButtonTransfer.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonInfo.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonTestCalib.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonStartup.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonTube.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonSettings.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonDiagchk.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonO2Regulate.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtondeviceUpdate.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonNetworkInfo.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonAdvancedCalibration.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonService.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonDebug.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonOTA.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.includeButtonWifi.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)

        // adding text colors
        binding.includeButtonTransfer.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonInfo.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonOTA.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtondeviceUpdate.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonNetworkInfo.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonTestCalib.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonStartup.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonSettings.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonTube.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonDiagchk.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonO2Regulate.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonAdvancedCalibration.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonService.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonDebug.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.includeButtonWifi.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )

        // added on current fragment
        view.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
        (view as AppCompatButton).setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
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
            binding.includeButtonStartup.buttonView.apply {
                setBackgroundResource(R.drawable.background_grey_border_white)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        } else {
            binding.includeButtonInfo.buttonView.apply {
                setBackgroundResource(R.drawable.background_grey_border_white)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
    }

    override fun doAction() {
        if (passWord == "8000") {
            binding.includeButtonNetworkInfo.root.visibility = View.VISIBLE
            binding.includeButtonDebug.root.visibility = View.VISIBLE

            if (tag == "FromDashboard") binding.includeButtonOTA.root.visibility =
                View.GONE
            else {
                binding.includeButtonWifi.root.visibility = View.VISIBLE
                binding.includeButtonOTA.root.visibility = View.VISIBLE
            }
        } else {
            binding.includeButtonDiagchk.root.visibility = View.VISIBLE
            binding.includeButtonO2Regulate.root.visibility = View.VISIBLE
            binding.includeButtondeviceUpdate.root.visibility = View.VISIBLE
            binding.includeButtonAdvancedCalibration.root.visibility =
                View.VISIBLE
        }
    }

}