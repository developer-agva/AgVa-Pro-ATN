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
import com.agvahealthcare.ventilator_ext.databinding.FragmentAlarmDialogBinding
import com.agvahealthcare.ventilator_ext.databinding.FragmentLimitOneBinding
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterLimit
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.KnobDialog
import com.agvahealthcare.ventilator_ext.utility.hideSystemUI
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*


class LimitOneFragment(
    private val communicationService: CommunicationService?,
    private val limitChangeListener: OnAlarmLimitChangeListener
) : Fragment(), OnKnobPressListener,
    OnDismissDialogListener, OnLimitChangeListener, View.OnClickListener/*, OnToggledListener*/ {
    companion object {
        val TAG = "LimitOneFragment"
    }
    private lateinit var binding : FragmentLimitOneBinding
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

            0 -> binding.includePressureUpperLimit.root.callOnClick()
            1 -> binding.includePressureLoweLimit.root.callOnClick()
            2 -> binding.includeVTeUpperLimit.root.callOnClick()
            3 -> binding.includeVTeLowerLimit.root.callOnClick()
            4 -> binding.includePeepUpperLimit.root.callOnClick()
            5 -> binding.includePeepLowerLimit.root.callOnClick()
            6 -> binding.includeRRUpperLimit.root.callOnClick()
            7 -> binding.includeRRLowerLimit.root.callOnClick()
            8 -> binding.includeMVEUpperLimit.root.callOnClick()
            9 -> binding.includeMVELowerLimit.root.callOnClick()
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
            constraintSet.clone(binding.mainViewPanelLimitOne)
            constraintSet.clear(binding.focusLayoutLimitOne.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutLimitOne.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutLimitOne.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutLimitOne.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelLimitOne)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelLimitOne)
        constraintSet.connect(
            binding.focusLayoutLimitOne.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutLimitOne.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutLimitOne.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutLimitOne.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelLimitOne)
    }

    private fun getViewForFocus(highlightedIndex: Int): View? {

        return when (highlightedIndex) {

            0 -> binding.layout1LimitOne
            1 -> binding.layout2LimitOne
            2 -> binding.layout3LimitOne
            3 -> binding.layout4LimitOne
            4 -> binding.layout5LimitOne
            5 -> binding.layout6LimitOne
            6 -> binding.layout7LimitOne
            7 -> binding.layout8LimitOne
            8 -> binding.layout9LimitOne
            9 -> binding.layout10LimitOne

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
        binding = FragmentLimitOneBinding.inflate(layoutInflater, container, false)
        return binding.root
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
            binding.presserToggle.isOn = readPipLimitState()
            binding.vteToggle.isOn = readVteLimitState()
            binding.peepToggle.isOn = readPeepLimitState()
            binding.rrToggle.isOn = readRRLimitState()
            binding.mveToggle.isOn = readMveLimitState()
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
        binding.includePressureUpperLimit.root.setOnClickListener(this)
        binding.includePressureLoweLimit.root.setOnClickListener(this)
        //ToDo:-includeVTeUpperLimit
        binding.includeVTeUpperLimit.root.setOnClickListener(this)
        binding.includeVTeLowerLimit.root.setOnClickListener(this)
        //ToDo:-includePeepUpperLimit
        binding.includePeepUpperLimit.root.setOnClickListener(this)
        binding.includePeepLowerLimit.root.setOnClickListener(this)
        //ToDo:-includeRRUpperLimit
        binding.includeRRUpperLimit.root.setOnClickListener(this)
        binding.includeRRLowerLimit.root.setOnClickListener(this)
        //ToDo:-includemveUpperLimit
        binding.includeMVEUpperLimit.root.setOnClickListener(this)
        binding.includeMVELowerLimit.root.setOnClickListener(this)
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
                    upperLimitView = binding.includePressureUpperLimit.root
                    lowerLimitView = binding.includePressureLoweLimit.root
                    defaultUpperLimit = default_presserUpperLimit
                    defaultLowerLimit = default_presserLowerLimit
                }

                LBL_VTE -> {
                    upperLimitView = binding.includeVTeUpperLimit.root
                    lowerLimitView = binding.includeVTeLowerLimit.root
                    defaultUpperLimit = default_vteUpperLimit
                    defaultLowerLimit = default_vteLowerLimit
                }

                LBL_PEEP -> {
                    upperLimitView = binding.includePeepUpperLimit.root
                    lowerLimitView = binding.includePeepLowerLimit.root
                    defaultUpperLimit = default_peepUpperLimit
                    defaultLowerLimit = default_peepLowerLimit
                }

                LBL_RR -> {
                    upperLimitView = binding.includeRRUpperLimit.root
                    lowerLimitView = binding.includeRRLowerLimit.root
                    defaultUpperLimit = default_respiratoryUpperLimit
                    defaultLowerLimit = default_respiratoryLowerLimit
                }

                LBL_MVE -> {
                    upperLimitView = binding.includeMVEUpperLimit.root
                    lowerLimitView = binding.includeMVELowerLimit.root
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

                    binding.includePressureUpperLimit.root -> {
                        setPipLimits(presserLowerLimit, presserUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            presserLowerLimit!!,
                            presserUpperLimit
                        )
                    }

                    binding.includePressureLoweLimit.root -> {
                        setPipLimits(presserLowerLimit, presserUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            presserLowerLimit!!,
                            presserUpperLimit
                        )
                    }

                    binding.includeVTeUpperLimit.root -> {
                        setVteLimits(vteLowerLimit, vteUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            vteLowerLimit!!,
                            vteUpperLimit
                        )
                    }

                    binding.includeVTeLowerLimit.root -> {
                        setVteLimits(vteLowerLimit, vteUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            vteLowerLimit!!,
                            vteUpperLimit
                        )
                    }

                    binding.includePeepUpperLimit.root -> {
                        setPEEPLimits(peepLowerLimit, peepUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            peepLowerLimit!!,
                            peepUpperLimit
                        )
                    }

                    binding.includePeepLowerLimit.root -> {
                        setPEEPLimits(peepLowerLimit, peepUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            peepLowerLimit!!,
                            peepUpperLimit
                        )
                    }

                    binding.includeRRUpperLimit.root -> {
                        setRRLimits(respiratoryLowerLimit, respiratoryUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            respiratoryLowerLimit!!,
                            respiratoryUpperLimit
                        )
                    }

                    binding.includeRRLowerLimit.root -> {
                        setRRLimits(respiratoryLowerLimit, respiratoryUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            respiratoryLowerLimit!!,
                            respiratoryUpperLimit
                        )
                    }

                    binding.includeMVEUpperLimit.root -> {
                        setMveLimits(mveLowerLimit, mveUpperLimit)
                        limitChangeListener.onChangeAlarmLimit(
                            currentKey,
                            mveLowerLimit!!,
                            mveUpperLimit
                        )
                    }

                    binding.includeMVELowerLimit.root -> {
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
        when (activeView) {
            binding.includePressureUpperLimit.root -> {
                binding.includePressureUpperLimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includePressureLoweLimit.root -> {
                binding.includePressureLoweLimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includeVTeUpperLimit.root -> {
                binding.includeVTeUpperLimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includeVTeLowerLimit.root -> {
                binding.includeVTeLowerLimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includePeepUpperLimit.root -> {
                binding.includePeepUpperLimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includePeepLowerLimit.root -> {
                binding.includePeepLowerLimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includeRRUpperLimit.root -> {
                binding.includeRRUpperLimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includeRRLowerLimit.root -> {
                binding.includeRRLowerLimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includeMVEUpperLimit.root -> {
                binding.includeMVEUpperLimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }

            binding.includeMVELowerLimit.root -> {
                binding.includeMVELowerLimit.paramProgressBar.maxProgress = upperLimit.toInt().toDouble()
            }
        }
    }

    private fun setValueOnLimitView(activeView: View, newValue: Float) {

        // update runtime variable value
        when (activeView) {
            binding.includePressureUpperLimit.root -> {
                presserUpperLimit = newValue
                binding.includePressureUpperLimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includePressureUpperLimit.textView.text = supportPrecision(activeView, newValue.toString())
            }

            binding.includePressureLoweLimit.root -> {
                presserLowerLimit = newValue
                binding.includePressureLoweLimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includePressureLoweLimit.textView.text = supportPrecision(activeView, newValue.toString())
            }

            binding.includeVTeUpperLimit.root -> {
                vteUpperLimit = newValue
                binding.includeVTeUpperLimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includeVTeUpperLimit.textView.text = supportPrecision(activeView, newValue.toString())
            }

            binding.includeVTeLowerLimit.root -> {
                vteLowerLimit = newValue
                binding.includeVTeLowerLimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includeVTeLowerLimit.textView.text = supportPrecision(activeView, newValue.toString())
            }

            binding.includePeepUpperLimit.root -> {
                peepUpperLimit = newValue
                binding.includePeepUpperLimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includePeepUpperLimit.textView.text = supportPrecision(activeView, newValue.toString())
            }

            binding.includePeepLowerLimit.root -> {
                peepLowerLimit = newValue
                binding.includePeepLowerLimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includePeepLowerLimit.textView.text = supportPrecision(activeView, newValue.toString())
            }

            binding.includeRRUpperLimit.root -> {
                respiratoryUpperLimit = newValue
                binding.includeRRUpperLimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includeRRUpperLimit.textView.text = supportPrecision(activeView, newValue.toString())
            }

            binding.includeRRLowerLimit.root -> {
                respiratoryLowerLimit = newValue
                binding.includeRRLowerLimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includeRRLowerLimit.textView.text = supportPrecision(activeView, newValue.toString())
            }

            binding.includeMVEUpperLimit.root -> {
                mveUpperLimit = newValue
                binding.includeMVEUpperLimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includeMVEUpperLimit.textView.text = supportPrecision(activeView, newValue.toString())
            }

            binding.includeMVELowerLimit.root -> {
                mveLowerLimit = newValue
                binding.includeMVELowerLimit.paramProgressBar.setCurrentProgress(newValue.toInt().toDouble())
                binding.includeMVELowerLimit.textView.text = supportPrecision(activeView, newValue.toString())
            }
        }
    }

    private fun supportPrecision(v: View, value: String): String {
        val decimalSupportedViews = listOf<View>(
            binding.includeMVELowerLimit.root,
            binding.includeMVEUpperLimit.root
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

        context?.let {
            when (limitView) {
                binding.includePressureUpperLimit.root -> {
                    setValueOnLimitView(limitView, binding.includePressureUpperLimit.paramProgressBar.progress.toFloat())
                    binding.includePressureUpperLimit.paramProgressBar.background = ContextCompat.getDrawable(it, R.drawable.progresscircle_with_selection_yellow)
                    binding.includePressureUpperLimit.textView.setTextColor(Color.BLACK)
                }

                binding.includePressureLoweLimit.root -> {
                    setValueOnLimitView(limitView, binding.includePressureLoweLimit.paramProgressBar.progress.toFloat())
                    binding.includePressureLoweLimit.paramProgressBar.background = ContextCompat.getDrawable(it, R.drawable.progresscircle_with_selection_yellow)
                    binding.includePressureLoweLimit.textView.setTextColor(Color.BLACK)
                }

                binding.includeVTeUpperLimit.root -> {
                    setValueOnLimitView(limitView, binding.includeVTeUpperLimit.paramProgressBar.progress.toFloat())
                    binding.includeVTeUpperLimit.paramProgressBar.background = ContextCompat.getDrawable(it, R.drawable.progresscircle_with_selection_yellow)
                    binding.includeVTeUpperLimit.textView.setTextColor(Color.BLACK)
                }

                binding.includeVTeLowerLimit.root -> {
                    setValueOnLimitView(limitView, binding.includeVTeLowerLimit.paramProgressBar.progress.toFloat())
                    binding.includeVTeLowerLimit.paramProgressBar.background = ContextCompat.getDrawable(it, R.drawable.progresscircle_with_selection_yellow)
                    binding.includeVTeLowerLimit.textView.setTextColor(Color.BLACK)
                }

                binding.includePeepUpperLimit.root -> {
                    setValueOnLimitView(limitView, binding.includePeepUpperLimit.paramProgressBar.progress.toFloat())
                    binding.includePeepUpperLimit.paramProgressBar.background = ContextCompat.getDrawable(it, R.drawable.progresscircle_with_selection_yellow)
                    binding.includePeepUpperLimit.textView.setTextColor(Color.BLACK)
                }

                binding.includePeepLowerLimit.root -> {
                    setValueOnLimitView(limitView, binding.includePeepLowerLimit.paramProgressBar.progress.toFloat())
                    binding.includePeepLowerLimit.paramProgressBar.background = ContextCompat.getDrawable(it, R.drawable.progresscircle_with_selection_yellow)
                    binding.includePeepLowerLimit.textView.setTextColor(Color.BLACK)
                }

                binding.includeRRUpperLimit.root -> {
                    setValueOnLimitView(limitView, binding.includeRRUpperLimit.paramProgressBar.progress.toFloat())
                    binding.includeRRUpperLimit.paramProgressBar.background = ContextCompat.getDrawable(it, R.drawable.progresscircle_with_selection_yellow)
                    binding.includeRRUpperLimit.textView.setTextColor(Color.BLACK)
                }

                binding.includeRRLowerLimit.root -> {
                    setValueOnLimitView(limitView, binding.includeRRLowerLimit.paramProgressBar.progress.toFloat())
                    binding.includeRRLowerLimit.paramProgressBar.background = ContextCompat.getDrawable(it, R.drawable.progresscircle_with_selection_yellow)
                    binding.includeRRLowerLimit.textView.setTextColor(Color.BLACK)
                }

                binding.includeMVEUpperLimit.root -> {
                    setValueOnLimitView(limitView, binding.includeMVEUpperLimit.paramProgressBar.progress.toFloat())
                    binding.includeMVEUpperLimit.paramProgressBar.background = ContextCompat.getDrawable(it, R.drawable.progresscircle_with_selection_yellow)
                    binding.includeMVEUpperLimit.textView.setTextColor(Color.BLACK)
                }

                binding.includeMVELowerLimit.root -> {
                    setValueOnLimitView(limitView, binding.includeMVELowerLimit.paramProgressBar.progress.toFloat())
                    binding.includeMVELowerLimit.paramProgressBar.background = ContextCompat.getDrawable(it, R.drawable.progresscircle_with_selection_yellow)
                    binding.includeMVELowerLimit.textView.setTextColor(Color.BLACK)
                }
            }
        }
    }

    private infix fun LimitOneFragment.deSelect(limitView: View?) {
        context?.let {
            when (limitView) {
                binding.includePressureUpperLimit.root -> {
                    binding.includePressureUpperLimit.textView.setTextColor(Color.BLACK)
                    binding.includePressureUpperLimit.paramProgressBar.background =
                        ContextCompat.getDrawable(it, R.drawable.progresscircle)
                }

                binding.includePressureLoweLimit.root -> {
                    binding.includePressureLoweLimit.textView.setTextColor(Color.BLACK)
                    binding.includePressureLoweLimit.paramProgressBar.background =
                        ContextCompat.getDrawable(it, R.drawable.progresscircle)
                }

                binding.includeVTeUpperLimit.root -> {
                    binding.includeVTeUpperLimit.textView.setTextColor(Color.BLACK)
                    binding.includeVTeUpperLimit.paramProgressBar.background =
                        ContextCompat.getDrawable(it, R.drawable.progresscircle)
                }

                binding.includeVTeLowerLimit.root -> {
                    binding.includeVTeLowerLimit.textView.setTextColor(Color.BLACK)
                    binding.includeVTeLowerLimit.paramProgressBar.background =
                        ContextCompat.getDrawable(it, R.drawable.progresscircle)
                }

                binding.includePeepUpperLimit.root -> {
                    binding.includePeepUpperLimit.textView.setTextColor(Color.BLACK)
                    binding.includePeepUpperLimit.paramProgressBar.background =
                        ContextCompat.getDrawable(it, R.drawable.progresscircle)
                }

                binding.includePeepLowerLimit.root -> {
                    binding.includePeepLowerLimit.textView.setTextColor(Color.BLACK)
                    binding.includePeepLowerLimit.paramProgressBar.background =
                        ContextCompat.getDrawable(it, R.drawable.progresscircle)
                }

                binding.includeRRUpperLimit.root -> {
                    binding.includeRRUpperLimit.textView.setTextColor(Color.BLACK)
                    binding.includeRRUpperLimit.paramProgressBar.background =
                        ContextCompat.getDrawable(it, R.drawable.progresscircle)
                }

                binding.includeRRLowerLimit.root -> {
                    binding.includeRRLowerLimit.textView.setTextColor(Color.BLACK)
                    binding.includeRRLowerLimit.paramProgressBar.background =
                        ContextCompat.getDrawable(it, R.drawable.progresscircle)
                }

                binding.includeMVEUpperLimit.root -> {
                    binding.includeMVEUpperLimit.textView.setTextColor(Color.BLACK)
                    binding.includeMVEUpperLimit.paramProgressBar.background =
                        ContextCompat.getDrawable(it, R.drawable.progresscircle)
                }

                binding.includeMVELowerLimit.root -> {
                    binding.includeMVELowerLimit.textView.setTextColor(Color.BLACK)
                    binding.includeMVELowerLimit.paramProgressBar.background =
                        ContextCompat.getDrawable(it, R.drawable.progresscircle)
                }
            }
        }
    }

    override fun onClick(selectedView: View) {
        currentView = selectedView
        select(selectedView)
        // ToDO : Write labels for all parameters 5 x 2
        var activeLabel: String? = null
        var isUpperLimit: Boolean? = null
        when (selectedView) {
            binding.includePressureLoweLimit.root -> {
                activeLabel = LBL_PIP
                isUpperLimit = false
            }

            binding.includePressureUpperLimit.root -> {
                activeLabel = LBL_PIP
                isUpperLimit = true
            }

            binding.includeVTeUpperLimit.root -> {
                activeLabel = LBL_VTE
                isUpperLimit = true
            }

            binding.includeVTeLowerLimit.root -> {
                activeLabel = LBL_VTE
                isUpperLimit = false
            }

            binding.includePeepUpperLimit.root -> {
                activeLabel = LBL_PEEP
                isUpperLimit = true
            }

            binding.includePeepLowerLimit.root -> {
                activeLabel = LBL_PEEP
                isUpperLimit = false
            }

            binding.includeRRUpperLimit.root -> {
                activeLabel = LBL_RR
                isUpperLimit = true
            }

            binding.includeRRLowerLimit.root -> {
                activeLabel = LBL_RR
                isUpperLimit = false
            }

            binding.includeMVEUpperLimit.root -> {
                activeLabel = LBL_MVE
                isUpperLimit = true
            }

            binding.includeMVELowerLimit.root -> {
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
            )["presserUpperLimit"]?.toFloat()//requireActivity().getString(R.string.default_max_pip_limit).toFloat()
            default_presserLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["presserLowerLimit"]?.toFloat()//requireActivity().getString(R.string.default_min_pip_limit).toFloat()
            default_vtiUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["vtiUpperLimit"]?.toFloat()//requireActivity().getString(R.string.default_max_vti_limit).toFloat()
            default_vtiLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["vtiLowerLimit"]?.toFloat()//requireActivity().getString(R.string.default_min_vti_limit).toFloat()
            default_vteUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["vteUpperLimit"]?.toFloat()//requireActivity().getString(R.string.default_max_vte_limit).toFloat()
            default_vteLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["vteLowerLimit"]?.toFloat()//requireActivity().getString(R.string.default_min_vte_limit).toFloat()
            default_peepUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["peepUpperLimit"]?.toFloat()//requireActivity().getString(R.string.default_max_peep_limit).toFloat()
            default_peepLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["peepLowerLimit"]?.toFloat()//requireActivity().getString(R.string.default_min_peep_limit).toFloat()
            default_respiratoryUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["respiratoryUpperLimit"]?.toFloat()//requireActivity().getString(R.string.default_max_rr_limit).toFloat()
            default_respiratoryLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["respiratoryLowerLimit"]?.toFloat()//requireActivity().getString(R.string.default_min_rr_limit).toFloat()
            default_mveUpperLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["mveUpperLimit"]?.toFloat()//requireActivity().getString(R.string.default_max_mve_limit).toFloat()
            default_mveLowerLimit = filterAlarmLimitsbyPatientType(
                prefManager?.readCurrentUid(),
                requireContext()
            )["mveLowerLimit"]?.toFloat()//requireActivity().getString(R.string.default_min_mve_limit).toFloat()


            mEventViewModel =
                ViewModelProvider(this@LimitOneFragment).get(EventViewModel::class.java)
            hideSystemUI()
            setupClickListener()
            createViewBindingMap()
            Log.d("THELIFECYCLEOWNERSTART", "START method invoked")
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            Log.d("THELIFECYCLEOWNERRESUME", "RESUME method invoked")
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            binding.includePressureLoweLimit.paramProgressBar.setCurrentProgress(0.0)
            binding.includePressureUpperLimit.paramProgressBar.setCurrentProgress(0.0)
            binding.includeVTeUpperLimit.paramProgressBar.setCurrentProgress(0.0)
            binding.includeVTeLowerLimit.paramProgressBar.setCurrentProgress(0.0)
            binding.includePeepUpperLimit.paramProgressBar.setCurrentProgress(0.0)
            binding.includePeepLowerLimit.paramProgressBar.setCurrentProgress(0.0)
            binding.includeRRUpperLimit.paramProgressBar.setCurrentProgress(0.0)
            binding.includeRRLowerLimit.paramProgressBar.setCurrentProgress(0.0)
            binding.includeMVEUpperLimit.paramProgressBar.setCurrentProgress(0.0)
            binding.includeMVELowerLimit.paramProgressBar.setCurrentProgress(0.0)

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




