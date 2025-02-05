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
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.xMaxRangeGlobal
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.dashboard.TraceArc
import com.agvahealthcare.ventilator_ext.dashboard.chart.paletteprovider.ColouredLinePaletteProviderFlow
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
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
import kotlinx.android.synthetic.main.fragment_chart.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class FlowChartFragment : GraphFragment() {

    companion object {
        const val TAG = "FlowChartFragment"

        fun newInstance(
            type: GraphType,
            minValue: Int,
            maxValue: Int,
        ): FlowChartFragment {
            val args = Bundle()
            args.putInt(KEY_MIN_VALUE_VIEW, minValue)
            args.putInt(KEY_MAX_VALUE_VIEW, maxValue)

            val fragment = FlowChartFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //    private lateinit var dataSeries0: IXyDataSeries<Int, Float>
    private lateinit var dataSeries1: IXyDataSeries<Int, Float>
    private var whichTrace = TraceArc.TraceA
    private var minValue: Int? = 0
    private var maxValue: Int? = 0
    val titleStyle = FontStyle(14.0f, ColorUtil.White)
    private var prefManager: PreferenceManager? = null
    private var mDashBoardViewModel: DashBoardViewModel? = null

    // modified at 31 jan 2023
    private var minRange = 0.0
    private var maxRange = 0.0
    private var yMinValue = 0.0f
    private var yMaxValue = Float.MIN_VALUE
    private var timePeek = -1
    private var isFirstTime = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_chart, container, false)

    }

    fun addTextOnMaxRange(xMaxRange: Double) {
        txtMaxLabel.text = xMaxRange.toString().split('.')[0]
        chartSurface.xAxes[1].visibleRange = DoubleRange(0.0, xMaxRange)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())
        mDashBoardViewModel =
            ViewModelProvider(requireActivity()).get(DashBoardViewModel::class.java)

        minValue = arguments?.getInt(KEY_MIN_VALUE_VIEW)
        maxValue = arguments?.getInt(KEY_MAX_VALUE_VIEW)

        txtMaxLabel.text = xMaxRangeGlobal.toString().split('.')[0]

        if (prefManager?.readGraphParentType() == parentType.LoopsFragmentGraph || prefManager?.readGraphParentType() == parentType.DivideQuadFragmentGraph) {
            if (prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT) {
                minRange = -50.0
                maxRange = 50.0
            } else {
                minRange = -200.0
                maxRange = 200.0
            }
        } else {
            minRange = Configs.getRangeOfYAxisChart(
                requireContext(),
                Configs.ChartType.FlowChart_Type
            ).first
            maxRange = Configs.getRangeOfYAxisChart(
                requireContext(),
                Configs.ChartType.FlowChart_Type
            ).second
        }

        Log.i("fadaw", "$minRange , $maxRange")

        textViewChartType.text = requireContext().getString(R.string.flow_l_min)
        initGraph()

        txtMaxLabel.setOnClickListener {
            Log.i("value_Adawd", xMaxRangeGlobal.toString())
            if (xMaxRangeGlobal == 12.9) (requireActivity() as DashBoardActivity).sendCommandForChangeXAxisGraph(
                "2"
            )
            else (requireActivity() as DashBoardActivity).sendCommandForChangeXAxisGraph("1")
        }
    }

    private fun initGraph() {

        val xPrimaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, GRAPH_THRESHOLD.toDouble()))
            .withAutoRangeMode(AutoRange.Never)
            .withTickLabelStyle(titleStyle)
            .withAxisId("Visible Axis")
            .withMaxAutoTicks(0)
            .build()
        val xSecondaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, xMaxRangeGlobal))
            .withAutoRangeMode(AutoRange.Never)
            .withTickLabelStyle(titleStyle)
            .withAxisId("Hidden XAxis")
            .withMaxAutoTicks(10)
            .build()

        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withVisibleRange(
                minRange,
                maxRange
            )
            .withMaxAutoTicks(10)
            .withTickLabelStyle(titleStyle)
            .withAutoRangeMode(AutoRange.Never)
            .build()

        chartSurface.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        chartSurface.renderableSeriesAreaBorderStyle = sciChartBuilder.newPen()
            .withColor(ColorUtil.Transparent)
            .build();
        xPrimaryAxis.visibility = View.GONE
        xSecondaryAxis.visibility = View.VISIBLE
        xPrimaryAxis.drawMajorGridLines = false
        xPrimaryAxis.drawMinorGridLines = false
        xPrimaryAxis.drawMajorBands = false
        xPrimaryAxis.drawMajorTicks = true
        xSecondaryAxis.drawMajorTicks = true
        xPrimaryAxis.drawMinorTicks = false


        yAxis.drawMajorGridLines = true
        yAxis.drawMinorGridLines = false
        yAxis.drawMajorBands = false
        yAxis.drawMajorTicks = true


        // RM Chart
//        dataSeries0 = sciChartBuilder.newXyDataSeries(
//            Int::class.javaObjectType,
//            Float::class.javaObjectType
//        ).withFifoCapacity(FIFO_CAPACITY).build()

        dataSeries1 = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()

        // RM chart hidden
//        val rs1: IRenderableSeries = sciChartBuilder.newMountainSeries()
//            .withDataSeries(dataSeries0)
//            .withXAxisId("Visible Axis")
//            .withStrokeStyle(
//                sciChartBuilder.newPen().withColor(Color.parseColor("#FFFFFF")).withThickness(1f)
//                    .build()
//            )
//            .withAreaFillColor(Color.parseColor("#FFFFFF"))
//            .build()

        // RM scichart
        val rs2: IRenderableSeries = sciChartBuilder.newMountainSeries()
            .withDataSeries(dataSeries1)
            .withXAxisId("Visible Axis")
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.FLOW))
            .withPaletteProvider(ColouredLinePaletteProviderFlow(prefManager))
//            .withStrokeStyle(
//                sciChartBuilder.newPen().withColor(Color.parseColor("#FFFFFF")).withThickness(1f)
//                    .build()
//            )
//            .withAreaFillColor(Color.parseColor("#00FF00"))
            .build()


        val horizontalLineAnnotation = HorizontalLineAnnotation(activity)
        horizontalLineAnnotation.x1 = 5.0
        horizontalLineAnnotation.y1 = 0.0
        horizontalLineAnnotation.stroke =
            SolidPenStyle(ColorUtil.Grey, false, 0.07f, floatArrayOf(0f, 0f))
        // horizontalLineAnnotation.horizontalGravity = Gravity.RIGHT
        chartSurface.annotations.add(horizontalLineAnnotation)

        val verticalLine = sciChartBuilder.newLineAnnotation()
            .withPosition(0.0, 0.0, 0.0, 1.0)
            .withCoordinateMode(AnnotationCoordinateMode.Relative)
            .withIsEditable(true)
            .withStroke(1f, Color.WHITE)
            .build()

        val horizontalLine = sciChartBuilder.newLineAnnotation()
            .withPosition(0.0, 1.0, 1.0, 1.0)
            .withIsEditable(true)
            .withCoordinateMode(AnnotationCoordinateMode.Relative)
            .withStroke(1f, Color.WHITE)
            .build()

        // RM scichart
        modifier.showTooltip = true
        modifier.showAxisLabels = true
        modifier.isEnabled = true
        Collections.addAll(chartSurface.chartModifiers, modifier)
        Collections.addAll(chartSurface.annotations, horizontalLine, verticalLine)

        Log.i("Horizantlelinecheck", "Horizantal Line")

        UpdateSuspender.using(chartSurface) {
            Collections.addAll(chartSurface.xAxes, xPrimaryAxis)
            Collections.addAll(chartSurface.xAxes, xSecondaryAxis)
            Collections.addAll(chartSurface.yAxes, yAxis)
            Collections.addAll(chartSurface.renderableSeries, rs2)

        }
    }

    // Modified at 31 jan 2023
    private fun changeGraphRangeAtRunTime(peak: Float?, x: Int) {

        if (timePeek == x) {
            chartSurface.yAxes.default.visibleRange = DoubleRange(minRange, maxRange)
            timePeek = -1
        } else {
            filterGraphValue(peak)
            minRange = -abs(maxRange)

            if (chartSurface.yAxes.default.visibleRange.max.toString() > maxRange.toString()) setTimePeekValue(
                setXToChangeGraph(x)
            )
            else if (chartSurface.yAxes.default.visibleRange.max.toString() < maxRange.toString()) {
                chartSurface.yAxes.default.visibleRange =
                    DoubleRange(minRange, maxRange)
            }

        }
    }

    // created at 31 jan 2023
    private fun filterGraphValue(yPeakValue: Float?) {

        yPeakValue?.let {
            if (prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT) {

                if (it < 5.0) {
                    maxRange = POSITIVE_MIN_RANGE_FLOW_NEO
                } else if (it > 5.0 && it < 25.0) {
                    maxRange = POSITIVE_MID_RANGE_FLOW_NEO
                } else {
                    maxRange = POSITIVE_MAX_RANGE_FLOW_NEO
                }
            } else {
                if (it < 40.0) {
                    maxRange = POSITIVE_MIN_RANGE_FLOW_ADULT_PEDIA
                } else if (it > 40.0 && it < 90.0) {
                    maxRange = POSITIVE_MID_RANGE_FLOW_ADULT_PEDIA
                } else {
                    maxRange = POSITIVE_MAX_RANGE_FLOW_ADULT_PEDIA
                }
            }
        }
    }


    // created at 25 jan 2023
    private fun setXToChangeGraph(x: Int): Int {
        if (x + 50 > 350) {
            return abs((x + 50) - 350)
        }
        return x + 50
    }

    // created at 25 jan 2023
    private fun setTimePeekValue(value: Int) {
        if (timePeek == -1) {
            timePeek = value
        }
    }

    fun addEntry(x: Int, y: Float) {
        val xAxis = x % (GRAPH_THRESHOLD + 1)
        Log.i("FLOW_CHART", "x = $xAxis , Y = $y , peak = ${VentilatorApp.flowPeakValuePositive}")

        if (prefManager?.readGraphParentType() != parentType.LoopsFragmentGraph && prefManager?.readGraphParentType() != parentType.DivideQuadFragmentGraph) {
            changeGraphRangeAtRunTime(VentilatorApp.flowPeakValueNegative?.let {
                VentilatorApp.flowPeakValuePositive?.let { it1 ->
                    max(
                        it1,
                        it
                    )
                }
            }, xAxis)
        }

        if (xAxis == GRAPH_THRESHOLD) {
            isFirstTime = true
        }

//        if (whichTrace == TraceArc.TraceA) {
//            dataSeries0.append(xAxis, y)
//            dataSeries1.append(xAxis, Float.NaN)
//
//        } else {
//            dataSeries0.append(xAxis, Float.NaN)
//            dataSeries1.append(xAxis, y)
//        }

        // RM scichart
        Configs.customFifoCapacity(isFirstTime, xAxis, VentilatorApp.xTestingFlow)
        if (whichTrace == TraceArc.TraceA) {
            if (isFirstTime) dataSeries1.updateXyAt(xAxis, xAxis, Float.NaN)
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
            Log.i("FLOW_CHART_VALUE", "x = $xAxis")
        }
    }

    // RM scichart
    fun setRollOver() {
        modifier.setRolloverAt(VentilatorApp.currentXValue, VentilatorApp.currentYValue)
    }

    fun removeRollover() {
        modifier.removeRolloverAt(VentilatorApp.currentXValue, VentilatorApp.currentYValue)
    }
}
