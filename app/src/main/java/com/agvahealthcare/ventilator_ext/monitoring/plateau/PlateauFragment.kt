package com.agvahealthcare.ventilator_ext.monitoring.plateau

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import kotlinx.android.synthetic.main.fragment_plateau.*

class PlateauFragment : Fragment() {

    private var preferenceManager: PreferenceManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_plateau, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())

        setDataViaPreference()
    }

    private fun setDataViaPreference() {

        preferenceManager?.apply {

            if (readManeuversPplatValue().toString() == "0.0") {
                textViewPlateauPressureValue.text = "N/A"
            } else {
                textViewPlateauPressureValue.text = readManeuversPplatValue().toString()
            }

            if (readManeuversAutoPeepValue().toString() == "0.0") {
                textViewAutoPeepValue.text = "N/A"
            } else {
                textViewAutoPeepValue.text = readManeuversAutoPeepValue().toString()
            }

            textViewPlateauPressureTime.text = readInspiratoryDate()
            textViewAutoPeepTime.text = readExpiratoryDate()
            textViewMeasureTimeValue.text = readMeasureTime()

        }
    }

}