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
import com.agvahealthcare.ventilator_ext.databinding.FragmentTestCalibBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs

class TestCalibrationFragment(private var communicationService: CommunicationService?) : Fragment(),
    View.OnClickListener {

    companion object {
        const val TAG = "TestCalibrationFragment"
    }
    private lateinit var binding: FragmentTestCalibBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTestCalibBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    private var o2CalibrateDialog: AlertDialog? = null
    private var prefManager: PreferenceManager? = null
    private var currentTag: String = ""

    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> if (binding.topBarTestCalib.isVisible) binding.backBtnTestCalib.callOnClick() else binding.includeButtonO2Sensor.buttonView.callOnClick()
            1 -> if (binding.topBarTestCalib.isVisible) binding.includeButtonSendCmd.buttonView.callOnClick() else binding.includeButtonExhaleValve.buttonView.callOnClick()
            2 -> binding.includeButtonExpFlowSensor.buttonView.callOnClick()

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
            constraintSet.clone(binding.mainViewPanelTestCalib)
            constraintSet.clear(binding.focusLayoutTestCalib.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutTestCalib.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutTestCalib.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutTestCalib.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelTestCalib)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelTestCalib)
        constraintSet.connect(
            binding.focusLayoutTestCalib.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutTestCalib.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutTestCalib.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutTestCalib.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelTestCalib)
    }

    //
    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {
            return when (highlightedIndex) {

                0 -> if (binding.topBarTestCalib.isVisible) binding.backBtnTestCalib else binding.includeButtonO2Sensor.root
                1 -> if (binding.topBarTestCalib.isVisible) binding.includeButtonSendCmd.root else binding.includeButtonExhaleValve.root
                2 -> binding.includeButtonExpFlowSensor.root

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
        binding.includeButtonExpFlowSensor.buttonView.text = getString(R.string.hint_Exp_Flow_Calibration)
        binding.includeButtonO2Sensor.buttonView.text = getString(R.string.hint_O2_Calibration)
        binding.includeButtonExhaleValve.buttonView.text = getString(R.string.hint_ExhaleValve_Calibration)
        binding.includeButtonSendCmd.buttonView.text = "START CALIBRATION"
        binding.includeButtonSendCmd.buttonView.textAlignment = View.TEXT_ALIGNMENT_INHERIT
        binding.includeButtonSendCmd.buttonView.setBackgroundColor(R.drawable.background_black_border_white)
        binding.includeButtonSendCmd.buttonView.setTextColor(
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
            binding.includeButtonExpFlowSensor.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }
            binding.includeButtonO2Sensor.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

            binding.includeButtonExhaleValve.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

        } else {
            binding.includeButtonExpFlowSensor.buttonView.setOnClickListener(this)
            binding.includeButtonO2Sensor.buttonView.setOnClickListener(this)
            binding.includeButtonExhaleValve.buttonView.setOnClickListener(this)
            binding.includeButtonSendCmd.buttonView.setOnClickListener(this)
        }

        binding.backBtnTestCalib.setOnClickListener {
            (parentFragment as SystemDialogFragment).highlightedIndex = -1
            (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 2

            hideGoneFunction(true)
        }

    }


    private fun hideGoneFunction(isCalib: Boolean) {

        if (isCalib) {
            // pre op check layout
            binding.topBarTestCalib.visibility = View.GONE
            binding.backBtnTestCalib.visibility = View.GONE
            binding.tvMainTitleTestCalib.visibility = View.GONE
            binding.ventigif.visibility = View.GONE
            binding.capgif.visibility = View.GONE
            binding.calibratorimg.visibility = View.GONE
            binding.tvtext1.visibility = View.GONE
            binding.tvtext2.visibility = View.GONE
            binding.tvtext3.visibility = View.GONE
            binding.tvtextHeadingTestCalib.visibility = View.GONE
            binding.includeButtonSendCmd.root.visibility = View.GONE

            // calib layouts
            binding.testCalibText.visibility = View.VISIBLE
            binding.includeButtonExpFlowSensor.root.visibility = View.VISIBLE
            binding.includeButtonO2Sensor.root.visibility = View.VISIBLE
            binding.includeButtonExhaleValve.root.visibility = View.VISIBLE
            binding.ivExpFlowSensorStatus.visibility = View.VISIBLE
            binding.ivO2SensorStatus.visibility = View.VISIBLE
            binding.ivExhaleValveStatus.visibility = View.VISIBLE
            binding.tvO2Sensor.visibility = View.VISIBLE
            binding.tvExhaleValve.visibility = View.VISIBLE
            binding.tvExpFlowSensor.visibility = View.VISIBLE

        } else {
            // pre op check layout
            binding.topBarTestCalib.visibility = View.VISIBLE
            binding.backBtnTestCalib.visibility = View.VISIBLE
            binding.tvMainTitleTestCalib.visibility = View.VISIBLE
            binding.tvtext1.visibility = View.VISIBLE
            binding.tvtext2.visibility = View.VISIBLE
            binding.tvtext3.visibility = View.VISIBLE
            binding.tvtextHeadingTestCalib.visibility = View.VISIBLE
            binding.includeButtonSendCmd.root.visibility = View.VISIBLE

            // calib layouts
            binding.testCalibText.visibility = View.GONE
            binding.includeButtonExpFlowSensor.root.visibility = View.GONE
            binding.includeButtonO2Sensor.root.visibility = View.GONE
            binding.includeButtonExhaleValve.root.visibility = View.GONE
            binding.ivExpFlowSensorStatus.visibility = View.GONE
            binding.ivO2SensorStatus.visibility = View.GONE
            binding.ivExhaleValveStatus.visibility = View.GONE
            binding.tvO2Sensor.visibility = View.GONE
            binding.tvExhaleValve.visibility = View.GONE
            binding.tvExpFlowSensor.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {

        when (v) {
            binding.includeButtonExpFlowSensor.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                currentTag = "Exp Flow"
                hideGoneFunction(false)
                binding.tvMainTitleTestCalib.text = "Exp Flow sensor pre-calibration check"
                binding.tvtext1.text = "1. Connect calibration tubing"
                binding.tvtext2.text = "2. Make sure the ventilator is connected to mains supply"
                binding.tvtext3.text = ""
                binding.ventigif.visibility = View.VISIBLE
                binding.capgif.visibility = View.GONE
                binding.calibratorimg.visibility = View.GONE
            }

            binding.includeButtonO2Sensor.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                currentTag = "Oxygen"
                hideGoneFunction(false)
                binding.tvMainTitleTestCalib.text = "Oxygen pre-calibration check"
                binding.tvtext1.text = "1. Make sure the ventilator is connected to mains supply"
                binding.tvtext2.text = "2. Connect calibration tubing"
                binding.tvtext3.text = "3. Ensure ventilator is connected to high pressure O2 line"
                binding.ventigif.visibility = View.VISIBLE
                binding.capgif.visibility = View.GONE
                binding.calibratorimg.visibility = View.GONE
            }

            binding.includeButtonExhaleValve.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                currentTag = "exhale_valve"
                hideGoneFunction(false)
                binding.tvMainTitleTestCalib.text = "Exhale valve pre-calibration check"
                binding.tvtext1.text = "1. Make sure the ventilator is connected to mains supply"
                binding.tvtext2.text = "2. Connect calibration tubing"
                binding.tvtext3.text = "3. Ensure the exhale valve is connected properly"
                binding.ventigif.visibility = View.VISIBLE
                binding.capgif.visibility = View.GONE
                binding.calibratorimg.visibility = View.GONE

            }

            binding.includeButtonSendCmd.buttonView -> {
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
                binding.tvO2Sensor.text = readOxygenCalibrationDate()
                binding.ivO2SensorStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                binding.tvO2Sensor.text = getString(R.string.sensore_not_calibrated)
                binding.ivO2SensorStatus.setImageResource(R.drawable.ic_red_cross)
            }

            // Flow Exp sensor
            if (readExpFlowCalibrationStatus()) {
                binding.tvExpFlowSensor.text = readExpFlowCalibrationDate()
                binding.ivExpFlowSensorStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                binding.tvExpFlowSensor.text = getString(R.string.sensore_not_calibrated)
                binding.ivExpFlowSensorStatus.setImageResource(R.drawable.ic_red_cross)
            }

            if (readExhaleValveCalibrationStatus()) {
                binding.tvExhaleValve.text = readExhaleValveCalibrationDate()
                binding.ivExhaleValveStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                binding.tvExhaleValve.text = getString(R.string.sensore_not_calibrated)
                binding.ivExhaleValveStatus.setImageResource(R.drawable.ic_red_cross)
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
