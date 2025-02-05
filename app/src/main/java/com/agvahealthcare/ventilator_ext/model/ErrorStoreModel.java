package com.agvahealthcare.ventilator_ext.model;

public class ErrorStoreModel {

    private int id;
    private String errorCode;
    private String time;

    public ErrorStoreModel(){}

    public ErrorStoreModel(int id) {
        this.id = id;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
