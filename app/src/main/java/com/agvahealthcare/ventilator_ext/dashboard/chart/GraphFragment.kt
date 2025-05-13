package com.agvahealthcare.ventilator_ext.dashboard.chart

import android.content.Context
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentXValue
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentYValue
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.dynCompTimeList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.fio2TimeList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.prTimeList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.spo2TimeList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.spontRRTimeList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.spontVTTimeList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.testingDashBoardViewModel
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.scichart.charting.model.RenderableSeriesCollection
import com.scichart.charting.modifiers.RolloverModifier
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries
import com.scichart.charting.visuals.renderableSeries.hitTest.DefaultXySeriesInfoProvider
import com.scichart.charting.visuals.renderableSeries.hitTest.XySeriesInfo
import com.scichart.charting.visuals.renderableSeries.tooltips.ISeriesTooltip
import com.scichart.charting.visuals.renderableSeries.tooltips.XySeriesTooltip
import com.scichart.core.observable.ObservableCollection
import com.scichart.core.utility.touch.ModifierTouchEventArgs
import com.scichart.drawing.utility.ColorUtil
import com.scichart.extensions.builders.SciChartBuilder

enum class GraphType {
    PRESSURE,
    VOLUME,
    FLOW,
    EtCo2,
    PRESSURE_VOLUME,
    FLOW_PRESSURE,
    FLOW_VOLUME,
    SPO2_CHART,
    FIO2_CHART,
    PR_CHART,
    DYNAMIC_COMP_CHART,
    SPONT_RR_CHART,
    SPONT_VT_CHART
}

enum class parentType{
    DivideTrioFragmentGraph,
    QuadFragmentGraph,
    TrioFragmentGraph,
    QuadTrendsFragment,
    DivideQuadFragmentGraph,
    DuoFragmentGraph,
    LoopsFragmentGraph,
    LinearQuadFragmentGraph,
    DefaultFragment
}
open class GraphFragment() : Fragment() {
    val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()
    var modifier = CustomRolloverModifier()

    inner class CustomSeriesInfoProvider(private var type: GraphType = GraphType.FLOW_PRESSURE) : DefaultXySeriesInfoProvider() {
        override fun getSeriesTooltipInternal(context: Context, seriesInfo: XySeriesInfo<*>?, modifierType: Class<*>): ISeriesTooltip {
            return CustomXySeriesTooltip(context, seriesInfo,type)
        }

        inner class CustomXySeriesTooltip(context: Context?, seriesInfo: XySeriesInfo<*>?,type: GraphType) : XySeriesTooltip(context, seriesInfo) {
            init {
                setPadding(10, 10, 10, 10)
            }

            override fun internalUpdate(seriesInfo: XySeriesInfo<*>) {


                when(type){
                    GraphType.PRESSURE -> {
                        text = "Pressure: ${seriesInfo.formattedYValue}"
                    }

                    GraphType.VOLUME -> {
                        text = "Volume: ${seriesInfo.formattedYValue}"
                    }

                    GraphType.FLOW -> {
                        text = "Flow: ${seriesInfo.formattedYValue}"
                    }

                    GraphType.SPO2_CHART -> {
                        text = "Time : ${spo2TimeList[seriesInfo.formattedXValue.toString().toInt()]},Value : ${seriesInfo.formattedYValue})"
                    }

                    GraphType.PR_CHART -> {
                        text = "Time : ${prTimeList[seriesInfo.formattedXValue.toString().toInt()]},Value : ${seriesInfo.formattedYValue})"
                    }

                    GraphType.FIO2_CHART -> {
                        text = "Time : ${fio2TimeList[seriesInfo.formattedXValue.toString().toInt()]},Value : ${seriesInfo.formattedYValue})"
                    }

                    GraphType.DYNAMIC_COMP_CHART -> {
                        text = "Time : ${dynCompTimeList[seriesInfo.formattedXValue.toString().toInt()]},Value : ${seriesInfo.formattedYValue})"
                    }

                    GraphType.SPONT_RR_CHART -> {
                        text = "Time : ${spontRRTimeList[seriesInfo.formattedXValue.toString().toInt()]},Value : ${seriesInfo.formattedYValue})"
                    }

                    GraphType.SPONT_VT_CHART -> {
                        text = "Time : ${spontVTTimeList[seriesInfo.formattedXValue.toString().toInt()]},Value : ${seriesInfo.formattedYValue})"
                    }
                    else -> {
                        text = "Testing: ${seriesInfo.formattedYValue}"
                    }
                }


                setTooltipBackgroundColor(ColorUtil.White)
                setTooltipStroke(ColorUtil.White)
                setTooltipTextColor(ColorUtil.Black)
            }
        }
    }

    inner class CustomRolloverModifier() : RolloverModifier() {

        override fun onTouchDown(args: ModifierTouchEventArgs?): Boolean {
            Log.i("asdawd","123123")
            VentilatorApp.isTouchGraph = true
            testingDashBoardViewModel?.isTouchGraph?.postValue(true)
            currentXValue = args?.e?.x?.toFloat() ?: 0.0f
            currentYValue = args?.e?.y?.toFloat() ?: 0.0f
            return true
        }

        override fun onTouchUp(args: ModifierTouchEventArgs?): Boolean {
            Log.i("asdawd","12312389")
            VentilatorApp.isTouchGraph = false
            testingDashBoardViewModel?.isTouchGraph?.postValue(false)
            currentXValue = args?.e?.x?.toFloat() ?: 0.0f
            currentYValue = args?.e?.y?.toFloat() ?: 0.0f
            return true
        }

        override fun onTouchMove(args: ModifierTouchEventArgs?): Boolean {
            Log.i("asdawd","12312315")
            VentilatorApp.isTouchGraph = true
            testingDashBoardViewModel?.isTouchGraph?.postValue(true)
            currentXValue = args?.e?.x?.toFloat() ?: 0.0f
            currentYValue = args?.e?.y?.toFloat() ?: 0.0f
            return true
        }

        fun setRolloverAt(xValue: Float, yValue: Float) {
            Log.i("valueTouch", "in roll over at")
            handleMasterTouchDownEvent(PointF(xValue, yValue))
        }

        fun removeRolloverAt(xValue: Float, yValue: Float) {
            handleMasterTouchUpEvent(PointF(xValue, yValue))
        }

        // hide x axis tooltip
        override fun getXAxesWithOverlays(): ObservableCollection<IAxis>? {
            return null
        }
    }

}