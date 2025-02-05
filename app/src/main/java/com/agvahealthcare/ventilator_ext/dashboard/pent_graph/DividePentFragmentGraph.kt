package com.agvahealthcare.ventilator_ext.dashboard.pent_graph

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.dashboard.GraphLayoutFragment
import com.agvahealthcare.ventilator_ext.dashboard.chart.*
import com.agvahealthcare.ventilator_ext.utility.*

class DividePentFragmentGraph : GraphLayoutFragment("DividePentGraphFragment") {

    private lateinit var pressureChartFragment: PressureChartFragment
    private lateinit var volumeChartFragment: VolumeChartFragment
    private lateinit var flowChartFragment: FlowChartFragment
    private lateinit var flowVolumeChartFragment: FlowVolumeChartFragment
    private lateinit var flowPressureChartFragment: FlowPressureChartFragment
    private lateinit var pressureVolumeChartFragment: PressureVolumeChartFragment

    companion object {
        const val TAG = "DividePentGraphFragment"
        private const val KEY_GRAPH_DATA = "KEY_GRAPH_DATA"

        fun newInstance(titleView: String?): DividePentFragmentGraph {
            val args = Bundle()
            args.putString(KEY_GRAPH_DATA, titleView)
            val fragment = DividePentFragmentGraph()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_divide_pent_graph, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }



}