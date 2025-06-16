package com.agvahealthcare.ventilator_ext.system.diagnosticCheck

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.defaultOfExhaleValveRanges
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.defaultOfOxygenValveRanges
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.defaultOfTurbineRanges
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.activity_main.buttonPreopCheck
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.content_button_layout_fix.view.textView
import kotlinx.android.synthetic.main.fragment_advanced_calibration.includeButtonTurbine
import kotlinx.android.synthetic.main.fragment_diagnostic_check.*
import kotlinx.android.synthetic.main.fragment_diagnostic_check.txtWait
import kotlinx.android.synthetic.main.fragment_test_calib.includeButtonExhaleValve
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader

enum class RangeType {
    OXYGEN_VALVE,
    EXHALE_VALVE,
    TURBINE,
}


class DiagnosticCheckFragment(private var communicationService: CommunicationService?) :
    Fragment() {

    private lateinit var mMainActivityViewModel: MainActivityViewModel
    private lateinit var mDiagnosticCheckViewModel: DiagnosticCheckViewModel

    private var clickTurbine = false
    private var clickValve = false
    private var clickNebulizer = false
    private var clickOxyValve = false
    private var timer: CountDownTimer? = null
    private var testTurbineTimer: CountDownTimer? = null
    private var testOxygenValveTimer: CountDownTimer? = null
    private var testValveTimer: CountDownTimer? = null
    var buttonState: RangeType? = null
    private var maxPWM = 100
    private var minPWM = 0
    private var stepPWM = 1

    override fun onPause() {

        testOxygenValveTimer?.cancel()
        testTurbineTimer?.cancel()
        testValveTimer?.cancel()
        timer?.cancel()
        buttonState = null
        defaultOfTurbineRanges = "0"
        defaultOfExhaleValveRanges = "0"
        defaultOfOxygenValveRanges = "0"
        (requireActivity() as MainActivity).returnCommandsToSocket("Start Diagnostic")
        // send command to stop data
        communicationService?.takeIf { it.isPortsConnected }
            ?.apply { send(getString(R.string.diagnostic_stop_cmd)) }
        super.onPause()
    }

    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> btnTurbineRanges.callOnClick()
            1 -> btnExhaleValveRanges.callOnClick()
            2 -> btnOxygenValveRanges.callOnClick()
            3 -> includeButtonTurbine.buttonView.callOnClick()
            4 -> includeButtonExhaleValve.buttonView.callOnClick()
            5 -> includeButtonOxyValve.buttonView.callOnClick()
            6 -> includeButtonPurge.buttonView.callOnClick()
            7 -> includeButtonNebulizer.buttonView.callOnClick()
            8 -> includeButtonRedLED.buttonView.callOnClick()
            9 -> includeButtonAmberLED.buttonView.callOnClick()
        }
    }

    //
    fun highlightAdapterPosition(highlightedIndex: Int, data: String?) {

        getViewForFocus(highlightedIndex, data)?.let {
            changeConstraintsOfFocusLayout(it)
        } ?: run {
            clearPreviousConstraints()
        }
    }


    fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainViewPanelDiagnostic)
            constraintSet.clear(focusLayoutDiagnostic.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutDiagnostic.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutDiagnostic.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutDiagnostic.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelDiagnostic)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelDiagnostic)
        constraintSet.connect(
            focusLayoutDiagnostic.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutDiagnostic.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutDiagnostic.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutDiagnostic.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelDiagnostic)
    }

    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {

            return when (highlightedIndex) {

                0 -> btnTurbineRanges
                1 -> btnExhaleValveRanges
                2 -> btnOxygenValveRanges
                3 -> includeButtonTurbine
                4 -> includeButtonExhaleValve
                5 -> includeButtonOxyValve
                6 -> includeButtonPurge
                7 -> includeButtonNebulizer
                8 -> includeButtonRedLED
                9 -> includeButtonAmberLED

                else -> null
            }
        } ?: run {
            return null
        }
    }
    // logic knob highlight ends here

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_diagnostic_check, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivityViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        mDiagnosticCheckViewModel = ViewModelProvider(requireActivity())[DiagnosticCheckViewModel::class.java]

        // send command to ventilator to get data
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            send(getString(R.string.diagnostic_start_cmd))
        }

        includeButtonTurbine.buttonView.text = "Turbine"
        includeButtonExhaleValve.buttonView.text = "Exhale Valve"
        includeButtonPurge.buttonView.text = "Purge"
        includeButtonOxyValve.buttonView.text = "Oxy Valve"
        includeButtonNebulizer.buttonView.text = "Nebulizer"
        includeButtonRedLED.buttonView.text = "Red LED Bar"
        includeButtonAmberLED.buttonView.text = "Amber LED Bar"

        (requireActivity() as MainActivity).returnCommandsToSocket("Loading Diagnostic")

        setOnClickListener()
        setDefaultValueOnViews()
        observeData()
        startUpTimer()
    }

    fun getRangesFromLiveWindow(ranges: String) {

            defaultOfTurbineRanges = ranges.split(",")[0]
            defaultOfExhaleValveRanges = ranges.split(",")[1]
            defaultOfOxygenValveRanges = ranges.split(",")[2]

            textViewTurbineRangesValue.text = defaultOfTurbineRanges
            textViewExhaleValveRangesValue.text = defaultOfExhaleValveRanges
            textViewOxygenValveRangesValue.text = defaultOfOxygenValveRanges
    }

    fun getCommandsFromLiveWindow(command: String) {
        when (command) {

            "Start Turbine" -> {
                includeButtonTurbine.buttonView.callOnClick()
            }

            "Stop Turbine" -> {
                includeButtonTurbine.buttonView.callOnClick()
            }

            "Start Oxygen" -> {
                includeButtonOxyValve.buttonView.callOnClick()
            }

            "Stop Oxygen" -> {
                includeButtonOxyValve.buttonView.callOnClick()
            }

            "Start Exhale" -> {
                includeButtonExhaleValve.buttonView.callOnClick()
            }

            "Stop Exhale" -> {
                includeButtonExhaleValve.buttonView.callOnClick()
            }

            "Start Nebulizer" -> {
                includeButtonNebulizer.buttonView.callOnClick()
            }

            "Stop Nebulizer" -> {
                includeButtonNebulizer.buttonView.callOnClick()
            }

            "Start Purge" -> {
                includeButtonPurge.buttonView.callOnClick()
            }

            "Stop Purge" -> {
                includeButtonPurge.buttonView.callOnClick()
            }

            "Start Red LED" -> {
                includeButtonRedLED.buttonView.callOnClick()
            }

            "Stop Red LED" -> {
                includeButtonRedLED.buttonView.callOnClick()
            }

            "Start Amber LED" -> {
                includeButtonAmberLED.buttonView.callOnClick()
            }

            "Stop Amber LED" -> {
                includeButtonAmberLED.buttonView.callOnClick()
            }
        }
    }

    private fun startUpTimer() {

        try {
            (parentFragment as SystemDialogFragment).updateEnableStatus(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mainViewPanelDiagnostic.visibility = View.GONE
        txtWait.visibility = View.VISIBLE
        timer = object : CountDownTimer(8000, 1000) {
            override fun onTick(milliSec: Long) {}

            override fun onFinish() {
                try {
                    (parentFragment as SystemDialogFragment).updateEnableStatus(true)
                    (requireActivity() as MainActivity).returnCommandsToSocket("Stop Diagnostic")
                    (requireActivity() as MainActivity).sendRangesToSocket("$defaultOfTurbineRanges,$defaultOfExhaleValveRanges,$defaultOfOxygenValveRanges")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mainViewPanelDiagnostic.visibility = View.VISIBLE
                txtWait.visibility = View.GONE
            }
        }.start()
    }

//    private fun setBatteryLevelUpdate(btryLevel: Int) {
//        if (btryLevel < 0 || btryLevel > 100) {
//            txtBatteryValue.text = "-"
//        } else {
//            txtBatteryValue.text = "$btryLevel %"
//        }
//    }

    private fun observeData() {

        // first line
        mDiagnosticCheckViewModel.inspPressureRawData.observe(viewLifecycleOwner) {
            txtInspPressueRawValue.text = it
        }

        mDiagnosticCheckViewModel.expPressureRawData.observe(viewLifecycleOwner) {
            txtExpPressueRawValue.text = it
        }

        mDiagnosticCheckViewModel.oxyPressureRawData.observe(viewLifecycleOwner) {
            txtOxyRawPressureValue.text = it
        }

        mDiagnosticCheckViewModel.inspPressureData.observe(viewLifecycleOwner) {
            txtInspPressueValue.text = it
        }

        mDiagnosticCheckViewModel.expPressureData.observe(viewLifecycleOwner) {
            txtExpPressueValue.text = it
        }

        mDiagnosticCheckViewModel.oxyPressureData.observe(viewLifecycleOwner) {
            txtOxyPressureValue.text = it
        }

        mDiagnosticCheckViewModel.inspFlowVoltageData.observe(viewLifecycleOwner) {
            txtInspFlowVoltageValue.text = it
        }

        mDiagnosticCheckViewModel.inspFlowData.observe(viewLifecycleOwner) {
            txtInspFlowValue.text = it
        }

        mDiagnosticCheckViewModel.expDPRawData.observe(viewLifecycleOwner) {
            txtExpDpRawValue.text = it
        }

        mDiagnosticCheckViewModel.expFlowData.observe(viewLifecycleOwner) {
            txtExpFlowValue.text = it
        }


        // mid line
        mDiagnosticCheckViewModel.batteryCurrentData.observe(viewLifecycleOwner) {
            txtBatteryCurrentValue.text = it
        }

        mDiagnosticCheckViewModel.batteryVoltageData.observe(viewLifecycleOwner) {
            txtBatteryVoltageValue.text = it
        }

        mDiagnosticCheckViewModel.batterySOCData.observe(viewLifecycleOwner) {
            txtBatterySocValue.text = it
        }

        mDiagnosticCheckViewModel.batteryRemainingTimeData.observe(viewLifecycleOwner) {
            txtBatteryRemainingTimeValue.text = it
        }

        mDiagnosticCheckViewModel.batteryStateData.observe(viewLifecycleOwner) {
            txtBatteryStateValue.text = it
        }

        mDiagnosticCheckViewModel.powerConnectionData.observe(viewLifecycleOwner) {
            txtPowerConnectionValue.text = it
        }

//        mDiagnosticCheckViewModel.mainSwitchData.observe(viewLifecycleOwner) {
//            txtMainSwitchValue.text = it
//        }
        mDiagnosticCheckViewModel.spo2StatusData.observe(viewLifecycleOwner) {
            txtSpo2StatusValue.text = it
        }

        mDiagnosticCheckViewModel.spo2Data.observe(viewLifecycleOwner) {
            txtSpo2Value.text = it
        }

        mDiagnosticCheckViewModel.hrData.observe(viewLifecycleOwner) {
            txtHRValue.text = it
        }

        // last line
        mDiagnosticCheckViewModel.o2SensorVoltageData.observe(viewLifecycleOwner) {
            txtOxySensorVoltageValue.text = it
        }

        mDiagnosticCheckViewModel.piTempData.observe(viewLifecycleOwner) {
            txtPiTempValue.text = it
        }

        mDiagnosticCheckViewModel.piCpuLoadData.observe(viewLifecycleOwner) {
            txtPiCpuLoadValue.text = it
        }

//        mDiagnosticCheckViewModel.hardwareVersionData.observe(viewLifecycleOwner) {
//            txtHardwareVersionValue.text = it
//        }

        // static line
        mDiagnosticCheckViewModel.knobPcbVersionData.observe(viewLifecycleOwner) {
            txtKnobPCBVersionValue.text = it
        }

        mDiagnosticCheckViewModel.knobPcbTypeData.observe(viewLifecycleOwner) {
            txtKnobPcbTypeValue.text = it
        }

        mDiagnosticCheckViewModel.screenCPUTempData.observe(viewLifecycleOwner) {
            txtScreenCpuTempValue.text = it
        }
    }

    private fun setDefaultValueOnViews() {

        // first line
        txtInspPressueRawValue.text = "-"
        txtExpPressueRawValue.text = "-"
        txtOxyRawPressureValue.text = "-"
        txtInspPressueValue.text = "-"
        txtExpPressueValue.text = "-"
        txtOxyPressureValue.text = "-"
        txtInspFlowValue.text = "-"
        txtInspFlowVoltageValue.text = "-"
        txtExpFlowValue.text = "-"
        txtExpDpRawValue.text = "-"

        // mid line
        txtBatteryCurrentValue.text = "-"
        txtBatteryVoltageValue.text = "-"
        txtBatterySocValue.text = "-"
        txtBatteryRemainingTimeValue.text = "-"
        txtBatteryStateValue.text = "-"
        txtPowerConnectionValue.text = "-"
//        txtMainSwitchValue.text = "-"
        txtSpo2StatusValue.text = "-"
        txtHRValue.text = "-"
        txtSpo2Value.text = "-"

        // last line
        txtOxySensorVoltageValue.text = "-"
        txtPiTempValue.text = "-"
        txtPiCpuLoadValue.text = "-"
//        txtHardwareVersionValue.text = "-"

        // static line
        txtKnobPcbTypeValue.text = "-"
        txtKnobPCBVersionValue.text = "-"
        txtScreenCpuTempValue.text = "-"

        defaultOfTurbineRanges = "0"
        defaultOfExhaleValveRanges = "0"
        defaultOfOxygenValveRanges = "0"

        textViewExhaleValveRangesValue.text = defaultOfExhaleValveRanges
        textViewTurbineRangesValue.text = defaultOfTurbineRanges
        textViewOxygenValveRangesValue.text = defaultOfOxygenValveRanges

        clickTurbine = false
        clickValve = false
        clickNebulizer = false
        clickOxyValve = false

    }

    private fun highlightButtons(view: View, textView: TextView) {
        btnTurbineRanges.setBackgroundResource(R.drawable.background_grey_border_white)
        textViewTurbineRangesValue.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.black
            )
        )

        btnExhaleValveRanges.setBackgroundResource(R.drawable.background_grey_border_white)
        textViewExhaleValveRangesValue.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.black
            )
        )
        btnOxygenValveRanges.setBackgroundResource(R.drawable.background_grey_border_white)
        textViewOxygenValveRangesValue.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.black
            )
        )

        // highlight
        view.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
        textView.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.white
            )
        )
    }

    private fun setOnClickListener() {

        btnExhaleValveRanges.setOnClickListener {

            buttonState = RangeType.EXHALE_VALVE
            highlightButtons(btnExhaleValveRanges, textViewExhaleValveRangesValue)
            defaultOfExhaleValveRanges = textViewExhaleValveRangesValue.text.toString()
            textViewTurbineRangesValue.text = defaultOfTurbineRanges
            textViewOxygenValveRangesValue.text = defaultOfOxygenValveRanges
            (requireActivity() as MainActivity).sendRangesToSocket("$defaultOfTurbineRanges,$defaultOfExhaleValveRanges,$defaultOfOxygenValveRanges")
        }

        btnOxygenValveRanges.setOnClickListener {

            buttonState = RangeType.OXYGEN_VALVE
            highlightButtons(btnOxygenValveRanges, textViewOxygenValveRangesValue)
            defaultOfOxygenValveRanges = textViewOxygenValveRangesValue.text.toString()
            textViewTurbineRangesValue.text = defaultOfTurbineRanges
            textViewExhaleValveRangesValue.text = defaultOfExhaleValveRanges
            (requireActivity() as MainActivity).sendRangesToSocket("$defaultOfTurbineRanges,$defaultOfExhaleValveRanges,$defaultOfOxygenValveRanges")
        }

        btnTurbineRanges.setOnClickListener {

            buttonState = RangeType.TURBINE
            highlightButtons(btnTurbineRanges, textViewTurbineRangesValue)
            defaultOfTurbineRanges = textViewTurbineRangesValue.text.toString()
            textViewOxygenValveRangesValue.text = defaultOfOxygenValveRanges
            textViewExhaleValveRangesValue.text = defaultOfExhaleValveRanges
            (requireActivity() as MainActivity).sendRangesToSocket("$defaultOfTurbineRanges,$defaultOfExhaleValveRanges,$defaultOfOxygenValveRanges")
        }

        includeButtonPurge.buttonView.setOnClickListener {
            includeButtonPurge.buttonView.setBackgroundResource(R.color.racing_green)
            includeButtonPurge.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.white
                )
            )
            communicationService?.takeIf { it.isPortsConnected }?.apply {
                send(getString(R.string.purge_cmd))
            }

            (requireActivity() as MainActivity).returnCommandsToSocket("Stop Purge")

            Handler(Looper.getMainLooper()).postDelayed({
                includeButtonPurge.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includeButtonPurge.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Purge")
            }, 500)
        }

        includeButtonRedLED.buttonView.setOnClickListener {
            includeButtonRedLED.buttonView.setBackgroundResource(R.color.racing_green)
            includeButtonRedLED.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.white
                )
            )
            communicationService?.takeIf { it.isPortsConnected }?.apply {
                send(getString(R.string.valve_testing_dia)+"502")
            }

            (requireActivity() as MainActivity).returnCommandsToSocket("Stop Red LED")

            Handler(Looper.getMainLooper()).postDelayed({
                includeButtonRedLED.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includeButtonRedLED.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Red LED")
            }, 500)
        }

        includeButtonAmberLED.buttonView.setOnClickListener {
            includeButtonAmberLED.buttonView.setBackgroundResource(R.color.racing_green)
            includeButtonAmberLED.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.white
                )
            )
            communicationService?.takeIf { it.isPortsConnected }?.apply {
                send(getString(R.string.valve_testing_dia)+"501")
            }

            (requireActivity() as MainActivity).returnCommandsToSocket("Stop Amber LED")

            Handler(Looper.getMainLooper()).postDelayed({
                includeButtonAmberLED.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includeButtonAmberLED.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Amber LED")
            }, 500)
        }

        includeButtonNebulizer.buttonView.setOnClickListener {

            if (clickNebulizer) {
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia)+"402")
                }
                includeButtonNebulizer.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                includeButtonNebulizer.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )

                clickNebulizer = false
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Nebulizer")
            } else {
                includeButtonNebulizer.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
                includeButtonNebulizer.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )

                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia)+"401")
                }
                clickNebulizer = true
                (requireActivity() as MainActivity).returnCommandsToSocket("Stop Nebulizer")
            }
        }

//        includeButtonI2C.buttonView.setOnClickListener {
//            includeButtonI2C.buttonView.setBackgroundResource(R.color.racing_green)
//            includeButtonI2C.buttonView.setTextColor(
//                ContextCompat.getColor(
//                    requireContext(), R.color.white
//                )
//            )
//            communicationService?.takeIf { it.isPortsConnected }?.apply {
//                send(getString(R.string.valve_testing_dia)+"300")
//            }
//
//            (requireActivity() as MainActivity).returnCommandsToSocket("Stop I2C")
//
//            Handler(Looper.getMainLooper()).postDelayed({
//                includeButtonI2C.buttonView.setBackgroundResource(R.color.dolphin_grey)
//                includeButtonI2C.buttonView.setTextColor(
//                    ContextCompat.getColor(
//                        requireContext(), R.color.black
//                    )
//                )
//                (requireActivity() as MainActivity).returnCommandsToSocket("Start I2C")
//            }, 500)
//        }

        includeButtonExhaleValve.buttonView.setOnClickListener {

            if (clickValve) {
                testValveTimer?.cancel()
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + "0")
                }
                includeButtonExhaleValve.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                includeButtonExhaleValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                includeButtonExhaleValve.buttonView.text = "Exhale Valve"

                clickValve = false
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Exhale")
            } else {
                includeButtonExhaleValve.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
                includeButtonExhaleValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )

                setTestValveTimer()
                var value = textViewExhaleValveRangesValue.text.toString()

                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + value.toInt())
                }
                clickValve = true
                (requireActivity() as MainActivity).returnCommandsToSocket("Stop Exhale")
            }

        }

        includeButtonOxyValve.buttonView.setOnClickListener {

            if (clickOxyValve) {
                testOxygenValveTimer?.cancel()
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + "200")
                }
                includeButtonOxyValve.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                includeButtonOxyValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                includeButtonOxyValve.buttonView.text = "Oxy Valve"

                clickOxyValve = false
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Oxygen")
            } else {
                includeButtonOxyValve.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
                includeButtonOxyValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )

                setTestOxygenValveTimer()
                val value = textViewOxygenValveRangesValue.text.toString()

                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + (value.toInt() + 200))
                }
                clickOxyValve = true
                (requireActivity() as MainActivity).returnCommandsToSocket("Stop Oxygen")
            }

        }

        includeButtonTurbine.buttonView.setOnClickListener {

            if (clickTurbine) {
                testTurbineTimer?.cancel()
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.turbine_testing_dia) + "0")
                }

                includeButtonTurbine.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                includeButtonTurbine.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                includeButtonTurbine.buttonView.text = "Turbine"
                clickTurbine = false

                (requireActivity() as MainActivity).returnCommandsToSocket("Start Turbine")

            } else {
                includeButtonTurbine.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
                includeButtonTurbine.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )
                setTestTurbineTimer()
                var value = textViewTurbineRangesValue.text.toString()

                if (value == "100") value = (("99".toInt()) * 10).toString()
                else value = ((value.toInt()) * 10).toString()

                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.turbine_testing_dia) + value)
                }
                clickTurbine = true
                (requireActivity() as MainActivity).returnCommandsToSocket("Stop Turbine")
            }
        }
    }

    fun updateValueOnKnobChange(data: String?) {
        when (data) {
            Configs.PREFIX_PLUS -> incDataOnView()
            Configs.PREFIX_MINUS -> decDataOnView()
            Configs.PREFIX_AND -> setDataOnView()
        }
    }

    //
    private fun incDataOnView() {

        when (buttonState) {

            RangeType.TURBINE -> {
                val value = textViewTurbineRangesValue.text.toString()
                if (value.toInt() + stepPWM <= maxPWM) textViewTurbineRangesValue.text =
                    "${value.toInt() + stepPWM}"
            }

            RangeType.EXHALE_VALVE -> {
                val value = textViewExhaleValveRangesValue.text.toString()
                if (value.toInt() + stepPWM <= maxPWM) textViewExhaleValveRangesValue.text =
                    "${value.toInt() + stepPWM}"
            }

            RangeType.OXYGEN_VALVE -> {
                val value = textViewOxygenValveRangesValue.text.toString()
                if (value.toInt() + stepPWM <= maxPWM) textViewOxygenValveRangesValue.text =
                    "${value.toInt() + stepPWM}"
            }

            else -> {}
        }
    }

    private fun decDataOnView() {
        when (buttonState) {

            RangeType.TURBINE -> {
                val value = textViewTurbineRangesValue.text.toString()
                if (value.toInt() - stepPWM >= minPWM) textViewTurbineRangesValue.text =
                    "${value.toInt() - stepPWM}"
            }

            RangeType.EXHALE_VALVE -> {
                val value = textViewExhaleValveRangesValue.text.toString()
                if (value.toInt() - stepPWM >= minPWM) textViewExhaleValveRangesValue.text =
                    "${value.toInt() - stepPWM}"
            }

            RangeType.OXYGEN_VALVE -> {
                val value = textViewOxygenValveRangesValue.text.toString()
                if (value.toInt() - stepPWM >= minPWM) textViewOxygenValveRangesValue.text =
                    "${value.toInt() - stepPWM}"
            }

            else -> {}
        }
    }

    private fun setDataOnView() {
        when (buttonState) {

            RangeType.TURBINE -> {
                defaultOfTurbineRanges = textViewTurbineRangesValue.text.toString()
                btnTurbineRanges.setBackgroundResource(R.drawable.background_grey_border_white)
                textViewTurbineRangesValue.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
            }

            RangeType.EXHALE_VALVE -> {
                defaultOfExhaleValveRanges = textViewExhaleValveRangesValue.text.toString()
                btnExhaleValveRanges.setBackgroundResource(R.drawable.background_grey_border_white)
                textViewExhaleValveRangesValue.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
            }

            RangeType.OXYGEN_VALVE -> {
                defaultOfOxygenValveRanges = textViewOxygenValveRangesValue.text.toString()
                btnOxygenValveRanges.setBackgroundResource(R.drawable.background_grey_border_white)
                textViewOxygenValveRangesValue.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
            }

            else -> {}
        }

        buttonState = null
        (requireActivity() as MainActivity).sendRangesToSocket("$defaultOfTurbineRanges,$defaultOfExhaleValveRanges,$defaultOfOxygenValveRanges")
    }

    private fun setTestTurbineTimer() {
        testTurbineTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                includeButtonTurbine.buttonView.text = "${(millisUntilFinished / 1000)} sec"

            }

            override fun onFinish() {
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.turbine_testing_dia) + "0")
                }
                includeButtonTurbine.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                includeButtonTurbine.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                includeButtonTurbine.buttonView.text = "Turbine"
            }
        }.start()
    }

    private fun setTestOxygenValveTimer() {
        testOxygenValveTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                includeButtonOxyValve.buttonView.text = "${(millisUntilFinished / 1000)} sec"
            }

            override fun onFinish() {
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + "200")
                }
                includeButtonOxyValve.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                includeButtonOxyValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                includeButtonOxyValve.buttonView.text = "Oxy Valve"
            }
        }.start()
    }

    private fun setTestValveTimer() {
        testValveTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                includeButtonExhaleValve.buttonView.text = "${(millisUntilFinished / 1000)} sec"

            }

            override fun onFinish() {
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + "0")
                }
                includeButtonExhaleValve.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                includeButtonExhaleValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                includeButtonExhaleValve.buttonView.text = "Exhale Valve"
            }
        }.start()
    }

}