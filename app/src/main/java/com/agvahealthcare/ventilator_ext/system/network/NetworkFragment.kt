package com.agvahealthcare.ventilator_ext.system.network

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.SetupActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.databinding.FragmentNetworkBinding
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class NetworkFragment(private var communicationService: CommunicationService?) : Fragment() {

    private lateinit var binding: FragmentNetworkBinding
    var prefManager: PreferenceManager? = null
    var mainActivityViewModel: MainActivityViewModel? = null
    var dashBoardViewModel: DashBoardViewModel? = null
    private lateinit var tableAdapter: SettingsParamsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNetworkBinding.inflate(layoutInflater, container, false)

        binding.root.setOnClickListener {
            AppUtils.hideKeyBoard(requireContext(), binding.etNewIPValue)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())
        mainActivityViewModel =
            ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]

        binding.tvUpdatedTime.text = prefManager?.readLastSettingsTime()
        binding.tvCurrentIPValue.text = FileLogger.readBaseUrl()

        binding.settingsData.layoutManager = GridLayoutManager(requireContext(), 5)

        var result = communicationService?.controlSettingsList


        try {
            (requireActivity() as DashBoardActivity)
            binding.btnClearHours.visibility = View.VISIBLE
            binding.tvClearOpHours.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }


        result?.let {
            if (it.size > 0) {
                tableAdapter = SettingsParamsAdapter(requireContext(), it, prefManager)
                binding.settingsData.adapter = tableAdapter
            }
        }

        binding.btnAssignIP.setOnClickListener {
            val ipAddress = "http://" + binding.etNewIPValue.text.toString()
            FileLogger.writeBaseUrl(requireContext(), ipAddress)
            binding.tvCurrentIPValue.text = FileLogger.readBaseUrl()
            AppUtils.hideKeyBoard(requireContext(), binding.etNewIPValue)
        }

        binding.btnResetVenti.setOnClickListener {
            prefManager?.setVentiConfigSetupStatus(false)
            Intent(requireActivity(), SetupActivity::class.java).also {
                startActivity(it)
                requireActivity().finish()
            }
        }

        binding.btnClearHours.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                FileLogger.writeServiceFile("0")
                FileLogger.writeOPFile("0")
            }

            prefManager?.apply {
                setDashBoardRunningTimeForService(0L)
                setDashBoardRunningTime(0L)
            }

            mainActivityViewModel?.OPHours?.postValue("0 hr, 0 min")
            mainActivityViewModel?.serviceHours?.postValue("0 hr, 0 min")
        }
    }

    fun updateKnobRawData(data: String) {
        binding.txtKnobValue.text = data
        binding.txtKnobDate.text = AppUtils.getCurrentTime()
    }

}