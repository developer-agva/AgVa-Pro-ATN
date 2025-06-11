package com.agvahealthcare.ventilator_ext.connection.support_threads;

import android.os.CountDownTimer;
import android.util.Log;

import com.agvahealthcare.ventilator_ext.service.CommunicationService;
import com.agvahealthcare.ventilator_ext.service.UsbService;
import com.agvahealthcare.ventilator_ext.utility.utils.Configs;


public class PingingTask
{

    private static String TAG = "PINGING THREAD";

    private final static long PINGING_INTERVAL = 1000 * 3; // 10 secs
    private final static long PINGING_SESSION_LIFE = 1000 * 60 * 60; // 60 mins

    private boolean isRunning;

    private CommunicationService service;

    public PingingTask(CommunicationService service){
        this.service = service;
    }


    private final CountDownTimer pingingTimer = new CountDownTimer(PINGING_SESSION_LIFE, PINGING_INTERVAL) {
        @Override
        public void onTick(long millisUntilFinished) {
            sendPinging();

        }

        @Override
        public void onFinish() {
            Log.w(TAG, "Pinging thread rebooted itself");
//            this.start();
        }
    };

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setRunningState(boolean isRunning) {
        this.isRunning = isRunning;
    }

    /*
     * Sends handshake to ventilator
     * NOTE : use only after gatt connects
     */

    private void sendPinging(){
        // INFORM VENTILATOR ABOUT THE CONNECTION
        if(service != null) {
           if(service instanceof UsbService){
                Log.i(TAG, "Pinging ventilator through USB");
                service.send("_");
                service.sendDatatoKnob("-");
            }
        }
    }

    public void start(){
        if(pingingTimer != null && !isRunning){
            pingingTimer.start();
            isRunning = true;
            Log.w(TAG, "Pinging thread started");
        }
    }

    public void stop(){
        if(pingingTimer != null){
            pingingTimer.cancel();
            isRunning = false;
            Log.w(TAG, "Pinging thread killed");
        }
    }
}
