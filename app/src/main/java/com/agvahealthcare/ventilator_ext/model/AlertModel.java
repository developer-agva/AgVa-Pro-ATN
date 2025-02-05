package com.agvahealthcare.ventilator_ext.model;

public class AlertModel {

    private String message;
    private int color;
    private int icon;

    public AlertModel(String message, int color) {
        this.message = message;
        this.color = color;
    }

    public AlertModel(String message, int color, int icon) {
        this.message = message;
        this.color = color;
        this.icon = icon;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
