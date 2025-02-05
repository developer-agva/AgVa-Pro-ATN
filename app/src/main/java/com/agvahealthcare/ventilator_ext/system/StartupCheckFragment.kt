package com.agvahealthcare.ventilator_ext.system

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager
import com.agvahealthcare.ventilator_ext.utility.LOG_TYPE_DEBUG
import kotlinx.android.synthetic.main.fragment_startup.*
import kotlinx.android.synthetic.main.fragment_startup.ivO2Sensor
import kotlinx.android.synthetic.main.layout_dialog_startup_check.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StartupCheckFragment : Fragment() {
    private var dataStoreManager: DataStoreManager? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_startup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataStoreManager = DataStoreManager(requireContext())

        CoroutineScope(Dispatchers.Main).launch {

            Log.i("listgetDataStore", dataStoreManager?.getStartUpCheckValue()?.first().toString())
            val list = dataStoreManager?.getStartUpCheckValue()?.first().toString()
                .split(",") as MutableList<String>

            if (list.size >= 12) {
                list[11] = "1"
                try {
                    if (list[1].toFloat() < 3.0f && list[5].toFloat() > 3.0f) layoutTurbineError.visibility =
                        View.GONE else if (list[1].toFloat() > 3.0f && list[5].toFloat() < 3.0f) layoutTurbineError.visibility =
                        View.GONE else if (list[1].toFloat() > 3.0f && list[5].toFloat() > 3.0f) layoutTurbineError.visibility =
                        View.GONE else layoutTurbineError.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (list.get(8) == "1") {
                    ivInspFlowSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    tvInspFlowSensor.setText("Expiratory Flow Sensor Pass")
                } else {
                    ivInspFlowSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    tvInspFlowSensor.setText("Expiratory Flow Sensor Failed")
                }
                if (list.get(7) == "1") {
                    ivExpFlowSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    tvExpFlowSensor.setText("Inspiratory Flow Sensor Pass")
                } else {
                    ivExpFlowSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    tvExpFlowSensor.setText("Inspiratory Flow Sensor Failed")
                }

                if (list.get(9) == "1") {
                    ivInspPressureSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    tvInspPressureSensor.setText("Inspiratory Pressure Sensor Pass")
                } else {
                    ivInspPressureSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    tvInspPressureSensor.setText("Inspiratory Pressure Sensor Failed")
                }
                if (list.get(10) == "1") {
                    ivO2PressureSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    tvO2PressureSensor.setText("O2 Pressure Sensor Pass")
                } else {
                    ivO2PressureSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    tvO2PressureSensor.setText("O2 Pressure Sensor Failed")
                }
                if (list.get(11) == "1") {
                    ivO2Sensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    tvO2Sensor.setText("O2 Sensor Pass")
                } else {
                    ivO2Sensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    tvO2Sensor.setText("O2 Sensor Failed")
                }

                if (list.get(12) == "1") {
                    ivNeoSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    tvNeoSensor.setText("Neo Sensor Pass")
                } else {
                    ivNeoSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    tvNeoSensor.setText("Neo Sensor Failed")
                }
            }
        }

    }

}