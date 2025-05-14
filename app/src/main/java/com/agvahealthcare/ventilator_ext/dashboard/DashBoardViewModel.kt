package com.agvahealthcare.ventilator_ext.dashboard

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.ListenableWorker
import com.agvahealthcare.ventilator_ext.BuildConfig
import com.agvahealthcare.ventilator_ext.api.LoggerApiService
import com.agvahealthcare.ventilator_ext.api.ServerLogger
import com.agvahealthcare.ventilator_ext.api.model.DeviceIdResponseModel
import com.agvahealthcare.ventilator_ext.api.model.alarmDataModel.AlarmRequestBodyModel
import com.agvahealthcare.ventilator_ext.database.entities.EventDataModel
import com.agvahealthcare.ventilator_ext.logs.event.EventViewModel
import com.agvahealthcare.ventilator_ext.model.AlarmModel
import com.agvahealthcare.ventilator_ext.model.DataStoreModel
import com.agvahealthcare.ventilator_ext.utility.utils.livedataConsolidators.PairMediatorLiveData
import com.agvahealthcare.ventilator_ext.utility.utils.livedataConsolidators.TripleMediatorLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

fun <T> MutableLiveData<T>.notifyObserver() {
    this.value = this.value
}

class DashBoardViewModel(application: Application) : AndroidViewModel(application) {



    //setter of the ackList
    val alarms = MutableLiveData(PriorityQueue<AlarmModel>(AlarmComparator()))


    val isTouchGraph = MutableLiveData<Boolean>()

    val currentTimeLiveData = MutableLiveData<Int>()

    val vtiValue = MutableLiveData<Float>()
    val vtApneaValue = MutableLiveData<Float>()
    val pipValue = MutableLiveData<Float>()
    private val alarmBuffer = arrayListOf<AlarmModel>()
    private val evemntBuffer = arrayListOf<EventViewModel>()
    private val EVENT_BUFFER_THRESHOLD = 50
    private val ALARM_BUFFER_THRESHOLD = 10
    val isUHIDSet=MutableLiveData<Boolean>()
    fun updateisUHIDSet (flag: Boolean){
        isUHIDSet.value=flag
    }
    fun addEvent(event:EventViewModel){

    }

    val graphPeekValue = MutableLiveData<String>()
    fun setGraphPeekValue(value: String){
        graphPeekValue.postValue(value)
    }
    val graphFreeze = MutableLiveData<Boolean>()

//    val tempValueForCondition = MutableLiveData<HashMap<String,Float>>()
   /* fun setTempValueForCondition(lbl:String,value:Float){
        tempValueForCondition.
    }
*/

    fun addAlarm(alarm: AlarmModel){

        alarms.value?.let {
            arrayListTemp.value?.add(alarm)
            it.add(alarm)

            try {
                CoroutineScope(Dispatchers.IO).launch {
                    Log.d("CHECK_ALARM_LINE",alarm.toString())
                    alarmBuffer.add(alarm)
                    if (!ServerLogger.sendAlarm(getApplication(), alarmBuffer)) ServerLogger.sendAlarm(getApplication(), alarmBuffer)
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        arrayListTemp.notifyObserver()
        alarms.notifyObserver()
    }
    //Operational hours in hours and minutes.
    val OPHours= MutableLiveData<String>()
    val serviceHours = MutableLiveData<String>()

    fun removeAlarm(alarm: AlarmModel){
        Log.d("alarmremove",alarm.code)
        arrayListTemp.value?.remove(alarm)
        arrayListTemp.notifyObserver()
        alarms.value?.remove(alarm)
        alarms.notifyObserver()
    }

    val avgSpo2LIstCache = MutableLiveData<ArrayList<Float>>()

    fun removeAllAlarms(){
        alarms.value?.clear()
        arrayListTemp.value?.clear()
        arrayListTemp.notifyObserver()
        alarms.notifyObserver()
    }




    val arrayListTemp = MutableLiveData(arrayListOf<AlarmModel>())
    val modeChange = MutableLiveData<Boolean>()


    val hfncResponse = MutableLiveData<Array<String>>()
    fun setHFNCResponse(value: Array<String>){
        hfncResponse.value = value
    }
    fun getHighestPriorityAlarm() = alarms.value?.peek()

    val numberOfDataEntries=MutableLiveData<Int>()
    fun numbrDataEntry(v:Int){
        numberOfDataEntries.value=v
        Log.d("value",numberOfDataEntries.value.toString())
    }

    val entryInflatedCounter=MutableLiveData<Int>()
    fun entryInflatCountr(v:Int){
        entryInflatedCounter.value=v
        Log.d("value",entryInflatedCounter.value.toString())
    }

    val logsDateUpdate = MutableLiveData<String>()


    //scoped up values for the information regarding Battery
    val ventBatteryLevel : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun setVentBatteryLevel(batteryLevel: Int) {
        ventBatteryLevel.value = batteryLevel
    }

    val isNebulizerActive = MutableLiveData<Boolean>()
    fun updateIsNebulizerActive(flag: Boolean){
        isNebulizerActive.value=flag
    }
    val isgraphFreeze = MutableLiveData<Boolean>()
    fun updateisGraphFreeze(flag: Boolean){
        isgraphFreeze.value = flag
    }

    val noOxygen = MutableLiveData<Boolean>()
    fun updateonNoOxygen(flag:Boolean){
        noOxygen.postValue(flag)
    }



    val ventBatteryHealth : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    fun setVentBatteryHealth(ventBattHealth: Int) {
        ventBatteryHealth.value = ventBattHealth
    }

    val ventBatteryRemainingTime : MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    fun setVentBatteryRemainingTime(ventBR: Int) {
        ventBatteryRemainingTime.value = ventBR
    }
    val isBatteryConnected=MutableLiveData<Boolean>() 
    fun setBAtteryConnectedFlag(isConnected:Boolean){
        isBatteryConnected.value=isConnected
    }
    val breathData=MutableLiveData<DataStoreModel>()
    //values required for the LogsTableFragment
    val dataStoreList=MutableLiveData<ArrayList<DataStoreModel>>()
    fun updateDataStoreList(list: ArrayList<DataStoreModel>){
        dataStoreList.value=list
    }
    val listOfDataStoreList = MutableLiveData<ArrayList<ArrayList<DataStoreModel>>>()
    fun updateListOfDataStoreList(list: ArrayList<ArrayList<DataStoreModel>>){
        listOfDataStoreList.value=list
    }
    val isLogsTrendsFragmentVisible=MutableLiveData<Boolean>()
    fun updateIsLogsTrendsFragmentVisible(flag:Boolean){
        isLogsTrendsFragmentVisible.value=flag
    }

    val isLogsEventsFragmentVisible= MutableLiveData<Boolean>()
    fun updateIsEventsFragmentVisible(flag:Boolean){
        isLogsEventsFragmentVisible.value=flag
    }
    val isLogsAlarmFragmentVisible= MutableLiveData<Boolean>()
    fun updateIsAlarmsFragmentVisible(flag:Boolean){
        isLogsAlarmFragmentVisible.value=flag
    }

    val tempVti=MutableLiveData<String>()
    val tempRR=MutableLiveData<String>()
    val RSBI=MutableLiveData<String>()

    val tempInspTime=MutableLiveData<String>()
    val tempExpTime=MutableLiveData<String>()
    val tempIERatio = MutableLiveData<String>()

    val abcdData = MutableLiveData<Map<String,String>>()
    val isabcdDataUpdated = MutableLiveData<Boolean>()




    open class AlarmComparator : Comparator<AlarmModel> {

        override fun compare(
            p0: AlarmModel,
            p1: AlarmModel): Int {
            if(p0.priority < p1.priority) return 1 else if (p0.priority > p1.priority) return  -1
            return  0
        }
    }

    class PriorityComparator : Comparator<AlarmModel> {
        override fun compare(
            o1: AlarmModel,
            o2: AlarmModel
        ): Int {
            return o2.priority.compareTo(o1.priority)
        }
    }
    var SmartSpo2Toggle = MutableStateFlow(false)
    var spo2hrDataStreaming = MutableStateFlow(false)
    var hrSpo2DataStreaming = MutableStateFlow(false)
    val avgTempSpo2List= MutableLiveData<ArrayList<Float>>()
    val avgTempHrList=MutableLiveData<ArrayList<Float>>()

    val Spo2TempMean = MutableStateFlow<Float?>(null)
    val HrTempMean = MutableStateFlow<Float?>(null)



}