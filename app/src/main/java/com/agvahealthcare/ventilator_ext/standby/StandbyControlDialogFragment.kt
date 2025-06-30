package com.agvahealthcare.ventilator_ext.standby

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.OnStartVentilationListener
import com.agvahealthcare.ventilator_ext.control.advanced.AdvancedFragment
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterClickListener
import com.agvahealthcare.ventilator_ext.control.etcuff.EtCuffFragment
import com.agvahealthcare.ventilator_ext.control.smartfio2.SmartFio2
import com.agvahealthcare.ventilator_ext.control.vtas.Vtas
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.utility.hideSystemUI
import com.agvahealthcare.ventilator_ext.utility.replaceFragment
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_AND
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_MINUS
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_PLUS
import com.github.angads25.toggle.interfaces.OnToggledListener
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.*

class StandbyControlDialogFragment : DialogFragment() {

    private var mMainActivityViewModel: MainActivityViewModel? = null

    private var basicParameterClickListener: ControlParameterClickListener? = null
    private var backupParameterClickListener: ControlParameterClickListener? = null
    private var smartFio2ParameterClickListener: ControlParameterClickListener? = null
    private var VTasParameterClickListener: ControlParameterClickListener? = null
    private var etCuffParameterClickListener: ControlParameterClickListener? = null

    private var prefManager: PreferenceManager? = null

    private var advancedParameterClickListener: ControlParameterClickListener? = null
    private var closeListener: OnDismissDialogListener? = null
    private var modeCode = 0
    private var onStartVentilationListener: OnStartVentilationListener? = null
    private var dialogStartVentConfirmation: AlertDialog? = null

    private var standbyBasicFragment: StandbyControlSettingFragment? = null
    private var standbyAdvancedFragment: StandbyControlSettingFragment? = null
    private var standbyBackupFragment: StandbyBackupFragment? = null
    private var standbySmartFio2Fragment: StandbyControlSettingFragment? = null
    private var standbyVTasFragment: StandbyControlSettingFragment? = null
    private var standbyEtCuffFragment: StandbyControlSettingFragment? = null


//    private var visibilityTimeout: CountDownTimer? = null


    private var isStatus: Boolean? = null
    private var basicControlParams: MutableList<ControlParameterModel>? = null
    private var backupControlParams: MutableList<ControlParameterModel>? = null
    private var onToggledListener: OnToggledListener? = null
    private var advancedControlParams: MutableList<ControlParameterModel>? = null
    private var smartFio2Params: MutableList<ControlParameterModel>? = null
    private var vTasParams: MutableList<ControlParameterModel>? = null
    private var etCuffParams: MutableList<ControlParameterModel>? = null

    companion object {
        const val TAG = "StandbyControlDialog"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"
        private const val KEY_STATUS = "KEY_STATUS"

        fun newInstance(
            height: Int?,
            width: Int?,
            status: Boolean?,
            basicParams: MutableList<ControlParameterModel>,
            advancedParams: MutableList<ControlParameterModel>?,
            backupParams: MutableList<ControlParameterModel>?,
            smartFio2Params: MutableList<ControlParameterModel>?,
            vTasParams: MutableList<ControlParameterModel>?,
            etCuffParams: MutableList<ControlParameterModel>?,
            closeListener: OnDismissDialogListener?,
            basicParameterClickListener: ControlParameterClickListener?,
            advancedParameterClickListener: ControlParameterClickListener?,
            backupParameterClickListener: ControlParameterClickListener?,
            smartFio2ParameterClickListener: ControlParameterClickListener?,
            vTasParameterClickListener: ControlParameterClickListener?,
            etCuffParameterClickListener: ControlParameterClickListener?,
            onStartVentilationListener: OnStartVentilationListener? = null
        ): StandbyControlDialogFragment {
            val args = Bundle()
            height?.let { args.putInt(KEY_HEIGHT, it) }
            width?.let { args.putInt(KEY_WIDTH, it) }
            status?.let { args.putBoolean(KEY_STATUS, it) }

            val fragment = StandbyControlDialogFragment()
            fragment.arguments = args
            fragment.closeListener = closeListener
            fragment.basicParameterClickListener = basicParameterClickListener
            fragment.advancedParameterClickListener = advancedParameterClickListener
            fragment.smartFio2ParameterClickListener = smartFio2ParameterClickListener
            fragment.VTasParameterClickListener = vTasParameterClickListener
            fragment.etCuffParameterClickListener = etCuffParameterClickListener
            fragment.backupParameterClickListener = backupParameterClickListener
            fragment.onStartVentilationListener = onStartVentilationListener
            fragment.basicControlParams = basicParams
            fragment.advancedControlParams = advancedParams
            fragment.backupControlParams = backupParams
            fragment.smartFio2Params = smartFio2Params
            fragment.vTasParams = vTasParams
            fragment.etCuffParams = etCuffParams

            return fragment

        }

    }

    private var sizeOfCurrentArray = 0

    // knob highlight logic starts here

    private var highlightedIndex = -1
    private var visibilityTimeout: CountDownTimer? = null


    private fun highlightAdapters(highlightedIndex:Int){
        if (standbyBasicFragment != null) (standbyBasicFragment as StandbyBasicFragment).highlightAdapterPosition(highlightedIndex)
        else if (standbyAdvancedFragment != null) (standbyAdvancedFragment as AdvancedFragment).highlightAdapterPosition(highlightedIndex)
        else if (standbyBackupFragment != null) (standbyBackupFragment as StandbyBackupFragment).highlightAdapterPosition(highlightedIndex)
        else if (standbyEtCuffFragment != null) (standbyEtCuffFragment as EtCuffFragment).highlightAdapterPosition(highlightedIndex)
        else if (standbySmartFio2Fragment != null) (standbySmartFio2Fragment as SmartFio2).highlightAdapterPosition(highlightedIndex)
        else if (standbyVTasFragment != null) (standbyVTasFragment as Vtas).highlightAdapterPosition(highlightedIndex)
    }

    private fun handleAdaptersClick(highlightedIndex:Int){
        if (standbyBasicFragment != null) (standbyBasicFragment as StandbyBasicFragment).handleClick(highlightedIndex)
        else if (standbyAdvancedFragment != null) (standbyAdvancedFragment as AdvancedFragment).handleClick(highlightedIndex)
        else if (standbyBackupFragment != null) (standbyBackupFragment as StandbyBackupFragment).handleClick(highlightedIndex)
        else if (standbyEtCuffFragment != null) (standbyEtCuffFragment as EtCuffFragment).handleClick(highlightedIndex)
        else if (standbySmartFio2Fragment != null) (standbySmartFio2Fragment as SmartFio2).handleClick(highlightedIndex)
        else if (standbyVTasFragment != null) (standbyVTasFragment as Vtas).handleClick(highlightedIndex)
    }



    @SuppressLint("NotifyDataSetChanged")
    fun highlightViewWithFocus(data: String) {


        Log.i("value_check_bonds", "index : $highlightedIndex ,size : $sizeOfCurrentArray")

        clearPreviousConstraints()
        startTimeoutWithDebounce()

        when (data) {
            PREFIX_PLUS -> {
                if (highlightedIndex < (sizeOfCurrentArray + 7)) highlightedIndex++
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
                    highlightedIndex = (sizeOfCurrentArray + 7)
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
                    if (highlightedIndex == sizeOfCurrentArray+1 || highlightedIndex == sizeOfCurrentArray+7){
                        it.callOnClick()
                    }else{
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

    private fun clearPreviousConstraints() {
        try {
            val constraintSet = ConstraintSet()
            constraintSet.clone(mainViewPanelStandbyControls)
            constraintSet.clear(focusLayoutStandbyControls.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutStandbyControls.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutStandbyControls.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutStandbyControls.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelStandbyControls)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelStandbyControls)
        constraintSet.connect(
            focusLayoutStandbyControls.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutStandbyControls.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutStandbyControls.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutStandbyControls.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelStandbyControls)
    }

    private fun getViewForFocus(isMinus: Boolean?): View? {


        return when (highlightedIndex) {

            in 0..sizeOfCurrentArray -> {
                null
            }

            sizeOfCurrentArray + 1 -> imageViewCrossStandbyControls
            sizeOfCurrentArray + 2 -> includeButtonStandbyBasic
            sizeOfCurrentArray + 3 -> {
                if (includeButtonStandbyAdvanced.isVisible) {
                    includeButtonStandbyAdvanced
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 4 -> {
                if (includeButtonStandbyBackup.isVisible) {
                    includeButtonStandbyBackup
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 5 -> {
                if (includeButtonStandbySmartFio2.isVisible) {
                    includeButtonStandbySmartFio2
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 6 -> {
                if (includeButtonStandbyVTas.isVisible) {
                    includeButtonStandbyVTas
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 7 -> buttonStartVent

            else -> null
        }
    }

    fun startTimeoutWithDebounce() {

        cancelTimeout()

        visibilityTimeout = object : CountDownTimer(10000, 2000) {
            override fun onTick(millisUntilFinished: Long) {
            }

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
        return inflater.inflate(R.layout.fragment_standbycontrol_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        Log.i("STANDBYBACKUPBUTTON", includeButtonStandbyBackup.visibility.toString())

        mMainActivityViewModel =
            ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]

        prefManager = PreferenceManager(context)
        includeButtonStandbyBackup.visibility =
            if (backupControlParams?.isNotEmpty() == true) View.VISIBLE else View.GONE
        includeButtonStandbyAdvanced.visibility =
            if (advancedControlParams?.isNotEmpty() == true) View.VISIBLE else View.GONE
        includeButtonStandbySmartFio2.visibility =
            if (smartFio2Params?.isNotEmpty() == true) View.VISIBLE else View.GONE
        includeButtonStandbyVTas.visibility =
            if (vTasParams?.isNotEmpty() == true) View.VISIBLE else View.GONE
        includeButtonStandbyEtCuff.visibility =
            if (etCuffParams?.isNotEmpty() == true) View.VISIBLE else View.GONE
        includeButtonStandbyBasic.buttonView.text = getString(R.string.hint_basic)
        includeButtonStandbyBackup.buttonView.text = getString(R.string.hint_backupsettings)
        includeButtonStandbyAdvanced.buttonView.text = getString(R.string.hint_advancedsettings)
        includeButtonStandbySmartFio2.buttonView.text = getString(R.string.hint_smart_fio2_btn)
        includeButtonStandbyVTas.buttonView.text = getString(R.string.hint_vtas_btn)
        includeButtonStandbyEtCuff.buttonView.text = getString(R.string.hint_etcuff_btn)
        setUpStandbyBasic()
        setOnClickListener()
        modebutton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        checkMode()
    }

    private fun makeAllFragmentNull() {
        standbyBasicFragment = null
        standbyAdvancedFragment = null
        standbyBackupFragment = null
        standbyVTasFragment = null
        standbyEtCuffFragment = null
        standbySmartFio2Fragment = null
    }

    override fun onStart() {
        super.onStart()
        val heightDialog = arguments?.getInt(StandbyControlDialogFragment.KEY_HEIGHT)
        val widthDialog = arguments?.getInt(StandbyControlDialogFragment.KEY_WIDTH)
        isStatus = arguments?.getBoolean(StandbyControlDialogFragment.KEY_STATUS)
        setHeightWidth(heightDialog, widthDialog, isStatus)
    }

    private fun checkMode() {
        if (tag == "FromDashBoardActivity") {
            modeCode = (activity as DashBoardActivity).modeCode
        } else {
            modeCode = (activity as MainActivity).requestedModeCode
        }

        when (modeCode) {
            Configs.MODE_VCV_CMV -> {
                modebutton.text = getString(R.string.hint_vc_cmv)
            }

            Configs.MODE_VCV_ACV -> {
                modebutton.text = getString(R.string.hint_vc_cv)
            }

            Configs.MODE_VCV_SIMV -> {
                modebutton.text = getString(R.string.hint_vc_simv)
            }

            Configs.MODE_PC_CMV -> {
                modebutton.text = getString(R.string.hint_pc_cmv)
            }

            Configs.MODE_PC_SIMV -> {
                modebutton.text = getString(R.string.hint_pc_simv)
            }

            Configs.MODE_PC_AC -> {
                modebutton.text = getString(R.string.hint_spont)
            }

            Configs.MODE_PC_PSV -> {
                modebutton.text = getString(R.string.hint_psv)
            }

            Configs.MODE_PC_PRVC -> {
                modebutton.text = getString(R.string.hint_prvc)
            }

            Configs.MODE_PC_ARPV -> {
                modebutton.text = getString(R.string.hint_pc_aprv)
            }

            Configs.MODE_HFNC -> {
                if (prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT) modebutton.text =
                    getString(R.string.NeoNatehfnc) else modebutton.text = getString(R.string.hfnc)
            }

            Configs.MODE_AUTO_VENTILATION -> {
                modebutton.text = getString(R.string.hint_ai_vent)
            }

            Configs.MODE_NIV_BPAP -> {
                if (prefManager?.readCurrentUid() != Configs.PatientProfile.TYPE_NEONAT)
                    modebutton.text = getString(R.string.hint_bpap)
                else modebutton.text = getString(R.string.hint_nbpap)
            }

            Configs.MODE_NC_IPPV -> {
                if (prefManager?.readCurrentUid() != Configs.PatientProfile.TYPE_NEONAT)
                    modebutton.text = getString(R.string.hint_nc_cpap)
                else modebutton.text = getString(R.string.hint_nc_cpap)
            }

            Configs.MODE_NIV_CPAP -> {
                if (prefManager?.readCurrentUid() != Configs.PatientProfile.TYPE_NEONAT)
                    modebutton.text = getString(R.string.hint_cpap)
                else
                    modebutton.text = getString(R.string.hint_ncpap)
            }

            Configs.MODE_NC_CPAP -> {
                if (prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT) modebutton.text =
                    getString(R.string.hint_ncpap)

            }
        }

    }

    private fun setUpStandbyBasic() {

        sizeOfCurrentArray = basicControlParams!!.size-1

        makeAllFragmentNull()
        basicControlParams?.let {
            if (standbyBasicFragment == null) standbyBasicFragment = StandbyBasicFragment(ArrayList(it), basicParameterClickListener)

            standbyBasicFragment?.apply {
                replaceFragment(
                    this,
                    this::class.java.javaClass.simpleName,
                    R.id.standbycontrol_nav_container
                )
            }
        }

        includeButtonStandbyAdvanced.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyAdvanced.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonStandbyBackup.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonStandbyEtCuff.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)



        includeButtonStandbySmartFio2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )


        includeButtonStandbySmartFio2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbyVTas.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )


        includeButtonStandbyVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)



        includeButtonStandbyBackup.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbyBasic.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
    }

    private fun setUpAdvanced() {

        sizeOfCurrentArray = advancedControlParams!!.size
        makeAllFragmentNull()
        if (advancedControlParams?.isNotEmpty() == true) {

            advancedControlParams?.let {
                if (standbyAdvancedFragment == null) standbyAdvancedFragment =
                    AdvancedFragment(ArrayList(it), advancedParameterClickListener)
                standbyAdvancedFragment?.apply {
                    replaceFragment(
                        this,
                        this::class.java.javaClass.simpleName,
                        R.id.standbycontrol_nav_container
                    )
                }
            }
        }

        includeButtonStandbyBasic.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonStandbyBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonStandbyBackup.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonStandbyEtCuff.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbyBackup.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonStandbyAdvanced.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyAdvanced.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)

        includeButtonStandbySmartFio2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )


        includeButtonStandbySmartFio2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbyVTas.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonStandbyVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
    }

    private fun setUpEtCuff() {

        sizeOfCurrentArray = etCuffParams!!.size
        makeAllFragmentNull()
        if (etCuffParams?.isNotEmpty() == true) {

            etCuffParams?.let {
                if (standbyEtCuffFragment == null) standbyEtCuffFragment =
                    EtCuffFragment(ArrayList(it), etCuffParameterClickListener)
                standbyEtCuffFragment?.apply {
                    replaceFragment(
                        this,
                        this::class.java.javaClass.simpleName,
                        R.id.standbycontrol_nav_container
                    )
                }
            }
        }

        includeButtonStandbyBasic.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonStandbyBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonStandbyBackup.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonStandbyAdvanced.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyAdvanced.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbyBackup.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonStandbyEtCuff.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)

        includeButtonStandbySmartFio2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonStandbySmartFio2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbyVTas.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )


        includeButtonStandbyVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
    }

    private fun setUpBackup() {

        sizeOfCurrentArray = backupControlParams!!.size
        makeAllFragmentNull()
        if (backupControlParams?.isNotEmpty() == true) {            // patientFragment = null
            backupControlParams?.let {
                if (standbyBackupFragment == null) standbyBackupFragment =
                    StandbyBackupFragment(
                        ArrayList(it),
                        backupParameterClickListener,
                        onToggledListener
                    )
                standbyBackupFragment?.apply {
                    replaceFragment(
                        this,
                        this::class.java.javaClass.simpleName,
                        R.id.standbycontrol_nav_container
                    )
                }
            }

            /*btn_update_settings.visibility = View.GONE*/
            includeButtonStandbyBackup.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonStandbyBackup.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)

            includeButtonStandbyEtCuff.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonStandbyEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

            includeButtonStandbyBasic.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonStandbyBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

            includeButtonStandbySmartFio2.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonStandbySmartFio2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

            includeButtonStandbyVTas.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )


            includeButtonStandbyVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)


            includeButtonStandbyAdvanced.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonStandbyAdvanced.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        }
    }

    // change here 8 feb
    private fun setUpFiO2() {

        sizeOfCurrentArray = smartFio2Params!!.size
        makeAllFragmentNull()
        if (smartFio2Params?.isNotEmpty() == true) {
            smartFio2Params?.let {
                if (standbySmartFio2Fragment == null) standbySmartFio2Fragment =
                    SmartFio2(ArrayList(it), smartFio2ParameterClickListener)
                standbySmartFio2Fragment?.apply {
                    replaceFragment(
                        this,
                        this::class.java.javaClass.simpleName,
                        R.id.standbycontrol_nav_container
                    )
                }
            }
        }

        includeButtonStandbyEtCuff.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)


        includeButtonStandbyBackup.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyBackup.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbyAdvanced.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyAdvanced.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbySmartFio2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbySmartFio2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
        includeButtonStandbyVTas.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbyBasic.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

    }

    // change here 8 feb
    private fun setUpVtas() {

        sizeOfCurrentArray = vTasParams!!.size
        makeAllFragmentNull()
        if (vTasParams?.isNotEmpty() == true) {

            Log.i("vTasEntry", "enter vTas")
            vTasParams?.let {
                if (standbyVTasFragment == null) standbyVTasFragment =
                    Vtas(ArrayList(it), VTasParameterClickListener)
                standbyVTasFragment?.apply {
                    replaceFragment(
                        this,
                        this::class.java.javaClass.simpleName,
                        R.id.standbycontrol_nav_container
                    )
                }
            }
        }

        includeButtonStandbyBackup.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyBackup.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbyEtCuff.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbyAdvanced.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonStandbyAdvanced.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonStandbySmartFio2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonStandbySmartFio2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonStandbyVTas.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonStandbyVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)

        includeButtonStandbyBasic.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        includeButtonStandbyBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

    }

    private fun setOnClickListener() {

        imageViewCrossStandbyControls.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitNow()
            closeListener?.handleDialogClose()
        }

        includeButtonStandbyBasic.buttonView.setOnClickListener {
            setUpStandbyBasic()
        }

        includeButtonStandbyBackup.buttonView.setOnClickListener {
            setUpBackup()
        }

        includeButtonStandbyEtCuff.buttonView.setOnClickListener {
            setUpEtCuff()
        }

        includeButtonStandbyAdvanced.buttonView.setOnClickListener {
            prefManager?.apply {
                if (readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT && readVGVStatus() == true) {
                    Log.i("additionOf", (readPEEP() + readPplat()).toString())
                    mMainActivityViewModel?.twoTileResponse?.postValue((readPEEP() + readPplat()).toInt())
                }
            }
            setUpAdvanced()
        }

        includeButtonStandbySmartFio2.buttonView.setOnClickListener {
            setUpFiO2()
        }

        includeButtonStandbyVTas.buttonView.setOnClickListener {
            setUpVtas()
        }

        buttonStartVent.setOnClickListener {
            // send mode code to ventilator
            Log.i("new_ventilation", "buttonStartVent click in standby control dialog")
            onStartVentilationListener?.onStart()
        }
    }

    fun getApneaStatus() = standbyBackupFragment?.getApneaToggleStatus()

    // change here 8 feb
    fun getAllControlParameters(): ArrayList<ControlParameterModel> {

        val basicSettings = standbyBasicFragment?.getControlParameters()
        val advancedSettings = standbyAdvancedFragment?.getControlParameters()
        val backupSettings = standbyBackupFragment?.getControlParameters()
        val smartFio2Settings = standbySmartFio2Fragment?.getControlParameters()
        val vTasSettings = standbyVTasFragment?.getControlParameters()
        val etCuffSettings = standbyEtCuffFragment?.getControlParameters()

        val allSettings = arrayListOf<ControlParameterModel>()

        basicSettings?.let { allSettings.addAll(it) }
        advancedSettings?.let { allSettings.addAll(it) }
        backupSettings?.let { allSettings.addAll(it) }
        smartFio2Settings?.let { allSettings.addAll(it) }
        vTasSettings?.let { allSettings.addAll(it) }
        etCuffSettings?.let { allSettings.addAll(it) }

        return allSettings
    }

    // change here 8 feb
    fun updateParamVal(
        lbl: String,
        value: String,
        type: Configs.ControlSettingType
    ): ControlParameterModel? {
        val settingsFragment = when (type) {
            Configs.ControlSettingType.BASIC -> standbyBasicFragment
            Configs.ControlSettingType.BACKUP -> standbyBackupFragment
            Configs.ControlSettingType.ADVANCED -> standbyAdvancedFragment
            Configs.ControlSettingType.SmartFio2 -> standbySmartFio2Fragment
            Configs.ControlSettingType.VTas -> standbyVTasFragment
            Configs.ControlSettingType.EtCuff -> standbyEtCuffFragment

        }
        return updateParamVal(settingsFragment, lbl, value)
    }

    private fun updateParamVal(
        controlSettingFragment: StandbyControlSettingFragment?,
        lbl: String,
        value: String
    ): ControlParameterModel? {
        var paramModel: ControlParameterModel? = null
        controlSettingFragment?.apply {
            val parameters = this.getControlParameters()
            paramModel = parameters?.filter { it.ventKey == lbl }?.getOrNull(0)

            paramModel?.let { model ->
                // not to update if the value is same
                if (value == model.reading) {
                    return model
                }
                model.reading = Configs.supportPrecision(lbl, value)

                parameters?.let {
                    if (it.contains(model)) {
                        it[it.indexOf(model)] = model
                        // update list view item wise
                        this.notifyAdapter()
                    } else Log.i("ADAPTER", "Unable to update")
                }
            }

        }

        return paramModel
    }


    fun updateBasicParameterValue(lbl: String, value: String): ControlParameterModel? =
        updateParamVal(standbyBasicFragment, lbl, value)

    fun updateAdvancedParameterValue(lbl: String, value: String): ControlParameterModel? =
        updateParamVal(standbyAdvancedFragment, lbl, value)

    fun updateBackupParameterValue(lbl: String, value: String): ControlParameterModel? =
        updateParamVal(standbyBackupFragment, lbl, value)

    // change here 8 feb
    fun updateSmartFio2ParameterValue(lbl: String, value: String): ControlParameterModel? =
        updateParamVal(standbySmartFio2Fragment, lbl, value)

    fun updateVTasParameterValue(lbl: String, value: String): ControlParameterModel? =
        updateParamVal(standbyVTasFragment, lbl, value)

    fun updateEtCuffParameterValue(lbl: String, value: String): ControlParameterModel? =
        updateParamVal(standbyEtCuffFragment, lbl, value)


    // change here 8 feb
    fun notifyItemParameterAdapter(position: Int) {

        if (standbyBasicFragment?.isVisible == true) {
            standbyBasicFragment?.notifyItemChangedAdapter(position)
        }
        if (standbyBackupFragment?.isVisible == true) {
            standbyBackupFragment?.notifyItemChangedAdapter(position)
        }
        if (standbyAdvancedFragment?.isVisible == true) {
            standbyAdvancedFragment?.notifyItemChangedAdapter(position)
        }

        if (standbySmartFio2Fragment?.isVisible == true) standbySmartFio2Fragment?.notifyItemChangedAdapter(
            position
        )
        if (standbyVTasFragment?.isVisible == true) standbyVTasFragment?.notifyItemChangedAdapter(
            position
        )
        if (standbyEtCuffFragment?.isVisible == true) standbyEtCuffFragment?.notifyItemChangedAdapter(
            position
        )

    }

    // change here 8 feb
    fun notifyParameterAdapter() {
        if (standbyBasicFragment?.isVisible == true) {
            standbyBasicFragment?.notifyAdapter()
        }

        if (standbyBackupFragment?.isVisible == true) {
            standbyBackupFragment?.notifyAdapter()
        }

        if (standbyAdvancedFragment?.isVisible == true)
            standbyAdvancedFragment?.notifyAdapter()

        if (standbySmartFio2Fragment?.isVisible == true) standbySmartFio2Fragment?.notifyAdapter()

        if (standbyVTasFragment?.isVisible == true) standbyVTasFragment?.notifyAdapter()
        if (standbyEtCuffFragment?.isVisible == true) standbyEtCuffFragment?.notifyAdapter()
    }


    public fun getStartVentConfirmation() = dialogStartVentConfirmation


    fun StandbyControlDialogFragment.setHeightWidth(
        heightDialog: Int?,
        widthDialog: Int?,
        status: Boolean?
    ) {
        dialog?.window?.apply {
            if (status == true) {
                setGravity(Gravity.CENTER_HORIZONTAL)
                decorView.apply {
                    val params: WindowManager.LayoutParams = attributes
                    params.x = -58
                    params.y = -15
                    params.dimAmount = 0.0F
                    params.screenBrightness = 5.0F
                    params.width = widthDialog!! + 30
                    params.height = heightDialog!! + 30
                    attributes = params
                }
            } else {
                setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
                decorView.apply {
                    val params: WindowManager.LayoutParams = attributes
                    params.dimAmount = 0.0F
                    params.screenBrightness = 1.0F
                    params.width = widthDialog!! + 20
                    params.height = heightDialog!! + 20
                    attributes = params
                }
            }
        }
        hideSystemUI()
    }
}




