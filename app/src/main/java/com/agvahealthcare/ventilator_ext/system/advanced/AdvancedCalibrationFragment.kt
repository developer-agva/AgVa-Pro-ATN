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
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.databinding.FragmentAdvancedCalibrationBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs


class AdvancedCalibrationFragment(private var communicationService: CommunicationService?) :
    Fragment(), View.OnClickListener {

    companion object {
        const val TAG = "AdvancedCalibrationFragment"
    }
    private lateinit var binding: FragmentAdvancedCalibrationBinding
    var visibilityTimeout: CountDownTimer? = null
    private var maxOfLeak = 0
    private var minOfLeak = 0
    private var step = 5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAdvancedCalibrationBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    private var prefManager: PreferenceManager? = null
    private var currentTag: String = ""

    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> if (binding.topBarAdvancedCalib.isVisible) binding.backBtnAdvancedCalib.callOnClick() else binding.includeButtonTurbine.buttonView.callOnClick()
            1 -> if (binding.topBarAdvancedCalib.isVisible) binding.includeButtonSendCmdAdvancedCalib.buttonView.callOnClick() else binding.includeButtonInspFlowSensor.buttonView.callOnClick()
            2 -> binding.includeButtonLeakTest.buttonView.callOnClick()
            3 -> binding.includeButtonNeoZero.buttonView.callOnClick()
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
            constraintSet.clone(binding.mainViewPanelAdvancedCalib)
            constraintSet.clear(binding.focusLayoutAdvancedCalib.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutAdvancedCalib.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutAdvancedCalib.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutAdvancedCalib.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelAdvancedCalib)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelAdvancedCalib)
        constraintSet.connect(
            binding.focusLayoutAdvancedCalib.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutAdvancedCalib.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutAdvancedCalib.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutAdvancedCalib.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelAdvancedCalib)
    }

    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {
            return when (highlightedIndex) {

                0 -> if (binding.topBarAdvancedCalib.isVisible) binding.backBtnAdvancedCalib else binding.includeButtonTurbine.root
                1 -> if (binding.topBarAdvancedCalib.isVisible) binding.includeButtonSendCmdAdvancedCalib.root else binding.includeButtonInspFlowSensor.root
                2 -> binding.includeButtonLeakTest.root
                3 -> binding.includeButtonNeoZero.root

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
            binding.topBarAdvancedCalib.visibility = View.GONE
            binding.backBtnAdvancedCalib.visibility = View.GONE
            binding.tvMainTitleAdvancedCalib.visibility = View.GONE
            binding.ventigif.visibility = View.GONE
            binding.capgif.visibility = View.GONE
            binding.tvtext1AdvancedCalib.visibility = View.GONE
            binding.tvtext2AdvancedCalib.visibility = View.GONE
            binding.tvtext3AdvancedCalib.visibility = View.GONE
            binding.tvtextHeadingAdvancedCalib.visibility = View.GONE
            binding.includeButtonSendCmdAdvancedCalib.root.visibility = View.GONE

            // calib layouts
            binding.advancedCalibText.visibility = View.VISIBLE
            binding.includeButtonTurbine.root.visibility = View.VISIBLE
            binding.includeButtonInspFlowSensor.root.visibility = View.VISIBLE
            binding.includeButtonLeakTest.root.visibility = View.VISIBLE
            binding.includeButtonNeoZero.root.visibility = View.GONE
            binding.ivTurbineSensorStatus.visibility = View.VISIBLE
            binding.ivInspFlowSensorStatus.visibility = View.VISIBLE
            binding.ivLeakTestStatus.visibility = View.VISIBLE
            binding.ivNeoZeroStatus.visibility = View.GONE
            binding.tvTurbineSensor.visibility = View.VISIBLE
            binding.tvInspFlowSensor.visibility = View.VISIBLE
            binding.tvLeakTest.visibility = View.VISIBLE
            binding.tvNeoZero.visibility = View.GONE
        } else {
            // pre op check layout
            binding.topBarAdvancedCalib.visibility = View.VISIBLE
            binding.backBtnAdvancedCalib.visibility = View.VISIBLE
            binding.tvMainTitleAdvancedCalib.visibility = View.VISIBLE
            binding.tvtext1AdvancedCalib.visibility = View.VISIBLE
            binding.tvtext2AdvancedCalib.visibility = View.VISIBLE
            binding.tvtext3AdvancedCalib.visibility = View.VISIBLE
            binding.tvtextHeadingAdvancedCalib.visibility = View.VISIBLE
            binding.includeButtonSendCmdAdvancedCalib.root.visibility = View.VISIBLE

            // calib layouts
            binding.advancedCalibText.visibility = View.GONE
            binding.includeButtonTurbine.root.visibility = View.GONE
            binding.includeButtonInspFlowSensor.root.visibility = View.GONE
            binding.includeButtonNeoZero.root.visibility = View.GONE
            binding.includeButtonLeakTest.root.visibility = View.GONE
            binding.ivTurbineSensorStatus.visibility = View.GONE
            binding.ivInspFlowSensorStatus.visibility = View.GONE
            binding.ivLeakTestStatus.visibility = View.GONE
            binding.ivNeoZeroStatus.visibility = View.GONE
            binding.tvTurbineSensor.visibility = View.GONE
            binding.tvInspFlowSensor.visibility = View.GONE
            binding.tvLeakTest.visibility = View.GONE
            binding.tvNeoZero.visibility = View.GONE
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
        binding.includeButtonTurbine.buttonView.text = getString(R.string.hint_Turbine_Calibration)
        binding.includeButtonInspFlowSensor.buttonView.text = getString(R.string.hint__Insp_Flow_Calibration)
        binding.includeButtonSendCmdAdvancedCalib.buttonView.text = "START CALIBRATION"
        binding.includeButtonLeakTest.buttonView.text = getString(R.string.hint_Leak_Test_Text)
        binding.includeButtonNeoZero.buttonView.text = getString(R.string.hint_Neo_Zero_Calibration)
        binding.includeButtonSendCmdAdvancedCalib.buttonView.textAlignment = View.TEXT_ALIGNMENT_INHERIT
        binding.includeButtonSendCmdAdvancedCalib.buttonView.setBackgroundColor(R.drawable.background_black_border_white)
        binding.includeButtonSendCmdAdvancedCalib.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white) )
        updateSensorCalibrationStatus()
    }

    private fun setUpOnClickListener() {
        if (tag == "FromDashboard") {

            binding.includeButtonTurbine.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

            binding.includeButtonInspFlowSensor.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

            binding.includeButtonLeakTest.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

            binding.includeButtonNeoZero.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }

        } else {
            binding.includeButtonTurbine.buttonView.setOnClickListener(this)
            binding.includeButtonInspFlowSensor.buttonView.setOnClickListener(this)
            binding.includeButtonSendCmdAdvancedCalib.buttonView.setOnClickListener(this)
            binding.includeButtonLeakTest.buttonView.setOnClickListener(this)
            binding.includeButtonNeoZero.buttonView.setOnClickListener(this)
        }

        binding.backBtnAdvancedCalib.setOnClickListener {
            (parentFragment as SystemDialogFragment).highlightedIndex = -1
            (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 3
            hideGoneFunction(true)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {

        when (v) {
            binding.includeButtonTurbine.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                hideGoneFunction(false)
                currentTag = "Turbine"
                binding.tvMainTitleAdvancedCalib.text = "Turbine pre-calibration check"
                binding.tvtext1AdvancedCalib.text = "1. Disconnect patient tubing"
                binding.tvtext2AdvancedCalib.text = "2. Insert calibration cap at inspiratory port"
                binding.tvtext3AdvancedCalib.text = "3. Make sure the ventilator is connected to mains supply"
                binding.includeButtonSendCmdAdvancedCalib.buttonView.text = "Start calibration"
                binding.capgif.visibility = View.VISIBLE
                binding.ventigif.visibility = View.GONE
            }

            binding.includeButtonInspFlowSensor.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                hideGoneFunction(false)
                currentTag = "Insp Flow"
                binding.tvMainTitleAdvancedCalib.text = "Insp Flow sensor pre-calibration check"
                binding.tvtext1AdvancedCalib.text = "1. Connect external flow calibrator to inspiratory port"
                binding.tvtext2AdvancedCalib.text = "2. Make sure the ventilator is connected to mains supply"
                binding.tvtext3AdvancedCalib.text = ""
                binding.ventigif.visibility = View.GONE
                binding.capgif.visibility = View.GONE
                binding.includeButtonSendCmdAdvancedCalib.buttonView.text = "Start calibration"
            }

            binding.includeButtonLeakTest.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                hideGoneFunction(false)
                currentTag = "Leak Test"
                binding.tvMainTitleAdvancedCalib.text = "System Leak Test"
                binding.tvtext1AdvancedCalib.text = "1. Insert calibration cap at inspiratory port"
                binding.tvtext2AdvancedCalib.text = "2. Make sure the ventilator is connected to mains supply"
                binding.tvtext3AdvancedCalib.text = ""
                binding.capgif.visibility = View.VISIBLE
                binding.ventigif.visibility = View.GONE
                binding.includeButtonSendCmdAdvancedCalib.buttonView.text = "Start leak test"
            }

            binding.includeButtonNeoZero.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
                hideGoneFunction(false)
                currentTag = "Neo Zero"
                binding.tvMainTitleAdvancedCalib.text = "Neonate Sensor Zeroing"
                binding.tvtext1AdvancedCalib.text = "1. Insert Neonate Cable at Neonate port"
                binding.tvtext2AdvancedCalib.text = "2. Make sure neonate sensor is connected to neonate cable"
                binding.tvtext3AdvancedCalib.text = "3. Make sure the ventilator is connected to mains supply"
                binding.capgif.visibility = View.GONE
                binding.ventigif.visibility = View.GONE
                binding.includeButtonSendCmdAdvancedCalib.buttonView.text = "Start Zeroing"
            }

            binding.includeButtonSendCmdAdvancedCalib.buttonView -> {
                when (currentTag) {
                    "Turbine" -> { sendCalibrationCommandToVentilator(Configs.TAG_SENSOR_TURBINE) }
                    "Leak Test" -> { sendCalibrationCommandToVentilator(Configs.TAG_LEAK_TEST) }
                    "Insp Flow" -> { sendCalibrationCommandToVentilator(Configs.TAG_SENSOR_INSP_FLOW) }
                    "Neo Zero" -> { sendCalibrationCommandToVentilator(Configs.TAG_NEO_ZERO) }
                }
            }
        }
    }

    fun updateSensorCalibrationStatus() {
        hideGoneFunction(true)
        prefManager?.apply {

            // Turbine sensor
            if (readTurbineCalibrationStatus()) {
                binding.tvTurbineSensor.text = readTurbineCalibrationDate()
                binding.ivTurbineSensorStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                binding.tvTurbineSensor.text = getString(R.string.sensore_not_calibrated)
                binding.ivTurbineSensorStatus.setImageResource(R.drawable.ic_red_cross)
            }

            // Flow Insp sensor
            if (readInspFlowCalibrationStatus()) {
                binding.tvInspFlowSensor.text = readInspFlowCalibrationDate()
                binding.ivInspFlowSensorStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                binding.tvInspFlowSensor.text = getString(R.string.sensore_not_calibrated)
                binding.ivInspFlowSensorStatus.setImageResource(R.drawable.ic_red_cross)
            }

            // leak test sensor
            if (readLeakTestCalibrationStatus()) {
                binding.tvLeakTest.text = readLeakTestCalibrationDate()
                binding.ivLeakTestStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                binding.tvLeakTest.text = getString(R.string.sensore_not_calibrated)
                binding.ivLeakTestStatus.setImageResource(R.drawable.ic_red_cross)
            }

            // Neo Zero
            if (readNeoZeroStatus()) {
                binding.tvNeoZero.text = readNeoZeroDate()
                binding.ivNeoZeroStatus.setImageResource(R.drawable.ic_green_circle_tick)
            } else {
                binding.tvNeoZero.text = getString(R.string.sensore_not_calibrated)
                binding.ivNeoZeroStatus.setImageResource(R.drawable.ic_red_cross)
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