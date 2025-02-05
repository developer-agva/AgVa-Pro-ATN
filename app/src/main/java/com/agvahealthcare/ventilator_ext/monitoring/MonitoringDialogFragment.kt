package com.agvahealthcare.ventilator_ext.monitoring

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel
import com.agvahealthcare.ventilator_ext.monitoring.general.GeneralFragment
import com.agvahealthcare.ventilator_ext.monitoring.plateau.PlateauFragment
import com.agvahealthcare.ventilator_ext.monitoring.spo.SpO2Fragment
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_AND
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_MINUS
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_PLUS
import kotlinx.android.synthetic.main.activity_main.includeFemale
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonAIVent
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonAcv
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonBpap
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonCpap
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonHFNC
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonPcCmv
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonPcSimv
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonPcac
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonPsv
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonVcCmv
import kotlinx.android.synthetic.main.fragment_mode_dialog.buttonVcSimv
import kotlinx.android.synthetic.main.fragment_mode_dialog.focusLayoutMode
import kotlinx.android.synthetic.main.fragment_mode_dialog.imageViewCrossMode
import kotlinx.android.synthetic.main.fragment_mode_dialog.mainLayoutPanelMode
import kotlinx.android.synthetic.main.fragment_mode_dialog.rbInvasive
import kotlinx.android.synthetic.main.fragment_mode_dialog.rbNonInvasive
import kotlinx.android.synthetic.main.fragment_monitoring_dialog.*


class MonitoringDialogFragment : DialogFragment() {


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

                getViewForFocus()?.let { changeConstraintsOfFocusLayout(it) }
            }

            PREFIX_MINUS -> {
                if (highlightedIndex > 1) highlightedIndex--
                else highlightedIndex = 3

                getViewForFocus()?.let { changeConstraintsOfFocusLayout(it) }
            }

            PREFIX_AND -> {

                if (highlightedIndex == 3) {
                    getViewForFocus()?.callOnClick()
                } else {
                    getViewForFocus()?.buttonView?.callOnClick()
                }
            }
        }
    }

    private fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainViewPanelMonitoring)
            constraintSet.clear(focusLayoutMonitoring.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutMonitoring.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutMonitoring.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutMonitoring.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelMonitoring)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelMonitoring)
        constraintSet.connect(
            focusLayoutMonitoring.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutMonitoring.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutMonitoring.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutMonitoring.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelMonitoring)
    }

    private fun getViewForFocus(): View? {


        return when (highlightedIndex) {
            1 -> includeButtonGeneral
            2 -> includeButtonSPO2
            3 -> imageViewCrossMonitoring

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

        val layout = inflater.inflate(R.layout.fragment_monitoring_dialog, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        setUpGeneral()
        setupClickListener()
    }


    // ClickListener on Button
    private fun setupClickListener() {

        includeButtonGeneral.buttonView.text = getString(R.string.hint_general)
        includeButtonSPO2.buttonView.text = getString(R.string.hint_spo2)

        imageViewCrossMonitoring.setOnClickListener {

            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitNow()

            closeListener?.handleDialogClose()

        }

        includeButtonGeneral.buttonView.setOnClickListener {
            setUpGeneral()
        }


        includeButtonSPO2.buttonView.setOnClickListener {

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

        includeButtonGeneral.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
        includeButtonGeneral.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonSPO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)


        includeButtonSPO2.buttonView.setTextColor(
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


        includeButtonGeneral.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonSPO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)


        includeButtonGeneral.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonSPO2.buttonView.setTextColor(
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

//        setHeightWidthPercent(heightDialog , widthDialog , true)

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