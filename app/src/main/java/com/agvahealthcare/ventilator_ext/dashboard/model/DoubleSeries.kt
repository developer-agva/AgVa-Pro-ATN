package com.agvahealthcare.ventilator_ext.dashboard.model

import com.scichart.core.model.DoubleValues

class DoubleSeries(count: Int) {

    var xValues = DoubleValues()
    var yValues = DoubleValues()

    fun add(x: Double, y: Double) {
        xValues.add(x)
        yValues.add(y)
    }
}