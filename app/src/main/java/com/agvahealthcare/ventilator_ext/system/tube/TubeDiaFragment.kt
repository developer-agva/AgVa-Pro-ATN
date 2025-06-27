package com.agvahealthcare.ventilator_ext.system.tube

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.system.configuration.VentilatorType
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.PatientProfile
import kotlinx.android.synthetic.main.activity_main.buttonAdult
import kotlinx.android.synthetic.main.activity_main.buttonNeonatal
import kotlinx.android.synthetic.main.activity_main.buttonPediatric
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_tube_dia.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TubeDiaFragment(private var communicationService: CommunicationService?) : Fragment(),
    View.OnClickListener {

    companion object {
        const val TAG = "TubeDiaFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_tube_dia, container, false)
    }

    private var prefManager: PreferenceManager? = null
    private lateinit var mEventViewModel: EventViewModel
    private var currentTag: String = "";
    private var tubeDiameter: String? = null

    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> if (topBarTube.isVisible) backBtnTube.callOnClick() else includeButtonCompliance.buttonView.callOnClick()

            1 -> if (topBarTube.isVisible) includeButtonAdultProfile.buttonView.callOnClick() else includeButtonResistance.buttonView.callOnClick()

            2 -> includeButtonPediatricProfile.buttonView.callOnClick()
            3 -> includeButtonNeonatalProfile.buttonView.callOnClick()
            4 -> includetubeAdult.buttonView.callOnClick()
            5 -> includetubePediatric.buttonView.callOnClick()
            6 -> includetubeNeoNate.buttonView.callOnClick()
            7 -> includeButtoncmdSend.buttonView.callOnClick()
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
            constraintSet.clone(mainViewPanelTube)
            constraintSet.clear(focusLayoutTube.id, ConstraintSet.TOP)
            constraintSet.clear(focusLayoutTube.id, ConstraintSet.BOTTOM)
            constraintSet.clear(focusLayoutTube.id, ConstraintSet.LEFT)
            constraintSet.clear(focusLayoutTube.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(mainViewPanelTube)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainViewPanelTube)
        constraintSet.connect(
            focusLayoutTube.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            focusLayoutTube.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            focusLayoutTube.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            focusLayoutTube.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(mainViewPanelTube)
    }

    //
    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {

            return when (highlightedIndex) {

                0 -> if (topBarTube.isVisible) backBtnTube else includeButtonCompliance
                1 -> if (topBarTube.isVisible) includeButtonAdultProfile else includeButtonResistance
                2 -> includeButtonPediatricProfile
                3 -> includeButtonNeonatalProfile
                4 -> includetubeAdult
                5 -> includetubePediatric
                6 -> includetubeNeoNate
                7 -> includeButtoncmdSend

                else -> null
            }
        } ?: run {
            return null
        }
    }
    // logic knob highlight ends here


    private fun hideGoneFunction(isCalib: Boolean) {

        if (isCalib) {
            // pre op check layout
            topBarTube.visibility = View.GONE
            backBtnTube.visibility = View.GONE
            tvMainTitleTube.visibility = View.GONE
            includeButtonAdultProfile.visibility = View.GONE
            includeButtonPediatricProfile.visibility = View.GONE
            includeButtonNeonatalProfile.visibility = View.GONE
            tvtubelength.visibility = View.GONE
            tvtextHeadingTube.visibility = View.GONE
            tvtext1Tube.visibility = View.GONE
            tvtext2Tube.visibility = View.GONE
            tvtext3Tube.visibility = View.GONE
            ventigifCompliance.visibility = View.GONE
            ventigifResistance.visibility = View.GONE
            includeButtoncmdSend.visibility = View.GONE

            // calib layouts
            tubeText.visibility = View.VISIBLE
            includeButtonCompliance.visibility = View.VISIBLE
            includeButtonResistance.visibility = View.VISIBLE
            tvCompensation.visibility = View.VISIBLE
            tvResistance.visibility = View.VISIBLE
            tvCompensationDate.visibility = View.VISIBLE
            tvResistanceDate.visibility = View.VISIBLE
            ivCompensationStatus.visibility = View.VISIBLE
            ivResistanceStatus.visibility = View.VISIBLE

        } else {
            // pre op check layout
            topBarTube.visibility = View.VISIBLE
            backBtnTube.visibility = View.VISIBLE
            tvMainTitleTube.visibility = View.VISIBLE

            if (prefManager?.readVentilatorType() == VentilatorType.ONLY_NEO) {
                includeButtonAdultProfile.visibility = View.GONE
                includeButtonPediatricProfile.visibility = View.GONE
                includeButtonNeonatalProfile.visibility = View.VISIBLE
            } else if (prefManager?.readVentilatorType() == VentilatorType.ATP) {
                includeButtonAdultProfile.visibility = View.VISIBLE
                includeButtonPediatricProfile.visibility = View.VISIBLE
                includeButtonNeonatalProfile.visibility = View.GONE
            } else {
                includeButtonAdultProfile.visibility = View.VISIBLE
                includeButtonPediatricProfile.visibility = View.VISIBLE
                includeButtonNeonatalProfile.visibility = View.VISIBLE
            }
            tvtubelength.visibility = View.VISIBLE
            tvtextHeadingTube.visibility = View.VISIBLE
            tvtext1Tube.visibility = View.VISIBLE
            tvtext2Tube.visibility = View.VISIBLE
            tvtext3Tube.visibility = View.VISIBLE
            includeButtoncmdSend.visibility = View.VISIBLE

            // calib layouts
            tubeText.visibility = View.GONE
            includeButtonCompliance.visibility = View.GONE
            includeButtonResistance.visibility = View.GONE
            tvCompensation.visibility = View.GONE
            tvResistance.visibility = View.GONE
            tvCompensationDate.visibility = View.GONE
            tvResistanceDate.visibility = View.GONE
            ivCompensationStatus.visibility = View.GONE
            ivResistanceStatus.visibility = View.GONE
        }
    }

    private fun handleUIChanges(){
        prefManager?.apply {
            if (readVentilatorType() == VentilatorType.ONLY_NEO) {
                includeButtonNeonatalProfile.visibility = View.VISIBLE
                includeButtonAdultProfile.visibility = View.GONE
                includeButtonPediatricProfile.visibility = View.GONE

            } else if (readVentilatorType() == VentilatorType.ATP) {
                includeButtonNeonatalProfile.visibility = View.GONE
                includeButtonAdultProfile.visibility = View.VISIBLE
                includeButtonPediatricProfile.visibility = View.VISIBLE

            } else {
                includeButtonNeonatalProfile.visibility = View.VISIBLE
                includeButtonAdultProfile.visibility = View.VISIBLE
                includeButtonPediatricProfile.visibility = View.VISIBLE

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())
        mEventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)

        // handle UI changes based on the type
        handleUIChanges()

        setUpOnClickListener()
        setUpView()
        updateTubeCalibrationStatusUI()

        hideGoneFunction(true)
    }

    private fun setUpView() {
        includeButtonCompliance.buttonView.text = getString(R.string.hint_compensation)
        includeButtonResistance.buttonView.text = getString(R.string.hint_resistance)
        includeButtonAdultProfile.buttonView.text = getString(R.string.hint_adult)
        includeButtonNeonatalProfile.buttonView.text = getString(R.string.hint_neonatal)
        includeButtonPediatricProfile.buttonView.text = getString(R.string.hint_ped)

        includetubeAdult.buttonView.text = "22 mm"
        includetubePediatric.buttonView.text = "15 mm"
        includetubeNeoNate.buttonView.text = "10 mm"

        includeButtoncmdSend.buttonView.text = "START CALIBRATION"

        includeButtoncmdSend.buttonView.setBackgroundColor(R.drawable.background_black_border_white)
        includeButtoncmdSend.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        backBtnTube.setOnClickListener{
            (parentFragment as SystemDialogFragment).highlightedIndex = -1
            (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
            hideGoneFunction(true)
            includetubeAdult.visibility = View.GONE
            includetubePediatric.visibility = View.GONE
            includetubeNeoNate.visibility = View.GONE
        }
    }

    private fun setUpOnClickListener() {
        if (tag == "FromDashboard") {
            includeButtonCompliance.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }
            includeButtonResistance.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }
        } else {
            includeButtonCompliance.buttonView.setOnClickListener(this)
            includeButtonResistance.buttonView.setOnClickListener(this)
        }

        includeButtonNeonatalProfile.buttonView.setOnClickListener {
            if (prefManager?.readCurrentUid() != Configs.PatientProfile.TYPE_NEONAT) {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to neonate profile"
                )
            } else {
                includeButtonNeonatalProfile.buttonView.setBackgroundResource(R.color.racing_green)
                includeButtonNeonatalProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                includeButtonAdultProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includeButtonAdultProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                includeButtonPediatricProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includeButtonPediatricProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

                includetubeAdult.visibility = View.VISIBLE
                includetubePediatric.visibility = View.VISIBLE
                includetubeNeoNate.visibility = View.VISIBLE

                includetubeNeoNate.buttonView.callOnClick()
            }
        }

        includeButtonAdultProfile.buttonView.setOnClickListener {

            if (prefManager?.readCurrentUid() != Configs.PatientProfile.TYPE_ADULT) {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to adult profile"
                )
            }else {
                includeButtonAdultProfile.buttonView.setBackgroundResource(R.color.racing_green)
                includeButtonAdultProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                includeButtonPediatricProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includeButtonPediatricProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

                includeButtonNeonatalProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includeButtonNeonatalProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

                includetubeAdult.visibility = View.VISIBLE
                includetubePediatric.visibility = View.VISIBLE
                includetubeNeoNate.visibility = View.VISIBLE

                includetubeAdult.buttonView.callOnClick()
            }
        }

        includeButtonPediatricProfile.buttonView.setOnClickListener {

            if (prefManager?.readCurrentUid() != Configs.PatientProfile.TYPE_PED) {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to pediatric profile"
                )
            }else {
                includeButtonPediatricProfile.buttonView.setBackgroundResource(R.color.racing_green)
                includeButtonPediatricProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                includeButtonAdultProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includeButtonAdultProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

                includeButtonNeonatalProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includeButtonNeonatalProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

                includetubeAdult.visibility = View.VISIBLE
                includetubePediatric.visibility = View.VISIBLE
                includetubeNeoNate.visibility = View.VISIBLE

                includetubePediatric.buttonView.callOnClick()
            }
        }

        includetubePediatric.buttonView.setOnClickListener(this)
        includetubeAdult.buttonView.setOnClickListener(this)
        includetubeNeoNate.buttonView.setOnClickListener(this)
        includeButtoncmdSend.buttonView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v) {

            includeButtonCompliance.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 7
                currentTag = "Compliance"
                hideGoneFunction(false)
                highlightProfileButton()
                ventigifCompliance.visibility = View.VISIBLE
                ventigifResistance.visibility = View.GONE
                tvMainTitleTube.text = "Compliance pre-calibration check"
                tvtext1Tube.text = "1. Make sure the ventilator is connected to mains supply"
                tvtext2Tube.text = "2. Ensure the patient is not connected to the ventilator"
                tvtext3Tube.text = "3. Block the patient end of the breathing circuit using your thumb"
            }

            includeButtonResistance.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 7
                currentTag = "Resistance"
                hideGoneFunction(false)
                highlightProfileButton()
                ventigifCompliance.visibility = View.GONE
                ventigifResistance.visibility = View.VISIBLE
                tvMainTitleTube.text = "Resistance pre-calibration check"
                tvtext1Tube.text = "1. Make sure the ventilator is connected to mains supply"
                tvtext2Tube.text = "2. Ensure the patient is not connected to the ventilator"
                tvtext3Tube.text = "3. Keep the breathing circuit open"
            }

            includetubeAdult.buttonView -> {
                tubeDiameter = "22 mm"
                includetubeAdult.buttonView.setBackgroundResource(R.color.racing_green)
                includetubeAdult.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                includetubePediatric.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includetubePediatric.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                includetubeNeoNate.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includetubeNeoNate.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

            }

            includetubePediatric.buttonView -> {
                tubeDiameter = "15 mm"
                includetubePediatric.buttonView.setBackgroundResource(R.color.racing_green)
                includetubePediatric.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                includetubeAdult.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includetubeAdult.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                includetubeNeoNate.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includetubeNeoNate.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }

            includetubeNeoNate.buttonView -> {
                tubeDiameter = "10 mm"
                includetubeNeoNate.buttonView.setBackgroundResource(R.color.racing_green)
                includetubeNeoNate.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                includetubePediatric.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includetubePediatric.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                includetubeAdult.buttonView.setBackgroundResource(R.color.dolphin_grey)
                includetubeAdult.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

            }

            includeButtoncmdSend.buttonView -> {
                when (currentTag) {
                    "Compliance" -> {
                        communicationService?.takeIf { it.isPortsConnected }?.apply {
                            communicationService?.send("CM+" + Configs.PREFIX_COMPLIANCE_CALIBRATION)
                        }

                        (requireActivity() as MainActivity).addEvents(
                            "Tube Compliance Calibration Initiated",
                            prefManager?.readUHID().toString()
                        )
                    }

                    "Resistance" -> {
                        communicationService?.takeIf { it.isPortsConnected }?.apply {
                            communicationService?.send("CM+" + Configs.PREFIX_RESISTANCE_CALIBRATION)
                        }

                        (requireActivity() as MainActivity).addEvents(
                            "Tube Resistance Calibration Initiated",
                            prefManager?.readUHID().toString()
                        )

                    }
                }

            }
        }
    }

    private fun highlightProfileButton() {
        when (prefManager?.readCurrentUid()) {
            Configs.PatientProfile.TYPE_ADULT -> includeButtonAdultProfile.buttonView.callOnClick()
            Configs.PatientProfile.TYPE_PED -> includeButtonPediatricProfile.buttonView.callOnClick()
            Configs.PatientProfile.TYPE_NEONAT -> includeButtonNeonatalProfile.buttonView.callOnClick()
            else -> {}
        }
    }

    fun updateTubeComplianceCalibrationStatus() {
        hideGoneFunction(true)
        includetubeAdult.visibility = View.GONE
        includetubePediatric.visibility = View.GONE
        includetubeNeoNate.visibility = View.GONE
        prefManager?.apply {
            Log.i("tubeCheck", "Sensor data is refreshing on the view......")

            if (readComplianceTubeCalibrationStatus()) {
                if (readComplianceTubeCalibration().length < 5 && readComplianceTubeCalibration().equals(
                        ""
                    )
                ) {
                    tvCompensation.text = "${readComplianceTubeCalibration()} mL/cmH₂O"
                    ivCompensationStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    setTubeComplianceCalibrationDate()
                    tvCompensationDate.text = readTubeComplianceCalibrationDate()

                } else {
                    if (readComplianceTubeCalibration().contains("0.00")) {
                        tvCompensation.text = "-"
                        tvCompensationDate.text = "-"
                        ivCompensationStatus.setImageResource(R.drawable.ic_red_cross)

                    } else {
                        tvCompensation.text = "${readComplianceTubeCalibration().subSequence(0, 4)} mL/cmH₂O"
                        ivCompensationStatus.setImageResource(R.drawable.ic_green_circle_tick)
                        setTubeComplianceCalibrationDate()
                        tvCompensationDate.text = readTubeComplianceCalibrationDate()
                    }
                }
            } else {
                tvCompensation.text = getString(R.string.sensore_not_calibrated)
                ivCompensationStatus.setImageResource(R.drawable.ic_red_cross)
                tvCompensationDate.text = "-"
            }
        }
    }

    fun updateTubeResistanceCalibrationStatus() {
        hideGoneFunction(true)
        includetubeAdult.visibility = View.GONE
        includetubePediatric.visibility = View.GONE
        includetubeNeoNate.visibility = View.GONE
        prefManager?.apply {
            // tube resistance
            if (readResistanceTubeCalibrationStatus()) {
                if (readResistanceTubeCalibration().length < 5 && readResistanceTubeCalibration().equals(
                        ""
                    )
                ) {
                    tvResistance.text = "${readResistanceTubeCalibration()} cmH₂O/L"
                    ivResistanceStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    setTubeResistanceCalibrationDate()
                    Log.i("tubeCheck", "Tube resistance calibration date: ${readTubeResistanceCalibrationDate()}")
                    tvResistanceDate.text = readTubeResistanceCalibrationDate()
                } else {
                    tvResistance.text =
                        "${readResistanceTubeCalibration().subSequence(0, 4)} cmH₂O/L"
                    ivResistanceStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    setTubeResistanceCalibrationDate()
                    Log.i("tubeCheck", "Tube resistance calibration date: ${readTubeResistanceCalibrationDate()}")
                    tvResistanceDate.text = readTubeResistanceCalibrationDate()
                }
            } else {
                Log.i("tubeCheck", "Tube resistance calibration date: ${readTubeResistanceCalibrationDate()}")
                tvResistance.text = getString(R.string.sensore_not_calibrated)
                ivResistanceStatus.setImageResource(R.drawable.ic_red_cross)
                tvResistanceDate.text = "-"
            }
        }
    }

    private fun updateTubeCalibrationStatusUI() {
        prefManager?.apply {

            if (readComplianceTubeCalibrationStatus()) {
                if (readComplianceTubeCalibration().length < 5 && readComplianceTubeCalibration().equals(
                        ""
                    )
                ) {
                    tvCompensation.text = "${readComplianceTubeCalibration()} mL/cmH₂O"
                    ivCompensationStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    tvCompensationDate.text = readTubeComplianceCalibrationDate()

                } else {
                    if (readComplianceTubeCalibration().contains("0.00")) {
                        tvCompensation.text = "-"
                        tvCompensationDate.text = "-"
                        ivCompensationStatus.setImageResource(R.drawable.ic_red_cross)
                    } else {
                        tvCompensation.text =
                            "${readComplianceTubeCalibration().subSequence(0, 4)} mL/cmH₂O"
                        ivCompensationStatus.setImageResource(R.drawable.ic_green_circle_tick)
                        tvCompensationDate.text = readTubeComplianceCalibrationDate()
                    }
                }
            } else {
                tvCompensation.text = getString(R.string.sensore_not_calibrated)
                ivCompensationStatus.setImageResource(R.drawable.ic_red_cross)
                tvCompensationDate.text = "-"
            }

            // tube resistance
            if (readResistanceTubeCalibrationStatus()) {
                if (readResistanceTubeCalibration().length < 5 && readResistanceTubeCalibration().equals("")) {
                    tvResistance.text = "${readResistanceTubeCalibration()} cmH₂O/L"
                    ivResistanceStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    tvResistanceDate.text = readTubeResistanceCalibrationDate()
                } else {
                    tvResistance.text = "${readResistanceTubeCalibration().subSequence(0, 4)} cmH₂O/L"
                    ivResistanceStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    tvResistanceDate.text = readTubeResistanceCalibrationDate()
                }
            } else {
                tvResistance.text = getString(R.string.sensore_not_calibrated)
                ivResistanceStatus.setImageResource(R.drawable.ic_red_cross)
                tvResistanceDate.text = "-"
            }
        }
    }

}

