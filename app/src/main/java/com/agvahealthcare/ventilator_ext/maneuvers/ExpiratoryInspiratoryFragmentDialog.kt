package com.agvahealthcare.ventilator_ext.maneuvers

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.setHeightWidth
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import kotlinx.android.synthetic.main.fragment_expiratory_inspiratory_dialog.*
import java.text.SimpleDateFormat
import java.util.*

class ExpiratoryInspiratoryFragmentDialog : DialogFragment() {

    private var preferenceManager: PreferenceManager? = null
    private var observedPplat: Float? = null
    private var staticCompliance: Float? = null
    private var autoPeep: Float? = null


    companion object {
        const val TAG = "ExpiratoryInspiManeuversDialog"
        private const val WINDOW_HEIGHT = 700
        private const val WINDOW_WIDTH = 280
        private const val KEY_OBSERVED_PPLAT = "KEY_OBSERVED_PPLAT"
        private const val KEY_STATIC_COMPLIANCE = "KEY_STATIC_COMPLIANCE"
        private const val KEY_AUTO_PEEP= "KEY_AUTO_PEEP"
        private const val KEY_STATUS = "KEY_STATUS"

        fun newInstance( observedPplat: Float?, staticCompliance: Float?, autoPeep: Float?, isInspiratory: Boolean?): ExpiratoryInspiratoryFragmentDialog {
            val args = Bundle()
            observedPplat?.let { args.putFloat(KEY_OBSERVED_PPLAT, it) }
            staticCompliance?.let { args.putFloat(KEY_STATIC_COMPLIANCE, it) }
            autoPeep?.let { args.putFloat(KEY_AUTO_PEEP, it) }
            isInspiratory?.let { args.putBoolean(KEY_STATUS, it) }

            val fragment = ExpiratoryInspiratoryFragmentDialog()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view= inflater.inflate(R.layout.fragment_expiratory_inspiratory_dialog,container,false)
        isCancelable = false
        return view
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        setHeightWidth(WINDOW_WIDTH, WINDOW_HEIGHT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        imageViewCross.setOnClickListener{ dismiss() }
        updateDataOnViewAndPreference()
    }

    val current = Calendar.getInstance().time
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val formatted = formatter.format(current)
    val inspTime = formatter.format(current)


    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateDataOnViewAndPreference() {

        observedPplat = arguments?.getFloat(KEY_OBSERVED_PPLAT)
        staticCompliance = arguments?.getFloat(KEY_STATIC_COMPLIANCE)
        autoPeep = arguments?.getFloat(KEY_AUTO_PEEP)
        val isInspiratory = arguments?.getBoolean(KEY_STATUS)

        preferenceManager?.apply {
            if (isInspiratory == true) {
                tvTitle.text = getString(R.string.inspiratory_hold_maneuver)
                setManeuversPplatValue(observedPplat)
                setManeuversStaticComplianceValue(staticCompliance)
                setInspiratoryDate("$inspTime")

                tvPplatValue.text = observedPplat.toString()
                tvPplatDateTime.text = readInspiratoryDate()
                tvSCValue.text = staticCompliance.toString()
                tvSCDateTime.text = readInspiratoryDate()
                layoutAutoPeep.visibility = View.GONE

            } else {
                tvTitle.text = getString(R.string.expiratory_hold_maneuver)
                setManeuversAutoPeepValue(autoPeep)
                setExpiratoryDate("$formatted")
                tvAutoPeepValue.text = autoPeep.toString()
                tvAutoPeepDateTime.text = readExpiratoryDate()
                layoutPplat.visibility = View.GONE
                layoutSC.visibility = View.GONE
            }
        }

    }
}