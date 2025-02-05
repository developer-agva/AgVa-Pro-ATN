package com.agvahealthcare.ventilator_ext.maneuvers.utilities

import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.alarm.limit_one.EncoderValue
import com.agvahealthcare.ventilator_ext.alarm.limit_one.KnobParameterModel
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.OnKnobPressListener
import com.agvahealthcare.ventilator_ext.callback.OnLimitChangeListener
import com.agvahealthcare.ventilator_ext.callback.SimpleCallbackListener
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.settings.SettingFragment
import com.agvahealthcare.ventilator_ext.utility.KnobDialog
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_utilities.*
import kotlinx.android.synthetic.main.knob_progress_view.view.*
import java.text.SimpleDateFormat
import java.util.*


class NebulizerFragment
    (
    private val communicationService: CommunicationService?

) : Fragment(), OnKnobPressListener,
    OnDismissDialogListener,
    OnLimitChangeListener {
    private var preferenceManager: PreferenceManager? = null
    var customProgressDialog: KnobDialog? = null
    private var dashBoardViewModel: DashBoardViewModel? = null


    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {

        when (highlightedIndex) {

            0 -> progress_nebuliser.callOnClick()
            1 -> {
                if (btnNebulizationsttart.isVisible) btnNebulizationsttart.callOnClick() else btnNebulizationend.callOnClick()
            }
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
            constraintSet.clone(mainViewPanelUtilities)
            constraintSet.clear(focusLayoutUtilities.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutUtilities.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutUtilities.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutUtilities.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelUtilities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelUtilities)
        constraintSet.connect(
            focusLayoutUtilities.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutUtilities.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutUtilities.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutUtilities.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelUtilities)
    }

    private fun getViewForFocus(highlightedIndex: Int): View? {

        return when (highlightedIndex) {

            0 -> progress_nebuliser
            1 -> {
                if (btnNebulizationsttart.isVisible) btnNebulizationsttart else btnNebulizationend
            }

            else -> null
        }
    }

    // logic knob highlight ends here


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        return inflater.inflate(R.layout.fragment_utilities, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
        preferenceManager = PreferenceManager(requireContext())
        dashBoardViewModel?.isNebulizerActive?.observe(viewLifecycleOwner) {
            if (it == true) {
                btnNebulizationsttart.visibility = View.GONE
                btnNebulizationend.visibility = View.VISIBLE
            } else {
                btnNebulizationsttart.visibility = View.VISIBLE
                btnNebulizationend.visibility = View.GONE
            }
        }
        preferenceManager?.apply {
            Log.d("valueofprogress", "The value of the nebulizer" + readNebuliserTime().toString())
            (progress_nebuliser?.param_progress_bar as? CircularProgressIndicator)?.apply {
                this.setCurrentProgress(
                    getPercentage(
                        readNebuliserTime().toDouble(),
                        5.0,
                        30.0
                    ).toDouble()
                )
            }
            progress_nebuliser.textView.setText("" + readNebuliserTime().toInt())
        }

        // the progress bar id
        progress_nebuliser.setOnClickListener {

            it.let {
                (it?.param_progress_bar as? CircularProgressIndicator)?.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.progresscircle_with_selection_yellow
                    )
            }

            // ToastFactory.custom(activity,"The click is working fine")
            var encoder = EncoderValue(5f, 30f, 5f)
            preferenceManager?.readNebuliserTime()?.let { it1 ->
                KnobParameterModel(
                    Configs.LBL_Volume_KEY,
                    Configs.LBL_Volume_KEY,
                    1,
                    it1,
                    ""
                ).also {
                    customProgressDialog = KnobDialog.newInstance(
                        onKnobPressListener = this,
                        onTimeoutListener = this,
                        parameterModel = it,
                        encoderValue = encoder,
                        onLimitChangeListener = this,
                        onCloseListener = this
                    )

                    customProgressDialog?.let { dialog ->
                        dialog.show(childFragmentManager, SettingFragment.TAG)
                        dialog.startTimeoutWithDebounce()
                    }
                }
            }
        }

        btnNebulizationsttart.setOnClickListener {
            Log.i("NEBULISER_FLAG", VentilatorApp.isNebuliserActive.toString())
            val tmv = preferenceManager?.readNebuliserTime()
            if (VentilatorApp.isNebuliserActive) {
                when (tmv) {
                    5f -> {
                        communicationService?.send(getString(R.string.cmd_vent_nebuliser) + "005")
                    }

                    10f -> {
                        communicationService?.send(getString(R.string.cmd_vent_nebuliser) + "010")
                    }

                    15f -> {
                        communicationService?.send(getString(R.string.cmd_vent_nebuliser) + "015")
                    }

                    20f -> {
                        communicationService?.send(getString(R.string.cmd_vent_nebuliser) + "020")
                    }

                    25f -> {
                        communicationService?.send(getString(R.string.cmd_vent_nebuliser) + "025")
                    }

                    30f -> {
                        communicationService?.send(getString(R.string.cmd_vent_nebuliser) + "030")
                    }

                }
                VentilatorApp.isNebuliserActive = true
            } else {
                ToastFactory.custom(context, "No Oxygen Supply connected")
            }
        }

        btnNebulizationend.setOnClickListener {
            communicationService?.send(getString(R.string.cmd_vent_nebuliser) + "000")
        }

    }

    val current = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val formatted = formatter.format(current)


    fun updateKnobData(knobData: String) {
        customProgressDialog?.takeIf { it.isVisible }?.apply {
            knobData.apply {
                updateWithTimeoutDebounce(this)
            }
        }
    }


    private fun getPercentage(value: Double, min: Double, max: Double): Int {
        return (((value - min) / (max - min)) * 100).toInt()
    }

    private fun getValueFromPercentage(percent: Float): Float {
        val value = (percent * 25 / 100) + 5
        Log.d("something", value.toString())
        return value
    }

    override fun handleDialogClose() {

        progress_nebuliser.let {
            (it?.param_progress_bar as? CircularProgressIndicator)?.background =
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.progresscircle
                )
        }

        preferenceManager?.readNebuliserTime()?.apply {
            progress_nebuliser.param_progress_bar.setCurrentProgress(
                getPercentage(
                    this.toDouble().toDouble(), 5.0, 30.0
                ).toDouble()
            )
            progress_nebuliser.textView.setText("" + this.toInt())
        }

        customProgressDialog?.takeIf { it.isVisible }?.dismiss()
    }

    override fun onKnobPress(previousValue: Float, newValue: Float) {

        progress_nebuliser.let {
            (it?.param_progress_bar as? CircularProgressIndicator)?.background =
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.progresscircle
                )
        }

        preferenceManager.apply {
            this?.setNebuliserTime(getValueFromPercentage(progress_nebuliser.param_progress_bar.progress.toFloat()))
        }

        progress_nebuliser.param_progress_bar.setCurrentProgress(
            getPercentage(
                newValue.toDouble().toDouble(), 5.0, 30.0
            ).toDouble()
        )
        progress_nebuliser.textView.setText("" + newValue.toInt())

        // change here 13 feb
        customProgressDialog?.takeIf { it.isVisible }?.apply {
            this.dismiss()
        }
    }

    override fun onLimitChange(previousValue: Float, newValue: Float) {

        (progress_nebuliser?.param_progress_bar as? CircularProgressIndicator)?.apply {
            this.setCurrentProgress(getPercentage(newValue.toDouble(), 5.0, 30.0).toDouble())
        }
        (progress_nebuliser?.textView as? TextView)?.apply {
            this.text = newValue.toInt().toString()
        }
    }

}