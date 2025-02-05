package com.agvahealthcare.ventilator_ext

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.agvahealthcare.ventilator_ext.service.UsbService
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import org.junit.Test

class Test2 {
    var dummydata= "ACK2000"
    lateinit var instrumentationContext: Context

    @Test
    fun `test usbread`(){
        val usbService:UsbService=UsbService()
        instrumentationContext = InstrumentationRegistry.getInstrumentation().getContext()

        val something=Configs.MessageFactory.getAckMessage(
            instrumentationContext,
            "ACK_0324",
            prefManager?.readLastVentMode()
        )
       println(something)
    }
}