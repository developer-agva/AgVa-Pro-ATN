package com.agvahealthcare.ventilator_ext.graph.waveforms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.content_waveforms_layout.view.*
import kotlinx.android.synthetic.main.fragment_waveforms.*

class WaveformsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_waveforms, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpData()
    }

    private fun setUpData() {

        includeContentWaveforms.includeButtonPressure.buttonView.text = getString(R.string.hint_pressure)
        includeContentWaveforms.includeButtonPCo2.buttonView.text = getString(R.string.hint_pco2)
        includeContentWaveforms.includeButtonPes.buttonView.text = getString(R.string.hint_pes)
        includeContentWaveforms.includeButtonFlow.buttonView.text = getString(R.string.hint_flow)
        includeContentWaveforms.includeButtonFCo2.buttonView.text = getString(R.string.hint_fco2)
        includeContentWaveforms.includeButtonPtranspulm.buttonView.text = getString(R.string.hint_ptranspulm)
        includeContentWaveforms.includeButtonVolume.buttonView.text = getString(R.string.hint_volume)
        includeContentWaveforms.includeButtonPlethysmogram.buttonView.text = getString(R.string.hint_plethysmogram)
        includeContentWaveforms.includeButtonOff.buttonView.text = getString(R.string.hint_off)

    }
}
