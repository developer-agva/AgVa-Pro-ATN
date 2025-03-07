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
import com.agvahealthcare.ventilator_ext.databinding.FragmentHoldBinding
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.maneuvers.ManeuversDialogFragment
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
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
    private lateinit var binding:FragmentHoldBinding
    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> if (binding.topBar.isVisible) binding.backBtn.callOnClick() else {

                binding.includeButtonExpirationHold.buttonView.callOnClick()
            }

            1 -> if (binding.topBar.isVisible) binding.btnDec.callOnClick() else {

                binding.includeButtonInspirationHold.buttonView.callOnClick()
            }

            2 -> binding.btnInc.callOnClick()
            3 -> if (binding.btnInspHoldStart.isVisible) binding.btnInspHoldStart.callOnClick() else binding.btnExpiratHoldStart.callOnClick()
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
            constraintSet.clone(binding.mainViewPanelHold)
            constraintSet.clear(binding.focusLayoutHold.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutHold.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutHold.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutHold.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelHold)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelHold)
        constraintSet.connect(
            binding.focusLayoutHold.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutHold.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutHold.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutHold.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelHold)
    }

    //
    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {


            return when (highlightedIndex) {

                0 -> if (binding.topBar.isVisible) binding.backBtn else binding.holdLayout1
                1 -> if (binding.topBar.isVisible) binding.holdLayout3 else binding.holdLayout2
                2 -> {
                    if (binding.topBar.isVisible) binding.holdLayout4 else null
                }

                3 -> {
                    if (binding.topBar.isVisible) binding.holdLayout5 else null
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
        binding = FragmentHoldBinding.inflate(layoutInflater,container,false)
        return binding.root
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
                binding.tvTime.text = minTimeLimit.toString()
            }
        }

        setOnClickListener()

        binding.btnInc.setOnClickListener {
            clickAdd()
        }

        binding.btnDec.setOnClickListener {
            clickSubtract()
        }

        binding.btnInspHoldStart.setOnClickListener {
            inspiraterClicView()
        }

        binding.btnExpiratHoldStart.setOnClickListener {
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
        binding.setDurationBoxLayout.visibility = View.GONE

        try {
            val timePeriod = String.format("%.1f", binding.tvTime.text.toString().toFloat())
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
        binding.setDurationBoxLayout.visibility = View.GONE

        try {
            val timePeriod = String.format("%.1f", binding.tvTime.text.toString().toFloat())

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

        binding.backBtn.setOnClickListener {
            (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
            (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 1
            if (TagFromFragment == "Expiratory" || TagFromFragment == "Inspiratory") {
                binding.topBar.visibility = View.GONE
                binding.tvMainTitle.visibility = View.GONE
                binding.backBtn.visibility = View.GONE
                binding.tvMainTitle.text = ""

                setupData()
                binding.setDurationBoxLayout.visibility = View.GONE
                binding.holdFragmentButtonsLayout.visibility = View.VISIBLE
                binding.includeButtonInspirationHold.buttonView.setOnClickListener {
                    (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
                    (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3
                    binding.setDurationBoxLayout.visibility = View.VISIBLE

                    binding.btnInspHoldStart.visibility = View.VISIBLE
                    binding.btnExpiratHoldStart.visibility = View.GONE

                    binding.holdFragmentButtonsLayout.visibility = View.GONE

                    binding.topBar.visibility = View.VISIBLE
                    binding.tvMainTitle.visibility = View.VISIBLE
                    binding.backBtn.visibility = View.VISIBLE
                    binding.tvMainTitle.text = "Inspiratory hold"
                }

                binding.includeButtonExpirationHold.buttonView.setOnClickListener {
                    (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
                    (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3
                    binding.setDurationBoxLayout.visibility = View.VISIBLE

                    binding.btnExpiratHoldStart.visibility = View.VISIBLE
                    binding.btnInspHoldStart.visibility = View.GONE

                    binding.holdFragmentButtonsLayout.visibility = View.GONE

                    binding.topBar.visibility = View.VISIBLE
                    binding.tvMainTitle.visibility = View.VISIBLE
                    binding.backBtn.visibility = View.VISIBLE
                    binding.tvMainTitle.text = "Expiratory hold"

                }
            }
        }
    }

    // setUp Data
    private fun setupData() {
        binding.topBar.visibility = View.GONE
        binding.tvMainTitle.visibility = View.GONE
        binding.backBtn.visibility = View.GONE
        binding.tvMainTitle.text = ""

        binding.includeButtonInspirationHold.buttonView.text = getString(R.string.hint_inspiration_hold)
        binding.includeButtonExpirationHold.buttonView.text = getString(R.string.hint_expiration_hold)
        binding.includeButtonInspirationHold.buttonView.setPaddingRelative(40, 10, 40, 10)
        binding.includeButtonExpirationHold.buttonView.setPaddingRelative(42, 10, 42, 10)

        preferenceManager?.apply {

            if (readManeuversPplatValue().toString().contains("0.0")) {
                binding.textViewInspirationHoldValue.text = "Pplat -"
                binding.textViewInspirationHoldTime.text = "-"
            } else {
                binding.textViewInspirationHoldValue.text = "Pplat = ${readManeuversPplatValue()} cmH₂O"
                binding.textViewInspirationHoldTime.text = readInspiratoryDate()
            }

            if (readManeuversStaticComplianceValue().toString().contains("0.0")) {
                binding.textViewInspirationHoldValuesecond.text = "Static Comp -"
                binding.textViewInspirationHoldTime.text = "-"

            } else {
                binding.textViewInspirationHoldValuesecond.text = "Static Comp = ${
                    readManeuversStaticComplianceValue().toString().substring(
                        0,
                        readManeuversStaticComplianceValue().toString().indexOf('.') + 2
                    )
                } mL/cmH₂O"
                binding.textViewInspirationHoldTime.text = readInspiratoryDate()
            }

            if (readManeuversAutoPeepValue().toString().contains("0.0")) {
                binding.textViewExpirationHoldValue.text = "Auto PEEP -"
                binding.textViewExpirationHoldTime.text = "-"

            } else {
                binding.textViewExpirationHoldValue.text =
                    "Auto PEEP = ${readManeuversAutoPeepValue()} cmH₂O"
                binding.textViewExpirationHoldTime.text = readExpiratoryDate()
            }
        }
    }


    // ClickListener on Buttons
    private fun setOnClickListener() {

        if (isFromKnob.equals(Configs.EXPIRATORY_HOLD)) {
            (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
            (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3
            binding.setDurationBoxLayout.visibility = View.VISIBLE
            binding.btnExpiratHoldStart.visibility = View.VISIBLE
            binding.btnInspHoldStart.visibility = View.GONE

            binding.holdFragmentButtonsLayout.visibility = View.GONE

            binding.topBar.visibility = View.VISIBLE
            binding.tvMainTitle.visibility = View.VISIBLE
            binding.backBtn.visibility = View.VISIBLE
            binding.tvMainTitle.text = "Expiratory hold"
            TagFromFragment = "Expiratory"

        } else if (isFromKnob.equals(Configs.INSPIRATORY_HOLD)) {
            (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
            (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3
            binding.setDurationBoxLayout.visibility = View.VISIBLE

            binding.btnInspHoldStart.visibility = View.VISIBLE
            binding.btnExpiratHoldStart.visibility = View.GONE

            binding.holdFragmentButtonsLayout.visibility = View.GONE
            binding.topBar.visibility = View.VISIBLE
            binding.tvMainTitle.visibility = View.VISIBLE
            binding.backBtn.visibility = View.VISIBLE
            binding.tvMainTitle.text = "Inspiratory hold"
            TagFromFragment = "Inspiratory"
        } else {
            binding.topBar.visibility = View.GONE
            binding.tvMainTitle.visibility = View.GONE
            binding.backBtn.visibility = View.GONE
            binding.tvMainTitle.text = ""

            binding.setDurationBoxLayout.visibility = View.GONE
            binding.includeButtonInspirationHold.buttonView.setOnClickListener {
                (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
                (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3
                binding.setDurationBoxLayout.visibility = View.VISIBLE

                binding.btnInspHoldStart.visibility = View.VISIBLE
                binding.btnExpiratHoldStart.visibility = View.GONE


                binding.holdFragmentButtonsLayout.visibility = View.GONE

                binding.topBar.visibility = View.VISIBLE
                binding.tvMainTitle.visibility = View.VISIBLE
                binding.backBtn.visibility = View.VISIBLE
                binding.tvMainTitle.text = "Inspiratory hold"
                TagFromFragment = "Inspiratory"

            }

            binding.includeButtonExpirationHold.buttonView.setOnClickListener {
                (parentFragment as ManeuversDialogFragment).highlightedIndex = -1
                (parentFragment as ManeuversDialogFragment).sizeOfCurrentArray = 3

                binding.setDurationBoxLayout.visibility = View.VISIBLE

                binding.btnExpiratHoldStart.visibility = View.VISIBLE
                binding.btnInspHoldStart.visibility = View.GONE

                binding.holdFragmentButtonsLayout.visibility = View.GONE

                binding.topBar.visibility = View.VISIBLE
                binding.tvMainTitle.visibility = View.VISIBLE
                binding.backBtn.visibility = View.VISIBLE
                binding.tvMainTitle.text = "Expiratory hold"
                TagFromFragment = "Expiratory"

            }
        }

    }


    private fun clickAdd() {
        try {
            var currentTime = binding.tvTime.text.toString().toFloat()
            currentTime = currentTime.plus(0.5f)
            if (currentTime <= maxTimeLimit && currentTime >= minTimeLimit)
                binding.tvTime.text = String.format("%.1f", currentTime);
        } catch (e: Exception) {
            Log.e("PARSE_ERROR", "Error in parsing expiratory time period")
            e.printStackTrace()
        }
    }

    private fun clickSubtract() {
        try {
            var currentTime = binding.tvTime.text.toString().toFloat()
            currentTime -= 0.5f
            if (currentTime <= maxTimeLimit && currentTime >= minTimeLimit)
                binding.tvTime.text = String.format("%.1f", currentTime)
        } catch (e: Exception) {
            Log.e("PARSE_ERROR", "Error in parsing expiratory time period")
            e.printStackTrace()
        }
    }


}