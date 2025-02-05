package com.agvahealthcare.ventilator_ext.graph.graphics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_graphics.*

class GraphicsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_graphics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
    }

    private fun setupClickListener() {
        includeButtonDynamicLung.buttonView.text = getString(R.string.hint_dynamic_lung)
        includeButtonVentStatus.buttonView.text = getString(R.string.hint_vent_status)
        includeButtonAsvGraph.buttonView.text = getString(R.string.hint_asv_graph)
        includeButtonMonitoring.buttonView.text = getString(R.string.hint_monitoring)

        includeButtonDynamicLung.buttonView.setPadding(40,0,40,0)
        includeButtonVentStatus.buttonView.setPadding(50,0,50,0)
        includeButtonAsvGraph.buttonView.setPadding(50,0,50,0)
        includeButtonMonitoring.buttonView.setPadding(50,0,50,0)

        includeButtonDynamicLung.buttonView.setOnClickListener {

        }

        includeButtonVentStatus.buttonView.setOnClickListener {

        }

        includeButtonAsvGraph.buttonView.setOnClickListener {

        }

        includeButtonMonitoring.buttonView.setOnClickListener {

        }
    }

}
