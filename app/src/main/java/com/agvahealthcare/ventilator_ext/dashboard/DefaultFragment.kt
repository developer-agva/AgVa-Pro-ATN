package com.agvahealthcare.ventilator_ext.dashboard

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.dashboard.chart.*
import com.agvahealthcare.ventilator_ext.dashboard.trio_graph.TrioFragmentGraph
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.fragment_default.*
import kotlinx.android.synthetic.main.layout_dialog_calibration.*

// Created by masoom on 02 Jan 2023
class DefaultFragment : GraphLayoutFragment("DefaultFragment") {

    private lateinit var pressureChartFragment: PressureChartFragment
    private lateinit var volumeChartFragment: VolumeChartFragment
    private lateinit var flowChartFragment: FlowChartFragment
    private var prefManager:PreferenceManager ?= null

    companion object {
        const val TAG = "DefaultFragment"
        private const val KEY_GRAPH_DATA = "KEY_GRAPH_DATA"

        fun newInstance(titleView: String?): DefaultFragment {
            val args = Bundle()
            args.putString(KEY_GRAPH_DATA, titleView)
            val fragment = DefaultFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_default, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(context)
        if(prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT) tvHFNC.text = getString(R.string.NeoNatehfnc) else tvHFNC.text = getString(R.string.hfnc)
    }

    private fun initData() {
        initTrioGraph1()
        initTrioGraph2()
        initTrioGraph3()
    }

    private fun initTrioGraph1() {
        pressureChartFragment = PressureChartFragment.newInstance(
            GraphType.PRESSURE,
            GRAPH_PRESSURE_MIN,
            GRAPH_PRESSURE_MAX
        )
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrioGraph12,
                pressureChartFragment,
                pressureChartFragment::class.java.javaClass.simpleName
            )
            .commit()
    }

    private fun initTrioGraph2() {
        volumeChartFragment =
            VolumeChartFragment.newInstance(GraphType.VOLUME, GRAPH_VOLUME_MIN, GRAPH_VOLUME_MAX)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrioGraph22,
                volumeChartFragment,
                volumeChartFragment::class.java.javaClass.simpleName
            )
            .commit()
    }

    private fun initTrioGraph3() {
        flowChartFragment =
            FlowChartFragment.newInstance(GraphType.FLOW, TRIO_GRAPH_FLOW_MIN, TRIO_GRAPH_FLOW_MAX)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrioGraph32,
                flowChartFragment,
                flowChartFragment::class.java.javaClass.simpleName
            )
            .commit()
    }

    fun addGraphPressureData(x: Int, y: Float) {
//         pressureChartFragment.addEntry(x, y)
    }

    fun addGraphVolumeData(x: Int, y: Float) {
//        volumeChartFragment.addEntry(x, y)
    }

    fun addGraphFlowData(x: Int, y: Float) {
//        flowChartFragment.addEntry(x, y)
    }

    fun clearSeries() {
    }

}