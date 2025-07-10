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
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.system.settings.CommonSetupAdapter
import com.agvahealthcare.ventilator_ext.system.settings.onDropDownSelectionListener
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.activity_setup.btnFinishSetup
import kotlinx.android.synthetic.main.activity_setup.flowLayoutExp
import kotlinx.android.synthetic.main.activity_setup.flowLayoutInsp
import kotlinx.android.synthetic.main.activity_setup.nebulizerTypeLayout
import kotlinx.android.synthetic.main.activity_setup.neoPcbTypeLayout
import kotlinx.android.synthetic.main.activity_setup.neonateSensorLayout
import kotlinx.android.synthetic.main.activity_setup.oxygenConcentratorLayout
import kotlinx.android.synthetic.main.activity_setup.oxygenLayout
import kotlinx.android.synthetic.main.activity_setup.pressureLayoutOne
import kotlinx.android.synthetic.main.activity_setup.pressureLayoutThree
import kotlinx.android.synthetic.main.activity_setup.pressureLayoutTwo
import kotlinx.android.synthetic.main.activity_setup.setupLayout
import kotlinx.android.synthetic.main.activity_setup.setupRecyclerView
import kotlinx.android.synthetic.main.activity_setup.spo2SensorLayout
import kotlinx.android.synthetic.main.activity_setup.txtFlowExpValue
import kotlinx.android.synthetic.main.activity_setup.txtFlowInspValue
import kotlinx.android.synthetic.main.activity_setup.txtNebulizerTypeValue
import kotlinx.android.synthetic.main.activity_setup.txtNeoPCBTypeValue
import kotlinx.android.synthetic.main.activity_setup.txtNeonateSensorValue
import kotlinx.android.synthetic.main.activity_setup.txtOxygenConcentratorValue
import kotlinx.android.synthetic.main.activity_setup.txtOxygenValue
import kotlinx.android.synthetic.main.activity_setup.txtPressureOneValue
import kotlinx.android.synthetic.main.activity_setup.txtPressureThreeValue
import kotlinx.android.synthetic.main.activity_setup.txtPressureTwoValue
import kotlinx.android.synthetic.main.activity_setup.txtSpo2SensorValue
import kotlinx.android.synthetic.main.activity_setup.txtValveValue
import kotlinx.android.synthetic.main.activity_setup.valveLayout
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

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        prefManager = PreferenceManager(this)

        // navigate to splash if flag is true
        if (prefManager?.readVentiConfigSetupStatus() == true) navigateToSplash("")

        setupLayout.setOnClickListener {
            setupRecyclerView.visibility = View.GONE
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
            txtPressureOneValue.text = readPressureSensorOne()
            txtPressureTwoValue.text = readPressureSensorTwo()
            txtPressureThreeValue.text = readPressureSensorThree()
            txtFlowInspValue.text = readInspFlowSensor()
            txtFlowExpValue.text = readExpFlowSensor()
            txtOxygenValue.text = readOxySensor()
            txtNeonateSensorValue.text = readNeoSensor()
            txtSpo2SensorValue.text = readSpo2Sensor()
            txtValveValue.text = readPropValve()
            txtNeoPCBTypeValue.text = readNeoPCBType()
            txtNebulizerTypeValue.text = readNebType()
        }
        updateOxyButton()
    }

    private fun updateOxyButton() {
        prefManager?.apply {
            if (readOxyConcentrator()) {
                oxygenConcentratorLayout.setBackgroundResource(R.drawable.background_green_border)
                txtOxygenConcentratorValue.text = "YES"
                txtOxygenConcentratorValue.setTextColor(resources.getColor(R.color.white))
            } else {
                oxygenConcentratorLayout.setBackgroundResource(R.drawable.background_offwhite_border_none)
                txtOxygenConcentratorValue.text = "NO"
                txtOxygenConcentratorValue.setTextColor(resources.getColor(R.color.black))
            }
        }
    }

    private fun setupDropDownAdapter() {

        setupRecyclerView.visibility = View.VISIBLE
        mAdapter = CommonSetupAdapter(commonList, this)
        setupRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SetupActivity)
            adapter = mAdapter
        }
    }

    private fun navigateToSplash(dataString: String) {
        Intent(this@SetupActivity, SplashActivity::class.java).also {
            it.putExtra(Configs.CONFIGS_STRING, dataString)
            startActivity(it)
        }
    }

    private fun setOnClickListeners() {

        btnFinishSetup.setOnClickListener {
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

        pressureLayoutOne.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.PRESSURE_SENSOR_ONE
            commonList = pressureSensorList
            setupDropDownAdapter()
        }

        pressureLayoutTwo.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.PRESSURE_SENSOR_TWO
            commonList = pressureSensorList
            setupDropDownAdapter()
        }

        pressureLayoutThree.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.PRESSURE_SENSOR_THREE
            commonList = pressureSensorList
            setupDropDownAdapter()
        }

        flowLayoutInsp.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.INSP_FLOW_SENSOR
            commonList = flowSensorList
            setupDropDownAdapter()
        }

        flowLayoutExp.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.EXP_FLOW_SENSOR
            commonList = flowSensorList
            setupDropDownAdapter()
        }

        oxygenLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.OXYGEN_SENSOR
            commonList = oxygenSensorList
            setupDropDownAdapter()
        }

        neonateSensorLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.NEO_SENSOR
            commonList = neoSensorList
            setupDropDownAdapter()
        }

        spo2SensorLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.SPO2_SENSOR
            commonList = spo2SensorList
            setupDropDownAdapter()
        }

        valveLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.PROPOSTIONAL_VALVE
            commonList = propostionalValveList
            setupDropDownAdapter()
        }

        neoPcbTypeLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.NEO_PCB_TYPE
            commonList = neoPCBTypeList
            setupDropDownAdapter()
        }

        nebulizerTypeLayout.setOnClickListener {
            changeConstraintsOfLayout(it)
            clickedTile = SelectedOption.NEB_TYPE
            commonList = nebTypeList
            setupDropDownAdapter()
        }

        oxygenConcentratorLayout.setOnClickListener {
            prefManager?.apply { setOxyConcentrator(!readOxyConcentrator()) }
            updateOxyButton()
        }
    }

    private fun changeConstraintsOfLayout(view: View) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(setupLayout)
        constraintSet.connect(
            setupRecyclerView.id,
            ConstraintSet.RIGHT,
            view.id,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            setupRecyclerView.id,
            ConstraintSet.TOP,
            view.id,
            ConstraintSet.BOTTOM,
            0
        )
        constraintSet.connect(
            setupRecyclerView.id,
            ConstraintSet.LEFT,
            view.id,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.applyTo(setupLayout)
    }

    override fun onItemSelect(text: String, colorInt: Int) {

        setupRecyclerView.visibility = View.GONE
        mAdapter = null

        prefManager?.apply {
            when (clickedTile) {
                SelectedOption.PRESSURE_SENSOR_ONE -> {
                    txtPressureOneValue.text = text
                    setPressureSensorOne(text)
                }

                SelectedOption.PRESSURE_SENSOR_TWO -> {
                    txtPressureTwoValue.text = text
                    setPressureSensorTwo(text)
                }

                SelectedOption.PRESSURE_SENSOR_THREE -> {
                    txtPressureThreeValue.text = text
                    setPressureSensorThree(text)
                }

                SelectedOption.INSP_FLOW_SENSOR -> {
                    txtFlowInspValue.text = text
                    setInspFlowSensor(text)
                }

                SelectedOption.EXP_FLOW_SENSOR -> {
                    txtFlowExpValue.text = text
                    setExpFlowSensor(text)
                }

                SelectedOption.OXYGEN_SENSOR -> {
                    txtOxygenValue.text = text
                    setOxySensor(text)
                }

                SelectedOption.NEO_SENSOR -> {
                    txtNeonateSensorValue.text = text
                    setNeoSensor(text)
                }

                SelectedOption.SPO2_SENSOR -> {
                    txtSpo2SensorValue.text = text
                    setSpo2Sensor(text)
                }

                SelectedOption.PROPOSTIONAL_VALVE -> {
                    txtValveValue.text = text
                    setPropValve(text)
                }

                SelectedOption.NEO_PCB_TYPE -> {
                    txtNeoPCBTypeValue.text = text
                    setNeoPCBType(text)
                }

                SelectedOption.NEB_TYPE -> {
                    txtNebulizerTypeValue.text = text
                    setNebType(text)
                }

                null -> {}
            }
        }

        clickedTile = null
    }
}