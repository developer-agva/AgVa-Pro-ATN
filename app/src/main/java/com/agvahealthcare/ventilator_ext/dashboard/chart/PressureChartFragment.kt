package com.agvahealthcare.ventilator_ext.dashboard.chart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentXValue
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentYValue
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.isPatientTrigger
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.xMaxRangeGlobal
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.xValuePatientTriggerList
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.dashboard.TraceArc
import com.agvahealthcare.ventilator_ext.dashboard.chart.paletteprovider.ColouredLinePaletteProvider
import com.agvahealthcare.ventilator_ext.databinding.FragmentChartBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.scichart.charting.model.dataSeries.IXyDataSeries
import com.scichart.charting.visuals.annotations.AnnotationLabel
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
import java.util.Collections
import kotlin.math.abs

class PressureChartFragment : GraphFragment() {

    companion object {
        const val TAG = "PressureChartFragment"
        fun newInstance(
            type: GraphType,
            minValue: Int,
            maxValue: Int,
            cc: ColourContainer? = null
        ): PressureChartFragment {
            val args = Bundle()
            args.putInt(KEY_MIN_VALUE_VIEW, minValue)
            args.putInt(KEY_MAX_VALUE_VIEW, maxValue)

            val fragment = PressureChartFragment()
            fragment.arguments = args
            return fragment
        }
    }

    fun addTextOnMaxRange(xMaxRange: Double) {
        binding.txtMaxLabel.text = xMaxRange.toString().split('.')[0]
        binding.chartSurface.xAxes[1].visibleRange = DoubleRange(0.0, xMaxRange)
    }

    private lateinit var dataSeries0: IXyDataSeries<Int, Float>
    private lateinit var dataSeries1: IXyDataSeries<Int, Float>
    private var whichTrace = TraceArc.TraceA
    private var minValue: Int? = 0
    private var maxValue: Int? = 0
    private var prefManager: PreferenceManager? = null
    var horizontalLineAnnotation: HorizontalLineAnnotation? = null
    var annotationLabel:AnnotationLabel? = null
    var horizontalLineAnnotation1: HorizontalLineAnnotation? = null
    val titleStyle = FontStyle(14.0f, ColorUtil.White)
    private var mDashBoardViewModel: DashBoardViewModel? = null

    // created at 20 jan 2023


    private var minRange = 0.0
    private var maxRange = 0.0
    private var timePeek = -1
    private var isFirstTime = false


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

        minValue = arguments?.getInt(KEY_MIN_VALUE_VIEW)
        maxValue = arguments?.getInt(KEY_MAX_VALUE_VIEW)

        binding.txtMaxLabel.text = xMaxRangeGlobal.toString().split('.')[0]

        // added at 20 jan 2023
        minRange = Configs.getRangeOfYAxisChart(
            requireContext(),
            Configs.ChartType.PressureChart_Type
        ).first
        maxRange = Configs.getRangeOfYAxisChart(
            requireContext(),
            Configs.ChartType.PressureChart_Type
        ).second

        horizontalLineAnnotation = HorizontalLineAnnotation(requireContext()).apply {
            this.xAxisId = "OLD"
        }

//        annotationLabel = AnnotationLabel(requireContext())?.apply {
//            this.labelPlacement = LabelPlacement.TopLeft
//          this.text = "Pplat"
//            this.fontStyle = FontStyle(15.0f, ColorUtil.Grey)
//        }
//        horizontalLineAnnotation1 = HorizontalLineAnnotation(requireContext()).apply {
//            this.xAxisId = "OLD"
//        }

        initGraph()


        binding.txtMaxLabel.setOnClickListener {
            Log.i("value_Adawd", xMaxRangeGlobal.toString())
            if (xMaxRangeGlobal == 12.9) (requireActivity() as DashBoardActivity).sendCommandForChangeXAxisGraph("2")
            else (requireActivity() as DashBoardActivity).sendCommandForChangeXAxisGraph("1")
        }

    }

    private fun initGraph() {

        //For the initial graphs the xprimary Axis and the XSecondary Axis will be updated.
        val xPRimaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, GRAPH_THRESHOLD.toDouble()))
            .withMaxAutoTicks(4)
            .withTickLabelStyle(titleStyle)
            .withAxisId("OLD")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        val xsecondaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, xMaxRangeGlobal))
            .withTickLabelStyle(titleStyle)
            .withMaxAutoTicks(5)
            .withAxisId("HiddenXAxis")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        // modified at 20 jan 2023

        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withMaxAutoTicks(5)
            .withTickLabelStyle(titleStyle)
            .withDrawMajorBands(true)
            .withAutoRangeMode(AutoRange.Never)
            .withVisibleRange(minRange, maxRange)
            .build()


        binding.chartSurface.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.chartSurface.renderableSeriesAreaBorderStyle =
            sciChartBuilder.newPen().withColor(ColorUtil.Transparent).build();

        xPRimaryAxis.visibility = View.GONE
        xsecondaryAxis.visibility = View.VISIBLE

        xPRimaryAxis.drawMajorGridLines = false
        xPRimaryAxis.drawMinorGridLines = false
        xPRimaryAxis.drawMajorBands = false
        xPRimaryAxis.drawMajorTicks = false
        xPRimaryAxis.drawMinorTicks = false


        yAxis.drawMajorGridLines = true
        yAxis.drawMinorGridLines = false
        yAxis.drawMajorBands = false
        yAxis.drawMajorTicks = true
        yAxis.drawMajorTicks = false

        // RM Chart
//        dataSeries0 = sciChartBuilder.newXyDataSeries(
//            Int::class.javaObjectType,
//            Float::class.javaObjectType
//        ).withFifoCapacity(FIFO_CAPACITY).build()

        dataSeries1 = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()

        horizontalLineAnnotation?.stroke =
            SolidPenStyle(ColorUtil.Red, false, 1.0f, floatArrayOf(0f, 0f))
//        horizontalLineAnnotation1?.stroke = SolidPenStyle(ColorUtil.Grey,false,1.0f, floatArrayOf(50f,50f))

        // draw desired border using LineAnnotation
//        val verticalLine = sciChartBuilder.newLineAnnotation()
//            .withPosition(0.0, 0.0, 0.0, 1.0)
//            .withCoordinateMode(AnnotationCoordinateMode.Relative)
//            .withIsEditable(true)
//            .withStroke(1f, Color.WHITE)
//            .build()

//        val horizontalLine = sciChartBuilder.newLineAnnotation()
//            .withPosition(0.0, 1.0, 1.0, 1.0)
//            .withCoordinateMode(AnnotationCoordinateMode.Relative)
//            .withIsEditable(true)
//            .withXAxisId("OLD")
//            .withStroke(1f, Color.WHITE)
//            .build()

        val rs2: IRenderableSeries = sciChartBuilder.newLineSeries()
            .withStrokeStyle(sciChartBuilder.newPen().withColor(ColorUtil.White).withThickness(3f).build())
//            .withAreaFillColor(ColorUtil.Wheat)
            .withDataSeries(dataSeries1)
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.PRESSURE))
            .withPaletteProvider(ColouredLinePaletteProvider(prefManager))
            .withXAxisId("OLD")
            .build()

        // RM scichart
        modifier.showTooltip = true
        modifier.showAxisLabels = true
        modifier.isEnabled = true

        Collections.addAll(binding.chartSurface.chartModifiers, modifier)
        UpdateSuspender.using(binding.chartSurface) {
            Log.i("SUSPEND", "")
            Collections.addAll(binding.chartSurface.xAxes, xPRimaryAxis)
            Collections.addAll(binding.chartSurface.xAxes, xsecondaryAxis)
            Collections.addAll(binding.chartSurface.yAxes, yAxis)
            Collections.addAll(binding.chartSurface.renderableSeries,  rs2)
            Collections.addAll(binding.chartSurface.annotations,horizontalLineAnnotation)
//            Collections.addAll(chartSurface.annotations,horizontalLineAnnotation1)
//            chartSurface.annotations.add(horizontalLineAnnotation)
//            Collections.addAll(horizontalLineAnnotation?.annotationLabels,annotationLabel)
        }
    }


    // created at 20 jan 2023
    private fun changeGraphRangeAtRunTime(y: Float, x: Int, trigger: String?) {

        Log.i("timeYValue", y.toString())
        if (x == timePeek) {
            binding.chartSurface.yAxes.default.visibleRange = DoubleRange(minRange, maxRange)
            timePeek = -1
        } else {
            Log.i("timePeekPressure", maxRange.toString())

            if (mDashBoardViewModel?.graphPeekValue?.value == "A") {

                if (prefManager?.readCurrentUid() != Configs.PatientProfile.TYPE_NEONAT) {

                    if (y < 25.0) {
                        maxRange = MIN_RANGE_PRESSURE_ADULT_PEDIA

                        setTimePeekValue(setXToChangeGraphDown(x))
                    } else {
                        maxRange = MAX_RANGE_PRESSURE_ADULT_PEDIA

                        binding.chartSurface.yAxes.default.visibleRange = DoubleRange(minRange, maxRange)
                        timePeek = -1
                    }
                }
            }
        }
    }


    private fun setXToChangeGraphDown(x: Int): Int {
        if (x + 340 > 350) {
            return abs((x + 340) - 350)
        }
        return x + 340
    }


    private fun setTimePeekValue(value: Int) {
        if (timePeek == -1) {
            timePeek = value
        }
    }


    // modified at 20 jan 2023
    fun addEntry(x: Int, y: Float, trigger: String?) {
        val xAxis = x % (GRAPH_THRESHOLD + 1)
        val limit = prefManager?.readPip()
//        val limit1 = prefManager?.readPip()
        horizontalLineAnnotation?.x1 = 5.0
//        horizontalLineAnnotation1?.x1 = 5.0
        horizontalLineAnnotation?.setIsEditable(true)
//        horizontalLineAnnotation1?.setIsEditable(true)
//        Log.i("limitValue", "${limit?.get(1)}")
        horizontalLineAnnotation?.y1 = limit
//        horizontalLineAnnotation1?.y1 = limit1


        changeGraphRangeAtRunTime(y, xAxis, trigger)


        if(xValuePatientTriggerList.contains(xAxis.toDouble())){
            xValuePatientTriggerList.remove(xAxis.toDouble())
        }


        if (xAxis == GRAPH_THRESHOLD) {
            isFirstTime = true
//            xValuePatientTriggerList.clear()
        }

        // RM scichart
        Configs.customFifoCapacity(isFirstTime, xAxis, VentilatorApp.xTestingPressure)

        if (isPatientTrigger) xValuePatientTriggerList.add(xAxis.toDouble())

        // RM Chart
            if (whichTrace == TraceArc.TraceA) {
                if (isFirstTime) dataSeries1.updateXyAt(xAxis,xAxis,Float.NaN)
                else dataSeries1.append(xAxis, Float.NaN)
            } else {
                if (isFirstTime) {
                    dataSeries1.updateXyAt(xAxis, xAxis, y)
                } else {
                    dataSeries1.append(xAxis, y)
                }
            }

        if (xAxis % GRAPH_THRESHOLD == 0) {
            whichTrace = if (whichTrace == TraceArc.TraceA) TraceArc.TraceB else TraceArc.TraceA
        }
    }

    // RM scichart
    fun setRollOver(){
        modifier.setRolloverAt(currentXValue, currentYValue)
    }

    fun removeRollover(){
        modifier.removeRolloverAt(currentXValue, currentYValue)
    }

}