package com.agvahealthcare.ventilator_ext


import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.Window
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.api.ServerLogger
import com.agvahealthcare.ventilator_ext.api.model.statusDataModel.StatusRequestModel
import com.agvahealthcare.ventilator_ext.dashboard.BaseActivity
import com.agvahealthcare.ventilator_ext.database.entities.EventDataModel
import com.agvahealthcare.ventilator_ext.databinding.ActivityShutDownBinding
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.RUNNING_STATUS_INACTIVE
import com.agvahealthcare.ventilator_ext.utility.utils.LocationFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class ShutDownActivity : BaseActivity() {

     var shutdownTimer : CountDownTimer? = null
    private var dataStoreManager : DataStoreManager? = null
    private var prefManager:PreferenceManager? = null
    private var mEventViewModel: EventViewModel? = null
    private lateinit var binding: ActivityShutDownBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        binding = ActivityShutDownBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvVersion.text = "${getString(R.string.hint_version)}  ${VentilatorApp.getInstance()?.getVersion()}"
        dataStoreManager = DataStoreManager(this@ShutDownActivity)
        prefManager = PreferenceManager(this@ShutDownActivity)
        mEventViewModel = ViewModelProvider(this)[EventViewModel::class.java]

        addEvents("Initiating Shutdown Process",prefManager?.readUHID().toString())

        // status api call
        callRunningStatusApi(RUNNING_STATUS_INACTIVE)

        VentilatorApp.currentActivityName = "Shutdown"
        CoroutineScope(Dispatchers.Main).launch {
            dataStoreManager?.saveStartUpCheckFlag(true)
            dataStoreManager?.saveCurrentActivity("Shutdown")
        }

        prefManager?.setAcitivityTrack(Configs.ACTIVITY_TRACK.SHUTDOWN)
        shutdownTimer = object : CountDownTimer(30000,1000){
            override fun onTick(millisUntilFinished: Long) {
                Log.i("shutdown clock tick",millisUntilFinished.toString())
            }

            override fun onFinish() {
                DialogBoxFactory.showshutScreenDialog(this@ShutDownActivity)
            }
        }.start()



    }

    private fun calculateTotalAndLastHours(
        millis: Long
    ): String {
        return String.format(
            "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
        )
    }

    private fun callRunningStatusApi(status: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val request = StatusRequestModel()
            dataStoreManager?.apply {
                request.apply {
                    this.did = Settings.Secure.getString(
                        this@ShutDownActivity.contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    this.message = status
                    this.last_hours = calculateTotalAndLastHours(getLastHours().first().toLong())
                    this.total_hours = calculateTotalAndLastHours(getTotalHours().first().toLong())
                    this.health = " Good"
                    this.address = LocationFilter(this@ShutDownActivity).getAddress(
                        VentilatorApp.latitude,
                        VentilatorApp.logitude
                    )
                }
                Log.i("value_check_hours", "INVENTILATION $request")
                if (!ServerLogger.sendStatusRequest(request)) ServerLogger.sendStatusRequest(request)
            }
        }
    }

    fun addEvents(eventMsg: String, uhid: String) {
        val eventDataModel = EventDataModel(
            eventMsg,
            uhid
        )
        mEventViewModel?.addEvent(eventDataModel)
    }


}