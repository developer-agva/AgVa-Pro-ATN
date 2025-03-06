package com.agvahealthcare.ventilator_ext

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import com.agvahealthcare.ventilator_ext.api.ServerLogger
import com.agvahealthcare.ventilator_ext.dashboard.BaseActivity
import com.agvahealthcare.ventilator_ext.databinding.ActivitySetupBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.system.settings.CommonSetupAdapter
import com.agvahealthcare.ventilator_ext.system.settings.onDropDownSelectionListener
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetupActivity : BaseActivity(), onDropDownSelectionListener {

    enum class SelectedOption {
        PRESSURE_SENSOR_ONE,
        PRESSURE_SENSOR_TWO,
        PRESSURE_SENSOR_THREE,
        INSP_FLOW_SENSOR,
        EXP_FLOW_SENSOR,
        OXYGEN_SENSOR,
        NEO_SENSOR,
        SPO2_SENSOR,
        PROPOSTIONAL_VALVE,
        NEO_PCB_TYPE,
        NEB_TYPE
    }

    private var pressureSensorList = arrayListOf("CONSENSIC", "AMS")
    private var flowSensorList = arrayListOf("HONEYWELL", "CONSENSIC", "SFM")
    private var oxygenSensorList = arrayListOf("ULTRASONIC", "GALVANIC")
    private var neoSensorList = arrayListOf("IN-1", "IN-2", "IN-3")
    private var spo2SensorList = arrayListOf("SP-1", "SP-2")
    private var propostionalValveList = arrayListOf("CAMOZZI", "FESTO")
    private var nebTypeList = arrayListOf("PNEUMATIC", "ULTRASONIC")
    private var neoPCBTypeList = arrayListOf("TYPE-C", "GENERIC")

    private var clickedTile: SelectedOption? = null
    private var mAdapter: CommonSetupAdapter? = null
    private var commonList = ArrayList<String>()
    private var prefManager: PreferenceManager? = null
    private lateinit var binding:ActivitySetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefManager = PreferenceManager(this)

        // navigate to splash if flag is true
        if (prefManager?.readVentiConfigSetupStatus() == true) navigateToSplash("")

        binding.setupLayout.setOnClickListener {
            binding.setupRecyclerView.visibility = View.GONE
        }

        setOnClickListeners()
        updateViewViaPreferences()
        callApi()
    }

    private fun callApi() {
        CoroutineScope(Dispatchers.IO).launch {
            ServerLogger.getVentiConfigs()?.let {
                pressureSensorList = it.pressureSensor.split(",") as ArrayList<String>
                flowSensorList = it.flowSensor.split(",") as ArrayList<String>
                oxygenSensorList = it.oxygenSensor.split(",") as ArrayList<String>
                neoSensorList = it.neoNateSensor.split(",") as ArrayList<String>
                spo2SensorList = it.spO2Sensor.split(",") as ArrayList<String>
                propostionalValveList = it.proportionalValve.split(",") as ArrayList<String>
                nebTypeList = it.nebuliserTYPE.split(",") as ArrayList<String>
                neoPCBTypeList = it.kNOBPCBTYPE.split(",") as ArrayList<String>
            }
        }
    }

    private fun updateViewViaPreferences() {
        prefManager?.apply {
            binding.txtPressureOneValue.text = readPressureSensorOne()
            binding.txtPressureTwoValue.text = readPressureSensorTwo()
            binding.txtPressureThreeValue.text = readPressureSensorThree()
            binding.txtFlowInspValue.text = readInspFlowSensor()
            binding.txtFlowExpValue.text = readExpFlowSensor()
            binding.txtOxygenValue.text = readOxySensor()
            binding.txtNeonateSensorValue.text = readNeoSensor()
            binding.txtSpo2SensorValue.text = readSpo2Sensor()
            binding.txtValveValue.text = readPropValve()
            binding.txtNeoPCBTypeValue.text = readNeoPCBType()
            binding.txtNebulizerTypeValue.text = readNebType()
        }
        updateOxyButton()
    }

    private fun updateOxyButton() {
        prefManager?.apply {
            if (readOxyConcentrator()) {
                binding.oxygenConcentratorLayout.setBackgroundResource(R.drawable.background_green_border)
                binding.txtOxygenConcentratorValue.text = "YES"
                binding.txtOxygenConcentratorValue.setTextColor(resources.getColor(R.color.white))
            } else {
                binding.oxygenConcentratorLayout.setBackgroundResource(R.drawable.background_offwhite_border_none)
                binding.txtOxygenConcentratorValue.text = "NO"
                binding.txtOxygenConcentratorValue.setTextColor(resources.getColor(R.color.black))
            }
        }
    }

    private fun setupDropDownAdapter() {

        binding.setupRecyclerView.visibility = View.VISIBLE
        mAdapter = CommonSetupAdapter(commonList, this)
        binding.setupRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SetupActivity)
            adapter = mAdapter
        }
    }

    private fun navigateToSplash(dataString: String){
        Intent(this@SetupActivity,SplashActivity::class.java).also {
            it.putExtra(Configs.CONFIGS_STRING,dataString)
            startActivity(it)
        }
    }

    private fun setOnClickListeners() {

        binding.btnFinishSetup.setOnClickListener {

            prefManager?.apply {
                val finalString = "ATP@" +
                        "${Configs.PROJECT_CODE}," +
                        "${pressureSensorList.indexOf(readPressureSensorOne()) + 1}," +
                        "${pressureSensorList.indexOf(readPressureSensorTwo()) + 1}," +
                        "${pressureSensorList.indexOf(readPressureSensorThree()) + 1}," +
                        "${flowSensorList.indexOf(readInspFlowSensor()) + 1}," +
                        "${flowSensorList.indexOf(readExpFlowSensor()) + 1}," +
                        "${oxygenSensorList.indexOf(readOxySensor()) + 1}," +
                        "${neoSensorList.indexOf(readNeoSensor()) + 1}," +
                        "${spo2SensorList.indexOf(readSpo2Sensor()) + 1}," +
                        "${propostionalValveList.indexOf(readPropValve()) + 1}," +
                        "${neoPCBTypeList.indexOf(readNeoPCBType()) + 1}," +
                        "${nebTypeList.indexOf(readNebType()) + 1}," +
                        if (readOxyConcentrator()) "1#" else "0#"

                Log.i("value_configs_write_check", finalString)
                navigateToSplash(finalString)
            }
        }

        binding.pressureLayoutOne.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.PRESSURE_SENSOR_ONE
            commonList = pressureSensorList
            setupDropDownAdapter()
        }
        binding.pressureLayoutTwo.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.PRESSURE_SENSOR_TWO
            commonList = pressureSensorList
            setupDropDownAdapter()
        }
        binding.pressureLayoutThree.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.PRESSURE_SENSOR_THREE
            commonList = pressureSensorList
            setupDropDownAdapter()
        }
        binding.flowLayoutInsp.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.INSP_FLOW_SENSOR
            commonList = flowSensorList
            setupDropDownAdapter()
        }
        binding.flowLayoutExp.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.EXP_FLOW_SENSOR
            commonList = flowSensorList
            setupDropDownAdapter()
        }
        binding.oxygenLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.OXYGEN_SENSOR
            commonList = oxygenSensorList
            setupDropDownAdapter()
        }
        binding.neonateSensorLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.NEO_SENSOR
            commonList = neoSensorList
            setupDropDownAdapter()
        }
        binding.spo2SensorLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.SPO2_SENSOR
            commonList = spo2SensorList
            setupDropDownAdapter()
        }
        binding.valveLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.PROPOSTIONAL_VALVE
            commonList = propostionalValveList
            setupDropDownAdapter()
        }
        binding.neoPcbTypeLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.NEO_PCB_TYPE
            commonList = neoPCBTypeList
            setupDropDownAdapter()
        }
        binding.nebulizerTypeLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.NEB_TYPE
            commonList = nebTypeList
            setupDropDownAdapter()
        }
        binding.oxygenConcentratorLayout.setOnClickListener {
            prefManager?.apply { setOxyConcentrator(!readOxyConcentrator()) }
            updateOxyButton()
        }
    }

    private fun changeConstraintsOfLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.setupLayout)
        constraintSet.connect(
            binding.setupRecyclerView.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            binding.setupRecyclerView.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )

        constraintSet.connect(
            binding.setupRecyclerView.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(binding.setupLayout)
    }

    override fun onItemSelect(text: String, colorInt: Int) {

        binding.setupRecyclerView.visibility = View.GONE
        mAdapter = null

        prefManager?.apply {
            when (clickedTile) {
                SelectedOption.PRESSURE_SENSOR_ONE -> {
                    binding.txtPressureOneValue.text = text
                    setPressureSensorOne(text)
                }

                SelectedOption.PRESSURE_SENSOR_TWO -> {
                    binding.txtPressureTwoValue.text = text
                    setPressureSensorTwo(text)
                }

                SelectedOption.PRESSURE_SENSOR_THREE -> {
                    binding.txtPressureThreeValue.text = text
                    setPressureSensorThree(text)
                }

                SelectedOption.INSP_FLOW_SENSOR -> {
                    binding.txtFlowInspValue.text = text
                    setInspFlowSensor(text)
                }

                SelectedOption.EXP_FLOW_SENSOR -> {
                    binding.txtFlowExpValue.text = text
                    setExpFlowSensor(text)
                }

                SelectedOption.OXYGEN_SENSOR -> {
                    binding.txtOxygenValue.text = text
                    setOxySensor(text)
                }

                SelectedOption.NEO_SENSOR -> {
                    binding.txtNeonateSensorValue.text = text
                    setNeoSensor(text)
                }

                SelectedOption.SPO2_SENSOR -> {
                    binding.txtSpo2SensorValue.text = text
                    setSpo2Sensor(text)
                }

                SelectedOption.PROPOSTIONAL_VALVE -> {
                    binding.txtValveValue.text = text
                    setPropValve(text)
                }

                SelectedOption.NEO_PCB_TYPE -> {
                    binding.txtNeoPCBTypeValue.text = text
                    setNeoPCBType(text)
                }

                SelectedOption.NEB_TYPE -> {
                    binding.txtNebulizerTypeValue.text = text
                    setNebType(text)
                }

                null -> {}
            }
        }

        clickedTile = null
    }

}