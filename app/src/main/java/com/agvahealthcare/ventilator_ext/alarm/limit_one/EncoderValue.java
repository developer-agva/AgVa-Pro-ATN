package com.agvahealthcare.ventilator_ext.alarm.limit_one;

public  class  EncoderValue{
    Float lowerLimit;
    Float upperLimit;
    Float step;



    public EncoderValue(Float lowerLimit, Float upperLimit, Float step) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.step = step;
    }

    public Float getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(Float lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    public Float getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(Float upperLimit) {
        this.upperLimit = upperLimit;
    }

    public Float getStep() {
        return step;
    }

    public void setStep(Float step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return "EncoderValue{" +
                "lowerLimit=" + lowerLimit +
                ", upperLimit=" + upperLimit +
                ", step=" + step +
                '}';
    }
}