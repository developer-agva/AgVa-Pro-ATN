package com.agvahealthcare.ventilator_ext.graph.lungs_dynamics

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.apneaActive
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.isPatientTrigger
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.databinding.FragmentLungsDynamicsBinding
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LungsDynamicsFragment : Fragment() {

    private var mDashBoardViewModel: DashBoardViewModel? = null
    private lateinit var prefManager: PreferenceManager
    private var count = 0
    private var maxCount = 0f
    private var targetCount = 20
    private var extendedCount = 6f
    private lateinit var binding : FragmentLungsDynamicsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLungsDynamicsBinding.inflate(layoutInflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())
        mDashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
        readTrendsViaParamAndDuration()

        if (prefManager.readGender() == Configs.Gender.TYPE_MALE) binding.imgMaleBody.visibility = View.VISIBLE else binding.imgFemaleBody.visibility = View.VISIBLE

        mDashBoardViewModel?.graphPeekValue?.observe(viewLifecycleOwner) { peekValue ->
            if (apneaActive) {
                mDashBoardViewModel?.vtiValue?.value?.let {
                    maxCount = if (((it / prefManager.readVtApnea().toInt()) * targetCount) <= targetCount) ((it / prefManager.readVtApnea().toInt()) * targetCount) else targetCount.toFloat() + extendedCount
                }
            } else{
                if (prefManager.readModeType() == Configs.ModeType.TYPE_Volume) {
                    mDashBoardViewModel?.vtiValue?.value?.let {
                        maxCount = if (((it / prefManager.readVti()
                                .toInt()) * targetCount) <= targetCount
                        ) ((it / prefManager.readVti()
                            .toInt()) * targetCount) else targetCount.toFloat() + extendedCount
                    }
                }
                else if (prefManager.readModeType() == Configs.ModeType.TYPE_Pressure) {

                    if (isPatientTrigger) {
                        mDashBoardViewModel?.pipValue?.value?.let {
                            maxCount = if (((it / (prefManager.readPEEP()
                                    .toInt() + prefManager.readSupportPressure())) * targetCount) <= targetCount
                            ) ((it / (prefManager.readPEEP()
                                .toInt() + prefManager.readSupportPressure())) * targetCount) else targetCount.toFloat() + extendedCount
                        }
                    }else{
                        mDashBoardViewModel?.pipValue?.value?.let {
                            maxCount = if (((it / (prefManager.readPEEP()
                                    .toInt() + prefManager.readPplat())) * targetCount) <= targetCount
                            ) ((it / (prefManager.readPEEP()
                                .toInt() + prefManager.readPplat())) * targetCount) else targetCount.toFloat() + extendedCount
                        }
                    }
                }
                else {
                    targetCount = 20
                    maxCount = 20f
                }
            }
            Log.i(
                "max_count_padding",
                "$maxCount , ${prefManager.readModeType()} | Volume analysis - ${mDashBoardViewModel?.vtiValue?.value} , ${prefManager.readVti()} | pressure analysis - ${mDashBoardViewModel?.pipValue?.value} , ${(prefManager.readPEEP().toInt() + prefManager.readPplat())} | Apnea analysis - $apneaActive , ${prefManager.readVtApnea()} | patient trigger analysis - $isPatientTrigger , ${mDashBoardViewModel?.pipValue?.value}, ${prefManager.readPEEP() + prefManager.readSupportPressure()}"
            )
            peekValue.let {
                when (it) {
                    "A" -> {
                        binding.imgLungs.setPadding(--count, --count, --count, --count)
                        binding.imgVeins.setPadding(--count, --count, --count, --count)
                        if (count < (targetCount-maxCount).toInt()) count = (targetCount-maxCount).toInt()
                    }

                    "B" -> {
                        binding.imgLungs.setPadding(count, count, count, count)
                        binding.imgVeins.setPadding(count, count, count, count)
                    }

                    "C" -> {
                        binding.imgLungs.setPadding(++count, ++count, ++count, ++count)
                        binding.imgVeins.setPadding(++count, ++count, ++count, ++count)
                        if (count > targetCount) count = targetCount
                    }

                    "D" -> {
                        binding.imgLungs.setPadding(count, count, count, count)
                        binding.imgVeins.setPadding(count, count, count, count)
                    }
                }
            }
        }
    }

    // change lungs color as per dynamic compliance value
    private fun changeLungsAsPerCompliance(dynamicCompliance :Int){

        // not valid
        if (dynamicCompliance <= 0) binding.imgLungs.setImageResource(R.drawable.lungs_grey)
        // extremely low
        else if (dynamicCompliance < 20) binding.imgLungs.setImageResource(R.drawable.lungs_red)
        // low
        else if (dynamicCompliance in 20..39) binding.imgLungs.setImageResource(R.drawable.lungs_amber)
        // normal
        else if (dynamicCompliance in 40..60) binding.imgLungs.setImageResource(R.drawable.lungs_green)
        // high
        else binding.imgLungs.setImageResource(R.drawable.lungs_blue)
    }

    // change veins and trachea color as per resistance value
    private fun changeVeinsAsPerResistance(resistance :Int){
        // not valid
        if (resistance <= 0) {
            binding.imgVeins.setImageResource(R.drawable.veins_grey)
            binding.imgTrachea.setImageResource(R.drawable.trachea_grey)
        }
        // low
        else if (resistance < 5) {
            binding.imgVeins.setImageResource(R.drawable.veins_blue)
            binding.imgTrachea.setImageResource(R.drawable.trachea_blue)
        }
        // normal
        else if (resistance in 5..15){
            binding.imgVeins.setImageResource(R.drawable.veins_green)
            binding.imgTrachea.setImageResource(R.drawable.trachea_green)
        }
        // high
        else if (resistance in 15..25){
            binding.imgVeins.setImageResource(R.drawable.veins_amber)
            binding.imgTrachea.setImageResource(R.drawable.trachea_amber)
        }
        // extremely high
        else {
            binding.imgVeins.setImageResource(R.drawable.veins_red)
            binding.imgTrachea.setImageResource(R.drawable.trachea_red)
        }
    }

    fun readTrendsViaParamAndDuration() {
        CoroutineScope(Dispatchers.IO).launch {

            val dataFirstChart = FileLogger.readLungsDynamicsFile(0)
            val dataSecondChart = FileLogger.readLungsDynamicsFile(3)
            val dataThirdChart = FileLogger.readLungsDynamicsFile(2)
            Log.i("Resistance","spontRR : $dataThirdChart")

            withContext(Dispatchers.Main) {
                if (dataFirstChart != FileLogger.dataNotFound) {
                    binding.dynamicComplianceValue.text = dataFirstChart.toFloat().toInt().toString()
                    changeLungsAsPerCompliance(dataFirstChart.toFloat().toInt())
                } else binding.dynamicComplianceValue.text = "-"

                if (dataSecondChart != FileLogger.dataNotFound) {
                    Log.i("Resistance","average : $dataSecondChart")
                     binding.resistanceValue.text = dataSecondChart.toFloat().toInt().toString()
                    changeVeinsAsPerResistance(dataSecondChart.toFloat().toInt())
                } else binding.resistanceValue.text = "-"

                if (dataThirdChart != FileLogger.dataNotFound) {
                    binding.spontRRValue.text = dataThirdChart
                } else binding.spontRRValue.text = "-"
            }
        }
    }
}