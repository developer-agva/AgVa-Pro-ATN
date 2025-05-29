package com.agvahealthcare.ventilator_ext.graph.lungs_dynamics

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agvahealthcare.ventilator_ext.dashboard.chart.GraphFragment
import com.agvahealthcare.ventilator_ext.dashboard.chart.GraphType
import com.agvahealthcare.ventilator_ext.databinding.FragmentOxygenModuleBinding
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.scichart.charting.model.dataSeries.IXyDataSeries
import com.scichart.charting.numerics.labelProviders.LabelProviderBase
import com.scichart.charting.numerics.tickProviders.TickProvider
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.AxisAlignment
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.axes.IAxisCore
import com.scichart.core.framework.UpdateSuspender
import com.scichart.core.model.DoubleValues
import com.scichart.data.model.DoubleRange
import com.scichart.drawing.common.FontStyle
import com.scichart.drawing.common.PenStyle
import com.scichart.drawing.common.SolidPenStyle
import com.scichart.drawing.utility.ColorUtil
import com.scichart.extensions.builders.SciChartBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections


class CustomTickProvider(var s: String) : TickProvider() {

    override fun updateTicks(majorTicks: DoubleValues?, minorTicks: DoubleValues?) {

        when (s) {
            "Fio2" -> {
                majorTicks?.apply {
                    clear()
                    add(21.0)
                    add(100.0)
                }

                minorTicks?.apply {
                    clear()
                    add(21.0)
                    add(100.0)
                } // No minor ticks
            }

            "Spo2" -> {
                majorTicks?.apply {
                    clear()
                    add(80.0)
                    add(100.0)
                }

                minorTicks?.apply {
                    clear()
                    add(80.0)
                    add(100.0)
                } // No minor ticks
            }

            "PULSE" -> {
                majorTicks?.apply {
                    clear()
                    add(50.0)
                    add(150.0)
                }

                minorTicks?.apply {
                    clear()
                    add(50.0)
                    add(150.0)
                } // No minor ticks
            }
        }
    }
}

class FixedTickProvider(private val ticks: List<String>) : TickProvider() {

    override fun updateTicks(majorTicks: DoubleValues?, minorTicks: DoubleValues?) {
        majorTicks?.apply {
            clear()
            add(0.0)
            add(1.0)
            add(2.0)
            add(3.0)
            add(4.0)
            add(5.0)
            add(9.0)
            add(12.0)
            add(15.0)
            add(17.0)
            add(19.0)
            add(21.0)
        }

        minorTicks?.apply {
            clear()
            add(0.0)
            add(1.0)
            add(2.0)
            add(3.0)
            add(4.0)
            add(5.0)
            // No minor ticks in this implementation
        }
    }
}

class StringLabelProvider(private val labels: List<String>) : LabelProviderBase<IAxisCore>(IAxisCore::class.java) {
    override fun formatLabel(p0: Comparable<Nothing>?): CharSequence {
        return ""
    }

    override fun formatLabel(dataValue: Double): CharSequence {
        val index = dataValue.toInt()
        return labels.getOrNull(index) ?: ""
    }

    override fun formatCursorLabel(p0: Comparable<Nothing>?): CharSequence {
        return ""
    }

    override fun formatCursorLabel(dataValue: Double): CharSequence {
        return formatLabel(dataValue)
    }
}

class OxygenModuleFragment(private var duration: String) : GraphFragment() {

    val spo2TimeList = ArrayList<String>()
    val prTimeList = ArrayList<String>()
    val fio2TimeList = ArrayList<String>()
    private var defaultParamsIndexFirst = "Spo2"
    private var defaultParamsIndexSecond = "Pulse"
    private var defaultParamsIndexThird = "Fio2"
    private lateinit var binding: FragmentOxygenModuleBinding
    private var dataSeries1First: IXyDataSeries<Int, Float>? = null
    private var dataSeries1Second: IXyDataSeries<Int, Float>? = null
    private var dataSeries1Third: IXyDataSeries<Int, Float>? = null

    val titleStyle = FontStyle(12.0f, ColorUtil.White)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOxygenModuleBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun initFirstGraph(list: List<String>) {

        val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()

        val xPRimaryAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, list.size.toDouble()))
            .withMaxAutoTicks(list.size)
            .withTickLabelStyle(titleStyle)
            .withAxisId("OLD")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        xPRimaryAxis.majorTickLineStyle = SolidPenStyle(Color.WHITE, false, 1f, null)
        xPRimaryAxis.minorTickLineStyle = SolidPenStyle(Color.WHITE, false, 1f, null)
        xPRimaryAxis.labelProvider = StringLabelProvider(spo2TimeList)

        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withAutoTicks(false)
            .withTickLabelStyle(titleStyle)
            .withDrawMajorBands(true)
            .withAutoRangeMode(AutoRange.Never)
            .withVisibleRange(80.0, 100.0)
            .build()

        yAxis.tickProvider = CustomTickProvider("Spo2")

        binding.trendFirstChart.setBackgroundColor(ColorUtil.Black)

        binding.trendFirstChart.renderableSeriesAreaBorderStyle = sciChartBuilder.newPen().withColor(ColorUtil.Transparent).build();

        xPRimaryAxis.visibility = View.VISIBLE

        xPRimaryAxis.drawMajorGridLines = false
        xPRimaryAxis.drawMinorGridLines = false
        xPRimaryAxis.drawMajorBands = false
        xPRimaryAxis.drawMajorTicks = true
        xPRimaryAxis.drawMinorTicks = true

        yAxis.drawMajorGridLines = false
        yAxis.drawMinorGridLines = false
        yAxis.drawMajorBands = false
        yAxis.drawMajorTicks = false
        yAxis.drawMajorTicks = false

        dataSeries1First = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()

        // === Create scatter series ===
        val rs2 = sciChartBuilder.newColumnSeries()
            .withDataSeries(dataSeries1First)
            .withFillColor(Color.WHITE)
            .withStrokeStyle(Color.GRAY, 0f)
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.SPO2_CHART))
            .withXAxisId("OLD").build()

        rs2.dataPointWidth = 0.2

        UpdateSuspender.using(binding.trendFirstChart)
        {
            Collections.addAll(binding.trendFirstChart.xAxes, xPRimaryAxis)
            Collections.addAll(binding.trendFirstChart.yAxes, yAxis)
            Collections.addAll(binding.trendFirstChart.renderableSeries, rs2)
        }
    }
    private fun initSecondGraph(list: List<String>) {

        val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()

        //For the initial graphs the xprimary Axis and the XSecondary Axis will be updated.
        val xPRimaryAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, list.size.toDouble()))
            .withMaxAutoTicks(list.size)
            .withTickLabelStyle(titleStyle)
            .withAxisId("OLD")
            .withAutoRangeMode(AutoRange.Never)
            .build()
        xPRimaryAxis.majorTickLineStyle = SolidPenStyle(Color.WHITE, false, 1f, null)
        xPRimaryAxis.minorTickLineStyle = SolidPenStyle(Color.WHITE, false, 1f, null)
        xPRimaryAxis.labelProvider = StringLabelProvider(prTimeList)
        // modified at 20 jan 2023

        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withAutoTicks(false)
            .withTickLabelStyle(titleStyle)
            .withDrawMajorBands(true)
            .withAutoRangeMode(AutoRange.Never)
            .withVisibleRange(50.0, 150.0)
            .build()

        yAxis.tickProvider = CustomTickProvider("PULSE")

        binding.trendSecondChart.setBackgroundColor(
            ColorUtil.Black
        )
        binding.trendSecondChart.renderableSeriesAreaBorderStyle =
            sciChartBuilder.newPen().withColor(ColorUtil.Transparent).build();


        xPRimaryAxis.drawMajorGridLines = false
        xPRimaryAxis.drawMinorGridLines = false
        xPRimaryAxis.drawMajorBands = false
        xPRimaryAxis.drawMajorTicks = true
        xPRimaryAxis.drawMinorTicks = true

        yAxis.drawMajorGridLines = false
        yAxis.drawMinorGridLines = false
        yAxis.drawMajorBands = false
        yAxis.drawMajorTicks = false
        yAxis.drawMajorTicks = false

        dataSeries1Second = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()

        // === Create scatter series ===
        val rs2 = sciChartBuilder.newColumnSeries()
            .withDataSeries(dataSeries1Second)
            .withFillColor(Color.RED)
            .withStrokeStyle(Color.RED, 0f)
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.PR_CHART))
            .withXAxisId("OLD").build()

        rs2.dataPointWidth = 0.2

        UpdateSuspender.using(binding.trendSecondChart) {
            Collections.addAll(binding.trendSecondChart.xAxes, xPRimaryAxis)
            Collections.addAll(binding.trendSecondChart.yAxes, yAxis)
            Collections.addAll(binding.trendSecondChart.renderableSeries, rs2)
        }
    }
    private fun initThirdGraph(list: List<String>) {

        val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()

        //For the initial graphs the xprimary Axis and the XSecondary Axis will be updated.
        val xPRimaryAxis = sciChartBuilder.newNumericAxis()
            .withVisibleRange(DoubleRange(0.0, list.size.toDouble()))
            .withMaxAutoTicks(list.size)
            .withTickLabelStyle(titleStyle)
            .withAxisId("OLD")
            .withAutoRangeMode(AutoRange.Never)
            .build()

        xPRimaryAxis.majorTickLineStyle = SolidPenStyle(Color.WHITE, false, 1f, null)
        xPRimaryAxis.minorTickLineStyle = SolidPenStyle(Color.WHITE, false, 1f, null)
        xPRimaryAxis.labelProvider = StringLabelProvider(fio2TimeList)

        // modified at 20 jan 2023

        val yAxis: IAxis = sciChartBuilder.newNumericAxis()
            .withAxisAlignment(AxisAlignment.Left)
            .withAutoTicks(false)
            .withTickLabelStyle(titleStyle)
            .withDrawMajorBands(true)
            .withAutoRangeMode(AutoRange.Never)
            .withVisibleRange(21.0, 100.0)
            .build()


        yAxis.tickProvider = CustomTickProvider("Fio2")

        binding.trendThirdChart.setBackgroundColor(
            ColorUtil.Black
        )
        binding.trendThirdChart.renderableSeriesAreaBorderStyle =
            sciChartBuilder.newPen().withColor(ColorUtil.Transparent).build();

        xPRimaryAxis.drawMajorGridLines = false
        xPRimaryAxis.drawMinorGridLines = false
        xPRimaryAxis.drawMajorBands = false
        xPRimaryAxis.drawMajorTicks = true
        xPRimaryAxis.drawMinorTicks = true

        yAxis.drawMajorGridLines = false
        yAxis.drawMinorGridLines = false
        yAxis.drawMajorBands = false
        yAxis.drawMajorTicks = false
        yAxis.drawMajorTicks = false

        dataSeries1Third = sciChartBuilder.newXyDataSeries(
            Int::class.javaObjectType,
            Float::class.javaObjectType
        ).withAcceptsUnsortedData().build()


        // === Create scatter series ===
        val rs2 = sciChartBuilder.newColumnSeries()
            .withDataSeries(dataSeries1Third)
            .withFillColor(Color.GREEN)
            .withStrokeStyle(Color.GREEN, 0f)
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.FIO2_CHART))
            .withXAxisId("OLD").build()



        rs2.dataPointWidth = 0.2

        UpdateSuspender.using(binding.trendThirdChart) {
            Collections.addAll(binding.trendThirdChart.xAxes, xPRimaryAxis)
            Collections.addAll(binding.trendThirdChart.yAxes, yAxis)
            Collections.addAll(binding.trendThirdChart.renderableSeries, rs2)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtChartFirst.text = defaultParamsIndexFirst
        binding.txtChartSecond.text = defaultParamsIndexSecond
        binding.txtChartThird.text = defaultParamsIndexThird

        readTrendsViaParamAndDuration(duration)
    }

    private fun readTrendsViaParamAndDuration(duration: String) {

        CoroutineScope(Dispatchers.IO).launch {

            val dataFirstChart = FileLogger.readTrendFileAsPerParamAndDuration(
                Configs.getTrendsFileName(
                    PreferenceManager(requireContext())
                ), 15, duration
            )
            val dataSecondChart = FileLogger.readTrendFileAsPerParamAndDuration(
                Configs.getTrendsFileName(
                    PreferenceManager(requireContext())
                ), 16, duration
            )
            val dataThirdChart = FileLogger.readTrendFileAsPerParamAndDuration(
                Configs.getTrendsFileName(
                    PreferenceManager(requireContext())
                ), 9, duration
            )

            // list having data like "time~data"
            if (dataFirstChart != FileLogger.dataNotFound) {
                val list = dataFirstChart.split("|").asReversed()
                withContext(Dispatchers.Main) {
                    spo2TimeList.clear()
                    dataSeries1First?.clear()
                    initFirstGraph(list)
                    for (i in list.indices) {
                        spo2TimeList.add(list[i].split("~")[0])
                        Log.i("OxygenModuleFragment", "readTrendsViaParamAndDuration: ${list[i].split("~")[0]}")
                        dataSeries1First?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            }
            
            if (dataSecondChart != FileLogger.dataNotFound) {
                val list = dataSecondChart.split("|").asReversed()
                withContext(Dispatchers.Main) {
                    prTimeList.clear()
                    dataSeries1Second?.clear()
                    initSecondGraph(list)
                    for (i in list.indices) {
                        prTimeList.add(list[i].split("~")[0])
                        dataSeries1Second?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            }

            if (dataThirdChart != FileLogger.dataNotFound) {
                val list = dataThirdChart.split("|").asReversed()
                withContext(Dispatchers.Main) {
                    dataSeries1Third?.clear()
                    fio2TimeList.clear()
                    initThirdGraph(list)
                    for (i in list.indices) {
                        fio2TimeList.add(list[i].split("~")[0])
                        dataSeries1Third?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            } else withContext(Dispatchers.Main) {
                binding.txtOxygenModuleLoading.text = "Trends Not Found"
                binding.txtOxygenModuleLoading.visibility = View.VISIBLE
            }
        }
    }

    // RM scichart
    fun setRollOver() {}
    fun removeRollover() {}
}