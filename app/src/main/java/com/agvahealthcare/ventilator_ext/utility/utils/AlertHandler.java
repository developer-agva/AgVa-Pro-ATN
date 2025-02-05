package com.agvahealthcare.ventilator_ext.utility.utils;

import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.ACK_CODE_31;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.ACK_CODE_5;
import static com.agvahealthcare.ventilator_ext.utility.utils.Configs.WARNING_LEVEL_UNMUTABLE;

import android.content.Context;
import android.util.Log;

import com.agvahealthcare.ventilator_ext.R;
import com.agvahealthcare.ventilator_ext.model.AlertModel;

import java.util.LinkedHashMap;
import java.util.Map;

/*
 * Handler handles various alerts
 * and sync them with Activity UI
 */
public class AlertHandler{


    public AlertHandler(Context context){
        this.context = context;
    }
    private int alarmFlag = 0;
    private final AlertModel leakDetected = new AlertModel("Leak detected", R.color.colorAlertWarning);
    private final AlertModel tubeBlockageDetected = new AlertModel("Tube blockage detected", R.color.colorAlertDanger);
    private final AlertModel flowSensorOcclusionDetected = new AlertModel("Flow sensor occlusion detected", R.color.colorAlertDanger);
    private final AlertModel cuffLeakageDetected = new AlertModel("Cuff Leak detected", R.color.colorAlertDanger);
    private final AlertModel deviceDisconnected = new AlertModel("Usb Disconnected", R.color.colorAlertDanger);
    private final AlertModel connectionAlert = new AlertModel("Ventilator disconnected", R.color.colorAlertDanger);
    private final AlertModel inactiveAlert = new AlertModel("Ventilator error detected", R.color.colorAlertWarning);
    private final AlertModel lowBatteryAlert = new AlertModel("Please charge your android device", R.color.colorAlertDanger);
    private final AlertModel highLeakInaccuracyAlert = new AlertModel("High leak, VT may be inaccurate", R.color.colorAlertWarning);

    private final Map<String, AlertModel> ackAlertMap = new LinkedHashMap<>();
    private final Map<String, AlertModel> softAlertMap = new LinkedHashMap<>();
    private final Context context;
    private CustomMediaPlayer mediaPlayer;

    /*
     * Invokes to inform when no response comes from the ventilator
     */
    public void showInactiveAlerts(){

        if(addAlert(inactiveAlert)){
            alarmFlag++;
        }

        startAlarm(WARNING_LEVEL_UNMUTABLE);
    }

    void hideInactiveAlerts(){
        if(removeAlert(inactiveAlert)){
            alarmFlag--;
        }

        stopAlarm();
    }

    /*
     * Invoke to inform when ventilator sends some acknowledgement
     */

   public void hideAcknowledgementAlert(String ackCode){

        final boolean isVolumeAlarm =  !Configs.noSoundAcks.contains(ackCode);

        if(ackAlertMap.containsKey(ackCode) && ackAlertMap.get(ackCode) != null){
            if(removeAlert(ackAlertMap.get(ackCode))){
                ackAlertMap.put(ackCode, null);
                alarmFlag--;
//                analysisFragment.registerErrorLogs(Configs.getPositiveAckOf(ackCode));

            }
        }

        stopAlarm();

    }

    /*
     * Invoke to inform when battery is low or ok
     */
    void showLimitAlert(String lbl, String msg){

        if(!(softAlertMap.containsKey(lbl) && softAlertMap.get(lbl) != null)){
            AlertModel alertModel = new AlertModel(msg, R.color.colorAlertDanger);
            if(addAlert(alertModel)) {
                softAlertMap.put(lbl, alertModel);
                alarmFlag++;


            }

        }

        startAlarm(Configs.WARNING_LEVEL_LOW);
    }

    void hideLimitAlert(String msg){
        if(softAlertMap.containsKey(msg) && softAlertMap.get(msg) != null){
            if(removeAlert(softAlertMap.get(msg))){
                softAlertMap.put(msg, null);
                alarmFlag--;
            }
        }

        stopAlarm();
    }

    void hideAllLimitAlerts(){
        for(Map.Entry<String, AlertModel> entry : softAlertMap.entrySet()){
            if(entry.getValue() != null && removeAlert(entry.getValue())) alarmFlag-- ;
        }
        softAlertMap.clear();
    }

    void hideAllAckAlerts(){
        for(Map.Entry<String, AlertModel> entry : ackAlertMap.entrySet()){
            if(entry.getValue() != null && removeAlert(entry.getValue())) alarmFlag--;
        }
        ackAlertMap.clear();
    }

    void hideAllAlerts(){
        hideConnectionAlert();
        hideInactiveAlerts();
        hideBatterLowAlert();
        hideAllAckAlerts();
        hideAllLimitAlerts();
        hideTubeBlockageAlert();
        hideCuffLeakAlert();
        hideFlowSensorAlert();
        hideLeakAlert();

        alarmFlag = 0;

    }

    /*
     * Invoke to inform when Tab battery is less tha threshold
     */

    void showdeviceDisconnectedAlert() {
        if(addAlert(deviceDisconnected)){
            alarmFlag++;
        }
        startAlarm(Configs.DEVICE_DISCONNECTED);
    }

    void hidedeviceDisConnectedAlert() {
        if(removeAlert(deviceDisconnected)){
            alarmFlag--;
        }
        stopAlarm();
    }



    void showBatteryLowAlert(){
        if(addAlert(lowBatteryAlert)){
            alarmFlag++;
        }

        startAlarm(Configs.WARNING_LEVEL_LOW);
    }

    void hideBatterLowAlert(){
        if(removeAlert(lowBatteryAlert)){
            alarmFlag--;
        }

        stopAlarm();
    }

    /*
     * Invoke to inform when FiO2
     * are low or high than certain limits
     */


    /*
     * Invoke to alert when GATT disconnects abruptly
     */

    void showConnectionAlert(){
        if(addAlert(connectionAlert)){
            alarmFlag++;
        }

        startAlarm(Configs.WARNING_LEVEL_UNMUTABLE);
    }

    void hideConnectionAlert(){
        if(removeAlert(connectionAlert)){
            alarmFlag--;
        }

        stopAlarm();
    }

    void showLeakAlert(){

        if(addAlert(leakDetected)){
            alarmFlag++;
        }

        startAlarm(Configs.WARNING_LEVEL_HIGH);
    }

    void hideLeakAlert(){


        if(removeAlert(leakDetected)){
            alarmFlag--;
        }

        stopAlarm();
    }

    void showHighLeakInaccuracyAlert(){

        if(addAlert(highLeakInaccuracyAlert)){
            alarmFlag++;
        }

        startAlarm(Configs.WARNING_LEVEL_HIGH);
    }

    void hideHighLeakInaccuracyAlert(){


        if(removeAlert(highLeakInaccuracyAlert)){
            alarmFlag--;
        }

        stopAlarm();
    }

    void showTubeBlockageAlert(){

        if(addAlert(tubeBlockageDetected)){
            alarmFlag++;
        }

        startAlarm(Configs.WARNING_LEVEL_HIGH);
    }

    void hideTubeBlockageAlert(){


        if(removeAlert(tubeBlockageDetected)){
            alarmFlag--;
        }

        stopAlarm();
    }


    void showCuffLeakAlert(){

        if(addAlert(cuffLeakageDetected)){
            alarmFlag++;
        }

        startAlarm(Configs.WARNING_LEVEL_HIGH);
    }

    void hideCuffLeakAlert(){


        if(removeAlert(cuffLeakageDetected)){
            alarmFlag--;
        }

        stopAlarm();
    }

    void showFlowSensorAlert(){

        if(addAlert(flowSensorOcclusionDetected)){
            alarmFlag++;
        }

        startAlarm(Configs.WARNING_LEVEL_HIGH);
    }

    void hideFlowSensorAlert(){


        if(removeAlert(flowSensorOcclusionDetected)){
            alarmFlag--;
        }

        stopAlarm();
    }


    /*
     * update the alert drop down
     */
    boolean addAlert(AlertModel alert){
//        return (analysisFragment != null) && analysisFragment.addAlertToDropdown(alert);
        return  false;
    }

    boolean addAlertAt(AlertModel alert, int index){
//        return (analysisFragment != null) && analysisFragment.addAlertToDropdown(alert, index);
        return  false;

    }

    boolean addAlertWithPriority(AlertModel alert){
//        return addAlertAt(alert, 0);
        return  false;

    }

    boolean removeAlert(AlertModel alert){
//        return (analysisFragment != null) && analysisFragment.removeAlertFromDropDown(alert);
        return  false;

    }

    boolean isAlertShowing(String msg){
//        return (analysisFragment != null) && analysisFragment.isAlertExist(msg);
        return  false;
    }

    /*
     * Invokes to start/stop ic_warning alarm
     */

    void startAlarm(int level){ startAlarm(level , false, false); }

    void startAlarm(int level, final boolean isTemporaryMutable, final boolean isBatteryAlarm){

        boolean checkAlarmPriority = (mediaPlayer != null) && (level >= Configs.WARNING_LEVEL_HIGH);
               /*  && (!Configs.URI_ALARM_HIGH_LEVEL.toString().equals(mediaPlayer.getDataSourcePath()));*/

        // if any one ic_warning available or not
        if(alarmFlag == 0) return;

       /* try {


            // Unmuted for High priority alarms
            if(level == Configs.WARNING_LEVEL_UNMUTABLE && !isTemporaryMutable) handleVolumeControl(isAlarmMuted = false);
            else handleVolumeControl(isAlarmMuted);

            if(mediaPlayer == null || checkAlarmPriority){

                if(checkAlarmPriority) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

                mediaPlayer = new CustomMediaPlayer();
//                    REGULAR APPLICATION CONDITION
                final Uri alarmUri = isBatteryAlarm ? Configs.URI_ALARM_BATTERY_LOW : ((level >= Configs.WARNING_LEVEL_HIGH) ? Configs.URI_ALARM_HIGH_LEVEL : Configs.URI_ALARM_LOW_LEVEL);

//                    IKALL SPECIFIC CONDITION : Single alarm sound
//                    final Uri alarmUri = Configs.URI_ALARM_HIGH_LEVEL;
                mediaPlayer.setDataSource(context, alarmUri);

                AudioAttributes attrib = new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_ALARM)
                        .build();

                mediaPlayer.setAudioAttributes(attrib);
                mediaPlayer.prepare();
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(100f, 100f);
                mediaPlayer.start();

                // to show hide alert option
//                    if(btnMute.getVisibility() != View.VISIBLE){
//                        btnMute.setImageResource(R.drawable.ic_volume_on);
//                        btnMute.setVisibility(View.VISIBLE);
//                    }

                Log.i("ALARM CHECK", "Media path " + mediaPlayer.getDataSourcePath());

                // show FAB layout
//                fabMainLayout.setVisibility(View.VISIBLE);


            }
        } catch (Exception e) {
            // show FAB layout
//            fabMainLayout.setVisibility(View.GONE);

            Log.e("ALARM CHECK", "Unable to start the PD alarm . Reason : " + e.getLocalizedMessage());
            e.printStackTrace();
            FileLogger.Companion.e(context, e);
        }*/
    }

    void stopAlarm(){

        // if any one ic_warning available or not
//            // OR if alarm is already muted
        if(alarmFlag > 0) return;

        if(mediaPlayer != null){

            if(mediaPlayer.isPlaying()) {
                Log.i("ALARM CHECK", "Playing Alarm detected");
                mediaPlayer.stop();
                Log.i("ALARM CHECK", "Alarm muted");
                mediaPlayer.release();
                // show FAB layout
//                fabMainLayout.setVisibility(View.GONE);
            }

            mediaPlayer = null;
        }
    }

    void forcePauseAlarm(){
        if(mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    void forceResumeAlarm(){
        if(mediaPlayer != null && !mediaPlayer.isPlaying()) mediaPlayer.start();
    }

    void forceStopAlarm(){
        if(mediaPlayer != null) {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}