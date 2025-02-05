package com.agvahealthcare.ventilator_ext.dashboard.chart

import androidx.fragment.app.Fragment
import com.scichart.extensions.builders.SciChartBuilder

open class BaseFragment : Fragment() {
    val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()

}