package com.agvahealthcare.ventilator_ext.connection.parser;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class ParserUtils {
    public static Map<String, String> getConfigMap(String... params){
        Map<String, String> map = new LinkedHashMap<>();
        for(String p : params) map.put(p, "");
        return map;
    }
}
