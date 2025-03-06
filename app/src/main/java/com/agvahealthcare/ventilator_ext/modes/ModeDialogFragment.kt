package com.agvahealthcare.ventilator_ext.modes

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.globalModeType
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.databinding.FragmentModeDialogBinding
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.utility.VENTILATOR_MODES
import com.agvahealthcare.ventilator_ext.utility.VENTILATOR_MODE_TYPE
import com.agvahealthcare.ventilator_ext.utility.setHeightWidthPercent
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*
import com.agvahealthcare.ventilator_ext.utility.utils.IntentFactory
import kotlinx.coroutines.Job


interface OnModeConfirmListener {
    fun onConfirm(modeCode: Int)
    fun onCancel()
}


class ModeDialogFragment : DialogFragment(), View.OnClickListener {

    private lateinit var binding : FragmentModeDialogBinding
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
            binding.rbInvasive.isChecked = false
            binding.rbNonInvasive.isChecked = false
            binding.rbNasalProngs.isChecked = false
            Log.i("check_selected_options", "main activity conditions")
        } else {
            Log.i("check_selected_options", "dashboard conditions")
            preferenceManager?.apply {

                readSelectedOptions()?.let {
                    when (it) {

                        SELECTED_OPTIONS.INVASIVE_NAME -> {

                            Log.i("check_selected_options", "$it conditions")
                            binding.rbInvasive.isChecked = true
                        }

                        SELECTED_OPTIONS.NON_INVASIVE_NAME -> {

                            Log.i("check_selected_options", "$it conditions")
                            binding.rbNonInvasive.isChecked = true
                        }

                        SELECTED_OPTIONS.PRONGS_NAME -> {

                            Log.i("check_selected_options", "$it conditions")
                            binding.rbNasalProngs.isChecked = true
                        }
                    }
                }
            }
        }
        super.onResume()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentModeDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        mEventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)

        modeButtons = listOf(
            binding.buttonVcCmv,
            binding.buttonVcSimv,
            binding.buttonAcv,
            binding.buttonPcCmv,
            binding.buttonPcSimv,
            binding.buttonPsv,
            binding.buttonPcac,
            binding.buttonHFNC,
            binding.buttonAprv,
            binding.buttonPrvc,
            binding.buttonAIVent,
            binding.buttonBpap,
            binding.buttonCpap,
            binding.buttonNCBpap
        )

        checkPatientType()
        setupClickListener()
        setModeViaPreference()

        binding.rbNasalProngs.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.rbInvasive.isChecked = false
                binding.rbNonInvasive.isChecked = false
                binding.textViewSpontaneous.text = "NON - INVASIVE MODE"
                binding.buttonCpap.visibility = View.VISIBLE
                binding.buttonBpap.visibility = View.GONE
                binding.textViewVolumeControl.visibility = View.GONE
                binding.buttonVcCmv.visibility = View.GONE
                binding.buttonVcSimv.visibility = View.GONE
                binding.buttonAcv.visibility = View.GONE
                binding.textViewPressureControl.visibility = View.GONE
                binding.buttonPcCmv.visibility = View.GONE
                binding.buttonPcSimv.visibility = View.GONE
                binding.buttonPcac.visibility = View.GONE
                binding.buttonPsv.visibility = View.GONE
                binding.colorLayout.visibility = View.GONE

                if (preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    binding.buttonHFNC.visibility = View.VISIBLE
                    binding.textViewHFNC.visibility = View.VISIBLE
                    binding.textViewIntelligentVentilation.visibility = View.GONE
                    binding.buttonAIVent.visibility = View.GONE
                } else {
                    binding.textViewIntelligentVentilation.visibility = View.VISIBLE
                    binding.buttonAIVent.visibility = View.VISIBLE
                    binding.buttonHFNC.visibility = View.GONE
                    binding.textViewHFNC.visibility = View.GONE
                }
                optionSelected = true
                animator?.cancel()
                VentilatorApp.selectedOptions = SELECTED_OPTIONS.PRONGS_NAME
                binding.radioLayout.setBackgroundResource(R.drawable.background_transparent_border_black)

                if (preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {

                    val constraintSetFirst = ConstraintSet()
                    constraintSetFirst.clone(binding.mainLayoutPanelMode)
                    constraintSetFirst.connect(
                        binding.textViewSpontaneous.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal1.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetFirst.applyTo(binding.mainLayoutPanelMode)

                    val constraintSetSecond = ConstraintSet()
                    constraintSetSecond.clone(binding.mainLayoutPanelMode)
                    constraintSetSecond.connect(
                        binding.textViewHFNC.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal2.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetSecond.applyTo(binding.mainLayoutPanelMode)
                }else{
                    val constraintSetFirst = ConstraintSet()
                    constraintSetFirst.clone(binding.mainLayoutPanelMode)
                    constraintSetFirst.connect(
                        binding.textViewSpontaneous.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal1.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetFirst.applyTo(binding.mainLayoutPanelMode)

                    val constraintSetSecond = ConstraintSet()
                    constraintSetSecond.clone(binding.mainLayoutPanelMode)
                    constraintSetSecond.connect(
                        binding.textViewHFNC.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal2.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetSecond.applyTo(binding.mainLayoutPanelMode)
                }
            }
        }

        binding.rbInvasive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.rbNasalProngs.isChecked = false
                binding.rbNonInvasive.isChecked = false
                binding.textViewSpontaneous.text = "INVASIVE MODE"
                binding.textViewVolumeControl.visibility = View.VISIBLE
                binding.buttonVcCmv.visibility = View.VISIBLE
                binding.buttonVcSimv.visibility = View.VISIBLE
                binding.buttonAcv.visibility = View.VISIBLE
                binding.buttonHFNC.visibility = View.GONE
                binding.textViewHFNC.visibility = View.GONE
                binding.colorLayout.visibility = View.GONE
                binding.buttonBpap.visibility = View.VISIBLE
                binding.buttonCpap.visibility = View.VISIBLE
//                buttonCpap.visibility = View.GONE
                binding.textViewPressureControl.visibility = View.VISIBLE
                binding.buttonPcCmv.visibility = View.VISIBLE
                binding.buttonPcSimv.visibility = View.VISIBLE
                binding.buttonPcac.visibility = View.VISIBLE
                binding.buttonPsv.visibility = View.VISIBLE
                if (preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    binding.textViewIntelligentVentilation.visibility = View.GONE
                    binding.buttonAIVent.visibility = View.GONE
                } else {
                    binding.textViewIntelligentVentilation.visibility = View.VISIBLE
                    binding.buttonAIVent.visibility = View.VISIBLE
                }
                optionSelected = true
                animator?.cancel()
                VentilatorApp.selectedOptions = SELECTED_OPTIONS.INVASIVE_NAME
                binding.radioLayout.setBackgroundResource(R.drawable.background_transparent_border_black)

                if (preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {

                    val constraintSetFirst = ConstraintSet()
                    constraintSetFirst.clone(binding.mainLayoutPanelMode)
                    constraintSetFirst.connect(
                        binding.textViewVolumeControl.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal1.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetFirst.applyTo(binding.mainLayoutPanelMode)

                    val constraintSetSecond = ConstraintSet()
                    constraintSetSecond.clone(binding.mainLayoutPanelMode)
                    constraintSetSecond.connect(
                        binding.textViewPressureControl.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal2.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetSecond.applyTo(binding.mainLayoutPanelMode)


                    val constraintSetThird = ConstraintSet()
                    constraintSetThird.clone(binding.mainLayoutPanelMode)
                    constraintSetThird.connect(
                        binding.textViewSpontaneous.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal3.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetThird.applyTo(binding.mainLayoutPanelMode)
                }
                // non neonatal
                else{

                    val constraintSetFirst = ConstraintSet()
                    constraintSetFirst.clone(binding.mainLayoutPanelMode)
                    constraintSetFirst.connect(
                        binding.textViewVolumeControl.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal1.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetFirst.applyTo(binding.mainLayoutPanelMode)

                    val constraintSetSecond = ConstraintSet()
                    constraintSetSecond.clone(binding.mainLayoutPanelMode)
                    constraintSetSecond.connect(
                        binding.textViewPressureControl.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal2.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetSecond.applyTo(binding.mainLayoutPanelMode)


                    val constraintSetThird = ConstraintSet()
                    constraintSetThird.clone(binding.mainLayoutPanelMode)
                    constraintSetThird.connect(
                        binding.textViewIntelligentVentilation.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal3.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetThird.applyTo(binding.mainLayoutPanelMode)

                    val constraintSetFourth = ConstraintSet()
                    constraintSetFourth.clone(binding.mainLayoutPanelMode)
                    constraintSetFourth.connect(
                        binding.textViewSpontaneous.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal4.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetFourth.applyTo(binding.mainLayoutPanelMode)
                }
            }
        }

        binding.rbNonInvasive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.rbNasalProngs.isChecked = false
                binding.rbInvasive.isChecked = false
                binding.textViewSpontaneous.text = "NON - INVASIVE MODE"
//                buttonCpap.visibility = View.VISIBLE
                binding.buttonBpap.visibility = View.VISIBLE
                binding.textViewVolumeControl.visibility = View.GONE
                binding.buttonVcCmv.visibility = View.GONE
                binding.buttonVcSimv.visibility = View.GONE
                binding.buttonAcv.visibility = View.GONE
                binding.colorLayout.visibility = View.GONE
                binding.buttonCpap.visibility = View.VISIBLE
                binding.textViewPressureControl.visibility = View.VISIBLE
                binding.buttonPcCmv.visibility = View.VISIBLE
                binding.buttonPcSimv.visibility = View.VISIBLE
                binding.buttonPcac.visibility = View.VISIBLE
                binding.buttonPsv.visibility = View.VISIBLE
                binding.textViewIntelligentVentilation.visibility = View.GONE
                binding.buttonAIVent.visibility = View.GONE
                if (preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT) {
                    binding.buttonHFNC.visibility = View.GONE
                    binding.textViewHFNC.visibility = View.GONE

                } else {
                    binding.buttonHFNC.visibility = View.VISIBLE
                    binding.textViewHFNC.visibility = View.VISIBLE
                }

                optionSelected = true
                animator?.cancel()
                VentilatorApp.selectedOptions = SELECTED_OPTIONS.NON_INVASIVE_NAME
                binding.radioLayout.setBackgroundResource(R.drawable.background_transparent_border_black)

                if (preferenceManager?.readCurrentUid() == PatientProfile.TYPE_NEONAT){
                    val constraintSetFirst = ConstraintSet()
                    constraintSetFirst.clone(binding.mainLayoutPanelMode)
                    constraintSetFirst.connect(
                        binding.textViewSpontaneous.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal2.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetFirst.applyTo(binding.mainLayoutPanelMode)

                    val constraintSetSecond = ConstraintSet()
                    constraintSetSecond.clone(binding.mainLayoutPanelMode)
                    constraintSetSecond.connect(
                        binding.textViewPressureControl.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal1.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetSecond.applyTo(binding.mainLayoutPanelMode)
                }else{
                    val constraintSetFirst = ConstraintSet()
                    constraintSetFirst.clone(binding.mainLayoutPanelMode)
                    constraintSetFirst.connect(
                        binding.textViewPressureControl.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal1.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetFirst.applyTo(binding.mainLayoutPanelMode)

                    val constraintSetThird = ConstraintSet()
                    constraintSetThird.clone(binding.mainLayoutPanelMode)
                    constraintSetThird.connect(
                        binding.textViewSpontaneous.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal2.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetThird.applyTo(binding.mainLayoutPanelMode)

                    val constraintSetFourth = ConstraintSet()
                    constraintSetFourth.clone(binding.mainLayoutPanelMode)
                    constraintSetFourth.connect(
                        binding.textViewHFNC.id,
                        ConstraintSet.TOP,
                        binding.ModeHorizontal3.id,
                        ConstraintSet.BOTTOM,
                        0
                    )
                    constraintSetFourth.applyTo(binding.mainLayoutPanelMode)
                }
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

                binding.textViewHFNC.text = getString(R.string.hfnc)
                binding.buttonHFNC.text = getString(R.string.hfnc)

                binding.buttonBpap.text = getString(R.string.hint_bpap)
                binding.buttonCpap.text = getString(R.string.hint_cpap)
                binding.rbNasalProngs.visibility = View.GONE
            }

            PatientProfile.TYPE_PED -> {

                binding.textViewHFNC.text = getString(R.string.hfnc)

                binding.buttonHFNC.text = getString(R.string.hfnc)

                binding.buttonBpap.text = getString(R.string.hint_bpap)
                binding.buttonCpap.text = getString(R.string.hint_cpap)
                binding.rbNasalProngs.visibility = View.GONE
            }

            PatientProfile.TYPE_NEONAT -> {
                binding.buttonCpap.text = getString(R.string.hint_ncpap)
                binding.buttonHFNC.text = getString(R.string.NeoNatehfnc)
                binding.buttonBpap.text = getString(R.string.hint_nbpap)
                binding.rbNasalProngs.visibility = View.VISIBLE
            }
        }
    }


    // Modified by Masoom on 29 Dec 2022
    private fun setModeViaPreference() {

        when (preferenceManager?.readCurrentUid()) {

            PatientProfile.TYPE_ADULT -> {
                when (getExistingVentilatorMode()) {

                    MODE_VCV_CMV -> {
                        this select binding.buttonVcCmv
                        currentMode = getString(R.string.hint_vc_cmv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                    }

                    MODE_VCV_SIMV -> {
                        this select binding.buttonVcSimv
                        currentMode = getString(R.string.hint_vc_simv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)

                    }


                    MODE_VCV_ACV -> {
                        this select binding.buttonAcv
                        currentMode = getString(R.string.hint_vc_cv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)

                    }


                    MODE_PC_CMV -> {
                        this select binding.buttonPcCmv
                        currentMode = getString(R.string.hint_pc_cmv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_SIMV -> {
                        this select binding.buttonPcSimv
                        currentMode = getString(R.string.hint_pc_imv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_AC -> {
                        this select binding.buttonPcac
                        currentMode = getString(R.string.hint_spont)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_ARPV -> {
                        this select binding.buttonAprv
                        currentMode = getString(R.string.hint_pc_aprv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }


                    MODE_HFNC -> {
                        this select binding.buttonHFNC
                        currentMode = getString(R.string.hfnc)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_HFNC)
                    }

                    MODE_PC_PSV -> {
                        this select binding.buttonPsv
                        currentMode = getString(R.string.hint_psv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_PRVC -> {
                        this select binding.buttonPrvc
                        currentMode = getString(R.string.hint_prvc)
                        preferenceManager?.setModeType(ModeType.TYPE_Pressure)
                    }

                    MODE_AUTO_VENTILATION -> {
                        this select binding.buttonAIVent
                        currentMode = getString(R.string.hint_ai_vent)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_AUTO_VENTILATION)

                    }

                    MODE_NIV_BPAP -> {
                        this select binding.buttonBpap
                        currentMode = getString(R.string.hint_bpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)
                    }


                    MODE_NIV_CPAP -> {
                        this select binding.buttonCpap
                        currentMode = getString(R.string.hint_cpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)

                    }
                }
            }

            PatientProfile.TYPE_NEONAT -> {
                when (getExistingVentilatorMode()) {

                    MODE_PC_CMV -> {
                        this select binding.buttonPcCmv
                        currentMode = getString(R.string.hint_pc_cmv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_SIMV -> {
                        this select binding.buttonPcSimv
                        currentMode = getString(R.string.hint_pc_imv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_ARPV -> {
                        this select binding.buttonAprv
                        currentMode = getString(R.string.hint_pc_aprv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_AC -> {
                        this select binding.buttonPcac
                        currentMode = getString(R.string.hint_spont)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_HFNC -> {
                        this select binding.buttonHFNC
                        currentMode = getString(R.string.NeoNatehfnc)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_HFNC)
                    }

                    MODE_PC_PSV -> {
                        this select binding.buttonPsv
                        currentMode = getString(R.string.hint_psv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_PRVC -> {
                        this select binding.buttonPrvc
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
                        this select binding.buttonBpap
                        currentMode = getString(R.string.hint_bpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)

                    }

                    MODE_NC_IPPV -> {
                        this select binding.buttonNCBpap
                        currentMode = getString(R.string.hint_nc_cpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)
                    }

                    MODE_NC_CPAP -> {
                        this select binding.buttonCpap
                        currentMode = getString(R.string.hint_ncpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)
                    }
                }
            }

            PatientProfile.TYPE_PED -> {
                when (getExistingVentilatorMode()) {

                    MODE_VCV_CMV -> {
                        this select binding.buttonVcCmv
                        currentMode = getString(R.string.hint_vc_cmv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                    }

                    MODE_VCV_SIMV -> {
                        this select binding.buttonVcSimv
                        currentMode = getString(R.string.hint_vc_simv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                    }


                    MODE_VCV_ACV -> {
                        this select binding.buttonAcv
                        currentMode = getString(R.string.hint_vc_cv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                    }

                    MODE_HFNC -> {
                        this select binding.buttonHFNC
                        currentMode = getString(R.string.hfnc)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_HFNC)
                    }


                    MODE_PC_CMV -> {
                        this select binding.buttonPcCmv
                        currentMode = getString(R.string.hint_pc_cmv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_SIMV -> {
                        this select binding.buttonPcSimv
                        currentMode = getString(R.string.hint_pc_imv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                    }

                    MODE_PC_ARPV -> {
                        this select binding.buttonAprv
                        currentMode = getString(R.string.hint_pc_aprv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_AC -> {
                        this select binding.buttonPcac
                        currentMode = getString(R.string.hint_spont)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_PSV -> {
                        this select binding.buttonPsv
                        currentMode = getString(R.string.hint_psv)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                    }

                    MODE_PC_PRVC -> {
                        this select binding.buttonPrvc
                        currentMode = getString(R.string.hint_prvc)
                        preferenceManager?.setModeType(ModeType.TYPE_Pressure)
                    }

                    MODE_AUTO_VENTILATION -> {
                        this select binding.buttonAIVent
                        currentMode = getString(R.string.hint_ai_vent)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_AUTO_VENTILATION)
                    }

                    MODE_NIV_BPAP -> {
                        this select binding.buttonBpap
                        currentMode = getString(R.string.hint_bpap)
                        preferenceManager?.setModeType(Configs.ModeType.TYPE_NIV)

                    }

                    MODE_NIV_CPAP -> {
                        this select binding.buttonCpap
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

        binding.imageViewCrossMode.setOnClickListener {

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
            binding.colorLayout.visibility = View.VISIBLE
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

                    binding.buttonVcCmv -> {
                        if (optionSelected) {
                            this select binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode = MODE_VCV_CMV
                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                        } else showToastForNonSelectOptions()
                    }


                    binding.buttonVcSimv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this select binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            ventMode = MODE_VCV_SIMV
                            this deSelect binding.buttonNCBpap

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonAcv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this select binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            ventMode = MODE_VCV_ACV
                            this deSelect binding.buttonNCBpap

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume

                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPcCmv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this select binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            ventMode = MODE_PC_CMV
                            this deSelect binding.buttonNCBpap

                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure

                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPcSimv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this select binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode = MODE_PC_SIMV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure

                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonAprv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this select binding.buttonAprv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode = MODE_PC_ARPV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure

                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPcac -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this select binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode = MODE_PC_AC
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()


                    }

                    binding.buttonPsv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this select binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode =
                                MODE_PC_PSV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPrvc -> {

                        if (optionSelected) {
                            this select binding.buttonPrvc
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode =
                                MODE_PC_PRVC
//                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonHFNC -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this select binding.buttonHFNC
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap
                            // preferenceManager?.setModeType(Configs.ModeType.TYPE_HFNC)
                            ventMode =
                                MODE_HFNC
                            VentilatorApp.globalModeType = ModeType.TYPE_HFNC
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonAIVent -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this select binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap
                            globalModeType = ModeType.TYPE_AUTO_VENTILATION

                            ventMode =
                                MODE_AUTO_VENTILATION
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonBpap -> {

                        if (optionSelected) {
                            this select binding.buttonBpap
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this select binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode =
                                MODE_NIV_BPAP
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonCpap -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonAprv
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this select binding.buttonCpap
                            this deSelect binding.buttonNCBpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode = MODE_NIV_CPAP
                        } else showToastForNonSelectOptions()

                    }

                }
            }

            PatientProfile.TYPE_NEONAT -> {
                when (v) {

                    binding.buttonVcCmv -> {

                        if (optionSelected) {
                            this select binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode =
                                MODE_VCV_CMV

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume

                            //  preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                            Log.i("CHECK_MODE_VAL", VentilatorApp.globalModeType.toString())
                            // ToastFactory.custom(context,preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()
                    }

                    binding.buttonVcSimv -> {
                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this select binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap
                            ventMode = MODE_VCV_SIMV
                            // preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                            //  ToastFactory.custom(context,preferenceManager?.readModeType().toString())

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                            Log.i("CHECK_MODE_VAL", VentilatorApp.globalModeType.toString())
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonAcv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this select binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap
                            ventMode = MODE_VCV_ACV
                            //  preferenceManager?.setModeType(Configs.ModeType.TYPE_Volume)
//                        Log.i("CHECK_MODE_VAL",preferenceManager?.readModeType().toString())
//                        ToastFactory.custom(context,preferenceManager?.readModeType().toString())
                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                            Log.i("CHECK_MODE_VAL", VentilatorApp.globalModeType.toString())
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPcCmv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this select binding.buttonPcCmv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap
                            ventMode = MODE_PC_CMV

                            //preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())

                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPcSimv -> {
                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this select binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode = MODE_PC_SIMV
                            //   preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())

                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonAprv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this select binding.buttonAprv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode = MODE_PC_ARPV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure

                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPcac -> {
                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this select binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap


                            ventMode =
                                MODE_PC_AC
                            //    preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)

                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()
                    }

                    binding.buttonPsv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this select binding.buttonPsv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode =
                                MODE_PC_PSV
//                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPrvc -> {

                        if (optionSelected) {
                            this select binding.buttonPrvc
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode =
                                MODE_PC_PRVC
//                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonHFNC -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this select binding.buttonHFNC
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonNCBpap
                            this deSelect binding.buttonCpap
//                        preferenceManager?.setModeType(Configs.ModeType.TYPE_HFNC)
                            ventMode =
                                MODE_HFNC

                            VentilatorApp.globalModeType = ModeType.TYPE_HFNC
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonAIVent -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this select binding.buttonAIVent
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonNCBpap
                            this deSelect binding.buttonCpap
                            globalModeType = ModeType.TYPE_AUTO_VENTILATION
                            ventMode =
                                MODE_AUTO_VENTILATION
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonBpap -> {
                        if (optionSelected) {
                            this select binding.buttonBpap
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonNCBpap
                            this deSelect binding.buttonAIVent
                            this select binding.buttonBpap
                            this deSelect binding.buttonCpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode =
                                MODE_NIV_BPAP
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonNCBpap -> {

                        if (optionSelected) {
                            this select binding.buttonNCBpap
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this select binding.buttonBpap
                            this deSelect binding.buttonCpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode = MODE_NC_IPPV
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonCpap -> {
                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonNCBpap
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonAprv
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this select binding.buttonCpap
                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

//                        ventMode = MODE_NIV_NCPAP
                            ventMode = MODE_NC_CPAP
                        } else showToastForNonSelectOptions()

                    }

                }
            }

            PatientProfile.TYPE_ADULT -> {
                when (v) {

                    binding.buttonVcCmv -> {

                        if (optionSelected) {
                            this select binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            // VentilatorApp.globalModeType = ModeType.TYPE_Volume.ordinal
//                        modeType = ModeType.TYPE_Volume.ordinal
                            ventMode =
                                MODE_VCV_CMV

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                        } else showToastForNonSelectOptions()

                    }


                    binding.buttonVcSimv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this select binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            ventMode = MODE_VCV_SIMV

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonAcv -> {
                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this select binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            ventMode = MODE_VCV_ACV

                            VentilatorApp.globalModeType = ModeType.TYPE_Volume
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPcCmv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this select binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            ventMode = MODE_PC_CMV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPcSimv -> {
                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this select binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap

                            ventMode = MODE_PC_SIMV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()


                    }

                    binding.buttonPrvc -> {

                        if (optionSelected) {
                            this select binding.buttonPrvc
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode =
                                MODE_PC_PRVC
//                        preferenceManager?.setModeType(Configs.ModeType.TYPE_Pressure)
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                            Log.i("CHECK_MODE_VAL", preferenceManager?.readModeType().toString())
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonAprv -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this select binding.buttonAprv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            this deSelect binding.buttonNCBpap

                            ventMode = MODE_PC_ARPV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure

                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonPcac -> {

                        if (optionSelected) {
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this select binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap


                            ventMode =
                                MODE_PC_AC
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()


                    }

                    binding.buttonPsv -> {

                        if (optionSelected) {

                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this select binding.buttonPsv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap

                            ventMode =
                                MODE_PC_PSV
                            VentilatorApp.globalModeType = ModeType.TYPE_Pressure
                        } else showToastForNonSelectOptions()

                    }

                    binding.buttonHFNC -> {

                        if (optionSelected) {

                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this select binding.buttonHFNC
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap
                            ventMode =
                                MODE_HFNC
                            VentilatorApp.globalModeType = ModeType.TYPE_HFNC

                        } else showToastForNonSelectOptions()


                    }

                    binding.buttonAIVent -> {

                        if (optionSelected) {

                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this select binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this deSelect binding.buttonCpap

                            ventMode =
                                MODE_AUTO_VENTILATION
                            VentilatorApp.globalModeType = ModeType.TYPE_AUTO_VENTILATION

                        } else showToastForNonSelectOptions()


                    }

                    binding.buttonBpap -> {

                        if (optionSelected) {

                            this select binding.buttonBpap
                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonHFNC
                            this deSelect binding.buttonAIVent
                            this select binding.buttonBpap
                            this deSelect binding.buttonCpap

                            VentilatorApp.globalModeType = ModeType.TYPE_NIV

                            ventMode =
                                MODE_NIV_BPAP

                        } else showToastForNonSelectOptions()


                    }

                    binding.buttonCpap -> {

                        if (optionSelected) {

                            this deSelect binding.buttonVcCmv
                            this deSelect binding.buttonVcSimv
                            this deSelect binding.buttonAcv
                            this deSelect binding.buttonPcCmv
                            this deSelect binding.buttonPcSimv
                            this deSelect binding.buttonPrvc
                            this deSelect binding.buttonPcac
                            this deSelect binding.buttonPsv
                            this deSelect binding.buttonAprv
                            this deSelect binding.buttonAIVent
                            this deSelect binding.buttonBpap
                            this select binding.buttonCpap
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
