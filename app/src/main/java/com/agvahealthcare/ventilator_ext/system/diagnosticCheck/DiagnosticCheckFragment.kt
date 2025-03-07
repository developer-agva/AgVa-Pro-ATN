package com.agvahealthcare.ventilator_ext.system.diagnosticCheck

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.defaultOfExhaleValveRanges
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.defaultOfOxygenValveRanges
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.defaultOfTurbineRanges
import com.agvahealthcare.ventilator_ext.databinding.FragmentDiagnosticCheckBinding
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.utility.utils.Configs

enum class RangeType {
    OXYGEN_VALVE,
    EXHALE_VALVE,
    TURBINE,
}


class DiagnosticCheckFragment(private var communicationService: CommunicationService?) :
    Fragment() {

    private lateinit var mMainActivityViewModel: MainActivityViewModel
    private lateinit var mDiagnosticCheckViewModel: DiagnosticCheckViewModel
    private lateinit var binding: FragmentDiagnosticCheckBinding
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

            0 -> binding.btnTurbineRanges.callOnClick()
            1 -> binding.btnExhaleValveRanges.callOnClick()
            2 -> binding.btnOxygenValveRanges.callOnClick()
            3 -> binding.includeButtonTurbine.buttonView.callOnClick()
            4 -> binding.includeButtonExhaleValve.buttonView.callOnClick()
            5 -> binding.includeButtonOxyValve.buttonView.callOnClick()
            6 -> binding.includeButtonPurge.buttonView.callOnClick()
            7 -> binding.includeButtonNebulizer.buttonView.callOnClick()
            8 -> binding.includeButtonRedLED.buttonView.callOnClick()
            9 -> binding.includeButtonAmberLED.buttonView.callOnClick()
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
            constraintSet.clone(binding.mainViewPanelDiagnostic)
            constraintSet.clear(binding.focusLayoutDiagnostic.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutDiagnostic.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutDiagnostic.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutDiagnostic.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelDiagnostic)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelDiagnostic)
        constraintSet.connect(
            binding.focusLayoutDiagnostic.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutDiagnostic.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutDiagnostic.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutDiagnostic.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelDiagnostic)
    }

    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {

            return when (highlightedIndex) {

                0 -> binding.btnTurbineRanges
                1 -> binding.btnExhaleValveRanges
                2 -> binding.btnOxygenValveRanges
                3 -> binding.includeButtonTurbine.root
                4 -> binding.includeButtonExhaleValve.root
                5 -> binding.includeButtonOxyValve.root
                6 -> binding.includeButtonPurge.root
                7 -> binding.includeButtonNebulizer.root
                8 -> binding.includeButtonRedLED.root
                9 -> binding.includeButtonAmberLED.root

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
        binding = FragmentDiagnosticCheckBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mMainActivityViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        mDiagnosticCheckViewModel = ViewModelProvider(requireActivity())[DiagnosticCheckViewModel::class.java]

        // send command to ventilator to get data
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            send(getString(R.string.diagnostic_start_cmd))
        }

        binding.includeButtonTurbine.buttonView.text = "Turbine"
        binding.includeButtonExhaleValve.buttonView.text = "Exhale Valve"
        binding.includeButtonPurge.buttonView.text = "Purge"
        binding.includeButtonOxyValve.buttonView.text = "Oxy Valve"
        binding.includeButtonNebulizer.buttonView.text = "Nebulizer"
        binding.includeButtonRedLED.buttonView.text = "Red LED Bar"
        binding.includeButtonAmberLED.buttonView.text = "Amber LED Bar"

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

            binding.textViewTurbineRangesValue.text = defaultOfTurbineRanges
            binding.textViewExhaleValveRangesValue.text = defaultOfExhaleValveRanges
            binding.textViewOxygenValveRangesValue.text = defaultOfOxygenValveRanges
    }

    fun getCommandsFromLiveWindow(command: String) {
        when (command) {

            "Start Turbine" -> {
                binding.includeButtonTurbine.buttonView.callOnClick()
            }

            "Stop Turbine" -> {
                binding.includeButtonTurbine.buttonView.callOnClick()
            }

            "Start Oxygen" -> {
                binding.includeButtonOxyValve.buttonView.callOnClick()
            }

            "Stop Oxygen" -> {
                binding.includeButtonOxyValve.buttonView.callOnClick()
            }

            "Start Exhale" -> {
                binding.includeButtonExhaleValve.buttonView.callOnClick()
            }

            "Stop Exhale" -> {
                binding.includeButtonExhaleValve.buttonView.callOnClick()
            }

            "Start Nebulizer" -> {
                binding.includeButtonNebulizer.buttonView.callOnClick()
            }

            "Stop Nebulizer" -> {
                binding.includeButtonNebulizer.buttonView.callOnClick()
            }

            "Start Purge" -> {
                binding.includeButtonPurge.buttonView.callOnClick()
            }

            "Stop Purge" -> {
                binding.includeButtonPurge.buttonView.callOnClick()
            }

            "Start Red LED" -> {
                binding.includeButtonRedLED.buttonView.callOnClick()
            }

            "Stop Red LED" -> {
                binding.includeButtonRedLED.buttonView.callOnClick()
            }

            "Start Amber LED" -> {
                binding.includeButtonAmberLED.buttonView.callOnClick()
            }

            "Stop Amber LED" -> {
                binding.includeButtonAmberLED.buttonView.callOnClick()
            }
        }
    }

    private fun startUpTimer() {

        try {
            (parentFragment as SystemDialogFragment).enableAllTabs(false)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.mainViewPanelDiagnostic.visibility = View.GONE
        binding.txtWait.visibility = View.VISIBLE
        timer = object : CountDownTimer(8000, 1000) {
            override fun onTick(milliSec: Long) {}

            override fun onFinish() {
                try {
                    (parentFragment as SystemDialogFragment).enableAllTabs(true)
                    (requireActivity() as MainActivity).returnCommandsToSocket("Stop Diagnostic")
                    (requireActivity() as MainActivity).sendRangesToSocket("$defaultOfTurbineRanges,$defaultOfExhaleValveRanges,$defaultOfOxygenValveRanges")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                binding.mainViewPanelDiagnostic.visibility = View.VISIBLE
                binding.txtWait.visibility = View.GONE
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
            binding.txtInspPressueRawValue.text = it
        }

        mDiagnosticCheckViewModel.expPressureRawData.observe(viewLifecycleOwner) {
            binding.txtExpPressueRawValue.text = it
        }

        mDiagnosticCheckViewModel.oxyPressureRawData.observe(viewLifecycleOwner) {
            binding.txtOxyRawPressureValue.text = it
        }

        mDiagnosticCheckViewModel.inspPressureData.observe(viewLifecycleOwner) {
            binding.txtInspPressueValue.text = it
        }

        mDiagnosticCheckViewModel.expPressureData.observe(viewLifecycleOwner) {
            binding.txtExpPressueValue.text = it
        }

        mDiagnosticCheckViewModel.oxyPressureData.observe(viewLifecycleOwner) {
            binding.txtOxyPressureValue.text = it
        }

        mDiagnosticCheckViewModel.inspFlowVoltageData.observe(viewLifecycleOwner) {
            binding.txtInspFlowVoltageValue.text = it
        }

        mDiagnosticCheckViewModel.inspFlowData.observe(viewLifecycleOwner) {
            binding.txtInspFlowValue.text = it
        }

        mDiagnosticCheckViewModel.expDPRawData.observe(viewLifecycleOwner) {
            binding.txtExpDpRawValue.text = it
        }

        mDiagnosticCheckViewModel.expFlowData.observe(viewLifecycleOwner) {
            binding.txtExpFlowValue.text = it
        }


        // mid line
        mDiagnosticCheckViewModel.batteryCurrentData.observe(viewLifecycleOwner) {
            binding.txtBatteryCurrentValue.text = it
        }

        mDiagnosticCheckViewModel.batteryVoltageData.observe(viewLifecycleOwner) {
            binding.txtBatteryVoltageValue.text = it
        }

        mDiagnosticCheckViewModel.batterySOCData.observe(viewLifecycleOwner) {
            binding.txtBatterySocValue.text = it
        }

        mDiagnosticCheckViewModel.batteryRemainingTimeData.observe(viewLifecycleOwner) {
            binding.txtBatteryRemainingTimeValue.text = it
        }

        mDiagnosticCheckViewModel.batteryStateData.observe(viewLifecycleOwner) {
            binding.txtBatteryStateValue.text = it
        }

        mDiagnosticCheckViewModel.powerConnectionData.observe(viewLifecycleOwner) {
            binding.txtPowerConnectionValue.text = it
        }

//        mDiagnosticCheckViewModel.mainSwitchData.observe(viewLifecycleOwner) {
//            txtMainSwitchValue.text = it
//        }
        mDiagnosticCheckViewModel.spo2StatusData.observe(viewLifecycleOwner) {
            binding.txtSpo2StatusValue.text = it
        }

        mDiagnosticCheckViewModel.spo2Data.observe(viewLifecycleOwner) {
            binding.txtSpo2Value.text = it
        }

        mDiagnosticCheckViewModel.hrData.observe(viewLifecycleOwner) {
            binding.txtHRValue.text = it
        }

        // last line
        mDiagnosticCheckViewModel.o2SensorVoltageData.observe(viewLifecycleOwner) {
            binding.txtOxySensorVoltageValue.text = it
        }

        mDiagnosticCheckViewModel.piTempData.observe(viewLifecycleOwner) {
            binding.txtPiTempValue.text = it
        }

        mDiagnosticCheckViewModel.piCpuLoadData.observe(viewLifecycleOwner) {
            binding.txtPiCpuLoadValue.text = it
        }

//        mDiagnosticCheckViewModel.hardwareVersionData.observe(viewLifecycleOwner) {
//            txtHardwareVersionValue.text = it
//        }

        // static line
        mDiagnosticCheckViewModel.knobPcbVersionData.observe(viewLifecycleOwner) {
            binding.txtKnobPCBVersionValue.text = it
        }

        mDiagnosticCheckViewModel.knobPcbTypeData.observe(viewLifecycleOwner) {
            binding.txtKnobPcbTypeValue.text = it
        }

        mDiagnosticCheckViewModel.screenCPUTempData.observe(viewLifecycleOwner) {
            binding.txtScreenCpuTempValue.text = it
        }
    }

    private fun setDefaultValueOnViews() {

        // first line
        binding.txtInspPressueRawValue.text = "-"
        binding.txtExpPressueRawValue.text = "-"
        binding.txtOxyRawPressureValue.text = "-"
        binding.txtInspPressueValue.text = "-"
        binding.txtExpPressueValue.text = "-"
        binding.txtOxyPressureValue.text = "-"
        binding.txtInspFlowValue.text = "-"
        binding.txtInspFlowVoltageValue.text = "-"
        binding.txtExpFlowValue.text = "-"
        binding.txtExpDpRawValue.text = "-"

        // mid line
        binding.txtBatteryCurrentValue.text = "-"
        binding.txtBatteryVoltageValue.text = "-"
        binding.txtBatterySocValue.text = "-"
        binding.txtBatteryRemainingTimeValue.text = "-"
        binding.txtBatteryStateValue.text = "-"
        binding.txtPowerConnectionValue.text = "-"
//        txtMainSwitchValue.text = "-"
        binding.txtSpo2StatusValue.text = "-"
        binding.txtHRValue.text = "-"
        binding.txtSpo2Value.text = "-"

        // last line
        binding.txtOxySensorVoltageValue.text = "-"
        binding.txtPiTempValue.text = "-"
        binding.txtPiCpuLoadValue.text = "-"
//        txtHardwareVersionValue.text = "-"

        // static line
        binding.txtKnobPcbTypeValue.text = "-"
        binding.txtKnobPCBVersionValue.text = "-"
        binding.txtScreenCpuTempValue.text = "-"

        defaultOfTurbineRanges = "0"
        defaultOfExhaleValveRanges = "0"
        defaultOfOxygenValveRanges = "0"

        binding.textViewExhaleValveRangesValue.text = defaultOfExhaleValveRanges
        binding.textViewTurbineRangesValue.text = defaultOfTurbineRanges
        binding.textViewOxygenValveRangesValue.text = defaultOfOxygenValveRanges

        clickTurbine = false
        clickValve = false
        clickNebulizer = false
        clickOxyValve = false

    }

    private fun highlightButtons(view: View, textView: TextView) {
        binding.btnTurbineRanges.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.textViewTurbineRangesValue.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.black
            )
        )

        binding.btnExhaleValveRanges.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.textViewExhaleValveRangesValue.setTextColor(
            ContextCompat.getColor(
                requireContext(), R.color.black
            )
        )
        binding.btnOxygenValveRanges.setBackgroundResource(R.drawable.background_grey_border_white)
        binding.textViewOxygenValveRangesValue.setTextColor(
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

        binding.btnExhaleValveRanges.setOnClickListener {

            buttonState = RangeType.EXHALE_VALVE
            highlightButtons(binding.btnExhaleValveRanges, binding.textViewExhaleValveRangesValue)
            defaultOfExhaleValveRanges = binding.textViewExhaleValveRangesValue.text.toString()
            binding.textViewTurbineRangesValue.text = defaultOfTurbineRanges
            binding.textViewOxygenValveRangesValue.text = defaultOfOxygenValveRanges
            (requireActivity() as MainActivity).sendRangesToSocket("$defaultOfTurbineRanges,$defaultOfExhaleValveRanges,$defaultOfOxygenValveRanges")
        }

        binding.btnOxygenValveRanges.setOnClickListener {

            buttonState = RangeType.OXYGEN_VALVE
            highlightButtons(binding.btnOxygenValveRanges, binding.textViewOxygenValveRangesValue)
            defaultOfOxygenValveRanges = binding.textViewOxygenValveRangesValue.text.toString()
            binding.textViewTurbineRangesValue.text = defaultOfTurbineRanges
            binding.textViewExhaleValveRangesValue.text = defaultOfExhaleValveRanges
            (requireActivity() as MainActivity).sendRangesToSocket("$defaultOfTurbineRanges,$defaultOfExhaleValveRanges,$defaultOfOxygenValveRanges")
        }

        binding.btnTurbineRanges.setOnClickListener {

            buttonState = RangeType.TURBINE
            highlightButtons(binding.btnTurbineRanges, binding.textViewTurbineRangesValue)
            defaultOfTurbineRanges = binding.textViewTurbineRangesValue.text.toString()
            binding.textViewOxygenValveRangesValue.text = defaultOfOxygenValveRanges
            binding.textViewExhaleValveRangesValue.text = defaultOfExhaleValveRanges
            (requireActivity() as MainActivity).sendRangesToSocket("$defaultOfTurbineRanges,$defaultOfExhaleValveRanges,$defaultOfOxygenValveRanges")
        }

        binding.includeButtonPurge.buttonView.setOnClickListener {
            binding.includeButtonPurge.buttonView.setBackgroundResource(R.color.racing_green)
            binding.includeButtonPurge.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.white
                )
            )
            communicationService?.takeIf { it.isPortsConnected }?.apply {
                send(getString(R.string.purge_cmd))
            }

            (requireActivity() as MainActivity).returnCommandsToSocket("Stop Purge")

            Handler(Looper.getMainLooper()).postDelayed({
                binding.includeButtonPurge.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includeButtonPurge.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Purge")
            }, 500)
        }

        binding.includeButtonRedLED.buttonView.setOnClickListener {
            binding.includeButtonRedLED.buttonView.setBackgroundResource(R.color.racing_green)
            binding.includeButtonRedLED.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.white
                )
            )
            communicationService?.takeIf { it.isPortsConnected }?.apply {
                send(getString(R.string.valve_testing_dia)+"502")
            }

            (requireActivity() as MainActivity).returnCommandsToSocket("Stop Red LED")

            Handler(Looper.getMainLooper()).postDelayed({
                binding.includeButtonRedLED.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includeButtonRedLED.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Red LED")
            }, 500)
        }

        binding.includeButtonAmberLED.buttonView.setOnClickListener {
            binding.includeButtonAmberLED.buttonView.setBackgroundResource(R.color.racing_green)
            binding.includeButtonAmberLED.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(), R.color.white
                )
            )
            communicationService?.takeIf { it.isPortsConnected }?.apply {
                send(getString(R.string.valve_testing_dia)+"501")
            }

            (requireActivity() as MainActivity).returnCommandsToSocket("Stop Amber LED")

            Handler(Looper.getMainLooper()).postDelayed({
                binding.includeButtonAmberLED.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includeButtonAmberLED.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Amber LED")
            }, 500)
        }

        binding.includeButtonNebulizer.buttonView.setOnClickListener {

            if (clickNebulizer) {
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia)+"402")
                }
                binding.includeButtonNebulizer.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                binding.includeButtonNebulizer.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )

                clickNebulizer = false
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Nebulizer")
            } else {
                binding.includeButtonNebulizer.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
                binding.includeButtonNebulizer.buttonView.setTextColor(
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

        binding.includeButtonExhaleValve.buttonView.setOnClickListener {

            if (clickValve) {
                testValveTimer?.cancel()
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + "0")
                }
                binding.includeButtonExhaleValve.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                binding.includeButtonExhaleValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                binding.includeButtonExhaleValve.buttonView.text = "Exhale Valve"

                clickValve = false
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Exhale")
            } else {
                binding.includeButtonExhaleValve.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
                binding.includeButtonExhaleValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )

                setTestValveTimer()
                var value = binding.textViewExhaleValveRangesValue.text.toString()

                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + value.toInt())
                }
                clickValve = true
                (requireActivity() as MainActivity).returnCommandsToSocket("Stop Exhale")
            }

        }

        binding.includeButtonOxyValve.buttonView.setOnClickListener {

            if (clickOxyValve) {
                testOxygenValveTimer?.cancel()
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + "200")
                }
                binding.includeButtonOxyValve.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                binding.includeButtonOxyValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                binding.includeButtonOxyValve.buttonView.text = "Oxy Valve"

                clickOxyValve = false
                (requireActivity() as MainActivity).returnCommandsToSocket("Start Oxygen")
            } else {
                binding.includeButtonOxyValve.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
                binding.includeButtonOxyValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )

                setTestOxygenValveTimer()
                val value = binding.textViewOxygenValveRangesValue.text.toString()

                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + (value.toInt() + 200))
                }
                clickOxyValve = true
                (requireActivity() as MainActivity).returnCommandsToSocket("Stop Oxygen")
            }

        }

        binding.includeButtonTurbine.buttonView.setOnClickListener {

            if (clickTurbine) {
                testTurbineTimer?.cancel()
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.turbine_testing_dia) + "0")
                }

                binding.includeButtonTurbine.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                binding.includeButtonTurbine.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                binding.includeButtonTurbine.buttonView.text = "Turbine"
                clickTurbine = false

                (requireActivity() as MainActivity).returnCommandsToSocket("Start Turbine")

            } else {
                binding.includeButtonTurbine.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
                binding.includeButtonTurbine.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.white
                    )
                )
                setTestTurbineTimer()
                var value = binding.textViewTurbineRangesValue.text.toString()

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
                val value = binding.textViewTurbineRangesValue.text.toString()
                if (value.toInt() + stepPWM <= maxPWM) binding.textViewTurbineRangesValue.text =
                    "${value.toInt() + stepPWM}"
            }

            RangeType.EXHALE_VALVE -> {
                val value = binding.textViewExhaleValveRangesValue.text.toString()
                if (value.toInt() + stepPWM <= maxPWM) binding.textViewExhaleValveRangesValue.text =
                    "${value.toInt() + stepPWM}"
            }

            RangeType.OXYGEN_VALVE -> {
                val value = binding.textViewOxygenValveRangesValue.text.toString()
                if (value.toInt() + stepPWM <= maxPWM) binding.textViewOxygenValveRangesValue.text =
                    "${value.toInt() + stepPWM}"
            }

            else -> {}
        }
    }

    private fun decDataOnView() {
        when (buttonState) {

            RangeType.TURBINE -> {
                val value = binding.textViewTurbineRangesValue.text.toString()
                if (value.toInt() - stepPWM >= minPWM) binding.textViewTurbineRangesValue.text =
                    "${value.toInt() - stepPWM}"
            }

            RangeType.EXHALE_VALVE -> {
                val value = binding.textViewExhaleValveRangesValue.text.toString()
                if (value.toInt() - stepPWM >= minPWM) binding.textViewExhaleValveRangesValue.text =
                    "${value.toInt() - stepPWM}"
            }

            RangeType.OXYGEN_VALVE -> {
                val value = binding.textViewOxygenValveRangesValue.text.toString()
                if (value.toInt() - stepPWM >= minPWM) binding.textViewOxygenValveRangesValue.text =
                    "${value.toInt() - stepPWM}"
            }

            else -> {}
        }
    }

    private fun setDataOnView() {
        when (buttonState) {

            RangeType.TURBINE -> {
                defaultOfTurbineRanges = binding.textViewTurbineRangesValue.text.toString()
                binding.btnTurbineRanges.setBackgroundResource(R.drawable.background_grey_border_white)
                binding.textViewTurbineRangesValue.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
            }

            RangeType.EXHALE_VALVE -> {
                defaultOfExhaleValveRanges = binding.textViewExhaleValveRangesValue.text.toString()
                binding.btnExhaleValveRanges.setBackgroundResource(R.drawable.background_grey_border_white)
                binding.textViewExhaleValveRangesValue.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
            }

            RangeType.OXYGEN_VALVE -> {
                defaultOfOxygenValveRanges = binding.textViewOxygenValveRangesValue.text.toString()
                binding.btnOxygenValveRanges.setBackgroundResource(R.drawable.background_grey_border_white)
                binding.textViewOxygenValveRangesValue.setTextColor(
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
                binding.includeButtonTurbine.buttonView.text = "${(millisUntilFinished / 1000)} sec"

            }

            override fun onFinish() {
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.turbine_testing_dia) + "0")
                }
                binding.includeButtonTurbine.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                binding.includeButtonTurbine.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                binding.includeButtonTurbine.buttonView.text = "Turbine"
            }
        }.start()
    }

    private fun setTestOxygenValveTimer() {
        testOxygenValveTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.includeButtonOxyValve.buttonView.text = "${(millisUntilFinished / 1000)} sec"
            }

            override fun onFinish() {
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + "200")
                }
                binding.includeButtonOxyValve.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                binding.includeButtonOxyValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                binding.includeButtonOxyValve.buttonView.text = "Oxy Valve"
            }
        }.start()
    }

    private fun setTestValveTimer() {
        testValveTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.includeButtonExhaleValve.buttonView.text = "${(millisUntilFinished / 1000)} sec"

            }

            override fun onFinish() {
                communicationService?.takeIf { it.isPortsConnected }?.apply {
                    send(getString(R.string.valve_testing_dia) + "0")
                }
                binding.includeButtonExhaleValve.buttonView.setBackgroundResource(R.drawable.background_grey_border_white)
                binding.includeButtonExhaleValve.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(), R.color.black
                    )
                )
                binding.includeButtonExhaleValve.buttonView.text = "Exhale Valve"
            }
        }.start()
    }

}