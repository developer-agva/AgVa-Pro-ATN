package com.agvahealthcare.ventilator_ext.connection.parser;

import android.util.Log;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by MOHIT MALHOTRA on 13-09-2018.
 */

public class RaspiParser extends ParserUtils
{


    public static final String TYPE_INHALATION = "A";
    public static final String TYPE_END_OF_INHALATION = "B";
    public static final String TYPE_EXHALATION = "C";
    public static final String TYPE_END_OF_EXHALATION = "D";
    public static final String TYPE_TUBE_COMPENSATION = "E";
    public static final String DATA_COMPENSATION = "Compensation";
    public static final String DATA_PRESSURE = "Pressure";
    public static final String DATA_PIP = "PIP";
    public static final String DATA_PMEAN = "Pmean";
    public static final String DATA_VTI = "VTi";
    public static final String DATA_VTE = "VTe";
    public static final String DATA_FLOW = "Flow";
    public static final String DATA_TRIGGER = "Trigger";
    public static final String DATA_MVI = "MVI";
    public static final String DATA_VOLUME = "Vol";
    public static final String DATA_VPEAK_I = "VPeakI";
    public static final String DATA_VPEAK_E = "VPeakE";
    public static final String DATA_TITOT = "Ti/ToT";
    public static final String DATA_PEEP = "PEEP";
    public static final String DATA_MVE = "MVe";
    public static final String DATA_RR = "RR";
    public static final String DATA_FIO2 = "FiO2";
    public static final String DATA_LEAK = "Leak";
    public static final String DATA_LEAK_FLOW = "Leak Flow";
    public static final String DATA_VOLUME_LEAK = "VLeak";
    public static final String DATA_TRIGFLOW = "TrigFlow";
    public static final String DATA_INSPIRE_TIME = "Inspiratory Time";
    public static final String DATA_EXPIRE_TIME = "Expiratory Time";
    public static final String DATA_PLATEAU_PRESSURE = "Plateau Pressure";
    public static final String DATA_MEAN_AIRWAY_PRESSURE = "Mean Airway Pressure";
    public static final String DATA_RISE_TIME = "Rise Time";
    public static final String DATA_RSBI = "Rsbi";
    public static final String DATA_SPONT_VT = "Spont Vt";
    public static final String DATA_HFNC_FLOW = "HFNC FLOW";
    public static final String DATA_HFNC_FIO2 = "HFNC FIO2";

    // added for diagnostic check start string
    public static final String TYPE_DIA_FIRST = "DS";

    public static final String DATA_INSP_PRESSURE_RAW = "DATA_INSP_PRESSURE_RAW";
    public static final String DATA_EXP_PRESSURE_RAW = "DATA_EXP_PRESSURE_RAW";
    public static final String DATA_OXP_PRESSURE_RAW = "DATA_OXP_PRESSURE_RAW";

    public static final String DATA_INSP_PRESSURE = "DATA_INSP_PRESSURE";
    public static final String DATA_EXP_PRESSURE = "DATA_EXP_PRESSURE";
    public static final String DATA_OXP_PRESSURE = "DATA_OXP_PRESSURE";

    public static final String DATA_INSP_FLOW_VOLTAGE = "DATA_INSP_FLOW_VOLTAGE";
    public static final String DATA_INSP_FLOW = "DATA_INSP_FLOW";
    public static final String DATA_EXP_FLOW = "DATA_EXP_FLOW";
    public static final String DATA_EXP_DP_RAW = "DATA_EXP_DP_RAW";

    // added for diagnostic check MID string
    public static final String TYPE_DIA_MID = "DM";

    public static final String DATA_BTRY_CURRENT = "DATA_BTRY_CURRENT";
    public static final String DATA_BTRY_VOLTAGE = "DATA_BTRY_VOLTAGE";
    public static final String DATA_BTRY_SOC = "DATA_BTRY_SOC";

    public static final String DATA_BTRY_REMAINING_TIME = "DATA_BTRY_REMAINING_TIME";
    public static final String DATA_BTRY_STATE = "DATA_BTRY_STATE";
    public static final String DATA_POWER_CONNECTION = "DATA_POWER_CONNECTION";

    public static final String DATA_MAIN_SWITCH = "DATA_MAIN_SWITCH";
    public static final String DATA_SPO2_STATUS = "DATA_SPO2_STATUS";
    public static final String DATA_HR = "DATA_HR";
    public static final String DATA_Spo2 = "DATA_Spo2";

    // added for diagnostic check LAST string
    public static final String TYPE_DIA_LAST = "DL";

    public static final String DATA_OXYGEN_SENSOR_VOLTAGE = "DATA_OXYGEN_SENSOR_VOLTAGE";
    public static final String DATA_PI_TEMP = "DATA_PI_TEMP";
    public static final String DATA_PI_CPU_LOAD = "DATA_PI_CPU_LOAD";

    public static final String DATA_HARDWARE_VERSION = "DATA_HARDWARE_VERSION";




    private Map<String, Map<String, String>> dataMap;{

        dataMap = new LinkedHashMap<>();
        dataMap.put(TYPE_INHALATION, getConfigMap(DATA_PRESSURE, DATA_FLOW, DATA_VOLUME, DATA_TRIGGER));
        dataMap.put(TYPE_END_OF_INHALATION, getConfigMap(DATA_PIP, DATA_VTI, DATA_VPEAK_I, DATA_PMEAN, DATA_MVI, DATA_TRIGFLOW, DATA_INSPIRE_TIME, DATA_PLATEAU_PRESSURE, DATA_RISE_TIME ,DATA_HFNC_FLOW, DATA_HFNC_FIO2));
        dataMap.put(TYPE_EXHALATION, getConfigMap(DATA_PRESSURE, DATA_FLOW, DATA_VOLUME, DATA_TITOT));
        dataMap.put(TYPE_END_OF_EXHALATION, getConfigMap(DATA_PEEP, DATA_RR, DATA_FIO2, DATA_VPEAK_E, DATA_MVE, DATA_LEAK, DATA_MEAN_AIRWAY_PRESSURE, DATA_VTE,DATA_EXPIRE_TIME, DATA_VTI,DATA_RSBI,DATA_SPONT_VT));
        dataMap.put(TYPE_DIA_FIRST, getConfigMap(DATA_INSP_PRESSURE_RAW,DATA_EXP_PRESSURE_RAW,DATA_OXP_PRESSURE_RAW,DATA_INSP_PRESSURE,DATA_EXP_PRESSURE,DATA_OXP_PRESSURE,DATA_INSP_FLOW_VOLTAGE,DATA_INSP_FLOW,DATA_EXP_DP_RAW,DATA_EXP_FLOW));
        dataMap.put(TYPE_DIA_MID, getConfigMap(DATA_BTRY_CURRENT,DATA_BTRY_VOLTAGE,DATA_BTRY_SOC,DATA_BTRY_REMAINING_TIME,DATA_BTRY_STATE,DATA_POWER_CONNECTION,DATA_MAIN_SWITCH,DATA_SPO2_STATUS,DATA_HR,DATA_Spo2));
        dataMap.put(TYPE_DIA_LAST, getConfigMap(DATA_OXYGEN_SENSOR_VOLTAGE,DATA_PI_TEMP,DATA_PI_CPU_LOAD,DATA_HARDWARE_VERSION,"0","0","0","0","0","0"));
    }

    public RaspiParser addExtension(Class<? extends ParserExtension> extClass){
        if(dataMap != null){
            try {
                ParserExtension ext = extClass.newInstance();
                dataMap.putAll(ext.getDataMap());
                Log.i("PARSE_EXT", "Extension added successfully");
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    public final String getDataType(String msg){
        String type=msg.substring(0,msg.indexOf("@"));
        return type;
    }

    public final Map<String,String> parserForWholeData(String msg){
        String type = msg.substring(0,msg.indexOf("@"));
        String returnValue="empty";
        Map<String, String> selectedMap=null;
        if (type.equalsIgnoreCase("B") || type.equalsIgnoreCase("D")){
            //returnValue=msg;
            selectedMap = dataMap.get(type);
            Log.d("Thevalueofgetintermediatemap",selectedMap.toString());
            String rawData= msg.substring(msg.indexOf("@")+1,msg.indexOf("#"));
            returnValue=rawData;
        } else  {
            returnValue="empty";
            selectedMap=null;
        }
        return selectedMap;
    }

    public final Map<String, Map<String, String>> diaParser(String msg){

        if(!msg.contains("~")){
            Log.i("PARSER EXCEPTION", "Type not defined");
            return null;
        }

        if(!msg.contains(",")){
            Log.i("PARSER EXCEPTION", "No data present");
            return null;
        }

        if(!msg.contains("{")){
            Log.i("PARSER EXCEPTION", "Delimiter not found");
            return null;
        }

        String type = msg.substring(0, msg.indexOf("~"));
        String rawData = msg.substring(msg.indexOf("~") + 1, msg.indexOf("{"));

        String[] data = rawData.split(",");

        Map<String, String> selectedMap = dataMap.get(type);

        // NPE safety check return
        if(selectedMap == null) return null;

        Iterator<String> iterator = selectedMap.keySet().iterator();
        for(String datum : data){
            if(iterator.hasNext()){
                selectedMap.put(iterator.next(), datum);
            }
        }
        Map<String, Map<String, String>> retMap = new LinkedHashMap<>();
        retMap.put(type, selectedMap);

        return retMap;

    }


    public final Map<String, Map<String, String>> parser(String msg){

        if(!msg.contains("@")){
            Log.i("PARSER EXCEPTION", "Type not defined");
            return null;
        }

        if(!msg.contains(",")){
            Log.i("PARSER EXCEPTION", "No data present");
            return null;
        }

        if(!msg.contains("#")){
            Log.i("PARSER EXCEPTION", "Delimiter not found");
            return null;
        }

        String type = msg.substring(0, msg.indexOf("@"));
        String rawData = msg.substring(msg.indexOf("@") + 1, msg.indexOf("#"));

        String[] data = rawData.split(",");

        Map<String, String> selectedMap = dataMap.get(type);

        // NPE safety check return
        if(selectedMap == null) return null;

        Iterator<String> iterator = selectedMap.keySet().iterator();
        for(String datum : data){
            if(iterator.hasNext()){
                selectedMap.put(iterator.next(), datum);
            }
        }
        Map<String, Map<String, String>> retMap = new LinkedHashMap<>();
        retMap.put(type, selectedMap);

        return retMap;

    }




}

