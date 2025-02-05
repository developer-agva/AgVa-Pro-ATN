package com.agvahealthcare.ventilator_ext.alarm.limit_one;

public class ViewHolder {

    private Float defaultMin;
    private Float defaultMax;
    private Float actualMin;
    private Float actualMax;

    public ViewHolder(Float defaultMin, Float defaultMax, Float actualMin, Float actualMax) {
        this.defaultMin = defaultMin;
        this.defaultMax = defaultMax;
        this.actualMin = actualMin;
        this.actualMax = actualMax;
    }

    public Float getDefaultMin() {
        return defaultMin;
    }

    public void setDefaultMin(Float defaultMin) {
        this.defaultMin = defaultMin;
    }

    public Float getDefaultMax() {
        return defaultMax;
    }

    public void setDefaultMax(Float defaultMax) {
        this.defaultMax = defaultMax;
    }

    public Float getActualMin() {
        return actualMin;
    }

    public void setActualMin(Float actualMin) {
        this.actualMin = actualMin;
    }

    public Float getActualMax() {
        return actualMax;
    }

    public void setActualMax(Float actualMax) {
        this.actualMax = actualMax;
    }
}




