package com.agvahealthcare.ventilator_ext.graph.divide_trends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.dashboard.GraphLayoutFragment
import com.agvahealthcare.ventilator_ext.dashboard.chart.FlowChartFragment
import com.agvahealthcare.ventilator_ext.dashboard.chart.GraphType
import com.agvahealthcare.ventilator_ext.dashboard.chart.PressureChartFragment
import com.agvahealthcare.ventilator_ext.graph.lungs_dynamics.ComplianceModuleFragment
import com.agvahealthcare.ventilator_ext.graph.lungs_dynamics.LungsDynamicsFragment
import com.agvahealthcare.ventilator_ext.graph.lungs_dynamics.OxygenModuleFragment
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.system.settings.CommonSetupAdapter
import com.agvahealthcare.ventilator_ext.system.settings.onDropDownSelectionListener
import com.agvahealthcare.ventilator_ext.utility.FIFOCAPACITY_CUSTOM_SIZE
import com.agvahealthcare.ventilator_ext.utility.GRAPH_PRESSURE_MAX
import com.agvahealthcare.ventilator_ext.utility.GRAPH_PRESSURE_MIN
import com.agvahealthcare.ventilator_ext.utility.TRIO_GRAPH_FLOW_MAX
import com.agvahealthcare.ventilator_ext.utility.TRIO_GRAPH_FLOW_MIN
import kotlinx.android.synthetic.main.fragment_quad_trends.moduleRecyclerView
import kotlinx.android.synthetic.main.fragment_quad_trends.moduleSwitchLayout
import kotlinx.android.synthetic.main.fragment_quad_trends.moduleSwitchText
import kotlinx.android.synthetic.main.fragment_quad_trends.trendDurationLayout
import kotlinx.android.synthetic.main.fragment_quad_trends.trendDurationText
import kotlinx.android.synthetic.main.fragment_quad_trends.trendsRecyclerView

class QuadTrendsFragment : GraphLayoutFragment("QuadTrendsGraphFragment"),
    onDropDownSelectionListener {

    private var pressureChartFragment: PressureChartFragment? = null
    private var flowChartFragment: FlowChartFragment? = null
    private var oxygenModuleFragment: OxygenModuleFragment? = null
    private var complianceModuleFragment: ComplianceModuleFragment? = null
    private var lungsDynamicsFragment: LungsDynamicsFragment? = null
    private var mDashBoardViewModel: DashBoardViewModel? = null
    private var clickedTrendTile = false
    private var clickedModuleTile = false
    private var trendParamList = arrayListOf("1 hour", "8 hours", "12 hours", "24 hours")
    private var moduleList = ArrayList<String>()
    private var mTrendAdapter: CommonSetupAdapter? = null
    private var prefManager: PreferenceManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quad_trends, container, false)
        return view
    }

    private fun makeNullAllFragment() {
        complianceModuleFragment = null
        flowChartFragment = null
        oxygenModuleFragment = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
        prefManager = PreferenceManager(requireContext())
        VentilatorApp.testingDashBoardViewModel = mDashBoardViewModel
        // RM scichart
        mDashBoardViewModel?.isTouchGraph?.observe(viewLifecycleOwner) {

            android.util.Log.i("asdawd", "123124536457")

            if (it) {
                pressureChartFragment?.setRollOver()
                oxygenModuleFragment?.setRollOver()
                complianceModuleFragment?.setRollOver()
                flowChartFragment?.setRollOver()
            } else {
                pressureChartFragment?.removeRollover()
                oxygenModuleFragment?.removeRollover()
                complianceModuleFragment?.removeRollover()
                flowChartFragment?.removeRollover()
            }
        }
        initData()
        trendDurationText.text = VentilatorApp.choosedHours
        moduleSwitchText.text = "OXYGEN"

        moduleSwitchLayout.setOnClickListener {
            moduleList.clear()
            if (oxygenModuleFragment == null) moduleList.add("OXYGEN")
            if (complianceModuleFragment == null) moduleList.add("COMPLIANCE")
            if (flowChartFragment == null) moduleList.add("FLOW")
            clickedTrendTile = false
            clickedModuleTile = !clickedModuleTile
            trendsRecyclerView.visibility = View.GONE
            moduleRecyclerView.visibility =
                if (moduleRecyclerView.isVisible) View.GONE else View.VISIBLE
            setupTrendDropDownAdapter(moduleRecyclerView, moduleList)
        }

        trendDurationLayout.setOnClickListener {
            clickedModuleTile = false
            clickedTrendTile = !clickedTrendTile
            moduleRecyclerView.visibility = View.GONE
            trendsRecyclerView.visibility =
                if (trendsRecyclerView.isVisible) View.GONE else View.VISIBLE
            setupTrendDropDownAdapter(trendsRecyclerView, trendParamList)
        }
    }

    private fun setupTrendDropDownAdapter(recyclerView: RecyclerView, list: ArrayList<String>) {
        mTrendAdapter = CommonSetupAdapter(list, this@QuadTrendsFragment)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mTrendAdapter
        }
    }

    override fun onItemSelect(text: String, colorInt: Int) {

        trendsRecyclerView.visibility = View.GONE
        moduleRecyclerView.visibility = View.GONE
        mTrendAdapter = null

        if (clickedTrendTile) {
            trendDurationText.text = text
            VentilatorApp.choosedHours = text
            (requireActivity() as DashBoardActivity).changeOxygenAndComplianceDurationListener(text)
        }
        if (clickedModuleTile) {
            when (text) {
                "OXYGEN" -> {
                    moduleSwitchText.text = "OXYGEN"
                    initOxygenModuleFragment(VentilatorApp.choosedHours)
                    trendDurationLayout.visibility = View.VISIBLE
                }

                "COMPLIANCE" -> {
                    moduleSwitchText.text = "COMPLIANCE"
                    initComplianceModuleFragment(VentilatorApp.choosedHours)
                    trendDurationLayout.visibility = View.VISIBLE
                }

                "FLOW" -> {
                    moduleSwitchText.text = "FLOW"
                    (requireActivity() as DashBoardActivity).resetFlowChartCounter()
                    initFlowChartFragment()
                    trendDurationLayout.visibility = View.GONE
                }
            }
        }
        clickedTrendTile = false
        clickedModuleTile = false
    }

    private fun initData() {
        initPressureChartFragment()
        initOxygenModuleFragment(VentilatorApp.choosedHours)
        initLungsDynamicsFragment()
    }

    fun setDataGobally(xMaxRange: Double) {
        pressureChartFragment?.addTextOnMaxRange(xMaxRange)
        flowChartFragment?.addTextOnMaxRange(xMaxRange)
    }

    private fun initPressureChartFragment() {
        pressureChartFragment = PressureChartFragment.newInstance(
            GraphType.PRESSURE,
            GRAPH_PRESSURE_MIN,
            GRAPH_PRESSURE_MAX
        )
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrendsQuadGraph1,
                pressureChartFragment!!,
                pressureChartFragment!!::class.java.javaClass.simpleName
            )
            .commit()
    }

    private fun initFlowChartFragment() {
        makeNullAllFragment()
        flowChartFragment =
            FlowChartFragment.newInstance(GraphType.FLOW, TRIO_GRAPH_FLOW_MIN, TRIO_GRAPH_FLOW_MAX)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrendsQuadGraph3,
                flowChartFragment!!,
                flowChartFragment!!::class.java.javaClass.simpleName
            )
            .commit()
    }

    fun updateLungsParams() {
        lungsDynamicsFragment?.takeIf { it.isVisible }?.apply { readTrendsViaParamAndDuration() }
    }

    fun updateOxygenAndComplianceParams(duration: String) {
        oxygenModuleFragment?.takeIf { it.isVisible }
            ?.apply { initOxygenModuleFragment(duration) }
        complianceModuleFragment?.takeIf { it.isVisible }
            ?.apply { initComplianceModuleFragment(duration) }
    }

    private fun initOxygenModuleFragment(duration: String) {
        makeNullAllFragment()
        oxygenModuleFragment = OxygenModuleFragment(duration)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrendsQuadGraph3,
                oxygenModuleFragment!!,
                oxygenModuleFragment!!::class.java.javaClass.simpleName
            )
            .commit()
    }

    private fun initComplianceModuleFragment(duration: String) {
        makeNullAllFragment()
        complianceModuleFragment = ComplianceModuleFragment(duration)
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrendsQuadGraph3,
                complianceModuleFragment!!,
                complianceModuleFragment!!::class.java.javaClass.simpleName
            )
            .commit()
    }

    private fun initLungsDynamicsFragment() {
        lungsDynamicsFragment = LungsDynamicsFragment()
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrendsQuadGraph4,
                lungsDynamicsFragment!!,
                lungsDynamicsFragment!!::class.java.javaClass.simpleName
            )
            .commit()
    }

    fun clearSeries() {}
    fun addGraphPressureData(x: Int, y: Float, trigger: String?) {
        pressureChartFragment?.addEntry(x, y, trigger)
    }

    fun addGraphFlowData(x: Int, y: Float) {
        flowChartFragment?.addEntry(x, y)
    }

    fun addGraphVolumeData(x: Int, y: Float) {}

    override fun onPause() {
        VentilatorApp.xTestingFlow = IntArray(FIFOCAPACITY_CUSTOM_SIZE) { i -> 0 }
        VentilatorApp.xTestingVolume = IntArray(FIFOCAPACITY_CUSTOM_SIZE) { i -> 0 }
        VentilatorApp.xTestingPressure = IntArray(FIFOCAPACITY_CUSTOM_SIZE) { i -> 0 }
        super.onPause()
    }
}