package com.agvahealthcare.ventilator_ext.alarm

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.alarm.buffer.BufferFragment
import com.agvahealthcare.ventilator_ext.alarm.limit_one.LimitOneFragment
import com.agvahealthcare.ventilator_ext.alarm.limit_two.LimitTwoFragment
import com.agvahealthcare.ventilator_ext.callback.OnAlarmLimitChangeListener
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.control.advanced.AdvancedFragment
import com.agvahealthcare.ventilator_ext.control.etcuff.EtCuffFragment
import com.agvahealthcare.ventilator_ext.control.smartfio2.SmartFio2
import com.agvahealthcare.ventilator_ext.control.vtas.Vtas
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.standby.StandbyBackupFragment
import com.agvahealthcare.ventilator_ext.standby.StandbyBasicFragment
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_AND
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_MINUS
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_PLUS
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_alarm_dialog.*
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.buttonStartVent
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.focusLayoutStandbyControls
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.imageViewCrossStandbyControls
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyAdvanced
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyBackup
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyBasic
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbySmartFio2
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyVTas
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.mainViewPanelStandbyControls


class AlarmDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "AlarmDialog"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"


        fun newInstance(
            height: Int?,
            width: Int?,
            communicationService: CommunicationService?,
            closeListener: OnDismissDialogListener?,
            onAlarmLimitChangeListener: OnAlarmLimitChangeListener?,
        ): AlarmDialogFragment {
            val args = Bundle()
            height?.let { args.putInt(KEY_HEIGHT, it) }
            width?.let { args.putInt(KEY_WIDTH, it) }
            val fragment = AlarmDialogFragment()
            fragment.arguments = args
            fragment.service = communicationService
            fragment.closeListener = closeListener
            fragment.onAlarmLimitChangeListener = onAlarmLimitChangeListener

            return fragment
        }
    }

    private var closeListener: OnDismissDialogListener? = null
    private var onAlarmLimitChangeListener: OnAlarmLimitChangeListener? = null

    private var limitOneFragment: LimitOneFragment? = null
    private var limitTwoFragment: LimitTwoFragment? = null
    private var bufferFragment: BufferFragment? = null
    private var preferenceManager: PreferenceManager? = null
    private var heightDialog: Int? = null
    private var widthDialog: Int? = null
    private var bundle: Bundle? = null

    var service: CommunicationService? = null
    private var alarmsDialogFragmentObserver: AlarmsDialogFragmentObserver? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        alarmsDialogFragmentObserver = AlarmsDialogFragmentObserver()
        alarmsDialogFragmentObserver.apply {
            this?.let { this@AlarmDialogFragment.lifecycle.addObserver(it) }
        }
    }


    // knob highlight logic starts here
    private var sizeOfCurrentArray = 0

    private var highlightedIndex = -1
    private var visibilityTimeout: CountDownTimer? = null

    private fun highlightAdapters(highlightedIndex: Int) {
        if (limitOneFragment != null) limitOneFragment?.highlightAdapterPosition(highlightedIndex)
        else if (limitTwoFragment != null) limitTwoFragment?.highlightAdapterPosition(
            highlightedIndex
        )
    }

    private fun handleAdaptersClick(highlightedIndex: Int) {
        if (limitOneFragment != null) limitOneFragment?.handleClick(highlightedIndex)
        else if (limitTwoFragment != null) limitTwoFragment?.handleClick(highlightedIndex)
    }

    private fun makeAllFragmentsNull() {
        limitTwoFragment = null
        limitOneFragment = null
        bufferFragment = null
    }

    @SuppressLint("NotifyDataSetChanged")
    fun highlightViewWithFocus(data: String) {

        Log.i("value_check_bonds", "index : $highlightedIndex ,size : $sizeOfCurrentArray")

        if (limitOneFragment?.customProgressDialog?.isVisible == true) {
            limitOneFragment?.updateKnob(data)
        } else if (limitTwoFragment?.knobDialog?.isVisible == true) {
            limitTwoFragment?.updateKnob(data)
        } else {

            clearPreviousConstraints()
            startTimeoutWithDebounce()

            when (data) {
                PREFIX_PLUS -> {
                    if (highlightedIndex < (sizeOfCurrentArray + 4)) highlightedIndex++
                    else {
                        highlightedIndex = 0
                    }

                    getViewForFocus(false)?.let {
                        highlightAdapters(-1)
                        changeConstraintsOfFocusLayout(it)
                    } ?: kotlin.run {
                        highlightAdapters(highlightedIndex)
                    }
                }

                PREFIX_MINUS -> {

                    if (highlightedIndex > 0) highlightedIndex--
                    else {
                        highlightedIndex = (sizeOfCurrentArray + 4)
                    }

                    getViewForFocus(true)?.let {
                        highlightAdapters(-1)
                        changeConstraintsOfFocusLayout(it)
                    } ?: kotlin.run {
                        highlightAdapters(highlightedIndex)
                    }
                }

                PREFIX_AND -> {
                    getViewForFocus(null)?.let {
                        if (highlightedIndex == sizeOfCurrentArray + 1) {
                            it.callOnClick()
                        }

                        else {
                            it.buttonView.callOnClick()

                            // reset highlight index to starting position after clicking on any fragments
                            highlightedIndex = -1
                        }
                    } ?: kotlin.run {
                        handleAdaptersClick(highlightedIndex)
                    }

                    clearPreviousConstraints()
                }
            }
        }
    }

    private fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainViewPanelAlarmDialog)
            constraintSet.clear(focusLayoutAlarmDialog.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutAlarmDialog.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutAlarmDialog.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutAlarmDialog.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelAlarmDialog)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelAlarmDialog)
        constraintSet.connect(
            focusLayoutAlarmDialog.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutAlarmDialog.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutAlarmDialog.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutAlarmDialog.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelAlarmDialog)
    }

    private fun getViewForFocus(isMinus: Boolean?): View? {

        return when (highlightedIndex) {

            in 0..sizeOfCurrentArray -> {
                if (sizeOfCurrentArray == 0) {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }else null
            }

            sizeOfCurrentArray + 1 -> imageViewCrossAlarmDialog
            sizeOfCurrentArray + 2 -> {
                if (includeButtonLimit1.isVisible) {
                    includeButtonLimit1
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 3 -> includeButtonLimit2

            sizeOfCurrentArray + 4 -> includeButtonBuffer

            else -> null
        }
    }

    fun startTimeoutWithDebounce() {

        cancelTimeout()

        visibilityTimeout = object : CountDownTimer(10000, 2000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                highlightAdapters(-1)
                clearPreviousConstraints()
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

    // knob highlight logic ends here
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_alarm_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        preferenceManager = PreferenceManager(requireContext())

        if (preferenceManager?.readLastVentMode() == Configs.MODE_HFNC) {
            includeButtonLimit1.visibility = View.GONE
        } else {
            includeButtonLimit1.visibility = View.VISIBLE
        }
        setupClickListener()
        includeButtonLimit1.buttonView.setPadding(10, 0, 10, 0)
        includeButtonLimit2.buttonView.setPadding(10, 0, 10, 0)
        includeButtonBuffer.buttonView.setPadding(10, 0, 10, 0)
    }

    override fun onStart() {
        super.onStart()
        bundle = this.arguments
    }

    //By Default Fragment
    private fun setUpLimitOne() {

        sizeOfCurrentArray = 9
        makeAllFragmentsNull()
        if (limitOneFragment == null) onAlarmLimitChangeListener?.let {
            limitOneFragment = LimitOneFragment(service, it)
        }

        limitOneFragment?.apply {
            replaceFragment(this, this::class.java.javaClass.simpleName, R.id.alarm_nav_container)
        }
        includeButtonLimit2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonLimit1.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)

        includeButtonLimit2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonLimit1.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonBuffer.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonBuffer.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        //setPaddingOnButton()

    }

    // ClickListener on Buttons
    private fun setupClickListener() {

        includeButtonLimit1.buttonView.text = getString(R.string.hint_basic_limits)
        includeButtonLimit2.buttonView.text = getString(R.string.hint_advance_limits)
        includeButtonBuffer.buttonView.text = getString(R.string.hint_active_alarms)

        includeButtonBuffer.buttonView.isEnabled = true
        includeButtonBuffer.buttonView.isFocusable = false
        includeButtonBuffer.buttonView.isClickable = false

        includeButtonBuffer.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonBuffer.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)


        imageViewCrossAlarmDialog.setOnClickListener {


            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitNow()
//            requireActivity().supportFragmentManager.popBackStack()

            closeListener?.handleDialogClose()

            /* closeListener?.handleDialogClose()
             dismiss()*/
        }

        includeButtonLimit1.buttonView.setOnClickListener {
            setUpLimitOne()
        }

        includeButtonLimit2.buttonView.setOnClickListener {

            sizeOfCurrentArray = 3
            makeAllFragmentsNull()
            if (limitTwoFragment == null) onAlarmLimitChangeListener?.let {
                limitTwoFragment = LimitTwoFragment(service, it)
            }

            limitTwoFragment?.apply {
                replaceFragment(
                    this,
                    this::class.java.javaClass.simpleName,
                    R.id.alarm_nav_container
                )
            }

            includeButtonLimit2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
            includeButtonLimit1.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

            includeButtonLimit2.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonLimit1.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )

            includeButtonBuffer.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonBuffer.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            //setPaddingOnButton()

        }

        includeButtonBuffer.buttonView.setOnClickListener {
            //var fragment:BufferFragment?=null

            sizeOfCurrentArray = 0
            makeAllFragmentsNull()


            if (bufferFragment == null) {
                bufferFragment = BufferFragment.newInstance()
                childFragmentManager.beginTransaction()
                    .replace(
                        R.id.alarm_nav_container,
                        bufferFragment!!,
                        bufferFragment!!::class.java.javaClass.simpleName,
                    )
                    .commit()
            }

            includeButtonLimit1.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            includeButtonLimit2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            includeButtonBuffer.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
            //setPaddingOnButton()

        }
    }


    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        hideSystemUI()
    }


    override fun onStop() {
        super.onStop()
        alarmsDialogFragmentObserver.apply {
            this?.let { this@AlarmDialogFragment.lifecycle.removeObserver(it) }
        }
        alarmsDialogFragmentObserver = null
    }

    inner class AlarmsDialogFragmentObserver : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
        }

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            heightDialog = arguments?.getInt(KEY_HEIGHT)
            widthDialog = arguments?.getInt(KEY_WIDTH)
            setHeightWidthPercent(heightDialog, widthDialog, true)
            if (bundle?.getString("fragment_val") == "BufferFragment") {
                //button background color specification
                includeButtonLimit1.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
                includeButtonLimit2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
                includeButtonBuffer.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
                //button text color specification
                includeButtonLimit2.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                includeButtonLimit1.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                includeButtonBuffer.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                sizeOfCurrentArray = 0
                makeAllFragmentsNull()
                if (bufferFragment == null) {
                    bufferFragment = BufferFragment.newInstance()
                    bufferFragment?.let {
                        childFragmentManager.beginTransaction()
                            .replace(
                                R.id.alarm_nav_container,
                                it,
                                it::class.java.javaClass.simpleName,
                            )
                            .commit()
                    }
                }
            } else if (bundle?.getString("fragment_val") == "FromButton") {
                if (preferenceManager?.readLastVentMode() == Configs.MODE_HFNC) includeButtonLimit2.buttonView.callOnClick()
                else setUpLimitOne()
            }

        }


        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            limitOneFragment = null
            limitTwoFragment = null
            bufferFragment = null

            heightDialog = null
            widthDialog = null
            bundle = null
        }


    }

}