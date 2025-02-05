package com.agvahealthcare.ventilator_ext.graph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.agvahealthcare.ventilator_ext.R
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.content_waveforms_layout.view.*
import kotlinx.android.synthetic.main.fragment_graph_dialog.*

class GraphDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "GraphDialog"

        fun newInstance(): GraphDialogFragment {
            return GraphDialogFragment()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_graph_dialog, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
    }


    override fun onResume() {
        super.onResume()
        setUpData()
    }
    // setUp Data
    private fun setUpData() {

        includeButtonWaveforms.buttonView.text = getString(R.string.hint_waveforms)
        includeButtonWaveforms.buttonView.setBackgroundResource(R.drawable.background_light_white_border)
        includeButtonWaveforms.buttonView.setPadding(20,0,20,0)

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

    // ClickListener on Buttons
    private fun setupClickListener() {

        imageViewCross.setOnClickListener {
            dismiss()
        }

        includeContentWaveforms.includeButtonPressure.buttonView.setOnClickListener {

        }

        includeContentWaveforms.includeButtonPCo2.buttonView.setOnClickListener {

        }

        includeContentWaveforms.includeButtonPes.buttonView.setOnClickListener {

        }

        includeContentWaveforms.includeButtonFlow.buttonView.setOnClickListener {

        }

        includeContentWaveforms.includeButtonFCo2.buttonView.setOnClickListener {

        }

        includeContentWaveforms.includeButtonPtranspulm.buttonView.setOnClickListener {

        }

        includeContentWaveforms.includeButtonVolume.buttonView.setOnClickListener {

        }

        includeContentWaveforms.includeButtonPlethysmogram.buttonView.setOnClickListener {

        }

        includeContentWaveforms.includeButtonOff.buttonView.setOnClickListener {

        }

    }

    override fun onStart() {
        super.onStart()
//        setHeightWidthPercent(KEY_HEIGHT, KEY_WIDTH)

    }

}