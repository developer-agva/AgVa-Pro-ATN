package com.agvahealthcare.ventilator_ext.graph.loops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.databinding.FragmentLoopsBinding

class LoopsFragment : Fragment() {

    private lateinit var binding : FragmentLoopsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoopsBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
    }

    private fun setupClickListener() {

        binding.includeButtonPressureVolume.buttonView.text = getString(R.string.hint_pressure_volume)
        binding.includeButtonPressureFlow.buttonView.text = getString(R.string.hint_pressure_flow)
        binding.includeButtonVolumeFlow.buttonView.text = getString(R.string.hint_volume_flow)
        binding.includeButtonVolumePCO2.buttonView.text = getString(R.string.hint_volume_pco2)
        binding.includeButtonVolumeFCO2.buttonView.text = getString(R.string.hint_volume_fco2)
        binding.includeButtonPesVolume.buttonView.text = getString(R.string.hint_pes_volume)
        binding.includeButtonPtranspulmVolume.buttonView.text = getString(R.string.hint_ptranspulm_volume)

        binding.includeButtonPressureVolume.buttonView.setOnClickListener {

        }

        binding.includeButtonPressureFlow.buttonView.setOnClickListener {

        }

        binding.includeButtonVolumeFlow.buttonView.setOnClickListener {

        }

        binding.includeButtonVolumePCO2.buttonView.setOnClickListener {

        }

        binding.includeButtonVolumeFCO2.buttonView.setOnClickListener {

        }

        binding.includeButtonPesVolume.buttonView.setOnClickListener {

        }

        binding.includeButtonPtranspulmVolume.buttonView.setOnClickListener {

        }
    }
}
