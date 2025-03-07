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
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentXValue
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentYValue
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.xMaxRangeGlobal
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.dashboard.TraceArc
import com.agvahealthcare.ventilator_ext.dashboard.chart.paletteprovider.ColouredLinePaletteProviderVolume
import com.agvahealthcare.ventilator_ext.databinding.FragmentChartBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.*
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.scichart.charting.model.dataSeries.IXyDataSeries
import com.scichart.charting.visuals.annotations.AnnotationCoordinateMode
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.AxisAlignment
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries
import com.scichart.core.framework.UpdateSuspender
import com.scichart.data.model.DoubleRange
import com.scichart.drawing.common.FontStyle
import com.scichart.drawing.utility.ColorUtil
import java.util.*
import kotlin.math.abs


class VolumeChartFragment : GraphFragment() {

    companion object {
        const val TAG = "volumeChartFragment"

        fun newInstance(
            type: GraphType,
            minValue: Int,
            maxValue: Int,
        ): VolumeChartFragment {
            val args = Bundle()
            args.putInt(KEY_MIN_VALUE_VIEW, minValue)
            args.putInt(KEY_MAX_VALUE_VIEW, maxValue)
            val fragment = VolumeChartFragment()
            fragment.arguments = args
            return fragment
        }
    }

    //        private lateinit var dataSeries0: IXyDataSeries<Int, Float>
    private lateinit var dataSeries1: IXyDataSeries<Int, Float>
    private var prefManager: PreferenceManager? = null
    private var whichTrace = TraceArc.TraceA
    private var minValue: Int? = 0
    private var maxValue: Int? = 0
    val titleStyle = FontStyle(14.0f, ColorUtil.White)
    private var mDashBoardViewModel: DashBoardViewModel? = null

    // created at 20 jan 2023
    private var minRange = 0.0
    private var maxRange = 0.0
    private var timePeek = -1
    private var isFirstTime = false
    //  private var peekValue = Double.MIN_VALUE


    private lateinit var binding : FragmentChartBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChartBinding.inflate(layoutInflater,container,false)
        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())
        mDashBoardViewModel =
            ViewModelProvider(requireActivity()).get(DashBoardViewModel::class.java)

        minValue = arguments?.getInt(KEY_MIN_VALUE_VIEW)
        maxValue = arguments?.getInt(KEY_MAX_VALUE_VIEW)
        binding.textViewChartType.text = requireContext().getString(R.string.vol_l_min)

        binding.txtMaxLabel.text = xMaxRangeGlobal.toString().split('.')[0]

        // added at 20 jan 2023
        minRange =
            Configs.getRangeOfYAxisChart(requireContext(), Configs.ChartType.VolumeChart_Type).first
        maxRange = Configs.getRangeOfYAxisChart(
            requireContext(),
            Configs.ChartType.VolumeChart_Type
        ).second
        initGraph()

        binding.txtMaxLabel.setOnClickListener {
            Log.i("value_Adawd", xMaxRangeGlobal.toString())
            if (xMaxRangeGlobal == 12.9) (requireActivity() as DashBoardActivity).sendCommandForChangeXAxisGraph(
                "2"
            )
            else (requireActivity() as DashBoardActivity).sendCommandForChangeXAxisGraph("1")
        }
    }

    fun addTextOnMaxRange(xMaxRange: Double) {
        Log.i("valuesea", "3")
        binding.txtMaxLabel.text = xMaxRange.toString().split('.')[0]
        binding.chartSurface.xAxes[1].visibleRange = DoubleRange(0.0, xMaxRange)
    }

    private fun initGraph() {
        val xPrimaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, GRAPH_THRESHOLD.toDouble()))
            .withMaxAutoTicks(30)
            .withTickLabelStyle(titleStyle)
            .withAxisId("Visible Axis")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        val xSecondaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, xMaxRangeGlobal))
            .withMaxAutoTicks(5)
            .withTickLabelStyle(titleStyle)
            .withAxisId("HiddenXAxis")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        // modified at 20 jan 2023
        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withVisibleRange(minRange, maxRange)
            .withMaxAutoTicks(5)
            .withTickLabelStyle(titleStyle)
            .withAutoRangeMode(AutoRange.Never)
            .build()

        binding.chartSurface.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.chartSurface.renderableSeriesAreaBorderStyle =
            sciChartBuilder.newPen().withColor(ColorUtil.Transparent).build();
        xPrimaryAxis.visibility = View.GONE
        xSecondaryAxis.visibility = View.VISIBLE

        xPrimaryAxis.drawMajorGridLines = false
        xPrimaryAxis.drawMinorGridLines = false
        xPrimaryAxis.drawMajorBands = false
        xPrimaryAxis.drawMajorTicks = false
        xPrimaryAxis.drawMinorTicks = false

        yAxis.drawMajorGridLines = true
        yAxis.drawMinorGridLines = false
        yAxis.drawMajorBands = false
        yAxis.drawMajorTicks = true
        yAxis.drawMajorTicks = false

        // RM Chart
//        dataSeries0 = sciChartBuilder.newXyDataSeries(
//            Int::class.javaObjectType,
//            Float::class.javaObjectType
//        ).withAcceptsUnsortedData().build()

        dataSeries1 = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()

//        val rs1: IRenderableSeries = sciChartBuilder.newMountainSeries()
//            .withDataSeries(dataSeries0)
//            .withXAxisId("Visible Axis")
//            .withStrokeStyle(
//                sciChartBuilder.newPen().withColor(Color.parseColor("#FFFFFF")).withThickness(1f)
//                    .build()
//            )
//            .withAreaFillColor(Color.parseColor("#FFFFFF"))
//            //  .withAreaFillColor(Color.WHITE)
//            .build()

        // RM scichart
        val rs2: IRenderableSeries = sciChartBuilder.newMountainSeries()
            .withDataSeries(dataSeries1)
            .withXAxisId("Visible Axis")
//            .withAreaFillColor(ColorUtil.CadetBlue)
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.VOLUME))
            .withPaletteProvider(ColouredLinePaletteProviderVolume(prefManager))
            .build()

        val verticalLine = sciChartBuilder.newLineAnnotation()
            .withPosition(0.0, 0.0, 0.0, 1.0)
            .withCoordinateMode(AnnotationCoordinateMode.Relative)
            .withIsEditable(true)
            .withStroke(1f, Color.WHITE)
            .build()

        val horizontalLine = sciChartBuilder.newLineAnnotation()
            .withPosition(0.0, 1.0, 1.0, 1.0)
            .withCoordinateMode(AnnotationCoordinateMode.Relative)
            .withIsEditable(true)
            .withStroke(1f, Color.WHITE)
            .build()

        // RM scichart
        modifier.showTooltip = true
        modifier.showAxisLabels = true
        modifier.isEnabled = true

        Collections.addAll(binding.chartSurface.annotations, horizontalLine, verticalLine)

        UpdateSuspender.using(binding.chartSurface) {
            Collections.addAll(binding.chartSurface.chartModifiers, modifier)
            Collections.addAll(binding.chartSurface.xAxes, xPrimaryAxis)
            Collections.addAll(binding.chartSurface.xAxes, xSecondaryAxis)
            Collections.addAll(binding.chartSurface.yAxes, yAxis)
            Collections.addAll(binding.chartSurface.renderableSeries ,rs2)
        }
    }

    // created at 20 jan 2023
    private fun changeGraphRangeAtRunTime(y: Float, x: Int) {

        if (x == timePeek) {
            binding.chartSurface.yAxes.default.visibleRange = DoubleRange(minRange, maxRange)
            timePeek = -1
        } else {
            Log.i("timePeekValue", mDashBoardViewModel?.graphPeekValue?.value.toString())
            if (mDashBoardViewModel?.graphPeekValue?.value == "A") {

                if (prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT) {

                    if (y < 40.0) {
                        maxRange = MIN_RANGE_VOLUME_NEO
                        setTimePeekValue(setXToChangeGraph(x))
                    } else if (y > 90.0) {
                        maxRange = MAX_RANGE_VOLUME_NEO
                        timePeek = -1
                        binding.chartSurface.yAxes.default.visibleRange = DoubleRange(minRange, maxRange)
                    } else if (y > 40.0) {
                        maxRange = MID_RANGE_VOLUME_NEO

                        if (binding.chartSurface.yAxes.default.visibleRange.getMax() == MAX_RANGE_VOLUME_NEO) {
                            setTimePeekValue(setXToChangeGraph(x))
                        } else {
                            timePeek = -1
                            binding.chartSurface.yAxes.default.visibleRange =
                                DoubleRange(minRange, maxRange)
                        }
                    }
                } else {
                    if (y < 400.0) {
                        maxRange = MIN_RANGE_VOLUME_ADULT_PEDIA
                        setTimePeekValue(setXToChangeGraph(x))
                    } else if (y > 900.0) {
                        maxRange = MAX_RANGE_VOLUME_ADULT_PEDIA
                        timePeek = -1
                        binding.chartSurface.yAxes.default.visibleRange = DoubleRange(minRange, maxRange)
                    } else if (y > 400.0) {
                        maxRange = MID_RANGE_VOLUME_ADULT_PEDIA

                        if (binding.chartSurface.yAxes.default.visibleRange.getMax() == MAX_RANGE_VOLUME_ADULT_PEDIA) {
                            setTimePeekValue(setXToChangeGraph(x))
                        } else {
                            timePeek = -1
                            binding.chartSurface.yAxes.default.visibleRange =
                                DoubleRange(minRange, maxRange)
                        }
                    }

                }
            }
        }
    }

    private fun setXToChangeGraph(x: Int): Int {
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
    fun addEntry(x: Int, y: Float) {
        val xAxis = x % (GRAPH_THRESHOLD + 1)
        Log.i("VOLUME_CHART", "x = $xAxis , Y = $y")

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
        Configs.customFifoCapacity(isFirstTime, xAxis, VentilatorApp.xTestingVolume)

        changeGraphRangeAtRunTime(y, xAxis)
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
            Log.i("VOLUME_CHART_VALUE", "x = $xAxis")
        }
//
    }

    // RM scichart
    fun setRollOver(){
        modifier.setRolloverAt(currentXValue, currentYValue)
    }

    fun removeRollover(){
        modifier.removeRolloverAt(currentXValue, currentYValue)
    }

}
