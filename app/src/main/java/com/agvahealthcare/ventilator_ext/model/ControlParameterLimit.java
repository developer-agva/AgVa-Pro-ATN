package com.agvahealthcare.ventilator_ext.model;

public class ControlParameterLimit {
    private static final float DEFAULT_MIN_VALUE = 0f;
    private static final float DEFAULT_MAX_VALUE = 100f;
    private static final int DEFAULT_VALUE_PER_ROTATION = 1;
    private float minValue = DEFAULT_MIN_VALUE;
    private float maxValue = DEFAULT_MAX_VALUE;
    private float valuePerRotation = DEFAULT_VALUE_PER_ROTATION;

    public ControlParameterLimit() { /* Retain the default configuration*/ }


    public ControlParameterLimit(float minValue, float maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public ControlParameterLimit(float minValue, float maxValue, float valuePerRotation) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.valuePerRotation = valuePerRotation;
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public float getValuePerRotation() {
        return valuePerRotation;
    }

    public void setValuePerRotation(float valuePerRotation) {
        this.valuePerRotation = valuePerRotation;
    }
}
