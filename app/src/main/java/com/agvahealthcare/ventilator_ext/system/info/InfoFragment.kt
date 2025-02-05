package com.agvahealthcare.ventilator_ext.system.info

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.globalCount
import com.agvahealthcare.ventilator_ext.api.BatteryHealthStaus
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit


class InfoFragment(private var communicationService: CommunicationService?) : Fragment() {

    private var timerForBatteryData: CountDownTimer? = null
    private var batteryLevel: Int? = null
    private var batteryHealth: Int? = null
    private var batteryRemainingTime: Int? = null
    private var isBatteryConnected: Boolean? = null
    private var isClicked = false
    var prefManager: PreferenceManager? = null
    var dataStoreManager: DataStoreManager? = null
    private var activityViewModel: AndroidViewModel? = null
    val usageStats: UsageStatsManager by lazy {
        requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }
    var usageStat: UsageStatsManager? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefManager = PreferenceManager(context)
        dataStoreManager = DataStoreManager(requireContext())


        textViewLogDeviceIdData.text = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ANDROID_ID
        )
        usageStat =
            requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        if (tag == "MainActivity") {


            communicationService?.send("CM+VES")

            activityViewModel =
                ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)
            (activityViewModel as MainActivityViewModel).ventBatteryRemainingTime.observe(
                viewLifecycleOwner,
                Observer { it ->
                    setBatteryTTEUpdate(it)
                })
            (activityViewModel as MainActivityViewModel).ventBatteryHealth.observe(
                viewLifecycleOwner,
                Observer { it ->
                    setBatteryHealthUpdate(it)
                })
            (activityViewModel as MainActivityViewModel).ventBatteryLevel.observe(
                viewLifecycleOwner,
                Observer { it ->
                    setBatteryLevelUpdate(it)
                })
            (activityViewModel as MainActivityViewModel).isBatteryConnected.distinctUntilChanged()
                .observe(viewLifecycleOwner, Observer { it ->
                    isBatteryConnected = it
                })

            //Operational hours in hours and minutes.
            (activityViewModel as MainActivityViewModel).OPHours.observe(viewLifecycleOwner,
                Observer {
                    textViewOpHoursData.text = it
                    Log.i("CHECK_OP_HOURS", it.toString())
                })
            (activityViewModel as MainActivityViewModel).serviceHours.observe(viewLifecycleOwner,
                Observer {
                    textViewServiceHoursData.text = it
                    Log.i("CHECK_SERVICE_HOURS", it.toString())
                })

        } else if (tag == "FromDashboard") {

            activityViewModel =
                ViewModelProvider(requireActivity()).get(DashBoardViewModel::class.java)
            (activityViewModel as DashBoardViewModel).ventBatteryRemainingTime.observe(
                viewLifecycleOwner,
                Observer { it ->
                    Log.i("swasdwa", "s124")
                    setBatteryTTEUpdate(it)
                })
            (activityViewModel as DashBoardViewModel).ventBatteryHealth.observe(
                viewLifecycleOwner,
                Observer { it ->
                    setBatteryHealthUpdate(it)
                })
            (activityViewModel as DashBoardViewModel).ventBatteryLevel.observe(
                viewLifecycleOwner,
                Observer { it ->
                    setBatteryLevelUpdate(it)
                })
            (activityViewModel as DashBoardViewModel).isBatteryConnected.distinctUntilChanged()
                .observe(viewLifecycleOwner, Observer { it ->
                    isBatteryConnected = it
                })
            //Operational hours in hours and minutes.
            (activityViewModel as DashBoardViewModel).OPHours.observe(viewLifecycleOwner,
                Observer {
                    Log.i("CHECK_OP_HOURS_DASH", it.toString())
                    textViewOpHoursData.text = it
                })
            (activityViewModel as DashBoardViewModel).serviceHours.observe(viewLifecycleOwner,
                Observer {
                    textViewServiceHoursData.text = it
                    Log.i("CHECK_SERVICE_HOURS_DASH", it.toString())
                })
        }
        if (tag == "MainActivity") {

            layout_content.visibility = View.VISIBLE

        }
        prefManager?.apply {
            try {
                setSoftWareUpdate(readVentilatorSoftwareVersion())
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "" + e.printStackTrace(), Toast.LENGTH_LONG).show()
            }
        }
// for the serial number
        CoroutineScope(Dispatchers.Main).launch {
            try {
                textViewSerialNumberData.text =
                    dataStoreManager?.getHardwareSerialNumber()?.first().toString()
                dataStoreManager?.saveHardwareSerialNumber(textViewSerialNumberData.text.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        // set hardware serial number
        textViewHardwareSerialNumberData.text = VentilatorApp.hardwareSerialNumber

        // set hardware version

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val dataArr = dataStoreManager?.getStartUpCheckValue()?.first()?.split(',')
                textViewOperatingHoursData.text = dataArr?.get(0).toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        textViewVersionData.setOnClickListener {

            DialogBoxFactory.showCommandDialog(requireContext()) { command ->
                sendRawCommandToVentilator(
                    java.lang.String.valueOf(
                        command
                    )
                )
            }
        }

        try {
            val pInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val version = pInfo.versionName
            Log.i("version name", version + "  " + pInfo.versionCode)
            textViewVersionData.text = version
            textViewModelData.text = "AGVAC Pro".uppercase()
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(requireContext(), "" + e.printStackTrace(), Toast.LENGTH_LONG).show()
        }

        VentilatorApp.connectivityObserver?.observe()?.distinctUntilChanged()
            ?.observe(viewLifecycleOwner) {
                if (it) textViewInternetConnectivityData.text = "Connected"
                else textViewInternetConnectivityData.text = "Disconnected"
            }
    }


    private fun calculateOperationalHourInTime(
        startTimeInMillis: Long,
        endTimeInMillis: Long
    ): String {
        val currentTimeInMillis = endTimeInMillis
        Log.d("Thelogofstarttimeininfofragment", currentTimeInMillis.toString())
        val totalRunningTime = currentTimeInMillis - startTimeInMillis
        val totalOperationalHours = String.format(
            "%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(totalRunningTime),
            TimeUnit.MILLISECONDS.toSeconds(totalRunningTime) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalRunningTime))
        );
        return totalOperationalHours
    }

    private fun calculateServiceHourInTime(startTimeInMillis: Long, endTimeInMillis: Long): String {
        val currentTimeInMillis = endTimeInMillis
        Log.d("Thelogofstarttimeininfofragment", currentTimeInMillis.toString())
        val totalRunningTime = currentTimeInMillis - startTimeInMillis
        val totalOperationalHours = String.format(
            "%d min, %d sec",
            TimeUnit.MILLISECONDS.toMinutes(totalRunningTime),
            TimeUnit.MILLISECONDS.toSeconds(totalRunningTime) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalRunningTime))
        );
        return totalOperationalHours
    }

    private fun sendRawCommandToVentilator(command: String?) {
        communicationService?.takeIf { it.isPortsConnected }?.apply {
            command?.takeIf { it.isNotEmpty() }?.apply {
                send(this)
            }
        }
    }

    // ClickListener on Buttons

    // TODO : WRONG - WRITE IN DASHBOARD ACTIVITY
    private fun setBatteryLevelUpdate(btryLevel: Int) {
        if (btryLevel < 0 || btryLevel > 100) {
            textViewBattery1Data.text = "-"
        } else {
            textViewBattery1Data.text = "$btryLevel %"
        }
        this.batteryLevel = btryLevel
    }

    private fun setBatteryHealthUpdate(health: Int) {
        when (health) {
            in 0..50 -> {
                textViewBatteryHealthData.text = "${BatteryHealthStaus.Bad}"
                textViewBatteryHealthData.setTextColor(Color.RED)
            }
            in 51..70 -> {
                textViewBatteryHealthData.text = "${BatteryHealthStaus.Marginal}"
                textViewBatteryHealthData.setTextColor(Color.YELLOW)
            }
            in 71..85 -> {
                textViewBatteryHealthData.text = "${BatteryHealthStaus.Good}"
                textViewBatteryHealthData.setTextColor(
                    resources.getColor(
                        R.color.racing_green,
                        null
                    )
                )
            }
            in 86..100 -> {
                textViewBatteryHealthData.text = "${BatteryHealthStaus.Excellent}"
                textViewBatteryHealthData.setTextColor(
                    resources.getColor(
                        R.color.racing_green,
                        null
                    )
                )
            }
            !in 0..100 -> {
                textViewBatteryHealthData.text = "-"
                textViewBatteryHealthData.setTextColor(Color.BLACK)
            }
            else -> {
                textViewBatteryHealthData.text = "-"
                textViewBatteryHealthData.setTextColor(Color.BLACK)
            }
        }

        this.batteryHealth = health
    }

    private fun roundMinutes(value: Int): Int {
        val returnmod = value % 5
        return (value - returnmod)
    }

    private fun cancelTimeOut() {
        timerForBatteryData?.cancel()
        timerForBatteryData = null
    }

    private fun setBatteryTTEUpdate(timeInMins: Int) {

        if (isBatteryConnected == false) {
            textViewBatteryRemTimeData.text = "-"
            isClicked = false
        } else {

            val formatter = DecimalFormat("00")
            val hour = timeInMins / 60
            var mins = timeInMins % 60
            mins = roundMinutes(mins)

//            if (!timerIsExists) {
//                textViewBatteryRemTimeData.text = "Calculating...."
//                timerForBatteryData = object : CountDownTimer(120000 , 1000) {
//                    override fun onTick(millisUntilFinished: Long) {}
//
//                    override fun onFinish() {
//                        cancelTimeOut()
//                        timerIsExists = true
//                    }
//                }
//                timerForBatteryData?.start()
//            }

            if (globalCount >= 60) {
                textViewBatteryRemTimeData.text =
                    if (hour == 0) "${formatter.format(mins)} min" else "$hour hr ${
                        formatter.format(mins)
                    } min"
            } else {
                if (!isClicked) {
                    textViewBatteryRemTimeData.text = "Calculating...."
                    isClicked = true
                }
            }
        }
        this.batteryRemainingTime = timeInMins
    }

    fun setSoftWareUpdate(softwareUpdate: String?) {
        softwareUpdate?.apply {
            textViewOperatingHoursData.text = this
        }
    }

    private fun shutDownBtnAppearance(btnView: Button) {
        btnView.text = getString(R.string.hint_shutdown)
        btnView.setTextColor(getResources().getColor(R.color.white, null))
        btnView.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        btnView.setPadding(50, 0, 50, 0)
    }


    override fun onPause() {
        cancelTimeOut()
        if (tag == "MainActivity") {

            layout_content.visibility = View.VISIBLE
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        try {
        } catch (ex: Exception) {
            textViewOpHoursData.text = "updating"
            textViewServiceHoursData.text = "updating"
        }
    }

    override fun onDetach() {
        timerForBatteryData?.cancel()
        super.onDetach()
    }


}