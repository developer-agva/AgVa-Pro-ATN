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
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.alarm.limit_one.EncoderValue
import com.agvahealthcare.ventilator_ext.alarm.limit_one.KnobParameterModel
import com.agvahealthcare.ventilator_ext.alarm.limit_one.ViewHolder
import com.agvahealthcare.ventilator_ext.callback.OnAlarmLimitChangeListener
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.OnKnobPressListener
import com.agvahealthcare.ventilator_ext.callback.OnLimitChangeListener
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
import kotlinx.android.synthetic.main.fragment_limit_one.focusLayoutLimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.includeMVELowerLimit
import kotlinx.android.synthetic.main.fragment_limit_one.includeMVEUpperLimit
import kotlinx.android.synthetic.main.fragment_limit_one.includePeepLowerLimit
import kotlinx.android.synthetic.main.fragment_limit_one.includePeepUpperLimit
import kotlinx.android.synthetic.main.fragment_limit_one.includePressureLoweLimit
import kotlinx.android.synthetic.main.fragment_limit_one.includePressureUpperLimit
import kotlinx.android.synthetic.main.fragment_limit_one.includeRRLowerLimit
import kotlinx.android.synthetic.main.fragment_limit_one.includeRRUpperLimit
import kotlinx.android.synthetic.main.fragment_limit_one.includeVTeLowerLimit
import kotlinx.android.synthetic.main.fragment_limit_one.includeVTeUpperLimit
import kotlinx.android.synthetic.main.fragment_limit_one.layout10LimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.layout1LimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.layout2LimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.layout3LimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.layout4LimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.layout5LimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.layout6LimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.layout7LimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.layout8LimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.layout9LimitOne
import kotlinx.android.synthetic.main.fragment_limit_one.mainViewPanelLimitOne
import kotlinx.android.synthetic.main.fragment_limit_two.*
import kotlinx.android.synthetic.main.knob_progress_view.view.*

class LimitTwoFragment(
    private val communicationService: CommunicationService?,
    private val limitChangeListener: OnAlarmLimitChangeListener
) : Fragment(), OnKnobPressListener, OnToggledListener,
    OnDismissDialogListener, View.OnClickListener, OnLimitChangeListener {

    companion object {
        val TAG = "LimitTwoFragment"
    }

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

            0 -> includefio2Upperlimit.callOnClick()
            1 -> includefio2lowerlimit.callOnClick()
            2 -> includeSpO2Upperlimit.callOnClick()
            3 -> includeSpO2lowerlimit.callOnClick()
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
            constraintSet.clone(mainViewPanelLimitTwo)
            constraintSet.clear(focusLayoutLimitTwo.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutLimitTwo.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutLimitTwo.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutLimitTwo.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelLimitTwo)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelLimitTwo)
        constraintSet.connect(
            focusLayoutLimitTwo.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutLimitTwo.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutLimitTwo.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutLimitTwo.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelLimitTwo)
    }

    private fun getViewForFocus(highlightedIndex: Int): View? {

        return when (highlightedIndex) {

            0 -> layout1LimitTwo
            1 -> layout2LimitTwo
            2 -> layout3LimitTwo
            3 -> layout4LimitTwo

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
        return inflater.inflate(R.layout.fragment_limit_two, container, false)
    }


    private fun createViewBindingMap() {
        createViewHolderMapping()
        initUserSetLimits()
        initToggalState()
    }

    private fun initToggalState() {
        prefManager?.apply {
            fio2Toggale.isOn = readFio2LimitState()
            spO2Toggle.isOn = readSpO2LimitState()
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

        includefio2Upperlimit.setOnClickListener(this)
        includefio2lowerlimit.setOnClickListener(this)

        includeSpO2Upperlimit.setOnClickListener(this)
        includeSpO2lowerlimit.setOnClickListener(this)
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
                    upperLimitView = includefio2Upperlimit
                    lowerLimitView = includefio2lowerlimit
                    defaultUpperLimit = default_fio2UpperLimit
                    defaultLowerLimit = default_fio2LowerLimit
                }

                LBL_SPO2 -> {
                    upperLimitView = includeSpO2Upperlimit
                    lowerLimitView = includeSpO2lowerlimit
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
        activeView.param_progress_bar.maxProgress = upperLimit.toInt().toDouble()
    }

    //method to set the progress bar value of the individual view in selection
    private fun setValueOnLimitView(activeView: View, newValue: Float) {
        //update the view
        activeView.param_progress_bar.setCurrentProgress(newValue.toInt().toDouble())
        Log.i("LIMIT2_CHECK", "value = ${nonDecimal(activeView, newValue.toString())}")
        activeView.textView.text = nonDecimal(activeView, newValue.toString())

        //update runtime variable value
        when (activeView) {
            includefio2Upperlimit -> {
                fio2UpperLimit = newValue
            }

            includefio2lowerlimit -> {
                fio2LowerLimit = newValue
            }

            includeSpO2lowerlimit -> {
                spo2LowerLimit = newValue
            }

            includeSpO2Upperlimit -> {
                spo2UpperLimit = newValue
            }
        }
    }

    private fun nonDecimal(v: View, value: String): String {
        val decimalSupportedValues = listOf<View>(
            includefio2Upperlimit,
            includefio2lowerlimit,
            includeSpO2Upperlimit,
            includeSpO2lowerlimit
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
                    includefio2lowerlimit -> {
                        setFiO2Limits(fio2LowerLimit, fio2UpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            fio2LowerLimit!!,
                            fio2UpperLimit
                        )
                    }

                    includefio2Upperlimit -> {
                        setFiO2Limits(fio2LowerLimit, fio2UpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            fio2LowerLimit!!,
                            fio2UpperLimit
                        )
                    }

                    includeSpO2lowerlimit -> {
                        setSpO2Limits(spo2LowerLimit, spo2UpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            spo2LowerLimit!!,
                            spo2UpperLimit
                        )
                    }

                    includeSpO2Upperlimit -> {
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
            fio2Toggale.id -> prefManager?.setFio2LimitState(isOn)
            spO2Toggle.id -> prefManager?.setSpO2LimitState(isOn)
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
            includeSpO2lowerlimit -> {
                isUpperLimit = false
                activeLabel = LBL_SPO2
            }

            includeSpO2Upperlimit -> {
                isUpperLimit = true
                activeLabel = LBL_SPO2
            }

            includefio2lowerlimit -> {
                isUpperLimit = false
                activeLabel = LBL_FIO2
            }

            includefio2Upperlimit -> {
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
        //limitView?.progress_bar?.progressDrawable=ContextCompat.getDrawable(requireContext(),R.drawable.progresscircle_with_selection)
        //  limitView?.textView?.setTextColor(Color.WHITE)
        limitView?.param_progress_bar?.run {
            val progressValue = progress
            //  val progressMin = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) min else 0
            val progressMax = maxProgress
            // setValueOnProgressView(limitView, progressMin.toFloat(), progressMax.toFloat())
            setValueOnLimitView(limitView, progressValue.toFloat())
            this.background =
                ContextCompat.getDrawable(context, R.drawable.progresscircle_with_selection_yellow)

        }

        limitView?.textView?.setTextColor(Color.BLACK)
    }

    private infix fun LimitTwoFragment.deSelect(limitView: View?) {
        val progressValue = limitView?.param_progress_bar?.progress
        limitView?.textView?.setTextColor(Color.BLACK)
        context?.let {
            limitView?.param_progress_bar?.background =
                ContextCompat.getDrawable(it, R.drawable.progresscircle)
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
            includeSpO2lowerlimit.param_progress_bar.setCurrentProgress(0.0)

            includeSpO2Upperlimit.param_progress_bar.setCurrentProgress(0.0)

            includefio2lowerlimit.param_progress_bar.setCurrentProgress(0.0)

            includefio2Upperlimit.param_progress_bar.setCurrentProgress(0.0)

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
