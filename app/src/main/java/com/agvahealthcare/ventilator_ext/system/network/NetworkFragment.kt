package com.agvahealthcare.ventilator_ext.system.network

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.SetupActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import kotlinx.android.synthetic.main.fragment_network.*
import kotlinx.android.synthetic.main.fragment_service.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


class NetworkFragment(private var communicationService: CommunicationService?) : Fragment(){

    var prefManager: PreferenceManager? = null
    var mainActivityViewModel: MainActivityViewModel? = null
    var dashBoardViewModel: DashBoardViewModel? = null
    private lateinit var tableAdapter: SettingsParamsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view =  inflater.inflate(R.layout.fragment_network, container, false)
        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                AppUtils.hideKeyBoard(requireContext(),etNewIPValue)
                false
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(requireContext())
        mainActivityViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]
        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]

        tvUpdatedTime.text = prefManager?.readLastSettingsTime()
        tvCurrentIPValue.text = FileLogger.readBaseUrl()

        settingsData.layoutManager = GridLayoutManager(requireContext(),5)

        var result = communicationService?.controlSettingsList


        try {
            (requireActivity() as DashBoardActivity)
            btnClearHours.visibility = View.VISIBLE
            tvClearOpHours.visibility = View.VISIBLE
        }catch (e:Exception){
            e.printStackTrace()
        }


        result?.let {
            if (it.size > 0){
                tableAdapter = SettingsParamsAdapter(requireContext(), it,prefManager)
                settingsData.adapter = tableAdapter
            }
        }

        btnAssignIP.setOnClickListener {
            val ipAddress = "http://" + etNewIPValue.text.toString()
            FileLogger.writeBaseUrl(requireContext(),ipAddress)
            tvCurrentIPValue.text = FileLogger.readBaseUrl()
            AppUtils.hideKeyBoard(requireContext(),etNewIPValue)
        }

        btnResetVenti.setOnClickListener {
            val calendar = Calendar.getInstance()
            val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

            FileLogger.writeDispatchDate(requireContext(), dayOfYear.toString())

            btnResetVenti.text = "Dispatching..."
        }

        btnClearHours.setOnClickListener {
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

    fun updateKnobRawData(data:String){
        txtKnobValue.text = data
        txtKnobDate.text = AppUtils.getCurrentTime()
    }

}