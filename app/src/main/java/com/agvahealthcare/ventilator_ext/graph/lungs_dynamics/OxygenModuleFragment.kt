package com.agvahealthcare.ventilator_ext.graph.lungs_dynamics

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentXValue
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentYValue
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.fio2TimeList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.prTimeList
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.spo2TimeList
import com.agvahealthcare.ventilator_ext.dashboard.chart.GraphFragment
import com.agvahealthcare.ventilator_ext.dashboard.chart.GraphType
import com.agvahealthcare.ventilator_ext.databinding.FragmentOxygenModuleBinding
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.scichart.charting.model.dataSeries.IXyDataSeries
import com.scichart.charting.visuals.axes.AutoRange
import com.scichart.charting.visuals.axes.AxisAlignment
import com.scichart.charting.visuals.axes.IAxis
import com.scichart.charting.visuals.renderableSeries.IRenderableSeries
import com.scichart.core.framework.UpdateSuspender
import com.scichart.data.model.DoubleRange
import com.scichart.drawing.common.FontStyle
import com.scichart.drawing.utility.ColorUtil
import com.scichart.extensions.builders.SciChartBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections


class OxygenModuleFragment : GraphFragment() {

    private var defaultParamsIndexFirst = "Spo2"
    private var defaultParamsIndexSecond = "Pulse"
    private var defaultParamsIndexThird = "Fio2"
    private lateinit var binding : FragmentOxygenModuleBinding
    private var dataSeries1First: IXyDataSeries<Int, Float>? = null
    private var dataSeries1Second: IXyDataSeries<Int, Float>? = null
    private var dataSeries1Third: IXyDataSeries<Int, Float>? = null

    private var modifierSpo2 : GraphFragment.CustomRolloverModifier? = null
    private var modifierPR : GraphFragment.CustomRolloverModifier? = null
    private var modifierFio2 : GraphFragment.CustomRolloverModifier? = null

    val titleStyle = FontStyle(14.0f, ColorUtil.White)
    val titleXStyle = FontStyle(14.0f, ColorUtil.Black)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOxygenModuleBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    private fun initFirstGraph(listSize: Double) {

        val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()
        modifierSpo2 = CustomRolloverModifier()

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
            .withVisibleRange(0.0, 120.0)
            .build()

        binding.trendFirstChart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.trendFirstChart.renderableSeriesAreaBorderStyle =
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

        val rs2 = sciChartBuilder.newSplineLineSeries()
            .withStrokeStyle(
                sciChartBuilder.newPen().withColor(resources.getColor(R.color.white))
                    .withThickness(3f).build()
            )
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.SPO2_CHART))
            .withDataSeries(dataSeries1First)
            .withXAxisId("OLD")
            .build()

        modifierSpo2?.showTooltip = true
        modifierSpo2?.showAxisLabels = true
        modifierSpo2?.isEnabled = true

        Collections.addAll(binding.trendFirstChart.chartModifiers, modifierSpo2!!)
        UpdateSuspender.using(binding.trendFirstChart) {
            Collections.addAll(binding.trendFirstChart.xAxes, xPRimaryAxis)
            Collections.addAll(binding.trendFirstChart.xAxes, xsecondaryAxis)
            Collections.addAll(binding.trendFirstChart.yAxes, yAxis)
            Collections.addAll(binding.trendFirstChart.renderableSeries, rs2)
        }

    }

    private fun initSecondGraph(listSize: Double) {

        val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()
        modifierPR = CustomRolloverModifier()
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
            .withVisibleRange(0.0, 200.0)
            .build()

        binding.trendSecondChart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.trendSecondChart.renderableSeriesAreaBorderStyle =
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

        val rs2: IRenderableSeries = sciChartBuilder.newSplineLineSeries()
            .withStrokeStyle(
                sciChartBuilder.newPen()
                    .withColor(resources.getColor(R.color.red))
                    .withThickness(3f).build()
            )
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.PR_CHART))
            .withOpacity(1.0f)
            .withDataSeries(dataSeries1Second)
            .withXAxisId("OLD")
            .build()
        modifierPR?.showTooltip = true
        modifierPR?.showAxisLabels = true
        modifierPR?.isEnabled = true

        Collections.addAll(binding.trendSecondChart.chartModifiers, modifierPR!!)
        UpdateSuspender.using(binding.trendSecondChart) {
            Collections.addAll(binding.trendSecondChart.xAxes, xPRimaryAxis)
            Collections.addAll(binding.trendSecondChart.xAxes, xsecondaryAxis)
            Collections.addAll(binding.trendSecondChart.yAxes, yAxis)
            Collections.addAll(binding.trendSecondChart.renderableSeries, rs2)
        }

    }

    private fun initThirdGraph(listSize: Double) {

        binding.txtOxygenModuleLoading.visibility = View.GONE

        val sciChartBuilder: SciChartBuilder = SciChartBuilder.instance()
        modifierFio2 = CustomRolloverModifier()

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
            .withVisibleRange(0.0, 105.0)
            .build()

        binding.trendThirdChart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        binding.trendThirdChart.renderableSeriesAreaBorderStyle =
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

        val rs2: IRenderableSeries = sciChartBuilder.newSplineLineSeries()
            .withStrokeStyle(
                sciChartBuilder.newPen()
                    .withColor(resources.getColor(R.color.green))
                    .withThickness(3f).build()
            )
            .withSeriesInfoProvider(CustomSeriesInfoProvider(GraphType.FIO2_CHART))
            .withOpacity(1.0f)
            .withDataSeries(dataSeries1Third)
            .withXAxisId("OLD")
            .build()

        modifierFio2?.showTooltip = true
        modifierFio2?.showAxisLabels = true
        modifierFio2?.isEnabled = true

        Collections.addAll(binding.trendThirdChart.chartModifiers, modifierFio2!!)
        UpdateSuspender.using(binding.trendThirdChart) {
            Collections.addAll(binding.trendThirdChart.xAxes, xPRimaryAxis)
            Collections.addAll(binding.trendThirdChart.xAxes, xsecondaryAxis)
            Collections.addAll(binding.trendThirdChart.yAxes, yAxis)
            Collections.addAll(binding.trendThirdChart.renderableSeries, rs2)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readTrendsViaParamAndDuration()

        binding.txtChartFirst.text = defaultParamsIndexFirst
        binding.txtChartSecond.text = defaultParamsIndexSecond
        binding.txtChartThird.text = defaultParamsIndexThird
    }

    fun updateTrendsViaParamAndDuration(duration :String) {
        Log.i("value_lungs", "read oxygen module $duration")
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

            Log.i("value_lungs", "$dataFirstChart - $dataSecondChart - $dataThirdChart")
            // list having data like "time~data"
            // handle data for first chart
            if (dataFirstChart != FileLogger.dataNotFound) {
                val list = dataFirstChart.split("|")
                withContext(Dispatchers.Main) {
                    dataSeries1First?.clear()
                    spo2TimeList.clear()
                    for (i in list.indices) {
                        spo2TimeList.add(list[i].split("~")[0])
                        dataSeries1First?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            }

            // list having data like "time~data"
            // handle data for second chart
            if (dataSecondChart != FileLogger.dataNotFound) {
                val list = dataSecondChart.split("|")
                withContext(Dispatchers.Main) {
                    dataSeries1Second?.clear()
                    prTimeList.clear()
                    for (i in list.indices) {
                        prTimeList.add(list[i].split("~")[0])
                        dataSeries1Second?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            }

            // list having data like "time~data"
            // handle data for third chart
            if (dataThirdChart != FileLogger.dataNotFound) {
                val list = dataThirdChart.split("|")
                withContext(Dispatchers.Main) {
                    dataSeries1Third?.clear()
                    fio2TimeList.clear()
                    for (i in list.indices) {
                        fio2TimeList.add(list[i].split("~")[0])
                        dataSeries1Third?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            } else withContext(Dispatchers.Main) { binding.txtOxygenModuleLoading.visibility = View.GONE }
        }
    }

    private fun readTrendsViaParamAndDuration() {
        Log.i("value_lungs", "default oxygen module")
        CoroutineScope(Dispatchers.IO).launch {

            val dataFirstChart = FileLogger.readTrendFileAsPerParamAndDuration(
                Configs.getTrendsFileName(
                    PreferenceManager(requireContext())
                ), 15, "one_hour"
            )
            val dataSecondChart = FileLogger.readTrendFileAsPerParamAndDuration(
                Configs.getTrendsFileName(
                    PreferenceManager(requireContext())
                ), 16, "one_hour"
            )
            val dataThirdChart = FileLogger.readTrendFileAsPerParamAndDuration(
                Configs.getTrendsFileName(
                    PreferenceManager(requireContext())
                ), 9, "one_hour"
            )

            Log.i("value_lungs", "$dataFirstChart - $dataSecondChart - $dataThirdChart")
            // list having data like "time~data"
            // handle data for first chart
            if (dataFirstChart != FileLogger.dataNotFound) {
                val list = dataFirstChart.split("|")
                withContext(Dispatchers.Main) {
                    initFirstGraph(list.size.toDouble())
                    spo2TimeList.clear()
                    for (i in list.indices) {
                        spo2TimeList.add(list[i].split("~")[0])
                        dataSeries1First?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            }

            // list having data like "time~data"
            // handle data for second chart
            if (dataSecondChart != FileLogger.dataNotFound) {
                val list = dataSecondChart.split("|")
                withContext(Dispatchers.Main) {
                    initSecondGraph(list.size.toDouble())
                    prTimeList.clear()
                    for (i in list.indices) {
                        prTimeList.add(list[i].split("~")[0])
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
                    fio2TimeList.clear()
                    for (i in list.indices) {
                        fio2TimeList.add(list[i].split("~")[0])
                        dataSeries1Third?.append(i, list[i].split("~")[1].toFloat())
                    }
                }
            } else withContext(Dispatchers.Main) { binding.txtOxygenModuleLoading.visibility = View.GONE }
        }
    }

    // RM scichart
    fun setRollOver(){
        modifierSpo2?.setRolloverAt(currentXValue, currentYValue)
        modifierPR?.setRolloverAt(currentXValue, currentYValue)
        modifierFio2?.setRolloverAt(currentXValue, currentYValue)
    }

    fun removeRollover(){
        modifierSpo2?.removeRolloverAt(currentXValue, currentYValue)
        modifierPR?.removeRolloverAt(currentXValue, currentYValue)
        modifierFio2?.removeRolloverAt(currentXValue, currentYValue)
    }
}