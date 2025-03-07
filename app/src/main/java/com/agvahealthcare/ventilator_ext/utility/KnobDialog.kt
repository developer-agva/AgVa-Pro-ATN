package com.agvahealthcare.ventilator_ext.utility

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.alarm.limit_one.EncoderValue
import com.agvahealthcare.ventilator_ext.alarm.limit_one.KnobParameterModel
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.OnKnobPressListener
import com.agvahealthcare.ventilator_ext.callback.OnLimitChangeListener
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.databinding.ProgressDialogViewBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*
import com.uk.tastytoasty.TastyToasty

class KnobDialog : DialogFragment() {

    private var onKnobPressListener: OnKnobPressListener? = null
    private var onTimeoutListener: OnDismissDialogListener? = null
    private var onCloseListener: OnDismissDialogListener? = null
    private var onLimitChangeListener: OnLimitChangeListener? = null
    private lateinit var binding : ProgressDialogViewBinding

    //Parameter model needs to be changed to null safety
    private lateinit var parameterModel: KnobParameterModel
    private lateinit var encoderValue: EncoderValue
    private var isCloseListenerAvoided = false
    private var modeCode: Int? = null
    private var currentValue: Float = 0f
    private var actualValue: Int = 0
    public var cancelableStatus: Boolean = false;
    private var prefManager: PreferenceManager? = null
    private var mDashBoardViewModel: DashBoardViewModel? = null

    private var countOfMaxLimit = 0

    companion object {

        const val TAG = "SimpleDialog"
        private const val KEY_STATUS = "KEY_STATUS"
        private const val KEY_WIDTH = "KEY_WIDTH"

        fun newInstance(
            onKnobPressListener: OnKnobPressListener,
            parameterModel: KnobParameterModel,
            encoderValue: EncoderValue,
            cancelableStatus: Boolean = true,
            height: Int? = 0,
            width: Int? = 975,
            // position: UnitPosition? =
            onTimeoutListener: OnDismissDialogListener? = null,
            onCloseListener: OnDismissDialogListener? = null,
            onLimitChangeListener: OnLimitChangeListener? = null
        ): KnobDialog {
            return KnobDialog().apply {
                this.onKnobPressListener = onKnobPressListener
                this.onTimeoutListener = onTimeoutListener
                this.onCloseListener = onCloseListener
                this.onLimitChangeListener = onLimitChangeListener
                this.parameterModel = parameterModel
                this.encoderValue = encoderValue
                this.cancelableStatus = cancelableStatus
            }
        }
    }

    var visibilityTimeout: CountDownTimer? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        prefManager = PreferenceManager(requireContext());
        mDashBoardViewModel =
            ViewModelProvider(requireActivity()).get(DashBoardViewModel::class.java)
        binding = ProgressDialogViewBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
        if (prefManager?.readKnobStatus() == false && tag == "FromDashBoardActivity") binding.cpBgView.visibility =View.GONE
        else binding.cpBgView.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        setHeightWidth(prefManager,tag)
    }

    override fun onDestroy() {
        cancelTimeout()

        if (!isCloseListenerAvoided) onCloseListener?.handleDialogClose()
        super.onDestroy()
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        hideSystemUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    private fun setupView(view: View) {

        binding.includeAccept.buttonView.text = "Accept"
        binding.includeAccept.buttonView.textAlignment = View.TEXT_ALIGNMENT_CENTER

        binding.textMinRange.text = encoderValue.lowerLimit.toInt().toString()
        binding.textMaxRange.text = encoderValue.upperLimit.toInt().toString()

        currentValue = parameterModel.reading

        if (isDecimalSupported(parameterModel.key)) {
            binding.seekBarId.min = (encoderValue.lowerLimit * 10)
            binding.seekBarId.max = (encoderValue.upperLimit * 10)
            binding.seekBarId.progress = (currentValue * 10)
        } else {
            binding.seekBarId.min = encoderValue.lowerLimit
            binding.seekBarId.max = encoderValue.upperLimit
            binding.seekBarId.progress = currentValue
        }

        binding.imageViewSubtract.setOnClickListener {

            if (tag == "LimitOneFragment") subtractionForLimitOne()
            else subtraction()
        }

        binding.imageViewAddition.setOnClickListener {

            if (tag == "LimitOneFragment") {
                additionForLimitOne()
            } else if (tag == "FromDashBoardActivity") {
                modeCode = (activity as DashBoardActivity).modeCode

                if (parameterModel.key == LBL_PPLAT || parameterModel.key == LBL_PEEP || parameterModel.key == LBL_SUPPORT_PRESSURE) additionForTwoTiles()
                else addition()
            } else if (tag == "FromMainActivity") {
                modeCode = (activity as MainActivity).requestedModeCode

                if (parameterModel.key == LBL_PPLAT || parameterModel.key == LBL_PEEP || parameterModel.key == LBL_SUPPORT_PRESSURE) additionForTwoTiles()
                else addition()
            } else {
                addition()
            }
        }

        binding.includeAccept.buttonView.setOnClickListener {
            ok()
        }

    }

    fun updateWithTimeoutDebounce(value: String) {

        when (value) {
            PREFIX_PLUS -> {

                if (tag == "LimitOneFragment") {
                    Log.i("value_limit_one_fragent", "incondtion")
                    additionForLimitOne()
                } else if (tag == "FromDashBoardActivity") {
                    modeCode = (activity as DashBoardActivity).modeCode

                    if (parameterModel.key == LBL_PPLAT || parameterModel.key == LBL_PEEP || parameterModel.key == LBL_SUPPORT_PRESSURE) additionForTwoTiles()
                    else addition()
                } else if (tag == "FromMainActivity") {
                    modeCode = (activity as MainActivity).requestedModeCode

                    if (parameterModel.key == LBL_PPLAT || parameterModel.key == LBL_PEEP || parameterModel.key == LBL_SUPPORT_PRESSURE) additionForTwoTiles()
                    else addition()
                } else {
                    addition()
                }
            }
            PREFIX_MINUS -> {
                if (tag == "LimitOneFragment") subtractionForLimitOne()
                else subtraction()
            }
            PREFIX_AND -> ok()

        }

    }

    fun startTimeoutWithDebounce() {

        cancelTimeout()

        visibilityTimeout = object : CountDownTimer(10000, 2000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                onTimeoutListener?.handleDialogClose()
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

    private fun isIRVActive() = prefManager?.readIRVStatus() ?: true
    private fun isIRVActiveTemp() = prefManager?.readIRVStatusTemp() ?: true

    private fun getIERatioLimits(): Pair<Float, Float> {
        var max = 0.0f
        var min = 0.0f

        if (VentilatorApp.isFromControlFragment == true) {
            max =
                requireContext().getString(if (isIRVActive()) R.string.max_ie_ratio_irv else R.string.max_ie_ratio)
                    .toFloat();
            min =
                requireContext().getString(if (isIRVActive()) R.string.min_ie_ratio_irv else R.string.min_ie_ratio)
                    .toFloat();
        } else {
            max =
                requireContext().getString(if ((isIRVActiveTemp())) R.string.max_ie_ratio_irv else R.string.max_ie_ratio)
                    .toFloat();
            min =
                requireContext().getString(if (isIRVActiveTemp()) R.string.min_ie_ratio_irv else R.string.min_ie_ratio)
                    .toFloat();
        }

        return Pair<Float, Float>(min, max);
    }

    private fun isIERatioValid(rr: Int?, tinsp: Float?): Boolean {

        if (rr != null && tinsp != null) {
            val (minIERatio, maxIERatio) = getIERatioLimits()
            val calculatedIERatio = Configs.calculateIERatio(rr, tinsp)
            VentilatorApp.IERatio = calculatedIERatio
            return try {
                val eiRatio = calculatedIERatio.split(":")[1].trim().toFloat()
                eiRatio in minIERatio..maxIERatio
            } catch (err: Error) {
                true
            }
        } else
            return true
    }

    private fun addition() {
        startTimeoutWithDebounce()
        val newValue = floatingPointFix(currentValue + encoderValue.step)
        var isNewValueValid = newValue <= encoderValue.upperLimit
        if (parameterModel.key == LBL_TINSP || parameterModel.key == LBL_RR) {
            val rr = if (parameterModel.key == LBL_RR) newValue.toInt() else {

                VentilatorApp.testingConditonMap.get(LBL_RR)?.let {
                    it.toInt()
                } ?: kotlin.run {
                    prefManager?.readRR()?.toInt()
                }
            }
            val tinsp = if (parameterModel.key == LBL_TINSP) newValue else {

                VentilatorApp.testingConditonMap.get(LBL_TINSP)?.let {
                    it
                } ?: kotlin.run {
                    prefManager?.readTinsp()
                }
            }

            isNewValueValid = isNewValueValid && isIERatioValid(rr, tinsp)
        }

        if (isNewValueValid) {
            currentValue = newValue
            countOfMaxLimit = 1
        } else {
            if (countOfMaxLimit == 1) {
                TastyToasty.orange(
                    requireContext(),
                    "${parameterModel.key} upper limit reached",
                    R.drawable.ic_info_small
                ).show();
                countOfMaxLimit = 0
            }
        }
        // display value
        val displayValue = if (isDecimalSupported(parameterModel.key)) String.format(
            "%.1f",
            currentValue
        ) else currentValue.toInt().toString()

        // notify change in value
        onLimitChangeListener?.onLimitChange(parameterModel.reading, currentValue)

        // seekbar
        if (isDecimalSupported(parameterModel.key)) binding.seekBarId.changeProgress(currentValue * 10)
        else binding.seekBarId.changeProgress(currentValue)
    }

    private fun additionForLimitOne() {
        startTimeoutWithDebounce()
        if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
            if (parameterModel.key == LBL_VTE) encoderValue.step = 1.0f
        } else {
            if (parameterModel.key == LBL_VTE) encoderValue.step = 10.0f
        }

        val newValue = floatingPointFix(currentValue + encoderValue.step)
        var isNewValueValid = newValue <= encoderValue.upperLimit
        Log.i("value_limit_one_fragent", currentValue.toString())

        if (isNewValueValid) {
            currentValue = newValue
            countOfMaxLimit = 1
        } else {
            if (countOfMaxLimit == 1) {
                TastyToasty.orange(
                    requireContext(),
                    "${parameterModel.key} upper limit reached",
                    R.drawable.ic_info_small
                ).show();
                countOfMaxLimit = 0
            }
        }
        // display value
        val displayValue = if (isDecimalSupported(parameterModel.key)) String.format(
            "%.1f",
            currentValue
        ) else currentValue.toInt().toString()

        // notify change in value
        onLimitChangeListener?.onLimitChange(parameterModel.reading, currentValue)

        // seekbar
        if (isDecimalSupported(parameterModel.key)) binding.seekBarId.changeProgress(currentValue * 10)
        else binding.seekBarId.changeProgress(currentValue)

    }

    private fun additionForTwoTiles() {
        startTimeoutWithDebounce()

        val newValueVGV = floatingPointFix(currentValue + encoderValue.step)
        var valueOfPip = 0.0f

        VentilatorApp.testingConditonMap[LBL_PIP]?.let {
            valueOfPip = it
        } ?: kotlin.run {
            prefManager?.readPip()?.apply {
                valueOfPip = this
            }
        }

        var isValidNewValue = false
        if (prefManager?.readLastVentMode() == MODE_NC_IPPV) {
            // first condition = plimit - (peep + pinsp)
            // second condition = plimit - (peep + pSupp)
            // final condition = min of first and second
            var valueOfPplat = 0.0f
            var valueOfPSupp = 0.0f
            var valueOfPeep = 0.0f

            VentilatorApp.testingConditonMap[LBL_SUPPORT_PRESSURE]?.let {
                valueOfPSupp = it
            } ?: kotlin.run {
                prefManager?.readSupportPressure()?.apply {
                    valueOfPSupp = this
                }
            }
            VentilatorApp.testingConditonMap[LBL_PPLAT]?.let {
                valueOfPplat = it
            } ?: kotlin.run {
                prefManager?.readPplat()?.apply {
                    valueOfPplat = this
                }
            }
            VentilatorApp.testingConditonMap[LBL_PEEP]?.let {
                valueOfPeep = it
            } ?: kotlin.run {
                prefManager?.readPEEP()?.apply {
                    valueOfPeep = this
                }
            }


            prefManager?.apply {

                val firstCondition = valueOfPip - (valueOfPeep + valueOfPplat)
                val secondCondition = valueOfPip - (valueOfPeep + valueOfPSupp)
                val finalCondition = if (firstCondition < secondCondition) valueOfPip - valueOfPplat else valueOfPip - valueOfPSupp

                if (parameterModel.key == LBL_PPLAT || parameterModel.key == LBL_PEEP || parameterModel.key == LBL_SUPPORT_PRESSURE) {
                    Log.i("value_test_nippv","in condition $finalCondition")
                    if (parameterModel.key == LBL_PEEP) isValidNewValue = newValueVGV <= finalCondition && newValueVGV <= encoderValue.upperLimit
                    else isValidNewValue = newValueVGV <= encoderValue.upperLimit && newValueVGV + valueOfPeep <= valueOfPip
                }
            }
        }

        else if (modeCode == Configs.MODE_VCV_SIMV) {

            if (parameterModel.key == LBL_SUPPORT_PRESSURE || parameterModel.key == LBL_PEEP) {
                if (parameterModel.key == LBL_SUPPORT_PRESSURE) {

                    VentilatorApp.testingConditonMap.get(LBL_PEEP)?.let {
                        isValidNewValue =
                            newValueVGV <= encoderValue.upperLimit && newValueVGV + it <= valueOfPip
                    } ?: kotlin.run {
                        prefManager?.readPEEP()?.apply {
                            isValidNewValue =
                                newValueVGV <= encoderValue.upperLimit && newValueVGV + this <= valueOfPip
                        }
                    }


                } else {

                    VentilatorApp.testingConditonMap.get(LBL_SUPPORT_PRESSURE)?.let {
                        isValidNewValue =
                            newValueVGV <= encoderValue.upperLimit && newValueVGV + it <= valueOfPip
                    } ?: kotlin.run {
                        prefManager?.readSupportPressure()?.apply {
                            isValidNewValue =
                                newValueVGV <= encoderValue.upperLimit && newValueVGV + this <= valueOfPip
                        }
                    }

                }
            }
        } else if (modeCode == Configs.MODE_VCV_CMV) {

            if (parameterModel.key == LBL_PEEP) {
                isValidNewValue = newValueVGV <= encoderValue.upperLimit && newValueVGV <= valueOfPip
            }
        } else {
            if (parameterModel.key == LBL_PPLAT || parameterModel.key == LBL_PEEP) {

                if (parameterModel.key == LBL_PPLAT) {

                    VentilatorApp.testingConditonMap.get(LBL_PEEP)?.let {
                        isValidNewValue =
                            newValueVGV <= encoderValue.upperLimit && newValueVGV + it <= valueOfPip
                    } ?: kotlin.run {
                        prefManager?.readPEEP()?.apply {
                            isValidNewValue =
                                newValueVGV <= encoderValue.upperLimit && newValueVGV + this <= valueOfPip
                        }
                    }
                } else {
                    VentilatorApp.testingConditonMap.get(LBL_PPLAT)?.let {
                        isValidNewValue =
                            newValueVGV <= encoderValue.upperLimit && newValueVGV + it <= valueOfPip
                    } ?: kotlin.run {
                        prefManager?.readPplat()?.apply {
                            isValidNewValue =
                                newValueVGV <= encoderValue.upperLimit && newValueVGV + this <= valueOfPip
                        }
                    }
                }
            } else {
                isValidNewValue = newValueVGV <= encoderValue.upperLimit
            }
        }

        if (isValidNewValue) {
            currentValue = newValueVGV
            countOfMaxLimit = 1
        } else {
            if (countOfMaxLimit == 1) {
//                TastyToasty.orange(context, "${parameterModel.key} upper limit reached", R.drawable.ic_info_small).show();
                TastyToasty.orange(
                    requireContext(),
                    "Update PLimit for further increment",
                    R.drawable.ic_info_small
                ).show();
//                ToastFactory.custom(context, "Update PLimit for further increment")
                countOfMaxLimit = 0
            }
        }

        // display value
        val displayValue = if (isDecimalSupported(parameterModel.key)) String.format(
            "%.1f",
            currentValue
        ) else currentValue.toInt().toString()

        // notify change in value
        onLimitChangeListener?.onLimitChange(parameterModel.reading, currentValue)


        // seekbar
        if (isDecimalSupported(parameterModel.key)) binding.seekBarId.changeProgress(currentValue * 10)
        else binding.seekBarId.changeProgress(currentValue)
    }

    private fun subtraction() {
        startTimeoutWithDebounce()
        val newValue = floatingPointFix(currentValue - encoderValue.step)
        var isNewValueValid = newValue >= encoderValue.lowerLimit
        if (parameterModel.key == LBL_TINSP || parameterModel.key == LBL_RR) {
            val rr = if (parameterModel.key == LBL_RR) newValue.toInt() else {

                VentilatorApp.testingConditonMap.get(LBL_RR)?.let {
                    it.toInt()
                } ?: kotlin.run {
                    prefManager?.readRR()
                        ?.toInt()
                }

            }
            val tinsp = if (parameterModel.key == LBL_TINSP) newValue else {

                VentilatorApp.testingConditonMap.get(LBL_TINSP)?.let {
                    it
                } ?: kotlin.run {
                    prefManager?.readTinsp()
                }

            }

            isNewValueValid = isNewValueValid && isIERatioValid(rr, tinsp)
        }

        if (isNewValueValid) {
            if (currentValue > 0.0f) currentValue = newValue // TODO : why > 0.0f
            countOfMaxLimit = 1
        } else {
            if (countOfMaxLimit == 1) {
                /* TastyToasty.orange(
                     requireContext(),
                     "${parameterModel.key} lower limit reached",
                     R.drawable.ic_info_small
                 ).show();*/
                countOfMaxLimit = 0
            }
        }

        val displayValue = if (isDecimalSupported(parameterModel.key)) String.format(
            "%.1f",
            currentValue
        ) else currentValue.toInt().toString()
        // notify change in value
        onLimitChangeListener?.onLimitChange(parameterModel.reading, currentValue)


        if (isDecimalSupported(parameterModel.key)) binding.seekBarId.changeProgress(currentValue * 10)
        else binding.seekBarId.changeProgress(currentValue)
    }

    private fun subtractionForLimitOne() {
        startTimeoutWithDebounce()
        if (prefManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
            if (parameterModel.key == LBL_VTE) encoderValue.step = 1.0f
        } else {
            if (parameterModel.key == LBL_VTE) encoderValue.step = 10.0f
        }
        val newValue = floatingPointFix(currentValue - encoderValue.step)
        var isNewValueValid = newValue >= encoderValue.lowerLimit

        if (isNewValueValid) {
            if (currentValue > 0.0f) currentValue = newValue
            countOfMaxLimit = 1
        } else {
            if (countOfMaxLimit == 1) {
                /*    TastyToasty.orange(
                        requireContext(),
                        "${parameterModel.key} lower limit reached",
                        R.drawable.ic_info_small
                    ).show();*/
                countOfMaxLimit = 0
            }
        }

        val displayValue = if (isDecimalSupported(parameterModel.key)) String.format(
            "%.1f",
            currentValue
        ) else currentValue.toInt().toString()
        // notify change in value
        onLimitChangeListener?.onLimitChange(parameterModel.reading, currentValue)

        if (isDecimalSupported(parameterModel.key)) binding.seekBarId.changeProgress(currentValue * 10)
        else binding.seekBarId.changeProgress(currentValue)
    }

    // integrated here
    private fun ok() {

        isCloseListenerAvoided = true
        onKnobPressListener?.onKnobPress(parameterModel.reading, currentValue)
        Log.i("Current_value ", "${parameterModel.reading}  " + currentValue)
    }
}

fun KnobDialog.setHeightWidth(prefManager: PreferenceManager?, tag: String?) {
    dialog?.window?.apply {
        setGravity(Gravity.BOTTOM)
        isCancelable = cancelableStatus
        decorView.apply {
            val params: WindowManager.LayoutParams = attributes

            params.x = 600
            params.y = 102
            params.dimAmount = 0.0F
            // params.screenBrightness = 5.0F

            if (prefManager?.readKnobStatus() == false){
                params.width = 0
                params.height = 0
            }
            else {

                params.width = 500
                params.height = 200
            }
            /*      params.width = resources.getDimension(R.dimen.knob_width).toInt()
                  params.height = resources.getDimension(R.dimen.knob_height).toInt()*/

            attributes = params
        }
    }


    hideSystemUI()
}
