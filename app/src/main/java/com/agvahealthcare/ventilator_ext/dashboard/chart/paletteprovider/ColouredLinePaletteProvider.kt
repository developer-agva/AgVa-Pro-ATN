package com.agvahealthcare.ventilator_ext.dashboard.chart.paletteprovider

import android.util.Log
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.xValueManualTriggerList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.xValuePatientTriggerList
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.scichart.charting.visuals.renderableSeries.XyRenderableSeriesBase
import com.scichart.charting.visuals.renderableSeries.data.XyRenderPassData
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IFillPaletteProvider
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IStrokePaletteProvider
import com.scichart.charting.visuals.renderableSeries.paletteProviders.PaletteProviderBase
import com.scichart.core.model.IntegerValues
import com.scichart.drawing.utility.ColorUtil


class ColouredLinePaletteProvider(
    private var prefManager: PreferenceManager?
) : PaletteProviderBase<XyRenderableSeriesBase>(XyRenderableSeriesBase::class.java),
    IStrokePaletteProvider,IFillPaletteProvider {

    private val colorValues = IntegerValues()
    // Update function for the Color values of the graph associated with pressure.
    override fun update() {
        // to update the data

        val renderableSeries: XyRenderableSeriesBase = renderableSeries
        val currentRenderPassData = renderableSeries.currentRenderPassData as XyRenderPassData
        val xValues = currentRenderPassData.xValues
        val size = currentRenderPassData.pointsCount()
        colorValues.setSize(size)
        val colorsArray: IntArray = colorValues.itemsArray
        val valuesArray = xValues.itemsArray

        for (i in 0 until size){

            // RM scichart
            if (VentilatorApp.xTestingPressure.contains(valuesArray[i].toInt())){
                colorsArray[i] = ColorUtil.Black
            }else {

                if (xValuePatientTriggerList.contains(valuesArray[i])){
                    colorsArray[i] = prefManager?.readCurrentGraphColor("PRESSURE")!!
                }
                else if(xValueManualTriggerList.contains(valuesArray[i])){
                    colorsArray[i] = ColorUtil.Brown
                }
                else {
                    colorsArray[i] = ColorUtil.White
                }
            }
        }
    }

    override fun getFillColors(): IntegerValues {
        return colorValues
    }

    override fun getStrokeColors(): IntegerValues {
        return colorValues
    }



}