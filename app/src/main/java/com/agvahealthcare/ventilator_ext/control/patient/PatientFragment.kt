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
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.content_female_layout.view.*
import kotlinx.android.synthetic.main.content_male_layout.view.*
import kotlinx.android.synthetic.main.fragment_patient.*
import kotlinx.android.synthetic.main.fragment_patient.et_uhid
import kotlinx.android.synthetic.main.fragment_patient.includeProgressAge
import kotlinx.android.synthetic.main.fragment_patient.includeProgressHeight
import kotlinx.android.synthetic.main.fragment_patient.includeProgressWeight
import kotlinx.android.synthetic.main.knob_progress_view_red.view.textView
import kotlinx.android.synthetic.main.text_knob_view.view.*

class PatientFragment : Fragment(), View.OnClickListener {

    private var prefManager: PreferenceManager? = null
    private var customCountDownTimer: CustomCountDownTimer? = null
    private var dashBoardViewModel: DashBoardViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_patient, container, false)
        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
//Here this is implemented in a different way if the Input field is empty the field will be active else it will not be editable.

        view.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                et_uhid.isCursorVisible = false
                et_uhid.setBackgroundColor(resources.getColor(R.color.uhid_grey, null))
                try {
                    val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
                } catch (e: Error) {
                    e.printStackTrace()
                }
                et_uhid.text?.clear()
                if (prefManager?.readUHID() != FIRST_FILTER_NAME) et_uhid?.setText(prefManager?.readUHID())
                return true
            }
        })
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())

        initViewViaPreference()
        setOnClickListener()

        if (prefManager?.readUHID() != FIRST_FILTER_NAME) et_uhid.setText(prefManager?.readUHID())

        et_uhid.isEnabled = et_uhid.text.toString().isEmpty()

        et_uhid.setOnClickListener {
            et_uhid.isCursorVisible = true
            et_uhid.setBackgroundColor(resources.getColor(R.color.white, null))
        }

        et_uhid.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_DONE) {

                    et_uhid.isEnabled = et_uhid.text.toString().isEmpty()

                    if (et_uhid.text.toString().length <= 10) {
                        prefManager?.setUHID(et_uhid.text.toString())

                        dashBoardViewModel?.updateisUHIDSet(true)
                        et_uhid.isCursorVisible = false
                        et_uhid.setBackgroundColor(resources.getColor(R.color.uhid_grey, null))
                    } else {
                        if (prefManager?.readUHID() != null) {
                        } else {
                            ToastFactory.custom(context, "Invalid UHID. Please Re-enter")
                        }
                        et_uhid.isCursorVisible = false
                        et_uhid.setBackgroundColor(resources.getColor(R.color.uhid_grey, null))
                    }
                    AppUtils.hideKeyBoard(context, et_uhid)
                    if (prefManager?.readUHID() != FIRST_FILTER_NAME) et_uhid?.setText(prefManager?.readUHID())
                    return true
                }
                return false
            }
        })
    }


    private fun initViewViaPreference() {

        prefManager?.apply {

            readBodyHeight()?.toDouble()?.toInt()
                ?.let { includeProgressHeight.progress_bar.progress = it }
            readBodyWeight()?.toDouble()?.toInt()
                ?.let { includeProgressWeight.progress_bar.progress = it }
            readAge()?.toDouble()?.toInt()?.let { includeProgressAge.progress_bar.progress = it }

            includeProgressAge.progress_bar.max = PATIENT_AGE_UPPER
            includeProgressHeight.progress_bar.max = PATIENT_ADULT_HEIGHT_UPPER
            includeProgressWeight.progress_bar.max = PATIENT_ADULT_WEIGHT_UPPER

            includeProgressAge.textView.text = readAge()?.toDouble()?.toInt().toString()
            includeProgressHeight.textView.text = readBodyHeight()?.toDouble()?.toInt().toString()
            includeProgressWeight.textView.text = readBodyWeight()?.toDouble()?.toInt().toString()


            readCurrentUid()?.let {
                when (it) {
                    Configs.PatientProfile.TYPE_ADULT -> {
                        patientType.text = "ADULT"
                        txtYears.text = "years"
                        txtAge.text = "AGE"
                    }

                    Configs.PatientProfile.TYPE_PED -> {
                        patientType.text = "Pediatric"
                        txtYears.text = "years"
                        txtAge.text = "AGE"
                    }

                    Configs.PatientProfile.TYPE_NEONAT -> {
                        patientType.text = "Neonate"
                        txtYears.text = "days"
                        txtAge.text = "DAY"
                    }
                }
            }
            readCurrentUid()?.let {
                when (it) {
                    Configs.PatientProfile.TYPE_ADULT -> {
                        patientType.text = "ADULT"
                    }

                    Configs.PatientProfile.TYPE_PED -> {
                        patientType.text = "Pediatric"
                    }

                    Configs.PatientProfile.TYPE_NEONAT -> {
                        patientType.text = "Neonate"
                    }
                }
            }

            if (Configs.Gender.TYPE_MALE == readGender()) setDataMale()
            else setDataFemale()

        }
    }

    private fun setOnClickListener() {

        includeButtonReset.buttonView.text = getString(R.string.hint_reset)
        includeButtonReset.buttonView.setTextColor(resources.getColor(R.color.white))
        includeButtonReset.buttonView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        includeButtonReset.buttonView.setPadding(50, 0, 50, 0)


        includeButtonReset.buttonView.setOnClickListener {
            customCountDownTimer?.count = 0L
            // Toast.makeText(context,"click",Toast.LENGTH_LONG).show()

        }
        /*progressBarHeight.layoutPanel.setOnClickListener {

        }*/
    }


    private fun setDataMale() {
        layoutMale.imageViewMale.setImageResource(R.drawable.ic_male_select)
        layoutMale.buttonMale.setBackgroundResource(R.drawable.background_green_border)
        layoutMale.buttonMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        layoutFemale.imageViewFemale.setImageResource(R.drawable.ic_female_unselect)
        layoutFemale.buttonFemale.setBackgroundResource(R.drawable.background_medium_grey)
        layoutFemale.buttonFemale.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
    }

    private fun setDataFemale() {
        layoutMale.imageViewMale.setImageResource(R.drawable.ic_male_unselect)
        layoutMale.buttonMale.setBackgroundResource(R.drawable.background_medium_grey)
        layoutMale.buttonMale.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        layoutFemale.imageViewFemale.setImageResource(R.drawable.ic_female_select)
        layoutFemale.buttonFemale.setBackgroundResource(R.drawable.background_green_border)
        layoutFemale.buttonFemale.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.white
            )
        )
    }

    fun setVentilationTime(counterState: String, customCountDownTimer: CustomCountDownTimer) {
        this.customCountDownTimer = customCountDownTimer
        textViewTime?.text = counterState
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