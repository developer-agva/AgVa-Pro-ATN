package com.agvahealthcare.ventilator_ext.alarm.limit_one

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnAlarmLimitChangeListener
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.OnKnobPressListener
import com.agvahealthcare.ventilator_ext.callback.OnLimitChangeListener
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterLimit
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.KnobDialog
import com.agvahealthcare.ventilator_ext.utility.hideSystemUI
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*
import kotlinx.android.synthetic.main.fragment_alarm_dialog.focusLayoutAlarmDialog
import kotlinx.android.synthetic.main.fragment_alarm_dialog.imageViewCrossAlarmDialog
import kotlinx.android.synthetic.main.fragment_alarm_dialog.includeButtonBuffer
import kotlinx.android.synthetic.main.fragment_alarm_dialog.includeButtonLimit1
import kotlinx.android.synthetic.main.fragment_alarm_dialog.includeButtonLimit2
import kotlinx.android.synthetic.main.fragment_alarm_dialog.mainViewPanelAlarmDialog
import kotlinx.android.synthetic.main.fragment_limit_one.*
import kotlinx.android.synthetic.main.knob_progress_view.view.*


class LimitOneFragment(
    private val communicationService: CommunicationService?,
    private val limitChangeListener: OnAlarmLimitChangeListener
) : Fragment(), OnKnobPressListener,
    OnDismissDialogListener, OnLimitChangeListener, View.OnClickListener/*, OnToggledListener*/ {
    companion object {
        val TAG = "LimitOneFragment"
    }

    private var prefManager: PreferenceManager? = null
    private var presserUpperLimit: Float? = null
    private var presserLowerLimit: Float? = null
    private var vtiUpperLimit: Float? = null
    private var vtiLowerLimit: Float? = null
    private var vteUpperLimit: Float? = null
    private var vteLowerLimit: Float? = null
    private var peepUpperLimit: Float? = null
    private var peepLowerLimit: Float? = null
    private var respiratoryUpperLimit: Float? = null
    private var respiratoryLowerLimit: Float? = null
    private var mveUpperLimit: Float? = null
    private var mveLowerLimit: Float? = null


    private var default_presserUpperLimit: Float? = null
    private var default_presserLowerLimit: Float? = null
    private var default_vtiUpperLimit: Float? = null
    private var default_vtiLowerLimit: Float? = null
    private var default_vteUpperLimit: Float? = null
    private var default_vteLowerLimit: Float? = null
    private var default_peepUpperLimit: Float? = null
    private var default_peepLowerLimit: Float? = null
    private var default_respiratoryUpperLimit: Float? = null
    private var default_respiratoryLowerLimit: Float? = null
    private var default_mveUpperLimit: Float? = null
    private var default_mveLowerLimit: Float? = null

    var customProgressDialog: KnobDialog? = null

    private lateinit var mEventViewModel: EventViewModel

    private var labelViewHolderMap: MutableMap<String, ViewHolder> = LinkedHashMap()
    private var parameterModel: KnobParameterModel? = null
    private var encoderOption: ControlParameterLimit? = null
    private var encoderValue: EncoderValue? = null

    private var currentView: View? = null
    private var currentKey: String? = null
    private var limitOneObserver: LimitOneObserver? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        limitOneObserver = LimitOneObserver()
        limitOneObserver.apply {
            this?.let { this@LimitOneFragment.lifecycle.addObserver(it) }
        }
    }


    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {

        when (highlightedIndex) {

            0 -> includePressureUpperLimit.callOnClick()
            1 -> includePressureLoweLimit.callOnClick()
            2 -> includeVTeUpperLimit.callOnClick()
            3 -> includeVTeLowerLimit.callOnClick()
            4 -> includePeepUpperLimit.callOnClick()
            5 -> includePeepLowerLimit.callOnClick()
            6 -> includeRRUpperLimit.callOnClick()
            7 -> includeRRLowerLimit.callOnClick()
            8 -> includeMVEUpperLimit.callOnClick()
            9 -> includeMVELowerLimit.callOnClick()
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
            constraintSet.clone(mainViewPanelLimitOne)
            constraintSet.clear(focusLayoutLimitOne.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutLimitOne.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutLimitOne.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutLimitOne.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelLimitOne)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelLimitOne)
        constraintSet.connect(
            focusLayoutLimitOne.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutLimitOne.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutLimitOne.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutLimitOne.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelLimitOne)
    }

    private fun getViewForFocus(highlightedIndex: Int): View? {

        return when (highlightedIndex) {

            0 -> layout1LimitOne
            1 -> layout2LimitOne
            2 -> layout3LimitOne
            3 -> layout4LimitOne
            4 -> layout5LimitOne
            5 -> layout6LimitOne
            6 -> layout7LimitOne
            7 -> layout8LimitOne
            8 -> layout9LimitOne
            9 -> layout10LimitOne

            else -> null
        }
    }
    // logic knob highlight ends here


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Log.i("ACTIVITY_LIFECYCLE", "ON_CREATE $javaClass")
        return inflater.inflate(R.layout.fragment_limit_one, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("ACTIVITY_LIFECYCLE", "ON_VIEW_CREATE $javaClass")

    }

    override fun onStart() {
        super.onStart()
        Log.i("ACTIVITY_LIFECYCLE", "ON_START $javaClass")
    }

    override fun onResume() {
        super.onResume()
        Log.i("ACTIVITY_LIFECYCLE", "ON_RESUME $javaClass")
    }

    private fun initTogalState() {
        prefManager?.apply {
            presserToggle.isOn = readPipLimitState()
            vteToggle.isOn = readVteLimitState()
            peepToggle.isOn = readPeepLimitState()
            rrToggle.isOn = readRRLimitState()
            mveToggle.isOn = readMveLimitState()
        }

    }


    private fun createViewBindingMap() {
        createViewHolderMapping()
        initUserSetLimits()
        initTogalState()
    }

    private fun createViewHolderMapping() {

        labelViewHolderMap.clear()

        labelViewHolderMap[LBL_PIP] = ViewHolder(
            default_presserLowerLimit,
            default_presserUpperLimit,
            presserLowerLimit,
            presserUpperLimit
        )
        labelViewHolderMap[LBL_VTE] = ViewHolder(
            default_vteLowerLimit,
            default_vteUpperLimit,
            vteLowerLimit,
            vteUpperLimit
        )

        labelViewHolderMap[LBL_PEEP] = ViewHolder(
            default_peepLowerLimit,
            default_peepUpperLimit,
            peepLowerLimit,
            peepUpperLimit
        )
        labelViewHolderMap[LBL_RR] = ViewHolder(
            default_respiratoryLowerLimit,
            default_respiratoryUpperLimit,
            respiratoryLowerLimit,
            respiratoryUpperLimit
        )
        labelViewHolderMap[LBL_MVE] =
            ViewHolder(default_mveLowerLimit, default_mveUpperLimit, mveLowerLimit, mveUpperLimit)


    }


    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        customProgressDialog?.takeIf { it.isVisible }?.apply {
            hideSystemUI()
        }
        Log.i("testingHideBar", isInMultiWindowMode.toString())
    }

    // set on ClickListener
    private fun setupClickListener() {
        includePressureUpperLimit.setOnClickListener(this)
        includePressureLoweLimit.setOnClickListener(this)
        //ToDo:-includeVTeUpperLimit
        includeVTeUpperLimit.setOnClickListener(this)
        includeVTeLowerLimit.setOnClickListener(this)
        //ToDo:-includePeepUpperLimit
        includePeepUpperLimit.setOnClickListener(this)
        includePeepLowerLimit.setOnClickListener(this)
        //ToDo:-includeRRUpperLimit
        includeRRUpperLimit.setOnClickListener(this)
        includeRRLowerLimit.setOnClickListener(this)
        //ToDo:-includemveUpperLimit
        includeMVEUpperLimit.setOnClickListener(this)
        includeMVELowerLimit.setOnClickListener(this)
    }

    private fun initUserSetLimits() {
        Log.i("USER_LIMIT", "Init user limit called")
        prefManager?.apply {
            renderUserLimits(LBL_PIP, readPipLimits())
            renderUserLimits(LBL_VTE, readVteLimits())
            renderUserLimits(LBL_PEEP, readPeepLimits())
            renderUserLimits(LBL_RR, readRRLimits())
            renderUserLimits(LBL_MVE, readMveLimits())

        }
    }


    private fun renderUserLimits(lbl: String, limit: Array<Float?>) {
        val isLimitValid = limit.size == 2 && limit[0] != null && limit[1] != null
        if (isLimitValid) {
            val minUserLimit = limit[0]!!
            val maxUserLimit = limit[1]!!

            var defaultUpperLimit: Float? = null
            var defaultLowerLimit: Float? = null
            var upperLimitView: View? = null
            var lowerLimitView: View? = null
            when (lbl) {
                LBL_PIP -> {
                    upperLimitView = includePressureUpperLimit
                    lowerLimitView = includePressureLoweLimit
                    defaultUpperLimit = default_presserUpperLimit
                    defaultLowerLimit = default_presserLowerLimit
                }

                LBL_VTE -> {
                    upperLimitView = includeVTeUpperLimit
                    lowerLimitView = includeVTeLowerLimit
                    defaultUpperLimit = default_vteUpperLimit
                    defaultLowerLimit = default_vteLowerLimit
                }

                LBL_PEEP -> {
                    upperLimitView = includePeepUpperLimit
                    lowerLimitView = includePeepLowerLimit
                    defaultUpperLimit = default_peepUpperLimit
                    defaultLowerLimit = default_peepLowerLimit
                }

                LBL_RR -> {
                    upperLimitView = includeRRUpperLimit
                    lowerLimitView = includeRRLowerLimit
                    defaultUpperLimit = default_respiratoryUpperLimit
                    defaultLowerLimit = default_respiratoryLowerLimit
                }

                LBL_MVE -> {
                    upperLimitView = includeMVEUpperLimit
                    lowerLimitView = includeMVELowerLimit
                    defaultUpperLimit = default_mveUpperLimit
                    defaultLowerLimit = default_mveLowerLimit
                }
            }

            lowerLimitView?.apply {
                if (defaultLowerLimit != null && defaultUpperLimit != null) setValueOnProgressView(
                    this,
                    defaultLowerLimit,
                    defaultUpperLimit
                )
                setValueOnLimitView(this, minUserLimit)
            }

            upperLimitView?.apply {
                if (defaultLowerLimit != null && defaultUpperLimit != null) setValueOnProgressView(
                    this,
                    defaultLowerLimit,
                    defaultUpperLimit
                )
                setValueOnLimitView(this, maxUserLimit)
            }
        }
        createViewHolderMapping()

    }


    override fun onKnobPress(previousValue: Float, newValue: Float) {
        currentView?.let {
            this deSelect it

            prefManager?.apply {
                when (it) {

                    includePressureUpperLimit -> {
                        setPipLimits(presserLowerLimit, presserUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            presserLowerLimit!!,
                            presserUpperLimit
                        )
                    }

                    includePressureLoweLimit -> {
                        setPipLimits(presserLowerLimit, presserUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            presserLowerLimit!!,
                            presserUpperLimit
                        )
                    }

                    includeVTeUpperLimit -> {
                        setVteLimits(vteLowerLimit, vteUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            vteLowerLimit!!,
                            vteUpperLimit
                        )
                    }

                    includeVTeLowerLimit -> {
                        setVteLimits(vteLowerLimit, vteUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            vteLowerLimit!!,
                            vteUpperLimit
                        )
                    }

                    includePeepUpperLimit -> {
                        setPEEPLimits(peepLowerLimit, peepUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            peepLowerLimit!!,
                            peepUpperLimit
                        )
                    }

                    includePeepLowerLimit -> {
                        setPEEPLimits(peepLowerLimit, peepUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            peepLowerLimit!!,
                            peepUpperLimit
                        )
                    }

                    includeRRUpperLimit -> {
                        setRRLimits(respiratoryLowerLimit, respiratoryUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            respiratoryLowerLimit!!,
                            respiratoryUpperLimit
                        )
                    }

                    includeRRLowerLimit -> {
                        setRRLimits(respiratoryLowerLimit, respiratoryUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            respiratoryLowerLimit!!,
                            respiratoryUpperLimit
                        )
                    }

                    includeMVEUpperLimit -> {
                        setMveLimits(mveLowerLimit, mveUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            mveLowerLimit!!,
                            mveUpperLimit
                        )
                    }

                    includeMVELowerLimit -> {
                        setMveLimits(mveLowerLimit, mveUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            mveLowerLimit!!,
                            mveUpperLimit
                        )
                    }
                }
            }

            currentView = null
            initUserSetLimits()
            createViewHolderMapping()
            communicationService?.sendAlarmLimitsToVentilator()
            customProgressDialog?.takeIf { it.isVisible }?.apply {
                this.dismiss()
            }
        }
        (requireActivity() as DashBoardActivity).sendAlarmLimitDataToLiveFragment()
    }


    fun updateKnob(value: String) {
        customProgressDialog?.takeIf { it.isVisible }?.apply {
            updateWithTimeoutDebounce(value)
        }

    }

    override fun handleDialogClose() {
        customProgressDialog?.takeIf { it.isVisible }?.dismiss()
        this deSelect currentView
        initUserSetLimits()
    }

    override fun onLimitChange(previousValue: Float, newValue: Float) {
        currentView?.apply { setValueOnLimitView(this, newValue) }
    }

    private fun setValueOnProgressView(activeView: View, lowerLimit: Float, upperLimit: Float) {
        activeView.param_progress_bar.maxProgress = upperLimit.toInt().toDouble()
    }

    private fun setValueOnLimitView(activeView: View, newValue: Float) {


        // update the view
        //activeView.progress_bar.progress = newValue.toInt()
        // activeView.param_progress_bar.setProgress(newValue.toInt())
        activeView.param_progress_bar.setCurrentProgress(newValue.toInt().toDouble())
        Log.i("LIMIT1_CHECK", "value = ${supportPrecision(activeView, newValue.toString())}")
        activeView.textView.text = supportPrecision(activeView, newValue.toString())

        // update runtime variable value
        when (activeView) {
            includePressureUpperLimit -> {
                presserUpperLimit = newValue
            }

            includePressureLoweLimit -> {
                presserLowerLimit = newValue
            }

            includeVTeUpperLimit -> {
                vteUpperLimit = newValue
            }

            includeVTeLowerLimit -> {
                vteLowerLimit = newValue
            }

            includePeepUpperLimit -> {
                peepUpperLimit = newValue
            }

            includePeepLowerLimit -> {
                peepLowerLimit = newValue
            }

            includeRRUpperLimit -> {
                respiratoryUpperLimit = newValue

            }

            includeRRLowerLimit -> {
                respiratoryLowerLimit = newValue
            }

            includeMVEUpperLimit -> {
                mveUpperLimit = newValue
            }

            includeMVELowerLimit -> {
                mveLowerLimit = newValue
            }
        }
        /* mveToggle.id -> {
            setmveLimitState(isOn)
        }*/
    }

    private fun supportPrecision(v: View, value: String): String {
        val decimalSupportedViews = listOf<View>(
            includeMVELowerLimit,
            includeMVEUpperLimit
        )

        return if (v in decimalSupportedViews) {
            Log.i("LIMIT1_CHECK", "Decimal supported view")
            value
        } else {
            Log.i("LIMIT1_CHECK", "Decimal NOT supported view")
            value.toFloatOrNull()?.toInt()?.toString() ?: value
        }

    }

    private fun select(limitView: View?) {
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

    private infix fun LimitOneFragment.deSelect(limitView: View?) {
        limitView?.param_progress_bar?.progress
        limitView?.textView?.setTextColor(Color.BLACK)
        context?.let {
            limitView?.param_progress_bar?.background =
                ContextCompat.getDrawable(it, R.drawable.progresscircle)
        }
    }

    override fun onClick(selectedView: View) {
        currentView = selectedView
        select(selectedView)
        // ToDO : Write labels for all parameters 5 x 2
        var activeLabel: String? = null
        var isUpperLimit: Boolean? = null
        when (selectedView) {
            includePressureLoweLimit -> {
                activeLabel = LBL_PIP
                isUpperLimit = false
            }

            includePressureUpperLimit -> {
                activeLabel = LBL_PIP
                isUpperLimit = true
            }

            includeVTeUpperLimit -> {
                activeLabel = LBL_VTE
                isUpperLimit = true
            }

            includeVTeLowerLimit -> {
                activeLabel = LBL_VTE
                isUpperLimit = false
            }

            includePeepUpperLimit -> {
                activeLabel = LBL_PEEP
                isUpperLimit = true
            }

            includePeepLowerLimit -> {
                activeLabel = LBL_PEEP
                isUpperLimit = false
            }

            includeRRUpperLimit -> {
                activeLabel = LBL_RR
                isUpperLimit = true
            }

            includeRRLowerLimit -> {
                activeLabel = LBL_RR
                isUpperLimit = false
            }

            includeMVEUpperLimit -> {
                activeLabel = LBL_MVE
                isUpperLimit = true
            }

            includeMVELowerLimit -> {
                activeLabel = LBL_MVE
                isUpperLimit = false
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
                    encoderOption =
                        ControlParameterLimit(
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
                            activeLabel, 1, it.actualMin, getParameterUnit(
                                requireContext(),
                                activeLabel
                            )
                        )

                    Log.i("limit_one_array", "" + parameterModel?.name)
                    encoderOption =
                        ControlParameterLimit(
                            it.defaultMin,
                            it.defaultMax
                        )
                    //                encoderValue = EncoderValue(it.minTileView,it.tvMax, encoderOption?.valuePerRotation?.toFloat() )
                    encoderValue = encoderOption?.valuePerRotation?.let { it1 ->
                        EncoderValue(
                            it.defaultMin, it.actualMax,
                            it1
                        )
                    }
                }
            }

            customProgressDialog = parameterModel?.let { it1 ->
                encoderValue?.let { it2 ->
                    KnobDialog.newInstance(
                        onKnobPressListener = this,
                        onTimeoutListener = this,
                        onCloseListener = object : OnDismissDialogListener {
                            override fun handleDialogClose() {
                                this@LimitOneFragment deSelect currentView
                                initUserSetLimits()
                            }
                        },
                        parameterModel = it1,
                        encoderValue = it2,
                        onLimitChangeListener = this
                    )
                }
            }
            customProgressDialog?.let {
                it.show(childFragmentManager, LimitOneFragment.TAG)
                it.startTimeoutWithDebounce()
            }

//        hideSystemUI()
//        currentView?.textView?.text=presserUpperLimit!!.toString()
        }
    }

    /*  override fun onSwitched(toggleableView: ToggleableView?, isOn: Boolean) {

                  prefManager?.apply {
                      when (toggleableView?.id) {

                          presserToggle.id -> {
                              setPipLimitState(isOn)
                          }

                          vteToggle.id -> {
                              setVteLimitState(isOn)
                          }

                          peepToggle.id -> {
                              setPeepLimitState(isOn)
                          }

                          rrToggle.id -> {
                              setRRLimitState(isOn)
                          }

                          mveToggle.id -> {
                              setmveLimitState(isOn)
                          }
                      }
                  }

              }*/


    override fun onPause() {
        super.onPause()

        Log.i("ACTIVITY_LIFECYCLE", "ON_PAUSE $javaClass")
    }

    override fun onStop() {
        super.onStop()
        limitOneObserver.apply {
            this?.let { this@LimitOneFragment.lifecycle.removeObserver(it) }
        }
        limitOneObserver = null
        Log.i("ACTIVITY_LIFECYCLE", "ON_STOP $javaClass")
    }

    //Views get destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("ACTIVITY_LIFECYCLE", "ON_DESTROY $javaClass")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("ACTIVITY_LIFECYCLE", "ON_STOP $javaClass")
    }


    inner class LimitOneObserver : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
        }

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            prefManager = PreferenceManager(requireContext())
            default_presserUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["presserUpperLimit"]?.toFloat() // requireActivity().getString(R.string.default_max_pip_limit).toFloat()
            default_presserLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["presserLowerLimit"]?.toFloat() // requireActivity().getString(R.string.default_min_pip_limit).toFloat()
            default_vtiUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["vtiUpperLimit"]?.toFloat() // requireActivity().getString(R.string.default_max_vti_limit).toFloat()
            default_vtiLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["vtiLowerLimit"]?.toFloat() // requireActivity().getString(R.string.default_min_vti_limit).toFloat()
            default_vteUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["vteUpperLimit"]?.toFloat() // requireActivity().getString(R.string.default_max_vte_limit).toFloat()
            default_vteLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["vteLowerLimit"]?.toFloat() // requireActivity().getString(R.string.default_min_vte_limit).toFloat()
            default_peepUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["peepUpperLimit"]?.toFloat() // requireActivity().getString(R.string.default_max_peep_limit).toFloat()
            default_peepLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["peepLowerLimit"]?.toFloat() // requireActivity().getString(R.string.default_min_peep_limit).toFloat()
            default_respiratoryUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["respiratoryUpperLimit"]?.toFloat() // requireActivity().getString(R.string.default_max_rr_limit).toFloat()
            default_respiratoryLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["respiratoryLowerLimit"]?.toFloat() // requireActivity().getString(R.string.default_min_rr_limit).toFloat()
            default_mveUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["mveUpperLimit"]?.toFloat() // requireActivity().getString(R.string.default_max_mve_limit).toFloat()
            default_mveLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["mveLowerLimit"]?.toFloat() // requireActivity().getString(R.string.default_min_mve_limit).toFloat()

            mEventViewModel =
                ViewModelProvider(this@LimitOneFragment).get(EventViewModel::class.java)
            hideSystemUI()
            setupClickListener()
            createViewBindingMap()
        }

        // configuration changes like rotation, multi-window mode, etc.
        // are handled in onPause and onStop
        // so that the view can be recreated
        // and the limits can be reset to default values
        // this is to avoid any issues with the progress bar and the limits
        // as the progress bar is not recreated on configuration changes
        // this is a workaround for the issues
        // as the progress bar is not recreated on configuration changes

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            includePressureLoweLimit.param_progress_bar.setCurrentProgress(0.0)
            includePressureUpperLimit.param_progress_bar.setCurrentProgress(0.0)
            includeVTeUpperLimit.param_progress_bar.setCurrentProgress(0.0)
            includeVTeLowerLimit.param_progress_bar.setCurrentProgress(0.0)
            includePeepUpperLimit.param_progress_bar.setCurrentProgress(0.0)
            includePeepLowerLimit.param_progress_bar.setCurrentProgress(0.0)
            includeRRUpperLimit.param_progress_bar.setCurrentProgress(0.0)
            includeRRLowerLimit.param_progress_bar.setCurrentProgress(0.0)
            includeMVEUpperLimit.param_progress_bar.setCurrentProgress(0.0)
            includeMVELowerLimit.param_progress_bar.setCurrentProgress(0.0)

            default_presserUpperLimit = null
            default_presserLowerLimit = null
            default_vtiUpperLimit = null
            default_vtiLowerLimit = null
            default_vteUpperLimit = null
            default_vteLowerLimit = null
            default_peepUpperLimit = null
            default_peepLowerLimit = null
            default_respiratoryUpperLimit = null
            default_respiratoryLowerLimit = null
            default_mveUpperLimit = null
            default_mveLowerLimit = null

            currentView = null

            presserUpperLimit = null
            presserLowerLimit = null
            vtiUpperLimit = null
            vtiLowerLimit = null
            vteUpperLimit = null
            vteLowerLimit = null
            peepUpperLimit = null
            peepLowerLimit = null
            respiratoryUpperLimit = null
            respiratoryLowerLimit = null
            mveUpperLimit = null
            mveLowerLimit = null
            Log.d("THELIFECYCLEOWNERPAUSE", "PAUSE method invoked")
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            Log.d("THELIFECYCLEOWNERSTOP", "STOP method invoked")
        }
    }
}




