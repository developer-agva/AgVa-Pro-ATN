package com.agvahealthcare.ventilator_ext

import com.agvahealthcare.ventilator_ext.model.DataStoreModelV2
import com.agvahealthcare.ventilator_ext.utility.avgDataStoreModels
import org.junit.Before
import org.junit.Test
import java.util.*

class Test1 {

    var dummy = listOf<DataStoreModelV2>()
    @Before
    fun createDummyData(){
        dummy = listOf(
            DataStoreModelV2(
                id = 1,
                pressure = 26f,
                volume = 400f,
                rr = 16f,
                fio2 = 21f,
                mve = 50f,
                vte = 400f,
                leak = 10f,
                peep = 7f,
                ieRatio = 21f,
                time = Date().toString()
            )
            ,

            DataStoreModelV2(
                id = 56,
                pressure = 16f,
                volume = 100f,
                rr = 14f,
                fio2 = 19f,
                mve = 30f,
                vte = 100f,
                leak = 20f,
                peep = 13f,
                ieRatio = 21f,
                time = Date().toString()
            ),
            DataStoreModelV2(
                id = 56,
                pressure = 16f,
                volume = 100f,
                rr = 14f,
                fio2 = 19f,
                mve = 30f,
                vte = 100f,
                leak = 20f,
                peep = 13f,
                ieRatio = 21f,
                time = Date().toString()
            ),
            DataStoreModelV2(
                id = 56,
                pressure = 16f,
                volume = 100f,
                rr = 14f,
                fio2 = 19f,
                mve = 30f,
                vte = 100f,
                leak = 20f,
                peep = 13f,
                ieRatio = 21f,
                time = Date().toString()
            )
        )
            
        
    }
    
    @Test
    fun `test avg`(){
        
        val model = avgDataStoreModels(dummy)
        assert( model.id == 1
                && model.pressure == 21f
                && model.volume == 250f
                && model.rr == 15f
                && model.fio2 == 22f
                && model.mve == 40f
                && model.vte == 250f
        )
    }
}