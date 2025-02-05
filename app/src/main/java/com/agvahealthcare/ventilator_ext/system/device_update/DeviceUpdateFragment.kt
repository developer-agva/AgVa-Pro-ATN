package com.agvahealthcare.ventilator_ext.system.device_update

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_deviceupdate.*


class DeviceUpdateFragment(private var communicationService: CommunicationService?) : Fragment(){

    companion object {
        const val TAG = "DeviceUpdateFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_deviceupdate, container, false)
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
        includeButtonDownload.buttonView.text = "DOWNLOAD"
        includeButtonUpdate.buttonView.text = "UPDATE"
        includeButtonRestore.buttonView.text = "RESTORE"

        updateSensorCalibrationStatus()

    }


    private fun setUpOnClickListener() {
        if (tag == "FromDashboard") {
            includeButtonDownload.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for update process"
                )

            }
            includeButtonUpdate.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for update process"
                )
            }
            includeButtonRestore.buttonView.setOnClickListener {
                DialogBoxFactory.dismissDialogs()
                DialogBoxFactory.showNeonateSensorDialog(
                    requireContext(),
                    "Switch to standby for update process"
                )
            }
        } else {
            includeButtonDownload.buttonView.setOnClickListener {
                sendUpdateCommandToVentilator("DN")
            }

            includeButtonUpdate.buttonView.setOnClickListener {
                sendUpdateCommandToVentilator("IN")
            }

            includeButtonRestore.buttonView.setOnClickListener {
                sendUpdateCommandToVentilator("RST")
            }
        }
    }

    fun showInfo(text:String) {

        txtProgress.text = text
        secondPanel.visibility = View.VISIBLE
        mainPanel.visibility = View.GONE
    }

    fun updateSensorCalibrationStatus() {
        mainPanel.visibility = View.VISIBLE
        secondPanel.visibility = View.GONE

        linearLayoutDownloadPanel.visibility = View.VISIBLE
        linearLayoutUpdatePanel.visibility = View.GONE
        linearLayoutRestorePanel.visibility = View.GONE

        prefManager?.apply {
            if (downloadStatus) linearLayoutUpdatePanel.visibility = View.VISIBLE else linearLayoutUpdatePanel.visibility = View.GONE
            if (updateStatus) linearLayoutRestorePanel.visibility = View.VISIBLE else linearLayoutRestorePanel.visibility = View.GONE

            tvDownloadTag.text = downloadType
            tvUpdateTag.text = updateType
            tvRestoreTag.text = restoreType

            tvDownloadTime.text = downloadTime
            tvUpdateTime.text = updateTime
            tvRestoreTime.text = restoreTime
        }
    }


    private fun sendUpdateCommandToVentilator(sensorTag: String) {
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            communicationService?.send("CM+$sensorTag")
        }
    }

}
