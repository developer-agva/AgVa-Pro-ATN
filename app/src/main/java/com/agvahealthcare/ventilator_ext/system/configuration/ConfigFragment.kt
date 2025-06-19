package com.agvahealthcare.ventilator_ext.system.configuration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.TUBE_COMPLIANCE_CALIBRATION
import com.agvahealthcare.ventilator_ext.utility.TUBE_RESISTANCE_CALIBRATION
import com.agvahealthcare.ventilator_ext.utility.VENTILATOR_ACK
import com.agvahealthcare.ventilator_ext.utility.utils.IntentFactory
import kotlinx.android.synthetic.main.fragment_config.btnATN
import kotlinx.android.synthetic.main.fragment_config.btnATP
import kotlinx.android.synthetic.main.fragment_config.btnEnableDynamicLungsModule
import kotlinx.android.synthetic.main.fragment_config.btnEnableTestingParams
import kotlinx.android.synthetic.main.fragment_config.btnExhaleValve
import kotlinx.android.synthetic.main.fragment_config.btnExpFlow
import kotlinx.android.synthetic.main.fragment_config.btnInspFlow
import kotlinx.android.synthetic.main.fragment_config.btnLeakTest
import kotlinx.android.synthetic.main.fragment_config.btnNeo
import kotlinx.android.synthetic.main.fragment_config.btnOxygen
import kotlinx.android.synthetic.main.fragment_config.btnPeepAlarm
import kotlinx.android.synthetic.main.fragment_config.btnTubeComp
import kotlinx.android.synthetic.main.fragment_config.btnTubeResistance
import kotlinx.android.synthetic.main.fragment_config.btnTurbine

enum class VentilatorType {
    ATN,
    ATP,
    ONLY_NEO
}

class ConfigFragment : Fragment() {

    private var preferenceManager: PreferenceManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        initTypeFromPreferences()
        initCalibrationFromPreferences()
        initOthersFromPreferences()

        btnATN.setOnClickListener {
            preferenceManager?.setVentilatorType(VentilatorType.ATN)
            initTypeFromPreferences()
        }

        btnATP.setOnClickListener {
            preferenceManager?.setVentilatorType(VentilatorType.ATP)
            initTypeFromPreferences()
        }

        btnNeo.setOnClickListener {
            preferenceManager?.setVentilatorType(VentilatorType.ONLY_NEO)
            initTypeFromPreferences()
        }

        btnTurbine.setOnClickListener {
            broadcastAcknowledgement("ACK5019")
        }
        btnExpFlow.setOnClickListener {
            broadcastAcknowledgement("ACK5029")
        }
        btnExhaleValve.setOnClickListener {
            broadcastAcknowledgement("ACK5079")
        }
        btnLeakTest.setOnClickListener {
            broadcastAcknowledgement("ACK4022")
        }
        btnInspFlow.setOnClickListener {
            broadcastAcknowledgement("ACK5122")
        }
        btnOxygen.setOnClickListener {
            broadcastAcknowledgement("ACK5089")
        }

        btnTubeComp.setOnClickListener {
            broadcastTubeComplianceResponse("0.22")
        }
        btnTubeResistance.setOnClickListener {
            broadcastTubeResistanceResponse("0.22")
        }

        btnPeepAlarm.setOnClickListener {
            preferenceManager?.savePeepAlarmStatus(!preferenceManager?.readPeepAlarmStatus()!!)
            initOthersFromPreferences()
        }
        
        btnEnableDynamicLungsModule.setOnClickListener { 
            preferenceManager?.saveDynamicLungsModuleStatus(!preferenceManager?.readDynamicLungsModuleStatus()!!)
            initOthersFromPreferences()
        }
        
        btnEnableTestingParams.setOnClickListener {
            preferenceManager?.saveDebugParamsStatus(!preferenceManager?.readDebugParamsStatus()!!)
            initOthersFromPreferences()
        }
    }

    private fun broadcastAcknowledgement(ack: String) {
        Log.w("ACK CHECK", ack)
        val i = Intent(IntentFactory.ACTION_ACK_AVAILABLE)
        i.putExtra(VENTILATOR_ACK, ack)
        requireActivity().sendBroadcast(i)
    }

    private fun broadcastTubeComplianceResponse(tubeComplianceResponse: String) {
        Log.i("testingCompliance", "DATA : $tubeComplianceResponse")
        val i = Intent(IntentFactory.ACTION_COMPLIANCE_CALIBRATION_RESPONSE)
        i.putExtra(TUBE_COMPLIANCE_CALIBRATION, tubeComplianceResponse)
        requireActivity().sendBroadcast(i)
    }

    private fun broadcastTubeResistanceResponse(tubeResistanceResponse: String) {
        Log.i("testingResistance", "DATA : $tubeResistanceResponse")
        val i = Intent(IntentFactory.ACTION_RESISTANCE_CALIBRATION_RESPONSE)
        i.putExtra(TUBE_RESISTANCE_CALIBRATION, tubeResistanceResponse)
        requireActivity().sendBroadcast(i)
    }

    private fun initOthersFromPreferences() {
        preferenceManager?.apply {

            if (readLeakTestCalibrationStatus()) {
                btnPeepAlarm.setBackgroundColor(resources.getColor(R.color.racing_green))
                btnPeepAlarm.text = "Disable PEEP Alarm"
            } else {
                btnPeepAlarm.setBackgroundColor(resources.getColor(R.color.light_grey))
                btnPeepAlarm.text = "Enable PEEP Alarm"
            }

            if (readDynamicLungsModuleStatus()){
                btnEnableDynamicLungsModule.setBackgroundColor(resources.getColor(R.color.racing_green))
                btnEnableDynamicLungsModule.text = "Disable Dynamic Lungs"
            } else {
                btnEnableDynamicLungsModule.setBackgroundColor(resources.getColor(R.color.light_grey))
                btnEnableDynamicLungsModule.text = "Enable Dynamic Lungs"
            }
            
            if (readDebugParamsStatus()){
                btnEnableTestingParams.setBackgroundColor(resources.getColor(R.color.racing_green))
                btnEnableTestingParams.text = "Disable Debug Params"
            } else {
                btnEnableTestingParams.setBackgroundColor(resources.getColor(R.color.light_grey))
                btnEnableTestingParams.text = "Enable Debug Params"
            }

        }
    }

    private fun initTypeFromPreferences() {
        val ventilatorType = preferenceManager?.readVentilatorType() ?: VentilatorType.ATN
        when (ventilatorType) {
            VentilatorType.ATN -> {
                btnATN.setBackgroundColor(resources.getColor(R.color.racing_green))
                btnATP.setBackgroundColor(resources.getColor(R.color.light_grey))
                btnNeo.setBackgroundColor(resources.getColor(R.color.light_grey))
            }

            VentilatorType.ATP -> {
                btnATN.setBackgroundColor(resources.getColor(R.color.light_grey))
                btnATP.setBackgroundColor(resources.getColor(R.color.racing_green))
                btnNeo.setBackgroundColor(resources.getColor(R.color.light_grey))
            }

            VentilatorType.ONLY_NEO -> {
                btnATN.setBackgroundColor(resources.getColor(R.color.light_grey))
                btnATP.setBackgroundColor(resources.getColor(R.color.light_grey))
                btnNeo.setBackgroundColor(resources.getColor(R.color.racing_green))
            }
        }
    }

    fun initCalibrationFromPreferences() {
        preferenceManager?.apply {

            if (readOxygenCalibrationStatus()) btnOxygen.setBackgroundColor(resources.getColor(R.color.racing_green))
            else btnOxygen.setBackgroundColor(resources.getColor(R.color.light_grey))

            if (readExhaleValveCalibrationStatus()) btnExhaleValve.setBackgroundColor(
                resources.getColor(
                    R.color.racing_green
                )
            )
            else btnExhaleValve.setBackgroundColor(resources.getColor(R.color.light_grey))

            if (readExpFlowCalibrationStatus()) btnExpFlow.setBackgroundColor(resources.getColor(R.color.racing_green))
            else btnExpFlow.setBackgroundColor(resources.getColor(R.color.light_grey))

            if (readTurbineCalibrationStatus()) btnTurbine.setBackgroundColor(resources.getColor(R.color.racing_green))
            else btnTurbine.setBackgroundColor(resources.getColor(R.color.light_grey))

            if (readInspFlowCalibrationStatus()) btnInspFlow.setBackgroundColor(resources.getColor(R.color.racing_green))
            else btnInspFlow.setBackgroundColor(resources.getColor(R.color.light_grey))

            if (readLeakTestCalibrationStatus()) btnLeakTest.setBackgroundColor(resources.getColor(R.color.racing_green))
            else btnLeakTest.setBackgroundColor(resources.getColor(R.color.light_grey))

            if (readComplianceTubeCalibrationStatus()) btnTubeComp.setBackgroundColor(
                resources.getColor(
                    R.color.racing_green
                )
            )
            else btnTubeComp.setBackgroundColor(resources.getColor(R.color.light_grey))

            if (readResistanceTubeCalibrationStatus()) btnTubeResistance.setBackgroundColor(
                resources.getColor(R.color.racing_green)
            )
            else btnTubeResistance.setBackgroundColor(resources.getColor(R.color.light_grey))
        }
    }

}