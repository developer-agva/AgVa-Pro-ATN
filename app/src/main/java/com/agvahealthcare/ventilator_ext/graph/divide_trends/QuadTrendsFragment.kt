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


enum class FragmentSwitchOptions {
    FIRST_FRAGMENT,
    SECOND_FRAGMENT
}

class QuadTrendsFragment : GraphLayoutFragment("QuadTrendsGraphFragment"), OnDismissDialogListener,
    onDropDownSelectionListener {

    private lateinit var binding : FragmentQuadTrendsBinding
    private var clickedFragmentOptions: FragmentSwitchOptions? = null
    private var pressureChartFragment: PressureChartFragment? = null
    private var oxygenModuleFragment: OxygenModuleFragment? = null
    private var complianceModuleFragment: ComplianceModuleFragment? = null
    private var lungsDynamicsFragment: LungsDynamicsFragment? = null
    private var waveSelectionDialogFragment: WaveSelectionDialogFragment? = null
    private var mDashBoardViewModel : DashBoardViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuadTrendsBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    private fun getCurrentNotSelectedModule() : ArrayList<String>{

        val tempList = arrayListOf(
            "OXYGEN MODULE",
            "COMPLIANCE MODULE",
            "LUNGS MODULE"
        )

        oxygenModuleFragment?.takeIf { it.isVisible }?.apply { tempList.remove("OXYGEN MODULE") }
        complianceModuleFragment?.takeIf { it.isVisible }?.apply { tempList.remove("COMPLIANCE MODULE") }
        lungsDynamicsFragment?.takeIf { it.isVisible }?.apply { tempList.remove("LUNGS MODULE") }

        return tempList
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

        binding.containerTrendsQuadGraph3.setOnLongClickListener {
            clickedFragmentOptions = FragmentSwitchOptions.FIRST_FRAGMENT

            waveSelectionDialogFragment?.dismiss()
            waveSelectionDialogFragment = WaveSelectionDialogFragment(
                this, this, getCurrentNotSelectedModule()
            )
            waveSelectionDialogFragment?.show(
                childFragmentManager,
                "WAVE"
            )
            return@setOnLongClickListener true
        }

        binding.containerTrendsQuadGraph4.setOnLongClickListener {
            clickedFragmentOptions = FragmentSwitchOptions.SECOND_FRAGMENT

            waveSelectionDialogFragment?.dismiss()
            waveSelectionDialogFragment = WaveSelectionDialogFragment(
                this, this, getCurrentNotSelectedModule()
            )
            waveSelectionDialogFragment?.show(
                childFragmentManager,
                "WAVE"
            )
            return@setOnLongClickListener true
        }
    }

    private fun initData() {
        initPressureChartFragment()
        initOxygenModuleFragment(R.id.containerTrendsQuadGraph3)
        initLungsDynamicsFragment(R.id.containerTrendsQuadGraph4)
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

    private fun initOxygenModuleFragment(container: Int) {
        oxygenModuleFragment = OxygenModuleFragment()
        childFragmentManager.beginTransaction()
            .replace(
                container,
                oxygenModuleFragment!!,
                oxygenModuleFragment!!::class.java.javaClass.simpleName
            )
            .commit()
    }

    private fun initComplianceModuleFragment(container: Int) {
        complianceModuleFragment = ComplianceModuleFragment()
        childFragmentManager.beginTransaction()
            .replace(
                container,
                complianceModuleFragment!!,
                complianceModuleFragment!!::class.java.javaClass.simpleName
            )
            .commit()
    }

    private fun initLungsDynamicsFragment(container: Int) {
        lungsDynamicsFragment = LungsDynamicsFragment()
        childFragmentManager.beginTransaction()
            .replace(
                container,
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

    override fun handleDialogClose() { waveSelectionDialogFragment?.dismiss() }

    override fun onItemSelect(text: String, colorInt: Int) {
        when (text) {

            "OXYGEN MODULE" -> {
                when(clickedFragmentOptions){
                    FragmentSwitchOptions.FIRST_FRAGMENT ->{
                        initOxygenModuleFragment(R.id.containerTrendsQuadGraph3)
                    }
                    FragmentSwitchOptions.SECOND_FRAGMENT ->{
                        initOxygenModuleFragment(R.id.containerTrendsQuadGraph4)
                    }
                    else ->{}
                }
            }

            "COMPLIANCE MODULE" -> {
                when(clickedFragmentOptions){
                    FragmentSwitchOptions.FIRST_FRAGMENT ->{
                        initComplianceModuleFragment(R.id.containerTrendsQuadGraph3)
                    }
                    FragmentSwitchOptions.SECOND_FRAGMENT ->{
                        initComplianceModuleFragment(R.id.containerTrendsQuadGraph4)
                    }
                    else ->{}
                }
            }

            "LUNGS MODULE" -> {
                when(clickedFragmentOptions){
                    FragmentSwitchOptions.FIRST_FRAGMENT ->{
                        initLungsDynamicsFragment(R.id.containerTrendsQuadGraph3)
                    }
                    FragmentSwitchOptions.SECOND_FRAGMENT ->{
                        initLungsDynamicsFragment(R.id.containerTrendsQuadGraph4)
                    }
                    else ->{}
                }
            }
        }
        waveSelectionDialogFragment?.dismiss()
        clickedFragmentOptions = null
    }
}