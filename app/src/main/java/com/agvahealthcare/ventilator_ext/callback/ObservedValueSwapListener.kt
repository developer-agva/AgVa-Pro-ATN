package com.agvahealthcare.ventilator_ext.callback

import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel

interface ObserveValueSwapListener {
    fun onSwap(containerModel: ObservedParameterModel, desiredModel: ObservedParameterModel)
}