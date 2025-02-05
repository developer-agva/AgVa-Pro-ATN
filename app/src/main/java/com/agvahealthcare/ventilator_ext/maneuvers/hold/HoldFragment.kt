package com.agvahealthcare.ventilator_ext.maneuvers.hold

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.SimpleCallbackListener
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.database.entities.EventDataModel
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.maneuvers.ManeuversDialogFragment
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.google.android.material.transition.Hold
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_hold.*
import java.text.SimpleDateFormat
import java.util.*


class HoldFragment
    (
    private val communicationService: CommunicationService?,
    private val onCloseListener: SimpleCallbackListener?,
    private var isFromKnob: String?,

    ) : Fragment() {
    private var preferenceManager: PreferenceManager? = null
    private var minTimeLimit: Float = 0.0f
    private var maxTimeLimit: Float = 0.0f
    private var dashBoardViewModel: DashBoardViewModel? = null
    private var mEventViewModel: EventViewModel? = null
    private var TagFromFragment: String? = null

    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> if (topBar.isVisible) backBtn.callOnClick() else {

                includeButtonExpirationHold.buttonView.callOnClick()
            }

            1 -> if (topBar.isVisible) btnDec.callOnClick() else {

                includeButtonInspirationHold.buttonView.callOnClick()
            }

            2 -> btnInc.callOnClick()
            3 -> if (btnInspHoldStart.isVisible) btnInspHoldStart.callOnClick() else btnExpiratHoldStart.callOnClick()
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
            constraintSet.clone(mainViewPanelHold)
            constraintSet.clear(focusLayoutHold.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutHold.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutHold.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutHold.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelHold)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelHold)
        constraintSet.connect(
            focusLayoutHold.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutHold.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutHold.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutHold.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelHold)
    }

    //
    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {

            Log.i("valueasd213", topBar.isVisible.toString() + " " + highlightedIndex + " " + data)

            return when (highlightedIndex) {

                0 -> if (topBar.isVisible) backBtn else holdLayout1
                1 -> if (topBar.isVisible) holdLayout3 else holdLayout2
                2 -> {
                    if (topBar.isVisible) holdLayout4 else null
                }

                3 -> {
                    if (topBar.isVisible) holdLayout5 else null
                }

                else -> null
            }
        } ?: run {
            return null
        }
    }
//    // logic knob highlight ends here


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_hold, container, false)
    }

    override fun onDetach() {
        super.onDetach()
        TagFromFragment = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
        mEventViewModel = ViewModelProvider(requireActivity())[EventViewModel::class.java]
        preferenceManager = PreferenceManager(requireContext())
        setupData()
        handleBackButton()
        preferenceManager?.apply {

            val limits = readManeuversPplatLimits()

            // null safety & condition check
            if(limits.size>1){
                minTimeLimit = limits[0]!!
                maxTimeLimit = limits[1]!!
                tvTime.text = minTimeLimit.toString()
            }
        }

        setOnClickListener()

        btnInc.setOnClickListener {
            clickAdd()
        }

        btnDec.setOnClickListener {
            clickSubtract()
        }

        btnInspHoldStart.setOnClickListener {
            inspiraterClicView()
        }

        btnExpiratHoldStart.setOnClickListener {
            expiratoryClickView()
        }

    }


    fun addEvents(eventMsg: String, uhid: String) {

        val eventDataModel = EventDataModel(
            eventMsg, uhid
        )
        mEventViewModel?.addEvent(eventDataModel)
    }


    val current: Date = Calendar.getInstance().time

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private val formatted: String = formatter.format(current)

    @SuppressLint("DefaultLocale")
    private fun expiratoryClickView() {
        setDurationBoxLayout.visibility = View.GONE

        try {
            val timePeriod = String.format("%.1f", tvTime.text.toString().toFloat())
            communicationService?.takeIf { it.isPortsConnected }?.apply {
                send(resources.getString(R.string.prefix_expiratory_hold) + timePeriod)
                onCloseListener?.doAction()
            }
            addEvents("Expiratory Hold Requested", preferenceManager?.readUHID().toString())

            preferenceManager?.apply {
                setMeasureTime(timePeriod)
            }

        } catch (e: Exception) {
            Log.e("PARSE_ERROR", "Error in parsing expiratory time period")
            e.printStackTrace()
        }
    }


    @SuppressLint("DefaultLocale")
    private fun inspiraterClicView() {
        setDurationBoxLayout.visibility = View.GONE

        try {
            val timePeriod = String.format("%.1f", tvTime.text.toString().toFloat())

            communicationService?.takeIf { it.isPortsConnected }?.apply {
                send(resources.getString(R.string.prefix_inspiratory_hold) + timePeriod)
                onCloseListener?.doAction()
            }
            addEvents("Inspiratory Hold Requested", preferenceManager?.readUHID().toString())

            preferenceManager?.apply {
                setMeasureTime(timePeriod)
            }

        } catch (e: Exception) {
            Log.e("PARSE_ERROR", "Error in parsing expiratory time period")
            e.printStackTrace()
        }
    }


    private fun handleBackButton() {

        backBtn.setOnClickListener {
            (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
            (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 1
            if (TagFromFragment == "Expiratory" || TagFromFragment == "Inspiratory") {
                topBar.visibility = View.GONE
                tvMainTitle.visibility = View.GONE
                backBtn.visibility = View.GONE
                tvMainTitle.text = ""

                setupData()
                setDurationBoxLayout.visibility = View.GONE
                holdFragmentButtonsLayout.visibility = View.VISIBLE
                includeButtonInspirationHold.buttonView.setOnClickListener {
                    (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
                    (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3
                    setDurationBoxLayout.visibility = View.VISIBLE

                    btnInspHoldStart.visibility = View.VISIBLE
                    btnExpiratHoldStart.visibility = View.GONE

                    holdFragmentButtonsLayout.visibility = View.GONE

                    topBar.visibility = View.VISIBLE
                    tvMainTitle.visibility = View.VISIBLE
                    backBtn.visibility = View.VISIBLE
                    tvMainTitle.text = "Inspiratory hold"
                }

                includeButtonExpirationHold.buttonView.setOnClickListener {
                    (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
                    (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3
                    setDurationBoxLayout.visibility = View.VISIBLE

                    btnExpiratHoldStart.visibility = View.VISIBLE
                    btnInspHoldStart.visibility = View.GONE

                    holdFragmentButtonsLayout.visibility = View.GONE

                    topBar.visibility = View.VISIBLE
                    tvMainTitle.visibility = View.VISIBLE
                    backBtn.visibility = View.VISIBLE
                    tvMainTitle.text = "Expiratory hold"

                }
            }
        }
    }

    // setUp Data
    private fun setupData() {
        topBar.visibility = View.GONE
        tvMainTitle.visibility = View.GONE
        backBtn.visibility = View.GONE
        tvMainTitle.text = ""

        includeButtonInspirationHold.buttonView.text = getString(R.string.hint_inspiration_hold)
        includeButtonExpirationHold.buttonView.text = getString(R.string.hint_expiration_hold)
        includeButtonInspirationHold.buttonView.setPaddingRelative(40, 10, 40, 10)
        includeButtonExpirationHold.buttonView.setPaddingRelative(42, 10, 42, 10)

        preferenceManager?.apply {

            if (readManeuversPplatValue().toString().contains("0.0")) {
                textViewInspirationHoldValue.text = "Pplat -"
                textViewInspirationHoldTime.text = "-"
            } else {
                textViewInspirationHoldValue.text = "Pplat = ${readManeuversPplatValue()} cmH₂O"
                textViewInspirationHoldTime.text = readInspiratoryDate()
            }

            if (readManeuversStaticComplianceValue().toString().contains("0.0")) {
                textViewInspirationHoldValuesecond.text = "Static Comp -"
                textViewInspirationHoldTime.text = "-"

            } else {
                textViewInspirationHoldValuesecond.text = "Static Comp = ${
                    readManeuversStaticComplianceValue().toString().substring(
                        0,
                        readManeuversStaticComplianceValue().toString().indexOf('.') + 2
                    )
                } mL/cmH₂O"
                textViewInspirationHoldTime.text = readInspiratoryDate()
            }

            if (readManeuversAutoPeepValue().toString().contains("0.0")) {
                textViewExpirationHoldValue.text = "Auto PEEP -"
                textViewExpirationHoldTime.text = "-"

            } else {
                textViewExpirationHoldValue.text =
                    "Auto PEEP = ${readManeuversAutoPeepValue()} cmH₂O"
                textViewExpirationHoldTime.text = readExpiratoryDate()
            }
        }
    }


    // ClickListener on Buttons
    private fun setOnClickListener() {

        if (isFromKnob.equals(Configs.EXPIRATORY_HOLD)) {
            (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
            (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3
            setDurationBoxLayout.visibility = View.VISIBLE
            btnExpiratHoldStart.visibility = View.VISIBLE
            btnInspHoldStart.visibility = View.GONE

            holdFragmentButtonsLayout.visibility = View.GONE

            topBar.visibility = View.VISIBLE
            tvMainTitle.visibility = View.VISIBLE
            backBtn.visibility = View.VISIBLE
            tvMainTitle.text = "Expiratory hold"
            TagFromFragment = "Expiratory"

        } else if (isFromKnob.equals(Configs.INSPIRATORY_HOLD)) {
            (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
            (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3
            setDurationBoxLayout.visibility = View.VISIBLE

            btnInspHoldStart.visibility = View.VISIBLE
            btnExpiratHoldStart.visibility = View.GONE

            holdFragmentButtonsLayout.visibility = View.GONE
            topBar.visibility = View.VISIBLE
            tvMainTitle.visibility = View.VISIBLE
            backBtn.visibility = View.VISIBLE
            tvMainTitle.text = "Inspiratory hold"
            TagFromFragment = "Inspiratory"
        } else {
            topBar.visibility = View.GONE
            tvMainTitle.visibility = View.GONE
            backBtn.visibility = View.GONE
            tvMainTitle.text = ""

            setDurationBoxLayout.visibility = View.GONE
            includeButtonInspirationHold.buttonView.setOnClickListener {
                (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
                (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3
                setDurationBoxLayout.visibility = View.VISIBLE

                btnInspHoldStart.visibility = View.VISIBLE
                btnExpiratHoldStart.visibility = View.GONE


                holdFragmentButtonsLayout.visibility = View.GONE

                topBar.visibility = View.VISIBLE
                tvMainTitle.visibility = View.VISIBLE
                backBtn.visibility = View.VISIBLE
                tvMainTitle.text = "Inspiratory hold"
                TagFromFragment = "Inspiratory"

            }

            includeButtonExpirationHold.buttonView.setOnClickListener {
                (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
                (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3

                setDurationBoxLayout.visibility = View.VISIBLE

                btnExpiratHoldStart.visibility = View.VISIBLE
                btnInspHoldStart.visibility = View.GONE

                holdFragmentButtonsLayout.visibility = View.GONE

                topBar.visibility = View.VISIBLE
                tvMainTitle.visibility = View.VISIBLE
                backBtn.visibility = View.VISIBLE
                tvMainTitle.text = "Expiratory hold"
                TagFromFragment = "Expiratory"

            }
        }

    }


    private fun clickAdd() {
        try {
            var currentTime = tvTime.text.toString().toFloat()
            currentTime = currentTime.plus(0.5f)
            if (currentTime <= maxTimeLimit && currentTime >= minTimeLimit)
                tvTime.text = String.format("%.1f", currentTime);
        } catch (e: Exception) {
            Log.e("PARSE_ERROR", "Error in parsing expiratory time period")
            e.printStackTrace()
        }
    }

    private fun clickSubtract() {
        try {
            var currentTime = tvTime.text.toString().toFloat()
            currentTime -= 0.5f
            if (currentTime <= maxTimeLimit && currentTime >= minTimeLimit)
                tvTime.text = String.format("%.1f", currentTime)
        } catch (e: Exception) {
            Log.e("PARSE_ERROR", "Error in parsing expiratory time period")
            e.printStackTrace()
        }
    }


}