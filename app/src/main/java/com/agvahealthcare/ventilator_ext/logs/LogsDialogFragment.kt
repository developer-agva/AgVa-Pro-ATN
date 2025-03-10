package com.agvahealthcare.ventilator_ext.logs

import LogsTrendsFragment
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.databinding.FragmentLogsDialogBinding
import com.agvahealthcare.ventilator_ext.logs.alarm.AlarmFragment
import com.agvahealthcare.ventilator_ext.logs.event.EventsFragment
import com.agvahealthcare.ventilator_ext.utility.replaceFragment
import com.agvahealthcare.ventilator_ext.utility.setHeightWidthPercent
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_AND
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_MINUS
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_PLUS

class LogsDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "LogsDialog"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"

        fun newInstance(
            height: Int?,
            width: Int?,
            closeListener: OnDismissDialogListener?
        ): LogsDialogFragment {
            val args = Bundle()
            height?.let { args.putInt(KEY_HEIGHT, it) }
            width?.let { args.putInt(KEY_WIDTH, it) }
            return LogsDialogFragment().apply {
                arguments = args
                this.closeListener = closeListener
            }
        }
    }

    private lateinit var binding : FragmentLogsDialogBinding
    private var closeListener: OnDismissDialogListener? = null
    private var eventsFragment: EventsFragment? = null
    private var alarmFragment: AlarmFragment? = null
    private var dashBoardViewModel: DashBoardViewModel? = null
    private var trendsOtherFragment: LogsTrendsFragment? = null

    private var logsType: LogsType? = null

    fun updateTrendData(){
        trendsOtherFragment?.takeIf { it.isVisible }?.apply {
            setupDataDefault()
        }
    }
    enum class LogsType {
        TRENDS,
        EVENTS,
        ALARMS
    }

    // knob highlight logic starts here


    private fun makeAllFragmentsNull() {
        trendsOtherFragment = null
        eventsFragment = null
        alarmFragment = null
    }

    private var highlightedIndex = 0
    private var visibilityTimeout: CountDownTimer? = null

    @SuppressLint("NotifyDataSetChanged")
    fun highlightViewWithFocus(data: String) {

        startTimeoutWithDebounce()
        Log.i("value_check_logs", "$highlightedIndex ,$logsType")

        when (data) {
            PREFIX_PLUS -> {


                when (logsType) {
                    LogsType.TRENDS -> scrollTrendsForward()
                    LogsType.ALARMS -> scrollAlarmsForward()
                    LogsType.EVENTS -> scrollEventsForward()
                    else -> {
                        if (highlightedIndex < 5) highlightedIndex++
                        else highlightedIndex = 1

                        getViewForFocus()?.let { changeConstraintsOfFocusLayout(it.second) }
                    }
                }

            }

            PREFIX_MINUS -> {

                when (logsType) {
                    LogsType.TRENDS -> scrollTrendsBack()
                    LogsType.ALARMS -> scrollAlarmsBackward()
                    LogsType.EVENTS -> scrollEventsBackward()
                    else -> {
                        if (highlightedIndex > 1) highlightedIndex--
                        else highlightedIndex = 5

                        getViewForFocus()?.let { changeConstraintsOfFocusLayout(it.second) }
                    }
                }
            }

            PREFIX_AND -> {

                if (highlightedIndex == 5) {
                    logsType = if (trendsOtherFragment != null){
                        if (logsType != LogsType.TRENDS) LogsType.TRENDS else null
                    }else if (eventsFragment != null){
                        if (logsType != LogsType.EVENTS) LogsType.EVENTS else null
                    } else {
                        if (logsType != LogsType.ALARMS) LogsType.ALARMS else null
                    }
                } else if (highlightedIndex == 4) {
                    getViewForFocus()?.first?.callOnClick()
                } else {
                    logsType = null
                    getViewForFocus()?.first?.callOnClick()
                }
            }
        }
    }

    private fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.mainViewPanelLogs)
            constraintSet.clear(binding.focusLayoutLogs.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutLogs.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutLogs.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutLogs.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelLogs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelLogs)
        constraintSet.connect(
            binding.focusLayoutLogs.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutLogs.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutLogs.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutLogs.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelLogs)
    }

    private fun getViewForFocus(): Pair<View,View>? {


        return when (highlightedIndex) {
            1 -> Pair(binding.includeButtonTrends.buttonView,binding.includeButtonTrends.root)

            2 -> Pair(binding.includeButtonEvents.buttonView,binding.includeButtonEvents.root)
            3 -> Pair(binding.includeButtonAlarms.buttonView,binding.includeButtonAlarms.root)
            4 -> Pair(binding.imageViewCrossLogs,binding.imageViewCrossLogs)
            5 -> Pair(binding.logsNavContainer,binding.logsNavContainer)

            else -> null
        }
    }

    fun startTimeoutWithDebounce() {

        cancelTimeout()

        visibilityTimeout = object : CountDownTimer(10000, 2000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
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
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogsDialogBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
        dashBoardViewModel?.updateIsLogsTrendsFragmentVisible(false)
        Log.i("eventTesting", "set false in viewCreated")
        dashBoardViewModel?.updateIsEventsFragmentVisible(false)
        dashBoardViewModel?.updateIsAlarmsFragmentVisible(false)
        dashBoardViewModel?.logsDateUpdate?.observe(viewLifecycleOwner, Observer {
            Log.i("dadw123", it.toString())
            binding.tvDate.text = it?.split(" ")?.get(0) ?: ""
        })
        setUpTrends()
        setupClickListener()
    }

    private fun setPaddingOnButton() {

        binding.includeButtonTrends.buttonView.setPadding(35, 10, 35, 10)
        binding.includeButtonEvents.buttonView.setPadding(35, 10, 35, 10)
        binding.includeButtonAlarms.buttonView.setPadding(35, 10, 35, 10)

    }

    private fun setAllFragmentsFalse() {
        if (trendsOtherFragment?.isVisible == false || trendsOtherFragment == null) {
            dashBoardViewModel?.updateIsLogsTrendsFragmentVisible(false)
        }
        if (alarmFragment?.isVisible == false || alarmFragment == null) {
            dashBoardViewModel?.updateIsAlarmsFragmentVisible(false)
        }
        if (eventsFragment?.isVisible == false || eventsFragment == null) {
            dashBoardViewModel?.updateIsEventsFragmentVisible(false)
        }
    }

    // ClickListener on Buttons
    private fun setupClickListener() {

        binding.includeButtonTrends.buttonView.text = getString(R.string.hint_trends)
        binding.includeButtonEvents.buttonView.text = getString(R.string.hint_events)
        binding.includeButtonAlarms.buttonView.text = getString(R.string.hint_alarms)

        binding.imageViewCrossLogs.setOnClickListener {

            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitNow()

            closeListener?.handleDialogClose()
        }
        //ToDo:- paging functionality
        binding.includeButtonTrends.buttonView.setOnClickListener {
            setUpTrends()
        }

        binding.includeButtonEvents.buttonView.setOnClickListener {
            setUpEvents()
        }

        binding.includeButtonAlarms.buttonView.setOnClickListener {
            setupAlarms()
        }
    }

    private fun setUpTrends() {
        makeAllFragmentsNull()
        setAllFragmentsFalse()
        if (logsType != null) logsType = LogsType.TRENDS
        if (trendsOtherFragment == null) {
            binding.tvDate.visibility = View.VISIBLE
            binding.tvlogsDate.visibility = View.VISIBLE

            trendsOtherFragment = LogsTrendsFragment()
            trendsOtherFragment?.apply {
                replaceFragment(
                    this,
                    this::class.java.javaClass.simpleName,
                    R.id.logs_nav_container
                )
            }


            binding.includeButtonTrends.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
            binding.includeButtonEvents.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            binding.includeButtonAlarms.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            binding.includeButtonTrends.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.includeButtonEvents.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.includeButtonAlarms.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            setPaddingOnButton()

        }
    }

    private fun setUpEvents() {
        makeAllFragmentsNull()
        setAllFragmentsFalse()
        binding.tvDate.visibility = View.GONE
        binding.tvlogsDate.visibility = View.GONE
        if (logsType != null) logsType = LogsType.EVENTS
        if (eventsFragment == null) {
            eventsFragment = EventsFragment()
            eventsFragment?.apply {
                replaceFragment(
                    this,
                    this::class.java.javaClass.simpleName,
                    R.id.logs_nav_container
                )
            }

            binding.includeButtonTrends.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            binding.includeButtonEvents.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
            binding.includeButtonAlarms.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            binding.includeButtonEvents.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.includeButtonTrends.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.includeButtonAlarms.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )

            setPaddingOnButton()


        }
    }

    private fun setupAlarms() {
        makeAllFragmentsNull()
        setAllFragmentsFalse()
        binding.tvDate.visibility = View.GONE
        binding.tvlogsDate.visibility = View.GONE
        if (logsType != null) logsType = LogsType.ALARMS
        if (alarmFragment == null) {
            alarmFragment = AlarmFragment()
            alarmFragment?.apply {
                replaceFragment(
                    this,
                    this::class.java.javaClass.simpleName,
                    R.id.logs_nav_container
                )
            }
            binding.includeButtonTrends.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            binding.includeButtonEvents.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            binding.includeButtonAlarms.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
            binding.includeButtonAlarms.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.includeButtonTrends.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            binding.includeButtonEvents.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )

        }
    }

    override fun onStart() {
        super.onStart()
        val heightDialog = arguments?.getInt(KEY_HEIGHT)
        val widthDialog = arguments?.getInt(KEY_WIDTH)

        setHeightWidthPercent(heightDialog, widthDialog, true)

    }


    fun scrollTrendsForward() = trendsOtherFragment?.takeIf { isVisible }?.scrollForward()
    fun scrollTrendsBack() = trendsOtherFragment?.takeIf { isVisible }?.scrollBack()

    fun scrollEventsForward() = eventsFragment?.takeIf { isVisible }?.scrollForward()
    fun scrollEventsBackward() = eventsFragment?.takeIf { isVisible }?.scrollBack()

    fun scrollAlarmsForward() = alarmFragment?.takeIf { isVisible }?.scrollForward()
    fun scrollAlarmsBackward() = alarmFragment?.takeIf { isVisible }?.scrollBack()


}