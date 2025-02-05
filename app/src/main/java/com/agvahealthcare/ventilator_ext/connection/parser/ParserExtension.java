package com.agvahealthcare.ventilator_ext.connection.parser;

import java.util.Map;

public abstract class ParserExtension extends ParserUtils {
    public abstract Map<String, Map<String, String>> getDataMap();
}