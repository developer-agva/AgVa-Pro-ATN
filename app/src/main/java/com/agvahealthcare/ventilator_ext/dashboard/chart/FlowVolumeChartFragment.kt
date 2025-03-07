package com.agvahealthcare.ventilator_ext.dashboard.chart

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.databinding.FragmentChartBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.scichart.charting.layoutManagers.DefaultLayoutManager
import com.scichart.charting.layoutManagers.ILayoutManager
import com.scichart.charting.layoutManagers.LeftAlignmentInnerAxisLayoutStrategy
import com.scichart.charting.layoutManagers.TopAlignmentInnerAxisLayoutStrategy
import com.scichart.charting.model.dataSeries.IXyDataSeries
import com.scichart.charting.modifiers.RolloverModifier
import com.scichart.charting.modifiers.ZoomPanModifier
import com.scichart.charting.visuals.annotations.AnnotationCoordinateMode
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.AxisAlignment
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.renderableSeries.XyRenderableSeriesBase
import com.scichart.charting.visuals.renderableSeries.paletteProviders.IStrokePaletteProvider
import com.scichart.charting.visuals.renderableSeries.paletteProviders.PaletteProviderBase
import com.scichart.core.IServiceContainer
import com.scichart.core.common.Size
import com.scichart.core.framework.UpdateSuspender
import com.scichart.core.model.IntegerValues
import com.scichart.data.model.DoubleRange
import com.scichart.drawing.common.FontStyle
import com.scichart.drawing.utility.ColorUtil
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class FlowVolumeChartFragment  : GraphFragment() {

    private lateinit var dataSeries: IXyDataSeries<Float, Float>
    private lateinit var dataSeries1: IXyDataSeries<Float, Float>
    val titleStyle = FontStyle(14.0f, ColorUtil.White)

    companion object {
        const val TAG = "FlowVolumeChartFragment"

        fun newInstance(
            type: GraphType
        ): FlowVolumeChartFragment {
            val args = Bundle()
            val fragment = FlowVolumeChartFragment()
            fragment.arguments = args
            return fragment
        }
    }

    // var for graph
    private var xMinRange : Double = 0.0
    private var xMaxRange : Double = 0.0
    private var yMinRange : Double = 0.0
    private var yMaxRange : Double = 0.0
    private var yMinValue = 0.0f
    private var yMaxValue = Float.MIN_VALUE
    private var xMaxValue = Float.MIN_VALUE
    private var prefManager : PreferenceManager? = null
    private var mDashBoardViewModel : DashBoardViewModel? = null


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

        prefManager = PreferenceManager(requireContext())
        mDashBoardViewModel = ViewModelProvider(requireActivity()).get(DashBoardViewModel::class.java)
        binding.textViewChartType.text = requireContext().getString(R.string.flow_vol_l_min)

        xMinRange =
            Configs.getRangeOfYAxisChartLoops(context, Configs.LoopsChartType.FlowVolumeChart_Type).first.first
        xMaxRange =
            Configs.getRangeOfYAxisChartLoops(context, Configs.LoopsChartType.FlowVolumeChart_Type).first.second

        yMinRange = Configs.getRangeOfYAxisChartLoops(context, Configs.LoopsChartType.FlowVolumeChart_Type).second.first
        yMaxRange = Configs.getRangeOfYAxisChartLoops(context, Configs.LoopsChartType.FlowVolumeChart_Type).second.second
        initGraph()

    }


    private fun initGraph() {



        val zoomPanModifier = ZoomPanModifier()
        val rolloverModifier = RolloverModifier()
        zoomPanModifier.isEnabled=false
        rolloverModifier.isEnabled=false
        val xAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(  xMinRange,
                xMaxRange)
            .withMaxAutoTicks(10)
            .withTickLabelStyle(titleStyle)
            .withDrawMajorGridLines(false)
            .withAutoRangeMode(AutoRange.Never).build()

        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(
                yMinRange,
                yMaxRange)
            .withAxisAlignment(AxisAlignment.Left)
            .withMaxAutoTicks(10)
            .withDrawMajorGridLines(false)
            .withAutoRangeMode(AutoRange.Never).build()
        // horizontalLine.y1= 80.0


        xAxis.drawMajorGridLines = false
        xAxis.drawMinorGridLines = false
        xAxis.drawMajorBands = false
        xAxis.drawMajorTicks = true
        xAxis.drawMinorTicks = true

        yAxis.drawMajorGridLines = true
        yAxis.drawMinorGridLines = false
        yAxis.drawMajorBands = false
        yAxis.drawMajorTicks = false
        yAxis.drawMajorTicks = false

        binding.chartSurface.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.chartSurface.isHorizontalScrollBarEnabled=false
        binding.chartSurface.isVerticalFadingEdgeEnabled=false
        binding.chartSurface.isClickable=false
        binding.chartSurface.renderableSeriesAreaBorderStyle = sciChartBuilder.newPen().withColor(ColorUtil.Transparent).build();

        dataSeries = sciChartBuilder.newXyDataSeries(
            Float::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()
        dataSeries1 = sciChartBuilder.newXyDataSeries(
            Float::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()


        val rSeries = sciChartBuilder.newLineSeries().withDataSeries(dataSeries)
            .withStrokeStyle(sciChartBuilder.newPen().withColor(ColorUtil.White).withThickness(2f).build())
            .build()

        val rSeries1 = sciChartBuilder.newLineSeries().withDataSeries(dataSeries1)
            .withStrokeStyle(sciChartBuilder.newPen().withColor(ColorUtil.White).withThickness(2f).build())
            .withPaletteProvider(DimTracePaletteProvider())
            .build()

        val verticalLine = sciChartBuilder.newLineAnnotation()
            .withPosition(0.0, 0.0, 0.0, 1.0)
            .withCoordinateMode(AnnotationCoordinateMode.Relative)
            .withStroke(1f, Color.TRANSPARENT)
            .build()

        val horizontalLine = sciChartBuilder.newLineAnnotation()
            .withPosition(0.0, 1.0, 1.0, 1.0)
            .withCoordinateMode(AnnotationCoordinateMode.Relative)
            .withStroke(1f, Color.TRANSPARENT)
            .build()

        val modifier = RolloverModifier()
        modifier.showTooltip
        modifier.showAxisLabels

        Log.i("Horizantlelinecheck","Horizantal Line")

        Collections.addAll(binding.chartSurface.annotations, horizontalLine, verticalLine)
        Collections.addAll(binding.chartSurface.getChartModifiers(), RolloverModifier())

        UpdateSuspender.using(binding.chartSurface) {

            binding.chartSurface.layoutManager = CenterLayoutManager(xAxis, yAxis)
            Collections.addAll(binding.chartSurface.xAxes, xAxis)
            Collections.addAll(binding.chartSurface.yAxes, yAxis)
            Collections.addAll(binding.chartSurface.renderableSeries, rSeries,rSeries1)
        }


    }

    fun clearSeries() {

        dataSeries1.clear()
        for (i in 0 until dataSeries.count){
            dataSeries1.append(dataSeries.xValues[i],dataSeries.yValues[i])
        }
        dataSeries.clear()
    }

    private fun changeRangeScale(y: Float, x: Float){


        if (mDashBoardViewModel?.graphPeekValue?.value == "D"){

            Log.i("FlowValue","$yMinValue , $yMaxValue")

            filterVolumeGraphValue(xMaxValue)

            if (yMaxValue > abs(yMinValue)){
                filterFlowGraphPositiveValue(yMaxValue)
                yMinRange = -Math.abs(yMaxRange)
            }else{
                filterFlowGraphNegativeValue(yMinValue)
                yMaxRange = abs(yMinRange)
            }

            xMaxValue = Float.MIN_VALUE
            yMaxValue = Float.MIN_VALUE
            yMinValue = 0.0f

            binding.chartSurface.yAxes.default.visibleRange = DoubleRange(yMinRange,yMaxRange)
            binding.chartSurface.xAxes.default.visibleRange = DoubleRange(xMinRange,xMaxRange)

        }else {
            if (mDashBoardViewModel?.graphPeekValue?.value == "A"){
                if (x > 0) xMaxValue = max(xMaxValue,x)
                if (y > 0) yMaxValue = max(yMaxValue,y)
            }
            if (mDashBoardViewModel?.graphPeekValue?.value == "C") yMinValue = min(yMinValue,y)
        }
    }

    // created at 31 jan 2023
    private fun filterVolumeGraphValue(x: Float){
        if (prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT){

            if (x < MIN_RANGE_VOLUME_NEO_LOOPS) xMaxRange = MIN_RANGE_VOLUME_NEO_LOOPS
            else xMaxRange = MAX_RANGE_VOLUME_NEO_LOOPS
        }
        else{
            if (x < 600.0) xMaxRange = MIN_RANGE_VOLUME_ADULT_PEDIA_LOOPS
            else xMaxRange = MAX_RANGE_VOLUME_ADULT_PEDIA_LOOPS
        }
    }

    // created at 31 jan 2023
    private fun filterFlowGraphPositiveValue(yMaxValue:Float){
        if (prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT) {

            if (yMaxValue < POSITIVE_MIN_RANGE_FLOW_NEO_LOOPS) {
                yMaxRange = POSITIVE_MIN_RANGE_FLOW_NEO_LOOPS
            }else {
                yMaxRange = POSITIVE_MAX_RANGE_FLOW_NEO_LOOPS
            }
        }
        else {

            if (yMaxValue < 80.0) {
                yMaxRange = POSITIVE_MIN_RANGE_FLOW_ADULT_PEDIA_LOOPS
            } else {
                yMaxRange = POSITIVE_MAX_RANGE_FLOW_ADULT_PEDIA_LOOPS
            }
        }
    }


    // created at 31 jan 2023
    private fun filterFlowGraphNegativeValue(yMinValue:Float){
        if (prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT) {

            if (yMinValue > NEGATIVE_MIN_RANGE_FLOW_NEO_LOOPS) {
                yMinRange = NEGATIVE_MIN_RANGE_FLOW_NEO_LOOPS
            }else {
                yMinRange = NEGATIVE_MAX_RANGE_FLOW_NEO_LOOPS
            }
        }
        else {
            if (yMinValue > -80.0) {
                yMinRange = NEGATIVE_MIN_RANGE_FLOW_ADULT_PEDIA_LOOPS
            } else {
                yMinRange = NEGATIVE_MAX_RANGE_FLOW_ADULT_PEDIA_LOOPS
            }
        }
    }


    fun addEntry(x: Float, y: Float) {
        Log.i("FLOWVOLUMEVALUE" , "x $x  y $y ")
        changeRangeScale(y,x)
        dataSeries.append(x,y)
    }

    private class CenterLayoutManager(xAxis: IAxis, yAxis: IAxis) :
        ILayoutManager {
        private val defaultLayoutManager: DefaultLayoutManager = DefaultLayoutManager.Builder()
            .setLeftInnerAxesLayoutStrategy(
                CenteredLeftAlignmentInnerAxisLayoutStrategy(
                    xAxis
                )
            )
            .setTopInnerAxesLayoutStrategy(
                CenteredTopAlignmentInnerAxisLayoutStrategy(
                    yAxis
                )
            )
            .build()

        private var isFirstLayout = false

        override fun attachAxis(axis: IAxis, isXAxis: Boolean) {
            defaultLayoutManager.attachAxis(axis, isXAxis)
        }

        override fun detachAxis(axis: IAxis) {
            defaultLayoutManager.detachAxis(axis)
        }

        override fun onAxisPlacementChanged(
            axis: IAxis,
            oldAxisAlignment: AxisAlignment,
            oldIsCenterAxis: Boolean,
            newAxisAlignment: AxisAlignment,
            newIsCenterAxis: Boolean,
        ) {
            defaultLayoutManager.onAxisPlacementChanged(
                axis,
                oldAxisAlignment,
                oldIsCenterAxis,
                newAxisAlignment,
                newIsCenterAxis
            )
        }

        override fun attachTo(services: IServiceContainer) {
            defaultLayoutManager.attachTo(services)
            // need to perform 2 layout passes during first layout of chart
            isFirstLayout = true
        }

        override fun detach() {
            defaultLayoutManager.detach()
        }

        override fun isAttached(): Boolean {
            return defaultLayoutManager.isAttached
        }

        override fun onLayoutChart(width: Int, height: Int): Size
        {
            // need to perform additional layout pass if it is a first layout pass
            // because we don't know correct size of axes during first layout pass
            if (isFirstLayout) {
                defaultLayoutManager.onLayoutChart(width, height)
                isFirstLayout = false
            }
            return defaultLayoutManager.onLayoutChart(width, height)
        }

        init {
            // need to override default inner layout strategies for bottom and right aligned axes
            // because xAxis has right axis alignment and yAxis has bottom axis alignment
        }
    }


    private class CenteredTopAlignmentInnerAxisLayoutStrategy(private val yAxis: IAxis) :
        TopAlignmentInnerAxisLayoutStrategy() {
        override fun layoutAxes(left: Int, top: Int, right: Int, bottom: Int) {
            // find the coordinate of 0 on the Y Axis in pixels
            // place the stack of the top-aligned X Axes at this coordinate
            val topCoordinate = yAxis.currentCoordinateCalculator.getCoordinate(0.0)
            layoutFromTopToBottom(left, topCoordinate.toInt(), right, axes)
        }
    }

    private class CenteredLeftAlignmentInnerAxisLayoutStrategy(private val xAxis: IAxis) :
        LeftAlignmentInnerAxisLayoutStrategy() {
        override fun layoutAxes(left: Int, top: Int, right: Int, bottom: Int) {
            // find the coordinate of 0 on the X Axis in pixels
            // place the stack of the left-aligned Y Axes at this coordinate
            val leftCoordinate = xAxis.currentCoordinateCalculator.getCoordinate(0.0)
            layoutFromLeftToRight(leftCoordinate.toInt(), top, bottom, axes)
        }
    }


    inner class DimTracePaletteProvider :
        PaletteProviderBase<XyRenderableSeriesBase>(XyRenderableSeriesBase::class.java),
        IStrokePaletteProvider {
        private val colors = IntegerValues()

        private val startOpacity = 0.2
        private var diffOpacity = 1 - startOpacity
        private var faction = 0.0
        private var opacity = 0.0f

        override fun getStrokeColors(): IntegerValues = colors

        override fun update() {
            val defaultColor = renderableSeries!!.strokeStyle.color
            val size = renderableSeries!!.currentRenderPassData.pointsCount()
            colors.setSize(size)

            val colorsArray = colors.itemsArray

            for (i in 0 until size) {
                faction = 0.10
                opacity = (startOpacity + faction * diffOpacity).toFloat()
                colorsArray[i] = ColorUtil.argb(defaultColor, opacity)
            }
        }

    }

}