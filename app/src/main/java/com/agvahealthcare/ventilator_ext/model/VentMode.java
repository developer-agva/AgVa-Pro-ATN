package com.agvahealthcare.ventilator_ext.model;

/**
 * Created by MOHIT MALHOTRA on 22-10-2018.
 */

public class VentMode {

    private String controlMode;
    private String modeType;
    private int modeCode;

    public VentMode(String controlMode, String modeType, int modeCode) {
        this.controlMode = controlMode;
        this.modeType = modeType;
        this.modeCode = modeCode;
    }

    public String getControlMode() {
        return controlMode;
    }

    public void setControlMode(String controlMode) {
        this.controlMode = controlMode;
    }

    public String getModeType() {
        return modeType;
    }

    public void setModeType(String modeType) {
        this.modeType = modeType;
    }

    public int getModeCode() {
        return modeCode;
    }

    public void setModeCode(int modeCode) {
        this.modeCode = modeCode;
    }
}
