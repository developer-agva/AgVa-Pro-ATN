package com.agvahealthcare.ventilator_ext.monitoring

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
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.databinding.FragmentMonitoringDialogBinding
import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel
import com.agvahealthcare.ventilator_ext.monitoring.general.GeneralFragment
import com.agvahealthcare.ventilator_ext.monitoring.spo.SpO2Fragment
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_AND
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_MINUS
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_PLUS


class MonitoringDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentMonitoringDialogBinding
    companion object {
        const val TAG = "MonitoringDialog"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"
        private var observedList = ArrayList<ObservedParameterModel>()
        private var observedSpHList = ArrayList<ObservedParameterModel>()

        fun newInstance(
            height: Int?,
            width: Int?,
            observedValueList: ArrayList<ObservedParameterModel>,
            observedValueSpHList: ArrayList<ObservedParameterModel>,
            closeListener: OnDismissDialogListener?
        ): MonitoringDialogFragment {
            val args = Bundle()
            height?.let { args.putInt(KEY_HEIGHT, it) }
            width?.let { args.putInt(KEY_WIDTH, it) }
            observedList = observedValueList
            observedSpHList = observedValueSpHList
            val fragment = MonitoringDialogFragment()
            fragment.arguments = args
            fragment.closeListener = closeListener
            return fragment
        }
    }

    private var closeListener: OnDismissDialogListener? = null
    private var generalFragment: GeneralFragment? = null
    private var spO2Fragment: SpO2Fragment? = null



    // knob highlight logic starts here

    private var highlightedIndex = 0
    private var visibilityTimeout: CountDownTimer? = null

    @SuppressLint("NotifyDataSetChanged")
    fun highlightViewWithFocus(data: String) {

        startTimeoutWithDebounce()
        Log.i("value_check_tiles", "$highlightedIndex")

        when (data) {
            PREFIX_PLUS -> {
                if (highlightedIndex < 3) highlightedIndex++
                else highlightedIndex = 1

                getViewForFocus()?.let { changeConstraintsOfFocusLayout(it.second) }
            }

            PREFIX_MINUS -> {
                if (highlightedIndex > 1) highlightedIndex--
                else highlightedIndex = 3

                getViewForFocus()?.let { changeConstraintsOfFocusLayout(it.second) }
            }

            PREFIX_AND -> {

                if (highlightedIndex == 3) {
                    getViewForFocus()?.first?.callOnClick()
                } else {
                    getViewForFocus()?.first?.callOnClick()
                }
            }
        }
    }

    private fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.mainViewPanelMonitoring)
            constraintSet.clear(binding.focusLayoutMonitoring.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutMonitoring.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutMonitoring.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutMonitoring.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelMonitoring)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelMonitoring)
        constraintSet.connect(
            binding.focusLayoutMonitoring.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutMonitoring.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutMonitoring.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutMonitoring.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelMonitoring)
    }

    private fun getViewForFocus(): Pair<View,View>? {


        return when (highlightedIndex) {
            1 -> {
                val pair = Pair(binding.includeButtonGeneral.buttonView, binding.includeButtonGeneral.root)
                pair
            }
            2 -> {
                val pair = Pair(binding.includeButtonSPO2.buttonView, binding.includeButtonSPO2.root)
                pair
            }
            3 -> {
                val pair =
                    Pair(binding.imageViewCrossMonitoring, binding.imageViewCrossMonitoring)
                pair
            }

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
        binding = FragmentMonitoringDialogBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        setUpGeneral()
        setupClickListener()
    }


    // ClickListener on Button
    private fun setupClickListener() {

        binding.includeButtonGeneral.buttonView.text = getString(R.string.hint_general)
        binding.includeButtonSPO2.buttonView.text = getString(R.string.hint_spo2)

        binding.imageViewCrossMonitoring.setOnClickListener {

            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitNow()

            closeListener?.handleDialogClose()

        }

        binding.includeButtonGeneral.buttonView.setOnClickListener {
            setUpGeneral()
        }


        binding.includeButtonSPO2.buttonView.setOnClickListener {

            setUSpO2()
        }
    }

    //By Default Fragment
    private fun setUpGeneral() {
        // spO2Fragment = null
        generalFragment = GeneralFragment()

        generalFragment?.apply {
            val bundle = Bundle()
            bundle.putSerializable("observedList", observedList)
            this.arguments = bundle


            this@MonitoringDialogFragment.childFragmentManager.beginTransaction()
                .replace(R.id.monitoring_nav_container, generalFragment!!, tag).commit()

        }

        binding.includeButtonGeneral.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
        binding.includeButtonGeneral.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        binding.includeButtonSPO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)


        binding.includeButtonSPO2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )


    }

    private fun setUSpO2() {
        spO2Fragment = SpO2Fragment()
        spO2Fragment?.apply {
            val bundle = Bundle()
            bundle.putSerializable("observedSpHList", observedSpHList)
            this.arguments = bundle


            this@MonitoringDialogFragment.childFragmentManager.beginTransaction()
                .replace(R.id.monitoring_nav_container, spO2Fragment!!, tag).commit()

        }


        binding.includeButtonGeneral.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonSPO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)


        binding.includeButtonGeneral.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        binding.includeButtonSPO2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

    }

    override fun onStart() {
        super.onStart()
        val heightDialog = arguments?.getInt(KEY_HEIGHT)
        val widthDialog = arguments?.getInt(KEY_WIDTH)
    }


    fun setModeList(observedValueList: ArrayList<ObservedParameterModel>) {
        generalFragment?.takeIf { it.isVisible }?.apply {
            setUpModeData(observedValueList)
        }
    }


    fun setSpo2DataList(observedValueSpo2List: ArrayList<ObservedParameterModel>) {
        spO2Fragment?.takeIf { it.isVisible }?.apply {
            setData(observedValueSpo2List)
        }
    }

}