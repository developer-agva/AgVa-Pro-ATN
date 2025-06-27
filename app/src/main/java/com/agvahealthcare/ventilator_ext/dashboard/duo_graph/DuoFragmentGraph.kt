package com.agvahealthcare.ventilator_ext.dashboard.duo_graph

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.dashboard.GraphLayoutFragment
import com.agvahealthcare.ventilator_ext.dashboard.chart.*
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.*

class DuoFragmentGraph : GraphLayoutFragment("DuoGraphFragment") {

    private lateinit var pressureChartFragment: PressureChartFragment
    private lateinit var flowChartFragment: FlowChartFragment
    private var mDashBoardViewModel: DashBoardViewModel? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_duo_graph, container, false)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]

        VentilatorApp.testingDashBoardViewModel = mDashBoardViewModel

        // RM scichart
        mDashBoardViewModel?.isTouchGraph?.observe(viewLifecycleOwner) {

            if (PreferenceManager(requireContext()).readRolloverModifierStatus()) {

                if (it) {
                    pressureChartFragment.setRollOver()
                    flowChartFragment.setRollOver()
                } else {
                    pressureChartFragment.removeRollover()
                    flowChartFragment.removeRollover()
                }
            }
        }
        initData()
    }

    private fun initData() {
        initDuoGraph1()
        initDuoGraph2()
    }

    fun setDataGobally(xMaxRange: Double) {
        pressureChartFragment.addTextOnMaxRange(xMaxRange)
        flowChartFragment.addTextOnMaxRange(xMaxRange)
    }

    private fun initDuoGraph1() {

        pressureChartFragment = PressureChartFragment.newInstance(
            GraphType.PRESSURE,
            GRAPH_PRESSURE_MIN,
            GRAPH_PRESSURE_MAX,
        )

        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerDuoGraph1,
                pressureChartFragment,
                pressureChartFragment::class.java.javaClass.simpleName
            )
            .commit()
    }

    private fun initDuoGraph2() {
        flowChartFragment =
            FlowChartFragment.newInstance(GraphType.FLOW, GRAPH_FLOW_MIN, GRAPH_FLOW_MAX)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerDuoGraph2,
                flowChartFragment,
                flowChartFragment::class.java.javaClass.simpleName
            )
            .commit()
    }

    fun addGraphPressureData(x: Int, y: Float) {
        Log.i("PRESSURE_GRAPH", "x = $x , Y = $y")
        pressureChartFragment.addEntry(x, y)
    }


    fun addGraphVolumeData(x: Int, y: Float) {
        Log.i("VOLUME_GRAPH", "x = $x , Y = $y")
        // volumeChartFragment.addEntry(x, y)
    }

    fun addGraphFlowData(x: Int, y: Float) {
        flowChartFragment.addEntry(x, y)
    }

    fun clearSeries() {

    }

}