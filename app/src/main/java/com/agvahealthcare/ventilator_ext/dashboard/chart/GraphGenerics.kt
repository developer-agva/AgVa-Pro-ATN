package com.agvahealthcare.ventilator_ext.dashboard.chart

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnChartSwapListener
import com.agvahealthcare.ventilator_ext.dashboard.TraceArc
import com.agvahealthcare.ventilator_ext.databinding.FragmentChartBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.FIFO_CAPACITY
import com.agvahealthcare.ventilator_ext.utility.GRAPH_THRESHOLD
import com.agvahealthcare.ventilator_ext.utility.KEY_MAX_VALUE_VIEW
import com.agvahealthcare.ventilator_ext.utility.KEY_MIN_VALUE_VIEW
import com.scichart.charting.model.dataSeries.IXyDataSeries
import com.scichart.charting.visuals.annotations.AnnotationCoordinateMode
import com.scichart.charting.visuals.annotations.HorizontalLineAnnotation
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.AxisAlignment
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries
import com.scichart.core.framework.UpdateSuspender
import com.scichart.data.model.DoubleRange
import com.scichart.drawing.common.FontStyle
import com.scichart.drawing.common.SolidPenStyle
import com.scichart.drawing.utility.ColorUtil
import java.util.*

class GraphGenerics  : GraphFragment() {

    companion object {

        fun newInstance(
            type: GraphType,
            minValue: Int,
            maxValue: Int,
//            onChartSelectListener: ChartOptionListener? = null
        ): PressureChartFragment {
            val args = Bundle()
            args.putInt(KEY_MIN_VALUE_VIEW, minValue)
            args.putInt(KEY_MAX_VALUE_VIEW, maxValue)

            val fragment = PressureChartFragment()
            fragment.arguments = args
//            fragment.onChartSelectListener = onChartSelectListener
            return fragment
        }
    }

    private var onChartSelectListener: OnChartSwapListener? = null

    private lateinit var dataSeries0: IXyDataSeries<Int, Float>
    private lateinit var dataSeries1: IXyDataSeries<Int, Float>
    private var whichTrace = TraceArc.TraceA
    private var minValue: Int? = 0
    private var maxValue: Int? = 0
    private var cc: ColourContainer? = null
    private var prefManager: PreferenceManager? = null
    var horizontalLineAnnotation: HorizontalLineAnnotation? = null
    val titleStyle = FontStyle(14.0f, ColorUtil.White)


    private lateinit var binding : FragmentChartBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChartBinding.inflate(layoutInflater,container,false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        minValue = arguments?.getInt(KEY_MIN_VALUE_VIEW)
        maxValue = arguments?.getInt(KEY_MAX_VALUE_VIEW)
        prefManager = PreferenceManager(requireContext())
        horizontalLineAnnotation= HorizontalLineAnnotation(requireContext())

        initGraph()

    }

    private fun initGraph() {

        val xPRimaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, GRAPH_THRESHOLD.toDouble()))
            .withMaxAutoTicks(4)
            .withTickLabelStyle(titleStyle)
            .withAxisId("Visible Axis")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        val xsecondaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, 12.9))
            .withTickLabelStyle(titleStyle)
            .withMaxAutoTicks(8)
            .withAxisId("HiddenXAxis")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withMaxAutoTicks(3)
            .withTickLabelStyle(titleStyle)
            .withAutoRangeMode(AutoRange.Never)
            .withVisibleRange(0.0, 60.0)
            .build()


        binding.chartSurface.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.chartSurface.renderableSeriesAreaBorderStyle = sciChartBuilder.newPen().withColor(ColorUtil.Transparent).build();
        xPRimaryAxis.visibility = View.GONE
        xsecondaryAxis.visibility = View.VISIBLE


        xPRimaryAxis.drawMajorGridLines = false
        xPRimaryAxis.drawMinorGridLines = false
        xPRimaryAxis.drawMajorBands = false
        xPRimaryAxis.drawMajorTicks = false
        xPRimaryAxis.drawMinorTicks = false


        yAxis.drawMajorGridLines = false
        yAxis.drawMinorGridLines = false
        yAxis.drawMajorBands = false
        yAxis.drawMajorTicks = false
        yAxis.drawMajorTicks = false


        dataSeries0 = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withFifoCapacity(FIFO_CAPACITY).build()


        dataSeries1 = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withFifoCapacity(FIFO_CAPACITY).build()


        horizontalLineAnnotation?.stroke = SolidPenStyle(ColorUtil.Red, false, 1.0f, floatArrayOf(0f, 0f))


        val rs1: IRenderableSeries = sciChartBuilder.newLineSeries()
            .withStrokeStyle(sciChartBuilder.newPen().withColor(ColorUtil.White).withThickness(1f).build())
            .withXAxisId("Visible Axis")
            .build()

        val rs2: IRenderableSeries = sciChartBuilder.newLineSeries()
            .withStrokeStyle(sciChartBuilder.newPen().withColor(
                ColorUtil.White).withThickness(1f).build())
            .withDataSeries(dataSeries1)
            .withXAxisId("Visible Axis")
            .build()

        // draw desired border using LineAnnotation
        val verticalLine = sciChartBuilder.newLineAnnotation()
            .withPosition(0.0, 0.0, 0.0, 1.0)
            .withCoordinateMode(AnnotationCoordinateMode.Relative)
            .withStroke(1f, Color.WHITE)
            .build()

        val horizontalLine = sciChartBuilder.newLineAnnotation()
            .withPosition(0.0, 1.0, 1.0, 1.0)
            .withCoordinateMode(AnnotationCoordinateMode.Relative)
            .withStroke(1f, Color.WHITE)
            .build()


        Log.i("Horizantlelinecheck","Horizantal Line")

        Collections.addAll(binding.chartSurface.annotations, horizontalLine, verticalLine)


        UpdateSuspender.using(binding.chartSurface) {
            Collections.addAll(binding.chartSurface.xAxes, xPRimaryAxis)
            Collections.addAll(binding.chartSurface.xAxes,xsecondaryAxis)
            Collections.addAll(binding.chartSurface.yAxes, yAxis)
            Collections.addAll(binding.chartSurface.renderableSeries, rs1,rs2)
            binding.chartSurface.annotations.add(horizontalLineAnnotation)

        }

    }

    fun addEntry(x: Int, y: Float) {
        val xAxis = x % (GRAPH_THRESHOLD + 1)

        if (whichTrace == TraceArc.TraceA) {
            dataSeries0.append(xAxis, y)
            dataSeries1.append(xAxis, Float.NaN)

        } else {
            dataSeries0.append(xAxis, Float.NaN)
            dataSeries1.append(xAxis, y)
        }

        if (xAxis % GRAPH_THRESHOLD == 0) {
            whichTrace = if (whichTrace == TraceArc.TraceA) TraceArc.TraceB else TraceArc.TraceA
        }
    }

    fun minimumAndMaximumRange(graphType:String): Pair<Double, Double>? {
when(graphType) {
    "PRESSURE" -> {
        return Pair(20.0,60.0)

    }
    "Flow" -> {
        return Pair(-100.0,100.0)
    }
    "Volume" -> {
        return Pair(100.0,800.0)
    }
}
         return Pair(10.0,60.0)

    }


}