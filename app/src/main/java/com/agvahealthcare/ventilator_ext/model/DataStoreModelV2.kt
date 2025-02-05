package com.agvahealthcare.ventilator_ext.model

import java.text.SimpleDateFormat

class DataStoreModelV2(
    var id: Int,
    var pressure: Float,
    var volume: Float,
    var rr: Float,
    var fio2: Float,
    var mve: Float,
    var vte: Float,
    var leak: Float,
    var peep: Float,
    var ieRatio: Float,
    var time: String,
){
    companion object{
        private val dateTimerFormatter = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
    }

    operator fun plus(o: DataStoreModelV2) : DataStoreModelV2{
        val dateDifference =
            dateTimerFormatter.parse(this.time)?.compareTo( dateTimerFormatter.parse(o.time)) ?: -1
        return DataStoreModelV2(
            id = Math.min(this.id, o.id),
            time = if( dateDifference > 0 ) this.time else o.time,
            pressure = this.pressure + o.pressure,
            volume = this.volume + o.volume,
            rr = this.rr + o.rr,
            fio2 = this.fio2 + o.fio2,
            mve = this.mve + o.mve,
            vte = this.vte + o.vte,
            leak = this.leak + o.leak,
            peep = this.peep + o.peep,
            ieRatio = this.ieRatio + o.ieRatio
        // TODO
        );
    }

}