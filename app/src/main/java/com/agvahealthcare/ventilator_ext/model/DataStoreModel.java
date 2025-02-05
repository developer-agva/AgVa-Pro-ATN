package com.agvahealthcare.ventilator_ext.model;

/**
 * Created by MOHIT MALHOTRA on 14-11-2018.
 */

public class DataStoreModel {

    private int id;
    private float pressure;
    private float volume;
    private float rr;
    private float fiO2;
    private float mve;
    private float vti;
    private float vte;
    private float leak;
    private float peep;
    private float ieRatio;
    private String time;
    //additional values added
    private float tinsp;
    private float texp;
    private float mvi;
    private float trigger;
    private float spO2;
    private float pR;
    private float meanAirwayPressure;
    private float rsbi;
    private String mode;


    public DataStoreModel() {}

    public DataStoreModel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
    //changes to integrate vti in the database
    public float getVti() {
        return vti;
    }
    //changes to integrate vti in the database
    public void setVti(float vti) {
        this.vti = vti;
    }

    public float getSpO2(){
        return spO2;
    }
    public void setSpO2(float spO2){
        this.spO2 = spO2;
    }

    public float getpR(){
        return pR;
    }
    public void setpR(float pR){
        this.pR = pR;
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

    public void setRR(float rr) {
        this.rr = rr;
    }


    public float getFiO2() {
        return fiO2;
    }

    public void setFiO2(float fiO2) {
        this.fiO2 = fiO2;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getMve() {
        return mve;
    }

    public void setMve(float mve) {
        this.mve = mve;
    }

    public float getVte() {
        return vte;
    }

    public void setVte(float vte) {
        this.vte = vte;
    }

    public float getLeak() {
        return leak;
    }

    public void setLeak(float leak) {
        this.leak = leak;
    }

    public Float getPeep() {
        return peep;
    }

    public void setPeep(Float peep) {
        this.peep = peep;
    }

    public float getIeRatio() {
        return ieRatio;
    }

    public void setIeRatio(float ieRatio) {
        this.ieRatio = ieRatio;
    }

    public float getTinsp() {
        return tinsp;
    }

    public void setTinsp(float tinsp) {
        this.tinsp = tinsp;
    }

    public float getTexp() {
        return texp;
    }

    public void setTexp(float texp) {
        this.texp = texp;
    }

    public float getMvi() {
        return mvi;
    }

    public void setMvi(float mvi) {
        this.mvi = mvi;
    }

    public float getTrigger() {
        return trigger;
    }

    public void setTrigger(float trigger) {
        this.trigger = trigger;
    }

    public float getMeanAirwayPressure() {
        return meanAirwayPressure;
    }

    public void setMeanAirwayPressure(float meanAirwayPressure) {
        this.meanAirwayPressure = meanAirwayPressure;
    }

    @Override
    public String toString() {
        return "DataStoreModel{" +
                "pressure=" + pressure +
                ", volume=" + volume +
                ", rr=" + rr +
                ", fiO2=" + fiO2 +
                ", mve=" + mve +
                ", vte=" + vte +
                ", leak=" + leak +
                ", peep=" + peep +
                ", ieRatio=" + ieRatio +
                ", mode=" + mode +
                ", time='" + time + '\'' +
                ", spo2='" + spO2 + '\'' +
                ", pr='" + pR + '\'' +
                '}';
    }
}
