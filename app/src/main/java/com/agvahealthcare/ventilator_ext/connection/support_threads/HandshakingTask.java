package com.agvahealthcare.ventilator_ext.connection.support_threads;

import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import com.agvahealthcare.ventilator_ext.service.CommunicationService;
import com.agvahealthcare.ventilator_ext.service.UsbService;
import com.agvahealthcare.ventilator_ext.utility.utils.Configs;
import com.agvahealthcare.ventilator_ext.utility.utils.IntentFactory;

import java.io.IOException;


public class HandshakingTask  {

    private static String TAG = "HANDSHAKING THREAD";

    private final static long HANDSHAKING_INTERVAL = 1000L ; // 1 secs
    private final static long HANDSHAKING_SESSION_LIFE = 90000L ; // 90 seconds


    private CommunicationService service;
    private boolean isHandshakeRunning;

    public HandshakingTask(CommunicationService service){
        this.service = service;
    }


    private CountDownTimer handshakeTimer = new CountDownTimer(HANDSHAKING_SESSION_LIFE, HANDSHAKING_INTERVAL) {
        @Override
        public void onTick(long millisUntilFinished) {
            Log.i(TAG,"HERERE_RUNN");
            sendHandshake();
        }

        @Override
        public void onFinish() {
            isHandshakeRunning = false;
            Log.w(TAG, "Unable to complete double handshaking");
            service.getApplicationContext().sendBroadcast(new Intent(IntentFactory.ACTION_HANDSHAKE_TIMEOUT));

        }
    };


    /*
     * Sends handshake to ventilator
     * NOTE : use only after gatt connects
     */
    private void sendHandshake() {
        // INFORM VENTILATOR ABOUT THE CONNECTION
        if(service != null && service.isPortsConnected()) {
           if(service instanceof UsbService){
                Log.i(TAG, "Sending handshake through USB");
                service.send(Configs.INFORM_HANDSHAKE);
            }
            else {
                service.getApplicationContext().sendBroadcast(new Intent(IntentFactory.ACTION_DEVICE_DISCONNECTED));
            }
        }
    }


    public void start(){
        if(handshakeTimer != null){
            handshakeTimer.start();
            isHandshakeRunning = true;
            Log.w(TAG, "Handshaking thread started");
        }
    }


    public void stop(){
        if(handshakeTimer != null){
            handshakeTimer.cancel();
            Log.w(TAG, "Handshaking thread killed");
            // SEND DATE TIME TO VENTILATOR
            if(service != null) service.sendCurrentDateTime();
            isHandshakeRunning = false;
        }
    }

    public boolean isRunning() {
        return isHandshakeRunning;
    }
}
