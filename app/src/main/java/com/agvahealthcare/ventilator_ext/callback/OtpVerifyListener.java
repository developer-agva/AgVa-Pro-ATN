package com.agvahealthcare.ventilator_ext.callback;

@FunctionalInterface
public interface OtpVerifyListener {

    void doAction(String otp,Boolean isServiceOpen);
}
