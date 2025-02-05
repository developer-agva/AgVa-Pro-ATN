package com.agvahealthcare.ventilator_ext.database.entities;

import com.agvahealthcare.ventilator_ext.model.DataStoreModel;

public class DataTableModel {
    private String time;
    private float pressure;
    private float volume;
    private float rr;
    private float fiO2;
    private float peep;
    private float leak;
    private float mve;
    private float ieratio;


    public DataTableModel(DataStoreModel DataStoreModel) {
        setTime(DataStoreModel.getTime());
        setPressure(DataStoreModel.getPressure());
        setVolume(DataStoreModel.getVolume());
        setRr(DataStoreModel.getRr());
        setFiO2(DataStoreModel.getFiO2());
        setPeep(DataStoreModel.getPeep());
        setLeak(DataStoreModel.getLeak());
        setMve(DataStoreModel.getMve());
        setIeratio(DataStoreModel.getIeRatio());
    }

    public float getPeep() { return peep; }
    public void setPeep(float peep) { this.peep = peep; }

    public float getLeak() { return leak; }
    public void setLeak(float leak) { this.leak = leak; }

    public float getMve() { return mve; }
    public void setMve(float mve) { this.mve = mve; }

    public float getIeratio() { return ieratio; }
    public void setIeratio(float ieratio) { this.ieratio = ieratio; }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public float getPressure() {
        return pressure;
    }
    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getVolume() {
        return volume;
    }
    public void setVolume(float volume) {
        this.volume = volume;
    }

    public float getRr() {
        return rr;
    }
    public void setRr(float rr) {
        this.rr = rr;
    }

    public float getFiO2() { return fiO2; }
    public void setFiO2(float fiO2) { this.fiO2 = fiO2; }
}
