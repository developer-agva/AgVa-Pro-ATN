package com.agvahealthcare.ventilator_ext.control.patient

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.databinding.FragmentPatientBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.agvahealthcare.ventilator_ext.utility.utils.Configs


class PatientFragment : Fragment(), View.OnClickListener {

    private var prefManager: PreferenceManager? = null
    private var customCountDownTimer: CustomCountDownTimer? = null
    private var dashBoardViewModel: DashBoardViewModel? = null
    private lateinit var binding: FragmentPatientBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPatientBinding.inflate(layoutInflater, container, false)
        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
//Here this is implemented in a different way if the Input field is empty the field will be active else it will not be editable.

        binding.root.setOnClickListener {
            binding.etUhid.isCursorVisible = false
            binding.etUhid.setBackgroundColor(resources.getColor(R.color.uhid_grey, null))
            try {
                val imm =
                    activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            } catch (e: Error) {
                e.printStackTrace()
            }
            binding.etUhid.text?.clear()
            if (prefManager?.readUHID() != FIRST_FILTER_NAME) binding.etUhid?.setText(prefManager?.readUHID())
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())

        initViewViaPreference()
        setOnClickListener()

        if (prefManager?.readUHID() != FIRST_FILTER_NAME) binding.etUhid.setText(prefManager?.readUHID())

        binding.etUhid.isEnabled = binding.etUhid.text.toString().isEmpty()

        binding.etUhid.setOnClickListener {
            binding.etUhid.isCursorVisible = true
            binding.etUhid.setBackgroundColor(resources.getColor(R.color.white, null))
        }

        binding.etUhid.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_DONE) {

                    binding.etUhid.isEnabled = binding.etUhid.text.toString().isEmpty()

                    if (binding.etUhid.text.toString().length <= 10) {
                        prefManager?.setUHID(binding.etUhid.text.toString())

                        dashBoardViewModel?.updateisUHIDSet(true)
                        binding.etUhid.isCursorVisible = false
                        binding.etUhid.setBackgroundColor(resources.getColor(R.color.uhid_grey, null))
                    } else {
                        if (prefManager?.readUHID() != null) {
                        } else {
                            ToastFactory.custom(context, "Invalid UHID. Please Re-enter")
                        }
                        binding.etUhid.isCursorVisible = false
                        binding.etUhid.setBackgroundColor(resources.getColor(R.color.uhid_grey, null))
                    }
                    AppUtils.hideKeyBoard(context, binding.etUhid)
                    if (prefManager?.readUHID() != FIRST_FILTER_NAME) binding.etUhid?.setText(prefManager?.readUHID())
                    return true
                }
                return false
            }
        })
    }


    private fun initViewViaPreference() {

        prefManager?.apply {

            readBodyHeight()?.toDouble()?.toInt()
                ?.let {  binding.includeProgressHeight.progressBar.progress = it }
            readBodyWeight()?.toDouble()?.toInt()
                ?.let { binding.includeProgressWeight.progressBar.progress = it }
            readAge()?.toDouble()?.toInt()?.let { binding.includeProgressAge.progressBar.progress = it }

            binding.includeProgressAge.progressBar.max = PATIENT_AGE_UPPER
            binding.includeProgressHeight.progressBar.max = PATIENT_ADULT_HEIGHT_UPPER
            binding.includeProgressWeight.progressBar.max = PATIENT_ADULT_WEIGHT_UPPER

            binding.includeProgressAge.textView.text = readAge()?.toDouble()?.toInt().toString()
            binding.includeProgressHeight.textView.text = readBodyHeight()?.toDouble()?.toInt().toString()
            binding.includeProgressWeight.textView.text = readBodyWeight()?.toDouble()?.toInt().toString()


            readCurrentUid()?.let {
                when (it) {
                    Configs.PatientProfile.TYPE_ADULT -> {
                        binding.patientType.text = "ADULT"
                        binding.txtYears.text = "years"
                        binding.txtAge.text = "AGE"
                    }

                    Configs.PatientProfile.TYPE_PED -> {
                        binding.patientType.text = "Pediatric"
                        binding.txtYears.text = "years"
                        binding.txtAge.text = "AGE"
                    }

                    Configs.PatientProfile.TYPE_NEONAT -> {
                        binding.patientType.text = "Neonate"
                        binding.txtYears.text = "days"
                        binding.txtAge.text = "DAY"
                    }
                }
            }
            readCurrentUid()?.let {
                when (it) {
                    Configs.PatientProfile.TYPE_ADULT -> {
                        binding.patientType.text = "ADULT"
                    }

                    Configs.PatientProfile.TYPE_PED -> {
                        binding.patientType.text = "Pediatric"
                    }

                    Configs.PatientProfile.TYPE_NEONAT -> {
                        binding.patientType.text = "Neonate"
                    }
                }
            }

            if (Configs.Gender.TYPE_MALE == readGender()) setDataMale()
            else setDataFemale()

        }
    }

    private fun setOnClickListener() {

        binding.includeButtonReset.buttonView.text = getString(R.string.hint_reset)
        binding.includeButtonReset.buttonView.setTextColor(resources.getColor(R.color.white))
        binding.includeButtonReset.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        binding.includeButtonReset.buttonView.setPadding(50, 0, 50, 0)


        binding.includeButtonReset.buttonView.setOnClickListener {
            customCountDownTimer?.count = 0L
            // Toast.makeText(context,"click",Toast.LENGTH_LONG).show()

        }
        /*progressBarHeight.layoutPanel.setOnClickListener {

        }*/
    }


    private fun setDataMale() {
        binding.layoutMale.imageViewMale.setImageResource(R.drawable.ic_male_select)
        binding.layoutMale.buttonMale.setBackgroundResource(R.drawable.background_green_border)
        binding.layoutMale.buttonMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.layoutFemale.imageViewFemale.setImageResource(R.drawable.ic_female_unselect)
        binding.layoutFemale.buttonFemale.setBackgroundResource(R.drawable.background_medium_grey)
        binding.layoutFemale.buttonFemale.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
    }

    private fun setDataFemale() {
        binding.layoutMale.imageViewMale.setImageResource(R.drawable.ic_male_unselect)
        binding.layoutMale.buttonMale.setBackgroundResource(R.drawable.background_medium_grey)
        binding.layoutMale.buttonMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.layoutFemale.imageViewFemale.setImageResource(R.drawable.ic_female_select)
        binding.layoutFemale.buttonFemale.setBackgroundResource(R.drawable.background_green_border)
        binding.layoutFemale.buttonFemale.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
    }

    fun setVentilationTime(counterState: String, customCountDownTimer: CustomCountDownTimer) {
        this.customCountDownTimer = customCountDownTimer
        binding.textViewTime.text = counterState
    }

    override fun onPause() {
        super.onPause()

        try {
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        } catch (e: Error) {
            e.printStackTrace()
        }
    }

    override fun onClick(p0: View?) {

    }
}