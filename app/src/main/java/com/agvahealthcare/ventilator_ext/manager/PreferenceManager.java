package com.agvahealthcare.ventilator_ext.manager;


import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.FIRST_FILTER_NAME;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.MAX_RANGE_PRESSURE_ADULT_PEDIA;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.MIN_RANGE_PRESSURE_ADULT_PEDIA;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.GRAPH_POINTS_MAX;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.Gender;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.Gender.TYPE_MALE;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_APNEA_RR;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_APNEA_TRIG_FLOW;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_APNEA_VT;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_ET_PRESSURE;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_FIO2;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_FIO2_DEV;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_FLOW;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_FREQUENCY;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_HR_LIMIT;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_INSP_PAUSE;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_PEAK_FLOW;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_PEEP;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_PEEP_VALVE;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_PIP;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_PPLAT;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_RR;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_SLOPE;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_SUPPORT_PRESSURE;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_TAPNEA;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_TARGET_SPO2;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_TARGET_VOLUME;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_TEXP;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_TINSP;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_TLOW;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_TRIG_FLOW;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.LBL_VTI;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.MODE_NIV;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.ModeType.TYPE_Pressure;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.PatientProfile;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.PatientProfile.TYPE_ADULT;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.PatientProfile.TYPE_NEONAT;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.SENSOR_CALIBRATION_SUCCESS;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.SENSOR_MISSING;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.filterAlarmLimitsbyPatientType;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.getModeCategory;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.StringRes;

import com.agvahealthcare.ventilator_ext.R;
import com.agvahealthcare.ventilator_ext.dashboard.chart.parentType;
import com.agvahealthcare.ventilator_ext.model.SensorCalibration;
import com.agvahealthcare.ventilator_ext.system.configuration.VentilatorType;
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils;
import com.agvahealthcare.ventilator_ext.utility.utils.Configs;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.scichart.drawing.utility.ColorUtil;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Set;

//For maintaining the local storage of the dat saved for the tiles and the mode.

/**
 * Created by MOHIT MALHOTRA on 28-09-2018.
 */

public class PreferenceManager {

    // venti configurations
    private static final String PREF_SELF_TEST_DATA = "PREF_SELF_TEST_DATA";

    private static final String PREF_PRESSURE_SENSOR_ONE = "PREF_PRESSURE_SENSOR_ONE";
    private static final String PREF_PRESSURE_SENSOR_TWO = "PREF_PRESSURE_SENSOR_TWO";
    private static final String PREF_PRESSURE_SENSOR_THREE = "PREF_PRESSURE_SENSOR_THREE";
    private static final String PREF_INSP_FLOW_SENSOR = "PREF_INSP_FLOW_SENSOR";
    private static final String PREF_EXP_FLOW_SENSOR = "PREF_EXP_FLOW_SENSOR";

    private static final String PREF_OXYGEN_SENSOR = "PREF_OXYGEN_SENSOR";
    private static final String PREF_NEO_SENSOR = "PREF_NEO_SENSOR";
    private static final String PREF_SPO2_SENSOR = "PREF_SPO2_SENSOR";

    private static final String PREF_PROP_VALVE = "PREF_PROP_VALVE";
    private static final String PREF_NEO_PCB_TYPE = "PREF_NEO_PCB_TYPE";
    private static final String PREF_NEB_TYPE = "PREF_NEB_TYPE";
    private static final String PREF_OXY_CONCENTRATOR = "PREF_OXY_CONCENTRATOR";
    private static final String PREF_VENTI_CONFIG_STATUS = "PREF_VENTI_CONFIG_STATUS";

    private static final String PREF_VENTI_TYPE_STATUS = "pref_venti_type_status";
    private static final String PREF_PEEP_ALARM_STATUS = "pref_peep_alarm_status";


    private static final String PREF_AUTO_FLOW = "pref_auto_flow";
    private static final String PREF_ACTIVITY_TRACK = "pref_activity_track";
    private static final String PREF_CONTROL_PARAMS = "pref_control_params";
    private static final String PREF_IS_RESTART = "pref_is_restart";

    private static final String PREF_CONTROL_PARAMS_NEONATE = "pref_control_params_neonate";
    private static final String PREF_CONTROL_PARAMS_NASAL_PRONGS = "pref_control_params_nasal_prongs";

    private static final String LIMIT_SEPARATOR = ",";
    private static final String PREF_OXYGEN_LEVEL = "pref_oxygen_level";

    private static final String CONFIGURATION_SHARED_PREFERENCES = "ventilator.settings";

    private static final String BLE_SERVICE_STATUS = "service_status.ble";

    private static final String PREF_NEO_ACTIVE_STATUS = "pref_neoActive";

    private static final String PREF_CURRENT_UID = "pref_userid";
    private static final String PREF_SELECTED_OPTION = "pref_selected_option";
    private static final String PREF_LAST_UID = "pref_last_userid";
    private static final String PREF_VENTILATOR_SOFTWARE_VERSION = "pref_vent_sw_version";

    private static final String PREF_GRAPH_PRESSURE = "pref_graph_pressure";
    private static final String PREF_GRAPH_VOLUME = "pref_graph_volume";
    private static final String PREF_GRAPH_FLOW = "pref_graph_flow";
    private static final String PREF_GRAPH_PRESSURE_FLOW = "pref_graph_pressure_flow";

    private static final String PREF_SUPPORT_INSP_PRESSURE = "pref_support_pinsp";
    private static final String PREF_SUPPORT_EXP_PRESSURE = "pref_support_pexp";
    private static final String PREF_SUPPORT_PRESSURE = "pref_support_pressure";
    private static final String PREF_SLOPE = "pref_slope";
    private static final String PREF_INSP_PAUSE = "pref_insp_pause";
    private static final String PREF_PEEP_VALVE = "pref_peep_valve";
    private static final String PREF_SPONT_VT = "pref_spont_vt";
    private static final String PREF_TARGET_SPO2 = "pref_target_spo2";
    private static final String PREF_HR_LIMIT = "pref_hr_limit";
    private static final String PREF_TARGET_VOLUME = "pref_target_volume";
    private static final String PREF_FLOW = "pref_flow";
    private static final String PREF_FIO2_DEV = "pref_fio2_dev";
    private static final String PREF_TEXP = "pref_texp";
    private static final String PREF_THigh = "pref_thigh";
    private static final String PREF_TLow = "pref_tlow";
    private static final String PREF_PHigh = "pref_phigh";
    private static final String PREF_PLow = "pref_plow";

    private static final String PREF_PIP = "pref_pip";
    private static final String PREF_PEAK_FLOW = "pref_peak_flow";
    private static final String PREF_PEEP = "pref_peep";
    private static final String PREF_RR = "pref_rr";
    private static final String PREF_NO_O2_NO_NEB = "pref_neb";
    private static final String PREF_PIP_VIA_VGV = "pref_pip_via_vgv";
    private static final String PREF_TIDAL_VOLUME = "pref_vti";
    private static final String PREF_TRIG_FLOW = "pref_trig_flow";
    private static final String PREF_TLOW = "pref_tlow";
    private static final String PREF_PLATEAU_PRESSURE = "pref_pplat";
    private static final String PREF_INSP_TIME = "pref_tisnp";
    private static final String PREF_UHID = "pref_uhid";
    private static final String PREF_FIO2 = "pref_fio2";
    private static final String PREF_IS_DEEP_SLEEP = "pref_deep_sleep_state";
    private static final String PREF_PATIENT_PROFILE = "pref_patient_profile";
    private static final String PREF_PBOUND_BLE_MAC = "pref_bound_ble_address";
    private static final String PREF_PBOUND_BLE_NAME = "pref_bound_ble_identifier";
    private static final String PREF_LAST_SETTINGS_TIME = "pref_last_settings_time";

    private static final String PREF_RADIO_PRONG = "pref_radio_prong";
    private static final String PREF_KNOB_STATUS = "pref_knob_status";
    private static final String PREF_RADIO_MASK = "pref_radio_mask";
    private static final String PREF_RADIO_INV = "pref_radio_invasive";
    private static final String PREF_WORK_SUCCESS = "pref_work_success";

    // APNEA PARAMETERS
    private static final String PREF_APNEA_SETTINGS_STATUS = "pref_apnea_status";

    private static final String PREF_ALARM_PAGE = "pref_alarm_page";
    private static final String PREF_ALARM_PAGE_UHID = "pref_alarm_page_uhid";

    private static final String PREF_EVENT_PAGE = "pref_event_page";
    private static final String PREF_EVENT_PAGE_UHID = "pref_event_page_uhid";

    // temporary for standby control
    private static final String PREF_IRV_STATUS_TEMP = "pref_irv_status_temp";
    private static final String PREF_VGV_STATUS_TEMP = "pref_vgv_status_temp";
    private static final String PREF_EtCuff_STATUS_TEMP = "pref_etcuff_status_temp";
    private static final String PREF_Deflashed_STATUS_TEMP = "pref_deflashed_status_temp";
    private static final String PREF_SMART_FIO2_STATUS_TEMP = "pref_smart_fio2_status_temp";
    private static final String PREF_APNEA_SETTINGS_STATUS_TEMP = "pref_apnea_status_temp";


    private static final String PREF_LEAK_FACTOR = "pref_leak_factor";

    private static final String PREF_TUBE_DIA_STATUS = "pref_tubeDia_status";
    private static final String PREF_APNEA_RR = "pref_apnea_rr";
    private static final String PREF_APNEA_TIME = "pref_apnea_time";
    private static final String PREF_APNEA_TIDAL_VOLUME = "pref_apnea_vti";
    private static final String PREF_APNEA_TRIG_FLOW = "pref_apnea_trig_flow";
    private static final String PREF_Et_PRESSURE = "pref_et_pressure";

    private static final String PREF_FREQUENCY = "pref_frequency";


    //The smart Fio2 average section code section
    private static final String PREF_SMARTFIO2 = "pref_smartfio2";
    private static final String PREF_TARGETSPO2 = "pref_targetspo2";
    private static final String PREF_HRLIMITS = "pref_hrlimits";
    private static final String PREF_SMART_FIO2_SETTINGS_STATUS = "pref_smart_fio2_status";

    private static final String PREF_TEMP_VTAS_STATUS = "pref_temp_Vtas";
    private static final String PREF_GRAPH_PARENT_TYPE = "pref_graph_parent_type";
    private static final String PREF_TREND_DURATION = "pref_trend_duration";

    private static final String PREF_BODY_WEIGHT = "pref_body_weight";
    private static final String PREF_MODE_TYPE = "pref_mode_type";
    private static final String PREF_NEO_BODY_WEIGHT = "pref_Neo_body_weight";
    private static final String PREF_PED_BODY_WEIGHT = "pref_Ped_body_weight";
    private static final String PREF_AGE = "pref_age";
    private static final String PREF_NEO_AGE = "pref_Neo_age";
    private static final String PREF_PED_AGE = "pref_Ped_age";

    private static final String PREF_BODY_HEIGHT = "pref_body_height";


    private static final String PREF_NEO_BODY_HEIGHT = "pref_Neo_body_height";

    private static final String PREF_PED_BODY_HEIGHT = "pref_Ped_body_height";

    private static final String PREF_IS_LOGGED_IN = "pref_login";
    private static final String PREF_GENDER = "pref_gender";

    private static final String PREF_CURRENT_GRAPH_COLOR = "pref_current_graph_color";


    private static final String PREF_STANDBY_STATUS = "pref_standby_status";
    private static final String PREF_SHUTDOWN_STATUS = "pref_shutdown_Status";
    private static final String PREF_VENTILATION_MODE = "pref_ventilation_mode";
    private static final String PREF_LAST_USED_VENT_MODE = "pref_last_vent_mode";
    private static final String PREF_EMERGENCY_CONTACT = "pref_emergency_contact";
    private static final String PREF_GRAPH_AUTOSCALE = "pref_graph_autoscaling";
    private static final String PREF_LEAK_COMPENSATE = "pref_leak_compensate";
    private static final String PREF_PT_DISCHARGE = "pref_patient_discharge";

    private static final String PREF_TUBE_BLOCKAGE_ALARM = "pref_tube_blockage_alarm";
    private static final String PREF_CUFF_LEAKAGE_ALARM = "pref_cuff_leakage_alarm";
    private static final String PREF_ALARM_SUGGESTION = "pref_alarm_suggestion";
    private static final String PREF_LEAK_BASED_DISCONNECT = "pref_leak_based_disconnect";
    private static final String PREF_IRV_STATUS = "pref_irv_status";
    private static final String PREF_VGV_STATUS = "pref_vgv_status";
    private static final String PREF_EtCuff_STATUS = "pref_etcuff_status";
    private static final String PREF_Deflashed_STATUS = "pref_deflashed_status";
    private static final String PREF_SMART_FIO2_STATUS = "pref_smart_fio2_status";
    private static final String PREF_VOLUME_LEVEL = "pref_volume_level";
    private static final String PREF_GRAPH_POINTS = "pref_graph_points";
    private static final String PREF_IS_PEDIATRIC_ACTIVE = "pref_is_pediatric_active";
    private static final String PREF_IS_NEONATAL_ACTIVE = "pref_is_neonatal_active";
    private static final String PREF_IS_OXYGEN_HOLD_ACTIVE = "pref_is_oxygen_hold_active";
    private static final String PREF_IS_EXPANDED_ALARM_VISIBLE = "pref_expand_alarm_visibility";
    private static final String PREF_SCREEN_LOCK = "pref_screen_lock";
    private static final String PREF_VOLUME = "pref_volume";
    private static final String PREF_TUBE_DIA = "pref_tube_Dia";
    private static final String PREF_NEBULISER = "pref_nebuliser";
    private static final String PREF_DISCHARGE_DATE = "pref_discharge_date";
    private static final String PREF_TUBE_RESISTANCE = "pref_tubeResistance";
    private static final String PREF_TUBE_COMPLIANCE = "pref_tubeCompliance";
    private static final String PREF_STARTUP_COUNT = "pref_startCount";


    // MANEUVERS LIMITS
    private static final String PREF_MANEUVERS_MIN_MAX = "pref_manuvers_limits";
    private static final String PREF_MANEUVERS_PEEP = "pref_manuvers_peep";
    private static final String PREF_MANEUVERS_PLATEAU = "pref_manuvers_plateau";
    private static final String PREF_MANEUVERS_STATIC_COMPLINES = "pref_manuvers_static_complines";
    private static final String PREF_EXPIRATORY_DATE = "pref_expiratory_date";
    private static final String PREF_INSPIRATORY_DATE = "pref_inspiratory_date";
    private static final String PREF_MEASURE_TIME = "pref_measure_time";


    private static final String PREF_TUBE_COMPLIANCE_DATE = "pref_tube_complaince_date";
    private static final String PREF_TUBE_RESISTANCE_DATE = "pref_tube_resistance_date";


    // Test & Calibration
    private static final String PREF_TURBINE_CALIBRATION = "pref_turbine_calibration";
    private static final String PREF_INSP_FLOW_SENSOR_CALIBRATION = "pref_insp_flow_sensor_calibration";
    private static final String PREF_EXP_FLOW_SENSOR_CALIBRATION = "pref_exp_flow_sensor_calibration";

    private static final String PREF_OXYGEN_CALIBRATION = "pref_oxygen_calibration";
    private static final String PREF_PRESSER_CALIBRATION = "pref_presser_calibration";
    private static final String PREF_EXHALE_VALVE_CALIBRATION = "pref_exhale_valve_calibration";
    private static final String PREF_LEAK_TEST_CALIBRATION = "pref_leak_test_calibration";

    //Operational hours in hours and minutes.
    private static final String PREF_DASHBOARD_RUNNING_TIME = "pref_dashboard_running_time";
    //Service hours in hours and minutes.
    private static final String PREF_DASHBOARD_SERVICE_RUNNING_TIME = "pref_dashboard_service_running_time";


    // VENTILATOR PARAM LIMITS
    private static final String PREF_PIP_MIN_MAX = "pref_pip_limits";
    private static final String PREF_VTI_MIN_MAX = "pref_vti_limits";
    private static final String PREF_VTE_MIN_MAX = "pref_vte_limits";
    private static final String PREF_PEEP_MIN_MAX = "pref_peep_limits";
    private static final String PREF_RR_MIN_MAX = "pref_rr_limits";

    private static final String PREF_TUBE_DIA_MIN_MAX = "pref_tube_Dia_limits";


    private static final String PREF_MVI_MIN_MAX = "pref_mvi_limits";
    private static final String PREF_MVE_MIN_MAX = "pref_mve_limits";
    private static final String PREF_FIO2_MIN_MAX = "pref_fio2_limits";
    private static final String PREF_SPO2_MIN_MAX = "pref_spo2_limits";
    private static final String PREF_TITOT_MIN_MAX = "pref_titot_limits";
    private static final String PREF_LEAK_MIN_MAX = "pref_leak_limits";


    // VENTILATOR ALARM STATESS
    private static final String PREF_PIP_ALARM_STATE = PREF_PIP_MIN_MAX + "_state";
    private static final String PREF_VTI_ALARM_STATE = PREF_VTI_MIN_MAX + "_state";
    private static final String PREF_VTE_ALARM_STATE = PREF_VTE_MIN_MAX + "_state";
    private static final String PREF_PEEP_ALARM_STATE = PREF_PEEP_MIN_MAX + "_state";
    private static final String PREF_RR_ALARM_STATE = PREF_RR_MIN_MAX + "_state";
    private static final String PREF_MVI_ALARM_STATE = PREF_MVI_MIN_MAX + "_state";
    private static final String PREF_MVE_ALARM_STATE = PREF_MVE_MIN_MAX + "_state";
    private static final String PREF_FIO2_ALARM_STATE = PREF_FIO2_MIN_MAX + "_state";
    private static final String PREF_SPO2_ALARM_STATE = PREF_SPO2_MIN_MAX + "_state";
    private static final String PREF_TITOT_ALARM_STATE = PREF_TITOT_MIN_MAX + "_state";
    private static final String PREF_LEAK_ALARM_STATE = PREF_LEAK_MIN_MAX + "_state";

    private static final String PREF_VENTI_DETAILS = "pref_venti_details";

    // LIMITING ALARMS
    private static final String PREF_O2_LIMIT = "pref_o2_limit";
    //log start time and end time
    private static final String PREF_LOG_START_DATETIME = "pref_logstarttime";
    private static final String PREF_LOG_END_DATETIME = "pref_logendtime";
    private static final String PREF_IS_LOGTIME_SAVED = "pref_isTimeSaved";

    //sensor Analysis
    private static final String PREF_SENSOR_LOW_PRESSURE_O2 = "pref_sens_low_o2";
    private static final String PREF_SENSOR_HIGH_PRESSURE_O2 = "pref_sens_high_o2";
    private static final String PREF_SENSOR_CO2 = "pref_sens_co2";
    private static final String PREF_SENSOR_SPO2 = "pref_sens_spo2";
    private static final String PREF_SENSOR_TEMP = "pref_sens_temp";
    private static final String PREF_SENSOR_DIAPHRAGM = "pref_sens_diaphragm";
    private static final String PREF_SENSOR_ADC = "pref_sens_adc";
    private static final String PREF_SENSOR_HIGH_PRESSURE_LINE_O2 = "pref_sens_high_line_pressure";
    private static final String PREF_SENSOR_EXP_PRESSURE = "pref_sens_exp_pressure";
    private static final String PREF_SENSOR_INSP_PRESSURE = "pref_sens_insp_pressure";
    private static final String PREF_SENSOR_INAHLE_FLOW = "pref_sens_insp_flow";

    private static final String PREF_SENSOR_EXHALE_FLOW = "pref_sens_exp_flow";
    private static final String PREF_SENSOR_NEONATE_FLOW = "pref_sens_neo_flow";
    private static final String PREF_OPERATIONAL_HOURS_START_TIME = "pref_operation_hours_start_time";
    private static final String PREF_OPERATIONAL_HOURS_END_TIME = "pref_operation_hours_end_time";
    private static final String PREF_TOTAL_HOURS_TIME = "pref_total_hours_time";
    private static final String PREF_LAST_HOURS_TIME = "pref_last_hours_time";

    private static final String PREF_SERVICE_HOURS_START_TIME = "pref_service_hours_start_time";
    private static final String PREF_SERVICE_HOURS_END_TIME = "pref_service_hours_end_time";

    private static final String PREF_TUBE_COMPLIANCE_CALIBRATION = "pref_tube_compliance_calibration";
    private static final String PREF_TUBE_COMPLIANCE_CALIBRATION_STATUS = "pref_tube_compliance_calibration_status";
    private static final String PREF_TUBE_RESISTANCE_CALIBRATION = "pref_tube_resistance_calibration";
    private static final String PREF_TUBE_RESISTANCE_CALIBRATION_STATUS = "pref_tube_resistance_calibration_status";
    private static final String PREF_CONCENTRATOR_STATUS = "pref_concentrator_status";

    private static final String PREF_IE_TILE_STATUS = "pref_ie_tile_status";
    private static final String PREF_NEBULISER_STATUS = "pref_neb_status";

    private static final String PREF_FILLED_PRESSURE = "pref_pressure_filled";
    private static final String PREF_FILLED_VOLUME = "pref_volume_filled";
    private static final String PREF_FILLED_FLOW = "pref_flow_filled";
    private static final String PREF_REBOOT_STATUS = "PREF_REBOOT_STATUS";

    private static final String PREF_OPERATING_HOURS = "pref_operating_hours";
    private static final String PREF_SERVICE_HOURS = "pref_service_hours";


    private static final String PREF_PRIMARY_CONTROL_PARAMS = "pref_primary_controls_params";

    private static final String PREF_DOWNLOAD_STATUS = "pref_download_status";
    private static final String PREF_DOWNLOAD_TIME = "pref_download_time";
    private static final String PREF_DOWNLOAD_TYPE = "pref_download_type";

    private static final String PREF_RESTORE_STATUS = "pref_restore_status";
    private static final String PREF_RESTORE_TIME = "pref_restore_time";
    private static final String PREF_RESTORE_TYPE = "pref_restore_type";


    private static final String PREF_UPDATE_STATUS = "pref_update_status";
    private static final String PREF_UPDATE_TIME = "pref_update_time";
    private static final String PREF_UPDATE_TYPE = "pref_update_type";
    private static final String PREF_PAYMENT_STATUS = "pref_payment_status";
    private static final String PREF_VENTI_NEED_LOCK = "PREF_VENTI_NEED_LOCK";
    private static final String PREF_DYN_LUNG_MODULE = "PREF_DYN_LUNG_MODULE";
    private static final String PREF_DEBUG_PARAMS_STATUS = "PREF_DEBUG_PARAMS_STATUS";
    private static final String PREF_ROLLOVER_STATUS = "PREF_ROLLOVER_STATUS";
    private static final String PREF_PRESSURE_PADDING_STATUS = "PREF_PRESSURE_PADDING_STATUS";
    private static final String PREF_VOLUME_PADDING = "PREF_VOLUME_PADDING";
    private static final String PREF_FLOW_PADDING = "PREF_FLOW_PADDING";
    private static final String PREF_VOLUME_GRAPH_FILLED_STATUS = "PREF_VOLUME_GRAPH_FILLED_STATUS";
    private static final String PREF_FLOW_GRAPH_FILLED_STATUS = "PREF_FLOW_GRAPH_FILLED_STATUS";
    private static final String PREF_PRESSURE_GRAPH_FILLED_STATUS = "PREF_PRESSURE_GRAPH_FILLED_STATUS";

    private Context context;
    private SharedPreferences sp;
    private Gson gson;

    public PreferenceManager(Context context) {
        this.context = context;
        this.sp = context.getSharedPreferences(CONFIGURATION_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void saveSelfTestData(String value) {
        updateData(PREF_SELF_TEST_DATA, value);
    }

    public String readSelfTestData() {
        return sp.getString(PREF_SELF_TEST_DATA, "");
    }

    public void savePressurePaddingStatus(Boolean value) {
        updateData(PREF_PRESSURE_PADDING_STATUS, value);
    }

    public Boolean readPressurePaddingStatus() {
        return sp.getBoolean(PREF_PRESSURE_PADDING_STATUS, false);
    }

    public void savePressureGraphTypeStatus(Boolean value) {
        updateData(PREF_PRESSURE_GRAPH_FILLED_STATUS, value);
    }

    public Boolean readPressureGraphTypeStatus() {
        return sp.getBoolean(PREF_PRESSURE_GRAPH_FILLED_STATUS, false);
    }

    public void saveVolumeGraphTypeStatus(Boolean value) {
        updateData(PREF_VOLUME_GRAPH_FILLED_STATUS, value);
    }

    public Boolean readVolumeGraphTypeStatus() {
        return sp.getBoolean(PREF_VOLUME_GRAPH_FILLED_STATUS, false);
    }

    public void saveFlowGraphTypeStatus(Boolean value) {
        updateData(PREF_FLOW_GRAPH_FILLED_STATUS, value);
    }

    public Boolean readFlowGraphTypeStatus() {
        return sp.getBoolean(PREF_FLOW_GRAPH_FILLED_STATUS, false);
    }

    public void saveRolloverModifierStatus(Boolean value) {
        updateData(PREF_ROLLOVER_STATUS, value);
    }

    public Boolean readRolloverModifierStatus() {
        return sp.getBoolean(PREF_ROLLOVER_STATUS, false);
    }

    public void saveDebugParamsStatus(Boolean value) {
        updateData(PREF_DEBUG_PARAMS_STATUS, value);
    }

    public Boolean readDebugParamsStatus() {
        return sp.getBoolean(PREF_DEBUG_PARAMS_STATUS, false);
    }

    public void savePeepAlarmStatus(Boolean value) {
        updateData(PREF_PEEP_ALARM_STATUS, value);
    }

    public Boolean readPeepAlarmStatus() {
        return sp.getBoolean(PREF_PEEP_ALARM_STATUS, false);
    }

    public void saveDynamicLungsModuleStatus(Boolean value) {
        updateData(PREF_DYN_LUNG_MODULE, value);
    }

    public Boolean readDynamicLungsModuleStatus() {
        return sp.getBoolean(PREF_DYN_LUNG_MODULE, false);
    }

    public void saveLockedStatus(String val) {
        updateData(PREF_PAYMENT_STATUS, val);
    }

    public String readLockedStatus() {
        return sp.getString(PREF_PAYMENT_STATUS, "Unlocked");
    }

    public void setVentilatorNeedToLock(Boolean val) {
        updateData(PREF_VENTI_NEED_LOCK, val);
    }

    public Boolean readVentilatorNeedToLock() {
        return sp.getBoolean(PREF_VENTI_NEED_LOCK, true);
    }

    // start for ota side embedded

    // status ota
    public void setDownloadStatus(Boolean value) {
        updateLimitState(PREF_DOWNLOAD_STATUS, value);
    }

    public Boolean getDownloadStatus() {
        return sp.getBoolean(PREF_DOWNLOAD_STATUS, false);
    }

    public void setUpdateStatus(Boolean value) {
        updateLimitState(PREF_UPDATE_STATUS, value);
    }

    public Boolean getUpdateStatus() {
        return sp.getBoolean(PREF_UPDATE_STATUS, false);
    }

    public void setRestoreStatus(Boolean value) {
        updateLimitState(PREF_RESTORE_STATUS, value);
    }

    public Boolean getRestoreStatus() {
        return sp.getBoolean(PREF_RESTORE_STATUS, false);
    }

    public Boolean readIETileStatus() {
        return sp.getBoolean(PREF_IE_TILE_STATUS, false);
    }

    public void setIETileStatus(boolean isActive) {
        updateData(PREF_IE_TILE_STATUS, isActive);
    }

    // ota status type

    public void setDownloadType(String value) {
        updateData(PREF_DOWNLOAD_TYPE, value);
    }

    public String getDownloadType() {
        return sp.getString(PREF_DOWNLOAD_TYPE, "");
    }

    public void setUpdateType(String value) {
        updateData(PREF_UPDATE_TYPE, value);
    }

    public String getUpdateType() {
        return sp.getString(PREF_UPDATE_TYPE, "");
    }

    public void setRestoreType(String value) {
        updateData(PREF_RESTORE_TYPE, value);
    }

    public String getRestoreType() {
        return sp.getString(PREF_RESTORE_TYPE, "");
    }


    // ota status date & time
    public void setDownloadTime(String value) {
        updateData(PREF_DOWNLOAD_TIME, value);
    }

    public String getDownloadTime() {
        return sp.getString(PREF_DOWNLOAD_TIME, "");
    }

    public void setUpdateTime(String value) {
        updateData(PREF_UPDATE_TIME, value);
    }

    public String getUpdateTime() {
        return sp.getString(PREF_UPDATE_TIME, "");
    }

    public void setRestoreTime(String value) {
        updateData(PREF_RESTORE_TIME, value);
    }

    public String getRestoreTime() {
        return sp.getString(PREF_RESTORE_TIME, "");
    }


    // end for ota embedded side


    public void setRebootStatusForHandshake(Boolean value) {
        updateLimitState(PREF_IS_RESTART, value);
    }

    public Boolean readRebootStatusForHandshake() {
        return sp.getBoolean(PREF_IS_RESTART, false);
    }


    public void setRebootStatus(Boolean value) {
        updateLimitState(PREF_REBOOT_STATUS, value);
    }

    public Boolean readRebootStatus() {
        return sp.getBoolean(PREF_REBOOT_STATUS, true);
    }


    public void setCurrentGraphColor(String key, Integer value) {
        updateData(key + PREF_CURRENT_GRAPH_COLOR, value);
    }

    public Integer readCurrentGraphColor(String key) {
        return sp.getInt(key + PREF_CURRENT_GRAPH_COLOR, ColorUtil.argb(255, 51, 153, 102));
    }

    //==============Venti configurations Start===================

    public void setPressureSensorOne(String value) {
        updateData(PREF_PRESSURE_SENSOR_ONE, value);
    }

    public String readPressureSensorOne() {
        return sp.getString(PREF_PRESSURE_SENSOR_ONE, "CONSENSIC");
    }

    public void setPressureSensorTwo(String value) {
        updateData(PREF_PRESSURE_SENSOR_TWO, value);
    }

    public String readPressureSensorTwo() {
        return sp.getString(PREF_PRESSURE_SENSOR_TWO, "CONSENSIC");
    }

    public void setPressureSensorThree(String value) {
        updateData(PREF_PRESSURE_SENSOR_THREE, value);
    }

    public String readPressureSensorThree() {
        return sp.getString(PREF_PRESSURE_SENSOR_THREE, "AMS");
    }

    public void setInspFlowSensor(String value) {
        updateData(PREF_INSP_FLOW_SENSOR, value);
    }

    public String readInspFlowSensor() {
        return sp.getString(PREF_INSP_FLOW_SENSOR, "HONEYWELL");
    }

    public void setExpFlowSensor(String value) {
        updateData(PREF_EXP_FLOW_SENSOR, value);
    }

    public String readExpFlowSensor() {
        return sp.getString(PREF_EXP_FLOW_SENSOR, "SFM");
    }

    public void setOxySensor(String value) {
        updateData(PREF_OXYGEN_SENSOR, value);
    }

    public String readOxySensor() {
        return sp.getString(PREF_OXYGEN_SENSOR, "ULTRASONIC");
    }

    public void setNeoSensor(String value) {
        updateData(PREF_NEO_SENSOR, value);
    }

    public String readNeoSensor() {
        return sp.getString(PREF_NEO_SENSOR, "IN-2");
    }

    public void setSpo2Sensor(String value) {
        updateData(PREF_SPO2_SENSOR, value);
    }

    public String readSpo2Sensor() {
        return sp.getString(PREF_SPO2_SENSOR, "SP-2");
    }

    public void setPropValve(String value) {
        updateData(PREF_PROP_VALVE, value);
    }

    public String readPropValve() {
        return sp.getString(PREF_PROP_VALVE, "CAMOZZI");
    }

    public void setNeoPCBType(String value) {
        updateData(PREF_NEO_PCB_TYPE, value);
    }

    public String readNeoPCBType() {
        return sp.getString(PREF_NEO_PCB_TYPE, "GENERIC");
    }

    public void setNebType(String value) {
        updateData(PREF_NEB_TYPE, value);
    }

    public String readNebType() {
        return sp.getString(PREF_NEB_TYPE, "PNEUMATIC");
    }

    public void setOxyConcentrator(Boolean value) {
        updateData(PREF_OXY_CONCENTRATOR, value);
    }

    public Boolean readOxyConcentrator() {
        return sp.getBoolean(PREF_OXY_CONCENTRATOR, false);
    }

    public void setVentiConfigSetupStatus(Boolean value) {
        updateData(PREF_VENTI_CONFIG_STATUS, value);
    }

    public Boolean readVentiConfigSetupStatus() {
        return sp.getBoolean(PREF_VENTI_CONFIG_STATUS, true);
    }


    //==============Venti configurations END===================

    //================= Log StartEnd DateTime ==============
    public void setLogStartTime(String startTime) {
        updateData(PREF_LOG_START_DATETIME, startTime);
    }

    public String readStartTime() {
        return sp.getString(PREF_LOG_START_DATETIME, null);
    }

    public void setLogEndTime(String endTime) {
        updateData(PREF_LOG_END_DATETIME, endTime);
    }

    public String readEndTime() {
        return sp.getString(PREF_LOG_END_DATETIME, null);
    }


    public void setIsLogTimeSaved(boolean isTimeSaved) {
        updateData(PREF_IS_LOGTIME_SAVED, isTimeSaved);
    }

    public Boolean readIsLogTimeSaved() {
        return sp.getBoolean(PREF_IS_LOGTIME_SAVED, false);
    }

    public void clearLogsFilterValues() {
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(PREF_LOG_START_DATETIME);
        editor.remove(PREF_LOG_END_DATETIME);
        editor.remove(PREF_IS_LOGTIME_SAVED);
        editor.apply();
    }

    //==========================================================

    public BluetoothDevice readBoundDevice() {
        return gson.fromJson(sp.getString(PREF_PBOUND_BLE_MAC, null), BluetoothDevice.class);
    }

    public String readBoundDeviceName() {
        return sp.getString(PREF_PBOUND_BLE_NAME, null);
    }

    public void setBoundDevice(BluetoothDevice device, String name) {
        updateData(PREF_PBOUND_BLE_MAC, gson.toJson(device));
        updateData(PREF_PBOUND_BLE_NAME, name);
    }

    // added pref for tube calibration compliance
    public void setComplianceTubeCalibration(String value) {

        if (value != null) {
            updateData(PREF_TUBE_COMPLIANCE_CALIBRATION, value);
        }
    }

    public void setLastSettingsTime(String data) {
        updateData(PREF_LAST_SETTINGS_TIME, data);
    }

    public String readLastSettingsTime() {
        return sp.getString(PREF_LAST_SETTINGS_TIME, "");
    }

    //The smart Fio2 average section code section
    public int readTargetSpO2() {
        return sp.getInt(readCurrentUid() + "." + PREF_TARGETSPO2, 0);
    }

    public void setTargetSpO2(int val) {
        updateData(readCurrentUid() + "." + PREF_TARGETSPO2, val);
    }

    //The smart Fio2 average section code section
    public int readHRLIMITS() {
        return sp.getInt(readCurrentUid() + "." + PREF_HRLIMITS, 0);
    }

    public void setHRLIMITS(int val) {
        updateData(readCurrentUid() + "." + PREF_HRLIMITS, val);
    }

    public Float readSmartFio2() {
        return sp.getFloat(readCurrentUid() + "." + PREF_SMARTFIO2, 0f);
    }

    public void setSmartFio2(Float val) {
        updateData(readCurrentUid() + "." + PREF_SMARTFIO2, val);
    }

    //DashBoardRunningTime hours in hours and minutes.
    public void setDashBoardRunningTime(long dashBoardRunningTime) {
        updateData(PREF_DASHBOARD_RUNNING_TIME, dashBoardRunningTime);
    }

    //DashBoardRunningTime hours in hours and minutes.
    public long readDashBoardRunningTime() {
        return sp.getLong(PREF_DASHBOARD_RUNNING_TIME, 0L);
    }

    //DashBoardRunningTime for Service hours in hours and minutes.
    //Service hours in hours and minutes.
    public void setDashBoardRunningTimeForService(long dashBoardserviceRunningTime) {
        updateData(PREF_DASHBOARD_SERVICE_RUNNING_TIME, dashBoardserviceRunningTime);
    }

    //DashBoardRunningTime for Service hours in hours and minutes.
    //Service hours in hours and minutes.
    public long readDashBoardRunningTimeForService() {
        return sp.getLong(PREF_DASHBOARD_SERVICE_RUNNING_TIME, 0L);
    }

    public String readComplianceTubeCalibration() {
        return sp.getString(PREF_TUBE_COMPLIANCE_CALIBRATION, "");
    }

    public void setComplianceTubeCalibrationStatus(Boolean value) {
        updateLimitState(PREF_TUBE_COMPLIANCE_CALIBRATION_STATUS, value);
    }

    public Long readOperatingHours() {
        return sp.getLong(PREF_OPERATING_HOURS, 0L);
    }

    public void setOperatingHours(Long value) {
        updateData(PREF_OPERATING_HOURS, value);
    }

    public Boolean readComplianceTubeCalibrationStatus() {
        return sp.getBoolean(PREF_TUBE_COMPLIANCE_CALIBRATION_STATUS, false);
    }


    public void setConcentratorStatus(Boolean value) {
        updateLimitState(PREF_CONCENTRATOR_STATUS, value);
    }

    public Boolean readConcentratorStatus() {
        return sp.getBoolean(PREF_CONCENTRATOR_STATUS, true);
    }


    public void saveVentiDetails(String value) {
        updateData(PREF_VENTI_DETAILS, value);
    }

    public String readVentiDetails() {
        return sp.getString(PREF_VENTI_DETAILS, "123,AIIMS,SOFTWARE");
    }

    public void setNebuliserCheck(Boolean value) {
        updateLimitState(PREF_NO_O2_NO_NEB, value);
    }

    public Boolean readNebuliserCheck() {
        return sp.getBoolean(PREF_NO_O2_NO_NEB, true);
    }


    // added pref for tube resistance compliance
    public void setResistanceTubeCalibration(String value) {

        if (value != null) {
            updateData(PREF_TUBE_RESISTANCE_CALIBRATION, value);
        }
    }

    public String readResistanceTubeCalibration() {
        return sp.getString(PREF_TUBE_RESISTANCE_CALIBRATION, "");
    }

    public void setResistanceTubeCalibrationStatus(Boolean value) {
        updateLimitState(PREF_TUBE_RESISTANCE_CALIBRATION_STATUS, value);
    }

    public Boolean readResistanceTubeCalibrationStatus() {
        return sp.getBoolean(PREF_TUBE_RESISTANCE_CALIBRATION_STATUS, false);
    }


    public boolean readExpandedAlarmVisility() {
        return sp.getBoolean(PREF_IS_EXPANDED_ALARM_VISIBLE, false);
    }

    public void setExpandedAlarmVisility(boolean status) {
        updateData(PREF_IS_EXPANDED_ALARM_VISIBLE, status);
    }

    public boolean readPressureGraphvisility() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_GRAPH_PRESSURE, true);
    }

    public void setPressureGraphvisility(boolean status) {
        updateData(readCurrentUid() + "." + PREF_GRAPH_PRESSURE, status);
    }

    public boolean readVolumeGraphvisility() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_GRAPH_VOLUME, false);
    }

    public void setVolumeGraphvisility(boolean status) {
        updateData(readCurrentUid() + "." + PREF_GRAPH_VOLUME, status);
    }

    public boolean readFlowGraphvisility() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_GRAPH_FLOW, false);
    }

    public void setFlowGraphvisility(boolean status) {
        updateData(readCurrentUid() + "." + PREF_GRAPH_FLOW, status);
    }


    public boolean readPresureFlowGraphvisility() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_GRAPH_PRESSURE_FLOW, true);
    }

    public void setPresureFlowGraphvisility(boolean status) {
        updateData(readCurrentUid() + "." + PREF_GRAPH_PRESSURE_FLOW, status);
    }

    public boolean readServiceStatus() {
        return sp.getBoolean(BLE_SERVICE_STATUS, false);
    }

    public void setServiceStatus(boolean status) {
        updateData(BLE_SERVICE_STATUS, status);
    }

    public boolean readNeoNateActiveStatus() {
        return sp.getBoolean(PREF_NEO_ACTIVE_STATUS, false);
    }

    public void setNeoNateActiveStatus(boolean status) {
        updateData(PREF_NEO_ACTIVE_STATUS, status);
    }

    // set temporary for standby control
    public Boolean readApneaSettingsStatusTemp() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_APNEA_SETTINGS_STATUS_TEMP, false);
    }

    public void setApneaSettingsStatusTemp(boolean isActive) {
        updateData(readCurrentUid() + "." + PREF_APNEA_SETTINGS_STATUS_TEMP, isActive);
    }

    public boolean readSmartFiO2StatusTemp() {
        return sp.getBoolean(PREF_SMART_FIO2_STATUS_TEMP, false);
    }

    public void setSmartFiO2StatusTemp(boolean isActive) {
        updateData(PREF_SMART_FIO2_STATUS_TEMP, isActive);
    }

    public boolean readIRVStatusTemp() {
        return sp.getBoolean(PREF_IRV_STATUS_TEMP, false);
    }

    public void setIRVStatusTemp(boolean isActive) {
        updateData(PREF_IRV_STATUS_TEMP, isActive);
    }

    public boolean readVGVStatusTemp() {
        return sp.getBoolean(PREF_VGV_STATUS_TEMP, false);
    }

    public void setVGVStatusTemp(boolean isActive) {
        updateData(PREF_VGV_STATUS_TEMP, isActive);
    }

    public boolean readEtCuffStatusTemp() {
        return sp.getBoolean(PREF_EtCuff_STATUS_TEMP, false);
    }

    public void setEtCuffStatusTemp(boolean isActive) {
        updateData(PREF_EtCuff_STATUS_TEMP, isActive);
    }

    public boolean readDeflashedStatusTemp() {
        return sp.getBoolean(PREF_Deflashed_STATUS_TEMP, false);
    }

    public void setDeflashedStatusTemp(boolean isActive) {
        updateData(PREF_Deflashed_STATUS_TEMP, isActive);
    }

    public boolean readOxygenLevelStatus() {
        return sp.getBoolean(PREF_OXYGEN_LEVEL, false);
//        return (readFiO2().intValue() > Configs.THRESHOLD_OXYGEN_VARIATION_VALUE);
    }

    private void setOxygenLevelStatus(boolean isHigh) {
        updateData(PREF_OXYGEN_LEVEL, isHigh);
    }

    public void setCurrentUid(PatientProfile uid) {
        updateData(PREF_CURRENT_UID, uid.toString());
    }

    public void setSelectedOptions(Configs.SELECTED_OPTIONS selectedOptions) {
        updateData(PREF_SELECTED_OPTION, selectedOptions.toString());
    }

    public Configs.SELECTED_OPTIONS readSelectedOptions() {
        try {
            return Configs.SELECTED_OPTIONS.valueOf(sp.getString(PREF_SELECTED_OPTION, String.valueOf(Configs.SELECTED_OPTIONS.INVASIVE_NAME)));

        } catch (Exception e) {
            e.printStackTrace();
            return Configs.SELECTED_OPTIONS.INVASIVE_NAME;
        }
    }

    public void setGraphParentType(parentType parentType) {
        updateData(PREF_GRAPH_PARENT_TYPE, parentType.toString());
    }

    public void setLastUid(PatientProfile uid) {
        updateData(PREF_LAST_UID, uid.toString());
    }

    public Configs.ModeType readModeType() {
        try {
            return Configs.ModeType.valueOf(sp.getString(PREF_MODE_TYPE, TYPE_Pressure.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return TYPE_Pressure;
        }
    }


    public void setModeType(Configs.ModeType modeType) {
        updateData(PREF_MODE_TYPE, modeType.toString());
    }

    public PatientProfile readCurrentUid() {
        try {
            return PatientProfile.valueOf(sp.getString(PREF_CURRENT_UID, TYPE_ADULT.toString()));

        } catch (Exception e) {
            e.printStackTrace();
            return TYPE_ADULT;
        }
    }

    public VentilatorType readVentilatorType() {
        try {
            return VentilatorType.valueOf(sp.getString(PREF_VENTI_TYPE_STATUS, VentilatorType.ATN.toString()));

        } catch (Exception e) {
            e.printStackTrace();
            return VentilatorType.ATN;
        }
    }

    public void setVentilatorType(VentilatorType ventilatorType) {
        updateData(PREF_VENTI_TYPE_STATUS, ventilatorType.toString());
    }

    public void setTrendDuration(String val) {
        updateData(PREF_TREND_DURATION, val);
    }

    public String readTrendDuration() {
        return sp.getString(PREF_TREND_DURATION, "10 Min");
    }

    public parentType readGraphParentType() {
        try {
            return parentType.valueOf(sp.getString(PREF_GRAPH_PARENT_TYPE, parentType.TrioFragmentGraph.toString()));

        } catch (Exception e) {
            e.printStackTrace();
            return parentType.TrioFragmentGraph;
        }
    }

    public PatientProfile readLastUid() {
        try {
            return PatientProfile.valueOf(sp.getString(PREF_LAST_UID, TYPE_ADULT.toString()));

        } catch (Exception e) {
            e.printStackTrace();
            return TYPE_ADULT;
        }
    }

    public String readVentilatorSoftwareVersion() {
        return sp.getString(PREF_VENTILATOR_SOFTWARE_VERSION, "");
    }

    public void setVentilatorSoftwareVersion(String version) {
        updateData(PREF_VENTILATOR_SOFTWARE_VERSION, version);
    }

    public int readVentilationMode() {
        return sp.getInt(readCurrentUid() + "." + PREF_VENTILATION_MODE, -1);
    }

    public void setVentilationMode(int val) {
        updateData(readCurrentUid() + "." + PREF_VENTILATION_MODE, val);
    }

    public int readStartupCount() {
        return sp.getInt(readCurrentUid() + "." + PREF_STARTUP_COUNT, 0);
    }

    public void setCount(int val) {
        updateData(readCurrentUid() + "." + PREF_STARTUP_COUNT, val);
    }

    public int readLastVentMode() {
        return (sp.getInt(readCurrentUid() + "." + PREF_LAST_USED_VENT_MODE, -1));
    }

    public void setLastVentMode(int val) {
        updateData(readCurrentUid() + "." + PREF_LAST_USED_VENT_MODE, val);
    }


    public void setControlParams(String dataList) {
        updateData(PREF_CONTROL_PARAMS, dataList);
    }

    public String readControlParams() {
        return sp.getString(PREF_CONTROL_PARAMS, "default value");
    }

    public Float readSupportPinsp() {
        return sp.getFloat(readCurrentUid() + "." + PREF_SUPPORT_INSP_PRESSURE, Float.parseFloat(context.getString(R.string.default_ipap)));
    }

    public void setSupportPinsp(Float val) {
        updateData(readCurrentUid() + "." + PREF_SUPPORT_INSP_PRESSURE, val);
    }

    public Float readSupportPexp() {
        return sp.getFloat(readCurrentUid() + "." + PREF_SUPPORT_EXP_PRESSURE, Float.parseFloat(context.getString(R.string.default_epap)));
    }

    public void setLeakFactor(Integer val) {
        updateData(readCurrentUid() + "." + PREF_LEAK_FACTOR, val);
    }

    public Integer readLeakFactor() {
        return sp.getInt(readCurrentUid() + "." + PREF_LEAK_FACTOR, Integer.parseInt(context.getString(R.string.default_leak_factor)));
    }

    public void setSupportPexp(Float val) {
        updateData(readCurrentUid() + "." + PREF_SUPPORT_EXP_PRESSURE, val);
    }

    public void setUHID(String UHID) {
        updateData(readCurrentUid() + "." + PREF_UHID, UHID);
    }

    //ToDo:-Current Task UHID
    public String readUHID() {
        String value = sp.getString(readCurrentUid() + "." + PREF_UHID, FIRST_FILTER_NAME);
        if (value.equals("")) return FIRST_FILTER_NAME;
        return value;
    }


   /* public int readFlowLimits() {
        int min;
        switch (readCurrentUid()) {
            case TYPE_ADULT:
                min = 20;
                break;
            case TYPE_PED:
                min = 40;
                break;
            case TYPE_NEONAT:
                min = 20;
                break;
            default :
                min = 20;
        }
        return sp.getInt(readCurrentUid() + "." + min);
    }
*/


    public Float readBodyWeight() {
        @StringRes int defaultvalue;
        switch (readCurrentUid()) {
            case TYPE_ADULT:
                defaultvalue = R.string.default_adult_body_weight;
                break;

            case TYPE_PED:
                defaultvalue = R.string.default_ped_body_weight;
                break;

            case TYPE_NEONAT:
                defaultvalue = R.string.default_neo_body_weight;
                break;

            default:
                defaultvalue = R.string.default_adult_body_weight;
                break;
        }
        return sp.getFloat(readCurrentUid() + "." + PREF_BODY_WEIGHT, Float.parseFloat(context.getString(defaultvalue)));
    }

    public void setBodyWeight(Float val) {
        updateData(readCurrentUid() + "." + PREF_BODY_WEIGHT, val);
    }

    public Float readBodyHeight() {
        @StringRes int defaultvalue;
        switch (readCurrentUid()) {
            case TYPE_ADULT:
                defaultvalue = R.string.default_adult_body_height;
                break;

            case TYPE_PED:
                defaultvalue = R.string.default_ped_body_height;
                break;

            case TYPE_NEONAT:
                defaultvalue = R.string.default_neo_body_height;
                break;

            default:
                defaultvalue = R.string.default_adult_body_height;
                break;
        }
        return sp.getFloat(readCurrentUid() + "." + PREF_BODY_HEIGHT, Float.parseFloat(context.getString(defaultvalue)));
    }

    public void setBodyHeight(Float val) {
        updateData(readCurrentUid() + "." + PREF_BODY_HEIGHT, val);
    }

    public Float readAge() {
        @StringRes int defaultvalue;
        switch (readCurrentUid()) {
            case TYPE_ADULT:
                defaultvalue = R.string.default_adult_age;
                break;

            case TYPE_PED:
                defaultvalue = R.string.default_ped_age;
                break;

            case TYPE_NEONAT:
                defaultvalue = R.string.default_neo_age;
                break;

            default:
                defaultvalue = R.string.default_adult_age;
                break;
        }
        return sp.getFloat(readCurrentUid() + "." + PREF_AGE, Float.parseFloat(context.getString(defaultvalue)));
    }

    public void setAge(Float val) {
        updateData(readCurrentUid() + "." + PREF_AGE, val);
    }


    public void setVolume(Float val) {
        updateData(PREF_VOLUME, val);
    }

    public Float readVolume() {
        return sp.getFloat(PREF_VOLUME, Float.parseFloat(context.getString(R.string.default_volume)));
    }

    public void setTubeDia(Float val) {
        updateData(PREF_TUBE_DIA, val);
    }

    public Float readTubeDia() {
        return sp.getFloat(PREF_TUBE_DIA, Float.parseFloat(context.getString(R.string.default_tube_dia)));
    }

    public Float readNebuliserTime() {
        return sp.getFloat(PREF_NEBULISER, Float.parseFloat(context.getString(R.string.default_nebuliser)));
    }

    public void setNebuliserTime(Float val) {
        updateData(PREF_NEBULISER, val);
    }

    public String readDischargeDateTime() {
        return sp.getString(readCurrentUid() + "." + PREF_DISCHARGE_DATE, "-");

    }

    public void setDischargeDateTime() {
        String dischargeDate = AppUtils.getCurrentDateTime();
        updateData(PREF_DISCHARGE_DATE, dischargeDate);


    }

    public Gender readGender() {
        try {
            return Gender.valueOf(sp.getString(readCurrentUid() + "." + PREF_GENDER, TYPE_MALE.toString()));
        } catch (Exception e) {
            e.printStackTrace();
            return TYPE_MALE;
        }
    }

    public void setGender(Gender gender) {
        updateData(readCurrentUid() + "." + PREF_GENDER, gender.toString());
    }

    public Float readPip() {
        Float pipValue = 0.0f;
        switch (readCurrentUid()) {
            case TYPE_PED:
                pipValue = Float.parseFloat(context.getString(R.string.default_pip));
                break;
            case TYPE_ADULT:
                pipValue = Float.parseFloat(context.getString(R.string.default_pip));
                break;
            case TYPE_NEONAT:
                pipValue = Float.parseFloat(context.getString(R.string.default_pip_neonate));
                break;
        }
        return sp.getFloat(readCurrentUid() + "." + PREF_PIP, pipValue);
    }

    public void setPip(Float val) {
        updateData(readCurrentUid() + "." + PREF_PIP, val);
        //  setPipLimits(readPipLimits()[0], val);   // set the upper limit of the alarm
    }

    public Float readPeakFlow() {
        return sp.getFloat(readCurrentUid() + "." + PREF_PEAK_FLOW, Float.parseFloat(context.getString(R.string.default_peakflow)));
    }

    public void setPeakFlow(Float val) {
        updateData(readCurrentUid() + "." + PREF_PEAK_FLOW, val);
    }


    public Float readPHigh() {
        return sp.getFloat(readCurrentUid() + "." + PREF_PHigh, Float.parseFloat(context.getString(R.string.default_PHigh)));
    }

    public void setPHigh(Float val) {
        updateData(readCurrentUid() + "." + PREF_PHigh, val);
    }

    public Float readPLow() {
        return sp.getFloat(readCurrentUid() + "." + PREF_PLow, Float.parseFloat(context.getString(R.string.default_PLow)));
    }

    public void setPLow(Float val) {
        updateData(readCurrentUid() + "." + PREF_PLow, val);
    }

    public Float readTHigh() {
        return sp.getFloat(readCurrentUid() + "." + PREF_THigh, Float.parseFloat(context.getString(R.string.default_THigh)));
    }

    public void setTHigh(Float val) {
        updateData(readCurrentUid() + "." + PREF_THigh, val);
    }

    public Float readTLow() {
        return sp.getFloat(readCurrentUid() + "." + PREF_TLOW, Float.parseFloat(context.getString(R.string.default_TLow)));
    }

    public void setTLow(Float val) {
        updateData(readCurrentUid() + "." + PREF_TLOW, val);
    }


    public Float readPEEP() {

        Float peepValue = 0.0f;

        switch (readCurrentUid()) {
            case TYPE_ADULT:
                peepValue = Float.parseFloat(context.getString(R.string.default_peep));
                break;

            case TYPE_PED:
                peepValue = Float.parseFloat(context.getString(R.string.default_peep));
                break;

            case TYPE_NEONAT:
                peepValue = Float.parseFloat(context.getString(R.string.default_peep_neo));
                break;
        }

        return sp.getFloat(readCurrentUid() + "." + PREF_PEEP, peepValue);
    }

    public void setPEEP(Float val) {
        updateData(readCurrentUid() + "." + PREF_PEEP, val);
    }

    public Boolean readAutoFlow() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_AUTO_FLOW, true);
    }

    public void setAutoFlow(Boolean isActive) {
        updateData(readCurrentUid() + "." + PREF_AUTO_FLOW, isActive);
    }


    public Float readRR() {

        Float rrValue = 0.0f;

        switch (readCurrentUid()) {
            case TYPE_PED:
                rrValue = Float.parseFloat(context.getString(R.string.default_rr));
                break;

            case TYPE_ADULT:
                rrValue = Float.parseFloat(context.getString(R.string.default_rr));
                break;
            case TYPE_NEONAT:
                rrValue = Float.parseFloat(context.getString(R.string.default_rr_neo));
                break;
        }

//        Float rrValue = Float.parseFloat(context.getString(R.string.default_rr));

        return sp.getFloat(readCurrentUid() + "." + PREF_RR, rrValue);
    }

    public void setRR(Float val) {
        updateData(readCurrentUid() + "." + PREF_RR, val);
    }

    public void setPipViaVGV(Integer val) {
        updateData(readCurrentUid() + "." + PREF_PIP_VIA_VGV, val);
    }

    public Integer readPipViaVGV() {
        return sp.getInt(readCurrentUid() + "." + PREF_PIP_VIA_VGV, Integer.parseInt(context.getString(R.string.default_pip_via_vgv)));
    }


    public Float readVti() {

        int resVti = 0;

        switch (readCurrentUid()) {

            case TYPE_ADULT:
                resVti = R.string.default_vti;
                break;
            case TYPE_PED:
                resVti = R.string.default_vti_ped;
                break;

            case TYPE_NEONAT:
                resVti = R.string.default_vti_neo;
                break;

        }

        //int resVti = readPediatricStatus() ? R.string.default_vti_ped : R.string.default_vti;
        return sp.getFloat(readCurrentUid() + "." + PREF_TIDAL_VOLUME, Float.parseFloat(context.getString(resVti)));
    }

    public void setVti(Float val) {
        updateData(readCurrentUid() + "." + PREF_TIDAL_VOLUME, val);
    }

    public Float readTrigFlow() {


        Float readTrigFlow = 0.0f;

        switch (readCurrentUid()) {

            case TYPE_ADULT:
                readTrigFlow = Float.parseFloat(context.getString(R.string.default_trigflow_adult));
                break;
            case TYPE_PED:
                readTrigFlow = Float.parseFloat(context.getString(R.string.default_trigflow));
                break;

            case TYPE_NEONAT:
                readTrigFlow = Float.parseFloat(context.getString(R.string.default_trigflow_neo));
                break;
        }

        return sp.getFloat(readCurrentUid() + "." + PREF_TRIG_FLOW, readTrigFlow);
    }

    public void setTrigFlow(Float val) {
        updateData(readCurrentUid() + "." + PREF_TRIG_FLOW, val);
    }

    public Float readTlow() {
        return sp.getFloat(readCurrentUid() + "." + PREF_TLOW, Float.parseFloat(context.getString(R.string.default_tlow)));
    }

    public void setTlow(Float val) {
        updateData(readCurrentUid() + "." + PREF_TLOW, val);
    }

    public Float readTexp() {

        return sp.getFloat(readCurrentUid() + "." + PREF_TEXP, Float.parseFloat(context.getString(R.string.default_texp)));
    }

    public void setTexp(Float val) {
        updateData(readCurrentUid() + "." + PREF_TEXP, val);
    }


    public Float readPplat() {


        Float readPplat = 0.0f;

        switch (readCurrentUid()) {

            case TYPE_ADULT:
                readPplat = Float.parseFloat(context.getString(R.string.default_pplat));
                break;
            case TYPE_PED:
                readPplat = Float.parseFloat(context.getString(R.string.default_pplat_ped));
                break;

            case TYPE_NEONAT:
                readPplat = Float.parseFloat(context.getString(R.string.default_pplat_neonate));
                break;
        }

        return sp.getFloat(readCurrentUid() + "." + PREF_PLATEAU_PRESSURE, readPplat);
    }

    public void setPplat(Float val) {
        updateData(readCurrentUid() + "." + PREF_PLATEAU_PRESSURE, val);
    }

    public Float readTinsp() {
        Float readTinsp = 0.0f;

        switch (readCurrentUid()) {

            case TYPE_ADULT:
                readTinsp = Float.parseFloat(context.getString(R.string.default_inhale_time));
                break;
            case TYPE_PED:
                readTinsp = Float.parseFloat(context.getString(R.string.default_inhale_time));
                break;

            case TYPE_NEONAT:
                readTinsp = Float.parseFloat(context.getString(R.string.default_inhale_time_neonate));
                break;
        }
        return sp.getFloat(readCurrentUid() + "." + PREF_INSP_TIME, readTinsp);
    }

    public void setTinsp(Float val) {
        updateData(readCurrentUid() + "." + PREF_INSP_TIME, val);
    }

    public Float readTubeResistance() {
        return sp.getFloat(readCurrentUid() + "." + PREF_TUBE_RESISTANCE, 1.23f);
    }

    public void setTubeResistance(Float val) {
        updateData(readCurrentUid() + "." + PREF_TUBE_RESISTANCE, val);
    }

    public Float readTubeCompensation() {
        return sp.getFloat(readCurrentUid() + "." + PREF_TUBE_COMPLIANCE, 12.33f);
    }

    public void setPrefTubeCompliance(Float val) {
        updateData(readCurrentUid() + "." + PREF_TUBE_COMPLIANCE, val);
    }

    public Float readSupportPressure() {
        Float readSupportPressure = 0.0f;

        switch (readCurrentUid()) {

            case TYPE_ADULT:
                readSupportPressure = Float.parseFloat(context.getString(R.string.default_support_pressure));
                break;
            case TYPE_PED:
                readSupportPressure = Float.parseFloat(context.getString(R.string.default_support_pressure));
                break;

            case TYPE_NEONAT:
                readSupportPressure = Float.parseFloat(context.getString(R.string.default_support_pressure_neonate));
                break;
        }

        return sp.getFloat(readCurrentUid() + "." + PREF_SUPPORT_PRESSURE, readSupportPressure);
    }

    public void setSupportPressure(Float val) {
        updateData(readCurrentUid() + "." + PREF_SUPPORT_PRESSURE, val);
    }

    public Float readSlope() {
        return sp.getFloat(readCurrentUid() + "." + PREF_SLOPE, Float.parseFloat(context.getString(R.string.default_slope)));
    }

    public void setSlope(Float val) {
        updateData(readCurrentUid() + "." + PREF_SLOPE, val);
    }

    public Float readInspiratoryPause() {
        return sp.getFloat(readCurrentUid() + "." + PREF_INSP_PAUSE, Float.parseFloat(context.getString(R.string.default_inspiratory_pause)));
    }

    public void setInspiratoryPause(Float val) {
        updateData(readCurrentUid() + "." + PREF_INSP_PAUSE, val);
    }

    public Float readPeepValve() {
        return sp.getFloat(readCurrentUid() + "." + PREF_PEEP_VALVE, Float.parseFloat(context.getString(R.string.default_peep_valve)));
    }

    public void setPeepValve(Float val) {
        updateData(readCurrentUid() + "." + PREF_PEEP_VALVE, val);
    }

    public Float readSpontVT() {
        return sp.getFloat(readCurrentUid() + "." + PREF_SPONT_VT, Float.parseFloat(context.getString(R.string.default_peep_valve)));
    }

    public void setSpontVT(Float val) {
        updateData(readCurrentUid() + "." + PREF_SPONT_VT, val);
    }


    public Float readHrLimit() {
        return sp.getFloat(readCurrentUid() + "." + PREF_HR_LIMIT, Float.parseFloat(context.getString(R.string.default_hr_limit)));
    }

    public void setHrLimit(Float val) {
        updateData(readCurrentUid() + "." + PREF_HR_LIMIT, val);
    }

    public Float readTargetSpo2() {
        return sp.getFloat(readCurrentUid() + "." + PREF_TARGET_SPO2, Float.parseFloat(context.getString(R.string.default_target_spo2)));
    }

    public void setTargetSpo2(Float val) {
        updateData(readCurrentUid() + "." + PREF_TARGET_SPO2, val);
    }

    public Float readTargetVolume() {
        @StringRes int defaultvalue;
        switch (readCurrentUid()) {
            case TYPE_ADULT:
                defaultvalue = R.string.adult_targetVolume_default;
                break;
            case TYPE_PED:
                defaultvalue = R.string.ped_targetVolume_default;
                break;
            case TYPE_NEONAT:
                defaultvalue = R.string.neo_targetVolume_default;
                break;

            default:
                defaultvalue = R.string.neo_targetVolume_default;
                break;

        }
        //        return sp.getFloat(readCurrentUid() + "." + PREF_TARGET_VOLUME,Float.parseFloat(context.getString(R.string.default_target_volume)));
        return sp.getFloat(readCurrentUid() + "." + PREF_TARGET_VOLUME, Float.parseFloat(context.getString(defaultvalue)));
    }

    public void setTargetVolume(Float val) {
        updateData(readCurrentUid() + "." + PREF_TARGET_VOLUME, val);
    }

    public Float readFrequency() {
        return sp.getFloat(readCurrentUid() + "." + PREF_FREQUENCY, Float.parseFloat(context.getString(R.string.default_flow)));
    }

    public void setFrequency(Float val) {
        updateData(readCurrentUid() + "." + PREF_FREQUENCY, val);
    }


    public Float readFlow() {
        return sp.getFloat(readCurrentUid() + "." + PREF_FLOW, Float.parseFloat(context.getString(R.string.default_flow)));
    }

    public void setFlow(Float val) {
        updateData(readCurrentUid() + "." + PREF_FLOW, val);
    }

    public Float readFiO2Dev() {
        return sp.getFloat(readCurrentUid() + "." + PREF_FIO2_DEV, Float.parseFloat(context.getString(R.string.default_fio2_dev)));
    }

    public void setFiO2Dev(Float val) {
        updateData(readCurrentUid() + "." + PREF_FIO2_DEV, val);
    }


    public Float readFiO2() {

        final boolean isMidRangeFiO2REquired = getModeCategory(readVentilationMode()) == MODE_NIV;  // For NIV modes and SPONT
        return sp.getFloat(readCurrentUid() + "." + PREF_FIO2, Float.parseFloat(context.getString(isMidRangeFiO2REquired ? R.string.default_fio2_niv : R.string.default_fio2)));
    }

    public void setFiO2(Float val) {
        Log.i("FIO2_VALUE", String.valueOf(val));
        updateData(readCurrentUid() + "." + PREF_FIO2, val);
    }

    public Boolean readProngStatus() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_RADIO_PRONG, false);
    }

    public void setProngStatus(boolean isActive) {
        updateData(readCurrentUid() + "." + PREF_RADIO_PRONG, isActive);
    }


    public Boolean readWorkerSuccessStatus() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_WORK_SUCCESS, false);
    }

    public void setWorkerSuccessStatus(boolean success) {
        updateData(readCurrentUid() + "." + PREF_WORK_SUCCESS, success);
    }

    public Boolean readKnobStatus() {
        return sp.getBoolean(PREF_KNOB_STATUS, true);
    }

    public void setKnobStatus(boolean isActive) {
        updateData(PREF_KNOB_STATUS, isActive);
    }

    public Boolean readMaskStatus() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_RADIO_MASK, false);
    }

    public void setMaskStatus(boolean isActive) {
        updateData(readCurrentUid() + "." + PREF_RADIO_MASK, isActive);
    }

    public Boolean readInvasiveStatus() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_RADIO_INV, false);
    }

    public void setInvasiveStatus(boolean isActive) {
        updateData(readCurrentUid() + "." + PREF_RADIO_INV, isActive);
    }


    public Boolean readTubeDiaStatus() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_TUBE_DIA_STATUS, false);
    }

    public void setTubeDiaStatus(boolean isActive) {
        updateData(readCurrentUid() + "." + PREF_TUBE_DIA_STATUS, isActive);
    }

    public Boolean readApneaSettingsStatus() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_APNEA_SETTINGS_STATUS, false);
    }

    public void setApneaSettingsStatus(boolean isActive) {
        updateData(readCurrentUid() + "." + PREF_APNEA_SETTINGS_STATUS, isActive);
    }

    public Integer readAlarmSize(String uhid) {
        return sp.getInt(readCurrentUid() + ".alarmSize" + uhid, 20);
    }

    public void setAlarmSize(Integer value, String uhid) {
        updateData(readCurrentUid() + ".alarmSize" + uhid, value);
    }

    public Integer readAlarmPage(String uhid) {
        return sp.getInt(readCurrentUid() + ".alarmPage" + uhid, 1);
    }

    public void setAlarmPage(Integer value, String uhid) {
        updateData(readCurrentUid() + ".alarmPage" + uhid, value);
    }


    public Integer readEventSize(String uhid) {
        return sp.getInt(readCurrentUid() + ".eventSize" + uhid, 11);
    }

    public void setEventSize(Integer value, String uhid) {
        updateData(readCurrentUid() + ".eventSize" + uhid, value);
    }

    public Integer readEventPage(String uhid) {
        return sp.getInt(readCurrentUid() + ".eventPage" + uhid, 1);
    }

    public void setEventPage(Integer value, String uhid) {
        updateData(readCurrentUid() + ".eventPage" + uhid, value);
    }


    // added smartFio2
    public Boolean readSmartFio2SettingsStatus() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_SMART_FIO2_SETTINGS_STATUS, false);
    }

    // added smartFio2
    public void setSmartFio2SettingsStatus(boolean isActive) {
        updateData(readCurrentUid() + "." + PREF_SMART_FIO2_SETTINGS_STATUS, isActive);
    }

    public Boolean readTempVtasStatus() {
        return sp.getBoolean(readCurrentUid() + "." + PREF_TEMP_VTAS_STATUS, false);
    }

    // added temp Vtas value
    public void setTempVtasStatus(boolean isActive) {
        updateData(readCurrentUid() + "." + PREF_TEMP_VTAS_STATUS, isActive);
    }


    public Float readRRApnea() {

        Float rrApneaValue = 0.0f;

        switch (readCurrentUid()) {
            case TYPE_PED:
                rrApneaValue = Float.parseFloat(context.getString(R.string.default_apnea_rr_ped));
                break;

            case TYPE_ADULT:
                rrApneaValue = Float.parseFloat(context.getString(R.string.default_apnea_rr));
                break;
            case TYPE_NEONAT:
                rrApneaValue = Float.parseFloat(context.getString(R.string.default_apnea_rr_neo));
                break;
        }


        return sp.getFloat(readCurrentUid() + "." + PREF_APNEA_RR, rrApneaValue);
    }

    public void setRRApnea(Float val) {

        updateData(readCurrentUid() + "." + PREF_APNEA_RR, val);

    }

    public Float readTApnea() {

        Float tApneaValue = 0.0f;

        switch (readCurrentUid()) {
            case TYPE_PED:
                tApneaValue = Float.parseFloat(context.getString(R.string.default_apnea_time));
                break;
            case TYPE_ADULT:
                tApneaValue = Float.parseFloat(context.getString(R.string.default_apnea_time));
                break;
            case TYPE_NEONAT:
                tApneaValue = Float.parseFloat(context.getString(R.string.default_apnea_time));
                break;
        }

        return sp.getFloat(readCurrentUid() + "." + PREF_APNEA_TIME, tApneaValue);
    }

    public void setTApnea(Float val) {

        updateData(readCurrentUid() + "." + PREF_APNEA_TIME, val);
    }


    public Float readVtApnea() {

        Float vtApneaValue = 0.0f;

        switch (readCurrentUid()) {
            case TYPE_PED:
                vtApneaValue = Float.parseFloat(context.getString(R.string.default_apnea_vt_ped));
                break;

            case TYPE_ADULT:
                vtApneaValue = Float.parseFloat(context.getString(R.string.default_apnea_vt));
                break;
            case TYPE_NEONAT:
                vtApneaValue = Float.parseFloat(context.getString(R.string.default_apnea_vt_neo));
                break;
        }
        return sp.getFloat(readCurrentUid() + "." + PREF_APNEA_TIDAL_VOLUME, vtApneaValue);
    }

    public void setVtApnea(Float val) {
        updateData(readCurrentUid() + "." + PREF_APNEA_TIDAL_VOLUME, val);
    }

    public Float readTrigFlowApnea() {
        return sp.getFloat(readCurrentUid() + "." + PREF_APNEA_TRIG_FLOW, Float.parseFloat(context.getString(R.string.default_apnea_trigflow)));
    }

    public void setTrigFlowApnea(Float val) {
        updateData(readCurrentUid() + "." + PREF_APNEA_TRIG_FLOW, val);
    }

    public Float readEtPressure() {
        return sp.getFloat(readCurrentUid() + "." + PREF_Et_PRESSURE, Float.parseFloat(context.getString(R.string.default_et_pressure)));
    }

    public void setEtPressure(Float val) {
        updateData(readCurrentUid() + "." + PREF_Et_PRESSURE, val);
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        updateData(PREF_IS_LOGGED_IN, isLoggedIn);
    }

    public boolean readIsLoggedIn() {
        return sp.getBoolean(PREF_IS_LOGGED_IN, false);
    }

//    public void setDeepSleepStatus(boolean isDeepSleep) {
//        Log.i("SLEEP_CHECK", "set deep sleep status : " + String.valueOf(isDeepSleep));
//        updateData(PREF_IS_DEEP_SLEEP, isDeepSleep);
//    }

//    public boolean readDeepSleepStatus() {
//        return sp.getBoolean(PREF_IS_DEEP_SLEEP, false);
//    }

    public void setPatientProfile(String json) {
        updateData(PREF_PATIENT_PROFILE, json);
    }

    public String readPatientProfile() {
        return sp.getString(PREF_PATIENT_PROFILE, null);
    }


    //maneuvers limits

    public void setManeuversPplatLimits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_MANEUVERS_MIN_MAX, min, max);
    }

    public Float[] readManeuversPplatLimits() {
        if (readCurrentUid() == TYPE_NEONAT)
            return readLimits(readCurrentUid() + "." + PREF_MANEUVERS_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_manuvers_limit_neo)), Float.valueOf(context.getString(R.string.default_max_manuvers_limit_neo)));
        else
            return readLimits(readCurrentUid() + "." + PREF_MANEUVERS_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_manuvers_limit)), Float.valueOf(context.getString(R.string.default_max_manuvers_limit)));
    }

    public Float readManeuversPplatValue() {
        return sp.getFloat(readCurrentUid() + "." + PREF_MANEUVERS_PLATEAU, Float.valueOf(context.getString(R.string.default_maneuvers)));
    }

    public void setManeuversPplatValue(Float plateau) {
        updateData(readCurrentUid() + "." + PREF_MANEUVERS_PLATEAU, plateau);
    }

    public Float readManeuversStaticComplianceValue() {
        return sp.getFloat(readCurrentUid() + "." + PREF_MANEUVERS_STATIC_COMPLINES, Float.valueOf(context.getString(R.string.default_maneuvers)));
    }

    public void setManeuversStaticComplianceValue(Float complines) {
        updateData(readCurrentUid() + "." + PREF_MANEUVERS_STATIC_COMPLINES, complines);
    }


    public Float readManeuversAutoPeepValue() {
        return sp.getFloat(readCurrentUid() + "." + PREF_MANEUVERS_PEEP, Float.valueOf(context.getString(R.string.default_maneuvers)));
    }

    public void setManeuversAutoPeepValue(Float peepManeuversValue) {
        updateData(readCurrentUid() + "." + PREF_MANEUVERS_PEEP, peepManeuversValue);
    }

    public String readExpiratoryDate() {
        return sp.getString(readCurrentUid() + "." + PREF_EXPIRATORY_DATE, "-");
    }

    public void setExpiratoryDate(String expiratoryDate) {
        updateData(readCurrentUid() + "." + PREF_EXPIRATORY_DATE, expiratoryDate);
    }


    public String readInspiratoryDate() {
        return sp.getString(readCurrentUid() + "." + PREF_INSPIRATORY_DATE, "-");
    }

    public void setInspiratoryDate(String inspiratoryDate) {
        updateData(readCurrentUid() + "." + PREF_INSPIRATORY_DATE, inspiratoryDate);
    }


    public String readMeasureTime() {
        return sp.getString(readCurrentUid() + "." + PREF_MEASURE_TIME, "");
    }

    public void setMeasureTime(String inspiratoryDate) {
        updateData(readCurrentUid() + "." + PREF_MEASURE_TIME, inspiratoryDate);
    }


    // Calibration of presser,flow , o2 , Turbine , flow


    public void setTurbineCalibration(SensorCalibration sensorCalibration) {
        // updateData(readCurrentUid() + "." + PREF_TURBINE_CALIBRATION, tightnessDate);

        if (sensorCalibration != null) {
            SharedPreferences.Editor editor = sp.edit();
            Gson gson = new Gson();
            String json = gson.toJson(sensorCalibration);
            editor.putString(PREF_TURBINE_CALIBRATION, json);
            editor.apply();
        }
    }


    public SensorCalibration readTurbineCalibration() throws JsonSyntaxException {
        Gson gson = new Gson();
        String json = sp.getString(PREF_TURBINE_CALIBRATION, "Not Calibrated");
        return gson.fromJson(json, SensorCalibration.class);
    }

    public boolean readTurbineCalibrationStatus() {
        try {
            return sp.getBoolean("", readTurbineCalibration().getStatus() == SENSOR_CALIBRATION_SUCCESS);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String readTurbineCalibrationDate() {
        try {
            return sp.getString("", readTurbineCalibration().getDate());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return "-";
    }

    public void setTubeComplianceCalibrationDate() {
        String data = AppUtils.getCurrentDateTime();
        updateData(PREF_TUBE_COMPLIANCE_DATE, data);
        Log.i("CHECK_DATE", data.toString());
    }

    public void setTubeComplianceCalibrationDate(String date) {
        updateData(PREF_TUBE_COMPLIANCE_DATE, date);
    }

    public void setTubeResistanceCalibrationDate() {
        String date = AppUtils.getCurrentDateTime();
        updateData(PREF_TUBE_RESISTANCE_DATE, date);
    }

    public void setTubeResistanceCalibrationDate(String date) {

        updateData(PREF_TUBE_RESISTANCE_DATE, date);
        ;
    }

    public String readTubeComplianceCalibrationDate() {
        return sp.getString(PREF_TUBE_COMPLIANCE_DATE, "-");

    }

    public String readTubeResistanceCalibrationDate() {
        return sp.getString(PREF_TUBE_RESISTANCE_DATE, "-");
    }

    public void setInspFlowCalibration(SensorCalibration sensorCalibration) {

        if (sensorCalibration != null) {
            SharedPreferences.Editor editor = sp.edit();
            Gson gson = new Gson();
            String json = gson.toJson(sensorCalibration);
            editor.putString(PREF_INSP_FLOW_SENSOR_CALIBRATION, json);
            editor.apply();
        }

    }

    public void setExpFlowCalibration(SensorCalibration sensorCalibration) {

        if (sensorCalibration != null) {
            SharedPreferences.Editor editor = sp.edit();
            Gson gson = new Gson();
            String json = gson.toJson(sensorCalibration);
            editor.putString(PREF_EXP_FLOW_SENSOR_CALIBRATION, json);
            editor.apply();
        }
    }

    // leak test pref
    public void setLeakTestCalibration(SensorCalibration sensorCalibration) {

        if (sensorCalibration != null) {
            SharedPreferences.Editor editor = sp.edit();
            Gson gson = new Gson();
            String json = gson.toJson(sensorCalibration);
            editor.putString(PREF_LEAK_TEST_CALIBRATION, json);
            editor.apply();
        }

    }

    public SensorCalibration readLeakTestCalibration() {

        Gson gson = new Gson();
        String json = sp.getString(PREF_LEAK_TEST_CALIBRATION, "Not Calibrated");
        return gson.fromJson(json, SensorCalibration.class);
    }

    public boolean readLeakTestCalibrationStatus() {
        try {
            return readLeakTestCalibration().getStatus() == SENSOR_CALIBRATION_SUCCESS;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String readLeakTestCalibrationDate() {
        try {
            return readLeakTestCalibration().getDate();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return "-";
    }


    public SensorCalibration readInspFlowSensorCalibration() {
        //return sp.getString(readCurrentUid() + "." + PREF_FLOW_SENSOR_CALIBRATION, "");

        Gson gson = new Gson();
        String json = sp.getString(PREF_INSP_FLOW_SENSOR_CALIBRATION, "Not Calibrated");
        return gson.fromJson(json, SensorCalibration.class);
    }

    public boolean readInspFlowCalibrationStatus() {
        try {
            return readInspFlowSensorCalibration().getStatus() == SENSOR_CALIBRATION_SUCCESS;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String readInspFlowCalibrationDate() {
        try {
            return readInspFlowSensorCalibration().getDate();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return "-";
    }

    public SensorCalibration readExpFlowSensorCalibration() {
        //return sp.getString(readCurrentUid() + "." + PREF_FLOW_SENSOR_CALIBRATION, "");

        Gson gson = new Gson();
        String json = sp.getString(PREF_EXP_FLOW_SENSOR_CALIBRATION, "Not Calibrated");
        return gson.fromJson(json, SensorCalibration.class);
    }

    public boolean readExpFlowCalibrationStatus() {
        try {
            return readExpFlowSensorCalibration().getStatus() == SENSOR_CALIBRATION_SUCCESS;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String readExpFlowCalibrationDate() {
        try {
            return readExpFlowSensorCalibration().getDate();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return "-";
    }


    public void setOxygenCalibration(SensorCalibration sensorCalibration) {
        //updateData(readCurrentUid() + "." + PREF_OXYGEN_CALIBRATION, oxygenCalibrationDate);


        if (sensorCalibration != null) {
            SharedPreferences.Editor editor = sp.edit();
            Gson gson = new Gson();
            String json = gson.toJson(sensorCalibration);
            editor.putString(PREF_OXYGEN_CALIBRATION, json);
            editor.apply();
        }

    }

    public SensorCalibration readOxygenCalibration() {

        Gson gson = new Gson();
        String json = sp.getString(PREF_OXYGEN_CALIBRATION, "Not Calibrated");
        return gson.fromJson(json, SensorCalibration.class);

    }

    public boolean readOxygenCalibrationStatus() {
        try {
            return sp.getBoolean("", readOxygenCalibration().getStatus() == SENSOR_CALIBRATION_SUCCESS);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String readOxygenCalibrationDate() {
        try {
            return sp.getString("", readOxygenCalibration().getDate());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return "-";
    }

    public void setExhaleValveCalibration(SensorCalibration sensorCalibration) {
        // updateData(readCurrentUid() + "." + PREF_PRESSER_CALIBRATION, oxygenCalibrationDate);

        if (sensorCalibration != null) {
            SharedPreferences.Editor editor = sp.edit();
            Gson gson = new Gson();
            String json = gson.toJson(sensorCalibration);
            editor.putString(PREF_EXHALE_VALVE_CALIBRATION, json);
            editor.apply();
        }

    }

    public SensorCalibration readExhaleValveCalibration() {
        // return sp.getString(readCurrentUid() + "." + PREF_PRESSER_CALIBRATION, "");

        Gson gson = new Gson();
        String json = sp.getString(PREF_EXHALE_VALVE_CALIBRATION, "Not Calibrated");
        return gson.fromJson(json, SensorCalibration.class);

    }

    public boolean readExhaleValveCalibrationStatus() {
        try {
            return sp.getBoolean("", readExhaleValveCalibration().getStatus() == SENSOR_CALIBRATION_SUCCESS);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return false;

    }

    public String readExhaleValveCalibrationDate() {
        try {
            return sp.getString("", readExhaleValveCalibration().getDate());
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return "-";
    }


    public void setPresserCalibration(SensorCalibration sensorCalibration) {
        // updateData(readCurrentUid() + "." + PREF_PRESSER_CALIBRATION, oxygenCalibrationDate);

        if (sensorCalibration != null) {
            SharedPreferences.Editor editor = sp.edit();
            Gson gson = new Gson();
            String json = gson.toJson(sensorCalibration);
            editor.putString(readCurrentUid() + "." + PREF_PRESSER_CALIBRATION, json);
            editor.apply();
        }

    }

    public SensorCalibration readPressureCalibration() {
        // return sp.getString(readCurrentUid() + "." + PREF_PRESSER_CALIBRATION, "");

        Gson gson = new Gson();
        String json = sp.getString(readCurrentUid() + "." + PREF_PRESSER_CALIBRATION, "");
        return gson.fromJson(json, SensorCalibration.class);

    }

    public boolean readPressureCalibrationStatus() {
        try {
            return readPressureCalibration().getStatus() == SENSOR_CALIBRATION_SUCCESS;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String readPressureCalibrationDate() {
        try {
            return readPressureCalibration().getDate();
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }


    //    VENTILATOR PARAMETER USER LIMITS
    public void setPipLimits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_PIP_MIN_MAX, min, max);
    }

    public Float[] readPipLimits() {

        //return readLimits(readCurrentUid() + "." + PREF_PIP_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_pip_limit)), Float.valueOf(context.getString(R.string.default_max_pip_limit)));
        return readLimits(readCurrentUid() + "." + PREF_PIP_MIN_MAX,
                Float.valueOf(filterAlarmLimitsbyPatientType(readCurrentUid(), context).get("presserLowerLimit")),
                Float.valueOf(filterAlarmLimitsbyPatientType(readCurrentUid(), context).get("presserUpperLimit")));

    }

    public void setVtiLimits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_VTI_MIN_MAX, min, max);
    }

    public Float[] readVtiLimits() {
        return readLimits(readCurrentUid() + "." + PREF_VTI_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_vti_limit)), Float.valueOf(context.getString(R.string.default_max_vti_limit)));
    }

    public void setVteLimits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_VTE_MIN_MAX, min, max);
    }

    public Float[] readVteLimits() {
        //return readLimits(readCurrentUid() + "." + PREF_VTE_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_vte_limit)), Float.valueOf(context.getString(R.string.default_max_vte_limit)));
        return readLimits(readCurrentUid() + "." + PREF_VTE_MIN_MAX,
                Float.valueOf(filterAlarmLimitsbyPatientType(readCurrentUid(), context).get("vteLowerLimit")),
                Float.valueOf(filterAlarmLimitsbyPatientType(readCurrentUid(), context).get("vteUpperLimit")));


    }


    public void setRRLimits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_RR_MIN_MAX, min, max);
    }

    public Float[] readRRLimits() {
        //return readLimits(readCurrentUid() + "." + PREF_RR_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_rr_limit)), Float.valueOf(context.getString(R.string.default_max_rr_limit)));
        return readLimits(readCurrentUid() + "." + PREF_RR_MIN_MAX,
                Float.valueOf(filterAlarmLimitsbyPatientType(readCurrentUid(), context).get("respiratoryLowerLimit")),
                Float.valueOf(filterAlarmLimitsbyPatientType(readCurrentUid(), context).get("respiratoryUpperLimit")));


    }

    public void setPEEPLimits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_PEEP_MIN_MAX, min, max);
    }

    public Float[] readPeepLimits() {
        //return readLimits(readCurrentUid() + "." + PREF_PEEP_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_peep_limit)), Float.valueOf(context.getString(R.string.default_max_peep_limit)));

        return readLimits(readCurrentUid() + "." + PREF_PEEP_MIN_MAX,
                Float.valueOf(filterAlarmLimitsbyPatientType(readCurrentUid(), context).get("peepLowerLimit")),
                Float.valueOf(filterAlarmLimitsbyPatientType(readCurrentUid(), context).get("peepUpperLimit")));

    }

    public void setMveLimits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_MVE_MIN_MAX, min, max);
    }

    public Float[] readMveLimits() {
        //return readLimits(readCurrentUid() + "." + PREF_MVI_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_mvi_limit)), Float.valueOf(context.getString(R.string.default_max_mvi_limit)));
        return readLimits(readCurrentUid() + "." + PREF_MVE_MIN_MAX,
                Float.valueOf(filterAlarmLimitsbyPatientType(readCurrentUid(), context).get("mveLowerLimit")),
                Float.valueOf(filterAlarmLimitsbyPatientType(readCurrentUid(), context).get("mveUpperLimit")));

    }

    public void setFiO2Limits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_FIO2_MIN_MAX, min, max);
    }

    public Float[] readFiO2Limits() {
        return readLimits(readCurrentUid() + "." + PREF_FIO2_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_fio2_limit)), Float.valueOf(context.getString(R.string.default_max_fio2_limit)));
    }

    public void setSpO2Limits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_SPO2_MIN_MAX, min, max);
    }

    public Float[] readSpO2Limits() {
        return readLimits(readCurrentUid() + "." + PREF_SPO2_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_spo2_limit)), Float.valueOf(context.getString(R.string.default_max_spo2_limit)));
    }

    public void setTiTotLimits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_TITOT_MIN_MAX, min, max);
    }

    public Float[] readTiTotLimits() {
        return readLimits(readCurrentUid() + "." + PREF_TITOT_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_titot_limit)), Float.valueOf(context.getString(R.string.default_max_titot_limit)));
    }

    public void setLeakLimits(Float min, Float max) {
        updateLimits(readCurrentUid() + "." + PREF_LEAK_MIN_MAX, min, max);
    }

    public Float[] readLeakLimits() {
        return readLimits(readCurrentUid() + "." + PREF_LEAK_MIN_MAX, Float.valueOf(context.getString(R.string.default_min_leak_limit)), Float.valueOf(context.getString(R.string.default_max_leak_limit)));
    }

    // VENTILATOR PARAMETER USER LIMITS STATE
    public void setPipLimitState(boolean isActive) {
        updateLimitState(readCurrentUid() + "." + PREF_PIP_ALARM_STATE, isActive);
    }

    public boolean readPipLimitState() {
        return readLimitState(readCurrentUid() + "." + PREF_PIP_ALARM_STATE);
    }


    public void setVtiLimitState(boolean isActive) {
        updateLimitState(readCurrentUid() + "." + PREF_VTI_ALARM_STATE, isActive);
    }


    public boolean readVtiLimitState() {
        return readLimitState(readCurrentUid() + "." + PREF_VTI_ALARM_STATE);
    }

    public void setVteLimitState(boolean isActive) {
        updateLimitState(readCurrentUid() + "." + PREF_VTE_ALARM_STATE, isActive);
    }

    public boolean readVteLimitState() {
        return readLimitState(readCurrentUid() + "." + PREF_VTE_ALARM_STATE);
    }

    public void setRRLimitState(boolean isActive) {
        updateLimitState(readCurrentUid() + "." + PREF_RR_ALARM_STATE, isActive);
    }

    public boolean readRRLimitState() {
        return readLimitState(readCurrentUid() + "." + PREF_RR_ALARM_STATE);
    }

    public void setPeepLimitState(boolean isActive) {
        updateLimitState(readCurrentUid() + "." + PREF_PEEP_ALARM_STATE, isActive);
    }

    public boolean readPeepLimitState() {
        return readLimitState(readCurrentUid() + "." + PREF_PEEP_ALARM_STATE);
    }

    public void setMveLimitState(boolean isActive) {
        updateLimitState(readCurrentUid() + "." + PREF_MVE_ALARM_STATE, isActive);
    }

    public boolean readMveLimitState() {
        return readLimitState(readCurrentUid() + "." + PREF_MVE_ALARM_STATE);
    }

    public void setFio2LimitState(boolean isActive) {
        updateLimitState(readCurrentUid() + "." + PREF_FIO2_ALARM_STATE, isActive);
    }

    public boolean readFio2LimitState() {
        return readLimitState(readCurrentUid() + "." + PREF_FIO2_ALARM_STATE);
    }

    public void setSpO2LimitState(boolean isActive) {
        updateLimitState(readCurrentUid() + "." + PREF_SPO2_ALARM_STATE, isActive);
    }

    public boolean readSpO2LimitState() {
        return readLimitState(readCurrentUid() + "." + PREF_SPO2_ALARM_STATE);
    }

    public void setTiTotLimitState(boolean isActive) {
        updateLimitState(readCurrentUid() + "." + PREF_TITOT_ALARM_STATE, isActive);
    }

    public boolean readTiTotLimitState() {
        return readLimitState(readCurrentUid() + "." + PREF_TITOT_ALARM_STATE);
    }

    public void setLeakLimitState(boolean isActive) {
        updateLimitState(readCurrentUid() + "." + PREF_LEAK_ALARM_STATE, isActive);
    }

    public boolean readLeakLimitState() {
        return readLimitState(readCurrentUid() + "." + PREF_LEAK_ALARM_STATE);
    }

    public void setGraphAutoScaling(boolean state) {
        updateData(PREF_GRAPH_AUTOSCALE, state);
    }

    public boolean readGraphAutoScaling() {
        return sp.getBoolean(PREF_GRAPH_AUTOSCALE, true);
    }

    public void setLeakCompensationStatus(boolean state) {
        updateData(PREF_LEAK_COMPENSATE, state);
    }

    public boolean readLeakCompensationStatus() {
        return sp.getBoolean(PREF_LEAK_COMPENSATE, true);
    }

    public void setPatientDischargeData(boolean isDischared) {

        updateData(PREF_PT_DISCHARGE, isDischared);
    }

    public boolean readPatientDischargeData() {
        return sp.getBoolean(PREF_PT_DISCHARGE, false);
    }

    //FILLED GRAPHS TOGGLE STATUS STARTS

    public void setPressureFilledStatus(boolean state) {
        updateData(PREF_FILLED_PRESSURE, state);
    }

    public boolean readPressureFilledStatus() {
        return sp.getBoolean(PREF_FILLED_PRESSURE, false);
    }

    public void setVolumeFilledStatus(boolean state) {
        updateData(PREF_FILLED_VOLUME, state);
    }

    public boolean readVolumeilledStatus() {
        return sp.getBoolean(PREF_FILLED_VOLUME, false);
    }

    public void setFlowFilledStatus(boolean state) {
        updateData(PREF_FILLED_FLOW, state);
    }

    public boolean readflowFilledStatus() {
        return sp.getBoolean(PREF_FILLED_FLOW, false);
    }


    //FILLED GRAPHS TOGGLE STATUS ENDS

    public void setNebuliserStatus(boolean state) {
        updateData(PREF_NEBULISER_STATUS, state);
    }

    public boolean readNebuliserStatus() {
        return sp.getBoolean(PREF_NEBULISER_STATUS, true);
    }

    public void setTubeBlockageAlarmStatus(boolean state) {
        updateData(PREF_TUBE_BLOCKAGE_ALARM, state);
    }

    public boolean readTubeBlockageAlarmStatus() {
        return sp.getBoolean(PREF_TUBE_BLOCKAGE_ALARM, true);
    }

    public void setCuffLeakageAlarmStatus(boolean state) {
        updateData(PREF_CUFF_LEAKAGE_ALARM, state);
    }

    public boolean readCuffLeakageAlarmStatus() {
        return sp.getBoolean(PREF_CUFF_LEAKAGE_ALARM, false);
    }

    public void setAlarmSuggestionStatus(boolean state) {
        updateData(PREF_ALARM_SUGGESTION, state);
    }

    public boolean readAlarmSuggestionStatus() {
        return sp.getBoolean(PREF_ALARM_SUGGESTION, false);
    }

    public void setLeakBasedDisconnectionStatus(boolean state) {
        updateData(PREF_LEAK_BASED_DISCONNECT, state);
    }

    public boolean readSmartFiO2Status() {
        return sp.getBoolean(PREF_SMART_FIO2_STATUS, false);
    }

    public void setSmartFiO2Status(boolean isActive) {
        updateData(PREF_SMART_FIO2_STATUS, isActive);
    }

    public boolean readIRVStatus() {
        return sp.getBoolean(PREF_IRV_STATUS, false);

    }

    public void setIRVStatus(boolean isActive) {

        updateData(PREF_IRV_STATUS, isActive);
        Log.i("IRV_STATUS", String.valueOf(readIRVStatus()));
    }

    public boolean readVGVStatus() {
        return sp.getBoolean(PREF_VGV_STATUS, false);

    }

    public void setVGVStatus(boolean isActive) {

        updateData(PREF_VGV_STATUS, isActive);
        Log.i("IRV_STATUS", String.valueOf(readVGVStatus()));
    }

    public boolean readEtCuffStatus() {
        return sp.getBoolean(PREF_EtCuff_STATUS, true);

    }

    public void setEtCuffStatus(boolean isActive) {

        updateData(PREF_EtCuff_STATUS, isActive);
    }

    public boolean readDeflashedStatus() {
        return sp.getBoolean(PREF_Deflashed_STATUS, false);

    }

    public void setDeflashedStatus(boolean isActive) {

        updateData(PREF_Deflashed_STATUS, isActive);
    }

    public boolean readLeakBasedAlarmStatus() {
        return sp.getBoolean(PREF_LEAK_BASED_DISCONNECT, true);
    }

    public boolean readOxygenHoldStatus() {
        return sp.getBoolean(PREF_IS_OXYGEN_HOLD_ACTIVE, false);
    }

    public void setOxygenHoldStatus(boolean status) {
        updateData(PREF_IS_OXYGEN_HOLD_ACTIVE, status);
    }


    public boolean readPediatricStatus() {
        return sp.getBoolean(PREF_IS_PEDIATRIC_ACTIVE, false);
    }

    public boolean readNeoNatalStatus() {
        return sp.getBoolean(PREF_IS_NEONATAL_ACTIVE, false);
    }

    public void setNeoNatalStatus(boolean status) {
        updateData(PREF_IS_NEONATAL_ACTIVE, status);
    }

    public void setPediatricStatus(boolean status) {
        Log.i("PEDIATRICCHECK", status ? "CHILD" : "ADULT");
        updateData(PREF_IS_PEDIATRIC_ACTIVE, status);
    }

    public void setGraphPoints(int graphPoints) {
        updateData(PREF_GRAPH_POINTS, graphPoints);
    }

    public int readGraphPoints() {
        return sp.getInt(PREF_GRAPH_POINTS, GRAPH_POINTS_MAX);
    }

    public void setEmergencyContact(String contact) {
        updateData(PREF_EMERGENCY_CONTACT, contact);
    }

    public String readEmergencyContact() {
        return sp.getString(PREF_EMERGENCY_CONTACT, null);
    }

    public void setStandbyStatus(boolean isStandby) {
        updateData(PREF_STANDBY_STATUS, isStandby);
    }

    public void setShutDownStatus(boolean isShutDown) {
        updateData(PREF_SHUTDOWN_STATUS, isShutDown);
    }

    public boolean readShutDownStatus() {
        return sp.getBoolean(PREF_SHUTDOWN_STATUS, false);
    }

    public boolean readStandbyStatus() {
        return sp.getBoolean(PREF_STANDBY_STATUS, false);
    }

    // Lock Screen Check
    public void setLockScreenStatus(boolean state) {
        updateData(PREF_SCREEN_LOCK, state);
    }

    public boolean readLockScreenStatus() {
        return sp.getBoolean(PREF_SCREEN_LOCK, true);
    }

    //setter method to save the operational hour of the dashboardactivity in the sharedpreference
    public void setOperationalHoursStartTime(long operationalHoursStartTime) {
        updateData(readCurrentUid() + "." + PREF_OPERATIONAL_HOURS_START_TIME, operationalHoursStartTime);
    }

    //getter method to get the saved value of the operational hours of the dashboardActivity from the sharedpreference
    public long readOperationalHoursStartTime() {
        return sp.getLong(readCurrentUid() + "." + PREF_OPERATIONAL_HOURS_START_TIME, 0L);
    }

    //setter method to save the operational end hour of the dashboardactivity in the sharedpreference
    public void setOperationalHoursEndTime(long operationalHoursEndTime) {
        updateData(readCurrentUid() + "." + PREF_OPERATIONAL_HOURS_END_TIME, operationalHoursEndTime);
    }

    //getter method to get the saved value of the operational end hours of the dashboardActivity from the sharedpreference
    public long readOperationalHoursEndTime() {
        return sp.getLong(readCurrentUid() + "." + PREF_OPERATIONAL_HOURS_END_TIME, 0L);
    }

    // pref for total hours & last hours
    public void setPrefTotalHoursTime(long value) {
        updateData(PREF_TOTAL_HOURS_TIME, value);
    }

    public long readPrefTotalHoursTime() {
        return sp.getLong(PREF_TOTAL_HOURS_TIME, 0L);
    }

    public void setPrefLastHoursTime(long value) {
        updateData(PREF_LAST_HOURS_TIME, value);
    }

    public long readPrefLastHoursTime() {
        return sp.getLong(PREF_LAST_HOURS_TIME, 0L);
    }

    //setter method to get the saved value of the service start hours of the whole application
    public void setServiceHoursStartTime(long serviceHoursStartTime) {
        updateData(readCurrentUid() + "." + PREF_SERVICE_HOURS_START_TIME, serviceHoursStartTime);
    }

    //getter method to get the saved value of the service start hours of the whole application
    public long readServiceHoursStartTime() {
        return sp.getLong(readCurrentUid() + "." + PREF_SERVICE_HOURS_START_TIME, 0L);
    }

    //setter method to get the saved value of the service end hours of the whole application
    public void setServiceHoursEndTime(long serviceHoursEndTime) {
        updateData(readCurrentUid() + "." + PREF_SERVICE_HOURS_END_TIME, serviceHoursEndTime);
    }

    //getter method to get the saved value of the service end hours of the whole application
    public long readServiceHoursEndTime() {
        return sp.getLong(readCurrentUid() + "." + PREF_SERVICE_HOURS_END_TIME, 0L);
    }

    //sensor Analysis

    public int readSensorLowFlowO2() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_LOW_PRESSURE_O2, SENSOR_MISSING);
    }

    public void setSensorLowFlowO2(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_LOW_PRESSURE_O2, val);
    }

    public int readSensorHighFlowO2() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_HIGH_PRESSURE_O2, SENSOR_MISSING);
    }

    public void setSensorHighFlowO2(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_HIGH_PRESSURE_O2, val);
    }

    public int readSensorCO2() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_CO2, SENSOR_MISSING);
    }

    public void setSensorCO2(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_CO2, val);
    }

    public int readSensorSPO2() {

        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_SPO2, SENSOR_MISSING);
    }

    public void setSensorSPO2(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_SPO2, val);
        Log.i("CHECK_SPO2", String.valueOf(val));
    }

    public int readSensorTemp() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_TEMP, SENSOR_MISSING);
    }

    public void setSensorTemp(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_TEMP, val);
    }


    public int readSensorDiaphragm() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_TEMP, SENSOR_MISSING);
    }

    public void setSensorDiaphragm(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_TEMP, val);
    }

    public int readSensorADC() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_ADC, SENSOR_MISSING);
    }

    public void setSensorADC(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_ADC, val);
    }

    public int readSensorHighPressureO2Line() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_HIGH_PRESSURE_LINE_O2, SENSOR_MISSING);
    }

    public void setSensorHighPressureO2Line(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_HIGH_PRESSURE_LINE_O2, val);
    }


    public int readSensorInspPressure() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_INSP_PRESSURE, SENSOR_MISSING);
    }

    public void setSensorInspPressure(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_INSP_PRESSURE, val);
    }

    public int readSensorExpPressure() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_EXP_PRESSURE, SENSOR_MISSING);
    }

    public void setSensorExpPressure(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_EXP_PRESSURE, val);
    }

    public int readSensorInhaleFlow() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_INAHLE_FLOW, SENSOR_MISSING);
    }

    public void setSensorInhaleFlow(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_INAHLE_FLOW, val);
    }

    public int readSensorNeoNateFlow() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_NEONATE_FLOW, SENSOR_MISSING);
    }

    public void setSensorNeoNateFlow(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_NEONATE_FLOW, val);
    }


    public int readSensorExhaleFlow() {
        return sp.getInt(readCurrentUid() + "." + PREF_SENSOR_EXHALE_FLOW, SENSOR_MISSING);
    }

    public void setSensorExhaleFlow(int val) {
        updateData(readCurrentUid() + "." + PREF_SENSOR_EXHALE_FLOW, val);
    }

    public int readSensorInspiratoryFlow() {
        return sp.getInt(PREF_SENSOR_INAHLE_FLOW, SENSOR_MISSING);
    }

    public void setSensorInspiratoryFlow(int val) {
        updateData(PREF_SENSOR_INAHLE_FLOW, val);
    }


    public Set<String> readPrimaryObservedParams() {
        Set<String> defaultParams = new LinkedHashSet<>();
        defaultParams.add(LBL_PIP);
        defaultParams.add(LBL_PEEP);
        //        defaultParams.add((getModeCategory(modeCode) == MODE_NIV) ? LBL_VTI : LBL_VTE);
        defaultParams.add(LBL_VTI);
        defaultParams.add(LBL_RR);
        defaultParams.add(LBL_FIO2);
        return sp.getStringSet(readCurrentUid() + "." + PREF_CONTROL_PARAMS, defaultParams);
    }

    public void setPrimaryControlParams(Set<String> params) {
        updateData(readCurrentUid() + "." + PREF_CONTROL_PARAMS, params);
    }


    public Set<String> readPrimaryObservedParamsNeonate() {
        Set<String> defaultParams = new LinkedHashSet<>();
        //        defaultParams.add((getModeCategory(modeCode) == MODE_NIV) ? LBL_VTI : LBL_VTE);
        defaultParams.add(LBL_FIO2);
        defaultParams.add(LBL_FLOW);
        defaultParams.add(PREF_PIP);
        return sp.getStringSet(readCurrentUid() + "." + PREF_CONTROL_PARAMS_NEONATE, defaultParams);
    }

    public Set<String> readPrimaryObservedParamsNasalProngs() {
        Set<String> defaultParams = new LinkedHashSet<>();
        //        defaultParams.add((getModeCategory(modeCode) == MODE_NIV) ? LBL_VTI : LBL_VTE);
        defaultParams.add(LBL_FIO2);
        defaultParams.add(LBL_PEEP);
        return sp.getStringSet(readCurrentUid() + "." + PREF_CONTROL_PARAMS_NASAL_PRONGS, defaultParams);
    }

    public void clearPrimaryControlParams() {
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(readCurrentUid() + "." + PREF_CONTROL_PARAMS);
        editor.apply();
    }


    public void clearVentilationPreferences(PatientProfile userid, SharedPreferences.Editor editor) {
        Log.i("CURRENT_USERID", String.valueOf(userid));
        Log.i("FIO2_CHECK", String.valueOf(PREF_FIO2));

        editor.remove(userid + "." + PREF_VENTILATION_MODE);
        editor.remove(userid + "." + PREF_PIP);
        editor.remove(userid + "." + PREF_TIDAL_VOLUME);
        editor.remove(userid + "." + PREF_RR);
        editor.remove(userid + "." + PREF_TRIG_FLOW);
        editor.remove(userid + "." + PREF_PLATEAU_PRESSURE);
        editor.remove(userid + "." + PREF_PEEP);
        editor.remove(userid + "." + PREF_INSP_TIME);
        editor.remove(userid + "." + PREF_SUPPORT_PRESSURE);
        editor.remove(userid + "." + PREF_PEAK_FLOW);
        editor.remove(userid + "." + PREF_FIO2);
        editor.remove(userid + "." + PREF_INSP_PAUSE);
        editor.remove(userid + "." + PREF_PEEP_VALVE);
        editor.remove(userid + "." + PREF_TARGET_VOLUME);
        editor.remove(userid + "." + PREF_FREQUENCY);
        editor.remove(userid + "." + PREF_FLOW);
        editor.remove(userid + "." + PREF_SLOPE);
        editor.remove(userid + "." + PREF_TLOW);
        editor.remove(userid + "." + PREF_TEXP);

        // editor.remove(userid + "." + PREF_SPONT_VT);

        //APRV SETTINGS
        editor.remove(userid + "." + PREF_THigh);
        editor.remove(userid + "." + PREF_TLow);
        editor.remove(userid + "." + PREF_PHigh);
        editor.remove(userid + "." + PREF_PLow);

        // APNEA PARAMETERS
//        editor.remove(userid + "." + PREF_APNEA_SETTINGS_STATUS);
        editor.remove(userid + "." + PREF_APNEA_TIDAL_VOLUME);
        editor.remove(userid + "." + PREF_APNEA_RR);
        editor.remove(userid + "." + PREF_APNEA_TRIG_FLOW);

        // SOFTWARE ALARM LIMITS
        editor.remove(userid + "." + PREF_PIP_MIN_MAX);
        editor.remove(userid + "." + PREF_VTI_MIN_MAX);
        editor.remove(userid + "." + PREF_RR_MIN_MAX);
        editor.remove(userid + "." + PREF_PEEP_MIN_MAX);
        editor.remove(userid + "." + PREF_MVI_MIN_MAX);
        editor.remove(userid + "." + PREF_MVE_MIN_MAX);
        editor.remove(userid + "." + PREF_TITOT_MIN_MAX);
        //setVGVStatus(false);


    }

/*    public void clearVentilationPreferences(PatientProfile userid) {
        SharedPreferences.Editor editor = sp.edit();
        clearVentilationPreferences(userid, editor);
        editor.apply();
    }*/

    public void clearProfilePreferences(PatientProfile userid) {
        SharedPreferences.Editor editor = sp.edit();

        clearVentilationPreferences(userid, editor);

//        editor.remove(userid + "." + PREF_VENTILATION_MODE);
//        editor.remove(userid + "." + PREF_PIP);
//        editor.remove(userid + "." + PREF_TIDAL_VOLUME);
//        editor.remove(userid + "." + PREF_RR);
//        editor.remove(userid + "." + PREF_TRIG_FLOW);
//        editor.remove(userid + "." + PREF_PLATEAU_PRESSURE);
//        editor.remove(userid + "." + PREF_PEEP);
//        editor.remove(userid + "." + PREF_INSP_TIME);
//        editor.remove(userid + "." + PREF_SUPPORT_PRESSURE);
//        editor.remove(userid + "." + PREF_PEAK_FLOW);
//        editor.remove(userid + "." + PREF_FIO2);
//        editor.remove(userid + "." + PREF_SLOPE);
//        editor.remove(userid + "." + PREF_TLOW);
//        editor.remove(userid + "." + PREF_TEXP);
//
//        // APNEA PARAMETERS
//        editor.remove(userid + "." + PREF_APNEA_SETTINGS_STATUS);
//        editor.remove(userid + "." + PREF_APNEA_TIDAL_VOLUME);
//        editor.remove(userid + "." + PREF_APNEA_RR);
//        editor.remove(userid + "." + PREF_APNEA_TRIG_FLOW);
//
//        // SOFTWARE ALARM LIMITS
//        editor.remove(userid + "." + PREF_PIP_MIN_MAX);
//        editor.remove(userid + "." + PREF_VTI_MIN_MAX);
//        editor.remove(userid + "." + PREF_RR_MIN_MAX);
//        editor.remove(userid + "." + PREF_PEEP_MIN_MAX);
//        editor.remove(userid + "." + PREF_MVI_MIN_MAX);
//        editor.remove(userid + "." + PREF_MVE_MIN_MAX);
//        editor.remove(userid + "." + PREF_TITOT_MIN_MAX);

//        editor.remove(PREF_EMERGENCY_CON
//        TACT);
//        editor.remove(PREF_BODY_WEIGHT);
        editor.remove(PREF_IS_OXYGEN_HOLD_ACTIVE);
//        editor.remove(PREF_UHID);
//        editor.remove(PREF_IS_PEDIATRIC_ACTIVE);

        editor.apply();
    }

    //Need to first check the data flowing in the preference manager and the flow of control towards the main activity
    //The data in the flow is the main cause of the control dialog fragment and the points of the settings of the data.
    @Deprecated
    public void createUserProfile(PatientProfile profile, Gender gender, Float height, Float age, Float weight) {
        Log.i("USER_PROFILE", profile + " " + gender + " " + " " + String.valueOf(age) + " " + String.valueOf(height) + " " + String.valueOf(weight));

        setCurrentUid(profile);
        setIsLoggedIn(true);
        setGender(gender);
        setBodyHeight(height);
        setAge(age);
        setBodyWeight(weight);
    }

    public void updateParameterViaName(final String name, Float val) {
        if (name != null && !name.trim().isEmpty() && val != null) {
            switch (name) {
                case LBL_PIP:
                    setPip(val);
                    break;

                case LBL_PEEP:
                    setPEEP(val);
                    break;

                case LBL_FIO2:

                    setFiO2(val);
                    break;

                case LBL_VTI:
                    setVti(val);
                    break;

                case LBL_RR:
                    setRR(val);
                    break;

                case LBL_TRIG_FLOW:
                    setTrigFlow(val);
                    break;

                case LBL_PPLAT:
                    setPplat(val);
                    break;

                case LBL_TINSP:
                    setTinsp(val);
                    break;

                case LBL_PEAK_FLOW:
                    setPeakFlow(val);
                    break;

                case LBL_SUPPORT_PRESSURE:
                    setSupportPressure(val);
                    break;

                case LBL_SLOPE:
                    setSlope(val);
                    break;

                case LBL_INSP_PAUSE:
                    setInspiratoryPause(val);
                    break;

                case LBL_PEEP_VALVE:
                    setPeepValve(val);
                    break;

                case LBL_TARGET_SPO2:
                    setTargetSpo2(val);
                    break;
                case LBL_HR_LIMIT:
                    setHrLimit(val);
                    break;

                case LBL_TARGET_VOLUME:
                    setTargetVolume(val);
                    break;

                case LBL_FREQUENCY:
                    setFrequency(val);
                    break;

                case LBL_FLOW:
                    setFlow(val);
                    break;

                case LBL_FIO2_DEV:
                    setFiO2Dev(val);
                    break;

                case LBL_TLOW:
                    setTlow(val);
                    break;

                case LBL_TEXP:
                    setTexp(val);
                    break;

                // APNEA PARAMETERS
                case LBL_APNEA_VT:
                    setVtApnea(val);
                    break;

                case LBL_APNEA_RR:
                    setRRApnea(val);
                    break;

                case LBL_TAPNEA:
                    setTApnea(val);
                    break;

                case LBL_APNEA_TRIG_FLOW:
                    setTrigFlowApnea(val);
                    break;

                case LBL_ET_PRESSURE:
                    setEtPressure(val);
                    break;

            }
        }
    }


    public void updateParameterForKnobCondition(final String name, Float val) {
        if (name != null && !name.trim().isEmpty() && val != null) {
            switch (name) {

                case LBL_PEEP:
                    setPEEP(val);
                    break;

                case LBL_RR:
                    setRR(val);
                    break;

                case LBL_PPLAT:
                    setPplat(val);
                    break;

                case LBL_TINSP:
                    setTinsp(val);
                    break;

                case LBL_SUPPORT_PRESSURE:
                    setSupportPressure(val);
                    break;
            }
        }
    }

    //    UPDATING SHARED PREFERENCES
    private void updateData(String key, Float val) {
        if (val != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat(key, val);
            editor.apply();
        }
    }

    private void updateData(String key, Long val) {
        if (val != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(key, val);
            editor.apply();
        }
    }

    private void updateData(String key, String val) {
        if (key != null && val != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, val);
            editor.apply();
        }
    }

    private void updateData(String key, Boolean val) {
        if (key != null && val != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(key, val);
            editor.apply();
        }
    }

    private void updateData(String key, Integer val) {
        if (key != null && val != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(key, val);
            editor.apply();
        }
    }

    private void updateData(String key, Set<String> val) {
        if (key != null && val != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putStringSet(key, val);
            editor.apply();
        }
    }

    private void updateLimits(String key, Float min, Float max) {
        if (key != null && min != null && max != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, String.valueOf(min) + LIMIT_SEPARATOR + String.valueOf(max));
            editor.apply();
        }
    }

    private void updateLimitState(String key, boolean state) {
        if (key != null) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(key, state);
            editor.apply();
        }
    }

    private boolean readLimitState(String key) {
        if (key != null) return sp.getBoolean(key, true);
        return false;
    }

    private Float[] readLimits(String key, Float defaultMin, Float defaultMax) {

        Float[] limits = null;

        if (key != null && defaultMin != null && defaultMax != null) {
            limits = new Float[2];
            limits[0] = defaultMin;
            limits[1] = defaultMax;

            String limit = sp.getString(key, null);
            if (limit != null) {
                String[] vals = limit.split(LIMIT_SEPARATOR);
                limits[0] = Float.valueOf(vals[0]);
                limits[1] = Float.valueOf(vals[1]);
            }

        }

        return limits;
    }

    public void clearLimitOnePreferences() {
        SharedPreferences.Editor editor = sp.edit();


        editor.remove(readCurrentUid() + "." + PREF_PIP_MIN_MAX);
        editor.remove(readCurrentUid() + "." + PREF_VTE_MIN_MAX);
        editor.remove(readCurrentUid() + "." + PREF_RR_MIN_MAX);
        editor.remove(readCurrentUid() + "." + PREF_PEEP_MIN_MAX);
        editor.remove(readCurrentUid() + "." + PREF_MVI_MIN_MAX);
        //  editor.remove(userid + "." + PREF_MVE_MIN_MAX);
        //  editor.remove(userid + "." + PREF_TITOT_MIN_MAX);

        //  editor.remove(PREF_PINSP_MIN_MAX);
        //  editor.remove(PREF_BODY_WEIGHT);
        //  editor.remove(PREF_IS_OXYGEN_HOLD_ACTIVE);
//        editor.remove(PREF_IS_PEDIATRIC_ACTIVE);

        editor.apply();
    }

    public void clearLimitOneStatePreferences() {
        SharedPreferences.Editor editor = sp.edit();


        editor.remove(readCurrentUid() + "." + PREF_PIP_ALARM_STATE);
        editor.remove(readCurrentUid() + "." + PREF_VTE_ALARM_STATE);
        editor.remove(readCurrentUid() + "." + PREF_RR_ALARM_STATE);
        editor.remove(readCurrentUid() + "." + PREF_PEEP_ALARM_STATE);
        editor.remove(readCurrentUid() + "." + PREF_MVI_ALARM_STATE);
        //  editor.remove(userid + "." + PREF_MVE_MIN_MAX);
        //  editor.remove(userid + "." + PREF_TITOT_MIN_MAX);

        //  editor.remove(PREF_PINSP_MIN_MAX);
        //  editor.remove(PREF_BODY_WEIGHT);
        //  editor.remove(PREF_IS_OXYGEN_HOLD_ACTIVE);
//        editor.remove(PREF_IS_PEDIATRIC_ACTIVE);

        editor.apply();
    }


    public void clearLimitTwoPreferences() {
        SharedPreferences.Editor editor = sp.edit();


        editor.remove(readCurrentUid() + "." + PREF_FIO2_MIN_MAX);
        // editor.remove(readCurrentUid() + "." + PREF_VTI_MIN_MAX);
        // editor.remove(readCurrentUid() + "." + PREF_RR_MIN_MAX);
        //  editor.remove(readCurrentUid() + "." + PREF_PEEP_MIN_MAX);
        // editor.remove(readCurrentUid() + "." + PREF_MVI_MIN_MAX);
        //  editor.remove(userid + "." + PREF_MVE_MIN_MAX);
        //  editor.remove(userid + "." + PREF_TITOT_MIN_MAX);

        //  editor.remove(PREF_PINSP_MIN_MAX);
        //  editor.remove(PREF_BODY_WEIGHT);
        //  editor.remove(PREF_IS_OXYGEN_HOLD_ACTIVE);
//        editor.remove(PREF_IS_PEDIATRIC_ACTIVE);

        editor.apply();
    }

    public void clearLimitTwoStatePreferences() {
        SharedPreferences.Editor editor = sp.edit();


        editor.remove(readCurrentUid() + "." + PREF_FIO2_ALARM_STATE);
        // editor.remove(readCurrentUid() + "." + PREF_VTI_MIN_MAX);
        // editor.remove(readCurrentUid() + "." + PREF_RR_MIN_MAX);
        //  editor.remove(readCurrentUid() + "." + PREF_PEEP_MIN_MAX);
        // editor.remove(readCurrentUid() + "." + PREF_MVI_MIN_MAX);
        //  editor.remove(userid + "." + PREF_MVE_MIN_MAX);
        //  editor.remove(userid + "." + PREF_TITOT_MIN_MAX);

        //  editor.remove(PREF_PINSP_MIN_MAX);
        //  editor.remove(PREF_BODY_WEIGHT);
        //  editor.remove(PREF_IS_OXYGEN_HOLD_ACTIVE);
//        editor.remove(PREF_IS_PEDIATRIC_ACTIVE);

        editor.apply();
    }

}
