package com.agvahealthcare.ventilator_ext.model;

public class AckAlarmDatabaseModel {
    private  String ackCode;
    private  String message;
    private  String startDateTime;
    private  String endDateTime;

    public AckAlarmDatabaseModel(String ackCode, String message, String startDateTime, String endDateTime) {
        this.ackCode = ackCode;
        this.message = message;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public String getAckCode() {
        return ackCode;
    }

    public void setAckCode(String ackCode) {
        this.ackCode = ackCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }
}
