package com.agvahealthcare.ventilator_ext.graph.lungs_dynamics

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentXValue
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentYValue
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.dynCompTimeList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.spontRRTimeList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.spontVTTimeList
import com.agvahealthcare.ventilator_ext.dashboard.chart.GraphFragment
import com.agvahealthcare.ventilator_ext.dashboard.chart.GraphType
import com.agvahealthcare.ventilator_ext.databinding.FragmentComplianceModuleBinding
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.scichart.charting.model.dataSeries.IXyDataSeries
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.AxisAlignment
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.pointmarkers.EllipsePointMarker
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries
import com.scichart.core.framework.UpdateSuspender
import com.scichart.data.model.DoubleRange
import com.scichart.drawing.common.FontStyle
import com.scichart.drawing.common.SolidBrushStyle
import com.scichart.drawing.utility.ColorUtil
import com.scichart.extensions.builders.SciChartBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections
import kotlin.time.Duration


class ComplianceModuleFragment(private var duration: String) : GraphFragment() {

    private var defaultParamsIndexFirst = "Compliance"
    private var defaultParamsIndexSecond = "Spont RR"
    private var defaultParamsIndexThird = "Spont VT"
    private var defaultDurationCount = 2
    private lateinit var binding : FragmentComplianceModuleBinding
    private var dataSeries1First: IXyDataSeries<Int, Float>? = null
    private var dataSeries1Second: IXyDataSeries<Int, Float>? = null
    private var dataSeries1Third: IXyDataSeries<Int, Float>? = null
    val titleStyle = FontStyle(14.0f, ColorUtil.White)
    val titleXStyle = FontStyle(14.0f, ColorUtil.Black)

    private var modifierDynComp : GraphFragment.CustomRolloverModifier? = null
    private var modifierSpontRR : GraphFragment.CustomRolloverModifier? = null
    private var modifierSpontVT : GraphFragment.CustomRolloverModifier? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentComplianceModuleBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    private fun initFirstGraph(listSize: Double) {

        val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()
        modifierDynComp = CustomRolloverModifier()
        //For the initial graphs the xprimary Axis and the XSecondary Axis will be updated.
        val xPRimaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, listSize))
            .withMaxAutoTicks(4)
            .withTickLabelStyle(titleXStyle)
            .withAxisId("OLD")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        val xsecondaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, listSize))
            .withTickLabelStyle(titleXStyle)
            .withMaxAutoTicks(5)
            .withAxisId("HiddenXAxis")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        // modified at 20 jan 2023

        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withMaxAutoTicks(2)
            .withTickLabelStyle(titleStyle)
            .withDrawMajorBands(true)
            .withAutoRangeMode(AutoRange.Never)
            .withVisibleRange(0.0, 500.0)
            .build()

        binding.trendFirstDynamicsChart.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.trendFirstDynamicsChart.renderableSeriesAreaBorderStyle =
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

        dataSeries1First = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()

        // === Create point marker ===
        val pointMarker = EllipsePointMarker().apply {
            fillStyle = SolidBrushStyle(Color.WHITE)
            width = 10
            height = 10
        }

        // === Create scatter series ===
        val rs2 = sciChartBuilder.newScatterSeries().withPointMarker(pointMarker)
            .withDataSeries(dataSeries1First)
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.DYNAMIC_COMP_CHART))
            .withXAxisId("OLD").build()
        modifierDynComp?.showTooltip = true
        modifierDynComp?.showAxisLabels = true
        modifierDynComp?.isEnabled = true

        Collections.addAll(binding.trendFirstDynamicsChart.chartModifiers, modifierDynComp!!)
        UpdateSuspender.using(binding.trendFirstDynamicsChart) {
            Collections.addAll(binding.trendFirstDynamicsChart.xAxes, xPRimaryAxis)
            Collections.addAll(binding.trendFirstDynamicsChart.xAxes, xsecondaryAxis)
            Collections.addAll(binding.trendFirstDynamicsChart.yAxes, yAxis)
            Collections.addAll(binding.trendFirstDynamicsChart.renderableSeries, rs2)
        }

    }

    private fun initSecondGraph(listSize: Double) {

        val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()
        modifierSpontRR = CustomRolloverModifier()
        //For the initial graphs the xprimary Axis and the XSecondary Axis will be updated.
        val xPRimaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, listSize))
            .withMaxAutoTicks(4)
            .withTickLabelStyle(titleXStyle)
            .withAxisId("OLD")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        val xsecondaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, listSize))
            .withTickLabelStyle(titleXStyle)
            .withMaxAutoTicks(5)
            .withAxisId("HiddenXAxis")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        // modified at 20 jan 2023

        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withMaxAutoTicks(2)
            .withTickLabelStyle(titleStyle)
            .withDrawMajorBands(true)
            .withAutoRangeMode(AutoRange.Never)
            .withVisibleRange(0.0, 50.0)
            .build()

        binding.trendSecondDynamicsChart.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.trendSecondDynamicsChart.renderableSeriesAreaBorderStyle =
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

        dataSeries1Second = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()

        // === Create point marker ===
        val pointMarker = EllipsePointMarker().apply {
            fillStyle = SolidBrushStyle(Color.WHITE)
            width = 10
            height = 10
        }

        // === Create scatter series ===
        val rs2 = sciChartBuilder.newScatterSeries().withPointMarker(pointMarker)
            .withDataSeries(dataSeries1Second)
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.SPONT_RR_CHART))
            .withXAxisId("OLD").build()
        modifierSpontRR?.showTooltip = true
        modifierSpontRR?.showAxisLabels = true
        modifierSpontRR?.isEnabled = true

        Collections.addAll(binding.trendSecondDynamicsChart.chartModifiers, modifierSpontRR!!)
        UpdateSuspender.using(binding.trendSecondDynamicsChart) {
            Collections.addAll(binding.trendSecondDynamicsChart.xAxes, xPRimaryAxis)
            Collections.addAll(binding.trendSecondDynamicsChart.xAxes, xsecondaryAxis)
            Collections.addAll(binding.trendSecondDynamicsChart.yAxes, yAxis)
            Collections.addAll(binding.trendSecondDynamicsChart.renderableSeries, rs2)
        }

    }

    private fun initThirdGraph(listSize: Double) {
        binding.txtComplianceModuleLoading.visibility = View.GONE
        val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()
        modifierSpontVT = CustomRolloverModifier()
        //For the initial graphs the xprimary Axis and the XSecondary Axis will be updated.
        val xPRimaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, listSize))
            .withMaxAutoTicks(4)
            .withTickLabelStyle(titleXStyle)
            .withAxisId("OLD")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        val xsecondaryAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, listSize))
            .withTickLabelStyle(titleXStyle)
            .withMaxAutoTicks(5)
            .withAxisId("HiddenXAxis")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        // modified at 20 jan 2023

        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withMaxAutoTicks(2)
            .withTickLabelStyle(titleStyle)
            .withDrawMajorBands(true)
            .withAutoRangeMode(AutoRange.Never)
            .withVisibleRange(0.0, 20.0)
            .build()

        binding.trendThirdDynamicsChart.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        binding.trendThirdDynamicsChart.renderableSeriesAreaBorderStyle =
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

        dataSeries1Third = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()

        // === Create point marker ===
        val pointMarker = EllipsePointMarker().apply {
            fillStyle = SolidBrushStyle(Color.WHITE)
            width = 10
            height = 10
        }

        // === Create scatter series ===
        val rs2 = sciChartBuilder.newScatterSeries().withPointMarker(pointMarker)
            .withDataSeries(dataSeries1Third)
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.SPONT_VT_CHART))
            .withXAxisId("OLD").build()

        modifierSpontVT?.showTooltip = true
        modifierSpontVT?.showAxisLabels = true
        modifierSpontVT?.isEnabled = true

        Collections.addAll(binding.trendThirdDynamicsChart.chartModifiers, modifierSpontVT!!)
        UpdateSuspender.using(binding.trendThirdDynamicsChart) {
            Collections.addAll(binding.trendThirdDynamicsChart.xAxes, xPRimaryAxis)
            Collections.addAll(binding.trendThirdDynamicsChart.xAxes, xsecondaryAxis)
            Collections.addAll(binding.trendThirdDynamicsChart.yAxes, yAxis)
            Collections.addAll(binding.trendThirdDynamicsChart.renderableSeries, rs2)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readTrendsViaParamAndDuration()

        binding.txtChartDynamicsFirst.text = defaultParamsIndexFirst
        binding.txtChartDynamicsSecond.text = defaultParamsIndexSecond
        binding.txtChartDynamicsThird.text = defaultParamsIndexThird
    }

    private fun readTrendsViaParamAndDuration() {

        CoroutineScope(Dispatchers.IO).launch {

            val dataFirstChart = FileLogger.readTrendFileAsPerParamAndDuration(
                    Configs.getTrendsFileName(
                        PreferenceManager(requireContext())
                    ), 17, duration
                )
            val dataSecondChart = FileLogger.readTrendFileAsPerParamAndDuration(
                    Configs.getTrendsFileName(
                        PreferenceManager(requireContext())
                    ), 18, duration
                )
            val dataThirdChart = FileLogger.readTrendFileAsPerParamAndDuration(
                    Configs.getTrendsFileName(
                        PreferenceManager(requireContext())
                    ), 19, duration
                )

            // list having data like "time~data"
            // handle data for first chart
            if (dataFirstChart != FileLogger.dataNotFound) {
                val list = dataFirstChart.split("|").asReversed()
                withContext(Dispatchers.Main) {
                    initFirstGraph(list.size.toDouble())
                    dynCompTimeList.clear()
                    dataSeries1First?.clear()
                    for (i in list.indices) {
                        dynCompTimeList.add(list[i].split("~")[0])
                        dataSeries1First?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            }

            // list having data like "time~data"
            // handle data for second chart
            if (dataSecondChart != FileLogger.dataNotFound) {
                val list = dataSecondChart.split("|").asReversed()
                withContext(Dispatchers.Main) {
                    initSecondGraph(list.size.toDouble())
                    spontRRTimeList.clear()
                    dataSeries1Second?.clear()
                    for (i in list.indices){
                        spontRRTimeList.add(list[i].split("~")[0])
                        dataSeries1Second?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            }

            // list having data like "time~data"
            // handle data for third chart
            if (dataThirdChart != FileLogger.dataNotFound) {
                val list = dataThirdChart.split("|")
                withContext(Dispatchers.Main) {
                    initThirdGraph(list.size.toDouble())
                    spontVTTimeList.clear()
                    dataSeries1Third?.clear()
                    for (i in list.indices){
                        spontVTTimeList.add(list[i].split("~")[0])
                        dataSeries1Third?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            } else withContext(Dispatchers.Main) {
                binding.txtComplianceModuleLoading.visibility = View.GONE
                binding.txtComplianceModuleLoading.text = "Trends Not Found"
            }
        }
    }

    // RM scichart
    fun setRollOver(){
        modifierDynComp?.setRolloverAt(currentXValue, currentYValue)
        modifierSpontRR?.setRolloverAt(currentXValue, currentYValue)
        modifierSpontVT?.setRolloverAt(currentXValue, currentYValue)
    }

    fun removeRollover(){
        modifierDynComp?.removeRolloverAt(currentXValue, currentYValue)
        modifierSpontRR?.removeRolloverAt(currentXValue, currentYValue)
        modifierSpontVT?.removeRolloverAt(currentXValue, currentYValue)
    }

}