package com.agvahealthcare.ventilator_ext.system.advanced

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_advanced_calibration.*
import kotlinx.android.synthetic.main.fragment_advanced_calibration.capgif
import kotlinx.android.synthetic.main.fragment_advanced_calibration.ventigif


class AdvancedCalibrationFragment(private var communicationService: CommunicationService?) :
    Fragment(), View.OnClickListener {

    companion object {
        const val TAG = "AdvancedCalibrationFragment"
    }

    var visibilityTimeout: CountDownTimer? = null
    private var maxOfLeak = 0
    private var minOfLeak = 0
    private var step = 5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_advanced_calibration, container, false)
    }

    private var prefManager: PreferenceManager? = null
    private var currentTag: String = ""

    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> if (topBarAdvancedCalib.isVisible) backBtnAdvancedCalib.callOnClick() else includeButtonTurbine.buttonView.callOnClick()
            1 -> if (topBarAdvancedCalib.isVisible) includeButtonSendCmdAdvancedCalib.buttonView.callOnClick() else includeButtonInspFlowSensor.buttonView.callOnClick()
            2 -> includeButtonLeakTest.buttonView.callOnClick()
        }
    }

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
            constraintSet.clone(mainViewPanelAdvancedCalib)
            constraintSet.clear(focusLayoutAdvancedCalib.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutAdvancedCalib.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutAdvancedCalib.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutAdvancedCalib.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelAdvancedCalib)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelAdvancedCalib)
        constraintSet.connect(
            focusLayoutAdvancedCalib.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutAdvancedCalib.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutAdvancedCalib.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutAdvancedCalib.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelAdvancedCalib)
    }

    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {

            return when (highlightedIndex) {

                0 -> if (topBarAdvancedCalib.isVisible) backBtnAdvancedCalib else includeButtonTurbine
                1 -> if (topBarAdvancedCalib.isVisible) includeButtonSendCmdAdvancedCalib else includeButtonInspFlowSensor
                2 -> includeButtonLeakTest

                else -> null
            }
        } ?: run {
            return null
        }
    }
    // logic knob highlight ends here

    private fun hideGoneFunction(isCalib: Boolean) {

        if (isCalib) {
            // pre op check layout
            topBarAdvancedCalib.visibility = View.GONE
            backBtnAdvancedCalib.visibility = View.GONE
            tvMainTitleAdvancedCalib.visibility = View.GONE
            ventigif.visibility = View.GONE
            capgif.visibility = View.GONE
            tvtext1AdvancedCalib.visibility = View.GONE
            tvtext2AdvancedCalib.visibility = View.GONE
            tvtext3AdvancedCalib.visibility = View.GONE
            tvtextHeadingAdvancedCalib.visibility = View.GONE
            includeButtonSendCmdAdvancedCalib.visibility = View.GONE

            // calib layouts
            advancedCalibText.visibility = View.VISIBLE
            includeButtonTurbine.visibility = View.VISIBLE
            includeButtonInspFlowSensor.visibility = View.VISIBLE
            includeButtonLeakTest.visibility = View.VISIBLE
            ivTurbineSensorStatus.visibility = View.VISIBLE
            ivInspFlowSensorStatus.visibility = View.VISIBLE
            ivLeakTestStatus.visibility = View.VISIBLE
            tvTurbineSensor.visibility = View.VISIBLE
            tvInspFlowSensor.visibility = View.VISIBLE
            tvLeakTest.visibility = View.VISIBLE
        } else {
            // pre op check layout
            topBarAdvancedCalib.visibility = View.VISIBLE
            backBtnAdvancedCalib.visibility = View.VISIBLE
            tvMainTitleAdvancedCalib.visibility = View.VISIBLE
            tvtext1AdvancedCalib.visibility = View.VISIBLE
            tvtext2AdvancedCalib.visibility = View.VISIBLE
            tvtext3AdvancedCalib.visibility = View.VISIBLE
            tvtextHeadingAdvancedCalib.visibility = View.VISIBLE
            includeButtonSendCmdAdvancedCalib.visibility = View.VISIBLE

            // calib layouts
            advancedCalibText.visibility = View.GONE
            includeButtonTurbine.visibility = View.GONE
            includeButtonInspFlowSensor.visibility = View.GONE
            includeButtonLeakTest.visibility = View.GONE
            ivTurbineSensorStatus.visibility = View.GONE
            ivInspFlowSensorStatus.visibility = View.GONE
            ivLeakTestStatus.visibility = View.GONE
            tvTurbineSensor.visibility = View.GONE
            tvInspFlowSensor.visibility = View.GONE
            tvLeakTest.visibility = View.GONE
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())
        setUpView()
        setUpOnClickListener()

        maxOfLeak = getString(R.string.max_leak_factor).toInt()
        minOfLeak = getString(R.string.min_leak_factor).toInt()

        hideGoneFunction(true)
    }

    @SuppressLint("SetTextI18n")
    private fun setUpView() {
        includeButtonTurbine.buttonView.text = getString(R.string.hint_Turbine_Calibration)
        includeButtonInspFlowSensor.buttonView.text = getString(R.string.hint__Insp_Flow_Calibration)
        includeButtonSendCmdAdvancedCalib.buttonView.text = "START CALIBRATION"
        includeButtonLeakTest.buttonView.text = getString(R.string.hint_Leak_Test_Text)
        includeButtonSendCmdAdvancedCalib.buttonView.textAlignment = View.TEXT_ALIGNMENT_INHERIT
        // includeButtonSendCmd.buttonView.isEnabled = false
        includeButtonSendCmdAdvancedCalib.buttonView.setBackgroundColor(R.drawable.background_black_border_white)
        includeButtonSendCmdAdvancedCalib.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        updateSensorCalibrationStatus()
    }

    private fun setUpOnClickListener() {
        if (tag == "FromDashboard") {
            includeButtonTurbine.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

            includeButtonInspFlowSensor.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

            includeButtonLeakTest.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

        } else {
            includeButtonTurbine.buttonView.setOnClickListener(this)
            includeButtonInspFlowSensor.buttonView.setOnClickListener(this)
            includeButtonSendCmdAdvancedCalib.buttonView.setOnClickListener(this)
            includeButtonLeakTest.buttonView.setOnClickListener(this)
        }

        backBtnAdvancedCalib.setOnClickListener {
            (parentFragment as SystemDialogFragment).highlightedIndex = -1
            (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 2
            hideGoneFunction(true)
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {

        when (v) {
            includeButtonTurbine.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                hideGoneFunction(false)
                currentTag = "Turbine"
                tvMainTitleAdvancedCalib.text = "Turbine pre-calibration check"
                tvtext1AdvancedCalib.text = "1. Disconnect patient tubing"
                tvtext2AdvancedCalib.text = "2. Insert calibration cap at inspiratory port"
                tvtext3AdvancedCalib.text = "3. Make sure the ventilator is connected to mains supply"
                includeButtonSendCmdAdvancedCalib.buttonView.text = "Start calibration"

                capgif.visibility = View.VISIBLE
                ventigif.visibility = View.GONE
            }

            includeButtonInspFlowSensor.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                hideGoneFunction(false)
                currentTag = "Insp Flow"
                tvMainTitleAdvancedCalib.text = "Insp Flow sensor pre-calibration check"
                tvtext1AdvancedCalib.text = "1. Connect external flow calibrator to inspiratory port"
                tvtext2AdvancedCalib.text = "2. Make sure the ventilator is connected to mains supply"
                tvtext3AdvancedCalib.text = ""
                ventigif.visibility = View.GONE
                capgif.visibility = View.GONE
                includeButtonSendCmdAdvancedCalib.buttonView.text = "Start calibration"
            }

            includeButtonLeakTest.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                hideGoneFunction(false)
                currentTag = "Leak Test"
                tvMainTitleAdvancedCalib.text = "System Leak Test"
                tvtext1AdvancedCalib.text = "1. Insert calibration cap at inspiratory port"
                tvtext2AdvancedCalib.text = "2. Make sure the ventilator is connected to mains supply"
                tvtext3AdvancedCalib.text = ""
                capgif.visibility = View.VISIBLE
                ventigif.visibility = View.GONE
                includeButtonSendCmdAdvancedCalib.buttonView.text = "Start leak test"
            }

            includeButtonSendCmdAdvancedCalib.buttonView -> {
                when (currentTag) {
                    "Turbine" -> { sendCalibrationCommandToVentilator(Configs.TAG_SENSOR_TURBINE) }

                    "Leak Test" -> { sendCalibrationCommandToVentilator(Configs.TAG_LEAK_TEST) }

                    "Insp Flow" -> { sendCalibrationCommandToVentilator(Configs.TAG_SENSOR_INSP_FLOW) }
                }
            }
        }
    }

    fun updateSensorCalibrationStatus() {
        hideGoneFunction(true)
        prefManager?.apply {
            Log.i("CALIBCHECK", "Sensor data is refreshing on the view......")

            // Turbine sensor
            if (readTurbineCalibrationStatus()) {
                ivTurbineSensorStatus.visibility = View.VISIBLE
                tvTurbineSensor.text = readTurbineCalibrationDate()
                ivTurbineSensorStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                tvTurbineSensor.text = getString(R.string.sensore_not_calibrated)
                ivTurbineSensorStatus.setImageResource(R.drawable.ic_red_cross)
            }

            // Flow Insp sensor
            if (readInspFlowCalibrationStatus()) {
                tvInspFlowSensor.text = readInspFlowCalibrationDate()
                ivInspFlowSensorStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                tvInspFlowSensor.text = getString(R.string.sensore_not_calibrated)
                ivInspFlowSensorStatus.setImageResource(R.drawable.ic_red_cross)
            }

            // leak test sensor
            if (readLeakTestCalibrationStatus()) {
                tvLeakTest.text = readLeakTestCalibrationDate()
                ivLeakTestStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                tvLeakTest.text = getString(R.string.sensore_not_calibrated)
                ivLeakTestStatus.setImageResource(R.drawable.ic_red_cross)
            }
        }
    }

    private fun sendCalibrationCommandToVentilator(sensorTag: String) {

        communicationService?.takeIf { it.isPortsConnected }?.apply {
            communicationService?.send("CM+" + Configs.PREFIX_SENSOR_CALIBRATION + sensorTag)
        }
    }


    fun updateValueOnKnobChange(data: String?) {
        when (data) {
            Configs.PREFIX_PLUS -> incDataOnView()
            Configs.PREFIX_MINUS -> decDataOnView()
            Configs.PREFIX_AND -> setDataOnView()
        }
    }


    private fun incDataOnView() {

//        val value = txtLeakFactor.text.toString()
//        if (value.toInt() + step <= maxOfLeak) txtLeakFactor.text =
//            "${value.toInt() + step}"
    }

    private fun decDataOnView() {

//        val value = txtLeakFactor.text.toString()
//        if (value.toInt() - step >= minOfLeak) txtLeakFactor.text =
//            (value.toInt() - step).toString()

    }

    private fun setDataOnView() {
//        prefManager?.setLeakFactor(txtLeakFactor.text.toString().toInt())
//        Log.i("send_leak_factor",prefManager?.readLeakFactor().toString())
    }

}