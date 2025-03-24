package com.agvahealthcare.ventilator_ext.graph.divide_trends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.custom_dialogs.WaveSelectionDialogFragment
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.dashboard.GraphLayoutFragment
import com.agvahealthcare.ventilator_ext.dashboard.chart.GraphType
import com.agvahealthcare.ventilator_ext.dashboard.chart.PressureChartFragment
import com.agvahealthcare.ventilator_ext.databinding.FragmentQuadTrendsBinding
import com.agvahealthcare.ventilator_ext.graph.lungs_dynamics.ComplianceModuleFragment
import com.agvahealthcare.ventilator_ext.graph.lungs_dynamics.LungsDynamicsFragment
import com.agvahealthcare.ventilator_ext.graph.lungs_dynamics.OxygenModuleFragment
import com.agvahealthcare.ventilator_ext.system.settings.onDropDownSelectionListener
import com.agvahealthcare.ventilator_ext.utility.FIFOCAPACITY_CUSTOM_SIZE
import com.agvahealthcare.ventilator_ext.utility.GRAPH_PRESSURE_MAX
import com.agvahealthcare.ventilator_ext.utility.GRAPH_PRESSURE_MIN


class QuadTrendsFragment : GraphLayoutFragment("QuadTrendsGraphFragment") {

    private lateinit var binding : FragmentQuadTrendsBinding
    private var pressureChartFragment: PressureChartFragment? = null
    private var oxygenModuleFragment: OxygenModuleFragment? = null
    private var complianceModuleFragment: ComplianceModuleFragment? = null
    private var lungsDynamicsFragment: LungsDynamicsFragment? = null
    private var mDashBoardViewModel : DashBoardViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuadTrendsBinding.inflate(layoutInflater,container,false)
        return binding.root
    }
    private fun makeNullAllFragment(){
        complianceModuleFragment = null
        oxygenModuleFragment = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
        // RM scichart
        mDashBoardViewModel?.isTouchGraph?.observe(viewLifecycleOwner) {

            if (it) {
                pressureChartFragment?.setRollOver()
                oxygenModuleFragment?.setRollOver()
                complianceModuleFragment?.setRollOver()
            } else {
                pressureChartFragment?.removeRollover()
                oxygenModuleFragment?.removeRollover()
                complianceModuleFragment?.removeRollover()
            }
        }
        initData()

        binding.swapFirstGraph.setOnClickListener {
            if (oxygenModuleFragment == null) initOxygenModuleFragment()
            else initComplianceModuleFragment()
        }
    }

    private fun initData() {
        initPressureChartFragment()
        initOxygenModuleFragment()
        initLungsDynamicsFragment()
    }

    fun setDataGobally(xMaxRange: Double) { pressureChartFragment?.addTextOnMaxRange(xMaxRange) }

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

    fun updateLungsParams(){ lungsDynamicsFragment?.takeIf { it.isVisible }?.apply { readTrendsViaParamAndDuration() } }
    fun updateOxygenAndComplianceParams(){
        oxygenModuleFragment?.takeIf { it.isVisible }?.apply { updateTrendsViaParamAndDuration() }
        complianceModuleFragment?.takeIf { it.isVisible }?.apply { updateTrendsViaParamAndDuration() }
    }

    private fun initOxygenModuleFragment() {
        makeNullAllFragment()
        oxygenModuleFragment = OxygenModuleFragment()
        childFragmentManager.beginTransaction()
            .replace(
                R.id.containerTrendsQuadGraph3,
                oxygenModuleFragment!!,
                oxygenModuleFragment!!::class.java.javaClass.simpleName
            )
            .commit()
    }
    private fun initComplianceModuleFragment() {
        makeNullAllFragment()
        complianceModuleFragment = ComplianceModuleFragment()
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
    fun addGraphPressureData(x: Int, y: Float, trigger: String?) { pressureChartFragment?.addEntry(x, y, trigger) }
    fun addGraphFlowData(x: Int, y: Float) {}
    fun addGraphVolumeData(x: Int, y: Float) {}

    override fun onPause() {
        VentilatorApp.xTestingFlow = IntArray(FIFOCAPACITY_CUSTOM_SIZE) { i -> 0 }
        VentilatorApp.xTestingVolume = IntArray(FIFOCAPACITY_CUSTOM_SIZE) { i -> 0 }
        VentilatorApp.xTestingPressure = IntArray(FIFOCAPACITY_CUSTOM_SIZE) { i -> 0 }
        super.onPause()
    }
}