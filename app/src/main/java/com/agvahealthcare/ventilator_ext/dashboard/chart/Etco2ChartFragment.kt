package com.agvahealthcare.ventilator_ext.dashboard.chart

import android.graphics.Color
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
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.xMaxRangeGlobal
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.dashboard.TraceArc
import com.agvahealthcare.ventilator_ext.dashboard.chart.paletteprovider.ColouredLinePaletteProviderVolume
import com.agvahealthcare.ventilator_ext.databinding.FragmentChartBinding
import com.agvahealthcare.ventilator_ext.databinding.FragmentEtcuffBinding
import com.agvahealthcare.ventilator_ext.databinding.FragmentSettingsBinding
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections
import kotlin.math.abs


class Etco2ChartFragment : GraphFragment() {

    companion object {
        const val TAG = "Etco2ChartFragment"
        fun newInstance(
            type: GraphType,
            minValue: Int,
            maxValue: Int,
            cc: ColourContainer? = null
        ): Etco2ChartFragment {
            val args = Bundle()
            args.putInt(KEY_MIN_VALUE_VIEW, minValue)
            args.putInt(KEY_MAX_VALUE_VIEW, maxValue)

            val fragment = Etco2ChartFragment()
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
        mDashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]

        minValue = arguments?.getInt(KEY_MIN_VALUE_VIEW)
        maxValue = arguments?.getInt(KEY_MAX_VALUE_VIEW)
        binding.textViewChartType.text = requireContext().getString(R.string.etco2_mmHg)

        binding.txtMaxLabel.text = xMaxRangeGlobal.toString().split('.')[0]

        // added at 20 jan 2023
        minRange = Configs.getRangeOfYAxisChart(
            requireContext(),
            Configs.ChartType.EtCo2_Type
        ).first
        maxRange = 50.0

        horizontalLineAnnotation = HorizontalLineAnnotation(requireContext())

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
            .withAxisId("Visible Axis")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        val xsecondaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, xMaxRangeGlobal))
            .withTickLabelStyle(titleStyle)
            .withMaxAutoTicks(1)
            .withAxisId("HiddenXAxis")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        // modified at 20 jan 2023
        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withMaxAutoTicks(1)
            .withTickLabelStyle(titleStyle)
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

        yAxis.drawMajorGridLines = false
        yAxis.drawMinorGridLines = false
        yAxis.drawMajorBands = false
        yAxis.drawMajorTicks = false
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

        // draw desired border using LineAnnotation
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

        val rs2: IRenderableSeries = sciChartBuilder.newLineSeries()
            .withStrokeStyle(sciChartBuilder.newPen().withColor(ColorUtil.White).withThickness(2f).build())
            .withDataSeries(dataSeries1)
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.EtCo2))
            .withPaletteProvider(ColouredLinePaletteProviderVolume(prefManager))
            .withXAxisId("Visible Axis")
            .build()

        // RM scichart
        modifier.showTooltip = true
        modifier.showAxisLabels = true
        modifier.isEnabled = true

        Collections.addAll(binding.chartSurface.annotations, horizontalLine, verticalLine)
        Collections.addAll(binding.chartSurface.chartModifiers, modifier)
        UpdateSuspender.using(binding.chartSurface) {
            Collections.addAll(binding.chartSurface.xAxes, xPRimaryAxis)
            Collections.addAll(binding.chartSurface.xAxes, xsecondaryAxis)
            Collections.addAll(binding.chartSurface.yAxes, yAxis)
            Collections.addAll(binding.chartSurface.renderableSeries,  rs2)
            binding.chartSurface.annotations.add(horizontalLineAnnotation)
        }
    }

    // created at 20 jan 2023
    private fun changeGraphRangeAtRunTime(y: Float, x: Int) {

        Log.i("timeYValue", y.toString())
        if (x == timePeek) {
            binding.chartSurface.yAxes.default.visibleRange = DoubleRange(minRange, maxRange)
            timePeek = -1
        } else {

            Log.i("timePeekPressure", maxRange.toString())

            if (mDashBoardViewModel?.graphPeekValue?.value == "A") {

                if (prefManager?.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT) {

                    if (y < 25.0) {
                        maxRange = MIN_RANGE_PRESSURE_NEO
                        setTimePeekValue(setXToChangeGraphDown(x))

                    } else {
                        maxRange = MID_RANGE_PRESSURE_NEO
                        timePeek = -1
                        binding.chartSurface.yAxes.default.visibleRange = DoubleRange(minRange, maxRange)
                    }
                } else {
                    if (y < 955.0) {
                        maxRange = MIN_RANGE_ETCO2_ADULT_PEDIA
                        setTimePeekValue(setXToChangeGraphDown(x))

                    } else {
                        maxRange = MAX_RANGE_ETCO2_ADULT_PEDIA
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
    fun addEntry(x: Int, y: Float) {
        val xAxis = x % (GRAPH_THRESHOLD + 1)
        val limit = prefManager?.readPipLimits()
        horizontalLineAnnotation?.x1 = 5.0
        horizontalLineAnnotation?.setIsEditable(false)
        Log.i("limitValue", "${limit?.get(1)}")
        horizontalLineAnnotation?.y1 = limit?.get(1)

//        changeGraphRangeAtRunTime(y, xAxis)

        if (xAxis.toDouble() == 350.0) {
            isFirstTime = true
        }

        // RM scichart
        Configs.customFifoCapacity(isFirstTime, xAxis, VentilatorApp.xTestingPressure)


        // RM Chart
        lifecycleScope.launch(Dispatchers.IO){
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
