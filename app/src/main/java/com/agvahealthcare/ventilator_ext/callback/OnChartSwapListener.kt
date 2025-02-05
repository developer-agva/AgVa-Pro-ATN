package com.agvahealthcare.ventilator_ext.callback

import com.agvahealthcare.ventilator_ext.dashboard.chart.GraphType

interface OnChartSwapListener {
    fun onSwapChart(olderChartType: GraphType, newChartType: GraphType)
}