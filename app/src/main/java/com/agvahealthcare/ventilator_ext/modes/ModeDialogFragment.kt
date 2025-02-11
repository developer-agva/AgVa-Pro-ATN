package com.agvahealthcare.ventilator_ext.modes

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.globalModeType
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import com.agvahealthcare.ventilator_ext.utility.VENTILATOR_MODES
import com.agvahealthcare.ventilator_ext.utility.VENTILATOR_MODE_TYPE
import com.agvahealthcare.ventilator_ext.utility.setHeightWidthPercent
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*
import com.agvahealthcare.ventilator_ext.utility.utils.IntentFactory
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_mode_dialog.*
import kotlinx.android.synthetic.main.fragment_settings.volumeLayout
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.buttonStartVent
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.focusLayoutStandbyControls
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.imageViewCrossStandbyControls
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyAdvanced
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyBackup
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyBasic
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbySmartFio2
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyVTas
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.mainViewPanelStandbyControls
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


interface OnModeConfirmListener {
    fun onConfirm(modeCode: Int)
    fun onCancel()
}


class ModeDialogFragment : DialogFragment(), View.OnClickListener {

    companion object {
        const val TAG = "ModeDialog"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"
        private const val KEY_STATUS = "KEY_STATUS"

        fun newInstance(
            height: Int?,
            width: Int?,
            status: Boolean?,
            onModeConfirmListener: OnModeConfirmListener?,
            closeListener: OnDismissDialogListener?
        ): ModeDialogFragment {
            val args = Bundle()
            height?.let { args.putInt(KEY_HEIGHT, it) }
            width?.let { args.putInt(KEY_WIDTH, it) }
            status?.let { args.putBoolean(KEY_STATUS, it) }

            return ModeDialogFragment().apply {
                this.arguments = args
                this.onModeConfirmListener = onModeConfirmListener
                this.closeListener = closeListener
            }
        }
    }

    private var closeListener: OnDismissDialogListener? = null
    private var onModeConfirmListener: OnModeConfirmListener? = null
    private lateinit var modeButtons: List<AppCompatButton>
    private var ventMode: Int? = null
    private var animator: ObjectAnimator? = null
    private var colorCoroutineScope: Job? = null
    private var modeType: Int = 0
    private var optionSelected = false

    private var currentMode: String? = null
    private var preferenceManager: PreferenceManager? = null
    private var modeSettingsList: ArrayList<ControlParameterModel>? = null

    //private var dialogModeConfirmation: AlertDialog? = null
    private var isStatus: Boolean? = null
    private lateinit var mEventViewModel: EventViewModel

    init {
        modeSettingsList = ArrayList<ControlParameterModel>()
    }

    override fun onResume() {

        Log.i("check_selected_options", "on resume")
        if (tag == "MainActivity") {
            rbInvasive.isChecked = false
            rbNonInvasive.isChecked = false
            rbNasalProngs.isChecked = false
            Log.i("check_selected_options", "main activity conditions")
        } else {
            Log.i("check_selected_options", "dashboard conditions")
            preferenceManager?.apply {

                readSelectedOptions()?.let {
                    when (it) {

                        SELECTED_OPTIONS.INVASIVE_NAME -> {

                            Log.i("check_selected_options", "$it conditions")
                            rbInvasive.isChecked = true
                        }

                        SELECTED_OPTIONS.NON_INVASIVE_NAME -> {

                            Log.i("check_selected_options", "$it conditions")
                            rbNonInvasive.isChecked = true
                        }

                        SELECTED_OPTIONS.PRONGS_NAME -> {

                            Log.i("check_selected_options", "$it conditions")
                            rbNasalProngs.isChecked = true
                        }
                    }
                }
            }
        }
        super.onResume()
    }

    // knob highlight logic starts here

    private var highlightedIndex = 0
    private var visibilityTimeout: CountDownTimer? = null

    @SuppressLint("NotifyDataSetChanged")
    fun highlightViewWithFocus(data: String) {

        startTimeoutWithDebounce()
        Log.i("value_check_tiles", "$highlightedIndex")

        when (data) {
            PREFIX_PLUS -> {
                if (highlightedIndex < 15) highlightedIndex++
                else highlightedIndex = 1

                getViewForFocus(false)?.let { changeConstraintsOfFocusLayout(it) }
            }

            PREFIX_MINUS -> {
                if (highlightedIndex > 1) highlightedIndex--
                else highlightedIndex = 15

                getViewForFocus(true)?.let { changeConstraintsOfFocusLayout(it) }
            }

            PREFIX_AND -> {

                if (highlightedIndex == 1) {
                    (getViewForFocus(null) as AppCompatRadioButton).isChecked =
                        !rbNasalProngs.isChecked
                } else if (highlightedIndex == 2) {
                    (getViewForFocus(null) as AppCompatRadioButton).isChecked =
                        !rbNonInvasive.isChecked
                } else if (highlightedIndex == 3) {
                    (getViewForFocus(null) as AppCompatRadioButton).isChecked =
                        !rbInvasive.isChecked
                } else {
                    getViewForFocus(null)?.callOnClick()
                }
            }
        }
    }

    private fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainLayoutPanelMode)
            constraintSet.clear(focusLayoutMode.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutMode.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutMode.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutMode.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainLayoutPanelMode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainLayoutPanelMode)
        constraintSet.connect(
            focusLayoutMode.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutMode.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutMode.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutMode.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainLayoutPanelMode)
    }

    private fun getViewForFocus(isMinus: Boolean?): View? {


        return when (highlightedIndex) {
            1 -> if (preferenceManager?.readCurrentUid() != PatientProfile.TYPE_NEONAT) {
                isMinus?.let {
                    if (isMinus) highlightedIndex-- else highlightedIndex++
                    getViewForFocus(isMinus)
                }
            } else {
                rbNasalProngs
            }

            2 -> rbNonInvasive
            3 -> rbInvasive
            4 -> imageViewCrossMode
            5 -> {
                if (VentilatorApp.selectedOptions != Configs.SELECTED_OPTIONS.INVASIVE_NAME) {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                } else {
                    buttonVcCmv
                }
            }

            6 -> {
                if (VentilatorApp.selectedOptions != Configs.SELECTED_OPTIONS.INVASIVE_NAME) {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                } else {
                    buttonVcSimv
                }
            }

            7 -> {
                if (VentilatorApp.selectedOptions != Configs.SELECTED_OPTIONS.INVASIVE_NAME) {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                } else {
                    buttonAcv
                }
            }

            8 -> if (VentilatorApp.selectedOptions == Configs.SELECTED_OPTIONS.PRONGS_NAME) {
                isMinus?.let {
                    if (isMinus) highlightedIndex-- else highlightedIndex++
                    getViewForFocus(isMinus)
                }
            } else {
                buttonPcCmv
            }

            9 -> if (VentilatorApp.selectedOptions == Configs.SELECTED_OPTIONS.PRONGS_NAME) {
                isMinus?.let {
                    if (isMinus) highlightedIndex-- else highlightedIndex++
                    getViewForFocus(isMinus)
                }
            } else {
                buttonPcSimv
            }

            10 -> if (VentilatorApp.selectedOptions == Configs.SELECTED_OPTIONS.PRONGS_NAME) {
                isMinus?.let {
                    if (isMinus) highlightedIndex-- else highlightedIndex++
                    getViewForFocus(isMinus)
                }
            } else {
                buttonPcac
            }

            11 -> if (VentilatorApp.selectedOptions == Configs.SELECTED_OPTIONS.PRONGS_NAME) {
                isMinus?.let {
                    if (isMinus) highlightedIndex-- else highlightedIndex++
                    getViewForFocus(isMinus)
                }
            } else {
                buttonPsv
            }

            12 -> {
                if (preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                } else {
                    buttonAIVent
                }
            }

            13 -> if (VentilatorApp.selectedOptions == Configs.SELECTED_OPTIONS.PRONGS_NAME) {
                isMinus?.let {
                    if (isMinus) highlightedIndex-- else highlightedIndex++
                    getViewForFocus(isMinus)
                }
            } else {
                buttonBpap
            }

            14 -> buttonCpap
            15 -> {
                if (VentilatorApp.selectedOptions == SELECTED_OPTIONS.PRONGS_NAME && preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) buttonHFNC
                if (VentilatorApp.selectedOptions == SELECTED_OPTIONS.NON_INVASIVE_NAME && preferenceManager?.readCurrentUid() != PatientProfile.TYPE_NEONAT) buttonHFNC
                else
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
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
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_mode_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        mEventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)

        modeButtons = listOf(
            buttonVcCmv,
            buttonVcSimv,
            buttonAcv,
            buttonPcCmv,
            buttonPcSimv,
            buttonPsv,
            buttonPcac,
            buttonHFNC,
            buttonAprv,
            buttonPrvc,
            buttonAIVent,
            buttonBpap,
            buttonCpap,
            buttonNCBpap
        )

        checkPatientType()
        setupClickListener()
        setModeViaPreference()

        rbNasalProngs.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbInvasive.isChecked = false
                rbNonInvasive.isChecked = false
                textViewSpontaneous.text = "INVASIVE MODE"
                buttonCpap.visibility = View.VISIBLE
                buttonBpap.visibility = View.GONE
                textViewVolumeControl.visibility = View.VISIBLE
                buttonVcCmv.visibility = View.GONE
                buttonVcSimv.visibility = View.GONE
                buttonAcv.visibility = View.GONE
                buttonCpap.visibility = View.GONE
                textViewPressureControl.visibility = View.GONE
                buttonPcCmv.visibility = View.GONE
                buttonPcSimv.visibility = View.GONE
                buttonPcac.visibility = View.GONE
                buttonPsv.visibility = View.GONE
                colorLayout.visibility = View.GONE

                if (preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    buttonHFNC.visibility = View.VISIBLE
                    textViewHFNC.visibility = View.VISIBLE
                    textViewIntelligentVentilation.visibility = View.GONE
                    buttonAIVent.visibility = View.GONE
                } else {
                    textViewIntelligentVentilation.visibility = View.VISIBLE
                    buttonAIVent.visibility = View.VISIBLE
                    buttonHFNC.visibility = View.GONE
                    textViewHFNC.visibility = View.GONE
                }
                optionSelected = true
                animator?.cancel()
                VentilatorApp.selectedOptions = Configs.SELECTED_OPTIONS.PRONGS_NAME
                radioLayout.setBackgroundResource(R.drawable.background_transparent_border_black)
            }
        }

        rbInvasive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbNasalProngs.isChecked = false
                rbNonInvasive.isChecked = false
                textViewSpontaneous.text = "INVASIVE MODE"
                textViewVolumeControl.visibility = View.VISIBLE
                buttonVcCmv.visibility = View.VISIBLE
                buttonVcSimv.visibility = View.VISIBLE
                buttonAcv.visibility = View.VISIBLE
                buttonHFNC.visibility = View.GONE
                textViewHFNC.visibility = View.GONE
                colorLayout.visibility = View.GONE
                buttonBpap.visibility = View.VISIBLE
                buttonCpap.visibility = View.VISIBLE
                buttonCpap.visibility = View.GONE
                textViewPressureControl.visibility = View.VISIBLE
                buttonPcCmv.visibility = View.VISIBLE
                buttonPcSimv.visibility = View.VISIBLE
                buttonPcac.visibility = View.VISIBLE
                buttonPsv.visibility = View.VISIBLE
                if (preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    textViewIntelligentVentilation.visibility = View.GONE
                    buttonAIVent.visibility = View.GONE
                } else {
                    textViewIntelligentVentilation.visibility = View.VISIBLE
                    buttonAIVent.visibility = View.VISIBLE
                }
                optionSelected = true
                animator?.cancel()
                VentilatorApp.selectedOptions = Configs.SELECTED_OPTIONS.INVASIVE_NAME
                radioLayout.setBackgroundResource(R.drawable.background_transparent_border_black)
            }
        }

        rbNonInvasive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                rbNasalProngs.isChecked = false
                rbInvasive.isChecked = false
                textViewSpontaneous.text = "NON - INVASIVE MODE"
                buttonCpap.visibility = View.VISIBLE
                buttonBpap.visibility = View.VISIBLE
                textViewVolumeControl.visibility = View.GONE
                buttonVcCmv.visibility = View.GONE
                buttonVcSimv.visibility = View.GONE
                buttonAcv.visibility = View.GONE
                colorLayout.visibility = View.GONE
                buttonCpap.visibility = View.GONE
                textViewPressureControl.visibility = View.VISIBLE
                buttonPcCmv.visibility = View.VISIBLE
                buttonPcSimv.visibility = View.VISIBLE
                buttonPcac.visibility = View.VISIBLE
                buttonPsv.visibility = View.VISIBLE

                if (preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    buttonHFNC.visibility = View.GONE
                    textViewHFNC.visibility = View.GONE
                    textViewIntelligentVentilation.visibility = View.GONE
                    buttonAIVent.visibility = View.GONE
                } else {
                    buttonHFNC.visibility = View.VISIBLE
                    textViewHFNC.visibility = View.VISIBLE
                    textViewIntelligentVentilation.visibility = View.VISIBLE
                    buttonAIVent.visibility = View.VISIBLE
                }

                optionSelected = true
                animator?.cancel()
                VentilatorApp.selectedOptions = Configs.SELECTED_OPTIONS.NON_INVASIVE_NAME
                radioLayout.setBackgroundResource(R.drawable.background_transparent_border_black)
            }
        }
    }

//    private fun checkSwitchChangeListener(isChecked : Boolean){
//        if (isChecked){
//
//            Log.i("check_selected_options","true")
//            textViewInvasive.setTextColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.black
//                )
//            )
//            textViewNonInvasive.setTextColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.racing_green
//                )
//            )
//            hfncModeLayout.visibility = View.VISIBLE
//        }else{
//
//            Log.i("check_selected_options","false")
//            textViewInvasive.setTextColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.racing_green
//                )
//            )
//            textViewNonInvasive.setTextColor(
//                ContextCompat.getColor(
//                    requireContext(),
//                    R.color.black
//                )
//            )
//            hfncModeLayout.visibility = View.GONE
//        }
//    }

    // Created by Masoom on 29 Dec 2022
    private fun checkPatientType() {

        when (preferenceManager?.readCurrentUid()) {

            PatientProfile.TYPE_ADULT -> {

                textViewHFNC.text = getString(R.string.hfnc)
                buttonHFNC.text = getString(R.string.hfnc)

                buttonBpap.text = getString(R.string.hint_bpap)
                buttonCpap.text = getString(R.string.hint_cpap)
            }

            PatientProfile.TYPE_PED -> {

                textViewHFNC.text = getString(R.string.hfnc)

                buttonHFNC.text = getString(R.string.hfnc)

                buttonBpap.text = getString(R.string.hint_bpap)
                buttonCpap.text = getString(R.string.hint_cpap)
            }

            PatientProfile.TYPE_NEONAT -> {
                buttonCpap.text = getString(R.string.hint_ncpap)
                buttonHFNC.text = getString(R.string.NeoNatehfnc)
                if (VentilatorApp.selectedOptions == SELECTED_OPTIONS.NON_INVASIVE_NAME) {
                    buttonBpap.text = getString(R.string.hint_nbpap)
                } else {
                    buttonBpap.text = getString(R.string.hint_bpap)
                }
            }
        }
    }


    // Modified by Masoom on 29 Dec 2022
    private fun setModeViaPreference() {

        when (preferenceManager?.readCurrentUid()) {

            PatientProfile.TYPE_ADULT -> {
                when (getExistingVentilatorMode()) {

                    MODE_VCV_CMV -> {
                        this select buttonVcCmv
                        currentMode = getString(R.string.hint_vc_cmv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                    }

                    MODE_VCV_SIMV -> {
                        this select buttonVcSimv
                        currentMode = getString(R.string.hint_vc_simv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)

                    }


                    MODE_VCV_ACV -> {
                        this select buttonAcv
                        currentMode = getString(R.string.hint_vc_cv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)

                    }


                    MODE_PC_CMV -> {
                        this select buttonPcCmv
                        currentMode = getString(R.string.hint_pc_cmv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_SIMV -> {
                        this select buttonPcSimv
                        currentMode = getString(R.string.hint_pc_imv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_AC -> {
                        this select buttonPcac
                        currentMode = getString(R.string.hint_spont)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_ARPV -> {
                        this select buttonAprv
                        currentMode = getString(R.string.hint_pc_aprv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }


                    MODE_HFNC -> {
                        this select buttonHFNC
                        currentMode = getString(R.string.hfnc)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_HFNC)
                    }

                    MODE_PC_PSV -> {
                        this select buttonPsv
                        currentMode = getString(R.string.hint_psv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_PRVC -> {
                        this select buttonPrvc
                        currentMode = getString(R.string.hint_prvc)
                        preferenceManager?.setModeType(ModeType.TYPE_Pressure)
                    }

                    MODE_AUTO_VENTILATION -> {
                        this select buttonAIVent
                        currentMode = getString(R.string.hint_ai_vent)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_AUTO_VENTILATION)

                    }

                    MODE_NIV_BPAP -> {
                        this select buttonBpap
                        currentMode = getString(R.string.hint_bpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)
                    }


                    MODE_NIV_CPAP -> {
                        this select buttonCpap
                        currentMode = getString(R.string.hint_cpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)

                    }
                }
            }

            PatientProfile.TYPE_NEONAT -> {
                when (getExistingVentilatorMode()) {

                    MODE_PC_CMV -> {
                        this select buttonPcCmv
                        currentMode = getString(R.string.hint_pc_cmv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_SIMV -> {
                        this select buttonPcSimv
                        currentMode = getString(R.string.hint_pc_imv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_ARPV -> {
                        this select buttonAprv
                        currentMode = getString(R.string.hint_pc_aprv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_AC -> {
                        this select buttonPcac
                        currentMode = getString(R.string.hint_spont)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_HFNC -> {
                        this select buttonHFNC
                        currentMode = getString(R.string.NeoNatehfnc)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_HFNC)
                    }

                    MODE_PC_PSV -> {
                        this select buttonPsv
                        currentMode = getString(R.string.hint_psv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_PRVC -> {
                        this select buttonPrvc
                        currentMode = getString(R.string.hint_prvc)
                        preferenceManager?.setModeType(ModeType.TYPE_Pressure)
                    }

                    // use when all things done
//                    MODE_NIV_NBPAP -> {
//                        this select buttonBpap
//                        currentMode = getString(R.string.hint_nbpap)
//                    }
//
//                    MODE_NIV_NCPAP -> {
//                        this select buttonCpap
//                        currentMode = getString(R.string.hint_ncpap)
//                    }

                    MODE_NIV_BPAP -> {
                        this select buttonBpap
                        currentMode = getString(R.string.hint_bpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)

                    }

                    MODE_NC_IPPV -> {
                        this select buttonNCBpap
                        currentMode = getString(R.string.hint_nc_cpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)
                    }

                    MODE_NC_CPAP -> {
                        this select buttonCpap
                        currentMode = getString(R.string.hint_ncpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)
                    }
                }
            }

            PatientProfile.TYPE_PED -> {
                when (getExistingVentilatorMode()) {

                    MODE_VCV_CMV -> {
                        this select buttonVcCmv
                        currentMode = getString(R.string.hint_vc_cmv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                    }

                    MODE_VCV_SIMV -> {
                        this select buttonVcSimv
                        currentMode = getString(R.string.hint_vc_simv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                    }


                    MODE_VCV_ACV -> {
                        this select buttonAcv
                        currentMode = getString(R.string.hint_vc_cv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                    }

                    MODE_HFNC -> {
                        this select buttonHFNC
                        currentMode = getString(R.string.hfnc)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_HFNC)
                    }


                    MODE_PC_CMV -> {
                        this select buttonPcCmv
                        currentMode = getString(R.string.hint_pc_cmv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_SIMV -> {
                        this select buttonPcSimv
                        currentMode = getString(R.string.hint_pc_imv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_ARPV -> {
                        this select buttonAprv
                        currentMode = getString(R.string.hint_pc_aprv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_AC -> {
                        this select buttonPcac
                        currentMode = getString(R.string.hint_spont)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_PSV -> {
                        this select buttonPsv
                        currentMode = getString(R.string.hint_psv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_PRVC -> {
                        this select buttonPrvc
                        currentMode = getString(R.string.hint_prvc)
                        preferenceManager?.setModeType(ModeType.TYPE_Pressure)
                    }

                    MODE_AUTO_VENTILATION -> {
                        this select buttonAIVent
                        currentMode = getString(R.string.hint_ai_vent)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_AUTO_VENTILATION)
                    }

                    MODE_NIV_BPAP -> {
                        this select buttonBpap
                        currentMode = getString(R.string.hint_bpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)

                    }

                    MODE_NIV_CPAP -> {
                        this select buttonCpap
                        currentMode = getString(R.string.hint_cpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)

                    }
                }
            }
        }
    }


    // ClickListener on Buttons
    private fun setupClickListener() {

        modeButtons.forEach { it.setOnClickListener(this) }

        imageViewCrossMode.setOnClickListener {

            VentilatorApp.selectedOptions = null


            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitNow()

            closeListener?.handleDialogClose()

        }


    }

    private fun getExistingVentilatorMode(): Int? = preferenceManager?.readLastVentMode()

    private fun isExistingVentilationModeAvailable(): Boolean {
        val ventMode = getExistingVentilatorMode()
        return ventMode != null && isValidVentilatorMode(requireContext(), ventMode)
    }

    private infix fun ModeDialogFragment.select(btn: AppCompatButton) {
        btn.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        /* btn.setBackgroundResource(R.drawable.background_green_border)
         btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))*/
    }

    private infix fun ModeDialogFragment.deSelect(btn: AppCompatButton) {
        btn.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    }

    override fun onStart() {
        super.onStart()

        val heightDialog = arguments?.getInt(KEY_HEIGHT)
        val widthDialog = arguments?.getInt(KEY_WIDTH)
        isStatus = arguments?.getBoolean(KEY_STATUS)

        setHeightWidthPercent(heightDialog, widthDialog, isStatus)
    }

    private fun showToastForNonSelectOptions() {

        try {
//            colorCoroutineScope = CoroutineScope(Dispatchers.Main).launch {
            colorLayout.visibility = View.VISIBLE
//                delay(5000L)

//                ToastFactory.custom(requireContext(),"Please select an option...")
//            }
        } catch (e: Exception) {
            Log.e("CHECK_ERROR", e.printStackTrace().toString())
        }

    }

    override fun onPause() {
        Log.i("logsdaw", "in pasua")
        colorCoroutineScope?.cancel()
        super.onPause()
    }

    // Modified by Masoom on 29 Dec 2022
    override fun onClick(v: View?) {
        when (preferenceManager?.readCurrentUid()) {

            PatientProfile.TYPE_PED -> {
                when (v) {

                    buttonVcCmv -> {
                        if (optionSelected) {
                            this select buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode = MODE_VCV_CMV
                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                        } else showToastForNonSelectOptions()
                    }


                    buttonVcSimv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this select buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            ventMode = MODE_VCV_SIMV
                            this deSelect buttonNCBpap

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                        } else showToastForNonSelectOptions()

                    }

                    buttonAcv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this select buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            ventMode = MODE_VCV_ACV
                            this deSelect buttonNCBpap

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume

                        } else showToastForNonSelectOptions()

                    }

                    buttonPcCmv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this select buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            ventMode = MODE_PC_CMV
                            this deSelect buttonNCBpap

                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure

                        } else showToastForNonSelectOptions()

                    }

                    buttonPcSimv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this select buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode = MODE_PC_SIMV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure

                        } else showToastForNonSelectOptions()

                    }

                    buttonAprv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this select buttonAprv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode = MODE_PC_ARPV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure

                        } else showToastForNonSelectOptions()

                    }

                    buttonPcac -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this select buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonPrvc
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode = MODE_PC_AC
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()


                    }

                    buttonPsv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this select buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonPrvc
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode =
                                MODE_PC_PSV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()

                    }

                    buttonPrvc -> {

                        if (optionSelected) {
                            this select buttonPrvc
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode =
                                MODE_PC_PRVC
//                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()

                    }

                    buttonHFNC -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this select buttonHFNC
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonPrvc
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap
                            // preferenceManager?.setModeType(Configs.ModeType.TYPE_HFNC)
                            ventMode =
                                MODE_HFNC
                            VentilatorApp.globalModeType = ModeType.TYPE_HFNC
                        } else showToastForNonSelectOptions()

                    }

                    buttonAIVent -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this select buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap
                            globalModeType = ModeType.TYPE_AUTO_VENTILATION

                            ventMode =
                                MODE_AUTO_VENTILATION
                        } else showToastForNonSelectOptions()

                    }

                    buttonBpap -> {

                        if (optionSelected) {
                            this select buttonBpap
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this select buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode =
                                MODE_NIV_BPAP
                        } else showToastForNonSelectOptions()

                    }

                    buttonCpap -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonPrvc
                            this deSelect buttonAprv
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this select buttonCpap
                            this deSelect buttonNCBpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode = MODE_NIV_CPAP
                        } else showToastForNonSelectOptions()

                    }

                }
            }

            PatientProfile.TYPE_NEONAT -> {
                when (v) {

                    buttonVcCmv -> {

                        if (optionSelected) {
                            this select buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode =
                                MODE_VCV_CMV

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume

                            //  preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                            Log.i("CHECK_MODE_VAL", VentilatorApp.globalModeType.toString())
                            // ToastFactory.custom(context,preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()
                    }

                    buttonVcSimv -> {
                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this select buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap
                            ventMode = MODE_VCV_SIMV
                            // preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                            //  ToastFactory.custom(context,preferenceManager?.readModeType().toString())

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                            Log.i("CHECK_MODE_VAL", VentilatorApp.globalModeType.toString())
                        } else showToastForNonSelectOptions()

                    }

                    buttonAcv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this select buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap
                            ventMode = MODE_VCV_ACV
                            //  preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
//                        Log.i("CHECK_MODE_VAL",preferenceManager?.readModeType().toString())
//                        ToastFactory.custom(context,preferenceManager?.readModeType().toString())
                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                            Log.i("CHECK_MODE_VAL", VentilatorApp.globalModeType.toString())
                        } else showToastForNonSelectOptions()

                    }

                    buttonPcCmv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this select buttonPcCmv
                            this deSelect buttonPrvc
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap
                            ventMode = MODE_PC_CMV

                            //preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())

                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()

                    }

                    buttonPcSimv -> {
                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this select buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode = MODE_PC_SIMV
                            //   preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())

                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()

                    }

                    buttonAprv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this select buttonAprv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonPrvc
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode = MODE_PC_ARPV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure

                        } else showToastForNonSelectOptions()

                    }

                    buttonPcac -> {
                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPrvc
                            this select buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap


                            ventMode =
                                MODE_PC_AC
                            //    preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()
                    }

                    buttonPsv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this select buttonPsv
                            this deSelect buttonPrvc
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode =
                                MODE_PC_PSV
//                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()

                    }

                    buttonPrvc -> {

                        if (optionSelected) {
                            this select buttonPrvc
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode =
                                MODE_PC_PRVC
//                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()

                    }

                    buttonHFNC -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this select buttonHFNC
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonNCBpap
                            this deSelect buttonCpap
//                        preferenceManager?.setModeType(Configs.ModeType.TYPE_HFNC)
                            ventMode =
                                MODE_HFNC

                            VentilatorApp.globalModeType = ModeType.TYPE_HFNC
                        } else showToastForNonSelectOptions()

                    }

                    buttonAIVent -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this select buttonAIVent
                            this deSelect buttonPrvc
                            this deSelect buttonBpap
                            this deSelect buttonNCBpap
                            this deSelect buttonCpap
                            globalModeType = ModeType.TYPE_AUTO_VENTILATION
                            ventMode =
                                MODE_AUTO_VENTILATION
                        } else showToastForNonSelectOptions()

                    }

                    buttonBpap -> {
                        if (optionSelected) {
                            this select buttonBpap
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonPrvc
                            this deSelect buttonHFNC
                            this deSelect buttonNCBpap
                            this deSelect buttonAIVent
                            this select buttonBpap
                            this deSelect buttonCpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode =
                                MODE_NIV_BPAP
                        } else showToastForNonSelectOptions()

                    }

                    buttonNCBpap -> {

                        if (optionSelected) {
                            this select buttonNCBpap
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPrvc
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this select buttonBpap
                            this deSelect buttonCpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode = MODE_NC_IPPV
                        } else showToastForNonSelectOptions()

                    }

                    buttonCpap -> {
                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonNCBpap
                            this deSelect buttonPsv
                            this deSelect buttonAprv
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this select buttonCpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

//                        ventMode = MODE_NIV_NCPAP
                            ventMode = MODE_NC_CPAP
                        } else showToastForNonSelectOptions()

                    }

                }
            }

            PatientProfile.TYPE_ADULT -> {
                when (v) {

                    buttonVcCmv -> {

                        if (optionSelected) {
                            this select buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            // VentilatorApp.globalModeType = ModeType.TYPE_Volume.ordinal
//                        modeType = ModeType.TYPE_Volume.ordinal
                            ventMode =
                                MODE_VCV_CMV

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                        } else showToastForNonSelectOptions()

                    }


                    buttonVcSimv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this select buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            ventMode = MODE_VCV_SIMV

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                        } else showToastForNonSelectOptions()

                    }

                    buttonAcv -> {
                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this select buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            ventMode = MODE_VCV_ACV

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                        } else showToastForNonSelectOptions()

                    }

                    buttonPcCmv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this select buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            ventMode = MODE_PC_CMV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()

                    }

                    buttonPcSimv -> {
                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this select buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap

                            ventMode = MODE_PC_SIMV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()


                    }

                    buttonPrvc -> {

                        if (optionSelected) {
                            this select buttonPrvc
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode =
                                MODE_PC_PRVC
//                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()

                    }

                    buttonAprv -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this select buttonAprv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            this deSelect buttonNCBpap

                            ventMode = MODE_PC_ARPV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure

                        } else showToastForNonSelectOptions()

                    }

                    buttonPcac -> {

                        if (optionSelected) {
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this select buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonPrvc
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap


                            ventMode =
                                MODE_PC_AC
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()


                    }

                    buttonPsv -> {

                        if (optionSelected) {

                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this select buttonPsv
                            this deSelect buttonPrvc
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap

                            ventMode =
                                MODE_PC_PSV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()

                    }

                    buttonHFNC -> {

                        if (optionSelected) {

                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this select buttonHFNC
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap
                            ventMode =
                                MODE_HFNC
                            VentilatorApp.globalModeType = ModeType.TYPE_HFNC

                        } else showToastForNonSelectOptions()


                    }

                    buttonAIVent -> {

                        if (optionSelected) {

                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this select buttonAIVent
                            this deSelect buttonBpap
                            this deSelect buttonCpap

                            ventMode =
                                MODE_AUTO_VENTILATION
                            VentilatorApp.globalModeType = ModeType.TYPE_AUTO_VENTILATION

                        } else showToastForNonSelectOptions()


                    }

                    buttonBpap -> {

                        if (optionSelected) {

                            this select buttonBpap
                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPcac
                            this deSelect buttonPrvc
                            this deSelect buttonPsv
                            this deSelect buttonHFNC
                            this deSelect buttonAIVent
                            this select buttonBpap
                            this deSelect buttonCpap

                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode =
                                MODE_NIV_BPAP

                        } else showToastForNonSelectOptions()


                    }

                    buttonCpap -> {

                        if (optionSelected) {

                            this deSelect buttonVcCmv
                            this deSelect buttonVcSimv
                            this deSelect buttonAcv
                            this deSelect buttonPcCmv
                            this deSelect buttonPcSimv
                            this deSelect buttonPrvc
                            this deSelect buttonPcac
                            this deSelect buttonPsv
                            this deSelect buttonAprv
                            this deSelect buttonAIVent
                            this deSelect buttonBpap
                            this select buttonCpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode = MODE_NIV_CPAP

                        } else showToastForNonSelectOptions()

                    }

                }
            }

        }

        Log.d("ventdata", ventMode.toString())
        Log.i("new_ventilation", "onCLick in Mode Dialog")
        ventMode?.takeIf { Configs.isValidVentilatorMode(requireContext(), it) }
            ?.apply {

                Log.i("new_ventilation", "condition in onCLick in Mode Dialog")
//                // IMPORTANT : NC-IPPV mode code for Nasal Prong not for Modes like HFNC and NIV_NCPAP
//                if(preferenceManager?.readProngStatus() == true ){
//                    if(preferenceManager?.readLastVentMode() != MODE_NIV_NCPAP || preferenceManager?.readLastVentMode() != MODE_HFNC )
//                    sendModeBroadcast(MODE_NC_IPPV)
//                }else{
//                    sendModeBroadcast(this)
//                }
                sendModeBroadcast(this)
                onModeConfirmListener?.onConfirm(this)
            }
//        VentilatorApp.globalModeType?.apply {
//            sendModeTypeBroadcast(this)
//        }
    }

    private fun sendModeTypeBroadcast(typeMode: Int) {
        val inte = Intent(IntentFactory.ACTION_MODE_TYPE)
        inte.putExtra(VENTILATOR_MODE_TYPE, typeMode)
        requireContext().sendBroadcast(inte)
        dismiss()
    }

    private fun sendModeBroadcast(mode: Int) {
        val i = Intent(IntentFactory.ACTION_MODE_SET)
        i.putExtra(VENTILATOR_MODES, mode)
        requireContext().sendBroadcast(i)
        dismiss()
    }

}
