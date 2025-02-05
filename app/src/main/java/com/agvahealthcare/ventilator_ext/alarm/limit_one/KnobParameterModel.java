package com.agvahealthcare.ventilator_ext.alarm.limit_one;

import com.agvahealthcare.ventilator_ext.model.ControlParameterModel;

public class KnobParameterModel {

    final static int DEFAULT_CODE = 1;

    private String key;
    private String name;
    private int code;
    private float reading;
    private String unit;

    public static KnobParameterModel fromControlParameter(ControlParameterModel param) throws NullPointerException, NumberFormatException{
        return new KnobParameterModel(
                param.getVentKey(),
                param.getTitle(),
                DEFAULT_CODE,
                Float.parseFloat(param.getReading()),
                param.getUnits()
        );
    }

    public KnobParameterModel(String key, String name, int code, float currentValue, String unit) {
        this.key=key;
        this.name = name;
        this.code = code;
        this.reading = currentValue;
        this.unit = unit;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public float getReading() {
        return reading;
    }

    public void setReading(float reading) {
        this.reading = reading;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}