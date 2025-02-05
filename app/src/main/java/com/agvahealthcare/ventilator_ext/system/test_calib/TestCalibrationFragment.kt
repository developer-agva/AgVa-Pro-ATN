package com.agvahealthcare.ventilator_ext.system.test_calib

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_test_calib.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestCalibrationFragment(private var communicationService: CommunicationService?) : Fragment(),
    View.OnClickListener {

    companion object {
        const val TAG = "TestCalibrationFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_test_calib, container, false)
    }

    private var o2CalibrateDialog: AlertDialog? = null
    private var prefManager: PreferenceManager? = null
    private var currentTag: String = ""

    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> if (topBarTestCalib.isVisible) backBtnTestCalib.callOnClick() else includeButtonO2Sensor.buttonView.callOnClick()
            1 -> if (topBarTestCalib.isVisible) includeButtonSendCmd.buttonView.callOnClick() else includeButtonExhaleValve.buttonView.callOnClick()
            2 -> includeButtonExpFlowSensor.buttonView.callOnClick()

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
            constraintSet.clone(mainViewPanelTestCalib)
            constraintSet.clear(focusLayoutTestCalib.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutTestCalib.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutTestCalib.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutTestCalib.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelTestCalib)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelTestCalib)
        constraintSet.connect(
            focusLayoutTestCalib.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutTestCalib.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutTestCalib.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutTestCalib.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelTestCalib)
    }

    //
    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {
            return when (highlightedIndex) {

                0 -> if (topBarTestCalib.isVisible) backBtnTestCalib else includeButtonO2Sensor
                1 -> if (topBarTestCalib.isVisible) includeButtonSendCmd else includeButtonExhaleValve
                2 -> includeButtonExpFlowSensor

                else -> null
            }
        } ?: run {
            return null
        }
    }
    // logic knob highlight ends here


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())
        setUpView()
        setUpOnClickListener()
    }

    @SuppressLint("SetTextI18n")
    private fun setUpView() {
        includeButtonExpFlowSensor.buttonView.text = getString(R.string.hint_Exp_Flow_Calibration)
        includeButtonO2Sensor.buttonView.text = getString(R.string.hint_O2_Calibration)
        includeButtonExhaleValve.buttonView.text = getString(R.string.hint_ExhaleValve_Calibration)
        includeButtonSendCmd.buttonView.text = "START CALIBRATION"
        includeButtonSendCmd.buttonView.textAlignment = View.TEXT_ALIGNMENT_INHERIT
        includeButtonSendCmd.buttonView.setBackgroundColor(R.drawable.background_black_border_white)
        includeButtonSendCmd.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        updateSensorCalibrationStatus()
        hideGoneFunction(true)
    }


    private fun setUpOnClickListener() {
        if (tag == "FromDashboard") {
            includeButtonExpFlowSensor.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }
            includeButtonO2Sensor.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

            includeButtonExhaleValve.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

        } else {
            includeButtonExpFlowSensor.buttonView.setOnClickListener(this)
            includeButtonO2Sensor.buttonView.setOnClickListener(this)
            includeButtonExhaleValve.buttonView.setOnClickListener(this)
            includeButtonSendCmd.buttonView.setOnClickListener(this)
        }

        backBtnTestCalib.setOnClickListener {
            (parentFragment as SystemDialogFragment).highlightedIndex = -1
            (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 2

            hideGoneFunction(true)
        }

    }


    private fun hideGoneFunction(isCalib: Boolean) {

        if (isCalib) {
            // pre op check layout
            topBarTestCalib.visibility = View.GONE
            backBtnTestCalib.visibility = View.GONE
            tvMainTitleTestCalib.visibility = View.GONE
            ventigif.visibility = View.GONE
            capgif.visibility = View.GONE
            calibratorimg.visibility = View.GONE
            tvtext1.visibility = View.GONE
            tvtext2.visibility = View.GONE
            tvtext3.visibility = View.GONE
            tvtextHeadingTestCalib.visibility = View.GONE
            includeButtonSendCmd.visibility = View.GONE

            // calib layouts
            testCalibText.visibility = View.VISIBLE
            includeButtonExpFlowSensor.visibility = View.VISIBLE
            includeButtonO2Sensor.visibility = View.VISIBLE
            includeButtonExhaleValve.visibility = View.VISIBLE
            ivExpFlowSensorStatus.visibility = View.VISIBLE
            ivO2SensorStatus.visibility = View.VISIBLE
            ivExhaleValveStatus.visibility = View.VISIBLE
            tvO2Sensor.visibility = View.VISIBLE
            tvExhaleValve.visibility = View.VISIBLE
            tvExpFlowSensor.visibility = View.VISIBLE

        } else {
            // pre op check layout
            topBarTestCalib.visibility = View.VISIBLE
            backBtnTestCalib.visibility = View.VISIBLE
            tvMainTitleTestCalib.visibility = View.VISIBLE
            tvtext1.visibility = View.VISIBLE
            tvtext2.visibility = View.VISIBLE
            tvtext3.visibility = View.VISIBLE
            tvtextHeadingTestCalib.visibility = View.VISIBLE
            includeButtonSendCmd.visibility = View.VISIBLE

            // calib layouts
            testCalibText.visibility = View.GONE
            includeButtonExpFlowSensor.visibility = View.GONE
            includeButtonO2Sensor.visibility = View.GONE
            includeButtonExhaleValve.visibility = View.GONE
            ivExpFlowSensorStatus.visibility = View.GONE
            ivO2SensorStatus.visibility = View.GONE
            ivExhaleValveStatus.visibility = View.GONE
            tvO2Sensor.visibility = View.GONE
            tvExhaleValve.visibility = View.GONE
            tvExpFlowSensor.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {

        when (v) {
            includeButtonExpFlowSensor.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                currentTag = "Exp Flow"
                hideGoneFunction(false)
                tvMainTitleTestCalib.text = "Exp Flow sensor pre-calibration check"
                tvtext1.text = "1. Connect calibration tubing"
                tvtext2.text = "2. Make sure the ventilator is connected to mains supply"
                tvtext3.text = ""
                ventigif.visibility = View.VISIBLE
                capgif.visibility = View.GONE
                calibratorimg.visibility = View.GONE
            }

            includeButtonO2Sensor.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                currentTag = "Oxygen"
                hideGoneFunction(false)
                tvMainTitleTestCalib.text = "Oxygen pre-calibration check"
                tvtext1.text = "1. Make sure the ventilator is connected to mains supply"
                tvtext2.text = "2. Connect calibration tubing"
                tvtext3.text = "3. Ensure ventilator is connected to high pressure O2 line"
                ventigif.visibility = View.VISIBLE
                capgif.visibility = View.GONE
                calibratorimg.visibility = View.GONE
            }

            includeButtonExhaleValve.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                currentTag = "exhale_valve"
                hideGoneFunction(false)
                tvMainTitleTestCalib.text = "Exhale valve pre-calibration check"
                tvtext1.text = "1. Make sure the ventilator is connected to mains supply"
                tvtext2.text = "2. Connect calibration tubing"
                tvtext3.text = "3. Ensure the exhale valve is connected properly"
                ventigif.visibility = View.VISIBLE
                capgif.visibility = View.GONE
                calibratorimg.visibility = View.GONE

            }

            includeButtonSendCmd.buttonView -> {
                when (currentTag) {
                    "exhale_valve" -> { sendCalibrationCommandToVentilator(Configs.TAG_SENSOR_EXHALE_VALVE) }

                    "Oxygen" -> { sendCalibrationCommandToVentilator(Configs.TAG_SENSOR_OXYGEN) }

                    "Exp Flow" -> { sendCalibrationCommandToVentilator(Configs.TAG_SENSOR_EXP_FLOW) }
                }

            }

        }
    }


    fun updateSensorCalibrationStatus() {

        hideGoneFunction(true)
        prefManager?.apply {
            Log.i("CALIBCHECK", "Sensor data is refreshing on the view......")

            if (readOxygenCalibrationStatus()) {
                tvO2Sensor.text = readOxygenCalibrationDate()
                ivO2SensorStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                tvO2Sensor.text = getString(R.string.sensore_not_calibrated)
                ivO2SensorStatus.setImageResource(R.drawable.ic_red_cross)
            }

            // Flow Exp sensor
            if (readExpFlowCalibrationStatus()) {
                tvExpFlowSensor.text = readExpFlowCalibrationDate()
                ivExpFlowSensorStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                tvExpFlowSensor.text = getString(R.string.sensore_not_calibrated)
                ivExpFlowSensorStatus.setImageResource(R.drawable.ic_red_cross)
            }

            if (readExhaleValveCalibrationStatus()) {
                tvExhaleValve.text = readExhaleValveCalibrationDate()
                ivExhaleValveStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                tvExhaleValve.text = getString(R.string.sensore_not_calibrated)
                ivExhaleValveStatus.setImageResource(R.drawable.ic_red_cross)
            }
        }
    }

    private fun sendCalibrationCommandToVentilator(sensorTag: String) {

        communicationService?.takeIf { it.isPortsConnected }?.apply {
            communicationService?.send("CM+" + Configs.PREFIX_SENSOR_CALIBRATION + sensorTag)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateOxygenCalibrateProgressStatus(progress: Int, msg: String, textAlignment: Int) {
        o2CalibrateDialog?.takeIf { it.isShowing }?.apply {
            val tvProgressCount = o2CalibrateDialog?.findViewById<TextView>(R.id.tvProgress)
            val tvProgressMsg = o2CalibrateDialog?.findViewById<TextView>(R.id.tvProgressMsg)
            if (progress >= 0) {
                tvProgressCount?.visibility = View.VISIBLE
                tvProgressCount?.text = "$progress%"
            } else {
                tvProgressCount?.visibility = View.GONE
            }
            tvProgressMsg?.text = msg
            tvProgressMsg?.textAlignment = textAlignment
        }
    }
}
