package com.agvahealthcare.ventilator_ext.system

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.databinding.FragmentStartupBinding
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StartupCheckFragment : Fragment() {
    private lateinit var binding:FragmentStartupBinding
    private var dataStoreManager: DataStoreManager? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartupBinding.inflate(layoutInflater,container,false)
        return binding.root
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
                    if (list[1].toFloat() < 3.0f && list[5].toFloat() > 3.0f) binding.layoutTurbineError.visibility =
                        View.GONE else if (list[1].toFloat() > 3.0f && list[5].toFloat() < 3.0f) binding.layoutTurbineError.visibility =
                        View.GONE else if (list[1].toFloat() > 3.0f && list[5].toFloat() > 3.0f) binding.layoutTurbineError.visibility =
                        View.GONE else binding.layoutTurbineError.visibility = View.VISIBLE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (list.get(8) == "1") {
                    binding.ivInspFlowSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    binding.tvInspFlowSensor.setText("Expiratory Flow Sensor Pass")
                } else {
                    binding.ivInspFlowSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    binding.tvInspFlowSensor.setText("Expiratory Flow Sensor Failed")
                }
                if (list.get(7) == "1") {
                    binding.ivExpFlowSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    binding.tvExpFlowSensor.setText("Inspiratory Flow Sensor Pass")
                } else {
                    binding.ivExpFlowSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    binding.tvExpFlowSensor.setText("Inspiratory Flow Sensor Failed")
                }

                if (list.get(9) == "1") {
                    binding.ivInspPressureSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    binding.tvInspPressureSensor.setText("Inspiratory Pressure Sensor Pass")
                } else {
                    binding.ivInspPressureSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    binding.tvInspPressureSensor.setText("Inspiratory Pressure Sensor Failed")
                }
                if (list.get(10) == "1") {
                    binding.ivO2PressureSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    binding.tvO2PressureSensor.setText("O2 Pressure Sensor Pass")
                } else {
                    binding.ivO2PressureSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    binding.tvO2PressureSensor.setText("O2 Pressure Sensor Failed")
                }
                if (list.get(11) == "1") {
                    binding.ivO2Sensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    binding.tvO2Sensor.setText("O2 Sensor Pass")
                } else {
                    binding.ivO2Sensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    binding.tvO2Sensor.setText("O2 Sensor Failed")
                }

                if (list.get(12) == "1") {
                    binding.ivNeoSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_green_circle_tick)
                    )
                    binding.tvNeoSensor.setText("Neo Sensor Pass")
                } else {
                    binding.ivNeoSensor.setImageDrawable(
                        requireContext().getResources().getDrawable(R.drawable.ic_red_cross)
                    )
                    binding.tvNeoSensor.setText("Neo Sensor Failed")
                }
            }
        }
    }

}