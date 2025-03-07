package com.agvahealthcare.ventilator_ext.dashboard.trio_graph

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
import com.agvahealthcare.ventilator_ext.utility.*

class TrioFragmentGraph : GraphLayoutFragment("TrioGraphFragment") {

    private lateinit var pressureChartFragment: PressureChartFragment
    private lateinit var volumeChartFragment: VolumeChartFragment
    private lateinit var flowChartFragment: FlowChartFragment

    var mDashBoardViewModel: DashBoardViewModel? = null


    companion object {
        const val TAG = "TrioGraphFragment"
        private const val KEY_GRAPH_DATA = "KEY_GRAPH_DATA"

        fun newInstance(titleView: String?): TrioFragmentGraph {
            val args = Bundle()
            args.putString(KEY_GRAPH_DATA, titleView)
            val fragment = TrioFragmentGraph()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_trio_graph, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]

        VentilatorApp.testingDashBoardViewModel = mDashBoardViewModel

        // RM scichart
        mDashBoardViewModel?.isTouchGraph?.observe(viewLifecycleOwner){

            if (it){
                Log.i("swadaw123124124","131234")
                pressureChartFragment.setRollOver()
                flowChartFragment.setRollOver()
//                volumeChartFragment.setRollOver()
            }else{
                Log.i("swadaw123124124","1565768")
                pressureChartFragment.removeRollover()
                flowChartFragment.removeRollover()
//                volumeChartFragment.removeRollover()
            }
        }

        initData()
    }


    override fun onPause() {
       VentilatorApp.xTestingFlow =  IntArray(FIFOCAPACITY_CUSTOM_SIZE){i->0}
       VentilatorApp.xTestingVolume =  IntArray(FIFOCAPACITY_CUSTOM_SIZE){i->0}
       VentilatorApp.xTestingPressure =  IntArray(FIFOCAPACITY_CUSTOM_SIZE){i->0}
        super.onPause()
    }

    fun setDataGobally(xMaxRange: Double) {
        Log.i("valuesea", "2")
        volumeChartFragment.addTextOnMaxRange(xMaxRange)
        pressureChartFragment.addTextOnMaxRange(xMaxRange)
        flowChartFragment.addTextOnMaxRange(xMaxRange)
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
                R.id.containerTrioGraph1,
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
                R.id.containerTrioGraph2,
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
                R.id.containerTrioGraph3,
                flowChartFragment,
                flowChartFragment::class.java.javaClass.simpleName
            )
            .commit()
    }


    fun addGraphPressureData(x: Int, y: Float, trigger: String?) {
        pressureChartFragment.addEntry(x, y, trigger)
    }

    fun addGraphVolumeData(x: Int, y: Float) {
        volumeChartFragment.addEntry(x, y)
    }

    fun addGraphFlowData(x: Int, y: Float) {
        flowChartFragment.addEntry(x, y)
    }

    fun clearSeries() {
    }
}