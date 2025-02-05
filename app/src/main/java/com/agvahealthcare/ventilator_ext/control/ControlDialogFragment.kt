import android.annotation.SuppressLint
import android.os.Bundle

import android.os.CountDownTimer

import android.util.Log

import android.view.LayoutInflater

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment

import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.control.advanced.AdvancedFragment
import com.agvahealthcare.ventilator_ext.control.backup.BackupFragment
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterClickListener
import com.agvahealthcare.ventilator_ext.control.etcuff.EtCuffFragment
import com.agvahealthcare.ventilator_ext.control.patient.PatientFragment
import com.agvahealthcare.ventilator_ext.control.smartfio2.SmartFio2
import com.agvahealthcare.ventilator_ext.control.vtas.Vtas
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.standby.StandbyBackupFragment
import com.agvahealthcare.ventilator_ext.standby.StandbyBasicFragment
import com.agvahealthcare.ventilator_ext.standby.StandbyControlSettingFragment
import com.agvahealthcare.ventilator_ext.utility.CustomCountDownTimer
import com.agvahealthcare.ventilator_ext.utility.ToastFactory

import com.agvahealthcare.ventilator_ext.utility.replaceFragment

import com.agvahealthcare.ventilator_ext.utility.setHeightWidthPercent

import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_AND
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_MINUS
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PREFIX_PLUS

import com.github.angads25.toggle.interfaces.OnToggledListener
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_contol_dialog.*
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.buttonStartVent
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.focusLayoutStandbyControls
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.imageViewCrossStandbyControls
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyAdvanced
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyBackup
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyBasic
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbySmartFio2
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.includeButtonStandbyVTas
import kotlinx.android.synthetic.main.fragment_standbycontrol_dialog.mainViewPanelStandbyControls


//interface OnStartVentilationListener {
//    fun onModeConfirmed()
//    fun onSettingsConfirmed(parameters: List<ControlParameterModel>)
//}

class ControlDialogFragment : DialogFragment() {

    private var closeListener: OnDismissDialogListener? = null
    private var basicParameterClickListener: ControlParameterClickListener?=null
    private  var advancedFragment: StandbyControlSettingFragment?=null
    private  var smartFio2Fragment: StandbyControlSettingFragment?=null
    private  var vTasFragment: StandbyControlSettingFragment?=null
    private  var etCuffFragment: StandbyControlSettingFragment?=null
    private var advancedControlParams : List<ControlParameterModel>? = null
    private var vTasControlParams : List<ControlParameterModel>? = null
    private var etCuffControlParams : List<ControlParameterModel>? = null
    private var backupParameterClickListener: ControlParameterClickListener?=null
    private var smartFioParameterClickListener: ControlParameterClickListener?=null
    private var vTasParameterClickListener: ControlParameterClickListener?=null
    private var etCuffParameterClickListener: ControlParameterClickListener?=null
    private var onToggledListener: OnToggledListener?=null
    private var basicControlParams :List<ControlParameterModel>? = null
    private var backupControlParams :List<ControlParameterModel>? = null
    private var smartFio2ControlParams :List<ControlParameterModel>? = null
//    private var onStartVentilationListener: OnStartVentilationListener?=null
    private var patientFragment : PatientFragment? = null
    private var prefManager: PreferenceManager? = null
    private var currentMode: String? = null
    private var dialogModeConfirmation: AlertDialog? = null
    private var basicFragment : StandbyControlSettingFragment? = null
    private var advancedParameterClickListener : ControlParameterClickListener?=null
    private var backupFragment : StandbyControlSettingFragment? = null
    private var isStatus: Boolean? = null
    public var cancelableStatus: Boolean = false

    companion object {
        const val TAG = "ControlDialog"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"
        //private const val KEY_POSITION = "KEY_POSITION"

        fun newInstance(
            height: Int?,
            width: Int?,
         //   positionx : Int?,
            basicParams: List<ControlParameterModel>,
            advancedParams: List<ControlParameterModel>?,
            backupParams: List<ControlParameterModel>?,
            smartFio2Params: List<ControlParameterModel>?,
            vTasParams: List<ControlParameterModel>?,
            etCuffParams: List<ControlParameterModel>?,
            closeListener: OnDismissDialogListener?,
            basicParameterClickListener: ControlParameterClickListener?,
            advancedParameterClickListener: ControlParameterClickListener?,
            backupParameterClickListener: ControlParameterClickListener?,
            smartFio2ParameterClickListener: ControlParameterClickListener?,
            vTasParameterClickListener: ControlParameterClickListener?,
            etCuffParameterClickListener: ControlParameterClickListener?,
            onToggledListener: OnToggledListener?
//            onStartVentilationListener: OnStartVentilationListener?
        ): ControlDialogFragment {
            val args = Bundle()
            height?.let { args.putInt(KEY_HEIGHT, it) }
            width?.let { args.putInt(KEY_WIDTH, it) }
            val fragment = ControlDialogFragment()
            fragment.arguments = args
            fragment.closeListener = closeListener
            fragment.basicParameterClickListener = basicParameterClickListener
            fragment.advancedParameterClickListener = advancedParameterClickListener
            fragment.backupParameterClickListener = backupParameterClickListener
            fragment.smartFioParameterClickListener = smartFio2ParameterClickListener
            fragment.vTasParameterClickListener = vTasParameterClickListener
            fragment.etCuffParameterClickListener = etCuffParameterClickListener
            fragment.onToggledListener = onToggledListener
            fragment.basicControlParams = basicParams
            fragment.advancedControlParams = advancedParams
            fragment.backupControlParams = backupParams
            fragment.smartFio2ControlParams = smartFio2Params
            fragment.vTasControlParams = vTasParams
            fragment.etCuffControlParams = etCuffParams

//            fragment.onStartVentilationListener = onStartVentilationListener

            return fragment
        }
    }


    private var sizeOfCurrentArray = 0

    // knob highlight logic starts here

    private var highlightedIndex = -1
    private var visibilityTimeout: CountDownTimer? = null


    private fun highlightAdapters(highlightedIndex:Int){
        if (basicFragment != null) (basicFragment as StandbyBasicFragment).highlightAdapterPosition(highlightedIndex)
        else if (advancedFragment != null) (advancedFragment as AdvancedFragment).highlightAdapterPosition(highlightedIndex)
        else if (backupFragment != null) (backupFragment as StandbyBackupFragment).highlightAdapterPosition(highlightedIndex)
        else if (etCuffFragment != null) (etCuffFragment as EtCuffFragment).highlightAdapterPosition(highlightedIndex)
        else if (smartFio2Fragment != null) (smartFio2Fragment as SmartFio2).highlightAdapterPosition(highlightedIndex)
        else if (vTasFragment != null) (vTasFragment as Vtas).highlightAdapterPosition(highlightedIndex)
    }

    private fun handleAdaptersClick(highlightedIndex:Int){
        if (basicFragment != null) (basicFragment as StandbyBasicFragment).handleClick(highlightedIndex)
        else if (advancedFragment != null) (advancedFragment as AdvancedFragment).handleClick(highlightedIndex)
        else if (backupFragment != null) (backupFragment as StandbyBackupFragment).handleClick(highlightedIndex)
        else if (etCuffFragment != null) (etCuffFragment as EtCuffFragment).handleClick(highlightedIndex)
        else if (smartFio2Fragment != null) (smartFio2Fragment as SmartFio2).handleClick(highlightedIndex)
        else if (vTasFragment != null) (vTasFragment as Vtas).handleClick(highlightedIndex)
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
            constraintSet.clone(mainViewPanelControls)
            constraintSet.clear(focusLayoutControls.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutControls.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutControls.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutControls.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelControls)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelControls)
        constraintSet.connect(
            focusLayoutControls.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutControls.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutControls.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutControls.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelControls)
    }

    private fun getViewForFocus(isMinus: Boolean?): View? {
        
        return when (highlightedIndex) {

            in 0..sizeOfCurrentArray -> {
                null
            }

            sizeOfCurrentArray + 1 -> imageViewCrossControls
            sizeOfCurrentArray + 2 -> includeButtonBasic
            sizeOfCurrentArray + 3 -> {
                if (includeButtonAdvance.isVisible) {
                    includeButtonAdvance
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 4 -> {
                if (includeButtonApnea.isVisible) {
                    includeButtonApnea
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 5 -> {
                if (includeButtonsmartFiO2.isVisible) {
                    includeButtonsmartFiO2
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 6 -> {
                if (includeButtonVTas.isVisible) {
                    includeButtonVTas
                } else {
                    isMinus?.let {
                        if (isMinus) highlightedIndex-- else highlightedIndex++
                        getViewForFocus(isMinus)
                    }
                }
            }

            sizeOfCurrentArray + 7 -> includeButtonPatient

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

    ): View
    {
        return inflater.inflate(R.layout.fragment_contol_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
            val tagg= tag
            val extras=arguments?.getString("passing")
           Log.d("bundle",tagg.toString())
            if (tagg=="patient"){
                setUpPatient()
            } else if (tagg=="nonpatient"){
                setUpBasic()
            }

         
        setOnClickListener()

        includeButtonAdvance.visibility = if(advancedControlParams?.isEmpty() == true) View.GONE else View.VISIBLE
        includeButtonsmartFiO2.visibility = if (smartFio2ControlParams?.isEmpty() == true) View.GONE else View.VISIBLE
        includeButtonVTas.visibility = if (vTasControlParams?.isEmpty() ==true ) View.GONE else View.VISIBLE
        includeButtonEtCuff.visibility = if (etCuffControlParams?.isEmpty() ==true ) View.GONE else View.VISIBLE
        includeButtonApnea.visibility = if (backupControlParams == null || backupControlParams?.isEmpty() == true) View.GONE else View.VISIBLE
        
    }


    fun notifyParameterAdapter(){
        if (backupFragment?.isVisible==true){
            backupFragment?.notifyAdapter()
        }
        else if (advancedFragment?.isVisible == true){
            advancedFragment?.notifyAdapter()
        }else if (smartFio2Fragment?.isVisible == true){
            smartFio2Fragment?.notifyAdapter()
        }else if (vTasFragment?.isVisible == true){
            vTasFragment?.notifyAdapter()
        }else if (etCuffFragment?.isVisible == true){
            etCuffFragment?.notifyAdapter()
        }
        else {
            basicFragment?.notifyAdapter()
        }
    }

    private fun makeAllFragmentNull() {
        basicFragment = null
        advancedFragment = null
        backupFragment = null
        smartFio2Fragment = null
        etCuffFragment = null
        vTasFragment = null
    }


    // fun isBasicFragmentVisible() = basicFragment?.isVisible == true
   fun isAdvancedFragmentVisible() = advancedFragment?.isVisible == true
    fun isBackupFragmentVisible() = backupFragment?.isVisible == true

    //By Default Fragment

    private fun setUpBasic() {
        sizeOfCurrentArray = basicControlParams!!.size-1
        makeAllFragmentNull()
       // patientFragment = null
        basicControlParams?.let {
            Log.i("CONTROLPARAMCHECK", "BASIC SIZE = ${it.size}")
            if(basicFragment==null) basicFragment = StandbyBasicFragment(ArrayList(it), basicParameterClickListener)
            basicFragment?.apply {
                replaceFragment(this,this::class.java.javaClass.simpleName, R.id.control_nav_container )
            }
        }

        /*btn_update_settings.visibility = View.GONE*/
        includeButtonPatient.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonPatient.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonVTas.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonEtCuff.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonsmartFiO2.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonsmartFiO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonApnea.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonApnea.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonAdvance.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonAdvance.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonBasic.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
    }

    private fun setUpEtCuff() {
        sizeOfCurrentArray = etCuffControlParams!!.size
        makeAllFragmentNull()
       // patientFragment = null
        etCuffControlParams?.let {
            Log.i("CONTROLPARAMCHECK", "Etcuff SIZE = ${it.size}")
            if(etCuffFragment==null) etCuffFragment = EtCuffFragment(ArrayList(it), etCuffParameterClickListener)
            etCuffFragment?.apply {
                replaceFragment(this,this::class.java.javaClass.simpleName, R.id.control_nav_container )
            }
        }

        /*btn_update_settings.visibility = View.GONE*/
        includeButtonPatient.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonPatient.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonVTas.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonEtCuff.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonsmartFiO2.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonsmartFiO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonApnea.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonApnea.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonAdvance.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonAdvance.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonBasic.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonEtCuff.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)

         

    }


    private fun setUpBackup() {
        makeAllFragmentNull()
        sizeOfCurrentArray = backupControlParams!!.size
        if( backupControlParams?.isNotEmpty() == true){

            // patientFragment = null
            backupControlParams?.let {
                Log.i("CONTROLPARAMCHECK", "BACKUP SIZE = ${it.size}")
                Log.i("BACKUPCHECK", " STEP = ${it.get(0).step}, ${it.get(1).step}")
                if (backupFragment == null) backupFragment = StandbyBackupFragment(ArrayList(it), backupParameterClickListener,onToggledListener)

                backupFragment?.apply {
                    replaceFragment(
                        this,
                        this::class.java.javaClass.simpleName,
                        R.id.control_nav_container
                    )
                }
            }

            includeButtonPatient.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonPatient.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)


            includeButtonEtCuff.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            includeButtonEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

            includeButtonApnea.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonApnea.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)

            includeButtonBasic.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

            includeButtonAdvance.buttonView.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
            includeButtonAdvance.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

            includeButtonVTas.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            includeButtonVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
            includeButtonsmartFiO2.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            includeButtonsmartFiO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
             
        }

    }


    private fun setUpAdvanced() {
        makeAllFragmentNull()
        sizeOfCurrentArray = advancedControlParams!!.size
        if(advancedControlParams?.isNotEmpty() == true){
            advancedControlParams?.let {
                if(advancedFragment == null) advancedFragment = AdvancedFragment(ArrayList(it), advancedParameterClickListener)
                advancedFragment?.apply {
                    replaceFragment(
                        this,
                        this::class.java.javaClass.simpleName,
                        R.id.control_nav_container
                    )
                }
            }
        }


        includeButtonEtCuff.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonPatient.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonPatient.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonApnea.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonApnea.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonAdvance.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonAdvance.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
        includeButtonVTas.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonsmartFiO2.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonsmartFiO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonBasic.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

         
    }

    private fun setUpFiO2() {
        makeAllFragmentNull()
        sizeOfCurrentArray = smartFio2ControlParams!!.size
        if (smartFio2ControlParams?.isNotEmpty() == true) {
            smartFio2ControlParams?.let {
                if (smartFio2Fragment == null) smartFio2Fragment =
                    SmartFio2(ArrayList(it), smartFioParameterClickListener)
                smartFio2Fragment?.apply {
                    replaceFragment(
                        this,
                        this::class.java.javaClass.simpleName,
                        R.id.control_nav_container
                    )
                }
            }
        }

        includeButtonEtCuff.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonPatient.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonPatient.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonApnea.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonApnea.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonAdvance.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonAdvance.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonsmartFiO2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonsmartFiO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
        includeButtonVTas.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonBasic.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

         

    }

    private fun setUpVtas() {
        makeAllFragmentNull()
        sizeOfCurrentArray = vTasControlParams!!.size
        if (vTasControlParams?.isNotEmpty() == true) {

            Log.i("vTasEntry","enter vTas")
            vTasControlParams?.let {
                if (vTasFragment == null) vTasFragment =
                    Vtas(ArrayList(it), vTasParameterClickListener)
                vTasFragment?.apply {
                    replaceFragment(
                        this,
                        this::class.java.javaClass.simpleName,
                        R.id.control_nav_container
                    )
                }
            }
        }

        includeButtonEtCuff.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonPatient.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonPatient.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonApnea.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonApnea.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonAdvance.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonAdvance.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonsmartFiO2.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonsmartFiO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonVTas.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)

        includeButtonBasic.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
        includeButtonBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

         

    }



    // ClickListener on Buttons
    private fun setOnClickListener() {

        includeButtonBasic.buttonView.text = getString(R.string.hint_basic)
        includeButtonApnea.buttonView.text = getString(R.string.hint_apneasettings)
        includeButtonAdvance.buttonView.text = getString(R.string.hint_advancedsettings)
        includeButtonPatient.buttonView.text = getString(R.string.hint_patient)
        includeButtonVTas.buttonView.text = getString(R.string.hint_vtas_btn)
        includeButtonsmartFiO2.buttonView.text = getString(R.string.hint_smart_fio2_btn)
        includeButtonEtCuff.buttonView.text = getString(R.string.hint_etcuff_btn)

        imageViewCrossControls.setOnClickListener {

            requireActivity().supportFragmentManager
                .beginTransaction()
                .remove(this)
                .commitNow()
//            requireActivity().supportFragmentManager.popBackStack()

            closeListener?.handleDialogClose()

           /* closeListener?.handleDialogClose()
            dismiss()*/
        }
        includeButtonBasic.buttonView.setOnClickListener {
            setUpBasic()
        }
        includeButtonEtCuff.buttonView.setOnClickListener {
            setUpEtCuff()
        }


        includeButtonApnea.buttonView.setOnClickListener {
            setUpBackup()

        }
        includeButtonAdvance.buttonView.setOnClickListener {
            setUpAdvanced()
        }
        includeButtonsmartFiO2.buttonView.setOnClickListener{
            setUpFiO2()
        }
        includeButtonVTas.buttonView.setOnClickListener{
            setUpVtas()
        }

        includeButtonPatient.buttonView.setOnClickListener {
            setUpPatient()
        }

    }

    private fun setUpPatient() {
        makeAllFragmentNull()
        sizeOfCurrentArray = 0
        if(patientFragment==null)
        patientFragment = PatientFragment()
        patientFragment?.apply {
            replaceFragment(this,this::class.java.javaClass.simpleName, R.id.control_nav_container )
        }

        includeButtonBasic.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonBasic.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        includeButtonApnea.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonApnea.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonEtCuff.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonEtCuff.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        includeButtonPatient.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded_selected_green)
        includeButtonPatient.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

        includeButtonAdvance.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonAdvance.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonVTas.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonVTas.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonsmartFiO2.buttonView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        includeButtonsmartFiO2.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
         
    }

    override fun onStart() {
        super.onStart()
        val heightDialog = arguments?.getInt(KEY_HEIGHT)
        val widthDialog = arguments?.getInt(KEY_WIDTH)

        setHeightWidthPercent(heightDialog , widthDialog , true)
    }

    fun setTime(counterState: String, customCountDownTimer: CustomCountDownTimer) {
        patientFragment?.takeIf { it.isVisible }?.apply {
            setVentilationTime(counterState,customCountDownTimer)
        }

    }

}

