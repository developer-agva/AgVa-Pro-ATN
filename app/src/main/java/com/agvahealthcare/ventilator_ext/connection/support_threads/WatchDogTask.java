package com.agvahealthcare.ventilator_ext.connection.support_threads;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import com.agvahealthcare.ventilator_ext.utility.utils.IntentFactory;


/*
* WATCH DOG : Watches whether data is coming from the ventilator periodically
* Surveillancing operations are done with broadcasting
 */
public class WatchDogTask {

    private final static long INACTIVE_RESPONSE_WINDOW = 6000;
    private final static long SURVEILLANCE_SESSION_LIFE = 1000 * 60 * 60;
    private final static long SURVEILLANCE_INTERVAL = 1000;

    private Context context;
    private long lastUpdationTimes;
    private boolean isActive;

    public WatchDogTask(Context context) {
        this.context = context;
    }

    private CountDownTimer watchDogTimer = new CountDownTimer(SURVEILLANCE_SESSION_LIFE, SURVEILLANCE_INTERVAL) {


        @Override
        public void onTick(long millisUntilFinished) {
            if( !isUpdationPeriodic() ){
                context.sendBroadcast(new Intent(IntentFactory.ACTION_INACTIVE));
                Log.w("WATCH DOG", "DETECTED NO ACTIVITY");
            }
        }

        @Override
        public void onFinish() {
            Log.w("WATCH DOG", "Watch Dog rebooted itself");
            this.start();
        }
    };

    public void inform(){
        lastUpdationTimes = System.currentTimeMillis();
    }

    public void startSurveillance(){
        if(watchDogTimer != null && !isActive){
            watchDogTimer.start();
            Log.w("WATCH DOG", "Surveillance started");
            isActive = true;
        }
    }

    private boolean isUpdationPeriodic(){
        return (System.currentTimeMillis() - lastUpdationTimes) < INACTIVE_RESPONSE_WINDOW;
    }


    public void stop(){
        if(watchDogTimer != null && isActive){
            watchDogTimer.cancel();
            Log.w("WATCH DOG", "Surveillance killed");
            isActive = false;
        }
    }
}