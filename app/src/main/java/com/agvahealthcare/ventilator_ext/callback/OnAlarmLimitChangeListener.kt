package com.agvahealthcare.ventilator_ext.callback

interface OnAlarmLimitChangeListener {
    fun onChangeAlarmLimit(currentKey: String?, lowerLimit: Float, upperLimit: Float?)

}

