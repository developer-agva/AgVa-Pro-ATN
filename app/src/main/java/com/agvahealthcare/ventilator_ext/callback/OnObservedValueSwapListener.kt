package com.agvahealthcare.ventilator_ext.callback
import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel

interface OnObserveValueSwapListener {
    fun onSwap(containerModel: ObservedParameterModel, desiredModel: ObservedParameterModel)
}