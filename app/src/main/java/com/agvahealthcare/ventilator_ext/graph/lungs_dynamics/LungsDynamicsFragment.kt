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
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.isPatientDisconnected
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
    private lateinit var binding: FragmentLungsDynamicsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLungsDynamicsBinding.inflate(layoutInflater, container, false)
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
                    maxCount = if (((it / prefManager.readVtApnea()
                            .toInt()) * targetCount) <= targetCount
                    ) ((it / prefManager.readVtApnea()
                        .toInt()) * targetCount) else targetCount.toFloat() + extendedCount
                }
            } else {
                if (prefManager.readModeType() == Configs.ModeType.TYPE_Volume) {
                    mDashBoardViewModel?.vtiValue?.value?.let {
                        maxCount = if (((it / prefManager.readVti()
                                .toInt()) * targetCount) <= targetCount
                        ) ((it / prefManager.readVti()
                            .toInt()) * targetCount) else targetCount.toFloat() + extendedCount
                    }
                } else if (prefManager.readModeType() == Configs.ModeType.TYPE_Pressure) {

                    if (isPatientDisconnected) {
                        mDashBoardViewModel?.pipValue?.value?.let {
                            maxCount = if (((it / (prefManager.readPEEP()
                                    .toInt() + prefManager.readSupportPressure())) * targetCount) <= targetCount
                            ) ((it / (prefManager.readPEEP()
                                .toInt() + prefManager.readSupportPressure())) * targetCount) else targetCount.toFloat() + extendedCount
                        }
                    } else {
                        mDashBoardViewModel?.pipValue?.value?.let {
                            maxCount = if (((it / (prefManager.readPEEP()
                                    .toInt() + prefManager.readPplat())) * targetCount) <= targetCount
                            ) ((it / (prefManager.readPEEP()
                                .toInt() + prefManager.readPplat())) * targetCount) else targetCount.toFloat() + extendedCount
                        }
                    }
                } else {
                    targetCount = 20
                    maxCount = 20f
                }
            }
            Log.i(
                "max_count_padding",
                "$maxCount , ${prefManager.readModeType()} | Volume analysis - ${mDashBoardViewModel?.vtiValue?.value} , ${prefManager.readVti()} | pressure analysis - ${mDashBoardViewModel?.pipValue?.value} , ${
                    (prefManager.readPEEP().toInt() + prefManager.readPplat())
                } | Apnea analysis - $apneaActive , ${prefManager.readVtApnea()} | patient trigger analysis - $isPatientDisconnected , ${mDashBoardViewModel?.pipValue?.value}, ${prefManager.readPEEP() + prefManager.readSupportPressure()}"
            )

            if (isPatientDisconnected) {
                binding.imgLungs.setPadding(targetCount, targetCount, targetCount, targetCount)
                binding.imgVeins.setPadding(targetCount, targetCount, targetCount, targetCount)

                binding.imgLungs.setImageResource(R.drawable.lungs_grey)
                binding.imgVeins.setImageResource(R.drawable.veins_grey)
                binding.imgTrachea.setImageResource(R.drawable.trachea_grey)

                binding.dynamicComplianceValue.text = "-"
                binding.resistanceValue.text = "-"
                binding.spontRRValue.text = "-"
            } else {
                peekValue.let {
                    when (it) {
                        "A" -> {
                            binding.imgLungs.setPadding(--count, --count, --count, --count)
                            binding.imgVeins.setPadding(--count, --count, --count, --count)
                            if (count < (targetCount - maxCount).toInt()) count =
                                (targetCount - maxCount).toInt()
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
    }

    // change lungs color as per dynamic compliance value
    private fun changeLungsAsPerCompliance(dynamicCompliance: Int) {

        if (dynamicCompliance <= 0) binding.imgLungs.setImageResource(R.drawable.lungs_grey) // not valid
        else if (dynamicCompliance < 20) binding.imgLungs.setImageResource(R.drawable.lungs_red) // extremely low
        else if (dynamicCompliance in 20..39) binding.imgLungs.setImageResource(R.drawable.lungs_amber) // low
        else if (dynamicCompliance in 40..60) binding.imgLungs.setImageResource(R.drawable.lungs_green) // normal
        else binding.imgLungs.setImageResource(R.drawable.lungs_blue) // high
    }

    // change veins and trachea color as per resistance value
    private fun changeVeinsAsPerResistance(resistance: Int) {

        if (resistance <= 0) { // not valid
            binding.imgVeins.setImageResource(R.drawable.veins_grey)
            binding.imgTrachea.setImageResource(R.drawable.trachea_grey)
        } else if (resistance < 5) { // low
            binding.imgVeins.setImageResource(R.drawable.veins_blue)
            binding.imgTrachea.setImageResource(R.drawable.trachea_blue)
        } else if (resistance in 5..15) { // normal
            binding.imgVeins.setImageResource(R.drawable.veins_green)
            binding.imgTrachea.setImageResource(R.drawable.trachea_green)
        } else if (resistance in 15..25) { // high
            binding.imgVeins.setImageResource(R.drawable.veins_amber)
            binding.imgTrachea.setImageResource(R.drawable.trachea_amber)
        } else { // extremely high
            binding.imgVeins.setImageResource(R.drawable.veins_red)
            binding.imgTrachea.setImageResource(R.drawable.trachea_red)
        }
    }

    fun readTrendsViaParamAndDuration() {
        if (!isPatientDisconnected) {
            CoroutineScope(Dispatchers.IO).launch {

                val dataFirstChart = FileLogger.readLungsDynamicsFile(0)
                val dataSecondChart = FileLogger.readLungsDynamicsFile(3)
                val dataThirdChart = FileLogger.readLungsDynamicsFile(2)

                withContext(Dispatchers.Main) {
                    if (dataFirstChart != FileLogger.dataNotFound) {
                        binding.dynamicComplianceValue.text = if (dataFirstChart.toFloat().toInt() == 0) "-" else dataFirstChart.toFloat().toInt().toString()
                        changeLungsAsPerCompliance(dataFirstChart.toFloat().toInt())
                    } else binding.dynamicComplianceValue.text = "-"

                    if (dataSecondChart != FileLogger.dataNotFound) {
                        binding.resistanceValue.text = if (dataSecondChart.toFloat().toInt() == 0) "-" else dataSecondChart.toFloat().toInt().toString()
                        changeVeinsAsPerResistance(dataSecondChart.toFloat().toInt())
                    } else binding.resistanceValue.text = "-"

                    if (dataThirdChart != FileLogger.dataNotFound) {
                        binding.spontRRValue.text = if (dataThirdChart.toFloat().toInt() == 0) "-" else dataThirdChart.toFloat().toInt().toString()
                    } else binding.spontRRValue.text = "-"
                }
            }
        }
    }
}