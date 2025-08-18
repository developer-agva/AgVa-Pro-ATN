package com.agvahealthcare.ventilator_ext.manager

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.ventiPref
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DataStoreManager( val context: Context) {

    private val HARDWARE_SERIAL_NUMBER = stringPreferencesKey("hardware_serial_number")
    private val LAST_HOURS = longPreferencesKey("last_hour")
    private val TOTAL_HOURS = longPreferencesKey("total_hours")
    private val SERIAL_NUMBER = intPreferencesKey("serial_number")
    private val NEBULIZER_CHECK = booleanPreferencesKey("nebulizer_check")
    private val STANDBY_CHECK = booleanPreferencesKey("standby_check")
    private val X_MAX_VALUE = doublePreferencesKey("x_max_value")
    private val STARTUP_CHECK_FLAG = booleanPreferencesKey("startup_check_flag")
    private val STARTUP_CHECK_VALUE = stringPreferencesKey("startup_check_value")
    private val CHECK_DASH_REBOOT = booleanPreferencesKey("dashboard_reboot_status")
    private val CHECK_KNOB_REBOOT = booleanPreferencesKey("knob_reboot_status")
    private val HARDWARE_MAC_DATA_KEY = stringPreferencesKey("hardware_mac_key")
    private val CHECK_CURRENT_ACTIIVTY = stringPreferencesKey("check_current_activity")

    suspend fun  saveCurrentActivity(value: String){
        context.ventiPref.edit {
            it[CHECK_CURRENT_ACTIIVTY] = value
        }
    }

    fun getCurrentActivity() : Flow<String> = context.ventiPref.data.map {
        it[CHECK_CURRENT_ACTIIVTY] ?: "Splash"
    }

    suspend fun  saveKnobRebootStatusFlag(value: Boolean){
        context.ventiPref.edit {
            it[CHECK_KNOB_REBOOT] = value
        }
    }



    fun getKnobRebootStatusFlag() : Flow<Boolean> = context.ventiPref.data.map {
        it[CHECK_KNOB_REBOOT] ?: true
    }

    suspend fun  saveDashRebootStatusFlag(value: Boolean){
        context.ventiPref.edit {
            it[CHECK_DASH_REBOOT] = value
        }
    }
    fun getHardwareMACAddress(): Flow<String> = context.ventiPref.data
        .map { preferences ->
            preferences[HARDWARE_MAC_DATA_KEY] ?: ""
        }

    suspend fun saveHardwareMACAddress(value:String){
        context.ventiPref.edit {
            it[HARDWARE_MAC_DATA_KEY] = value
        }
    }

    fun getDashRebootStatusFlag() : Flow<Boolean> = context.ventiPref.data.map {
        it[CHECK_DASH_REBOOT] ?: true
    }


    suspend fun  saveStandByCheckFlag(value: Boolean){
        context.ventiPref.edit {
            it[STANDBY_CHECK] = value
        }
    }

    fun getStandByCheckFlag() : Flow<Boolean> = context.ventiPref.data.map {
        it[STANDBY_CHECK] ?: true
    }

    suspend fun  saveStartUpCheckFlag(value: Boolean){
        context.ventiPref.edit {
            it[STARTUP_CHECK_FLAG] = value
        }
    }

    fun getStartUpCheckFlag() : Flow<Boolean> = context.ventiPref.data.map {
        it[STARTUP_CHECK_FLAG] ?: true
    }



    suspend fun saveStartUpCheckValue(value: String){
        context.ventiPref.edit {
            it[STARTUP_CHECK_VALUE] = value.toString()
            Log.i("CHECKLIST",it.toString())
        }
    }

    fun getStartUpCheckValue() : Flow<String> = context.ventiPref.data.map {
        it[STARTUP_CHECK_VALUE] ?: ""
    }

//All the data for the data store implemented here.
    suspend fun saveHardwareSerialNumber(value: String){
        context.ventiPref.edit {
            it[HARDWARE_SERIAL_NUMBER] = value
        }
    }

    fun getHardwareSerialNumber() : Flow<String> = context.ventiPref.data.map {
        it[HARDWARE_SERIAL_NUMBER] ?: "A012306003"
    }

    // firstventilatorSale = A012306001
    // secondVentilatorSale = A012306002

    suspend fun saveNebulizerCheck(value: Boolean){
        context.ventiPref.edit {
            it[NEBULIZER_CHECK] = value
        }
    }

    fun getNebulizerCheck() :Flow<Boolean> = context.ventiPref.data.map {
        it[NEBULIZER_CHECK] ?: true
    }

    suspend fun saveXMaxValue(value:Double){
        context.ventiPref.edit {
            it[X_MAX_VALUE] = value
        }
    }

    fun getXMaxValue() : Flow<Double> = context.ventiPref.data.map {
        it[X_MAX_VALUE] ?: 12.9
    }

    suspend fun saveSerialNumber(value: Int){
        context.ventiPref.edit {
            it[SERIAL_NUMBER] = value
        }
    }

    fun getSerialNumber() : Flow<Int> = context.ventiPref.data.map {
        it[SERIAL_NUMBER] ?: 0
    }

    suspend fun saveLastHours(value: Long) {
        context.ventiPref.edit { preferences ->
            preferences[LAST_HOURS] = value
        }
    }

    fun getLastHours(): Flow<Long> = context.ventiPref.data
        .map { preferences ->
            preferences[LAST_HOURS] ?: 0L
        }

    suspend fun saveTotalHours(value: Long) {
        context.ventiPref.edit { preferences ->
            preferences[TOTAL_HOURS] = value
        }
    }

    fun getTotalHours(): Flow<Long> = context.ventiPref.data
        .map { preferences ->
            preferences[TOTAL_HOURS] ?: 0L
        }
}