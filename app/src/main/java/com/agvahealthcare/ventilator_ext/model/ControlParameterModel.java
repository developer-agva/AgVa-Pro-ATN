package com.agvahealthcare.ventilator_ext.model;


import com.agvahealthcare.ventilator_ext.utility.utils.Configs;


public class ControlParameterModel {

    // Make them all private
    private String title;
    private String reading;
    private String unit;
    private String ventKey;
    private Double  upperLimit;
    private Double  lowerLimit;
    private Double  step;
    private boolean isselected ;

    public static ControlParameterModel empty(){
        return new ControlParameterModel();
    }


    private ControlParameterModel(){}

    public ControlParameterModel(String ventKey, String title, String reading, String unit, Double upperLimit, Double lowerLimit, Double step) {
        this.title = title;
        this.reading = reading;
        this.unit = unit;
        this.ventKey = ventKey;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
        this.step = step;
    }

    public Double getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Double upperLimit) {
        this.upperLimit = upperLimit;
    }

    public Double getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Double lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public String getVentKey() {
        return ventKey;
    }

    public void setVentKey(String ventKey) {
        this.ventKey = ventKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReading() {
        return reading;
    }

    public void setReading(String reading) {
        this.reading = reading;
    }

    public String getUnits() {
        return unit;
    }

    public void setUnits(String unit) {
        this.unit = unit;
    }

    public Double getStep() {
        return step;
    }

    public void setStep(Double step) {
        this.step = step;
    }

    public boolean isIsselected() {
        return isselected;
    }

    public void setIsselected(boolean isselected) {
        this.isselected = isselected;
    }

    public boolean isVoid(){
        return !Configs.LBL_VOID_TILE.equals( ventKey ) && (title != null && !title.isEmpty()) && (reading != null && !reading.isEmpty());
    }

    public boolean isEmpty() {
        return this.ventKey == null && title == null && reading == null;
    }


    @Override
    public String toString() {
        return "ControlParameterModel{" +
                "title='" + title + '\'' +
                ", reading='" + reading + '\'' +
                ", unit='" + unit + '\'' +
                ", ventKey='" + ventKey + '\'' +
                ", upperLimit=" + upperLimit +
                ", lowerLimit=" + lowerLimit +
                ", step=" + step +
                ", isselected=" + isselected +
                '}';
    }
}
