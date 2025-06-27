package com.agvahealthcare.ventilator_ext.dashboard.quad_graph

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


class DivideQuadFragmentGraph : GraphLayoutFragment("DivideQuadGraphFragment"){

    private lateinit var graphgenerics: GraphGenerics
    private lateinit var pressureChartFragment:  PressureChartFragment
    private lateinit var volumeChartFragment:  VolumeChartFragment
    private lateinit var flowChartFragment:  FlowChartFragment
    private lateinit var flowVolumeChartFragment:  FlowVolumeChartFragment
    private lateinit var flowPressureChartFragment:  FlowPressureChartFragment
    private lateinit var pressureVolumeChartFragment:  PressureVolumeChartFragment


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_divide_quad_graph, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()

    }

    private fun initData() {
        initDivideQuadGraph1()
        initDivideQuadGraph2()
        initDivideQuadGraph3()
        initDivideQuadGraph4()

    }

    fun setDataGobally(xMaxRange:Double){
        volumeChartFragment.addTextOnMaxRange(xMaxRange)
        pressureChartFragment.addTextOnMaxRange(xMaxRange)
        flowChartFragment.addTextOnMaxRange(xMaxRange)
    }

    private fun initDivideQuadGraph1() {

        pressureChartFragment= PressureChartFragment.newInstance(GraphType.PRESSURE,
            GRAPH_PRESSURE_MIN,
            GRAPH_PRESSURE_MAX)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerDivideQuadGraph1,
                pressureChartFragment,
                pressureChartFragment::class.java.javaClass.simpleName)
            .commit()
    }

    private fun initDivideQuadGraph2() {
        volumeChartFragment = VolumeChartFragment.newInstance(GraphType.VOLUME,
            GRAPH_VOLUME_MIN,
            GRAPH_VOLUME_MAX)
        childFragmentManager.beginTransaction()
            .replace(R.id.containerDivideQuadGraph2, volumeChartFragment, volumeChartFragment::class.java.javaClass.simpleName)
            .commit()
    }

    private fun initDivideQuadGraph3() {
        flowChartFragment = FlowChartFragment.newInstance(
            GraphType.FLOW,GRAPH_FLOW_MIN, GRAPH_FLOW_MAX)
        childFragmentManager.beginTransaction()
            .replace(R.id.containerDivideQuadGraph3,
                flowChartFragment,
                flowChartFragment::class.java.javaClass.simpleName)
            .commit()
    }


    private fun initDivideQuadGraph4() {
        flowVolumeChartFragment = FlowVolumeChartFragment.newInstance(GraphType.FLOW_VOLUME)
        childFragmentManager.beginTransaction()
            .replace(R.id.containerDivideQuadGraph4,
                flowVolumeChartFragment,
                flowVolumeChartFragment::class.java.javaClass.simpleName)
            .commit()
    }



    fun addGraphPressureData(x: Int, y: Float) {
        Log.i("PRESSURE_GRAPH", "x = $x , Y = $y")
        pressureChartFragment.addEntry(x, y)
    }


    fun addGraphVolumeData(x: Int, y: Float ) {
        Log.i("VOLUME_GRAPH", "x = $x , Y = $y")
        volumeChartFragment.addEntry(x, y)
    }

    fun addGraphFlowData(x: Int, y: Float) {
        Log.i("FLOW_GRAPH", "x = $x , Y = $y")
        flowChartFragment.addEntry(x, y)

    }

    fun clearSeries(){


    }

    fun addGraphPressureVolumeData(x: Float?, y: Float?, isRedrawRequired: Boolean) {
        if(isRedrawRequired) pressureVolumeChartFragment.clearSeries()
        else{
            if(x != null && y != null) pressureVolumeChartFragment.addEntry(x, y)
            Log.i("DIVIDE_QUAD_LOOP_GRAPH", "PV Value Of x = $x , Y = $y")

        }
    }

    fun addGraphFlowVolumeData(x: Float?, y: Float?, isRedrawRequired: Boolean) {
        if(isRedrawRequired) flowVolumeChartFragment.clearSeries()
        else{
            if(x != null && y != null) flowVolumeChartFragment.addEntry(x, y)
            Log.i("DIVIDE_QUAD_LOOP_GRAPH", "FV Value Of x = $x , Y = $y")
        }
    }

    fun addGraphFlowPressureData(x: Float?, y: Float?, isRedrawRequired: Boolean) {

        if(isRedrawRequired) flowPressureChartFragment.clearSeries()
        else{
            if(x != null && y != null) flowPressureChartFragment.addEntry(x, y)
            Log.i("DIVIDE_QUAD_LOOP_GRAPH", "FP Value Of x = $x , Y = $y")
        }
    }

}