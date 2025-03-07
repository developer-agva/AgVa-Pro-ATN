package com.agvahealthcare.ventilator_ext.system.tube

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.databinding.FragmentTubeDiaBinding
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs

class TubeDiaFragment(private var communicationService: CommunicationService?) : Fragment(),
    View.OnClickListener {

    companion object {
        const val TAG = "TubeDiaFragment"
    }
    private lateinit var binding: FragmentTubeDiaBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentTubeDiaBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private var prefManager: PreferenceManager? = null
    private lateinit var mEventViewModel: EventViewModel
    private var currentTag: String = "";
    private var tubeDiameter: String? = null


    // logic knob highlight starts here

    fun handleClick(highlightedIndex: Int) {
        clearPreviousConstraints()

        when (highlightedIndex) {

            0 -> if (binding.topBarTube.isVisible) binding.backBtnTube.callOnClick() else binding.includeButtonCompliance.buttonView.callOnClick()

            1 -> if (binding.topBarTube.isVisible) binding.includeButtonAdultProfile.buttonView.callOnClick() else binding.includeButtonResistance.buttonView.callOnClick()

            2 -> binding.includeButtonPediatricProfile.buttonView.callOnClick()
            3 -> binding.includeButtonNeonatalProfile.buttonView.callOnClick()
            4 -> binding.includetubeAdult.buttonView.callOnClick()
            5 -> binding.includetubePediatric.buttonView.callOnClick()
            6 -> binding.includetubeNeoNate.buttonView.callOnClick()
            7 -> binding.includeButtoncmdSend.buttonView.callOnClick()
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
            constraintSet.clone(binding.mainViewPanelTube)
            constraintSet.clear(binding.focusLayoutTube.id, ConstraintSet.TOP)
            constraintSet.clear(binding.focusLayoutTube.id, ConstraintSet.BOTTOM)
            constraintSet.clear(binding.focusLayoutTube.id, ConstraintSet.LEFT)
            constraintSet.clear(binding.focusLayoutTube.id, ConstraintSet.RIGHT)
            constraintSet.applyTo(binding.mainViewPanelTube)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun changeConstraintsOfFocusLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.mainViewPanelTube)
        constraintSet.connect(
            binding.focusLayoutTube.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.focusLayoutTube.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            binding.focusLayoutTube.id,
            ConstraintSet.BOTTOM,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            binding.focusLayoutTube.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.mainViewPanelTube)
    }

    //
    private fun getViewForFocus(highlightedIndex: Int, data: String?): View? {

        data?.let {

            return when (highlightedIndex) {

                0 -> if (binding.topBarTube.isVisible) binding.backBtnTube else binding.includeButtonCompliance.root
                1 -> if (binding.topBarTube.isVisible) binding.includeButtonAdultProfile.root else binding.includeButtonResistance.root
                2 -> binding.includeButtonPediatricProfile.root
                3 -> binding.includeButtonNeonatalProfile.root
                4 -> binding.includetubeAdult.root
                5 -> binding.includetubePediatric.root
                6 -> binding.includetubeNeoNate.root
                7 -> binding.includeButtoncmdSend.root

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
            binding.topBarTube.visibility = View.GONE
            binding.backBtnTube.visibility = View.GONE
            binding.tvMainTitleTube.visibility = View.GONE
            binding.includeButtonAdultProfile.root.visibility = View.GONE
            binding.includeButtonPediatricProfile.root.visibility = View.GONE
            binding.includeButtonNeonatalProfile.root.visibility = View.GONE
            binding.tvtubelength.visibility = View.GONE
            binding.tvtextHeadingTube.visibility = View.GONE
            binding.tvtext1Tube.visibility = View.GONE
            binding.tvtext2Tube.visibility = View.GONE
            binding.tvtext3Tube.visibility = View.GONE
            binding.ventigifCompliance.visibility = View.GONE
            binding.ventigifResistance.visibility = View.GONE
            binding.includeButtoncmdSend.root.visibility = View.GONE

            // calib layouts
            binding.tubeText.visibility = View.VISIBLE
            binding.includeButtonCompliance.root.visibility = View.VISIBLE
            binding.includeButtonResistance.root.visibility = View.VISIBLE
            binding.tvCompensation.visibility = View.VISIBLE
            binding.tvResistance.visibility = View.VISIBLE
            binding.tvCompensationDate.visibility = View.VISIBLE
            binding.tvResistanceDate.visibility = View.VISIBLE
            binding.ivCompensationStatus.visibility = View.VISIBLE
            binding.ivResistanceStatus.visibility = View.VISIBLE

        } else {
            // pre op check layout
            binding.topBarTube.visibility = View.VISIBLE
            binding.backBtnTube.visibility = View.VISIBLE
            binding.tvMainTitleTube.visibility = View.VISIBLE
            binding.includeButtonAdultProfile.root.visibility = View.VISIBLE
            binding.includeButtonPediatricProfile.root.visibility = View.VISIBLE
            binding.includeButtonNeonatalProfile.root.visibility = View.VISIBLE
            binding.tvtubelength.visibility = View.VISIBLE
            binding.tvtextHeadingTube.visibility = View.VISIBLE
            binding.tvtext1Tube.visibility = View.VISIBLE
            binding.tvtext2Tube.visibility = View.VISIBLE
            binding.tvtext3Tube.visibility = View.VISIBLE
            binding.includeButtoncmdSend.root.visibility = View.VISIBLE

            // calib layouts
            binding.tubeText.visibility = View.GONE
            binding.includeButtonCompliance.root.visibility = View.GONE
            binding.includeButtonResistance.root.visibility = View.GONE
            binding.tvCompensation.visibility = View.GONE
            binding.tvResistance.visibility = View.GONE
            binding.tvCompensationDate.visibility = View.GONE
            binding.tvResistanceDate.visibility = View.GONE
            binding.ivCompensationStatus.visibility = View.GONE
            binding.ivResistanceStatus.visibility = View.GONE
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())
        mEventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        setUpOnClickListener()
        setUpView()
        updateTubeCalibrationStatusUI()

        hideGoneFunction(true)
    }

    private fun setUpView() {
        binding.includeButtonCompliance.buttonView.text = getString(R.string.hint_compensation)
        binding.includeButtonResistance.buttonView.text = getString(R.string.hint_resistance)
        binding.includeButtonAdultProfile.buttonView.text = getString(R.string.hint_adult)
        binding.includeButtonNeonatalProfile.buttonView.text = getString(R.string.hint_neonatal)
        binding.includeButtonPediatricProfile.buttonView.text = getString(R.string.hint_ped)

        binding.includetubeAdult.buttonView.text = "22 mm"
        binding.includetubePediatric.buttonView.text = "15 mm"
        binding.includetubeNeoNate.buttonView.text = "10 mm"

        binding.includeButtoncmdSend.buttonView.text = "START CALIBRATION"

        binding.includeButtoncmdSend.buttonView.setBackgroundColor(R.drawable.background_black_border_white)
        binding.includeButtoncmdSend.buttonView.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )

        binding.backBtnTube.setOnClickListener{
            (parentFragment as SystemDialogFragment).highlightedIndex = -1
            (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 1
            hideGoneFunction(true)
            binding.includetubeAdult.root.visibility = View.GONE
            binding.includetubePediatric.root.visibility = View.GONE
            binding.includetubeNeoNate.root.visibility = View.GONE
        }

    }

    private fun setUpOnClickListener() {
        if (tag == "FromDashboard") {
            binding.includeButtonCompliance.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }
            binding.includeButtonResistance.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for calibration process"
                )
            }
        } else {
            binding.includeButtonCompliance.buttonView.setOnClickListener(this)
            binding.includeButtonResistance.buttonView.setOnClickListener(this)
        }

        binding.includeButtonNeonatalProfile.buttonView.setOnClickListener {
            if (prefManager?.readCurrentUid() != Configs.PatientProfile.TYPE_NEONAT) {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to neonate profile"
                )
            } else {
                binding.includeButtonNeonatalProfile.buttonView.setBackgroundResource(R.color.racing_green)
                binding.includeButtonNeonatalProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.includeButtonAdultProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includeButtonAdultProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                binding.includeButtonPediatricProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includeButtonPediatricProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

                binding.includetubeAdult.root.visibility = View.VISIBLE
                binding.includetubePediatric.root.visibility = View.VISIBLE
                binding.includetubeNeoNate.root.visibility = View.VISIBLE

                binding.includetubeNeoNate.buttonView.callOnClick()
            }
        }

        binding.includeButtonAdultProfile.buttonView.setOnClickListener {

            if (prefManager?.readCurrentUid() != Configs.PatientProfile.TYPE_ADULT) {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to adult profile"
                )
            }else {
                binding.includeButtonAdultProfile.buttonView.setBackgroundResource(R.color.racing_green)
                binding.includeButtonAdultProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.includeButtonPediatricProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includeButtonPediatricProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

                binding.includeButtonNeonatalProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includeButtonNeonatalProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

                binding.includetubeAdult.root.visibility = View.VISIBLE
                binding.includetubePediatric.root.visibility = View.VISIBLE
                binding.includetubeNeoNate.root.visibility = View.VISIBLE

                binding.includetubeAdult.buttonView.callOnClick()
            }
        }

        binding.includeButtonPediatricProfile.buttonView.setOnClickListener {

            if (prefManager?.readCurrentUid() != Configs.PatientProfile.TYPE_PED) {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to pediatric profile"
                )
            }else {
                binding.includeButtonPediatricProfile.buttonView.setBackgroundResource(R.color.racing_green)
                binding.includeButtonPediatricProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.includeButtonAdultProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includeButtonAdultProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

                binding.includeButtonNeonatalProfile.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includeButtonNeonatalProfile.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

                binding.includetubeAdult.root.visibility = View.VISIBLE
                binding.includetubePediatric.root.visibility = View.VISIBLE
                binding.includetubeNeoNate.root.visibility = View.VISIBLE

                binding.includetubePediatric.buttonView.callOnClick()
            }
        }

        binding.includetubePediatric.buttonView.setOnClickListener(this)
        binding.includetubeAdult.buttonView.setOnClickListener(this)
        binding.includetubeNeoNate.buttonView.setOnClickListener(this)
        binding.includeButtoncmdSend.buttonView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v) {

            binding.includeButtonCompliance.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 7
                currentTag = "Compliance"
                hideGoneFunction(false)
                highlightProfileButton()
                binding.ventigifCompliance.visibility = View.VISIBLE
                binding.ventigifResistance.visibility = View.GONE
                binding.tvMainTitleTube.text = "Compliance pre-calibration check"
                binding.tvtext1Tube.text = "1. Make sure the ventilator is connected to mains supply"
                binding.tvtext2Tube.text = "2. Ensure the patient is not connected to the ventilator"
                binding.tvtext3Tube.text = "3. Block the patient end of the breathing circuit using your thumb"

            }

            binding.includeButtonResistance.buttonView -> {
                (parentFragment as SystemDialogFragment).highlightedIndex = -1
                (parentFragment as SystemDialogFragment).sizeOfCurrentArray = 7
                currentTag = "Resistance"
                hideGoneFunction(false)
                highlightProfileButton()
                binding.ventigifCompliance.visibility = View.GONE
                binding.ventigifResistance.visibility = View.VISIBLE
                binding.tvMainTitleTube.text = "Resistance pre-calibration check"
                binding.tvtext1Tube.text = "1. Make sure the ventilator is connected to mains supply"
                binding.tvtext2Tube.text = "2. Ensure the patient is not connected to the ventilator"
                binding.tvtext3Tube.text = "3. Keep the breathing circuit open"
            }

            binding.includetubeAdult.buttonView -> {
                tubeDiameter = "22 mm"
                binding.includetubeAdult.buttonView.setBackgroundResource(R.color.racing_green)
                binding.includetubeAdult.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.includetubePediatric.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includetubePediatric.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                binding.includetubeNeoNate.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includetubeNeoNate.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

            }

            binding.includetubePediatric.buttonView -> {
                tubeDiameter = "15 mm"
                binding.includetubePediatric.buttonView.setBackgroundResource(R.color.racing_green)
                binding.includetubePediatric.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.includetubeAdult.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includetubeAdult.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                binding.includetubeNeoNate.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includetubeNeoNate.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }

            binding.includetubeNeoNate.buttonView -> {
                tubeDiameter = "10 mm"
                binding.includetubeNeoNate.buttonView.setBackgroundResource(R.color.racing_green)
                binding.includetubeNeoNate.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.includetubePediatric.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includetubePediatric.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                binding.includetubeAdult.buttonView.setBackgroundResource(R.color.dolphin_grey)
                binding.includetubeAdult.buttonView.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )

            }

            binding.includeButtoncmdSend.buttonView -> {
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
            Configs.PatientProfile.TYPE_ADULT -> binding.includeButtonAdultProfile.buttonView.callOnClick()
            Configs.PatientProfile.TYPE_PED -> binding.includeButtonPediatricProfile.buttonView.callOnClick()
            Configs.PatientProfile.TYPE_NEONAT -> binding.includeButtonNeonatalProfile.buttonView.callOnClick()
            else -> {}
        }
    }

    fun updateTubeComplianceCalibrationStatus() {
        hideGoneFunction(true)
        binding.includetubeAdult.root.visibility = View.GONE
        binding.includetubePediatric.root.visibility = View.GONE
        binding.includetubeNeoNate.root.visibility = View.GONE
        prefManager?.apply {
            Log.i("tubeCheck", "Sensor data is refreshing on the view......")

            if (readComplianceTubeCalibrationStatus()) {
                if (readComplianceTubeCalibration().length < 5 && readComplianceTubeCalibration().equals(
                        ""
                    )
                ) {
                    Log.i("DataAvaiasd", "2")
                    Log.i("CHECK_COMPL", readComplianceTubeCalibration().toString())
                    binding.tvCompensation.text = "${readComplianceTubeCalibration()} mL/cmH₂O"
                    binding.ivCompensationStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    setTubeComplianceCalibrationDate()
                    binding.tvCompensationDate.text = readTubeComplianceCalibrationDate()

                } else {
                    Log.i("DataAvaiasd", readComplianceTubeCalibration().toString())
                    if (readComplianceTubeCalibration().contains("0.00")) {
                        binding.tvCompensation.text = "-"
                        binding.tvCompensationDate.text = "-"
                        binding.ivCompensationStatus.setImageResource(R.drawable.ic_red_cross)


                    } else {
                        Log.i("DataAvaiasd", "5")
                        binding.tvCompensation.text =
                            "${readComplianceTubeCalibration().subSequence(0, 4)} mL/cmH₂O"
                        binding.ivCompensationStatus.setImageResource(R.drawable.ic_green_circle_tick)
                        setTubeComplianceCalibrationDate()
                        binding.tvCompensationDate.text = readTubeComplianceCalibrationDate()
                    }
                }
            } else {
                binding.tvCompensation.text = getString(R.string.sensore_not_calibrated)
                binding.ivCompensationStatus.setImageResource(R.drawable.ic_red_cross)
                binding.tvCompensationDate.text = "-"
            }

        }
    }

    fun updateTubeResistanceCalibrationStatus() {
        hideGoneFunction(true)
        binding.includetubeAdult.root.visibility = View.GONE
        binding.includetubePediatric.root.visibility = View.GONE
        binding.includetubeNeoNate.root.visibility = View.GONE
        prefManager?.apply {
            // tube resistance
            if (readResistanceTubeCalibrationStatus()) {

                if (readResistanceTubeCalibration().length < 5 && readResistanceTubeCalibration().equals(
                        ""
                    )
                ) {
                    binding.tvResistance.text = "${readResistanceTubeCalibration()} cmH₂O/L"
                    binding.ivResistanceStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    setTubeResistanceCalibrationDate()
                    binding.tvResistanceDate.text = readTubeResistanceCalibrationDate()
                } else {
                    binding.tvResistance.text =
                        "${readResistanceTubeCalibration().subSequence(0, 4)} cmH₂O/L"
                    binding.ivResistanceStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    setTubeResistanceCalibrationDate()
                    binding.tvResistanceDate.text = readTubeResistanceCalibrationDate()
                }
            } else {
                binding.tvResistance.text = getString(R.string.sensore_not_calibrated)
                binding.ivResistanceStatus.setImageResource(R.drawable.ic_red_cross)
                binding.tvResistanceDate.text = "-"
            }

        }
    }

    private fun updateTubeCalibrationStatusUI() {
        prefManager?.apply {
            Log.i("tubeCheck", "Sensor data is refreshing on the view......")

            if (readComplianceTubeCalibrationStatus()) {
                if (readComplianceTubeCalibration().length < 5 && readComplianceTubeCalibration().equals(
                        ""
                    )
                ) {
                    Log.i("DataAvaiasd", "2")
                    Log.i("CHECK_COMPL", readComplianceTubeCalibration().toString())
                    binding.tvCompensation.text = "${readComplianceTubeCalibration()} mL/cmH₂O"
                    binding.ivCompensationStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    binding.tvCompensationDate.text = readTubeComplianceCalibrationDate()

                } else {
                    Log.i("DataAvaiasd", readComplianceTubeCalibration().toString())
                    if (readComplianceTubeCalibration().contains("0.00")) {
                        binding.tvCompensation.text = "-"
                        binding.tvCompensationDate.text = "-"
                        binding.ivCompensationStatus.setImageResource(R.drawable.ic_red_cross)
                    } else {
                        Log.i("DataAvaiasd", "5")
                        binding.tvCompensation.text =
                            "${readComplianceTubeCalibration().subSequence(0, 4)} mL/cmH₂O"
                        binding.ivCompensationStatus.setImageResource(R.drawable.ic_green_circle_tick)
                        binding.tvCompensationDate.text = readTubeComplianceCalibrationDate()
                    }
                }
            } else {
                binding.tvCompensation.text = getString(R.string.sensore_not_calibrated)
                binding.ivCompensationStatus.setImageResource(R.drawable.ic_red_cross)
                binding.tvCompensationDate.text = "-"
            }

            // tube resistance
            if (readResistanceTubeCalibrationStatus()) {

                if (readResistanceTubeCalibration().length < 5 && readResistanceTubeCalibration().equals(
                        ""
                    )
                ) {
                    binding.tvResistance.text = "${readResistanceTubeCalibration()} cmH₂O/L"
                    binding.ivResistanceStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    binding.tvResistanceDate.text = readTubeResistanceCalibrationDate()
                } else {
                    binding.tvResistance.text =
                        "${readResistanceTubeCalibration().subSequence(0, 4)} cmH₂O/L"
                    binding.ivResistanceStatus.setImageResource(R.drawable.ic_green_circle_tick)
                    binding.tvResistanceDate.text = readTubeResistanceCalibrationDate()
                }
            } else {
                binding.tvResistance.text = getString(R.string.sensore_not_calibrated)
                binding.ivResistanceStatus.setImageResource(R.drawable.ic_red_cross)
                binding.tvResistanceDate.text = "-"
            }
        }
    }

}

