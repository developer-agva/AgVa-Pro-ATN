package com.agvahealthcare.ventilator_ext.connection.parser;

import java.util.LinkedHashMap;
import java.util.Map;

public class SpO2ParserExtension extends ParserExtension {

    public static final String TYPE_SPO2 = "K";
    public static final String TYPE_HEARTRATE = "L";
    public static final String TYPE_PLETHMOGRAPH = "M";

    public static final String DATA_SPO2 = "spo2";
    public static final String DATA_HEARTRATE = "hb";
    public static final String DATA_PLETHMOGRAPH = "pleth";

    private Map<String, Map<String, String>> dataMap;

    @Override
    public Map<String, Map<String, String>> getDataMap() {
        Map<String, Map<String, String>> map = new LinkedHashMap<>();
        map.put(TYPE_SPO2, getConfigMap(DATA_SPO2));
        map.put(TYPE_HEARTRATE, getConfigMap(DATA_HEARTRATE));
        map.put(TYPE_PLETHMOGRAPH, getConfigMap(DATA_PLETHMOGRAPH));

        return map;
    }
}
