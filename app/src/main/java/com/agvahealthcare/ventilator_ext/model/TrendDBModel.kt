package com.agvahealthcare.ventilator_ext.model

data class TrendDBModel(
    var mode :String = "",
    var pip: Float = 0f,
    var peep: Float = 0f,
    var meanAirway: Float = 0f,
    var vti: Float = 0f,
    var vte: Float = 0f,
    var mve: Float = 0f,
    var mvi: Float = 0f,
    var fio2: Float = 0f,
    var respiratoryRate: Float = 0f,
    var iE: Float = 0f,
    var tinsp: Float = 0f,
    var texp: Float = 0f,
    var averageLeak: Float = 0f,
    var spo2: Float = 0f,
    var pr: Float = 0f,

    )
