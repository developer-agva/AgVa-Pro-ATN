package com.agvahealthcare.ventilator_ext.alarm.limit_two

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.alarm.limit_one.EncoderValue
import com.agvahealthcare.ventilator_ext.alarm.limit_one.KnobParameterModel
import com.agvahealthcare.ventilator_ext.alarm.limit_one.ViewHolder
import com.agvahealthcare.ventilator_ext.callback.OnAlarmLimitChangeListener
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.OnKnobPressListener
import com.agvahealthcare.ventilator_ext.callback.OnLimitChangeListener
import com.agvahealthcare.ventilator_ext.databinding.FragmentLimitTwoBinding
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterLimit
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.KnobDialog
import com.agvahealthcare.ventilator_ext.utility.hideSystemUI
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*
import com.github.angads25.toggle.interfaces.OnToggledListener
import com.github.angads25.toggle.model.ToggleableView

class LimitTwoFragment(
    private val communicationService: CommunicationService?,
    private val limitChangeListener: OnAlarmLimitChangeListener
) : Fragment(), OnKnobPressListener, OnToggledListener,
    OnDismissDialogListener, View.OnClickListener, OnLimitChangeListener {

    companion object {
        val TAG = "LimitTwoFragment"
    }

    private lateinit var binding:FragmentLimitTwoBinding
    private var fio2UpperLimit: Float? = null
    private var fio2LowerLimit: Float? = null

    private var default_fio2UpperLimit: Float? = null
    private var default_fio2LowerLimit: Float? = null

    private var spo2UpperLimit: Float? = null
    private var spo2LowerLimit: Float? = null

    //setting the default value from the strings for spo2
    private var default_spo2UpperLimit: Float? = null
    private var default_spo2LowerLimit: Float? = null

    var knobDialog: KnobDialog? = null
    private var labelViewHolderMap: MutableMap<String, ViewHolder> = LinkedHashMap()
    private var parameterModel: KnobParameterModel? = null
    private var encoderOption: ControlParameterLimit? = null
    private var encoderValue: EncoderValue? = null
    private var currentView: View? = null
    private var currentKey: String? = null

    private var prefManager: PreferenceManager? = null
    private lateinit var mEventViewModel: EventViewModel
    private var limitTwoObserver: LimitTwoObserver? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        limitTwoObserver = LimitTwoObserver()
        limitTwoObserver.apply {
            this?.let { this@LimitTwoFragment.lifecycle.addObserver(it) }
        }
    }

    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {

        when (highlightedIndex) {

            0 -> binding.includefio2Upperlimit.root.callOnClick()
            1 -> binding.includefio2lowerlimit.root.callOnClick()
            2 -> binding.includeSpO2Upperlimit.root.callOnClick()
            3 -> binding.includeSpO2lowerlimit.root.callOnClick()
        }
    }

    fun highlightAdapterPosition(highlightedIndex: Int) {

        getViewForFocus(highlightedIndex)?.let {
            changeConstraintsOfFocusLayout(it)
        } ?: run {
            clearPreviousConstraints()
        }
    }

    fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.mainViewPanelLimitTwo)
            constraintSet.clear(binding.focusLayoutLimitTwo.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutLimitTwo.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutLimitTwo.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutLimitTwo.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelLimitTwo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelLimitTwo)
        constraintSet.connect(
            binding.focusLayoutLimitTwo.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutLimitTwo.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutLimitTwo.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutLimitTwo.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelLimitTwo)
    }

    private fun getViewForFocus(highlightedIndex: Int): View? {

        return when (highlightedIndex) {

            0 -> binding.layout1LimitTwo
            1 -> binding.layout2LimitTwo
            2 -> binding.layout3LimitTwo
            3 -> binding.layout4LimitTwo

            else -> null
        }
    }
    // logic knob highlight ends here

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        default_fio2UpperLimit =
            requireActivity().getString(R.string.default_max_fio2_limit).toFloat()
        default_fio2LowerLimit =
            requireActivity().getString(R.string.default_min_fio2_limit).toFloat()
        default_spo2UpperLimit =
            requireActivity().getString(R.string.default_max_spo2_limit).toFloat()
        default_spo2LowerLimit =
            requireActivity().getString(R.string.default_min_spo2_limit).toFloat()
        binding = FragmentLimitTwoBinding.inflate(layoutInflater,container,false)
        return binding.root
    }


    private fun createViewBindingMap() {
        createViewHolderMapping()
        initUserSetLimits()
        initToggalState()
    }

    private fun initToggalState() {
        prefManager?.apply {
            binding.fio2Toggale.isOn = readFio2LimitState()
            binding.spO2Toggle.isOn = readSpO2LimitState()
        }
    }

    fun updateKnob(value: String) {
        knobDialog?.takeIf { it.isVisible }?.apply {
            updateWithTimeoutDebounce(value)
        }
    }


    private fun createViewHolderMapping() {

        labelViewHolderMap.clear()

        labelViewHolderMap.put(
            LBL_FIO2,
            ViewHolder(
                default_fio2LowerLimit,
                default_fio2UpperLimit,
                fio2LowerLimit,
                fio2UpperLimit
            )
        )
        labelViewHolderMap.put(
            LBL_SPO2,
            ViewHolder(
                default_spo2LowerLimit,
                default_spo2UpperLimit,
                spo2LowerLimit,
                spo2UpperLimit
            )
        )

    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        knobDialog?.takeIf { it.isVisible }?.apply {
            hideSystemUI()
        }
        Log.i("testingHideBar", isInMultiWindowMode.toString())
    }

    private fun setupClickListener() {

        binding.includefio2Upperlimit.root.setOnClickListener(this)
        binding.includefio2lowerlimit.root.setOnClickListener(this)

        binding.includeSpO2Upperlimit.root.setOnClickListener(this)
        binding.includeSpO2lowerlimit.root.setOnClickListener(this)
    }

    private fun initUserSetLimits() {
        prefManager?.apply {
            renderUserLimits(LBL_FIO2, readFiO2Limits())
            renderUserLimits(LBL_SPO2, readSpO2Limits())
        }
    }

    private fun renderUserLimits(lbl: String, limit: Array<Float?>) {
        val isLimitValid = limit.size == 2 && limit[0] != null && limit[1] != null
        if (isLimitValid) {
            val minUserLimit = limit[0]!!
            val maxUserLimit = limit[1]!!
            //from the limit one fragment code
            var defaultUpperLimit: Float? = null
            var defaultLowerLimit: Float? = null
            var upperLimitView: View? = null
            var lowerLimitView: View? = null

            when (lbl) {
                LBL_FIO2 -> {
                    upperLimitView = binding.includefio2Upperlimit.root
                    lowerLimitView = binding.includefio2lowerlimit.root
                    defaultUpperLimit = default_fio2UpperLimit
                    defaultLowerLimit = default_fio2LowerLimit
                }

                LBL_SPO2 -> {
                    upperLimitView = binding.includeSpO2Upperlimit.root
                    lowerLimitView = binding.includeSpO2lowerlimit.root
                    defaultUpperLimit = default_spo2UpperLimit
                    defaultLowerLimit = default_spo2LowerLimit
                }
            }
            //ToDo:- the points of integration
            //set the lower limit of the currently selected item in the view
            lowerLimitView?.apply {
                if (defaultLowerLimit != null && defaultUpperLimit != null) setValueOnProgressView(
                    this,
                    defaultLowerLimit,
                    defaultUpperLimit
                )
                setValueOnLimitView(this, minUserLimit)
            }
            //set the upper limit of the currently selected item in the view
            upperLimitView?.apply {
                if (defaultLowerLimit != null && defaultUpperLimit != null) setValueOnProgressView(
                    this,
                    defaultLowerLimit,
                    defaultUpperLimit
                )
                setValueOnLimitView(this, maxUserLimit)
            }
            createViewHolderMapping()
        }
    }

    private fun setValueOnProgressView(activeView: View, lowerLimit: Float, upperLimit: Float) {
        when (activeView) {
            binding.includeSpO2lowerlimit.root -> {
                binding.includeSpO2lowerlimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includeSpO2Upperlimit.root -> {
                binding.includeSpO2Upperlimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includefio2lowerlimit.root -> {
                binding.includefio2lowerlimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includefio2Upperlimit.root -> {
                binding.includefio2Upperlimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }
        }
    }

    //method to set the progress bar value of the individual view in selection
    private fun setValueOnLimitView(activeView: View, newValue: Float) {

        when (activeView) {
            binding.includeSpO2lowerlimit.root -> {
                binding.includeSpO2lowerlimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includeSpO2lowerlimit.textView.text = nonDecimal(activeView, newValue.toString())
                spo2LowerLimit = newValue
            }

            binding.includeSpO2Upperlimit.root -> {
                binding.includeSpO2Upperlimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includeSpO2Upperlimit.textView.text = nonDecimal(activeView, newValue.toString())
                spo2UpperLimit = newValue
            }

            binding.includefio2lowerlimit.root -> {
                binding.includefio2lowerlimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includefio2lowerlimit.textView.text = nonDecimal(activeView, newValue.toString())
                fio2LowerLimit = newValue
            }

            binding.includefio2Upperlimit.root -> {
                binding.includefio2Upperlimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includefio2Upperlimit.textView.text = nonDecimal(activeView, newValue.toString())
                fio2UpperLimit = newValue
            }
        }
    }

    private fun nonDecimal(v: View, value: String): String {
        val decimalSupportedValues = listOf<View>(
            binding.includefio2Upperlimit.root,
            binding.includefio2lowerlimit.root,
            binding.includeSpO2Upperlimit.root,
            binding.includeSpO2lowerlimit.root
        )
        return if (v in decimalSupportedValues) {
            value.toFloatOrNull()?.toInt()?.toString() ?: value
        } else {
            value.toFloatOrNull()?.toInt()?.toString() ?: value
        }
    }

    override fun onLimitChange(previousValue: Float, newValue: Float) {
        currentView?.apply { setValueOnLimitView(this, newValue) }
    }


    override fun onKnobPress(previousValue: Float, newValue: Float) {
        currentView?.let {
            this deSelect it

            prefManager?.apply {
                when (it) {
                    binding.includefio2lowerlimit.root -> {
                        setFiO2Limits(fio2LowerLimit, fio2UpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            fio2LowerLimit!!,
                            fio2UpperLimit
                        )
                    }

                    binding.includefio2Upperlimit.root -> {
                        setFiO2Limits(fio2LowerLimit, fio2UpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            fio2LowerLimit!!,
                            fio2UpperLimit
                        )
                    }

                    binding.includeSpO2lowerlimit.root -> {
                        setSpO2Limits(spo2LowerLimit, spo2UpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            spo2LowerLimit!!,
                            spo2UpperLimit
                        )
                    }

                    binding.includeSpO2Upperlimit.root -> {
                        setSpO2Limits(spo2LowerLimit, spo2UpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            spo2LowerLimit!!,
                            spo2UpperLimit
                        )
                    }
                }
            }


            currentView = null
            initUserSetLimits()
            createViewHolderMapping()
            communicationService?.sendAlarmLimitsToVentilator()
            // change here 13 feb
            knobDialog?.takeIf { it.isVisible }?.apply {
                this.dismiss()
            }
        }

    }

    override fun onSwitched(toggleableView: ToggleableView?, isOn: Boolean) {
        when (toggleableView?.id) {
            binding.fio2Toggale.id -> prefManager?.setFio2LimitState(isOn)
            binding.spO2Toggle.id -> prefManager?.setSpO2LimitState(isOn)
        }
    }

    override fun handleDialogClose() {
        knobDialog?.takeIf { it.isVisible }?.dismiss()
        this deSelect currentView
        initUserSetLimits()
    }

    override fun onClick(selectedView: View?) {
        currentView = selectedView
        select(selectedView)
        var isUpperLimit: Boolean? = null
        var activeLabel: String? = null


        when (selectedView) {
            binding.includeSpO2lowerlimit.root -> {
                isUpperLimit = false
                activeLabel = LBL_SPO2
            }

            binding.includeSpO2Upperlimit.root -> {
                isUpperLimit = true
                activeLabel = LBL_SPO2
            }

            binding.includefio2lowerlimit.root -> {
                isUpperLimit = false
                activeLabel = LBL_FIO2
            }

            binding.includefio2Upperlimit.root -> {
                isUpperLimit = true
                activeLabel = LBL_FIO2
            }

        }

        val value = labelViewHolderMap[activeLabel]

        currentKey = activeLabel
        Log.i(
            "limit_one_array",
            "" + value?.defaultMax + " " + value?.defaultMin + " " + value?.actualMax + " " + value?.actualMin
        )


        if (activeLabel != null && isUpperLimit != null) {
            if (isUpperLimit) {
                value?.let {
                    parameterModel =
                        KnobParameterModel(
                            activeLabel,
                            activeLabel, 1, it.actualMax, getParameterUnit(
                                requireContext(),
                                activeLabel
                            )
                        )


                    Log.i("limit_one_array", "" + parameterModel?.name)

                    encoderOption = ControlParameterLimit(
                        it.defaultMin,
                        it.defaultMax
                    )

                    encoderValue = encoderOption?.let { it.valuePerRotation }?.let { it1 ->
                        EncoderValue(
                            it.actualMin, it.defaultMax,
                            it1.toFloat()

                        )
                    }

                }
            } else {
                value?.let {
                    parameterModel =

                        KnobParameterModel(
                            activeLabel,
                            activeLabel, 1, it.actualMin, Configs.getParameterUnit(
                                requireContext(),
                                activeLabel
                            )
                        )


                    Log.i("limit_one_array", "" + parameterModel?.name)

                    encoderOption = ControlParameterLimit(
                        it.defaultMin,
                        it.defaultMax
                    )

                    encoderValue = encoderOption?.let { it.valuePerRotation }?.let { it1 ->
                        EncoderValue(
                            it.defaultMin, it.actualMax,
                            it1.toFloat()

                        )
                    }

                }
            }
            knobDialog = parameterModel?.let { it1 ->
                encoderValue?.let { it2 ->
                    KnobDialog.newInstance(
                        onKnobPressListener = this,
                        onTimeoutListener = this,
                        onCloseListener = object : OnDismissDialogListener {
                            override fun handleDialogClose() {
                                this@LimitTwoFragment deSelect currentView
                                initUserSetLimits()
                            }
                        },
                        parameterModel = it1,
                        encoderValue = it2,
                        onLimitChangeListener = this
                    )
                }
            }



            knobDialog?.let {
                it.show(childFragmentManager, LimitTwoFragment.TAG)
                it.startTimeoutWithDebounce()
            }
        }
    }

    private fun select(limitView: View?) {
        context?.let {
            when (limitView) {
                binding.includeSpO2lowerlimit.root -> {
                    setValueOnLimitView(limitView, binding.includeSpO2lowerlimit.paramProgressBar.progress.toFloat())
                    binding.includeSpO2lowerlimit.paramProgressBar.background = ContextCompat.getDrawable(it,R.drawable.progresscircle_with_selection_yellow)
                    binding.includeSpO2lowerlimit.textView.setTextColor(Color.BLACK)
                }

                binding.includeSpO2Upperlimit.root -> {
                    setValueOnLimitView(limitView, binding.includeSpO2Upperlimit.paramProgressBar.progress.toFloat())
                    binding.includeSpO2Upperlimit.paramProgressBar.background = ContextCompat.getDrawable(it,R.drawable.progresscircle_with_selection_yellow)
                    binding.includeSpO2Upperlimit.textView.setTextColor(Color.BLACK)
                }

                binding.includefio2lowerlimit.root -> {
                    setValueOnLimitView(limitView, binding.includefio2lowerlimit.paramProgressBar.progress.toFloat())
                    binding.includefio2lowerlimit.paramProgressBar.background = ContextCompat.getDrawable(it,R.drawable.progresscircle_with_selection_yellow)
                    binding.includefio2lowerlimit.textView.setTextColor(Color.BLACK)
                }

                binding.includefio2Upperlimit.root -> {
                    setValueOnLimitView(limitView, binding.includefio2Upperlimit.paramProgressBar.progress.toFloat())
                    binding.includefio2Upperlimit.paramProgressBar.background = ContextCompat.getDrawable(it,R.drawable.progresscircle_with_selection_yellow)
                    binding.includefio2Upperlimit.textView.setTextColor(Color.BLACK)
                }
            }
        }
    }

    private infix fun LimitTwoFragment.deSelect(limitView: View?) {
        context?.let {
            when (limitView) {
                binding.includeSpO2lowerlimit.root -> {
                    binding.includeSpO2lowerlimit.paramProgressBar.background = ContextCompat.getDrawable(it,R.drawable.progresscircle)
                    binding.includeSpO2lowerlimit.textView.setTextColor(Color.BLACK)
                }

                binding.includeSpO2Upperlimit.root -> {
                    binding.includeSpO2Upperlimit.paramProgressBar.background = ContextCompat.getDrawable(it,R.drawable.progresscircle)
                    binding.includeSpO2Upperlimit.textView.setTextColor(Color.BLACK)
                }

                binding.includefio2lowerlimit.root -> {
                    binding.includefio2lowerlimit.paramProgressBar.background = ContextCompat.getDrawable(it,R.drawable.progresscircle)
                    binding.includefio2lowerlimit.textView.setTextColor(Color.BLACK)
                }

                binding.includefio2Upperlimit.root -> {
                    binding.includefio2Upperlimit.paramProgressBar.background = ContextCompat.getDrawable(it,R.drawable.progresscircle)
                    binding.includefio2Upperlimit.textView.setTextColor(Color.BLACK)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        limitTwoObserver.apply {
            this?.let { this@LimitTwoFragment.lifecycle.removeObserver(it) }
        }
        limitTwoObserver = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    inner class LimitTwoObserver : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)

        }

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            prefManager = PreferenceManager(requireContext())
            mEventViewModel =
                ViewModelProvider(this@LimitTwoFragment).get(EventViewModel::class.java)
            hideSystemUI()
            //fio2Toggale.setOnToggledListener(this)

            setupClickListener()
            createViewBindingMap()
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            binding.includeSpO2lowerlimit.paramProgressBar.setCurrentProgress(0.0)

            binding.includeSpO2Upperlimit.paramProgressBar.setCurrentProgress(0.0)

            binding.includefio2lowerlimit.paramProgressBar.setCurrentProgress(0.0)

            binding.includefio2Upperlimit.paramProgressBar.setCurrentProgress(0.0)

            default_fio2UpperLimit = null

            default_fio2LowerLimit = null

            default_spo2UpperLimit = null

            default_spo2LowerLimit = null

            currentView = null
            fio2UpperLimit = null
            fio2LowerLimit = null
            spo2UpperLimit = null
            spo2LowerLimit = null
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)

        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
        }
    }

}
