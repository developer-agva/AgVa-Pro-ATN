package com.agvahealthcare.ventilator_ext.system.device_update

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.databinding.FragmentDeviceupdateBinding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory


class DeviceUpdateFragment(private var communicationService: CommunicationService?) : Fragment(){

    companion object {
        const val TAG = "DeviceUpdateFragment"
    }
    private lateinit var binding: FragmentDeviceupdateBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDeviceupdateBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    private var prefManager: PreferenceManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())
        setUpView()
        setUpOnClickListener()

    }

    @SuppressLint("SetTextI18n")
    private fun setUpView() {
        binding.includeButtonDownload.buttonView.text = "DOWNLOAD"
        binding.includeButtonUpdate.buttonView.text = "UPDATE"
        binding.includeButtonRestore.buttonView.text = "RESTORE"

        updateSensorCalibrationStatus()

    }


    private fun setUpOnClickListener() {
        if (tag == "FromDashboard") {
            binding.includeButtonDownload.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for update process"
                )

            }
            binding.includeButtonUpdate.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for update process"
                )
            }
            binding.includeButtonRestore.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for update process"
                )
            }
        } else {
            binding.includeButtonDownload.buttonView.setOnClickListener {
                sendUpdateCommandToVentilator("DN")
            }

            binding.includeButtonUpdate.buttonView.setOnClickListener {
                sendUpdateCommandToVentilator("IN")
            }

            binding.includeButtonRestore.buttonView.setOnClickListener {
                sendUpdateCommandToVentilator("RST")
            }
        }
    }

    fun showInfo(text:String) {

        binding.txtProgress.text = text
        binding.secondPanel.visibility = View.VISIBLE
        binding.mainPanel.visibility = View.GONE
    }

    fun updateSensorCalibrationStatus() {
        binding.mainPanel.visibility = View.VISIBLE
        binding.secondPanel.visibility = View.GONE

        binding.linearLayoutDownloadPanel.visibility = View.VISIBLE
        binding.linearLayoutUpdatePanel.visibility = View.GONE
        binding.linearLayoutRestorePanel.visibility = View.GONE

        prefManager?.apply {
            if (downloadStatus) binding.linearLayoutUpdatePanel.visibility = View.VISIBLE else binding.linearLayoutUpdatePanel.visibility = View.GONE
            if (updateStatus) binding.linearLayoutRestorePanel.visibility = View.VISIBLE else binding.linearLayoutRestorePanel.visibility = View.GONE

            binding.tvDownloadTag.text = downloadType
            binding.tvUpdateTag.text = updateType
            binding.tvRestoreTag.text = restoreType

            binding.tvDownloadTime.text = downloadTime
            binding.tvUpdateTime.text = updateTime
            binding.tvRestoreTime.text = restoreTime
        }
    }


    private fun sendUpdateCommandToVentilator(sensorTag: String) {
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            communicationService?.send("CM+$sensorTag")
        }
    }

}
