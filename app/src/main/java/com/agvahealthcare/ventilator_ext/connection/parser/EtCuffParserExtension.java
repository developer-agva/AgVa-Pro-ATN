package com.agvahealthcare.ventilator_ext.connection.parser;

import java.util.LinkedHashMap;
import java.util.Map;

public class EtCuffParserExtension extends ParserExtension {

    public static final String TYPE_DEVICE_ETCUFF = "N";


    public static final String DATA_ET_PRESSURE = "ETpressure";
    public static final String DATA_ET_MODE = "mode";


    @Override
    public Map<String, Map<String, String>> getDataMap() {
        Map<String, Map<String, String>> map = new LinkedHashMap<>();
        map.put(TYPE_DEVICE_ETCUFF, getConfigMap(DATA_ET_PRESSURE, DATA_ET_MODE)); // preserve the order
        return map;
    }
}
