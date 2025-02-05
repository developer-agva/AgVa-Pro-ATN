package com.agvahealthcare.ventilator_ext.model;

import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.AlarmType;

import com.agvahealthcare.ventilator_ext.api.model.alarmDataModel.Ack;
import com.agvahealthcare.ventilator_ext.database.entities.AlarmDBModel;
import com.agvahealthcare.ventilator_ext.utility.utils.AlarmConfiguration;
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils;

public class AlarmModel {

    private String message;

    private String code;
    private String createdAt;
    private String Uhid;


    public AlarmModel(String message, String code, String createdAt,String uhid) {
        this.message = message;
        this.code = code;
        this.createdAt = createdAt;
        this.Uhid = uhid;
    }

    public AlarmModel(AlarmDBModel model){
        this.code = model.getKey();
        this.message = model.getMessage();
        this.createdAt = model.getCreatedAt();
        this.Uhid = model.getUhid();
    }

    public String getUhid() {
        return Uhid;
    }

    public void setUhid(String uhid) {
        Uhid = uhid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public AlarmType getPriority() { return AlarmConfiguration.getPriority(this.code); }

    public int getColor() { return AlarmConfiguration.getColor(this.code); }

    public AlarmDBModel toDBModel(){
        return new AlarmDBModel(
                getCode(),
                getMessage(),
                getCreatedAt(),
                getUhid()
        );
    }

    public Ack toAckModel(){
        String dateTime = null;
        try{
            dateTime = AppUtils.errorDateTimeFormatter.format(AppUtils.dateTimeFormatter.parse(this.createdAt));
        }
        catch(Exception e){
            dateTime = AppUtils.getCurrentDateReverse();
        }

        return new Ack(
                this.message,
                this.code,
                dateTime,
                getPriority().toString()
        );
    }


}
