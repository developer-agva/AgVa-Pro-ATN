package com.agvahealthcare.ventilator_ext.utility.utils;

import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.FIFOCAPACITY_CUSTOM_HALF;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.FIFOCAPACITY_CUSTOM_SIZE;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.GRAPH_THRESHOLD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.Pair;

import com.agvahealthcare.ventilator_ext.R;
import com.agvahealthcare.ventilator_ext.VentilatorApp;
import com.agvahealthcare.ventilator_ext.exceptions.InvalidModeException;
import com.agvahealthcare.ventilator_ext.logging.FileLogger;
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager;
import com.agvahealthcare.ventilator_ext.model.ControlParameterLimit;
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel;
import com.agvahealthcare.ventilator_ext.model.VentMode;
import com.agvahealthcare.ventilator_ext.utility.ConstantKt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by MOHIT MALHOTRA on 12-09-2018.
 */

public interface Configs {
    // for testing purpose

    enum Direction {
        UP,
        DOWN
    }

    enum BodyParamsType {
        AGE,
        WEIGHT,
        HEIGHT
    }

    enum ACTIVITY_TRACK {
        SPLASH,
        STANDBY,
        DASHBOARD,
        SHUTDOWN
    }

    enum ModeType {
        TYPE_Pressure,
        TYPE_Volume,
        TYPE_HFNC,
        TYPE_AUTO_VENTILATION,
        TYPE_NIV
    }

    enum GraphType_Change {
        TYPE_PRESSURE,
        TYPE_FLOW,
        TYPE_VOLUME
    }

    enum AckType {
        INVALID_ACK,
        ACK, NACK, OP_ACK
    }

    // create here 8 feb
    enum ControlSettingType {
        BASIC, BACKUP, ADVANCED, SmartFio2, VTas, EtCuff
    }

    enum RespiratoryHoldType {
        INSPIRATORY_HOLD,
        EXPIRATORY_HOLD
    }

    String trendFiveMin = "trend_five_min";
    String trendTwoMin = "trend_two_min";
    String trendTenMin = "trend_ten_min";

    String trendOneHour = "trend_one_hour";
    String trendEightHour = "trend_eight_hours";
    String trendTwelveHour = "trend_twelve_hours";
    String trendTwentyFourHour = "trend_twenty_four_hours";

    public static String getTrendsFileName(PreferenceManager preferenceManager) {

        switch (preferenceManager.readTrendDuration()) {

            case "5 Min":
                return trendFiveMin;
            case "2 Min":
                return trendTwoMin;
            case "10 Min":
                return trendTenMin;
        }

        return trendTenMin;
    }


    int BLUETOOTH = 0;
    int WIRED = 1;
    int CONNECTION_MODE = WIRED;
    String TILE_NO_SET_VALUE = "N/A";

    enum PatientProfile {TYPE_ADULT, TYPE_PED, TYPE_NEONAT,}

    enum SELECTED_OPTIONS {PRONGS_NAME, INVASIVE_NAME, NON_INVASIVE_NAME}

    enum CurrentActivity {TYPE_SPLASH, TYPE_MAINACTIVITY, TYPE_DASHBOARDACTIVITY}

    enum GraphType {TYPE_PRESSURE, TYPE_FLOW, TYPE_VOLUME, TYPE_PRESSURE_VOLUME, TYPE_FLOW_PRESSURE}

    enum Gender {TYPE_MALE, TYPE_FEMALE}

    enum BatteryLevelType {FULLY_CHARGED, CRITICALLY_LOW, CHARGING, ON_BATTERY}

    // SENSOR AVAILABILITY
    int SENSOR_AVAILABLE = 1;
    int SENSOR_MISSING = 0;

    String PREFIX_TUBE_COMPLIANCE = "COMP@";
    String PREFIX_TUBE_RESISTANCE = "RES@";

    String PREFIX_DIAG_START = "DIA~";
    String PREFIX_STARTUP_CHECK_START = "STA~";
    String PREFIX_OR_START = "OR~";
    String SUFFIX_DIAG_START = "{";
    String SUFFIX_STATUP_CHECK_STOP = "!";
    String SUFFIX_OR_START = "!";
    String PREFIX_HFNC = "HF~";

    String CALIBRATION_INSP_FLOW = "INSPIRATORY FLOW CALIBRATION";
    String CALIBRATION_LEAK_TEST = "LEAK TEST CALIBRATION";
    String CALIBRATION_EXP_FLOW = "EXPIRATORY FLOW CALIBRATION";
    String CALIBRATION_TURBINE = "TURBINE CALIBRATION";
    String CALIBRATION_OXYGEN = "OXYGEN CALIBRATION";
    String CALIBRATION_EXHALE_VALVE = "EXHALE VALVE CALIBRATION";
    String CALIBRATION_TUBE_RESISTANCE = "TUBE RESISTANCE CALIBRATION";
    String CALIBRATION_TUBE_COMPLIANCE = "TUBE COMPLIANCE CALIBRATION";

    String CALIBRATION_SUCCESS = "SUCCESS";
    String CALIBRATION_FAILED = "FAILED";

    String LIMITTER_ALARM_PRIORITY = "Z,";
    String DELIMETER_ALARM_PRIORITY = ",#";

    String  LIMITER_HARDWARE_MAC = "HMAC@";
    String DELIMITER_HARDWARE_MAC = "#";

    Character COMMON_TUBE_LIMITER = '@';
    Character COMMON_TUBE_DELIMETER = '!';

    int SENSOR_CALIBRATION_SUCCESS = 1;
    int SENSOR_CALIBRATION_FAILURE = 0;

    int ONSCREEN_GRAPH_LIMIT = 3;

    String ALARM_MAX_FIO2 = "Max FIO2 SPO2";
    String ALARM_MAX_FIO2_HR_HIGH = "Max FIO2 Hr";
    String ALARM_VENTILATOR_FAILURE = "VENTILATOR FAILURE";
    String ALARM_KNOB_FAILURE = "KNOB FAILURE";

    int GRAPH_POINTS_MAX = 12;
    int GRAPH_POINTS_DEFAULT = 180;

    // To toggle the configuration for Oxygen level support {3 VALUE MODE}
    // low = 2 value mode ; high = 3 value mode
    boolean isOxygenLevelsAvailable = true;

    // Fio2 setting structure supported
    // Related to backed functionality and doesnot govern any UI changes
    boolean isFio2SettingAvailable = true;

    String PREFIX_ACK = "ACK";
    String SUFFIX_ACK = "}";

    String LIMITER_GRAPH_LIMIT = "GR@";
    String DELIMITER_GRAPH_LIMIT = "!";
    String LIMITER_HARDWARE_SERIAL_NUMBER = "RP@";
    String DELIMITER_HARDWARE_SERIAL_NUMBER = "!";
    String PREFIX_BATTERY = "BTRY~";
    int BATTERY_CODE_LENGTH = 9;
    //BTRY@094-085-263#
    String LIMITER_BATTERY = "@";
    String DELIMITER_BATTERY = "`";

    String LIMITER_VENTI_LIVE = "M-";
    String DELIMITER_VENTI_LIVE = "!";

    String PREFIX_HEATSENSE = "HEAT";
    int HEATSENSE_CODE_LENGTH = 3 * 4; // 4 sensors with 3 digit value

    String PREFIX_MOTOR_LIFE = "MTRLF";
    int MOTOR_LIFE_CODE_LENGTH = 4;

    String PREFIX_CALIBRATION_ERROR = "ERR@";
    int CALIBRATION_ERROR_CODE_LENGTH = 24;

    String PREFIX_HARDWARE_VERSION = "VER:";
    String SUFIX_HARDWARE_VERSION = "$";

    String PREFIX_SELFTEST = "STP";
    int SELFTEST_RESPONSE_LENGTH = 3;

    String PREFIX_STANDBY = "STND";
    int STANDBY_RESPONSE_LENGTH = 2;
    String PREFIX_WIFI_CONN = "WIFI";
    int WIFI_CONN_RESPONSE_LENGTH = 1;

    String PREFIX_HANDSHAKE_CALIBRATE = "CALIB";
    int HANDSHAKE_CALIBRATE_LENGTH = 5;

    String PREFIX_DEVICE_NAME_REQUEST = "RQDN";
    int DEVICE_NAME_REQUEST_LENGTH = 1;

    int THRESHOLD_BATTERY_LEVEL = 20;
    int THRESHOLD_OXYGEN_VARIATION_VALUE = 40; // in percentage
    int CONFIGURATION_MODULE_DELAY = 250; // 150ms

    String PREFIX_SENSOR_AVAILABILITY = "SA";
    String SUFIX_SENSOR_AVAILABILITY = "!";
    String PREFIX_SENSOR_CALIBRATION = "SC";
    String PREFIX_RESISTANCE_CALIBRATION = "RES";
    String PREFIX_NEO_SENSOR = "NEO";
    String PREFIX_COMPLIANCE_CALIBRATION = "COMP";
    int SENSOR_CALIBRATION_REQUEST_LENGTH = 2;
    int TAG_SENSOR_LENGTH = 1;
    String TAG_SENSOR_EXP_FLOW = "1";
    String TAG_SENSOR_PRESSURE = "2";
    String TAG_SENSOR_OXYGEN = "3";
    String TAG_SENSOR_TURBINE = "4";
    String TAG_SENSOR_EXHALE_VALVE = "5";
    String TAG_SENSOR_INSP_FLOW = "6";
    String TAG_LEAK_TEST = "7";

    //Testing
    int DEVICE_DISCONNECTED = 0;
    int DEVICE_CONNECTED = 1;

    int WARNING_LEVEL_LOW = 0;
    int WARNING_LEVEL_HIGH = 1;
    int WARNING_LEVEL_UNMUTABLE = 2;

    String PREFIX_PLUS = "+";
    String PREFIX_MINUS = "-";
    String PREFIX_AND = "&";
    String QB_ALARM_MUTE_UNMUTE = "I1";
    String QB_NEBULISER = "I2";
    String QB_OXYGEN = "I3";
    String QB_INSPIRATORY_HOLD = "I4";
    String QB_EXPIRATORY_HOLD = "I5";
    String QB_MANUAL_BREATH = "I6";
    String QB_HOME = "I7";
    String QB_LOCK = "I8";
    String QB_POWER_SWITCH = "I0";
    String QB_POWER_ON = "I9";
    String QB_NEONATE_SENSOR_CONNECTED = "T1";
    String QB_NEONATE_SENSOR_DISCONNECTED = "T2";

    int KNOB_LENGTH = 1;

    //Alarms LEVEL for the alarms of different priority
    Uri URI_ALARM_HIGH_LEVEL = Uri.parse("android.resource://" + VentilatorApp.Companion.getInstance().getPackageName() + "/raw/warning_high_level");
    Uri URI_ALARM_MEDIUM_LEVEL = Uri.parse("android.resource://" + VentilatorApp.Companion.getInstance().getPackageName() + "/raw/warning_high_level");
    Uri URI_ALARM_CRITICAL_LEVEL = Uri.parse("android.resource://" + VentilatorApp.Companion.getInstance().getPackageName() + "/raw/warning_high_level");
    Uri URI_ALARM_LOW_LEVEL = Uri.parse("android.resource://" + VentilatorApp.Companion.getInstance().getPackageName() + "/raw/warning_low_level");
    Uri URI_ALARM_BATTERY_LOW = Uri.parse("android.resource://" + VentilatorApp.Companion.getInstance().getPackageName() + "/raw/warning_battery_low");
    Uri URI_BEEP = Uri.parse("android.resource://" + VentilatorApp.Companion.getInstance().getPackageName() + "/raw/beep");
    Uri URI_LONG_BEEP = Uri.parse("android.resource://" + VentilatorApp.Companion.getInstance().getPackageName() + "/raw/long_tune_beep");

    // parameters
    String LBL_AVERAGE_LEAK = "Average Leak";

    String ALARM_FIO2_LEAK = "Fio2 low Leakage";
    String ALARM_AUTO_PEEP = "Auto Peep";
    String ALARM_WRONG_PEEP = "Wrong Peep";
    String LBL_LEAK = "Leak";
    String LBL_PRESSURE = "P";
    String LBL_VOLUME = "V";
    String LBL_RAW_VOLUME = "Raw Volume";
    String LBL_RR = "RR";
    String LBL_PEEP = "PEEP";
    String LBL_ET_PRESSURE = "Cuff Pr.";
    String LBL_MV = "MV";
    String LBL_TRIGGER = "Respiratory Type";
    String LBL_RESPIRATORY_PHASE = "Respiratory Phase";

    String LBL_VPEAK = "PFR";
    String LBL_TITOT = "Ti/ToT";
    String LBL_IE_RATIO = "I:E";
    String LBL_TIME = "T";
    String LBL_FIO = "FiO";
    String LBL_SPO = "SpO";
    String LBL_PULSE = "Pulse";
    String LBL_TEMPERATURE = "Temp";
    String LBL_DYNAMIC_COMPLIANCE = "Dyn Comp.";
    String LBL_STATIC_COMPLIANCE = "Static Comp.";
    String LBL_LEAK_FLOW = "Leak Flow";
    String LBL_OBSERVED_PLATEAU_PRESSURE = "Pplat";
    String LBL_VPLAT = "Vplat";
    String LBL_MEAN_AIRWAY_PRESSURE = "Pmean";
    String LBL_TRISE = LBL_TIME + "rise";
    // Derived paramter names
    String LBL_PIP = "PIP";
    String LBL_PPLAT = "Pinsp";
    String LBL_PMEAN = LBL_PRESSURE + "mean";
    String LBL_MVI = LBL_MV + "i";

    String LBL_MVE = LBL_MV + "e";
    String LBL_FIO2 = LBL_FIO + "\u2082";
    String LBL_SPO2 = LBL_SPO + "\u2082";
    String LBL_HR = "PR";
    String LBL_ETCO2 = "EtCo2";
    String LBL_RAW_INSP_FLOW = "Raw Insp.";
    String LBL_RAW_EXP_FLOW = "Raw Exp.";
    String LBL_RAW_INSP_ZERO = "Raw Insp. Zero";
    String LBL_RAW_EXP_ZERO = "Raw Exp. Zero";
    String LBL_RAW_INEXP_DIFF = "Raw IE Diff";
    String LBL_CuffPressure = "Cuff Pressure";

    String LBL_VT = "VT";
    String LBL_VTI = LBL_VT + "i";
    String LBL_VTE = LBL_VT + "e";
    String LBL_VPEAK_I = LBL_VPEAK + "i";
    String LBL_VPEAK_E = LBL_VPEAK + "e";
    String LBL_TRIG_FLOW = "Trigger";
    String LBL_PEAK_FLOW = "Flow Limit";
    String LBL_TINSP = LBL_TIME + "insp";
    String LBL_TEXPR = LBL_TIME + "exp";  // observed param for expire time or insp termination
    String LBL_VLEAK = LBL_VOLUME + LBL_LEAK;
    String LBL_SUPPORT_PRESSURE = "Support Pressure";
    String LBL_SLOPE = "Slope";
    String LBL_INSP_PAUSE = "Inspiratory Pause";
    String LBL_AUTOFLOW = "AUTO FLOW";
    String LBL_PEEP_VALVE = "Peep Valve";
    String LBL_SPONT_VT = "Spont VT";
    String LBL_SPONT_RR = "Spont RR";
    String LBL_SPONT_MVI = "Spont MVi";
    String LBL_TARGET_SPO2 = "Target SPO2";
    String LBL_HR_LIMIT = "HR Limit";
    String LBL_TARGET_VOLUME = "Target Volume";
    String LBL_FREQUENCY = "Frequency";
    String LBL_FLOW = "Flow";
    String LBL_FIO2_DEV = "FiO2 Dev";
    String LBL_THIGH = "THigh";
    String LBL_PHIGH = "PHigh";
    //    String LBL_TLOW = "TLow";
    String LBL_PLOW = "PLow";

    String LBL_TLOW = LBL_TIME + "low";

    String LBL_TEXP = LBL_TEXPR;  // control param for expire time or insp termination
    String LBL_VOID_TILE = "void";
    String LBL_Volume_KEY = "Alarm Key";
    String LBL_TUBEDIA_KEY = "Tube Dia Key";
    String LBL_HEIGHT_KEY = "Height Key";
    String LBL_AGE_KEY = "Age Key";
    String LBL_WEIGHT_KEY = "Weigh tKey";
    String LBL_RSBI = "RSBI";
    String POWER_BUTTON = "Power Button";

    String LBL_APNEA = "apnea";
    String LBL_APNEA_VT = LBL_VT + LBL_APNEA;
    String LBL_APNEA_RR = LBL_RR + LBL_APNEA;
    String LBL_APNEA_TRIG_FLOW = LBL_TRIG_FLOW + LBL_APNEA;
    String LBL_TAPNEA = LBL_TIME + LBL_APNEA;
    String LBL_IRV_STATUS = "Irv Status";

    // testing for apnea
    ControlParameterModel rrApneaTest = null;
    ControlParameterModel tApneaTest = null;
    ControlParameterModel vtApneaTest = null;

    int MODE_PCV = 1;
    int MODE_PC_CMV = 11;
    int MODE_PC_SIMV = 12;
    int MODE_PC_PSV = 13;
    int MODE_PC_PRVC = 19;
    int MODE_PC_SPONTANEOUS = 14;
    int MODE_NEONAT_PC_SIMV = 15;
    int MODE_PC_SPONT_DUMMY = 16;
    int MODE_PC_ARPV = 18;
    int MODE_PC_AC = 17;
    int MODE_HFNC = 60;
    int MODE_VCV = 2;
    int MODE_VCV_CMV = 21;
    int MODE_VCV_SIMV = 22;
    int MODE_VCV_PRVC = 23;
    int MODE_AUTO_VENTILATION = 24;
    int MODE_VCV_ACV = 25;

    int MODE_VCV_VCV = 26;

    int MODE_NEONAT_VC_SIMV = 27;

    int MODE_NIV = 3;
    int MODE_NIV_CPAP = 31;
    int MODE_NIV_BPAP = 32;

    //    int MODE_NC_NCPAP = 38;
    int MODE_NC_IPPV = 33;
    int MODE_NC_CPAP = 34;

    int MODE_NIV_AV_BPAP = 35;

    int MODE_NIV_NCPAP = 36;
    int MODE_NIV_NBPAP = 37;

    //In embedded this is NON-INVASIVE
    int INV_CPAP = 38;
    int INV_BPAP = 39;

    int PARAMETER_TILE_COUNT = 9;

    UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    String SERV_ID_PREFIX = "0000ffe0";
    String CHARACT_ID_PREFIX = "0000ffe1";

    String REGISTERED_ACCOUNT = "1";
    String UNREGISTERED_ACCOUNT = "0";
    String DEACTIVATED_ACCOUNT = "-1";


    String PROFILE_NAME = "profile_name";
    String PROFILE_AGE = "profile_age";
    String PROFILE_GENDER = "profile_gender";
    String PROFILE_HEIGHT = "profile_height";
    String PROFILE_WEIGHT = "profile_weight";
    String PROFILE_EMAIL = "profile_email";
    String PROFILE_PHONE = "profile_phone";
    String PROFILE_HOSPITAL_ID = "profile_hospital_id";

    String INSPIRATORY_HOLD = "INSPIRATORY HOLD";
    String EXPIRATORY_HOLD = "EXPIRATORY HOLD";
    String NEBULIZER = "NEBULIZER";

    //acknowlegement priority

    enum AlarmType {
        ALARM_NO_LEVEL,
        ALARM_LOW_LEVEL,
        ALARM_MEDIUM_LEVEL,
        ALARM_HIGH_LEVEL,
        ALARM_CRITICAL_LEVEL
    }

    // change here 15 feb
    enum LoopsChartType {
        PressureVolumeChart_Type, FlowVolumeChart_Type, FlowPressureChart_Type,
    }

    int ALARM_NO_LEVEL = 0;
    int ALARM_LOW_LEVEL = 1;
    int ALARM_MEDIUM_LEVEL = 2;
    int ALARM_HIGH_LEVEL = 3;
    int ALARM_CRITICAL_LEVEL = 4;


    // TRIGGERS
    int TRIGGER_MACHINE = 0;
    int TRIGGER_PATIENT = 1;

//    int TRIGGER_MACHINE = 1;
//    int TRIGGER_PATIENT = 2;
//    int TRIGGER_APNEA = 0;


    //Alarm Lable
    String ALARM_PIP = "Alarm PIP";
    String ALARM_VTE = "VTE";
    String ALARM_FIO2 = "Alarm Fio2";
    String ALARM_VOLUME = "V";
    String ALARM_RAW_VOLUME = "Raw Volume";
    String ALARM_RR = "Respiratory Rate";
    String ALARM_PEEP = "PEEP";
    String ALARM_MVI = "MVI";
    String ALARM_MVE = "MVE";
    String ALARM_TRIGGER = "Respiratory Type";
    String ALARM_RESPIRATORY_PHASE = "Respiratory Phase";
    String ALARM_TITOT = "Ti/ToT";
    String ALARM_AVERAGE_LEAK = "Average Leak";

    //Low priority Alarms
    // ACKNOWLEDGEMENT CODES
    String ACK_CODE_0 = "ACK0000";
    String ACK_CODE_1 = "ACK0001";
    String ACK_CODE_2 = "ACK0002";
    String ACK_CODE_3 = "ACK0003";
    String ACK_CODE_4 = "ACK0004";
    String ACK_CODE_5 = "ACK0005";
    String ACK_CODE_6 = "ACK0006";
    String ACK_CODE_7 = "ACK0007";
    String ACK_CODE_8 = "ACK0008";
    String ACK_CODE_9 = "ACK0009";
    String ACK_CODE_10 = "ACK0010";
    String ACK_CODE_11 = "ACK0011";
    String ACK_CODE_12 = "ACK0012";
    String ACK_CODE_13 = "ACK0013";
    String ACK_CODE_14 = "ACK0014";
    String ACK_CODE_15 = "ACK0015";
    String ACK_CODE_16 = "ACK0016";
    String ACK_CODE_17 = "ACK0017";
    String ACK_CODE_18 = "ACK0018";
    String ACK_CODE_19 = "ACK0019";
    String ACK_CODE_20 = "ACK0020";
    String ACK_CODE_21 = "ACK0021";
    String ACK_CODE_22 = "ACK0022";
    String ACK_CODE_23 = "ACK0023";
    String ACK_CODE_24 = "ACK0024";
    String ACK_CODE_25 = "ACK0025";
    String ACK_CODE_26 = "ACK0026";
    String ACK_CODE_27 = "ACK0027";
    String ACK_CODE_28 = "ACK0028";
    String ACK_CODE_29 = "ACK0029";
    String ACK_CODE_30 = "ACK0030";
    String ACK_CODE_31 = "ACK0031";
    String ACK_CODE_32 = "ACK0032";
    String ACK_CODE_33 = "ACK0033";
    String ACK_CODE_34 = "ACK0034";
    String ACK_CODE_35 = "ACK0035";
    String ACK_CODE_36 = "ACK0036";
    String ACK_CODE_37 = "ACK0037";
    String ACK_CODE_38 = "ACK0038";
    String ACK_CODE_39 = "ACK0039";
    String ACK_CODE_40 = "ACK0040";
    String ACK_CODE_41 = "ACK0041";
    String ACK_CODE_42 = "ACK0042";
    String ACK_CODE_43 = "ACK0043";
    String ACK_CODE_44 = "ACK0044";
    String ACK_CODE_45 = "ACK0045";
    String ACK_CODE_46 = "ACK0046";
    String ACK_CODE_47 = "ACK0047";
    String ACK_CODE_48 = "ACK0048";

    String ACK_CODE_49 = "ACK0049";
    String ACK_CODE_50 = "ACK0050";
    String ACK_CODE_51 = "ACK0051";
    String ACK_CODE_52 = "ACK0052";
    String ACK_CODE_53 = "ACK0053";
    String ACK_CODE_54 = "ACK0054";
    String ACK_CODE_55 = "ACK0055";
    String ACK_CODE_56 = "ACK0056";
    String ACK_CODE_57 = "ACK0057";
    String ACK_CODE_58 = "ACK0058";
    String ACK_CODE_59 = "ACK0059";
    String ACK_CODE_60 = "ACK0060";
    String ACK_CODE_61 = "ACK0061";
    String ACK_CODE_62 = "ACK0062";
    String ACK_CODE_63 = "ACK0063";
    String ACK_CODE_64 = "ACK0064";
    String ACK_CODE_65 = "ACK0065";
    String ACK_CODE_66 = "ACK0066";
    String ACK_CODE_67 = "ACK0067";
    String ACK_CODE_68 = "ACK0068";
    String ACK_CODE_69 = "ACK0069";
    String ACK_CODE_70 = "ACK0070";
    String ACK_CODE_71 = "ACK0071";
    String ACK_CODE_72 = "ACK0072";
    String ACK_CODE_73 = "ACK0073";
    String ACK_CODE_74 = "ACK0074";
    String ACK_CODE_75 = "ACK0075";
    String ACK_CODE_76 = "ACK0076";
    String ACK_CODE_77 = "ACK0077";
    String ACK_CODE_78 = "ACK0078";
    String ACK_CODE_79 = "ACK0079";
    String ACK_CODE_80 = "ACK0080";
    String ACK_CODE_81 = "ACK0081";
    String ACK_CODE_82 = "ACK0082";
    String ACK_CODE_83 = "ACK0083";
    String ACK_CODE_84 = "ACK0084";
    String ACK_CODE_90 = "ACK0090";
    String ACK_CODE_91 = "ACK0091";
    String ACK_CODE_92 = "ACK0092";
    String ACK_CODE_93 = "ACK0093";
    String ACK_CODE_94 = "ACK0094";

    //Medium priority alarms
    String ACK_CODE_320 = "ACK0320";
    String ACK_CODE_321 = "ACK0321";
    String ACK_CODE_322 = "ACK0322";
    String ACK_CODE_323 = "ACK0323";
    String ACK_CODE_324 = "ACK0324";
    String ACK_CODE_325 = "ACK0325";
    String ACK_CODE_326 = "ACK0326";
    String ACK_CODE_327 = "ACK0327";
    String ACK_CODE_328 = "ACK0328";
    String ACK_CODE_329 = "ACK0329";
    String ACK_CODE_330 = "ACK0330";
    String ACK_CODE_331 = "ACK0331";
    String ACK_CODE_332 = "ACK0332";
    String ACK_CODE_333 = "ACK0333";
    String ACK_CODE_334 = "ACK0334";
    String ACK_CODE_335 = "ACK0335";
    String ACK_CODE_336 = "ACK0336";
    String ACK_CODE_337 = "ACK0337";
    String ACK_CODE_338 = "ACK0338";
    String ACK_CODE_339 = "ACK0339";
    String ACK_CODE_340 = "ACK0340";
    String ACK_CODE_341 = "ACK0341";
    String ACK_CODE_342 = "ACK0342";
    String ACK_CODE_343 = "ACK0343";
    String ACK_CODE_344 = "ACK0344";
    String ACK_CODE_345 = "ACK0345";
    String ACK_CODE_346 = "ACK0346";
    String ACK_CODE_347 = "ACK0347";
    String ACK_CODE_348 = "ACK0348";
    String ACK_CODE_349 = "ACK0349";
    String ACK_CODE_350 = "ACK0350";
    String ACK_CODE_351 = "ACK0351";
    String ACK_CODE_352 = "ACK0352";
    String ACK_CODE_353 = "ACK0353";
    String ACK_CODE_354 = "ACK0354";
    String ACK_CODE_355 = "ACK0355";
    String ACK_CODE_356 = "ACK0356";
    String ACK_CODE_357 = "ACK0357";
    String ACK_CODE_358 = "ACK0358";
    String ACK_CODE_359 = "ACK0359";
    String ACK_CODE_360 = "ACK0360";
    String ACK_CODE_361 = "ACK0361";
    String ACK_CODE_362 = "ACK0362";
    String ACK_CODE_363 = "ACK0363";
    String ACK_CODE_364 = "ACK0364";
    String ACK_CODE_365 = "ACK0365";
    String ACK_CODE_366 = "ACK0366";
    String ACK_CODE_367 = "ACK0367";
    String ACK_CODE_368 = "ACK0368";
    String ACK_CODE_369 = "ACK0369";
    String ACK_CODE_370 = "ACK0370";
    String ACK_CODE_371 = "ACK0371";
    String ACK_CODE_372 = "ACK0372";
    String ACK_CODE_373 = "ACK0373";
    String ACK_CODE_374 = "ACK0374";
    String ACK_CODE_375 = "ACK0375";
    String ACK_CODE_376 = "ACK0376";
    String ACK_CODE_377 = "ACK0377";
    String ACK_CODE_378 = "ACK0378";
    String ACK_CODE_379 = "ACK0379";
    String ACK_CODE_380 = "ACK0380";
    String ACK_CODE_381 = "ACK0381";
    String ACK_CODE_382 = "ACK0382";
    String ACK_CODE_383 = "ACK0383";
    String ACK_CODE_384 = "ACK0384";
    String ACK_CODE_385 = "ACK0385";
    String ACK_CODE_386 = "ACK0386";
    String ACK_CODE_387 = "ACK0387";
    String ACK_CODE_388 = "ACK0388";
    String ACK_CODE_389 = "ACK0389";
    String ACK_CODE_390 = "ACK0390";
    String ACK_CODE_391 = "ACK0391";
    String ACK_CODE_392 = "ACK0392";
    String ACK_CODE_393 = "ACK0393";
    String ACK_CODE_394 = "ACK0394";
    String ACK_CODE_395 = "ACK0395";
    String ACK_CODE_396 = "ACK0396";
    String ACK_CODE_397 = "ACK0397";
    String ACK_CODE_398 = "ACK0398";
    String ACK_CODE_399 = "ACK0399";
    String ACK_CODE_400 = "ACK0400";
    String ACK_CODE_401 = "ACK0401";
    String ACK_CODE_402 = "ACK0402";
    String ACK_CODE_403 = "ACK0403";
    String ACK_CODE_404 = "ACK0404";
    String ACK_CODE_405 = "ACK0405";
    String ACK_CODE_406 = "ACK0406";
    String ACK_CODE_407 = "ACK0407";
    String ACK_CODE_408 = "ACK0408";
    String ACK_CODE_409 = "ACK0409";
    String ACK_CODE_410 = "ACK0410";
    String ACK_CODE_411 = "ACK0411";
    String ACK_CODE_412 = "ACK0412";
    String ACK_CODE_413 = "ACK0413";
    String ACK_CODE_414 = "ACK0414";
    String ACK_CODE_415 = "ACK0415";
    String ACK_CODE_416 = "ACK0416";
    String ACK_CODE_417 = "ACK0417";
    String ACK_CODE_418 = "ACK0418";
    String ACK_CODE_419 = "ACK0419";
    String ACK_CODE_420 = "ACK0420";
    String ACK_CODE_421 = "ACK0421";
    String ACK_CODE_422 = "ACK0422";
    String ACK_CODE_423 = "ACK0423";
    String ACK_CODE_424 = "ACK0424";
    String ACK_CODE_425 = "ACK0425";
    String ACK_CODE_426 = "ACK0426";
    String ACK_CODE_427 = "ACK0427";
    String ACK_CODE_428 = "ACK0428";
    String ACK_CODE_429 = "ACK0429";

    String ACK_CODE_430 = "ACK0430";
    String ACK_CODE_431 = "ACK0431";
    String ACK_CODE_432 = "ACK0432";
    String ACK_CODE_433 = "ACK0433";
    String ACK_CODE_434 = "ACK0434";
    String ACK_CODE_435 = "ACK0435";
    String ACK_CODE_436 = "ACK0436";
    String ACK_CODE_437 = "ACK0437";
    String ACK_CODE_438 = "ACK0438";
    String ACK_CODE_439 = "ACK0439";
    String ACK_CODE_441 = "ACK0441";
    String ACK_CODE_451 = "ACK0451";


    // high priority alarm
    String ACK_CODE_640 = "ACK0640";
    String ACK_CODE_641 = "ACK0641";
    String ACK_CODE_642 = "ACK0642";
    String ACK_CODE_643 = "ACK0643";
    String ACK_CODE_644 = "ACK0644";
    String ACK_CODE_645 = "ACK0645";
    String ACK_CODE_646 = "ACK0646";
    String ACK_CODE_647 = "ACK0647";
    String ACK_CODE_648 = "ACK0648";
    String ACK_CODE_649 = "ACK0649";
    String ACK_CODE_650 = "ACK0650";
    String ACK_CODE_651 = "ACK0651";
    String ACK_CODE_652 = "ACK0652";
    String ACK_CODE_653 = "ACK0653";
    String ACK_CODE_654 = "ACK0654";
    String ACK_CODE_655 = "ACK0655";
    String ACK_CODE_656 = "ACK0656";
    String ACK_CODE_657 = "ACK0657";
    String ACK_CODE_658 = "ACK0658";
    String ACK_CODE_659 = "ACK0659";
    String ACK_CODE_660 = "ACK0660";
    String ACK_CODE_661 = "ACK0661";
    String ACK_CODE_662 = "ACK0662";
    String ACK_CODE_663 = "ACK0663";
    String ACK_CODE_664 = "ACK0664";
    String ACK_CODE_665 = "ACK0665";
    String ACK_CODE_666 = "ACK0666";
    String ACK_CODE_667 = "ACK0667";
    String ACK_CODE_668 = "ACK0668";
    String ACK_CODE_669 = "ACK0669";
    String ACK_CODE_670 = "ACK0670";
    String ACK_CODE_671 = "ACK0671";
    String ACK_CODE_672 = "ACK0672";
    String ACK_CODE_673 = "ACK0673";
    String ACK_CODE_674 = "ACK0674";
    String ACK_CODE_675 = "ACK0675";
    String ACK_CODE_676 = "ACK0676";
    String ACK_CODE_677 = "ACK0677";
    String ACK_CODE_678 = "ACK0678";
    String ACK_CODE_679 = "ACK0679";
    String ACK_CODE_680 = "ACK0680";
    String ACK_CODE_681 = "ACK0681";
    String ACK_CODE_682 = "ACK0682";
    String ACK_CODE_683 = "ACK0683";
    String ACK_CODE_684 = "ACK0684";
    String ACK_CODE_685 = "ACK0685";
    String ACK_CODE_686 = "ACK0686";
    String ACK_CODE_687 = "ACK0687";
    String ACK_CODE_688 = "ACK0688";
    String ACK_CODE_689 = "ACK0689";
    String ACK_CODE_690 = "ACK0690";
    String ACK_CODE_691 = "ACK0691";
    String ACK_CODE_692 = "ACK0692";
    String ACK_CODE_693 = "ACK0693";
    String ACK_CODE_694 = "ACK0694";
    String ACK_CODE_695 = "ACK0695";
    String ACK_CODE_696 = "ACK0696";
    String ACK_CODE_697 = "ACK0697";
    String ACK_CODE_698 = "ACK0698";
    String ACK_CODE_699 = "ACK0699";
    String ACK_CODE_700 = "ACK0700";
    String ACK_CODE_701 = "ACK0701";
    String ACK_CODE_702 = "ACK0702";
    String ACK_CODE_703 = "ACK0703";
    String ACK_CODE_704 = "ACK0704";
    String ACK_CODE_705 = "ACK0705";
    String ACK_CODE_706 = "ACK0706";
    String ACK_CODE_707 = "ACK0707";
    String ACK_CODE_708 = "ACK0708";
    String ACK_CODE_709 = "ACK0709";
    String ACK_CODE_710 = "ACK0710";
    String ACK_CODE_711 = "ACK0711";
    String ACK_CODE_712 = "ACK0712";
    String ACK_CODE_713 = "ACK0713";
    String ACK_CODE_714 = "ACK0714";
    String ACK_CODE_715 = "ACK0715";
    String ACK_CODE_716 = "ACK0716";
    String ACK_CODE_717 = "ACK0717";
    String ACK_CODE_718 = "ACK0718";
    String ACK_CODE_719 = "ACK0719";
    String ACK_CODE_720 = "ACK0720";
    String ACK_CODE_721 = "ACK0721";
    String ACK_CODE_722 = "ACK0722";
    String ACK_CODE_723 = "ACK0723";
    String ACK_CODE_724 = "ACK0724";
    String ACK_CODE_725 = "ACK0725";
    String ACK_CODE_726 = "ACK0726";
    String ACK_CODE_727 = "ACK0727";
    String ACK_CODE_728 = "ACK0728";
    String ACK_CODE_729 = "ACK0729";
    String ACK_CODE_730 = "ACK0730";
    String ACK_CODE_731 = "ACK0731";
    String ACK_CODE_732 = "ACK0732";
    String ACK_CODE_733 = "ACK0733";
    String ACK_CODE_734 = "ACK0734";
    String ACK_CODE_735 = "ACK0735";
    String ACK_CODE_736 = "ACK0736";
    String ACK_CODE_737 = "ACK0737";
    String ACK_CODE_738 = "ACK0738";
    String ACK_CODE_739 = "ACK0739";
    String ACK_CODE_740 = "ACK0740";
    String ACK_CODE_741 = "ACK0741";
    String ACK_CODE_742 = "ACK0742";
    String ACK_CODE_743 = "ACK0743";
    String ACK_CODE_744 = "ACK0744";
    String ACK_CODE_745 = "ACK0745";
    String ACK_CODE_746 = "ACK0746";
    String ACK_CODE_747 = "ACK0747";
    String ACK_CODE_748 = "ACK0748";
    String ACK_CODE_749 = "ACK0749";
    String ACK_CODE_750 = "ACK0750";
    String ACK_CODE_751 = "ACK0751";
    String ACK_CODE_752 = "ACK0752";
    String ACK_CODE_753 = "ACK0753";
    String ACK_CODE_754 = "ACK0754";
    String ACK_CODE_755 = "ACK0755";
    String ACK_CODE_756 = "ACK0756";
    String ACK_CODE_757 = "ACK0757";
    String ACK_CODE_758 = "ACK0758";
    String ACK_CODE_759 = "ACK0759";
    String ACK_CODE_760 = "ACK0760";
    String ACK_CODE_761 = "ACK0761";
    String ACK_CODE_762 = "ACK0762";
    String ACK_CODE_763 = "ACK0763";
    String ACK_CODE_764 = "ACK0764";
    String ACK_CODE_765 = "ACK0765";
    String ACK_CODE_766 = "ACK0766";
    String ACK_CODE_767 = "ACK0767";
    String ACK_CODE_768 = "ACK0768";
    String ACK_CODE_769 = "ACK0769";
    String ACK_CODE_770 = "ACK0770";
    String ACK_CODE_771 = "ACK0771";
    String ACK_CODE_772 = "ACK0772";
    String ACK_CODE_773 = "ACK0773";
    String ACK_CODE_774 = "ACK0774";
    String ACK_CODE_775 = "ACK0775";
    String ACK_CODE_776 = "ACK0776";
    String ACK_CODE_777 = "ACK0777";
    String ACK_CODE_778 = "ACK0778";
    String ACK_CODE_779 = "ACK0779";
    String ACK_CODE_780 = "ACK0780";
    String ACK_CODE_781 = "ACK0781";
    String ACK_CODE_782 = "ACK0782";
    String ACK_CODE_783 = "ACK0783";
    String ACK_CODE_784 = "ACK0784";
    String ACK_CODE_785 = "ACK0785";
    String ACK_CODE_786 = "ACK0786";
    String ACK_CODE_787 = "ACK0787";
    String ACK_CODE_788 = "ACK0788";//for ShutDown Alert Add
    String ACK_CODE_789 = "ACK0789";
    String ACK_CODE_790 = "ACK0790";
    String ACK_CODE_791 = "ACK0791";
    String ACK_CODE_792 = "ACK0792";
    String ACK_CODE_793 = "ACK0793";
    String ACK_CODE_794 = "ACK0794";
    String ACK_CODE_795 = "ACK0795";
    String ACK_CODE_796 = "ACK0796";
    String ACK_CODE_797 = "ACK0797";
    String ACK_CODE_798 = "ACK0798";//AntiACK for ShutDown Alert Removal
    String ACK_CODE_799 = "ACK0799";
    String ACK_CODE_800 = "ACK0800";
    String ACK_CODE_801 = "ACK0801";
    String ACK_CODE_802 = "ACK0802";
    String ACK_CODE_803 = "ACK0803";

    String ACK_CODE_804 = "ACK0804";
    String ACK_CODE_805 = "ACK0805";
    String ACK_CODE_806 = "ACK0806";
    String ACK_CODE_807 = "ACK0807";
    String ACK_CODE_809 = "ACK0809";

    String ACK_CODE_810 = "ACK0810";
    String ACK_CODE_811 = "ACK0811";
    String ACK_CODE_812 = "ACK0812";
    String ACK_CODE_813 = "ACK0813";
    String ACK_CODE_814 = "ACK0814";
    String ACK_CODE_815 = "ACK0815";
    String ACK_CODE_816 = "ACK0816";
    String ACK_CODE_817 = "ACK0817";
    String ACK_CODE_819 = "ACK0819";
    String ACK_CODE_821 = "ACK0821";
    String ACK_CODE_822 = "ACK0822";
    String ACK_CODE_823 = "ACK0823";
    String ACK_CODE_824 = "ACK0824";
    String ACK_CODE_825 = "ACK0825";
    String ACK_CODE_826 = "ACK0826";
    String ACK_CODE_827 = "ACK0827";

    String ACK_CODE_831 = "ACK0831";
    String ACK_CODE_832 = "ACK0832";
    String ACK_CODE_833 = "ACK0833";
    String ACK_CODE_834 = "ACK0834";
    String ACK_CODE_835 = "ACK0835";
    String ACK_CODE_836 = "ACK0836";
    String ACK_CODE_837 = "ACK0837";


    String ACK_CODE_5001 = "ACK5001";
    String ACK_CODE_5002 = "ACK5002";
    String ACK_CODE_5003 = "ACK5003";
    String ACK_CODE_5004 = "ACK5004";
    String ACK_CODE_5005 = "ACK5005";
    String ACK_CODE_5006 = "ACK5006";
    String ACK_CODE_5007 = "ACK5007";

    // ACK FOR iNSP FLOW
    String ACK_CODE_5120 = "ACK5120";

    String ACK_CODE_5122 = "ACK5122";
    String ACK_CODE_4001 = "ACK4001";

    // Ack for leak test
    String ACK_CODE_4020 = "ACK4020";
    String ACK_CODE_4021 = "ACK4021";
    String ACK_CODE_4022 = "ACK4022";

    //Inspiratory Flow Calibration Error
    String ACK_CODE_5133 = "ACK5133";
    String ACK_CODE_5121 = "ACK5121";
    String ACK_CODE_5134 = "ACK5134";
    String ACK_CODE_5135 = "ACK5135";
    String ACK_CODE_5136 = "ACK5136";

    //ACK for Calibrator Error 126
    String ACK_CODE_5126 = "ACK5126";
    //
    // ACK FOR EXPP FLOW
    String ACK_CODE_5123 = "ACK5123"; //
    String ACK_CODE_5124 = "ACK5124"; //
    String ACK_CODE_5125 = "ACK5125"; //
    String ACK_CODE_5008 = "ACK5008"; //
    String ACK_CODE_5009 = "ACK5009"; //

    //Calibration ACKs
    String ACK_CODE_5011 = "ACK5011";
    String ACK_CODE_5012 = "ACK5012";
    String ACK_CODE_5013 = "ACK5013";
    String ACK_CODE_5014 = "ACK5014";
    String ACK_CODE_5015 = "ACK5015";
    String ACK_CODE_5016 = "ACK5016";
    String ACK_CODE_5017 = "ACK5017";
    String ACK_CODE_5019 = "ACK5019";

    String ACK_CODE_5021 = "ACK5021";
    String ACK_CODE_5022 = "ACK5022";
    String ACK_CODE_5023 = "ACK5023";
    String ACK_CODE_5024 = "ACK5024";
    String ACK_CODE_5025 = "ACK5025";
    String ACK_CODE_5026 = "ACK5026";
    String ACK_CODE_5028 = "ACK5028";
    String ACK_CODE_5029 = "ACK5029";

    String ACK_CODE_5041 = "ACK5041";
    String ACK_CODE_5042 = "ACK5042";
    String ACK_CODE_5043 = "ACK5043";
    String ACK_CODE_5044 = "ACK5044";
    String ACK_CODE_5045 = "ACK5045";
    String ACK_CODE_5046 = "ACK5046";
    String ACK_CODE_5047 = "ACK5047";
    String ACK_CODE_5048 = "ACK5048";
    String ACK_CODE_5049 = "ACK5049";
    String ACK_CODE_5050 = "ACK5050";
    String ACK_CODE_5051 = "ACK5051";
    String ACK_CODE_5052 = "ACK5052";
    String ACK_CODE_5053 = "ACK5053";
    String ACK_CODE_5054 = "ACK5054";
    String ACK_CODE_5055 = "ACK5055";
    String ACK_CODE_5056 = "ACK5056";
    String ACK_CODE_5057 = "ACK5057";
    String ACK_CODE_5058 = "ACK5058";
    String ACK_CODE_5059 = "ACK5059";
    String ACK_CODE_5060 = "ACK5060";
    String ACK_CODE_5071 = "ACK5071";
    String ACK_CODE_5072 = "ACK5072";
    String ACK_CODE_5073 = "ACK5073";
    String ACK_CODE_5074 = "ACK5074";
    String ACK_CODE_5075 = "ACK5075";
    String ACK_CODE_5078 = "ACK5078";
    String ACK_CODE_5079 = "ACK5079";
    String ACK_CODE_5081 = "ACK5081";
    String ACK_CODE_5082 = "ACK5082";
    String ACK_CODE_5083 = "ACK5083";
    String ACK_CODE_5084 = "ACK5084";
    String ACK_CODE_5085 = "ACK5085";
    String ACK_CODE_5088 = "ACK5088";
    String ACK_CODE_5089 = "ACK5089";
    String ACK_CODE_5090 = "ACK5090";
    String ACK_CODE_5091 = "ACK5091";
    String ACK_CODE_5092 = "ACK5092";
    String ACK_CODE_5093 = "ACK5093";
    String ACK_CODE_5094 = "ACK5094";
    String ACK_CODE_5095 = "ACK5095";
    String ACK_CODE_5096 = "ACK5096";
    String ACK_CODE_5101 = "ACK5101";
    String ACK_CODE_5102 = "ACK5102";
    String ACK_CODE_5103 = "ACK5103";
    String ACK_CODE_5111 = "ACK5111";
    String ACK_CODE_5114 = "ACK5114";
    String ACK_CODE_5115 = "ACK5115";
    String ACK_CODE_5140 = "ACK5140";
    String ACK_CODE_5141 = "ACK5141";
    String ACK_CODE_5169 = "ACK5169";
    String ACK_CODE_5170 = "ACK5170";
    String ACK_CODE_4000 = "ACK4000";
    String ACK_CODE_4050 = "ACK4050";
    String ACK_CODE_4010 = "ACK4010";
    String ACK_CODE_4011 = "ACK4011";
    String ACK_CODE_4012 = "ACK4012";
    String ACK_CODE_5180 = "ACK5180";

    //ACK 5190 is used on socket for Connection

    //Start ACKs for EtCuff

    String ACK_CODE_5181 = "ACK5181"; // For Start of Process.
    String ACK_CODE_5182 = "ACK5182"; //For Error
    String ACK_CODE_5183 = "ACK5183"; //For Error
    String ACK_CODE_5189 = "ACK5189"; //For success

    //Ends Here

    //device update acks (embedded ota functionality
    String ACK_CODE_5301 = "ACK5301";
    String ACK_CODE_5302 = "ACK5302";
    String ACK_CODE_5303 = "ACK5303";
    String ACK_CODE_5304 = "ACK5304";
    String ACK_CODE_5305 = "ACK5305";
    String ACK_CODE_5306 = "ACK5306";
    String ACK_CODE_5307 = "ACK5307";
    String ACK_CODE_5308 = "ACK5308";
    String ACK_CODE_5309 = "ACK5309";
    String ACK_CODE_5310 = "ACK5310";
    String ACK_CODE_5311 = "ACK5311";
    String ACK_CODE_5312 = "ACK5312";
    String ACK_CODE_5313 = "ACK5313";
    String ACK_CODE_5314 = "ACK5314";


    String PROJECT_CODE = "008";
    String CONFIGS_STRING = "CONFIGS_STRING";


    // ack for diagnostic
    String ACK_CODE_5201 = "ACK5201";
    String ACK_CODE_5202 = "ACK5202";


    // ack for O2 regulation check
    String ACK_CODE_5203 = "ACK5203";
    String ACK_CODE_5204 = "ACK5204";


    //NEO SENSOR WARNING

    String ACK_CODE_5601 = "ACK5601";
    String ACK_CODE_5602 = "ACK5602";
    String ACK_CODE_5603 = "ACK5603";

    // for venti configs
    String ACK_CODE_5604 = "ACK5604";

    String RUNNING_STATUS_ACTIVE = "ACTIVE";
    String RUNNING_STATUS_INACTIVE = "INACTIVE";
    String ACTIVITY_SPLASH = "SPLASH";
    String ACTIVITY_STANDBY = "STANDBY";
    String ACTIVITY_SHUTDOWN = "SHUTDOWN";
    String ACTIVITY_VENTILATION = "VENTILATION";

    // created by masoom on 04 jan 2023
    enum ChartType {
        PressureChart_Type, FlowChart_Type, VolumeChart_Type, EtCo2_Type
    }

    // method for dashboard chart y-axis range
    // Modified by masoom on 04 jan 2023
    static Pair<Double, Double> getRangeOfYAxisChart(Context ctx, ChartType chartType) {

        final PreferenceManager prefManager = new PreferenceManager(ctx);
        // Pair for ranges of y-axis of dashboard chart
        Pair<Double, Double> range = new Pair<>(0.0, 0.0);

        switch (prefManager.readCurrentUid()) {
            case TYPE_ADULT:
                switch (chartType) {
                    case PressureChart_Type:
                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_PRESSURE_ADULT_PEDIA);
                        break;

                    case FlowChart_Type:
                        range = new Pair<>(ConstantKt.NEGATIVE_MIN_RANGE_FLOW_ADULT_PEDIA, ConstantKt.POSITIVE_MIN_RANGE_FLOW_ADULT_PEDIA);
                        break;

                    case VolumeChart_Type:
                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_VOLUME_ADULT_PEDIA);
                        break;
                }
                break;

            case TYPE_PED:
                switch (chartType) {
                    case PressureChart_Type:
                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_PRESSURE_ADULT_PEDIA);
                        break;

                    case FlowChart_Type:
                        range = new Pair<>(ConstantKt.NEGATIVE_MIN_RANGE_FLOW_ADULT_PEDIA, ConstantKt.POSITIVE_MIN_RANGE_FLOW_ADULT_PEDIA);
                        break;

                    case VolumeChart_Type:
                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_VOLUME_ADULT_PEDIA);
                        break;
                }
                break;

            case TYPE_NEONAT:
                switch (chartType) {
                    case PressureChart_Type:
                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MID_RANGE_PRESSURE_NEO);
                        break;

                    case FlowChart_Type:
                        range = new Pair<>(ConstantKt.NEGATIVE_MIN_RANGE_FLOW_NEO, ConstantKt.POSITIVE_MIN_RANGE_FLOW_NEO);
                        break;

                    case VolumeChart_Type:
                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_VOLUME_NEO);
                        break;
                }
                break;
        }
        return range;
    }


//    static Pair<Double, Double> getRangeOfYAxisChartForLoops(Context ctx, ChartType chartType) {
//
//        final PreferenceManager prefManager = new PreferenceManager(ctx);
//        // Pair for ranges of y-axis of dashboard chart
//        Pair<Double, Double> range = new Pair<>(0.0, 0.0);
//
//        switch (prefManager.readCurrentUid()) {
//            case TYPE_ADULT:
//                switch (chartType) {
//                    case PressureChart_Type:
//                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_PRESSURE_ADULT_PEDIA);
//                        break;
//
//                    case FlowChart_Type:
//                        range = new Pair<>(ConstantKt.NEGATIVE_MIN_RANGE_FLOW_ADULT_PEDIA, ConstantKt.POSITIVE_MIN_RANGE_FLOW_ADULT_PEDIA);
//                        break;
//
//                    case VolumeChart_Type:
//                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_VOLUME_ADULT_PEDIA);
//                        break;
//                }
//                break;
//
//            case TYPE_PED:
//                switch (chartType) {
//                    case PressureChart_Type:
//                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_PRESSURE_ADULT_PEDIA);
//                        break;
//
//                    case FlowChart_Type:
//                        range = new Pair<>(ConstantKt.NEGATIVE_MIN_RANGE_FLOW_ADULT_PEDIA, ConstantKt.POSITIVE_MIN_RANGE_FLOW_ADULT_PEDIA);
//                        break;
//
//                    case VolumeChart_Type:
//                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_VOLUME_ADULT_PEDIA);
//                        break;
//                }
//
//                break;
//
//            case TYPE_NEONAT:
//                switch (chartType) {
//                    case PressureChart_Type:
//                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_PRESSURE_NEO);
//                        break;
//
//                    case FlowChart_Type:
//                        range = new Pair<>(ConstantKt.NEGATIVE_MIN_RANGE_FLOW_NEO, ConstantKt.POSITIVE_MIN_RANGE_FLOW_NEO);
//                        break;
//
//                    case VolumeChart_Type:
//                        range = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_VOLUME_NEO);
//                        break;
//                }
//                break;
//        }
//
//
//        return range;
//    }

    // HANDSHAKE
    String INFORM_HANDSHAKE = "HS";
    // PING
    String INFORM_PING = "PING";

    List<String> o2CalibrationAck = Arrays.asList(ACK_CODE_0, ACK_CODE_1, ACK_CODE_5, ACK_CODE_6, ACK_CODE_52, ACK_CODE_53, ACK_CODE_54, ACK_CODE_55, ACK_CODE_56);

    // This is list of higher priority alarms but less than unmutable alarms
    List<String> highLevelAcks = Arrays.asList(
            ACK_CODE_12, ACK_CODE_13, ACK_CODE_16, ACK_CODE_17, ACK_CODE_18, ACK_CODE_19,
            ACK_CODE_30
    );

    // This list provides alarm color code scheme
    List<String> dangerColoredAcks = Arrays.asList(
            ACK_CODE_5,
            ACK_CODE_10, ACK_CODE_11, ACK_CODE_12, ACK_CODE_13, ACK_CODE_14, ACK_CODE_15, ACK_CODE_16, ACK_CODE_17, ACK_CODE_18, ACK_CODE_19,
            ACK_CODE_30, ACK_CODE_31, ACK_CODE_35,
            ACK_CODE_50, ACK_CODE_62, ACK_CODE_68, ACK_CODE_80
    );

    // This is list of Highest priority alarms which cannot be muted
    List<String> unmutedLevelAcks = Arrays.asList(
            ACK_CODE_5,
            ACK_CODE_31
    );

    List<String> noSoundAcks = Arrays.asList(
            ACK_CODE_0,
            ACK_CODE_36
    );

    // change here 15 feb
    static Pair<Pair<Double, Double>, Pair<Double, Double>> getRangeOfYAxisChartLoops(Context ctx, LoopsChartType chartType) {

        final PreferenceManager prefManager = new PreferenceManager(ctx);
        // Pair for ranges of y-axis of dashboard chart
        Pair<Double, Double> rangeFirst = new Pair<>(0.0, 0.0);
        Pair<Double, Double> rangeSecond = new Pair<>(0.0, 0.0);

        switch (prefManager.readCurrentUid()) {
            case TYPE_PED:
            case TYPE_ADULT:
                switch (chartType) {
                    case PressureVolumeChart_Type:
                        rangeFirst = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_PRESSURE_ADULT_PEDIA_LOOPS);
                        rangeSecond = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_VOLUME_ADULT_PEDIA_LOOPS);
                        break;

                    case FlowPressureChart_Type:
                        rangeFirst = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_PRESSURE_ADULT_PEDIA_LOOPS);
                        rangeSecond = new Pair<>(ConstantKt.NEGATIVE_MIN_RANGE_FLOW_ADULT_PEDIA_LOOPS, ConstantKt.POSITIVE_MIN_RANGE_FLOW_ADULT_PEDIA_LOOPS);
                        break;

                    case FlowVolumeChart_Type:
                        rangeFirst = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_VOLUME_ADULT_PEDIA_LOOPS);
                        rangeSecond = new Pair<>(ConstantKt.NEGATIVE_MIN_RANGE_FLOW_ADULT_PEDIA_LOOPS, ConstantKt.POSITIVE_MIN_RANGE_FLOW_ADULT_PEDIA_LOOPS);
                        break;
                }
                break;

            case TYPE_NEONAT:
                switch (chartType) {
                    case PressureVolumeChart_Type:
                        rangeFirst = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_PRESSURE_NEO_LOOPS);
                        rangeSecond = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_VOLUME_NEO_LOOPS);
                        break;

                    case FlowPressureChart_Type:
                        rangeFirst = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_PRESSURE_NEO_LOOPS);
                        rangeSecond = new Pair<>(ConstantKt.NEGATIVE_MIN_RANGE_FLOW_NEO_LOOPS, ConstantKt.POSITIVE_MIN_RANGE_FLOW_NEO_LOOPS);
                        break;

                    case FlowVolumeChart_Type:
                        rangeFirst = new Pair<>(ConstantKt.MIN_RANGE_Y_AXIS_COMMON, ConstantKt.MIN_RANGE_VOLUME_NEO_LOOPS);
                        rangeSecond = new Pair<>(ConstantKt.NEGATIVE_MIN_RANGE_FLOW_NEO_LOOPS, ConstantKt.POSITIVE_MIN_RANGE_FLOW_NEO_LOOPS);
                        break;
                }
                break;
        }

        Pair range = new Pair<>(rangeFirst, rangeSecond);
        return range;
    }


/*     static void Limiters(int minLimit ,int maxLimit) {
        switch(minLimit){
            case
        }
     }*/


    /**
     * @param ctx      provides the context access
     * @param modeCode provides the current operational mode
     * @return map of encoder constraints with parameter labels
     */


    static Map<String, ControlParameterLimit> getParameterEncoderOptions(Context ctx, final int modeCode) {

        final PreferenceManager prefManager = new PreferenceManager(ctx);

        final boolean isPediatric = prefManager != null && prefManager.readPediatricStatus();
        final boolean isNeoNatal = prefManager != null && prefManager.readNeoNatalStatus();

        float minInhaleTime = 0.0f;
        float maxInhaleTime = 0.0f;

        float minTLow = 0.0f;
        float maxTLow = 0.0f;

        float minIERatio = 0.0f;
        float maxIERatio = 0.0f;

        int minRR = 0;
        int maxRR = 0;

        int minPip = 0;
        int maxPip = 0;

        int minPeep = 0;
        int maxPeep = 0;

        int minEtPressure = 0;
        int maxEtPressure = 0;

        int minPplat = 0;
        int maxPplat = 0;

        int minFio2 = 0;
        int maxFio2 = 0;

        int minSupportPressure = 0;
        int maxSupportPressure = 0;

        int minSlope = 0;
        int maxSlope = 0;


        int minInspPause = 0;
        int maxInspPause = 0;

        int minPeepValve = 0;
        int maxPeepValve = 0;

        int minSpontVt = 0;
        int maxSpontVt = 0;

        int minTargetSpo2 = 0;
        int maxTargetSpo2 = 0;

        int minHrLimit = 0;
        int maxHrLimit = 0;

        int minTargetVolume = 0;
        int maxTargetVolume = 0;

        int minFrequency = 0;
        int maxFrequency = 0;

        int minFlow = 0;
        int maxFlow = 0;

        int minFiO2Dev = 0;
        int maxFiO2Dev = 0;

        int minTexp = 0;
        int maxTexp = 0;

        int minVti = 0;
        int maxVti = 0;

        int minPeakFlow = 0;
        int maxPeakFlow = 0;

        float minTrigFlow = 0.0f;
        float maxTrigFlow = 0.0f;

        float triggerFlowStep = 0.0f;
        int vtiStep = 0;
        int targetVolumeStep = 0;
        int targetSlope = 0;

        int minRRApnea = 0;
        int maxRRApnea = 0;

        int minTApnea = 0;
        int maxTApnea = 0;

        int minVTApnea = 0;
        int maxVTApnea = 0;


        switch (prefManager.readCurrentUid()) {

            case TYPE_PED: {

                minInhaleTime = Float.valueOf(ctx.getResources().getString(R.string.min_inhale_time_ped));
                maxInhaleTime = Float.valueOf(ctx.getResources().getString(R.string.max_inhale_time_ped));

                minTLow = Float.valueOf(ctx.getResources().getString(R.string.min_inhale_time_ped));
                maxTLow = Float.valueOf(ctx.getResources().getString(R.string.max_inhale_time_ped));

                minIERatio = Float.valueOf(ctx.getResources().getString(R.string.min_ie_ratio));
                maxIERatio = Float.valueOf(ctx.getResources().getString(R.string.max_ie_ratio));
                minRR = Integer.valueOf(ctx.getResources().getString(R.string.min_rr_ped));
                maxRR = Integer.valueOf(ctx.getResources().getString(R.string.max_rr_ped));
                minPip = Integer.valueOf(ctx.getResources().getString(R.string.min_pip_ped));
                maxPip = Integer.valueOf(ctx.getResources().getString(R.string.max_pip_ped));
                minPeep = Integer.valueOf(ctx.getResources().getString(R.string.min_peep_ped));
                maxPeep = Integer.valueOf(ctx.getResources().getString(R.string.max_peep_ped));
                minPplat = Integer.valueOf(ctx.getResources().getString(R.string.min_pplat_ped));
                minTargetSpo2 = Integer.valueOf(ctx.getResources().getString(R.string.min_target_spo2));
                targetVolumeStep = Integer.valueOf("10");

                maxTargetSpo2 = Integer.valueOf(ctx.getResources().getString(R.string.max_target_spo2));
                minHrLimit = Integer.valueOf(ctx.getResources().getString(R.string.min_hr_limit));
                maxHrLimit = Integer.valueOf(ctx.getResources().getString(R.string.max_hr_limit));
                maxPplat = Integer.valueOf(ctx.getResources().getString(R.string.max_pplat_ped));
                minFio2 = Integer.parseInt(ctx.getResources().getString(R.string.min_fio2));
                maxFio2 = Integer.parseInt(ctx.getResources().getString(R.string.max_fio2));
                minSupportPressure = Integer.parseInt(ctx.getResources().getString(R.string.min_support_pressure));
                maxSupportPressure = Integer.parseInt(ctx.getResources().getString(R.string.max_support_pressure));
                minSlope = Integer.parseInt(ctx.getResources().getString(R.string.min_slope));
                maxSlope = Integer.parseInt(ctx.getResources().getString(R.string.max_slope));
                minInspPause = Integer.parseInt(ctx.getResources().getString(R.string.min_inpiratory_pause));
                maxInspPause = Integer.parseInt(ctx.getResources().getString(R.string.max_inpiratory_pause));
                minPeepValve = Integer.parseInt(ctx.getResources().getString(R.string.min_peep_valve));
                maxPeepValve = Integer.parseInt(ctx.getResources().getString(R.string.max_peep_valve));
                minFlow = Integer.parseInt(ctx.getResources().getString(R.string.min_flow_ped));
                maxFlow = Integer.parseInt(ctx.getResources().getString(R.string.max_flow_ped));
                minFiO2Dev = Integer.parseInt(ctx.getResources().getString(R.string.min_fio2_dev));
                maxFiO2Dev = Integer.parseInt(ctx.getResources().getString(R.string.max_fio2_dev));
                minTexp = VentilatorApp.Companion.getSelectedOptions() == SELECTED_OPTIONS.NON_INVASIVE_NAME ? Integer.parseInt(ctx.getResources().getString(R.string.min_texp_non_inv)) : Integer.parseInt(ctx.getResources().getString(R.string.min_texp));
                maxTexp = Integer.parseInt(ctx.getResources().getString(R.string.max_texp));
                minVti = Integer.valueOf(ctx.getResources().getString(R.string.min_vti_ped));
                maxVti = Integer.valueOf(ctx.getResources().getString(R.string.max_vti_ped));
                minPeakFlow = Integer.valueOf(ctx.getResources().getString(R.string.min_peakflow_ped));
                maxPeakFlow = Integer.valueOf(ctx.getResources().getString(R.string.max_peakflow_ped));


                minTargetVolume = Integer.valueOf(ctx.getResources().getString(R.string.min_target_volume_ped));
                maxTargetVolume = Integer.valueOf(ctx.getResources().getString(R.string.max_target_volume_ped));

                minFrequency = Integer.valueOf(ctx.getResources().getString(R.string.min_frequency));
                maxFrequency = Integer.valueOf(ctx.getResources().getString(R.string.max_frequency));


                minTrigFlow = Float.valueOf(ctx.getResources().getString(R.string.min_trigflow_ped));
                maxTrigFlow = Float.valueOf(ctx.getResources().getString(R.string.max_trigflow_ped));

                triggerFlowStep = Float.valueOf(ctx.getResources().getString(R.string.trigger_flow_step));
                vtiStep = Integer.parseInt(ctx.getResources().getString(R.string.vt_step_normal));

                minRRApnea = Integer.parseInt(ctx.getResources().getString(R.string.min_apnea_rr));
                maxRRApnea = Integer.parseInt(ctx.getResources().getString(R.string.max_apnea_rr));


                minTApnea = Integer.parseInt(ctx.getResources().getString(R.string.min_apnea_time));
                maxTApnea = Integer.parseInt(ctx.getResources().getString(R.string.max_apnea_time));

                minVTApnea = Integer.parseInt(ctx.getResources().getString(R.string.min_apnea_vt_ped));
                maxVTApnea = Integer.parseInt(ctx.getResources().getString(R.string.max_apnea_vt_ped));

                minEtPressure = Integer.valueOf(ctx.getResources().getString(R.string.min_et_pressure_ped));
                maxEtPressure = Integer.valueOf(ctx.getResources().getString(R.string.max_et_pressure_ped));

                break;
            }

            case TYPE_ADULT: {
                minInhaleTime = Float.valueOf(ctx.getResources().getString(R.string.min_inhale_time));
                maxInhaleTime = Float.valueOf(ctx.getResources().getString(R.string.max_inhale_time));

                minTLow = Float.valueOf(ctx.getResources().getString(R.string.min_tlow));
                maxTLow = Float.valueOf(ctx.getResources().getString(R.string.max_tlow));

                minIERatio = Float.valueOf(ctx.getResources().getString(R.string.min_ie_ratio));
                maxIERatio = Float.valueOf(ctx.getResources().getString(R.string.max_ie_ratio));
                minRR = Integer.valueOf(ctx.getResources().getString(R.string.min_rr));
                targetVolumeStep = Integer.valueOf("10");
                maxRR = Integer.valueOf(ctx.getResources().getString(R.string.max_rr));
                minPip = Integer.valueOf(ctx.getResources().getString(R.string.min_pip));
                maxPip = Integer.valueOf(ctx.getResources().getString(R.string.max_pip));
                minPeep = Integer.valueOf(ctx.getResources().getString(R.string.min_peep));
                if (VentilatorApp.Companion.getSelectedOptions() == SELECTED_OPTIONS.INVASIVE_NAME) {
                    maxPeep = Integer.valueOf(ctx.getResources().getString(R.string.max_peep_invasive));
                    Log.i("CHECK_MAX_PEEP", String.valueOf(maxPeep));
                } else {

                    maxPeep = Integer.valueOf(ctx.getResources().getString(R.string.max_peep_nonInvasive));
                    Log.i("CHECK_MAX_PEEP", String.valueOf(maxPeep));
                }
//                maxPeep = Integer.valueOf(ctx.getResources().getString(R.string.max_peep));
                minPplat = Integer.valueOf(ctx.getResources().getString(R.string.min_pplat));
                maxPplat = Integer.valueOf(ctx.getResources().getString(R.string.max_pplat));
                minTargetSpo2 = Integer.valueOf(ctx.getResources().getString(R.string.min_target_spo2));

                minFrequency = Integer.valueOf(ctx.getResources().getString(R.string.min_frequency));
                maxFrequency = Integer.valueOf(ctx.getResources().getString(R.string.max_frequency));

                maxTargetSpo2 = Integer.valueOf(ctx.getResources().getString(R.string.max_target_spo2));

                minTargetVolume = Integer.valueOf(ctx.getResources().getString(R.string.min_target_volume_adult));
                maxTargetVolume = Integer.valueOf(ctx.getResources().getString(R.string.max_target_volume_adult));

                minHrLimit = Integer.valueOf(ctx.getResources().getString(R.string.min_hr_limit));
                maxHrLimit = Integer.valueOf(ctx.getResources().getString(R.string.max_hr_limit));
                minFio2 = Integer.parseInt(ctx.getResources().getString(R.string.min_fio2));
                maxFio2 = Integer.parseInt(ctx.getResources().getString(R.string.max_fio2));
                minSupportPressure = Integer.parseInt(ctx.getResources().getString(R.string.min_support_pressure));
                maxSupportPressure = Integer.parseInt(ctx.getResources().getString(R.string.max_support_pressure));
                minSlope = Integer.parseInt(ctx.getResources().getString(R.string.min_slope));
                maxSlope = Integer.parseInt(ctx.getResources().getString(R.string.max_slope));
                minInspPause = Integer.parseInt(ctx.getResources().getString(R.string.min_inpiratory_pause));
                maxInspPause = Integer.parseInt(ctx.getResources().getString(R.string.max_inpiratory_pause));
                minPeepValve = Integer.parseInt(ctx.getResources().getString(R.string.min_peep_valve));
                maxPeepValve = Integer.parseInt(ctx.getResources().getString(R.string.max_peep_valve));
                minFlow = Integer.parseInt(ctx.getResources().getString(R.string.min_flow));
                maxFlow = Integer.parseInt(ctx.getResources().getString(R.string.max_flow));
                minFiO2Dev = Integer.parseInt(ctx.getResources().getString(R.string.min_fio2_dev));
                maxFiO2Dev = Integer.parseInt(ctx.getResources().getString(R.string.max_fio2_dev));
                minTexp = Integer.parseInt(ctx.getResources().getString(R.string.min_texp));
                maxTexp = Integer.parseInt(ctx.getResources().getString(R.string.max_texp));
                minVti = Integer.valueOf(ctx.getResources().getString(R.string.min_vti));
                maxVti = Integer.valueOf(ctx.getResources().getString(R.string.max_vti));
                minPeakFlow = Integer.valueOf(ctx.getResources().getString(R.string.min_peakflow));
                maxPeakFlow = Integer.valueOf(ctx.getResources().getString(R.string.max_peakflow));

                minTrigFlow = Float.valueOf(ctx.getResources().getString(R.string.min_trigflow));
                maxTrigFlow = Float.valueOf(ctx.getResources().getString(R.string.max_trigflow));

                triggerFlowStep = Float.valueOf(ctx.getResources().getString(R.string.trigger_flow_step));
                vtiStep = Integer.parseInt(ctx.getResources().getString(R.string.vt_step_normal));

                minRRApnea = Integer.parseInt(ctx.getResources().getString(R.string.min_apnea_rr));
                maxRRApnea = Integer.parseInt(ctx.getResources().getString(R.string.max_apnea_rr));

                minTApnea = Integer.parseInt(ctx.getResources().getString(R.string.min_apnea_time));
                maxTApnea = Integer.parseInt(ctx.getResources().getString(R.string.max_apnea_time));

                minVTApnea = Integer.parseInt(ctx.getResources().getString(R.string.min_apnea_vt));
                maxVTApnea = Integer.parseInt(ctx.getResources().getString(R.string.max_apnea_vt));

                minEtPressure = Integer.valueOf(ctx.getResources().getString(R.string.min_et_pressure));
                maxEtPressure = Integer.valueOf(ctx.getResources().getString(R.string.max_et_pressure));
                break;
            }

            case TYPE_NEONAT: {
                // don't have value for neonate now
                minInhaleTime = Float.valueOf(ctx.getResources().getString(R.string.min_inhale_time_neonat));
                maxInhaleTime = Float.valueOf(ctx.getResources().getString(R.string.max_inhale_time_neonat));

                minRR = Integer.valueOf(ctx.getResources().getString(R.string.min_rr_neonate));
                maxRR = Integer.valueOf(ctx.getResources().getString(R.string.max_rr_neonate));

                minTargetVolume = Integer.valueOf(ctx.getResources().getString(R.string.min_target_volume));
                maxTargetVolume = Integer.valueOf(ctx.getResources().getString(R.string.max_target_volume));

                minPip = Integer.valueOf(ctx.getResources().getString(R.string.min_pip_neonate));
                maxPip = Integer.valueOf(ctx.getResources().getString(R.string.max_pip_neonate));

                minPeep = Integer.valueOf(ctx.getResources().getString(R.string.min_peep_neonate));
                maxPeep = Integer.valueOf(ctx.getResources().getString(R.string.max_peep_neonate));

                minPplat = Integer.valueOf(ctx.getResources().getString(R.string.min_pplat_neonate));
                maxPplat = Integer.valueOf(ctx.getResources().getString(R.string.max_pplat_neonate));

                minFrequency = Integer.valueOf(ctx.getResources().getString(R.string.min_frequency));
                maxFrequency = Integer.valueOf(ctx.getResources().getString(R.string.max_frequency));

                targetVolumeStep = Integer.valueOf("1");
                minFio2 = Integer.parseInt(ctx.getResources().getString(R.string.min_fio2_neonate));
                maxFio2 = Integer.parseInt(ctx.getResources().getString(R.string.max_fio2_neonate));

                minSupportPressure = Integer.parseInt(ctx.getResources().getString(R.string.min_support_pressure_neonate));
                maxSupportPressure = Integer.parseInt(ctx.getResources().getString(R.string.max_support_pressure_neonate));

                minSlope = Integer.parseInt(ctx.getResources().getString(R.string.min_slope));
                maxSlope = Integer.parseInt(ctx.getResources().getString(R.string.max_slope));

                minInspPause = Integer.parseInt(ctx.getResources().getString(R.string.min_inpiratory_pause));
                maxInspPause = Integer.parseInt(ctx.getResources().getString(R.string.max_inpiratory_pause));

                minVti = Integer.valueOf(ctx.getResources().getString(R.string.min_vti_neonate));
                maxVti = Integer.valueOf(ctx.getResources().getString(R.string.max_vti_neonate));

                minPeepValve = Integer.parseInt(ctx.getResources().getString(R.string.min_peep_valve));
                maxPeepValve = Integer.parseInt(ctx.getResources().getString(R.string.max_peep_valve));

                minFlow = Integer.parseInt(ctx.getResources().getString(R.string.min_flow_neo));
                maxFlow = Integer.parseInt(ctx.getResources().getString(R.string.max_flow_neo));

                minFiO2Dev = Integer.parseInt(ctx.getResources().getString(R.string.min_fio2_dev));
                maxFiO2Dev = Integer.parseInt(ctx.getResources().getString(R.string.max_fio2_dev));

                minTexp = Integer.parseInt(ctx.getResources().getString(R.string.min_texp_neonate));
                maxTexp = Integer.parseInt(ctx.getResources().getString(R.string.max_texp_neonate));

                minTrigFlow = Float.valueOf(ctx.getResources().getString(R.string.min_trigflow_neonate));
                maxTrigFlow = Float.valueOf(ctx.getResources().getString(R.string.max_trigflow_neonate));

                triggerFlowStep = Float.valueOf(ctx.getResources().getString(R.string.trigger_flow_step_neonate));
                vtiStep = Integer.parseInt(ctx.getResources().getString(R.string.vt_step_neo));


                minRRApnea = Integer.parseInt(ctx.getResources().getString(R.string.min_apnea_rr_neo));
                maxRRApnea = Integer.parseInt(ctx.getResources().getString(R.string.max_apnea_rr_neo));

                minTApnea = Integer.parseInt(ctx.getResources().getString(R.string.min_apnea_time));
                maxTApnea = Integer.parseInt(ctx.getResources().getString(R.string.max_apnea_time));

                minVTApnea = Integer.parseInt(ctx.getResources().getString(R.string.min_apnea_vt_neo));
                maxVTApnea = Integer.parseInt(ctx.getResources().getString(R.string.max_apnea_vt_neo));

                minEtPressure = Integer.valueOf(ctx.getResources().getString(R.string.min_et_pressure_neonate));
                maxEtPressure = Integer.valueOf(ctx.getResources().getString(R.string.max_et_pressure_neonate));

                break;
            }
        }


        float minTrigFlowApnea = Float.parseFloat(ctx.getResources().getString(R.string.min_apnea_trigflow));
        float maxTrigFlowApnea = Float.parseFloat(ctx.getResources().getString(R.string.max_apnea_trigflow));


        //Modified by Masoom on 30 Dec 2022
        Map<String, ControlParameterLimit> paramsScaleBindingMap = new HashMap<>();

        Log.i("valueOfminPip", String.valueOf(minPip));
        paramsScaleBindingMap.put(LBL_PIP, new ControlParameterLimit(minPip, maxPip, 1));
        Log.i("MAX_VTI", String.valueOf(maxVti));
        paramsScaleBindingMap.put(LBL_VTI, new ControlParameterLimit(minVti, maxVti, vtiStep));
        paramsScaleBindingMap.put(LBL_PEEP, new ControlParameterLimit(minPeep, maxPeep, 1));
        paramsScaleBindingMap.put(LBL_PPLAT, new ControlParameterLimit(minPplat, maxPplat, 1));
        paramsScaleBindingMap.put(LBL_RR, new ControlParameterLimit(minRR, maxRR, 1));
        paramsScaleBindingMap.put(LBL_TINSP, new ControlParameterLimit(minInhaleTime, maxInhaleTime, 0.01f));
        paramsScaleBindingMap.put(LBL_IE_RATIO, new ControlParameterLimit(minIERatio, maxIERatio, 1));  // Inhale time mapped to IE Ratio
        paramsScaleBindingMap.put(LBL_PEAK_FLOW, new ControlParameterLimit(minPeakFlow, maxPeakFlow, 1));
        paramsScaleBindingMap.put(LBL_TRIG_FLOW, new ControlParameterLimit(minTrigFlow, maxTrigFlow, triggerFlowStep));
        paramsScaleBindingMap.put(LBL_FIO2, new ControlParameterLimit(minFio2, maxFio2, 1));
        paramsScaleBindingMap.put(LBL_SUPPORT_PRESSURE, new ControlParameterLimit(minSupportPressure, maxSupportPressure, 1));
        // paramsScaleBindingMap.put(LBL_SPONT_VT, new ControlParameterLimit(minSpontVt,maxSpontVt,1));
        paramsScaleBindingMap.put(LBL_SLOPE, new ControlParameterLimit(minSlope, maxSlope, 1));
        paramsScaleBindingMap.put(LBL_INSP_PAUSE, new ControlParameterLimit(minInspPause, maxInspPause, 5));
        paramsScaleBindingMap.put(LBL_PEEP_VALVE, new ControlParameterLimit(minPeepValve, maxPeepValve, 1));
        paramsScaleBindingMap.put(LBL_TARGET_SPO2, new ControlParameterLimit(minTargetSpo2, maxTargetSpo2, 1));
        paramsScaleBindingMap.put(LBL_HR_LIMIT, new ControlParameterLimit(minHrLimit, maxHrLimit, 5));
        paramsScaleBindingMap.put(LBL_TARGET_VOLUME, new ControlParameterLimit(minTargetVolume, maxTargetVolume, targetVolumeStep));
        paramsScaleBindingMap.put(LBL_FREQUENCY, new ControlParameterLimit(minFrequency, maxFrequency, 1));
        // changing step count for flow on 03 june 2023 by masoom
        paramsScaleBindingMap.put(LBL_FLOW, new ControlParameterLimit(minFlow, maxFlow, 1));
        paramsScaleBindingMap.put(LBL_FIO2_DEV, new ControlParameterLimit(minFiO2Dev, maxFiO2Dev, 5));
        paramsScaleBindingMap.put(LBL_TLOW, new ControlParameterLimit(minTLow, maxTLow, 0.1f));
        paramsScaleBindingMap.put(LBL_TEXP, new ControlParameterLimit(minTexp, maxTexp, 5));

        paramsScaleBindingMap.put(LBL_APNEA_RR, new ControlParameterLimit(minRRApnea, maxRRApnea, 1));
        paramsScaleBindingMap.put(LBL_TAPNEA, new ControlParameterLimit(minTApnea, maxTApnea, 1));
        paramsScaleBindingMap.put(LBL_APNEA_VT, new ControlParameterLimit(minVTApnea, maxVTApnea, vtiStep));
        paramsScaleBindingMap.put(LBL_APNEA_TRIG_FLOW, new ControlParameterLimit(minTrigFlowApnea, maxTrigFlowApnea, 0.5f));

        paramsScaleBindingMap.put(LBL_ET_PRESSURE, new ControlParameterLimit(minEtPressure, maxEtPressure, 1));


        return paramsScaleBindingMap;
    }

 /*   static String getPositiveAckOf(String antiAckCode){
        if(antiAckCode != null && Mapping.ackMapping.containsKey(antiAckCode)) {
            return Mapping.ackMapping.get(antiAckCode);
        }
        return null;
    }*/

    int exp_flow_index = 0;
    int oxygen_index = 1;
    int exhale_valve_index = 2;
    int tube_comp_index = 3;
    int tube_resis_index = 4;
    int turbine_index = 5;
    int insp_flow_index = 6;
    int leak_test_index = 7;

    List<ControlParameterModel> apneaParametersTest = new ArrayList<>();

    static List<List<ControlParameterModel>> getAllControlParameterLists(Context ctx, final int modeCode) throws InvalidModeException {
        Log.i("MODECHECK", String.valueOf(modeCode));
        if (!isValidVentilatorMode(ctx, modeCode) && !isValidVentilatorMode(ctx, Integer.parseInt(FileLogger.Companion.readModeFile())))
            throw new InvalidModeException();

        PreferenceManager prefs = new PreferenceManager(ctx);

        List<ControlParameterModel> basicParameters = new ArrayList<>();
        List<ControlParameterModel> advancedParameters = new ArrayList<>();
        List<ControlParameterModel> smartFio2Parameters = new ArrayList<>();
        List<ControlParameterModel> vTasParameters = new ArrayList<>();
        List<ControlParameterModel> apneaParameters = new ArrayList<>();
        List<ControlParameterModel> etCuffParameters = new ArrayList<>();


        boolean isAIVent = modeCode == 24;
        Log.i("isAIV", String.valueOf(isAIVent));
        boolean isCmvMode = (modeCode == MODE_VCV_CMV || modeCode == MODE_PC_CMV);


        //getting value from the preference with the modecode
        Map<String, ControlParameterLimit> map = getParameterEncoderOptions(ctx, modeCode);

        Double vtiUpperLimit = null;
        Double vtiLowerLimit = null;
        Double vtiStep = null;

        Double peepUpperLimit = null;
        Double peepLowerLimit = null;
        Double peepStep = null;

        Double trigUpperLimit = null;
        Double trigLowerLimit = null;
        Double trigStep = null;

        Double pipUpperLimit = null;
        Double pipLowerLimit = null;
        Double pipStep = null;

        Double rrUpperLimit = null;
        Double rrLowerLimit = null;
        Double rrStep = null;

        Double tinspUpperLimit = null;
        Double tinspLowerLimit = null;
        Double tinspStep = null;

        Double fioUpperLimit = null;
        Double fioLowerLimit = null;
        Double fioStep = null;

        Double peakFlowUpperLimit = null;
        Double peakFlowLowerLimit = null;
        Double peakFlowStep = null;

        Double platUpperLimit = null;
        Double platLowerLimit = null;
        Double platStep = null;

        Double supportPressureUpperLimit = null;
        Double supportPressureLowerLimit = null;
        Double supportPressureStep = null;


        Double slopeUpperLimit = null;
        Double slopeLowerLimit = null;
        Double slopeStep = null;

        Double inspPauseUpperLimit = null;
        Double inspPauseLowerLimit = null;
        Double inspPauseStep = null;

        Double peepValveUpperLimit = null;
        Double peepValveLowerLimit = null;
        Double peepValveStep = null;

        Double spontVtUpperLimit = null;
        Double spontVtLowerLimit = null;
        Double spontVtStep = null;


        Double targetSpo2UpperLimit = null;
        Double targetSpo2LowerLimit = null;
        Double targetSpo2Step = null;

        Double hrLimitUpperLimit = null;
        Double hrLimitLowerLimit = null;
        Double hrLimitStep = null;

        Double targetVolumeUpperLimit = null;
        Double targetVolumeLowerLimit = null;
        Double targetVolumeStep = null;

        Double frequencyUpperLimit = null;
        Double frequencyLowerLimit = null;
        Double frequencyStep = null;

        Double flowUpperLimit = null;
        Double flowLowerLimit = null;
        Double flowStep = null;

        Double fio2DevUpperLimit = null;
        Double fio2DevLowerLimit = null;
        Double fio2DevStep = null;

        Double tLowUpperLimit = null;
        Double tLowLowerLimit = null;
        Double tLowStep = null;

        Double tExpUpperLimit = null;
        Double tExpLowerLimit = null;
        Double tExpStep = null;

        // APNEA PARAMETERS
        Double rrApneaUpperLimit = null;
        Double rrApneaLowerLimit = null;
        Double rrApneaStep = null;

        Double tApneaUpperLimit = null;
        Double tApneaLowerLimit = null;
        Double tApneaStep = null;

        Double vtApneaUpperLimit = null;
        Double vtApneaLowerLimit = null;
        Double vtApneaStep = null;

        Double trigApneaUpperLimit = null;
        Double trigApneaLowerLimit = null;
        Double trigApneaStep = null;

        Double etPressureUpperLimit = null;
        Double etPressureLowerLimit = null;
        Double etPressureStep = null;


        float vtiValue = isAIVent ? calculateVolumeForAutoVentilation(prefs.readBodyWeight()) : prefs.readVti();
        float rrValue = isAIVent ? calculateRRForAutoVentilation(vtiValue) : prefs.readRR();
        float tinspValue = isAIVent ? calculateInspTimeForAutoVentilation(vtiValue) : prefs.readTinsp();
        float pipValue = isAIVent ? calculatePIPForAutoVentilation(vtiValue) : prefs.readPip();
        boolean isPsvMode = modeCode == Configs.MODE_PC_PSV;

        for (Map.Entry<String, ControlParameterLimit> valueTile : map.entrySet()) {
            Log.i("limits", valueTile.getKey());

            switch (valueTile.getKey()) {

                case LBL_PEEP:
                    peepUpperLimit = (double) valueTile.getValue().getMaxValue();
                    peepLowerLimit = (double) valueTile.getValue().getMinValue();
                    peepStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_PPLAT:
                    platUpperLimit = (double) valueTile.getValue().getMaxValue();
                    platLowerLimit = (double) valueTile.getValue().getMinValue();
                    platStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_RR:
                    rrUpperLimit = (double) valueTile.getValue().getMaxValue();
                    rrLowerLimit = (double) valueTile.getValue().getMinValue();
                    rrStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_VTI:
                    vtiUpperLimit = (double) valueTile.getValue().getMaxValue();
                    vtiLowerLimit = (double) valueTile.getValue().getMinValue();
                    vtiStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_TRIG_FLOW:
                    trigUpperLimit = (double) valueTile.getValue().getMaxValue();
                    trigLowerLimit = (double) valueTile.getValue().getMinValue();
                    trigStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_PIP:
                    pipUpperLimit = (double) valueTile.getValue().getMaxValue();
                    pipLowerLimit = (double) valueTile.getValue().getMinValue();
                    pipStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_TINSP:
                    tinspUpperLimit = (double) valueTile.getValue().getMaxValue();
                    tinspLowerLimit = (double) valueTile.getValue().getMinValue();
                    tinspStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_FIO2:
                    fioUpperLimit = (double) valueTile.getValue().getMaxValue();
                    fioLowerLimit = (double) valueTile.getValue().getMinValue();
                    fioStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_PEAK_FLOW:
                    peakFlowUpperLimit = (double) valueTile.getValue().getMaxValue();
                    peakFlowLowerLimit = (double) valueTile.getValue().getMinValue();
                    peakFlowStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_SUPPORT_PRESSURE:
                    supportPressureUpperLimit = (double) valueTile.getValue().getMaxValue();
                    supportPressureLowerLimit = (double) valueTile.getValue().getMinValue();
                    supportPressureStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_SLOPE:
                    slopeUpperLimit = (double) valueTile.getValue().getMaxValue();
                    slopeLowerLimit = (double) valueTile.getValue().getMinValue();
                    slopeStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_INSP_PAUSE:
                    inspPauseUpperLimit = (double) valueTile.getValue().getMaxValue();
                    inspPauseLowerLimit = (double) valueTile.getValue().getMinValue();
                    inspPauseStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_PEEP_VALVE:
                    peepValveUpperLimit = (double) valueTile.getValue().getMaxValue();
                    peepValveLowerLimit = (double) valueTile.getValue().getMinValue();
                    peepValveStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_TARGET_SPO2:
                    targetSpo2UpperLimit = (double) valueTile.getValue().getMaxValue();
                    targetSpo2LowerLimit = (double) valueTile.getValue().getMinValue();
                    targetSpo2Step = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_HR_LIMIT:
                    hrLimitUpperLimit = (double) valueTile.getValue().getMaxValue();
                    hrLimitLowerLimit = (double) valueTile.getValue().getMinValue();
                    hrLimitStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_TARGET_VOLUME:
                    targetVolumeUpperLimit = (double) valueTile.getValue().getMaxValue();
                    targetVolumeLowerLimit = (double) valueTile.getValue().getMinValue();
                    targetVolumeStep = (double) valueTile.getValue().getValuePerRotation();

                    break;

                case LBL_FREQUENCY:
                    frequencyUpperLimit = (double) valueTile.getValue().getMaxValue();
                    frequencyLowerLimit = (double) valueTile.getValue().getMinValue();
                    frequencyStep = (double) valueTile.getValue().getValuePerRotation();

                    break;

                case LBL_FLOW:
                    flowUpperLimit = (double) valueTile.getValue().getMaxValue();
                    flowLowerLimit = (double) valueTile.getValue().getMinValue();
                    flowStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_FIO2_DEV:
                    fio2DevUpperLimit = (double) valueTile.getValue().getMaxValue();
                    fio2DevLowerLimit = (double) valueTile.getValue().getMinValue();
                    fio2DevStep = (double) valueTile.getValue().getValuePerRotation();
                    break;


                case LBL_TLOW:
                    tLowUpperLimit = (double) valueTile.getValue().getMaxValue();
                    tLowLowerLimit = (double) valueTile.getValue().getMinValue();
                    tLowStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_TEXP:
                    tExpUpperLimit = (double) valueTile.getValue().getMaxValue();
                    tExpLowerLimit = (double) valueTile.getValue().getMinValue();
                    tExpStep = (double) valueTile.getValue().getValuePerRotation();
                    break;

                case LBL_APNEA_RR:
                    rrApneaUpperLimit = (double) valueTile.getValue().getMaxValue();
                    rrApneaLowerLimit = (double) valueTile.getValue().getMinValue();
                    rrApneaStep = (double) valueTile.getValue().getValuePerRotation();
                    Log.i("BACKUPCHECK", "Step = " + rrApneaStep + " | Max = " + rrApneaUpperLimit + " | Min = " + rrApneaLowerLimit);

                    break;


                case LBL_TAPNEA:
                    tApneaUpperLimit = (double) valueTile.getValue().getMaxValue();
                    tApneaLowerLimit = (double) valueTile.getValue().getMinValue();
                    tApneaStep = (double) valueTile.getValue().getValuePerRotation();
                    Log.i("BACKUPCHECK", "Step = " + tApneaStep + " | Max = " + tApneaUpperLimit + " | Min = " + tApneaLowerLimit);

                    break;

                case LBL_APNEA_VT:
                    vtApneaUpperLimit = (double) valueTile.getValue().getMaxValue();
                    vtApneaLowerLimit = (double) valueTile.getValue().getMinValue();
                    vtApneaStep = (double) valueTile.getValue().getValuePerRotation();
                    Log.i("BACKUPCHECK", "Step = " + vtApneaStep + " | Max = " + vtApneaUpperLimit + " | Min = " + vtApneaLowerLimit);

                    break;

                case LBL_APNEA_TRIG_FLOW:
                    trigApneaUpperLimit = (double) valueTile.getValue().getMaxValue();
                    trigApneaLowerLimit = (double) valueTile.getValue().getMinValue();
                    trigApneaStep = (double) valueTile.getValue().getValuePerRotation();
                    Log.i("BACKUPCHECK", "Step = " + trigApneaStep + " | Max = " + trigApneaUpperLimit + " | Min = " + trigApneaLowerLimit);
                    break;

                case LBL_ET_PRESSURE:
                    etPressureUpperLimit = (double) valueTile.getValue().getMaxValue();
                    etPressureLowerLimit = (double) valueTile.getValue().getMinValue();
                    etPressureStep = (double) valueTile.getValue().getValuePerRotation();
                    Log.i("BACKUPCHECK", "Step = " + etPressureStep + " | Max = " + etPressureUpperLimit + " | Min = " + etPressureLowerLimit);

                    break;


            }

        }


        ControlParameterModel peep = new ControlParameterModel(
                LBL_PEEP,
                ctx.getResources().getString(R.string.peep),
                supportPrecision(LBL_PEEP, prefs.readPEEP()),
                ctx.getResources().getString(R.string.hint_cmH2o),
                peepUpperLimit,
                peepLowerLimit,
                peepStep
        );
        basicParameters.add(peep);

        ControlParameterModel pPlat = new ControlParameterModel(
                LBL_PPLAT,
                isPsvMode ? ctx.getResources().getString(R.string.support_pressure) : ctx.getResources().getString(R.string.pinsp),
                supportPrecision(LBL_PPLAT, prefs.readPplat()),
                ctx.getResources().getString(R.string.hint_cmH2o),
                platUpperLimit,
                platLowerLimit,
                platStep
        );
        basicParameters.add(pPlat);
        advancedParameters.add(pPlat);

        ControlParameterModel rr = new ControlParameterModel(
                LBL_RR,
                ctx.getResources().getString(R.string.respiratory_rate),
                supportPrecision(LBL_RR, rrValue),
                ctx.getResources().getString(R.string.hint_bpm),
                rrUpperLimit,
                rrLowerLimit,
                rrStep
        );
        basicParameters.add(rr);

        ControlParameterModel vti = new ControlParameterModel(
                LBL_VTI,
                ctx.getResources().getString(R.string.vti),
                supportPrecision(LBL_VTI, vtiValue),
                ctx.getResources().getString(R.string.hint_ml),
                vtiUpperLimit,
                vtiLowerLimit,
                vtiStep
        );
        basicParameters.add(vti);

        ControlParameterModel triggerFlow = new ControlParameterModel(
                LBL_TRIG_FLOW,
                ctx.getResources().getString(R.string.trigger_flow),
                String.valueOf(prefs.readTrigFlow()),
                ctx.getResources().getString(R.string.hint_l_min),
                trigUpperLimit,
                trigLowerLimit,
                trigStep
        );
        basicParameters.add(triggerFlow);


        ControlParameterModel pip = new ControlParameterModel(
                LBL_PIP,
                ctx.getResources().getString(R.string.plimit),
                supportPrecision(LBL_PIP, pipValue),
                ctx.getResources().getString(R.string.hint_cmH2o),
                pipUpperLimit,
                pipLowerLimit,
                pipStep
        );
        advancedParameters.add(pip);
        vTasParameters.add(pip);


        ControlParameterModel tInsp = new ControlParameterModel(
                LBL_TINSP,
                ctx.getResources().getString(R.string.inhale_time),
                String.valueOf(tinspValue),
                ctx.getResources().getString(R.string.hint_sec),
                tinspUpperLimit,
                tinspLowerLimit,
                tinspStep
        );
        basicParameters.add(tInsp);


        ControlParameterModel fio2 = new ControlParameterModel(
                LBL_FIO2,
                ctx.getResources().getString(R.string.fio2),
                supportPrecision(LBL_FIO2, prefs.readFiO2()),
                ctx.getResources().getString(R.string.hint_percentage),
                fioUpperLimit,
                fioLowerLimit,
                fioStep
        );
        basicParameters.add(fio2);


        ControlParameterModel supportPressure = new ControlParameterModel(
                LBL_SUPPORT_PRESSURE,
                ctx.getResources().getString(R.string.support_pressure),
                supportPrecision(LBL_SUPPORT_PRESSURE, prefs.readSupportPressure()),
                ctx.getResources().getString(R.string.hint_cmH2o),
                supportPressureUpperLimit,
                supportPressureLowerLimit,
                supportPressureStep
        );
        basicParameters.add(supportPressure);


        ControlParameterModel slope = new ControlParameterModel(
                LBL_SLOPE,
                ctx.getResources().getString(R.string.slope),
                supportPrecision(LBL_SLOPE, prefs.readSlope()),
                "",
                slopeUpperLimit,
                slopeLowerLimit,
                slopeStep
        );
        basicParameters.add(slope);
        advancedParameters.add(slope);

        ControlParameterModel inspPause = new ControlParameterModel(
                LBL_INSP_PAUSE,
                ctx.getResources().getString(R.string.inspiratory_pause),
                supportPrecision(LBL_INSP_PAUSE, prefs.readInspiratoryPause()),
                ctx.getResources().getString(R.string.hint_percentage),
                inspPauseUpperLimit,
                inspPauseLowerLimit,
                inspPauseStep
        );
        advancedParameters.add(inspPause);

        ControlParameterModel peepValve = new ControlParameterModel(
                LBL_PEEP_VALVE,
                ctx.getResources().getString(R.string.peep_valve),
                supportPrecision(LBL_PEEP_VALVE, prefs.readPeepValve()),
                ctx.getResources().getString(R.string.hint_percentage),
                peepValveUpperLimit,
                peepValveLowerLimit,
                peepValveStep
        );
        advancedParameters.add(peepValve);
      /*  ControlParameterModel spontVt = new ControlParameterModel(
                LBL_SPONT_VT,
                ctx.getResources().getString(R.string.spont_vt),
                supportPrecision(LBL_SPONT_VT,prefs.readSpontVT()),
        ctx.getResources().getString(R.string.hint_ml),
        spontVtUpperLimit,
        spontVtLowerLimit,
        spontVtStep
        );*/
        // basicParameters.add(spontVt);

        ControlParameterModel targetSpo2 = new ControlParameterModel(
                LBL_TARGET_SPO2,
                ctx.getResources().getString(R.string.target_spo2),
                supportPrecision(LBL_TARGET_SPO2, prefs.readTargetSpo2()),
                ctx.getResources().getString(R.string.hint_percentage),
                targetSpo2UpperLimit,
                targetSpo2LowerLimit,
                targetSpo2Step
        );
        smartFio2Parameters.add(targetSpo2);

        ControlParameterModel hrLimit = new ControlParameterModel(
                LBL_HR_LIMIT,
                ctx.getResources().getString(R.string.hr_Limit),
                supportPrecision(LBL_HR_LIMIT, prefs.readHrLimit()),
                ctx.getResources().getString(R.string.hint_bpm),
                hrLimitUpperLimit,
                hrLimitLowerLimit,
                hrLimitStep
        );
        smartFio2Parameters.add(hrLimit);

        ControlParameterModel targetVolume = new ControlParameterModel(
                LBL_TARGET_VOLUME,
                ctx.getResources().getString(R.string.target_volume),
                String.valueOf(prefs.readTargetVolume()),
                ctx.getResources().getString(R.string.hint_ml),
                targetVolumeUpperLimit,
                targetVolumeLowerLimit,
                targetVolumeStep
        );
        vTasParameters.add(targetVolume);
        basicParameters.add(targetVolume);

        ControlParameterModel frequency = new ControlParameterModel(
                LBL_FREQUENCY,
                ctx.getResources().getString(R.string.frequency),
                String.valueOf(prefs.readFrequency()),
                ctx.getResources().getString(R.string.hint_hz),
                frequencyUpperLimit,
                frequencyLowerLimit,
                frequencyStep
        );
        advancedParameters.add(frequency);

        ControlParameterModel flow = new ControlParameterModel(
                LBL_FLOW,
                ctx.getResources().getString(R.string.flow),
                supportPrecision(LBL_FLOW, prefs.readFlow()),
                ctx.getResources().getString(R.string.hint_l_min),
                flowUpperLimit,
                flowLowerLimit,
                flowStep
        );
        basicParameters.add(flow);

        ControlParameterModel fio2Dev = new ControlParameterModel(
                LBL_FIO2_DEV,
                ctx.getResources().getString(R.string.fio2_dev),
                String.valueOf(prefs.readFiO2Dev()),
                "%",
                fio2DevUpperLimit,
                fio2DevLowerLimit,
                fio2DevStep
        );
        smartFio2Parameters.add(fio2Dev);


        ControlParameterModel tLow = new ControlParameterModel(
                LBL_TLOW,
                ctx.getResources().getString(R.string.tLow),
                String.valueOf(prefs.readTlow()),
                ctx.getResources().getString(R.string.hint_sec),
                tLowUpperLimit,
                tLowLowerLimit,
                tLowStep
        );
        basicParameters.add(tLow);

        ControlParameterModel tExp = new ControlParameterModel(
                LBL_TEXP,
                ctx.getResources().getString(R.string.tExp),
                supportPrecision(LBL_TEXP, prefs.readTexp()),
                ctx.getResources().getString(R.string.hint_percentage),
                tExpUpperLimit,
                tExpLowerLimit,
                tExpStep
        );
//        basicParameters.add(tExp);
        advancedParameters.add(tExp);


        // APNEA PARAMETERS
        ControlParameterModel rrApnea = new ControlParameterModel(
                LBL_APNEA_RR,
                ctx.getResources().getString(R.string.apnea_respiratory_rate),
                supportPrecision(LBL_APNEA_RR, prefs.readRRApnea()),
                ctx.getResources().getString(R.string.hint_bpm),
                rrApneaUpperLimit,
                rrApneaLowerLimit,
                rrApneaStep
        );
        apneaParameters.add(rrApnea);

        ControlParameterModel tApnea = new ControlParameterModel(
                LBL_TAPNEA,
                ctx.getResources().getString(R.string.apnea_time),
                String.valueOf(prefs.readTApnea().intValue()),
                ctx.getResources().getString(R.string.hint_sec),
                tApneaUpperLimit,
                tApneaLowerLimit,
                tApneaStep
        );
        apneaParameters.add(tApnea);

        ControlParameterModel vtApnea = new ControlParameterModel(
                LBL_APNEA_VT,
                ctx.getResources().getString(R.string.apnea_vt),
                supportPrecision(LBL_APNEA_VT, prefs.readVtApnea()),
                ctx.getResources().getString(R.string.hint_ml),
                vtApneaUpperLimit,
                vtApneaLowerLimit,
                vtApneaStep
        );
        apneaParameters.add(vtApnea);

        ControlParameterModel triggerFlowApnea = new ControlParameterModel(
                LBL_APNEA_TRIG_FLOW,
                ctx.getResources().getString(R.string.apnea_trigger_flow),
                String.valueOf(prefs.readTrigFlowApnea()),
                ctx.getResources().getString(R.string.hint_l_min),
                trigApneaUpperLimit,
                trigApneaLowerLimit,
                trigApneaStep
        );
        apneaParameters.add(triggerFlowApnea);

        ControlParameterModel etPressure = new ControlParameterModel(
                LBL_ET_PRESSURE,
                ctx.getResources().getString(R.string.et_pressure),
                String.valueOf(prefs.readEtPressure()),
                ctx.getResources().getString(R.string.hint_cmH2o),
                etPressureUpperLimit,
                etPressureLowerLimit,
                etPressureStep
        );
        etCuffParameters.add(etPressure);

        // boolean isBPAPMode = (modeCode == MODE_NIV_BPAP);
        List<List<ControlParameterModel>> parameters = new ArrayList<>();
        parameters.add(basicParameters);
        parameters.add(advancedParameters);
        parameters.add(apneaParameters);
        parameters.add(smartFio2Parameters);
        parameters.add(vTasParameters);
        parameters.add(etCuffParameters);
        return parameters;

    }


    static String supportPrecision(String lbl, String value) {
        try {
            return supportPrecision(lbl, Float.parseFloat(value));
        } catch (Exception e) {
            return value;
        }
    }

    static void customFifoCapacity(Boolean isFirstTime, int xAxis, int[] testingArray) {
        if (isFirstTime) {
            for (int i = 0; i < FIFOCAPACITY_CUSTOM_SIZE; i++) {

                if (i < FIFOCAPACITY_CUSTOM_HALF) {
                    // if fifocapacity at 1 after 350
                    if ((xAxis - (FIFOCAPACITY_CUSTOM_HALF - i)) < 0)
                        testingArray[i] = GRAPH_THRESHOLD + (xAxis - (FIFOCAPACITY_CUSTOM_HALF - i));

                        // if graph creates in normal flow
                    else testingArray[i] = (xAxis - (FIFOCAPACITY_CUSTOM_HALF - i));
                } else {

                    testingArray[i] = (xAxis + (i - FIFOCAPACITY_CUSTOM_HALF));
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    static String supportPrecision(String lbl, Float value) {
        if (isDecimalSupported(lbl)) {
            return String.format("%.1f", value);
        } else return String.valueOf(value.intValue());
    }

    // Modified by Masoom on 30 Dec 2022
    static Map<ControlSettingType, List<ControlParameterModel>> filterControlParameterViaMode(Context ctx, final int modeCode, List<ControlParameterModel> allParameters) throws InvalidModeException {


        final PreferenceManager prefManager = new PreferenceManager(ctx);

        if (!isValidVentilatorMode(ctx, modeCode) && !isValidVentilatorMode(ctx, Integer.parseInt(FileLogger.Companion.readModeFile())))
            throw new InvalidModeException();
        boolean isNonApneaMode = ((modeCode == MODE_PC_CMV || modeCode == MODE_PC_SIMV) || modeCode == MODE_PC_AC);

        ControlParameterModel peep = null;
        ControlParameterModel triggerFlow = null;
        ControlParameterModel triggerFlowApnea = null;
        ControlParameterModel pPlat = null;
        ControlParameterModel vti = null;
        ControlParameterModel vtApnea = null;
        ControlParameterModel pip = null;
        ControlParameterModel rr = null;
        ControlParameterModel rrApnea = null;
        ControlParameterModel tInsp = null;
        ControlParameterModel tApnea = null;
        ControlParameterModel fio2 = null;
        ControlParameterModel supportPressure = null;
        ControlParameterModel slope = null;
        ControlParameterModel etPressure = null;
        ControlParameterModel insp_Pause = null;
        ControlParameterModel peepValve = null;
        ControlParameterModel spontVt = null;
        ControlParameterModel targetSpo2 = null;
        ControlParameterModel hrLimit = null;
        ControlParameterModel targetVolume = null;
        ControlParameterModel frequency = null;
        ControlParameterModel flow = null;
        ControlParameterModel fio2_dev = null;
        ControlParameterModel tLow = null;
        ControlParameterModel tExp = null;


        for (ControlParameterModel param : allParameters) {
            switch (param.getVentKey()) {

                case LBL_ET_PRESSURE:
                    etPressure = param;
                    break;

                case LBL_PEEP:
                    peep = param;
                    break;

                case LBL_TRIG_FLOW:
                    triggerFlow = param;
                    break;

                case LBL_PPLAT:
                    pPlat = param;
                    break;

                case LBL_VTI:
                    vti = param;
                    break;

                case LBL_PIP:
                    pip = param;
                    break;

                case LBL_RR:
                    rr = param;
                    break;

                case LBL_TINSP:
                    tInsp = param;
                    break;

                case LBL_FIO2:
                    fio2 = param;
                    break;

                case LBL_SUPPORT_PRESSURE:
                    supportPressure = param;
                    break;

                case LBL_SLOPE:
                    slope = param;
                    break;

                case LBL_INSP_PAUSE:
                    insp_Pause = param;
                    break;

                case LBL_PEEP_VALVE:
                    peepValve = param;
                    break;

             /*   case LBL_SPONT_VT:
                    spontVt = param;
                    break;*/

                case LBL_TARGET_SPO2:
                    targetSpo2 = param;
                    break;

                case LBL_HR_LIMIT:
                    hrLimit = param;
                    break;

                case LBL_TARGET_VOLUME:
                    targetVolume = param;
                    break;

                case LBL_FREQUENCY:
                    frequency = param;
                    break;

                case LBL_FLOW:
                    flow = param;
                    break;

                case LBL_FIO2_DEV:
                    fio2_dev = param;
                    break;

                case LBL_TLOW:
                    tLow = param;
                    break;

                case LBL_TEXP:
                    tExp = param;
                    break;

                case LBL_APNEA_RR:
                    Log.i("value_baclup1", "sarasd");
                    rrApnea = param;
                    break;

                case LBL_TAPNEA:
                    tApnea = param;
                    break;

                case LBL_APNEA_VT:
                    vtApnea = param;
                    break;

                case LBL_APNEA_TRIG_FLOW:
                    triggerFlowApnea = param;
                    break;

            }
        }

        List<ControlParameterModel> basicParameters = new ArrayList<>();

        List<ControlParameterModel> advancedParameters = new ArrayList<>();

        List<ControlParameterModel> apneaParameters = new ArrayList<>();
        List<ControlParameterModel> smartFiO2Parameters = new ArrayList<>();
        List<ControlParameterModel> vTasParameters = new ArrayList<>();
        List<ControlParameterModel> etCuffParamters = new ArrayList<>();

        //apneaParameters.add(triggerFlowApnea);

        switch (modeCode) {
            case MODE_VCV_CMV:

                switch (prefManager.readCurrentUid()) {

                    case TYPE_ADULT:
                    case TYPE_PED:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (vti != null) basicParameters.add(vti);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (pip != null) advancedParameters.add(pip);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
//                        if (insp_Pause != null) advancedParameters.add(insp_Pause);
                        //if(pip != null) basicParameters.add(pip);
                        //  if (peepValve != null) advancedParameters.add(peepValve);
                        if (rr != null) basicParameters.add(rr);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (peep != null) basicParameters.add(peep);
//                        if (etPressure != null) etCuffParamters.add(etPressure);
                        break;
                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (vti != null) basicParameters.add(vti);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (pip != null) advancedParameters.add(pip);
                        //if (insp_Pause != null) advancedParameters.add(insp_Pause);
                        //if(pip != null) basicParameters.add(pip);
//                        if(frequency != null) advancedParameters.add(frequency);
                        // if (peepValve != null) advancedParameters.add(peepValve);
                        if (rr != null) basicParameters.add(rr);
                        // if (fio2_dev != null) advancedParameters.add(fio2_dev);
                        if (peep != null) basicParameters.add(peep);
//                        if (etPressure != null) etCuffParamters.add(etPressure);
                }
                break;


            case MODE_VCV_ACV:
                switch (prefManager.readCurrentUid()) {
                    case TYPE_ADULT:
                    case TYPE_PED:

                        if (fio2 != null) basicParameters.add(fio2);
                        if (vti != null) basicParameters.add(vti);
                        if (tInsp != null) basicParameters.add(tInsp);
                        // if(pip != null) basicParameters.add(pip);
                        if (pip != null) advancedParameters.add(pip);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
//                        if (insp_Pause != null) advancedParameters.add(insp_Pause);
                        //  if (peepValve != null) advancedParameters.add(peepValve);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (rr != null) basicParameters.add(rr);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        //if (triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        break;
                    case TYPE_NEONAT:

                        if (fio2 != null) basicParameters.add(fio2);
                        if (vti != null) basicParameters.add(vti);
                        if (tInsp != null) basicParameters.add(tInsp);
                        // if(pip != null) basicParameters.add(pip);
                        if (pip != null) advancedParameters.add(pip);
//                        if(frequency != null) advancedParameters.add(frequency);
                        //  if (insp_Pause != null) advancedParameters.add(insp_Pause);
                        //   if (peepValve != null) advancedParameters.add(peepValve);
                        //if (fio2_dev != null) advancedParameters.add(fio2_dev);
                        if (rr != null) basicParameters.add(rr);

                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        //if (triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        break;
                }
                break;


            case MODE_AUTO_VENTILATION:
                switch (prefManager.readCurrentUid()) {

                    case TYPE_ADULT:
                    case TYPE_PED:

                        if (fio2 != null) basicParameters.add(fio2);
                        if (vti != null) basicParameters.add(vti);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        // if (peepValve != null) advancedParameters.add(peepValve);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        if (pip != null) advancedParameters.add(pip);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
//                        if (insp_Pause != null) advancedParameters.add(insp_Pause);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        //if(triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
                        if (supportPressure != null) basicParameters.add(supportPressure);
                        // if (slope != null) basicParameters.add(slope);
//                        if (slope != null) advancedParameters.add(slope);
//                        if (etPressure != null) etCuffParamters.add(etPressure);
                        //if(tExp != null) basicParameters.add(tExp);
                      /*  apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);*/

                        if (tExp != null) advancedParameters.add(tExp);
                        break;
                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (vti != null) basicParameters.add(vti);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        //   if (peepValve != null) advancedParameters.add(peepValve);
                        //if (fio2_dev != null) advancedParameters.add(fio2_dev);
                        if (pip != null) advancedParameters.add(pip);
//                        if(frequency != null) advancedParameters.add(frequency);
                        //  if (insp_Pause != null) advancedParameters.add(insp_Pause);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        if (supportPressure != null) basicParameters.add(supportPressure);
                        //if(triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
//                        if (etPressure != null) etCuffParamters.add(etPressure);
                        // if (slope != null) basicParameters.add(slope);
                        // if (slope != null) advancedParameters.add(slope);
                        //if(tExp != null) basicParameters.add(tExp);

                        if (tExp != null) advancedParameters.add(tExp);
                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;
                }
                break;

            case MODE_NEONAT_VC_SIMV:
            case MODE_VCV_SIMV:

                switch (prefManager.readCurrentUid()) {

                    case TYPE_ADULT:
                    case TYPE_PED:

                        if (fio2 != null) basicParameters.add(fio2);
                        if (vti != null) basicParameters.add(vti);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        // if (peepValve != null) advancedParameters.add(peepValve);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        if (pip != null) advancedParameters.add(pip);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
//                        if (insp_Pause != null) advancedParameters.add(insp_Pause);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        //if(triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
                        if (supportPressure != null) basicParameters.add(supportPressure);
                        // if (slope != null) basicParameters.add(slope);
                        if (slope != null) advancedParameters.add(slope);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);

                        if (tExp != null) advancedParameters.add(tExp);
                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (vti != null) basicParameters.add(vti);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
//                        if(frequency != null) advancedParameters.add(frequency);
                        // if (peepValve != null) advancedParameters.add(peepValve);
                        //if (fio2_dev != null) advancedParameters.add(fio2_dev);
                        if (pip != null) advancedParameters.add(pip);
//                        if (pip != null) vTasParameters.add(pip);
                        //  if (insp_Pause != null) advancedParameters.add(insp_Pause);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        //if(triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
                        if (supportPressure != null) basicParameters.add(supportPressure);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        // if (slope != null) basicParameters.add(slope);
                        // if (slope != null) advancedParameters.add(slope);
                        //if(tExp != null) basicParameters.add(tExp);

                        if (tExp != null) advancedParameters.add(tExp);
                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;
                }
                break;


            case MODE_PC_CMV:


                switch (prefManager.readCurrentUid()) {

                    case TYPE_PED:

                    case TYPE_ADULT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        if (pip != null) advancedParameters.add(pip);
                        if (peep != null) basicParameters.add(peep);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
//                        if(frequency != null) advancedParameters.add(frequency);
                        // if (peepValve != null) advancedParameters.add(peepValve);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        if (targetVolume != null) vTasParameters.add(targetVolume);
                        //  if (pip != null) advancedParameters.add(pip);
                        if (pip != null) vTasParameters.add(pip);
                        if (slope != null) advancedParameters.add(slope);
                        if (pPlat != null) basicParameters.add(pPlat);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        if (pPlat != null) basicParameters.add(pPlat);
                        // if (pip != null) advancedParameters.add(pip);
                        // if (pPlat != null) advancedParameters.add(pPlat);
                        if (peep != null) basicParameters.add(peep);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (targetVolume != null) vTasParameters.add(targetVolume);
                        if (pip != null) advancedParameters.add(pip);
                        if (pip != null) vTasParameters.add(pip);
//                        if (etPressure != null) etCuffParamters.add(etPressure);
                        // if (triggerFlow != null) basicParameters.add(triggerFlow);
                        //  if (slope != null) advancedParameters.add(slope);
                        //if (tExp != null) advancedParameters.add(tExp);

                        break;
                }

                break;


            case MODE_PC_PRVC:

                switch (prefManager.readCurrentUid()) {

                    case TYPE_PED:

                    case TYPE_ADULT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        // if(triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
                        if (targetVolume != null) basicParameters.add(targetVolume);
                        if (supportPressure != null) basicParameters.add(supportPressure);
                        // if (slope != null) basicParameters.add(slope);
                        //  if (peepValve != null) advancedParameters.add(peepValve);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        if (slope != null) advancedParameters.add(slope);
                        // if(pip != null) basicParameters.add(pip);
                        if (pip != null) advancedParameters.add(pip);
//                        if (pip != null) vTasParameters.add(pip);
                        if (pPlat != null) basicParameters.add(pPlat);
                        // if(tExp != null) basicParameters.add(tExp);
                        if (tExp != null) advancedParameters.add(tExp);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

//                        apneaParameters.add(rrApnea);
//                        apneaParameters.add(tApnea);
//                        apneaParameters.add(vtApnea);
                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        // if (slope != null) advancedParameters.add(slope);
                        if (tExp != null) advancedParameters.add(tExp);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (targetVolume != null) vTasParameters.add(targetVolume);
                        if (pip != null) advancedParameters.add(pip);
                        if (supportPressure != null) basicParameters.add(supportPressure);
                        if (pPlat != null) basicParameters.add(pPlat);
                        if (peep != null) basicParameters.add(peep);
                        //if (pPlat != null) advancedParameters.add(pPlat);
                        if (pip != null) vTasParameters.add(pip);

                        if (triggerFlow != null) basicParameters.add(triggerFlow);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;
                }


                Log.i("ADVANCE_CHECK", advancedParameters.toString());
                break;

            case MODE_PC_SIMV:

                switch (prefManager.readCurrentUid()) {

                    case TYPE_PED:

                    case TYPE_ADULT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        // if(triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
                        if (targetVolume != null) vTasParameters.add(targetVolume);
                        if (supportPressure != null) basicParameters.add(supportPressure);
//                         if (slope != null) basicParameters.add(slope);
                        //  if (peepValve != null) advancedParameters.add(peepValve);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        if (slope != null) advancedParameters.add(slope);

                        // if(pip != null) basicParameters.add(pip);
                        if (pip != null) advancedParameters.add(pip);
                        if (pip != null) vTasParameters.add(pip);


                        if (pPlat != null) basicParameters.add(pPlat);
                        // if(tExp != null) basicParameters.add(tExp);
                        if (tExp != null) advancedParameters.add(tExp);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        // if (slope != null) advancedParameters.add(slope);
                        if (tExp != null) advancedParameters.add(tExp);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (targetVolume != null) vTasParameters.add(targetVolume);
                        if (pip != null) advancedParameters.add(pip);
                        if (supportPressure != null) basicParameters.add(supportPressure);
                        if (pPlat != null) basicParameters.add(pPlat);
                        if (peep != null) basicParameters.add(peep);
                        //if (pPlat != null) advancedParameters.add(pPlat);
                        if (pip != null) vTasParameters.add(pip);

                        if (triggerFlow != null) basicParameters.add(triggerFlow);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;
                }


                Log.i("ADVANCE_CHECK", advancedParameters.toString());
                break;


            case MODE_PC_AC:

                switch (prefManager.readCurrentUid()) {

                    case TYPE_PED:

                    case TYPE_ADULT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (targetVolume != null) vTasParameters.add(targetVolume);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (pip != null) advancedParameters.add(pip);
                        if (pip != null) vTasParameters.add(pip);

                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
                        // if (peepValve != null) advancedParameters.add(peepValve);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        //  if (pip != null) advancedParameters.add(pip);
                        //  if (supportPressure != null) basicParameters.add(supportPressure);
                        if (slope != null) advancedParameters.add(slope);
                        if (pPlat != null) basicParameters.add(pPlat);
//                        if (etPressure != null) etCuffParamters.add(etPressure);


               /*         apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);*/
                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
//                        if (supportPressure != null) basicParameters.add(supportPressure);
                        if (pPlat != null) basicParameters.add(pPlat);
                        if (pip != null) advancedParameters.add(pip);
                        //  if (slope != null) advancedParameters.add(slope);
                        if (pip != null) vTasParameters.add(pip);
                        // if (pip != null) advancedParameters.add(pip);
//                        if(frequency != null) advancedParameters.add(frequency);
                        //if (pPlat != null) advancedParameters.add(pPlat);
                        if (targetVolume != null) vTasParameters.add(targetVolume);
                        if (tExp != null) advancedParameters.add(tExp);
                        if (peep != null) basicParameters.add(peep);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        break;
                }


                break;


            case MODE_PC_PSV:

                switch (prefManager.readCurrentUid()) {

                    case TYPE_PED:

                    case TYPE_ADULT:

                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        // if(triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
                        if (pip != null) vTasParameters.add(pip);
//                        if(frequency != null) advancedParameters.add(frequency);
                        // if (peepValve != null) advancedParameters.add(peepValve);
                        if (targetVolume != null) vTasParameters.add(targetVolume);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        if (slope != null) advancedParameters.add(slope);
//                         if(pip != null) basicParameters.add(pip);
                        if (pip != null) advancedParameters.add(pip);
                        if (pPlat != null) basicParameters.add(pPlat);
                        // if(tExp != null) basicParameters.add(tExp);
                        if (tExp != null) advancedParameters.add(tExp);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        if (pPlat != null) basicParameters.add(pPlat);
                        if (pip != null) vTasParameters.add(pip);
                        if (targetVolume != null) vTasParameters.add(targetVolume);
                        if (peep != null) basicParameters.add(peep);
                        //if (pPlat != null) advancedParameters.add(pPlat);
                        //   if (slope != null) advancedParameters.add(slope);
                        if (pip != null) advancedParameters.add(pip);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (tExp != null) advancedParameters.add(tExp);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;
                }

                break;


            case MODE_HFNC:

                switch (prefManager.readCurrentUid()) {

                    case TYPE_PED:
                    case TYPE_NEONAT:
                    case TYPE_ADULT:

                        if (fio2 != null) basicParameters.add(fio2);
                        // if (tInsp != null) basicParameters.add(tInsp);
                        // if (rr != null) basicParameters.add(rr);
                        //  if (slope != null) advancedParameters.add(slope);
                        //    if (tExp != null) advancedParameters.add(tExp);
                        //  if (targetVolume != null) vTasParameters.add(targetVolume);
                        //   if (supportPressure != null) basicParameters.add(supportPressure);
                        //  if (pPlat != null) basicParameters.add(pPlat);
                        if (flow != null) basicParameters.add(flow);

                        // if (peep != null) basicParameters.add(peep);
                        //if (triggerFlow != null) basicParameters.add(triggerFlow);
                        break;


                }

                break;


            case MODE_NIV_BPAP:
                switch (prefManager.readCurrentUid()) {

                    case TYPE_PED:

                    case TYPE_ADULT:

                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        if (peep != null) basicParameters.add(peep);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        if (slope != null) advancedParameters.add(slope);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        //if (triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (pip != null) advancedParameters.add(pip);
                        //  if (peepValve != null) advancedParameters.add(peepValve);

                        if (tExp != null) advancedParameters.add(tExp);
                        if (supportPressure != null) basicParameters.add(supportPressure);
                        if (pPlat != null) basicParameters.add(pPlat);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (peep != null) basicParameters.add(peep);
                        if (supportPressure != null) basicParameters.add(supportPressure);
//                        if (slope != null) advancedParameters.add(slope);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (tExp != null) advancedParameters.add(tExp);
                        if (pip != null) advancedParameters.add(pip);
                        if (pPlat != null) basicParameters.add(pPlat);
                        if (rr != null) basicParameters.add(rr);
//                        if (triggerFlow != null) basicParameters.add(triggerFlow);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;
                }
                break;


            case MODE_NC_IPPV:
                switch (prefManager.readCurrentUid()) {

                    case TYPE_PED:

                    case TYPE_ADULT:

                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (rr != null) basicParameters.add(rr);
                        if (peep != null) basicParameters.add(peep);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        if (slope != null) advancedParameters.add(slope);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);

                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        //if (triggerFlow != null) advancedParameters.add(triggerFlow);
                        if (pip != null) advancedParameters.add(pip);
                        //  if (peepValve != null) advancedParameters.add(peepValve);
                        if (tExp != null) advancedParameters.add(tExp);
                        if (supportPressure != null) basicParameters.add(supportPressure);
                        if (pPlat != null) basicParameters.add(pPlat);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
                        if (peep != null) basicParameters.add(peep);
//                        if (slope != null) advancedParameters.add(slope);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (tExp != null) advancedParameters.add(tExp);
                        if (pip != null) advancedParameters.add(pip);
                        if (pPlat != null) basicParameters.add(pPlat);
                        if (rr != null) basicParameters.add(rr);
                        if (supportPressure != null) basicParameters.add(supportPressure);

//                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;
                }
                break;


            case MODE_PC_ARPV:

                switch (prefManager.readCurrentUid()) {

                    case TYPE_PED:

                    case TYPE_ADULT:

                        if (fio2 != null) basicParameters.add(fio2);
                        if (tInsp != null) basicParameters.add(tInsp);
//                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
                        if (pip != null) advancedParameters.add(pip);
                        if (pPlat != null) basicParameters.add(pPlat);
//                        if(frequency != null) advancedParameters.add(frequency);

                        //  if (peepValve != null) advancedParameters.add(peepValve);
//                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
//                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
//                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
//                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        if (tExp != null) advancedParameters.add(tExp);
                        if (supportPressure != null) basicParameters.add(supportPressure);
//                        if (slope != null) advancedParameters.add(slope);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (peep != null) basicParameters.add(peep);
//                        if (slope != null) advancedParameters.add(slope);
                        if (tExp != null) advancedParameters.add(tExp);
//                        if(frequency != null) advancedParameters.add(frequency);
                        if (pip != null) advancedParameters.add(pip);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        if (supportPressure != null) basicParameters.add(supportPressure);

//
                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;
                }
                break;

            case MODE_NIV_CPAP:

                switch (prefManager.readCurrentUid()) {

                    case TYPE_PED:

                    case TYPE_ADULT:

                        if (fio2 != null) basicParameters.add(fio2);
                        //   if (tInsp != null) basicParameters.add(tInsp);
                        if (triggerFlow != null) basicParameters.add(triggerFlow);
                        if (peep != null) basicParameters.add(peep);
                        if (pip != null) advancedParameters.add(pip);
//                        if(frequency != null) advancedParameters.add(frequency);
                        //  if (peepValve != null) advancedParameters.add(peepValve);
                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
//                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
                        if (tExp != null) advancedParameters.add(tExp);
                        if (supportPressure != null) basicParameters.add(supportPressure);
                        if (slope != null) advancedParameters.add(slope);
//                        if (etPressure != null) etCuffParamters.add(etPressure);

                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (peep != null) basicParameters.add(peep);
//                        if(frequency != null) advancedParameters.add(frequency);
//                        if (slope != null) advancedParameters.add(slope);
//                        if (tExp != null) advancedParameters.add(tExp);
//                        if (pip != null) advancedParameters.add(pip);
//                        if (triggerFlow != null) basicParameters.add(triggerFlow);
//                        if (supportPressure != null) basicParameters.add(supportPressure);
//                        if (etPressure != null) etCuffParamters.add(etPressure);


                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;
                }
                break;

            case MODE_NC_CPAP:

                switch (prefManager.readCurrentUid()) {

//                    case TYPE_PED:
//
//                    case TYPE_ADULT:
//
//                        if (fio2 != null) basicParameters.add(fio2);
//                        //   if (tInsp != null) basicParameters.add(tInsp);
//                        if (triggerFlow != null) basicParameters.add(triggerFlow);
//                        if (peep != null) basicParameters.add(peep);
//                        if (pip != null) advancedParameters.add(pip);
//                        //  if (peepValve != null) advancedParameters.add(peepValve);
//                        if (targetSpo2 != null) smartFiO2Parameters.add(targetSpo2);
//                        if (hrLimit != null) smartFiO2Parameters.add(hrLimit);
//                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
////                        if (fio2_dev != null) smartFiO2Parameters.add(fio2_dev);
//                        if (tExp != null) advancedParameters.add(tExp);
//                        if (supportPressure != null) basicParameters.add(supportPressure);
//                        if (slope != null) advancedParameters.add(slope);

//                        apneaParameters.add(rrApnea);
//                        apneaParameters.add(tApnea);
//                        apneaParameters.add(vtApnea);
//                        break;

                    case TYPE_NEONAT:
                        if (fio2 != null) basicParameters.add(fio2);
                        if (peep != null) basicParameters.add(peep);
//                        if(frequency != null) advancedParameters.add(frequency);
//                        if (slope != null) advancedParameters.add(slope);
//                        if (tExp != null) advancedParameters.add(tExp);
//                        if (pip != null) advancedParameters.add(pip);
//                        if (triggerFlow != null) basicParameters.add(triggerFlow);
//                        if (supportPressure != null) basicParameters.add(supportPressure);
//                        if (etPressure != null) etCuffParamters.add(etPressure);


                        apneaParameters.add(rrApnea);
                        apneaParameters.add(tApnea);
                        apneaParameters.add(vtApnea);
                        break;
                }


                break;

        }

        // for no slope code in ATN 24
        if (Build.VERSION.SDK_INT >= 27) {
            advancedParameters.remove(slope);
        }

        Map<ControlSettingType, List<ControlParameterModel>> parameters = new HashMap<ControlSettingType, List<ControlParameterModel>>();

        if (!basicParameters.isEmpty()) parameters.put(ControlSettingType.BASIC, basicParameters);
        if (!advancedParameters.isEmpty())
            parameters.put(ControlSettingType.ADVANCED, advancedParameters);
        if (!apneaParameters.isEmpty()) {
            Log.i("value_backup", "in condtion");
            parameters.put(ControlSettingType.BACKUP, apneaParameters);
        }
        if (!smartFiO2Parameters.isEmpty())
            parameters.put(ControlSettingType.SmartFio2, smartFiO2Parameters);
        if (!vTasParameters.isEmpty()) parameters.put(ControlSettingType.VTas, vTasParameters);
        if (!etCuffParamters.isEmpty()) parameters.put(ControlSettingType.EtCuff, etCuffParamters);

        Log.i("vTas_check1", String.valueOf(parameters.size()));
        return parameters;

    }


/*    static String[] getAckFromAntiAck(String antiAckCode){


        String[] keyValue= new String[10];
        int index=0;
        if(antiAckCode != null ) {
            for (Map.Entry<String, String> i :Mapping.ackMapping.entrySet()) {
                if (i.getValue().equals(antiAckCode)) {
                    keyValue[index++]=i.getKey();
                    System.out.println(i.getKey());
                    // break;
                }

            }

            return keyValue;
        }
        return null;
    }*/

    HashMap<String, String> alarmLimitsMap = new HashMap<String, String>();

    /*
     * Returns the category / control type of ventilator mode
     */
    public static HashMap<String, String> filterAlarmLimitsbyPatientType(PatientProfile patientProfile, Context context) {
        switch (patientProfile) {
            case TYPE_ADULT: {
                alarmLimitsMap.put("presserUpperLimit", context.getString(R.string.default_max_pip_limit));
                alarmLimitsMap.put("presserLowerLimit", context.getString(R.string.default_min_pip_limit));

                alarmLimitsMap.put("vteUpperLimit", context.getString(R.string.default_max_vte_limit));
                alarmLimitsMap.put("vteLowerLimit", context.getString(R.string.default_min_vte_limit));

                alarmLimitsMap.put("peepUpperLimit", context.getString(R.string.default_max_peep_limit));
                alarmLimitsMap.put("peepLowerLimit", context.getString(R.string.default_min_peep_limit));

                alarmLimitsMap.put("respiratoryUpperLimit", context.getString(R.string.default_max_rr_limit));
                alarmLimitsMap.put("respiratoryLowerLimit", context.getString(R.string.default_min_rr_limit));

                alarmLimitsMap.put("mveUpperLimit", context.getString(R.string.default_max_mve_limit));
                alarmLimitsMap.put("mveLowerLimit", context.getString(R.string.default_min_mve_limit));

                return alarmLimitsMap;
            }
            case TYPE_PED: {
                alarmLimitsMap.put("presserUpperLimit", context.getString(R.string.default_max_pip_ped_limit));
                alarmLimitsMap.put("presserLowerLimit", context.getString(R.string.default_min_pip_ped_limit));

                alarmLimitsMap.put("vteUpperLimit", context.getString(R.string.default_max_vte_ped_limit));
                alarmLimitsMap.put("vteLowerLimit", context.getString(R.string.default_min_vte_ped_limit));

                alarmLimitsMap.put("peepUpperLimit", context.getString(R.string.default_max_peep_ped_limit));
                alarmLimitsMap.put("peepLowerLimit", context.getString(R.string.default_min_peep_ped_limit));

                alarmLimitsMap.put("respiratoryUpperLimit", context.getString(R.string.default_max_rr_ped_limit));
                alarmLimitsMap.put("respiratoryLowerLimit", context.getString(R.string.default_min_rr_ped_limit));

                alarmLimitsMap.put("mveUpperLimit", context.getString(R.string.default_max_mve_ped_limit));
                alarmLimitsMap.put("mveLowerLimit", context.getString(R.string.default_min_mve_ped_limit));

                return alarmLimitsMap;
            }
            case TYPE_NEONAT: {
                alarmLimitsMap.put("presserUpperLimit", context.getString(R.string.default_max_pip_limit_neo));
                alarmLimitsMap.put("presserLowerLimit", context.getString(R.string.default_min_pip_limit_neo));

                alarmLimitsMap.put("vteUpperLimit", context.getString(R.string.default_max_vte_limit_neo));
                alarmLimitsMap.put("vteLowerLimit", context.getString(R.string.default_min_vte_limit_neo));

                alarmLimitsMap.put("peepUpperLimit", context.getString(R.string.default_max_peep_limit_neo));
                alarmLimitsMap.put("peepLowerLimit", context.getString(R.string.default_min_peep_limit_neo));

                alarmLimitsMap.put("respiratoryUpperLimit", context.getString(R.string.default_max_rr_limit_neo));
                alarmLimitsMap.put("respiratoryLowerLimit", context.getString(R.string.default_min_rr_limit_neo));

                alarmLimitsMap.put("mveUpperLimit", context.getString(R.string.default_max_mve_limit_neo));
                alarmLimitsMap.put("mveLowerLimit", context.getString(R.string.default_min_mve_limit_neo));

                return alarmLimitsMap;
            }
            default: {
                return alarmLimitsMap;
                //return context.getString(R.string.default_max_pip_ped_limit);
            }
        }
    }

    /*
     * Returns the category / control type of ventilator mode
     */
    static int getModeCategory(VentMode mode) {
        return (mode != null) ? getModeCategory(mode.getModeCode()) : 0;
    }

    static int getModeCategory(int modeCode) {
        switch (modeCode / 10) {
            case 1:
                return modeCode == Configs.MODE_PC_SPONT_DUMMY ? MODE_NIV : MODE_PCV;
            case 2:
                return MODE_VCV;
            case 3:
                return MODE_NIV;
            default:
                return 0;
        }
    }

    static List<VentMode> getModesByCategory(Context ctx, int category) {
        List<VentMode> modes = new ArrayList<>();
        for (VentMode mode : getVentilatorModes(ctx)) {
            if (mode.getModeCode() / 10 == category && mode.getModeCode() != MODE_AUTO_VENTILATION)
                modes.add(mode);
        }

        return modes;
    }

    static ArrayList<VentMode> getVentilatorModes(Context ctx) {
        ArrayList<VentMode> availableModes = new ArrayList<>();

        // PRESSURE CONTROLLED MODES
        availableModes.add(new VentMode(ctx.getString(R.string.hint_pressure_control), ctx.getString(R.string.hint_pc_cmv), MODE_PC_CMV));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_pressure_control), ctx.getString(R.string.hint_pc_imv), MODE_PC_SIMV));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_pressure_control), ctx.getString(R.string.hint_spont), MODE_PC_AC));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_pressure_control), ctx.getString(R.string.hint_psv), MODE_PC_PSV));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_pressure_control), ctx.getString(R.string.hint_prvc), MODE_PC_PRVC));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_pressure_control), ctx.getString(R.string.hint_pc_aprv), MODE_PC_ARPV));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_hfnc), ctx.getString(R.string.hint_hfnc), MODE_HFNC));
        //  availableModes.add(new VentMode(ctx.getString(R.string.hint_pressure_control), ctx.getString(R.string.hint_hfnc), MODE_HFNC));


        // VOLUME CONTROLLED MODES
        availableModes.add(new VentMode(ctx.getString(R.string.auto_control), ctx.getString(R.string.hint_ai_vent), MODE_AUTO_VENTILATION));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_volume_control), ctx.getString(R.string.hint_vc_cmv), MODE_VCV_CMV));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_volume_control), ctx.getString(R.string.hint_vc_simv), MODE_VCV_SIMV));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_volume_control), ctx.getString(R.string.hint_vc_cv), MODE_VCV_ACV));

        // INO INVASIVE CONTROLLED MODES
        availableModes.add(new VentMode(ctx.getString(R.string.hint_noninvasive), ctx.getString(R.string.hint_cpap), MODE_NIV_CPAP));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_noninvasive), ctx.getString(R.string.hint_bpap), MODE_NIV_BPAP));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_noninvasive), ctx.getString(R.string.hint_bpap), INV_CPAP));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_noninvasive), ctx.getString(R.string.hint_bpap), INV_BPAP));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_noninvasive), ctx.getString(R.string.hint_nc_cpap), MODE_NC_IPPV));
        availableModes.add(new VentMode(ctx.getString(R.string.hint_noninvasive), ctx.getString(R.string.hint_nccpap), MODE_NC_CPAP));
        //NEONATAL INVASIVE MODES

        //   availableModes.add(new VentMode(ctx.getString(R.string.hint_noninvasive), ctx.getString(R.string.hint_ncpap), MODE_NIV_NCPAP));
        //  availableModes.add(new VentMode(ctx.getString(R.string.hint_noninvasive), ctx.getString(R.string.hint_nbpap), MODE_NIV_NBPAP));

        return availableModes;
    }

    static String getPatientType(PreferenceManager prefManager) {

        if (prefManager != null) {
            switch (prefManager.readCurrentUid()) {

                case TYPE_NEONAT:
                    return "Neonate";
                case TYPE_PED:
                    return "Pediatric";
                case TYPE_ADULT:
                    return "Adult";
            }
        }
        return "";
    }

    static VentMode getVentilatorModeByCode(Context context, int modeCode) {
        ArrayList<VentMode> modes = getVentilatorModes(context);
        if (modes != null && !modes.isEmpty()) {
            for (VentMode m : modes) {
                if (m.getModeCode() == modeCode) return m;
            }
        }

        return null;
    }

    static boolean isValidVentilatorMode(Context ctx, int mode) {
        for (VentMode ventMode : Configs.getVentilatorModes(ctx)) {
            if (ventMode.getModeCode() == mode) {
                return true;
            }
        }

        return false;
    }

    static boolean isVentilatorModeNeoNatal(Context ctx, int mode) {
        for (VentMode ventMode : Configs.getVentilatorModes(ctx)) {
            if (ventMode.getModeCode() == mode) return true;
        }
        return false;
    }

    static boolean isAutoScalableSupported(Context ctx, int mode) {
        boolean isSupported = (mode != MODE_NIV_BPAP && mode != MODE_PC_SPONT_DUMMY && mode != MODE_NIV_CPAP);
        return isValidVentilatorMode(ctx, mode) && isSupported;
    }

    static boolean isValidOxygenSupportedMode(Context ctx, int mode) {
//        boolean isOxygenMode = (getModeCategory(mode) != MODE_NIV && mode != MODE_PC_SPONTANEOUS && mode != MODE_PC_PSV );
        boolean isOxygenMode = true;
        return isValidVentilatorMode(ctx, mode) && isOxygenLevelsAvailable && isOxygenMode;
    }

    /**
     * This is a filter for acknowledgement validation with working mode
     *
     * @param mode : current running ventilation mode
     * @param ack  : Acknowledgement coming from ventilator
     * @return is acknowledgement valid
     */
    static boolean isAcknowledgementAcceptable(int mode, String ack) {

        if (ack == null) return false;

        // TEMP0RARY BLOCKING
        if (Configs.ACK_CODE_14.equals(ack) || Configs.ACK_CODE_24.equals(ack)) return false;

        if (mode == MODE_NIV_CPAP) {
            for (String prohibitAck : cpapProhibitedAcks()) {
                if (prohibitAck.equals(ack)) return false;
            }
        }

        return true;
    }

    static int getAckPriorityLevel(String ack) {
        return unmutedLevelAcks.contains(ack) ? WARNING_LEVEL_UNMUTABLE : (highLevelAcks.contains(ack) ? WARNING_LEVEL_HIGH : WARNING_LEVEL_LOW);
    }

    static boolean isVolumeControlMode(int mode) {
        return getModeCategory(mode) == MODE_VCV;
    }

    static boolean isBackupVentilationAcceptable(int mode) {
        return getModeCategory(mode) == MODE_NIV || mode == MODE_PC_SPONTANEOUS || mode == MODE_PC_SPONT_DUMMY;
    }

    static String getParameterUnit(final Context context, final String lbl) {
        switch (lbl) {
            case LBL_PIP:
                return context.getString(R.string.hint_cmH2o);
            case LBL_VTI:
                return context.getString(R.string.hint_ml);
            case LBL_PEEP:
                return context.getString(R.string.hint_cmH2o);
            case LBL_RR:
                return context.getString(R.string.hint_bpm);
            case LBL_FIO2:
                return context.getString(R.string.hint_percentage);
            case LBL_PPLAT:
                return context.getString(R.string.hint_cmH2o);
            case LBL_VLEAK:
                return context.getString(R.string.hint_ml);
            case LBL_AVERAGE_LEAK:
                return context.getString(R.string.hint_percentage);
            case LBL_MVI:
                return context.getString(R.string.hint_ml);
            case LBL_MVE:
                return context.getString(R.string.hint_ml);
            case LBL_PEAK_FLOW:
                return context.getString(R.string.hint_l_min);
            case LBL_TINSP:
                return context.getString(R.string.hint_sec);
            default:
                return "";
        }
    }

//    static int getCompensateInputRR(int rawRR){
//        final double rr = (-478.2953) + (44.29698 * rawRR) - (1.441746 * Math.pow(rawRR, 2)) + (0.02107701 * Math.pow(rawRR, 3)) - (0.000113417 * Math.pow(rawRR, 4));
//        return (int)Math.round(rr);
//    }

    static ArrayList<String> cpapProhibitedAcks() {
        ArrayList<String> prohibitedAcks = new ArrayList<>();
        prohibitedAcks.add(ACK_CODE_7);
        prohibitedAcks.add(ACK_CODE_8);
        prohibitedAcks.add(ACK_CODE_9);
        prohibitedAcks.add(ACK_CODE_12);
        prohibitedAcks.add(ACK_CODE_13);
        prohibitedAcks.add(ACK_CODE_14);
        prohibitedAcks.add(ACK_CODE_22);
        prohibitedAcks.add(ACK_CODE_24);

        return prohibitedAcks;
    }

    static Pair<Float, Float> calculateInspTimeConstraints(int rr) {
        if (rr == 0) return null;

        final float cycleTime = 60f / rr;

        final Pair<Float, Float> limits = new Pair<>((float) (0.1 * cycleTime), (float) (0.5 * cycleTime));
        return limits;
    }

    static Pair<Float, Float> FlowGraphLimits(int min, int max) {
        switch (min) {
            case 100: {
                min = ConstantKt.NEO_GRAPH_FLOW_MIN;
            }
            break;
            case 200: {
                min = ConstantKt.PED_GRAPH_FLOW_MIN;
            }
            break;
            case 400: {
                min = ConstantKt.ADULT_GRAPH_FLOW_MIN;
            }
            default:
                min = 100;
        }
        switch (max) {

            case 100: {
                max = ConstantKt.NEO_GRAPH_FLOW_MAX;
            }

            case 200: {
                max = ConstantKt.PED_GRAPH_FLOW_MAX;
            }
            case 400: {
                max = ConstantKt.ADULT_GRAPH_FLOW_MAX;
            }
            default:
        }
        final Pair<Float, Float> limiters = new Pair<>((float) (min), (float) (max));
        return limiters;
    }

    @SuppressLint("DefaultLocale")
    static String calculateInspiratoryTimeLimit(int rr) {
        if (rr == 0) return null;
        int cycleTime = 60 / rr;
        int maxExpTime = 300;
        int maxInspTime = (cycleTime * 1000) - maxExpTime;

        return String.format("%.1f", maxInspTime);
    }
//Method Used Previously for calculatin IE Ratio
/*    static String calculateIERatio(int rr, Float tinsp){
        if(rr == 0 || tinsp == null) return null;

        float cycleTime = 60f / rr;
        Float texp = cycleTime - tinsp;
        float ratio = texp/tinsp;
        return 1 + " : " + String.format("%.2f", ratio);
    }*/

//    static Float calculateETTDepth(double height){
//        double eTTDepth = 0.1 * (height * 4);
//                return  (float)eTTDepth;
//    }
/*static Float calculateIdealBodyWeight(double height, String gender){
   if(gender == String.valueOf(Gender.TYPE_MALE) ){
       double ibw = 22 * Math.pow(height, 2);
       return (float) ibw;
   }else {
       double ibw = 22 * Math.pow((height - 10),2);
       return (float) ibw;
   }
}
static Float calculateTidalVolume(float height,String gender){
    float tidalVolume = 6 * calculateIdealBodyWeight(height,gender);
    return tidalVolume;
}*/


    static String calculateIERatio(int rr, Float tinsp) {
        if (rr == 0 || tinsp == null) return null;

        float cycleTime = 60f / rr;
        float texp = cycleTime - tinsp;
        float eiRatio = (texp) / (tinsp);

        Log.i("EI Ratio", "RR: " + rr + ", TInsp: " + String.format("%.2f", tinsp) + ", CycleTime: " + cycleTime + ", Texp: " + String.format("%.3f", texp) + ", EI Ratio: " + eiRatio);

        return 1 + " : " + String.format("%.1f", eiRatio);
    }

//    static Float calculateTiFromIERatio(int rr) {
//        if (rr == 0) return 0f;
//        float cycleTime = 60f / 25;
//
//        for (Float i=1.0f; i<=30.0f; i+= 0.1f) {
//            float tinsp = cycleTime * (1f / (1f + i));
//            Log.i("EI Ratio",tinsp + " " + i + " " + cycleTime + " " + rr);
//        }
////      float texp = cycleTime - tinsp;
//        return 1f;
//    }

    static Float calculateInspTimeFromIERatio(int rr, String ratio) {
        if (ratio != null && ratio.contains(":") && rr > 0) {
            try {
                float cycleTime = 60f / rr;
                float eiratio = Float.valueOf(ratio.substring(ratio.indexOf(":") + 1));
                return cycleTime / (eiratio + 1);
            } catch (Exception e) {
                Log.i("PARSE CHECK", "Unable to parse IE ratio. Not a number");
            }
        }

        return null;
    }

    static float calculatePIPForAutoVentilation(float vol) {
        if (vol < 200) return 30f;
        else if (vol < 300 && vol >= 200) return 40f;
        else if (vol < 500 && vol >= 300) return 50f;
        else if (vol >= 500) return 60f;
        else return 60f;
    }

    static float calculateInspTimeForAutoVentilation(float vol) {
        if (vol >= 50 && vol < 100) return 0.6f;
        else if (vol >= 100 && vol < 150) return 0.7f;
        else if (vol >= 150 && vol < 200) return 0.8f;
        else if (vol >= 200 && vol < 250) return 1.0f;
        else if (vol >= 250 && vol < 300) return 1.1f;
        else if (vol >= 300 && vol < 2000) return 1.3f;
        else return 1.3f;
    }

    static float calculateRRForAutoVentilation(float vol) {
        if (vol >= 50 && vol < 100) return 35;
        else if (vol >= 100 && vol < 150) return 30;
        else if (vol >= 150 && vol < 200) return 25;
        else if (vol >= 200 && vol < 250) return 20;
        else if (vol >= 250 && vol < 300) return 18;
        else if (vol >= 300 && vol < 2000) return 16;
        else return 16;


    }

    static float calculateVolumeForAutoVentilation(float weight) {
        return weight * 7;
    }

    static float calculateIBW(float heightInCms) {
        // 1 inch = 2.54 cms
        if (heightInCms > 0) {
            return (float) (48 + (2.3 * ((heightInCms / 2.54) - 60)));
        }

        return -1;
    }


    static String calculateTtot(int rr) {
        if (rr == 0) return null;
        float cycleTime = 60f / rr;
        return String.format("%.1f", cycleTime);
    }

    static String calculateTexp(int rr, float ti) {
        float cycleTime = 60f / rr;
        float texp = cycleTime - ti;
        return String.format("%.1f", texp);
    }

    //decimal check with boolean values
    static boolean isDecimalSupported(String label) {
        if (LBL_TRIG_FLOW.equals(label) || LBL_APNEA_TRIG_FLOW.equals(label) || LBL_TLOW.equals(label) || LBL_TINSP.equals(label) || LBL_MVE.equals(label) || LBL_MVI.equals(label)) {
            return true;
        } else {
            return false;
        }
    }

    static


            //ack99 added here
    class MessageFactory {
        /* public static String getAckMess(Context ctx,String code){
             switch (code){
                 case ACK_CODE_0 : return ctx.getResources().getString(R.string.ack_0);
                 case ACK_CODE_1 : return ctx.getResources().getString(R.string.ack_1);
                 case ACK_CODE_2 : return ctx.getResources().getString(R.string.ack_2);
                 case ACK_CODE_3 : return ctx.getResources().getString(R.string.ack_3);
                 case ACK_CODE_4 : return ctx.getResources().getString(R.string.ack_4);
                 case ACK_CODE_5 : return ctx.getResources().getString(R.string.ack_5);
                 case ACK_CODE_6 : return ctx.getResources().getString(R.string.ack_6);
                 case ACK_CODE_7 : return ctx.getResources().getString(R.string.ack_7);
                 case ACK_CODE_8 : return ctx.getResources().getString(R.string.ack_8);
                 case ACK_CODE_9 : return ctx.getResources().getString(R.string.ack_9);
                 case ACK_CODE_10 : return ctx.getResources().getString(R.string.ack_10);
                 case ACK_CODE_11 : return ctx.getResources().getString(R.string.ack_11);
                 case ACK_CODE_12 : return ctx.getResources().getString(R.string.ack_12);
                 case ACK_CODE_13 : return ctx.getResources().getString(R.string.ack_13);
                 case ACK_CODE_14 : return ctx.getResources().getString(R.string.ack_14);
                 case ACK_CODE_15 : return ctx.getResources().getString(R.string.ack_15);
                 case ACK_CODE_16 : return ctx.getResources().getString(R.string.ack_16);
                 case ACK_CODE_17 : return ctx.getResources().getString(R.string.ack_17);
                 case ACK_CODE_18 : return ctx.getResources().getString(R.string.ack_18);
                 case ACK_CODE_19 : return ctx.getResources().getString(R.string.ack_19);
                 case ACK_CODE_20 : return ctx.getResources().getString(R.string.ack_20);
                 case ACK_CODE_21 : return ctx.getResources().getString(R.string.ack_21);
                 case ACK_CODE_22 : return ctx.getResources().getString(R.string.ack_22);
                 case ACK_CODE_23 : return ctx.getResources().getString(R.string.ack_23);
                 case ACK_CODE_24 : return ctx.getResources().getString(R.string.ack_24);
                 case ACK_CODE_25 : return ctx.getResources().getString(R.string.ack_25);
                 case ACK_CODE_26 : return ctx.getResources().getString(R.string.ack_26);
                 case ACK_CODE_27 : return ctx.getResources().getString(R.string.ack_27);
                 case ACK_CODE_28 : return ctx.getResources().getString(R.string.ack_28);
                 case ACK_CODE_29 : return ctx.getResources().getString(R.string.ack_29);
                 case ACK_CODE_30 : return ctx.getResources().getString(R.string.ack_30);
                 case ACK_CODE_31 : return ctx.getResources().getString(R.string.ack_31);
                 case ACK_CODE_32 : return ctx.getResources().getString(R.string.ack_32);
                 case ACK_CODE_33 : return ctx.getResources().getString(R.string.ack_33);
                 case ACK_CODE_34 : return ctx.getResources().getString(R.string.ack_34);
                 case ACK_CODE_35 : return ctx.getResources().getString(R.string.ack_35);
                 case ACK_CODE_36 : return ctx.getResources().getString(R.string.ack_36);
                 case ACK_CODE_37 : return ctx.getResources().getString(R.string.ack_37);
                 case ACK_CODE_38 : return ctx.getResources().getString(R.string.ack_38);
                 case ACK_CODE_39 : return ctx.getResources().getString(R.string.ack_39);
                 case ACK_CODE_40 : return ctx.getResources().getString(R.string.ack_40);
                 case ACK_CODE_41 : return ctx.getResources().getString(R.string.ack_41);
                 case ACK_CODE_44 : return ctx.getResources().getString(R.string.ack_44);
                 case ACK_CODE_45 : return ctx.getResources().getString(R.string.ack_45);
                 case ACK_CODE_46 : return ctx.getResources().getString(R.string.ack_46);
                 case ACK_CODE_47 : return ctx.getResources().getString(R.string.ack_47);
                 case ACK_CODE_48 : return ctx.getResources().getString(R.string.ack_48);
                 case ACK_CODE_49 : return ctx.getResources().getString(R.string.ack_49);
                 case ACK_CODE_50 : return ctx.getResources().getString(R.string.ack_50);
                 case ACK_CODE_51 : return ctx.getResources().getString(R.string.ack_51);
                 case ACK_CODE_52 : return ctx.getResources().getString(R.string.ack_52);
                 case ACK_CODE_53 : return ctx.getResources().getString(R.string.ack_53);
                 case ACK_CODE_54 : return ctx.getResources().getString(R.string.ack_54);
                 case ACK_CODE_55 : return ctx.getResources().getString(R.string.ack_55);
                 case ACK_CODE_56 : return ctx.getResources().getString(R.string.ack_56);
                 case ACK_CODE_57 : return ctx.getResources().getString(R.string.ack_57);
                 case ACK_CODE_58 : return ctx.getResources().getString(R.string.ack_58);
                 case ACK_CODE_59 : return ctx.getResources().getString(R.string.ack_59);
                 case ACK_CODE_60 : return ctx.getResources().getString(R.string.ack_60);
                 case ACK_CODE_61 : return ctx.getResources().getString(R.string.ack_61);
                 case ACK_CODE_62 : return ctx.getResources().getString(R.string.ack_62);
                 case ACK_CODE_63 : return ctx.getResources().getString(R.string.ack_63);
                 case ACK_CODE_64 : return ctx.getResources().getString(R.string.ack_64);
                 case ACK_CODE_65 : return ctx.getResources().getString(R.string.ack_65);
                 case ACK_CODE_66 : return ctx.getResources().getString(R.string.ack_66);
                 case ACK_CODE_67 : return ctx.getResources().getString(R.string.ack_67);
                 case ACK_CODE_68 : return ctx.getResources().getString(R.string.ack_68);

                 case ACK_CODE_70 : return ctx.getResources().getString(R.string.ack_70);
                 case ACK_CODE_71 : return ctx.getResources().getString(R.string.ack_71);
                 case ACK_CODE_72 : return ctx.getResources().getString(R.string.ack_72);
                 case ACK_CODE_73 : return ctx.getResources().getString(R.string.ack_73);
                 case ACK_CODE_74 : return ctx.getResources().getString(R.string.ack_74);
                 case ACK_CODE_75 : return ctx.getResources().getString(R.string.ack_75);
                 case ACK_CODE_76 : return ctx.getResources().getString(R.string.ack_76);
                 case ACK_CODE_80 : return ctx.getResources().getString(R.string.ack_80);
                 case ACK_CODE_81 : return ctx.getResources().getString(R.string.ack_81);
                 case ACK_CODE_90 : return ctx.getResources().getString(R.string.ack_90);
                 case ACK_CODE_91 : return ctx.getResources().getString(R.string.ack_91);

             }
 */
        public static String getAckMessage(Context ctx, String code, Integer readLastVentMode) {
            switch (code) {
                case ACK_CODE_0:
                    return ctx.getResources().getString(R.string.ack_0);
                case ACK_CODE_1:
                    return ctx.getResources().getString(R.string.ack_1);
                case ACK_CODE_2:
                    return ctx.getResources().getString(R.string.ack_2);
                case ACK_CODE_3:
                    return ctx.getResources().getString(R.string.ack_3);
                case ACK_CODE_4:
                    return ctx.getResources().getString(R.string.ack_4);
                case ACK_CODE_5:
                    return ctx.getResources().getString(R.string.ack_5);
                case ACK_CODE_6:
                    return ctx.getResources().getString(R.string.ack_6);
                case ACK_CODE_7:
                    return ctx.getResources().getString(R.string.ack_7);
                case ACK_CODE_8:
                    return ctx.getResources().getString(R.string.ack_8);
                case ACK_CODE_9:
                    return ctx.getResources().getString(R.string.ack_9);
                case ACK_CODE_10:
                    return ctx.getResources().getString(R.string.ack_10);
                case ACK_CODE_11:
                    return ctx.getResources().getString(R.string.ack_11);
                case ACK_CODE_12:
                    return ctx.getResources().getString(R.string.ack_12);
                case ACK_CODE_13:
                    return ctx.getResources().getString(R.string.ack_13);
                case ACK_CODE_14:
                    return ctx.getResources().getString(R.string.ack_14);
                case ACK_CODE_15:
                    return ctx.getResources().getString(R.string.ack_15);
                case ACK_CODE_16:
                    return ctx.getResources().getString(R.string.ack_16);
                case ACK_CODE_17:
                    return ctx.getResources().getString(R.string.ack_17);
                case ACK_CODE_18:
                    return ctx.getResources().getString(R.string.ack_18);
                case ACK_CODE_19:
                    return ctx.getResources().getString(R.string.ack_19);
                case ACK_CODE_20:
                    return ctx.getResources().getString(R.string.ack_20);
                case ACK_CODE_21:
                    return ctx.getResources().getString(R.string.ack_21);
                case ACK_CODE_22:
                    return ctx.getResources().getString(R.string.ack_22);
                case ACK_CODE_23:
                    return ctx.getResources().getString(R.string.ack_23);
                case ACK_CODE_24:
                    return ctx.getResources().getString(R.string.ack_24);
                case ACK_CODE_25:
                    return ctx.getResources().getString(R.string.ack_25);
                case ACK_CODE_26:
                    return ctx.getResources().getString(R.string.ack_26);
                case ACK_CODE_27:
                    return ctx.getResources().getString(R.string.ack_27);
                case ACK_CODE_28:
                    return ctx.getResources().getString(R.string.ack_28);
                case ACK_CODE_29:
                    return ctx.getResources().getString(R.string.ack_29);
                case ACK_CODE_30:
                    return ctx.getResources().getString(R.string.ack_30);
                case ACK_CODE_31:
                    return ctx.getResources().getString(R.string.ack_31);
                case ACK_CODE_32:
                    return ctx.getResources().getString(R.string.ack_32);
                case ACK_CODE_33:
                    return ctx.getResources().getString(R.string.ack_33);
                case ACK_CODE_34:
                    return ctx.getResources().getString(R.string.ack_34);
                case ACK_CODE_35:
                    return ctx.getResources().getString(R.string.ack_35);
                case ACK_CODE_36:
                    return ctx.getResources().getString(R.string.ack_36);
                case ACK_CODE_37:
                    return ctx.getResources().getString(R.string.ack_37);
                case ACK_CODE_38:
                    return ctx.getResources().getString(R.string.ack_38);
                case ACK_CODE_39:
                    return ctx.getResources().getString(R.string.ack_39);
                case ACK_CODE_40:
                    return ctx.getResources().getString(R.string.ack_40);
                case ACK_CODE_41:
                    return ctx.getResources().getString(R.string.ack_41);
                case ACK_CODE_44:
                    return ctx.getResources().getString(R.string.ack_44);
                case ACK_CODE_45:
                    return ctx.getResources().getString(R.string.ack_45);
                case ACK_CODE_46:
                    return ctx.getResources().getString(R.string.ack_46);
                case ACK_CODE_47:
                    return ctx.getResources().getString(R.string.ack_47);
                case ACK_CODE_48:
                    return ctx.getResources().getString(R.string.ack_48);
                case ACK_CODE_49:
                    return ctx.getResources().getString(R.string.ack_49);
                case ACK_CODE_50:
                    return ctx.getResources().getString(R.string.ack_50);
                case ACK_CODE_51:
                    return ctx.getResources().getString(R.string.ack_51);
                case ACK_CODE_52:
                    return ctx.getResources().getString(R.string.ack_52);
                case ACK_CODE_53:
                    return ctx.getResources().getString(R.string.ack_53);
                case ACK_CODE_54:
                    return ctx.getResources().getString(R.string.ack_54);
                case ACK_CODE_55:
                    return ctx.getResources().getString(R.string.ack_55);
                case ACK_CODE_56:
                    return ctx.getResources().getString(R.string.ack_56);
                case ACK_CODE_57:
                    return ctx.getResources().getString(R.string.ack_57);
                case ACK_CODE_58:
                    return ctx.getResources().getString(R.string.ack_58);
                case ACK_CODE_59:
                    return ctx.getResources().getString(R.string.ack_59);
                case ACK_CODE_60:
                    return ctx.getResources().getString(R.string.ack_60);
                case ACK_CODE_61:
                    return ctx.getResources().getString(R.string.ack_61);
                case ACK_CODE_62:
                    return ctx.getResources().getString(R.string.ack_62);
                case ACK_CODE_63:
                    return ctx.getResources().getString(R.string.ack_63);
                case ACK_CODE_64:
                    return ctx.getResources().getString(R.string.ack_64);
                case ACK_CODE_65:
                    return ctx.getResources().getString(R.string.ack_65);
                case ACK_CODE_66:
                    return ctx.getResources().getString(R.string.ack_66);
                case ACK_CODE_67:
                    return ctx.getResources().getString(R.string.ack_67);
                case ACK_CODE_68:
                    return ctx.getResources().getString(R.string.ack_68);
                case ACK_CODE_69:
                    return ctx.getResources().getString(R.string.ack_69);
                case ACK_CODE_70:
                    return ctx.getResources().getString(R.string.ack_70);
                case ACK_CODE_71:
                    return ctx.getResources().getString(R.string.ack_71);
                case ACK_CODE_72:
                    return ctx.getResources().getString(R.string.ack_72);
                case ACK_CODE_73:
                    return ctx.getResources().getString(R.string.ack_73);
                case ACK_CODE_74:
                    return ctx.getResources().getString(R.string.ack_74);
                case ACK_CODE_75:
                    return ctx.getResources().getString(R.string.ack_75);
                case ACK_CODE_76:
                    return ctx.getResources().getString(R.string.ack_76);
                case ACK_CODE_79:
                    return ctx.getResources().getString(R.string.ack_79);
                case ACK_CODE_80:
                    return ctx.getResources().getString(R.string.ack_80);
                case ACK_CODE_81:
                    return ctx.getResources().getString(R.string.ack_81);
                case ACK_CODE_82:
                    return "VT not reached Pmax active";
                case ACK_CODE_84:
                    return ctx.getResources().getString(R.string.ack_84);
                case ACK_CODE_90:
                    return ctx.getResources().getString(R.string.ack_90);
                case ACK_CODE_91:
                    return ctx.getResources().getString(R.string.ack_91);
                case ACK_CODE_92:
                    return "VT not reached Pmax active nack";

                case ACK_CODE_320:
                    return ctx.getResources().getString(R.string.ack_320);
                case ACK_CODE_321:
                    return ctx.getResources().getString(R.string.ack_321);
                case ACK_CODE_322:
                    return ctx.getResources().getString(R.string.ack_322);
                case ACK_CODE_323:
                    return ctx.getResources().getString(R.string.ack_323);
                case ACK_CODE_324:
                    return ctx.getResources().getString(R.string.ack_324);
                case ACK_CODE_325:
                    return ctx.getResources().getString(R.string.ack_325);
                case ACK_CODE_326:
                    return ctx.getResources().getString(R.string.ack_326);
                case ACK_CODE_327:
                    return ctx.getResources().getString(R.string.ack_327);
                case ACK_CODE_328:
                    return ctx.getResources().getString(R.string.ack_328);
                case ACK_CODE_329:
                    return ctx.getResources().getString(R.string.ack_329);
                case ACK_CODE_330:
                    return ctx.getResources().getString(R.string.ack_330);
                case ACK_CODE_331:
                    return ctx.getResources().getString(R.string.ack_331);
                case ACK_CODE_332:
                    return ctx.getResources().getString(R.string.ack_332);
                case ACK_CODE_333:
                    return ctx.getResources().getString(R.string.ack_333);
                case ACK_CODE_334:
                    return ctx.getResources().getString(R.string.ack_334);
                case ACK_CODE_335:
                    return ctx.getResources().getString(R.string.ack_335);
                case ACK_CODE_336:
                    return ctx.getResources().getString(R.string.ack_336);
                case ACK_CODE_337:
                    return ctx.getResources().getString(R.string.ack_337);
                case ACK_CODE_338:
                    return ctx.getResources().getString(R.string.ack_338);
                case ACK_CODE_339:
                    return ctx.getResources().getString(R.string.ack_339);
                case ACK_CODE_340:
                    return ctx.getResources().getString(R.string.ack_340);
                case ACK_CODE_341:
                    return ctx.getResources().getString(R.string.ack_341);
                case ACK_CODE_342:
                    return ctx.getResources().getString(R.string.ack_342);
                case ACK_CODE_343:
                    return ctx.getResources().getString(R.string.ack_343);
                case ACK_CODE_344:
                    return ctx.getResources().getString(R.string.ack_344);
                case ACK_CODE_345:
                    return ctx.getResources().getString(R.string.ack_345);
                case ACK_CODE_346:
                    return ctx.getResources().getString(R.string.ack_346);
                case ACK_CODE_347:
                    return ctx.getResources().getString(R.string.ack_347);
                case ACK_CODE_348:
                    return ctx.getResources().getString(R.string.ack_348);
                case ACK_CODE_349:
                    return ctx.getResources().getString(R.string.ack_349);
                case ACK_CODE_350:
                    return ctx.getResources().getString(R.string.ack_350);
                case ACK_CODE_351:
                    return ctx.getResources().getString(R.string.ack_351);
                case ACK_CODE_352:
                    return ctx.getResources().getString(R.string.ack_352);
                case ACK_CODE_353:
                    return ctx.getResources().getString(R.string.ack_353);
                case ACK_CODE_354:
                    return ctx.getResources().getString(R.string.ack_354);
                case ACK_CODE_355:
                    return ctx.getResources().getString(R.string.ack_355);
                case ACK_CODE_356:
                    return ctx.getResources().getString(R.string.ack_356);
                case ACK_CODE_357:
                    return ctx.getResources().getString(R.string.ack_357);
                case ACK_CODE_358:
                    return ctx.getResources().getString(R.string.ack_358);
                case ACK_CODE_359:
                    return ctx.getResources().getString(R.string.ack_359);
                case ACK_CODE_360:
                    return ctx.getResources().getString(R.string.ack_360);
                case ACK_CODE_361:
                    return ctx.getResources().getString(R.string.ack_361);
                case ACK_CODE_362:
                    return ctx.getResources().getString(R.string.ack_362);
                case ACK_CODE_363:
                    return ctx.getResources().getString(R.string.ack_363);
                case ACK_CODE_364:
                    return ctx.getResources().getString(R.string.ack_364);
                case ACK_CODE_365:
                    return ctx.getResources().getString(R.string.ack_365);
                case ACK_CODE_366:
                    return ctx.getResources().getString(R.string.ack_366);
                case ACK_CODE_367:
                    return ctx.getResources().getString(R.string.ack_367);
                case ACK_CODE_368:
                    return ctx.getResources().getString(R.string.ack_368);
                case ACK_CODE_369:
                    return ctx.getResources().getString(R.string.ack_369);
                case ACK_CODE_370:
                    return ctx.getResources().getString(R.string.ack_370);
                case ACK_CODE_371:
                    return ctx.getResources().getString(R.string.ack_371);
                case ACK_CODE_372:
                    return ctx.getResources().getString(R.string.ack_372);
                case ACK_CODE_373:
                    return ctx.getResources().getString(R.string.ack_373);
                case ACK_CODE_374:
                    return ctx.getResources().getString(R.string.ack_374);
                case ACK_CODE_375:
                    return ctx.getResources().getString(R.string.ack_375);
                case ACK_CODE_376:
                    return ctx.getResources().getString(R.string.ack_376);
                case ACK_CODE_377:
                    return ctx.getResources().getString(R.string.ack_377);
                case ACK_CODE_378:
                    return ctx.getResources().getString(R.string.ack_378);
                case ACK_CODE_379:
                    return ctx.getResources().getString(R.string.ack_379);
                case ACK_CODE_380:
                    return ctx.getResources().getString(R.string.ack_380);
                case ACK_CODE_381:
                    return ctx.getResources().getString(R.string.ack_381);
//                case ACK_CODE_382:
//                    return ctx.getResources().getString(R.string.ack_382);
                case ACK_CODE_383:
                    return ctx.getResources().getString(R.string.ack_383);
                case ACK_CODE_384:
                    return ctx.getResources().getString(R.string.ack_384);
                case ACK_CODE_385:
                    return ctx.getResources().getString(R.string.ack_385);
                case ACK_CODE_386:
                    return ctx.getResources().getString(R.string.ack_386);
                case ACK_CODE_387:
                    return ctx.getResources().getString(R.string.ack_387);
                case ACK_CODE_388:
                    return ctx.getResources().getString(R.string.ack_388);
                case ACK_CODE_389:
                    return ctx.getResources().getString(R.string.ack_389);
                case ACK_CODE_390:
                    return ctx.getResources().getString(R.string.ack_390);
                case ACK_CODE_391:
                    return ctx.getResources().getString(R.string.ack_391);
//                case ACK_CODE_392:
//                    return ctx.getResources().getString(R.string.ack_392);
                case ACK_CODE_393:
                    return ctx.getResources().getString(R.string.ack_393);
                case ACK_CODE_394:
                    return ctx.getResources().getString(R.string.ack_394);
                case ACK_CODE_395:
                    return ctx.getResources().getString(R.string.ack_395);
                case ACK_CODE_396:
                    return ctx.getResources().getString(R.string.ack_396);
                case ACK_CODE_397:
                    return ctx.getResources().getString(R.string.ack_397);
                case ACK_CODE_398:
                    return ctx.getResources().getString(R.string.ack_398);
                case ACK_CODE_399:
                    return ctx.getResources().getString(R.string.ack_399);
                case ACK_CODE_400:
                    return ctx.getResources().getString(R.string.ack_400);
                case ACK_CODE_401:
                    return ctx.getResources().getString(R.string.ack_401);
                case ACK_CODE_402:
                    return ctx.getResources().getString(R.string.ack_402);
                case ACK_CODE_403:
                    return ctx.getResources().getString(R.string.ack_403);
                case ACK_CODE_404:
                    return ctx.getResources().getString(R.string.ack_404);

                case ACK_CODE_405:
                    return ctx.getResources().getString(R.string.ack_405);
                case ACK_CODE_406:
                    return ctx.getResources().getString(R.string.ack_406);
                case ACK_CODE_407:
                    return ctx.getResources().getString(R.string.ack_407);
                case ACK_CODE_408:
                    return ctx.getResources().getString(R.string.ack_408);
                case ACK_CODE_409:
                    return ctx.getResources().getString(R.string.ack_409);
                case ACK_CODE_410:
                    return ctx.getResources().getString(R.string.ack_410);
                case ACK_CODE_411:
                    return ctx.getResources().getString(R.string.ack_411);
                case ACK_CODE_412:
                    return ctx.getResources().getString(R.string.ack_412);
                case ACK_CODE_413:
                    return ctx.getResources().getString(R.string.ack_413);
                case ACK_CODE_414:
                    return ctx.getResources().getString(R.string.ack_414);
                case ACK_CODE_415:
                    return ctx.getResources().getString(R.string.ack_415);
                case ACK_CODE_416:
                    return ctx.getResources().getString(R.string.ack_416);
                case ACK_CODE_417:
                    return ctx.getResources().getString(R.string.ack_417);
                case ACK_CODE_418:
                    return ctx.getResources().getString(R.string.ack_418);
                case ACK_CODE_419:
                    return ctx.getResources().getString(R.string.ack_419);
                case ACK_CODE_420:
                    return ctx.getResources().getString(R.string.ack_420);
                case ACK_CODE_421:
                    return ctx.getResources().getString(R.string.ack_421);
                case ACK_CODE_422:
                    return ctx.getResources().getString(R.string.ack_422);
                case ACK_CODE_423:
                    return ctx.getResources().getString(R.string.ack_423);
                case ACK_CODE_424:
                    return ctx.getResources().getString(R.string.ack_424);
                case ACK_CODE_425:
                    if (readLastVentMode == Configs.MODE_PC_PRVC)
                        return ctx.getResources().getString(R.string.prvc_alarm_custom);
                    else return ctx.getResources().getString(R.string.ack_425);
                case ACK_CODE_426:
                    return ctx.getResources().getString(R.string.ack_426);
                case ACK_CODE_427:
                    return ctx.getResources().getString(R.string.ack_427);
                case ACK_CODE_428:
                    return ctx.getResources().getString(R.string.ack_428);
                case ACK_CODE_429:
                    return ctx.getResources().getString(R.string.ack_429);


                case ACK_CODE_441:
                    return ctx.getResources().getString(R.string.ack_441);
                case ACK_CODE_451:
                    return ctx.getResources().getString(R.string.ack_451);
                case ACK_CODE_430:
                    return ctx.getResources().getString(R.string.ack_430);
                case ACK_CODE_431:
                    return ctx.getResources().getString(R.string.ack_431);
                case ACK_CODE_432:
                    return ctx.getResources().getString(R.string.ack_432);
                case ACK_CODE_433:
                    return ctx.getResources().getString(R.string.ack_433);
                case ACK_CODE_434:
                    return ctx.getResources().getString(R.string.ack_434);
                case ACK_CODE_435:
                    return ctx.getResources().getString(R.string.ack_435);
                case ACK_CODE_436:
                    return ctx.getResources().getString(R.string.ack_436);
                case ACK_CODE_437:
                    return ctx.getResources().getString(R.string.ack_437);
                case ACK_CODE_438:
                    return ctx.getResources().getString(R.string.ack_438);
                case ACK_CODE_439:
                    return ctx.getResources().getString(R.string.ack_439);
                case ACK_CODE_640:
                    return ctx.getResources().getString(R.string.ack_640);
                case ACK_CODE_641:
                    return ctx.getResources().getString(R.string.ack_641);
                case ACK_CODE_642:
                    return ctx.getResources().getString(R.string.ack_642);
                case ACK_CODE_643:
                    return ctx.getResources().getString(R.string.ack_643);
                case ACK_CODE_644:
                    return ctx.getResources().getString(R.string.ack_644);
                case ACK_CODE_645:
                    return ctx.getResources().getString(R.string.ack_645);
                case ACK_CODE_646:
                    return ctx.getResources().getString(R.string.ack_646);
                case ACK_CODE_647:
                    return ctx.getResources().getString(R.string.ack_647);
                case ACK_CODE_648:
                    return ctx.getResources().getString(R.string.ack_648);
                case ACK_CODE_649:
                    return ctx.getResources().getString(R.string.ack_649);
                case ACK_CODE_650:
                    return ctx.getResources().getString(R.string.ack_650);
                case ACK_CODE_651:
                    return ctx.getResources().getString(R.string.ack_651);
                case ACK_CODE_652:
                    return ctx.getResources().getString(R.string.ack_652);
                case ACK_CODE_653:
                    return ctx.getResources().getString(R.string.ack_653);
                case ACK_CODE_654:
                    return ctx.getResources().getString(R.string.ack_654);
                case ACK_CODE_655:
                    return ctx.getResources().getString(R.string.ack_655);
                case ACK_CODE_656:
                    return ctx.getResources().getString(R.string.ack_656);
                case ACK_CODE_657:
                    return ctx.getResources().getString(R.string.ack_657);
                case ACK_CODE_658:
                    return ctx.getResources().getString(R.string.ack_658);
                case ACK_CODE_659:
                    return ctx.getResources().getString(R.string.ack_659);
                case ACK_CODE_660:
                    return ctx.getResources().getString(R.string.ack_660);
                case ACK_CODE_661:
                    return ctx.getResources().getString(R.string.ack_661);
                case ACK_CODE_662:
                    return ctx.getResources().getString(R.string.ack_662);
                case ACK_CODE_663:
                    return ctx.getResources().getString(R.string.ack_663);
                case ACK_CODE_664:
                    return ctx.getResources().getString(R.string.ack_664);
                case ACK_CODE_665:
                    return ctx.getResources().getString(R.string.ack_665);
                case ACK_CODE_666:
                    return ctx.getResources().getString(R.string.ack_666);
                case ACK_CODE_667:
                    return ctx.getResources().getString(R.string.ack_667);
                case ACK_CODE_668:
                    return ctx.getResources().getString(R.string.ack_668);
                case ACK_CODE_669:
                    return ctx.getResources().getString(R.string.ack_669);
                case ACK_CODE_670:
                    return ctx.getResources().getString(R.string.ack_670);
                case ACK_CODE_671:
                    return ctx.getResources().getString(R.string.ack_671);
                case ACK_CODE_672:
                    return ctx.getResources().getString(R.string.ack_672);
                case ACK_CODE_673:
                    return ctx.getResources().getString(R.string.ack_673);
                case ACK_CODE_674:
                    return ctx.getResources().getString(R.string.ack_674);
                case ACK_CODE_675:
                    return ctx.getResources().getString(R.string.ack_675);
                case ACK_CODE_676:
                    return ctx.getResources().getString(R.string.ack_676);
                case ACK_CODE_677:
                    return ctx.getResources().getString(R.string.ack_677);
                case ACK_CODE_678:
                    return ctx.getResources().getString(R.string.ack_678);
                case ACK_CODE_679:
                    return ctx.getResources().getString(R.string.ack_679);
                case ACK_CODE_680:
                    return ctx.getResources().getString(R.string.ack_680);
                case ACK_CODE_681:
                    return ctx.getResources().getString(R.string.ack_681);
                case ACK_CODE_682:
                    return ctx.getResources().getString(R.string.ack_682);
                case ACK_CODE_683:
                    return ctx.getResources().getString(R.string.ack_683);
                case ACK_CODE_684:
                    return ctx.getResources().getString(R.string.ack_684);
                case ACK_CODE_685:
                    return ctx.getResources().getString(R.string.ack_685);
                case ACK_CODE_686:
                    return ctx.getResources().getString(R.string.ack_686);
                case ACK_CODE_687:
                    return ctx.getResources().getString(R.string.ack_687);
                case ACK_CODE_688:
                    return ctx.getResources().getString(R.string.ack_688);
                case ACK_CODE_689:
                    return ctx.getResources().getString(R.string.ack_689);
                case ACK_CODE_690:
                    return ctx.getResources().getString(R.string.ack_690);
                case ACK_CODE_691:
                    return ctx.getResources().getString(R.string.ack_691);
                case ACK_CODE_692:
                    return ctx.getResources().getString(R.string.ack_692);
                case ACK_CODE_693:
                    return ctx.getResources().getString(R.string.ack_693);
                case ACK_CODE_694:
                    return ctx.getResources().getString(R.string.ack_694);
                case ACK_CODE_695:
                    return ctx.getResources().getString(R.string.ack_695);
                case ACK_CODE_696:
                    return ctx.getResources().getString(R.string.ack_696);
                case ACK_CODE_697:
                    return ctx.getResources().getString(R.string.ack_697);
                case ACK_CODE_698:
                    return ctx.getResources().getString(R.string.ack_698);
                case ACK_CODE_699:
                    return ctx.getResources().getString(R.string.ack_699);
                case ACK_CODE_700:
                    return ctx.getResources().getString(R.string.ack_700);
                case ACK_CODE_701:
                    return ctx.getResources().getString(R.string.ack_701);
                case ACK_CODE_702:
                    return ctx.getResources().getString(R.string.ack_702);
                case ACK_CODE_703:
                    return ctx.getResources().getString(R.string.ack_703);
                case ACK_CODE_704:
                    return ctx.getResources().getString(R.string.ack_704);
                case ACK_CODE_705:
                    return ctx.getResources().getString(R.string.ack_705);
                case ACK_CODE_706:
                    return ctx.getResources().getString(R.string.ack_706);
                case ACK_CODE_707:
                    return ctx.getResources().getString(R.string.ack_707);
                case ACK_CODE_708:
                    return ctx.getResources().getString(R.string.ack_708);
                case ACK_CODE_709:
                    return ctx.getResources().getString(R.string.ack_709);
                case ACK_CODE_710:
                    return ctx.getResources().getString(R.string.ack_710);
                case ACK_CODE_711:
                    return ctx.getResources().getString(R.string.ack_711);
                case ACK_CODE_712:
                    return ctx.getResources().getString(R.string.ack_712);
                case ACK_CODE_713:
                    return ctx.getResources().getString(R.string.ack_713);
                case ACK_CODE_714:
                    return ctx.getResources().getString(R.string.ack_714);
                case ACK_CODE_715:
                    return ctx.getResources().getString(R.string.ack_715);
                case ACK_CODE_716:
                    return ctx.getResources().getString(R.string.ack_716);
                case ACK_CODE_717:
                    return ctx.getResources().getString(R.string.ack_717);
                case ACK_CODE_718:
                    return ctx.getResources().getString(R.string.ack_718);
                case ACK_CODE_719:
                    return ctx.getResources().getString(R.string.ack_719);
                case ACK_CODE_720:
                    return ctx.getResources().getString(R.string.ack_720);
                case ACK_CODE_721:
                    return ctx.getResources().getString(R.string.ack_721);
                case ACK_CODE_722:
                    return ctx.getResources().getString(R.string.ack_722);
                case ACK_CODE_723:
                    return ctx.getResources().getString(R.string.ack_723);
                case ACK_CODE_724:
                    return ctx.getResources().getString(R.string.ack_724);
                case ACK_CODE_725:
                    return ctx.getResources().getString(R.string.ack_725);
                case ACK_CODE_726:
                    return ctx.getResources().getString(R.string.ack_726);
                case ACK_CODE_727:
                    return ctx.getResources().getString(R.string.ack_727);
                case ACK_CODE_728:
                    return ctx.getResources().getString(R.string.ack_728);
                case ACK_CODE_729:
                    return ctx.getResources().getString(R.string.ack_729);
                case ACK_CODE_730:
                    return ctx.getResources().getString(R.string.ack_730);
                case ACK_CODE_731:
                    return ctx.getResources().getString(R.string.ack_731);
                case ACK_CODE_732:
                    return ctx.getResources().getString(R.string.ack_732);
                case ACK_CODE_733:
                    return ctx.getResources().getString(R.string.ack_733);
                case ACK_CODE_734:
                    return ctx.getResources().getString(R.string.ack_734);
                case ACK_CODE_735:
                    return ctx.getResources().getString(R.string.ack_735);
                case ACK_CODE_736:
                    return ctx.getResources().getString(R.string.ack_736);
                case ACK_CODE_737:
                    return ctx.getResources().getString(R.string.ack_737);
                case ACK_CODE_738:
                    return ctx.getResources().getString(R.string.ack_738);
                case ACK_CODE_739:
                    return ctx.getResources().getString(R.string.ack_739);
                case ACK_CODE_740:
                    return ctx.getResources().getString(R.string.ack_740);
                case ACK_CODE_741:
                    return ctx.getResources().getString(R.string.ack_741);
                case ACK_CODE_742:
                    return ctx.getResources().getString(R.string.ack_742);
                case ACK_CODE_743:
                    return ctx.getResources().getString(R.string.ack_743);
                case ACK_CODE_744:
                    return ctx.getResources().getString(R.string.ack_744);
                case ACK_CODE_745:
                    return ctx.getResources().getString(R.string.ack_745);
                case ACK_CODE_746:
                    return ctx.getResources().getString(R.string.ack_746);
                case ACK_CODE_747:
                    return ctx.getResources().getString(R.string.ack_747);
                case ACK_CODE_748:
                    return ctx.getResources().getString(R.string.ack_748);
                case ACK_CODE_749:
                    return ctx.getResources().getString(R.string.ack_749);
                case ACK_CODE_750:
                    return ctx.getResources().getString(R.string.ack_750);
                case ACK_CODE_751:
                    return ctx.getResources().getString(R.string.ack_751);
                case ACK_CODE_752:
                    return ctx.getResources().getString(R.string.ack_752);
                case ACK_CODE_753:
                    return ctx.getResources().getString(R.string.ack_753);
                case ACK_CODE_754:
                    return ctx.getResources().getString(R.string.ack_754);
                case ACK_CODE_755:
                    return ctx.getResources().getString(R.string.ack_755);
                case ACK_CODE_756:
                    return ctx.getResources().getString(R.string.ack_756);
                case ACK_CODE_757:
                    return ctx.getResources().getString(R.string.ack_757);
                case ACK_CODE_758:
                    return ctx.getResources().getString(R.string.ack_758);
                case ACK_CODE_759:
                    return ctx.getResources().getString(R.string.ack_759);
                case ACK_CODE_760:
                    return ctx.getResources().getString(R.string.ack_760);
                case ACK_CODE_761:
                    return ctx.getResources().getString(R.string.ack_761);
                case ACK_CODE_762:
                    return ctx.getResources().getString(R.string.ack_762);

                case ACK_CODE_763:
                    return ctx.getResources().getString(R.string.ack_763);
                case ACK_CODE_764:
                    return ctx.getResources().getString(R.string.ack_764);
                case ACK_CODE_765:
                    return ctx.getResources().getString(R.string.ack_765);
                case ACK_CODE_766:
                    return ctx.getResources().getString(R.string.ack_766);
                case ACK_CODE_767:
                    return ctx.getResources().getString(R.string.ack_767);
                case ACK_CODE_768:
                    return ctx.getResources().getString(R.string.ack_768);
                case ACK_CODE_769:
                    return ctx.getResources().getString(R.string.ack_769);
                case ACK_CODE_770:
                    return ctx.getResources().getString(R.string.ack_770);
                case ACK_CODE_771:
                    return ctx.getResources().getString(R.string.ack_771);
                case ACK_CODE_772:
                    return ctx.getResources().getString(R.string.ack_772);
                case ACK_CODE_773:
                    return ctx.getResources().getString(R.string.ack_773);
                case ACK_CODE_774:
                    return ctx.getResources().getString(R.string.ack_774);
                case ACK_CODE_775:
                    return ctx.getResources().getString(R.string.ack_775);
                case ACK_CODE_776:
                    return ctx.getResources().getString(R.string.ack_776);
                case ACK_CODE_777:
                    return ctx.getResources().getString(R.string.ack_777);
                case ACK_CODE_778:
                    return ctx.getResources().getString(R.string.ack_778);
                case ACK_CODE_779:
                    return ctx.getResources().getString(R.string.ack_779);
                case ACK_CODE_780:
                    return ctx.getResources().getString(R.string.ack_780);
                case ACK_CODE_781:
                    return ctx.getResources().getString(R.string.ack_781);
                case ACK_CODE_782:
                    return ctx.getResources().getString(R.string.ack_782);
                case ACK_CODE_783:
                    return ctx.getResources().getString(R.string.ack_783);
                case ACK_CODE_784:
                    return ctx.getResources().getString(R.string.ack_784);
                case ACK_CODE_785:
                    return ctx.getResources().getString(R.string.ack_785);
                case ACK_CODE_786:
                    return ctx.getResources().getString(R.string.ack_786);
                case ACK_CODE_787:
                    return ctx.getResources().getString(R.string.ack_787);
                case ACK_CODE_788:
                    return ctx.getResources().getString(R.string.ack_788);
                case ACK_CODE_789:
                    return ctx.getResources().getString(R.string.ack_789);
                case ACK_CODE_790:
                    return ctx.getResources().getString(R.string.ack_790);
                case ACK_CODE_791:
                    return ctx.getResources().getString(R.string.ack_791);
                case ACK_CODE_792:
                    return ctx.getResources().getString(R.string.ack_792);
                case ACK_CODE_793:
                    return ctx.getResources().getString(R.string.ack_793);
                case ACK_CODE_794:
                    return ctx.getResources().getString(R.string.ack_794);
                case ACK_CODE_795:
                    return ctx.getResources().getString(R.string.ack_795);
                case ACK_CODE_796:
                    return ctx.getResources().getString(R.string.ack_796);
                case ACK_CODE_797:
                    return ctx.getResources().getString(R.string.ack_797);
                case ACK_CODE_799:
                    return ctx.getResources().getString(R.string.ack_799);
                case ACK_CODE_800:
                    return ctx.getResources().getString(R.string.ack_800);
                case ACK_CODE_801:
                    return ctx.getResources().getString(R.string.ack_801);
                case ACK_CODE_802:
                    return ctx.getResources().getString(R.string.ack_802);
                case ACK_CODE_803:
                    return ctx.getResources().getString(R.string.ack_803);
                case ACK_CODE_804:
                    return ctx.getResources().getString(R.string.ack_804);
                case ACK_CODE_805:
                    return ctx.getResources().getString(R.string.ack_805);
                case ACK_CODE_806:
                    return ctx.getResources().getString(R.string.ack_806);
                case ACK_CODE_807:
                    return ctx.getResources().getString(R.string.ack_807);
                case ACK_CODE_809:
                    return ctx.getResources().getString(R.string.ack_809);
                case ACK_CODE_810:
                    return ctx.getResources().getString(R.string.ack_810);
                case ACK_CODE_811:
                    return ctx.getResources().getString(R.string.ack_811);
                case ACK_CODE_812:
                    return ctx.getResources().getString(R.string.ack_812);
                case ACK_CODE_813:
                    return ctx.getResources().getString(R.string.ack_813);
                case ACK_CODE_814:
                    return ctx.getResources().getString(R.string.ack_814);
                case ACK_CODE_815:
                    return ctx.getResources().getString(R.string.ack_815);
                case ACK_CODE_816:
                    return ctx.getResources().getString(R.string.ack_816);
                case ACK_CODE_817:
                    return ctx.getResources().getString(R.string.ack_817);
                case ACK_CODE_819:
                    return ctx.getResources().getString(R.string.ack_819);
                case ACK_CODE_821:
                    return ctx.getResources().getString(R.string.ack_821);
                case ACK_CODE_822:
                    return ctx.getResources().getString(R.string.ack_822);
                case ACK_CODE_823:
                    return ctx.getResources().getString(R.string.ack_823);

                case ACK_CODE_824:
                    return ctx.getResources().getString(R.string.ack_824);
                case ACK_CODE_825:
                    return ctx.getResources().getString(R.string.ack_825);
                case ACK_CODE_826:
                    return ctx.getResources().getString(R.string.ack_826);
                case ACK_CODE_827:
                    return ctx.getResources().getString(R.string.ack_827);


                case ACK_CODE_831:
                    return ctx.getResources().getString(R.string.ack_831);
                case ACK_CODE_832:
                    return ctx.getResources().getString(R.string.ack_832);

                case ACK_CODE_833:
                    return ctx.getResources().getString(R.string.ack_833);

                case ACK_CODE_834:
                    return ctx.getResources().getString(R.string.ack_834);

                case ACK_CODE_835:
                    return ctx.getResources().getString(R.string.ack_835);
                case ACK_CODE_836:
                    return ctx.getResources().getString(R.string.ack_836);
                case ACK_CODE_837:
                    return ctx.getResources().getString(R.string.ack_837);
                default:
                    return "ERROR " + code;
            }
        }
    }
}
