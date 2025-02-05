package com.agvahealthcare.ventilator_ext.model;

import android.util.Log;

import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils;

public class SensorCalibration {
    private  String date;
    private  int status;


    public SensorCalibration(String date, int status) {
        this.date = date;
        this.status = status;
    }

    public SensorCalibration(int status) {
        this(AppUtils.getCurrentDateTime(), status);
        Log.i("Sensor_Calib_Date_time",String.valueOf(AppUtils.getCurrentDateTime()));
    }

    public String getDate() {
        return date;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "SensorCalibration{" +
                "date='" + date + '\'' +
                ", status=" + status +
                '}';
    }
}
