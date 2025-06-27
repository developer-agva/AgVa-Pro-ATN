package com.agvahealthcare.ventilator_ext.dashboard.linear_quad

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
import kotlinx.android.synthetic.main.fragment_chart.*

class LinearQuadGraphFragment : GraphLayoutFragment("LinearQuadGraphFragment") {

    private lateinit var pressureChartFragment: PressureChartFragment
    private lateinit var etco2ChartFragment: Etco2ChartFragment
    private lateinit var volumeChartFragment: VolumeChartFragment
    private lateinit var flowChartFragment: FlowChartFragment

    var mDashBoardViewModel: DashBoardViewModel? = null


    companion object {
        const val TAG = "LinearQuadGraphFragment"
        private const val KEY_GRAPH_DATA = "KEY_GRAPH_DATA"

        fun newInstance(titleView: String?): LinearQuadGraphFragment {
            val args = Bundle()
            args.putString(KEY_GRAPH_DATA, titleView)
            val fragment = LinearQuadGraphFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_linear_quad_graph, container, false)
        return view
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
                    etco2ChartFragment.setRollOver()
                } else {
                    pressureChartFragment.removeRollover()
                    flowChartFragment.removeRollover()
                    etco2ChartFragment.removeRollover()
                }
            }
        }

        initData()
    }

    // RM scichart
    override fun onPause() {
        VentilatorApp.xTestingFlow = IntArray(FIFOCAPACITY_CUSTOM_SIZE) { i -> 0 }
        VentilatorApp.xTestingVolume = IntArray(FIFOCAPACITY_CUSTOM_SIZE) { i -> 0 }
        VentilatorApp.xTestingPressure = IntArray(FIFOCAPACITY_CUSTOM_SIZE) { i -> 0 }
        VentilatorApp.xTestingEtCo2 = IntArray(FIFOCAPACITY_CUSTOM_SIZE) { i -> 0 }

        super.onPause()
    }

    fun setDataGobally(xMaxRange: Double) {
        Log.i("valuesea", "2")
        volumeChartFragment.addTextOnMaxRange(xMaxRange)
        pressureChartFragment.addTextOnMaxRange(xMaxRange)
        flowChartFragment.addTextOnMaxRange(xMaxRange)
        etco2ChartFragment.addTextOnMaxRange(xMaxRange)
    }


    private fun initData() {
        initTrioGraph1()
        initTrioGraph2()
        initTrioGraph3()
        initTrioGraph4()
    }


    private fun initTrioGraph2() {
        pressureChartFragment = PressureChartFragment.newInstance(
            GraphType.PRESSURE,
            GRAPH_PRESSURE_MIN,
            GRAPH_PRESSURE_MAX
        )
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrioGraph2,
                pressureChartFragment,
                pressureChartFragment::class.java.javaClass.simpleName
            )
            .commit()
    }

    private fun initTrioGraph3() {
        volumeChartFragment =
            VolumeChartFragment.newInstance(GraphType.VOLUME, GRAPH_VOLUME_MIN, GRAPH_VOLUME_MAX)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrioGraph3,
                volumeChartFragment,
                volumeChartFragment::class.java.javaClass.simpleName
            )
            .commit()

    }

    private fun initTrioGraph4() {
        flowChartFragment =
            FlowChartFragment.newInstance(GraphType.FLOW, TRIO_GRAPH_FLOW_MIN, TRIO_GRAPH_FLOW_MAX)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrioGraph4,
                flowChartFragment,
                flowChartFragment::class.java.javaClass.simpleName
            )
            .commit()
    }

    private fun initTrioGraph1() {
        etco2ChartFragment = Etco2ChartFragment.newInstance(
            GraphType.EtCo2,
            GRAPH_PRESSURE_MIN,
            GRAPH_PRESSURE_MAX
        )
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrioGraph1,
                etco2ChartFragment,
                etco2ChartFragment::class.java.javaClass.simpleName
            )
            .commit()
    }


    fun addGraphPressureData(x: Int, y: Float) {
        pressureChartFragment.addEntry(x, y)
    }

    fun addGraphVolumeData(x: Int, y: Float) {
        volumeChartFragment.addEntry(x, y)
    }

    fun addGraphFlowData(x: Int, y: Float) {
        flowChartFragment.addEntry(x, y)
    }

    fun addEtCo2GraphData(x: Int, y: Float) {
        etco2ChartFragment.addEntry(x, y)
    }


    fun clearSeries() {
    }
}