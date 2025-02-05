package com.agvahealthcare.ventilator_ext.dashboard.chart

import com.scichart.core.model.IntegerValues
import java.io.Serializable

class ColourContainer: Serializable {
    private var colours: IntegerValues = IntegerValues()

    fun get() = colours
    fun add(value: Int) = colours.add(value)

}