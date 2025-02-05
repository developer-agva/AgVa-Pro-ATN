package com.agvahealthcare.ventilator_ext.utility.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.IOException;



public class  CustomMediaPlayer extends MediaPlayer {

    private String dataSourcePath;
    //    private AlarmState alarmState;
    private boolean isRunning;



    @Override
    public void setDataSource(@NonNull Context context, @NonNull Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        super.setDataSource(context, uri);
        this.dataSourcePath = uri.toString();

//        if(dataSourcePath.equals(Configs.URI_ALARM_LOW_LEVEL.toString())){
//            alarmState = AlarmState.LOW;
//        } else if(dataSourcePath.equals(Configs.URI_ALARM_HIGH_LEVEL.toString())){
//            alarmState = AlarmState.HIGH;
//        } else if(dataSourcePath.equals(Configs.URI_ALARM_HIGH_LEVEL.toString())){
//            alarmState = AlarmState.BATTERY;
//        } else throw new IllegalArgumentException();

    }

    @Override
    public boolean isPlaying() {
        return super.isPlaying();
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void start() throws IllegalStateException {
        isRunning = true;
        super.start();
    }


    @Override
    public void pause() throws IllegalStateException {
        isRunning = false;
        super.pause();
    }


    @Override
    public void stop() throws IllegalStateException {
        isRunning = false;
        super.stop();
    }

    @Override
    public void release() {
        isRunning = false;
        super.release();
    }
}
