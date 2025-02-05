package com.agvahealthcare.ventilator_ext.graph.loops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_loops.*

class LoopsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_loops, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
    }

    private fun setupClickListener() {

        includeButtonPressureVolume.buttonView.text = getString(R.string.hint_pressure_volume)
        includeButtonPressureFlow.buttonView.text = getString(R.string.hint_pressure_flow)
        includeButtonVolumeFlow.buttonView.text = getString(R.string.hint_volume_flow)
        includeButtonVolumePCO2.buttonView.text = getString(R.string.hint_volume_pco2)
        includeButtonVolumeFCO2.buttonView.text = getString(R.string.hint_volume_fco2)
        includeButtonPesVolume.buttonView.text = getString(R.string.hint_pes_volume)
        includeButtonPtranspulmVolume.buttonView.text = getString(R.string.hint_ptranspulm_volume)

        includeButtonPressureVolume.buttonView.setOnClickListener {

        }

        includeButtonPressureFlow.buttonView.setOnClickListener {

        }

        includeButtonVolumeFlow.buttonView.setOnClickListener {

        }

        includeButtonVolumePCO2.buttonView.setOnClickListener {

        }

        includeButtonVolumeFCO2.buttonView.setOnClickListener {

        }

        includeButtonPesVolume.buttonView.setOnClickListener {

        }

        includeButtonPtranspulmVolume.buttonView.setOnClickListener {

        }
    }
}
