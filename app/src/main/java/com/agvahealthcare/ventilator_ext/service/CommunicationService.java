package com.agvahealthcare.ventilator_ext.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.agvahealthcare.ventilator_ext.VentilatorApp;
import com.agvahealthcare.ventilator_ext.callback.ConfigurationMiddleware;
import com.agvahealthcare.ventilator_ext.connection.support_threads.WatchDogTask;
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager;
import com.agvahealthcare.ventilator_ext.utility.ToastFactory;
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils;
import com.agvahealthcare.ventilator_ext.utility.utils.Configs;
import com.agvahealthcare.ventilator_ext.utility.utils.ConfigurationArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public abstract class CommunicationService extends Service {

    private final IBinder mBinder = new LocalBinder();

    public Boolean isEthernetConnected = false;

    private final WatchDogTask watchDog;

    public CommunicationService() {
        watchDog = new WatchDogTask(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /*
     * Generates a log for the class with which service is bound
     */

    public void makeLog(String className) {
        Log.i(this.getClass().getSimpleName(), "bounded with " + className);
    }

    /*
     * Initiate Watch dog timer to keep a check of incoming data
     */

    public void startWatchDog() {
        if (watchDog != null) watchDog.startSurveillance();
    }

    /*
     * Halt the watching and kills the watch dog thread
     * After this invocation, data monitoring will no more work
     */
    public void stopWatchDog() {
        if (watchDog != null) watchDog.stop();
    }

    /*
     * Inform the watch dog thread regarding the incoming data
     * and updates the last updation count
     */

    public void informWatchDog() {
        if (watchDog != null) watchDog.inform();
    }

    /*
     * Verifies if the ventilator is connected to the device
     */

    abstract public boolean isVentilatorConnected();

    /*
     * Verifies if the encoder knob is connected to the device
     */

    abstract public boolean isHIDConnected();

    /*
     * Verifies if the encoder knob & ventilator both are connected to the device
     */

    public final boolean isPortsConnected() {
        broadcastUsbCommunicationData(isVentilatorConnected(), isHIDConnected());
        return isVentilatorConnected() || isHIDConnected();
    }

    /*
     * Broadcast Ventilator raw data signal throughout app receivers
     */
    abstract protected void broadcastRawData(String data);

    /*
     * Broadcast Ventilator functional data signal throughout app receivers
     */
    abstract protected void broadcastData(String data);

    abstract protected void broadcastDiagnosticData(String data);
    abstract protected void broadcastSelfTestData(String data);
    abstract protected void broadcastMStringData(String data);
    abstract protected void broadcastSensorTestData(String data);

    abstract protected void broadcastStartupcheckData(String data);

    abstract protected void broadcastOxygenRegulationData(String data);

    abstract public void checkConnection();

    /*
     * Broadcast Ventilator acknowledgement throughout app receivers
     */
    abstract protected void broadcastAcknowledgement(String ack);

    /*
     * Broadcast Ventilator battery status throughout app receivers
     */

    abstract protected void broadcastVentiLiveData(String data);


    abstract protected void broadcastBatteryStatus(String brtyLevel, String btryHealth, String remainingTime);

    /*
     * Broadcast Ventilator battery status throughout app receivers
     */


    abstract protected void broadcastHeatSensorStatus(ArrayList<String> values);


    /*
     * Broadcast Ventilator aensor availability status throughout app receivers
     */

    abstract protected void broadcastSensorAnalysis(String sensorAnalysis);

    /*
     * Broadcast Ventilator sensor calibration analysis status throughout app receivers
     */
    abstract protected void broadcastCalibrationSensorAnalysis(String sensorTag, String calibrationStatus);


    /*
     * Broadcast Ventilator Motor life (in hrs) status throughout app receivers
     */
    abstract protected void broadcastMotorLifeLevelStatus(String motorLife);

    /*
     * Broadcast Standby response (in code 00/01) status throughout app receivers
     */

    abstract protected void broadcastStandbyResponse(String standbyResponse);

    abstract protected void broadcastHardwareSerialNumber(String data);

    /*
     * Broadcast Wifi connection response (in code 00/01) status throughout app receivers
     */


    abstract protected void broadcastWifiConnectionResponse(String wifiConnectionResponse);

    /*
     * Broadcast Self Test response (in code 00/007) status throughout app receivers
     */

    abstract protected void broadcastSelfTestResponse(String stpResponse);

    /*
     * Broadcast Available Wifi devices throughout app receivers
     */

    abstract protected void broadcastScannedWifiDevices(String devicesJson);

    //Broadcast for HFNC Response for FLOW , FiO2 in HFNC Mode.

//    abstract public void broadcastHFNCModeResponse(String[] value);

    /*
     * Broadcast Available ventilator software version throughout app receivers
     */

    abstract protected void broadcastSoftwareVersion(String softwareUpdateData);

    /*
     * Broadcast Device name request by ventilator throughout app receivers
     */

    abstract protected void broadcastDeviceNameRequested(String deviceNameReqCode);

    /*
     * Broadcast Ventilator connected signal throughout app receivers
     */

    abstract public void openConnectionToReadHID(Boolean permissionGranted);

    abstract public void openConnectionToReadVentilator(Boolean permissionGranted);

    abstract public void sendBroadcastHandshakeCompleted();

    /*
     * This enables the device to start listening from Ventilator.
     */

    abstract public void startReading();

    /*
     * This disable the device to stop listening from Ventilator.
     */

    abstract public void stopReading();

    /*
     * Send data to the KNOB
     */

    abstract public void sendDatatoKnob(String data);

    /*
     * Send data to the ventilator
     */
    abstract public void send(String data);

    // send tube compliance calibration

    abstract public void broadcastTubeComplianceResponse(String tubeComplianceResponse);


    // send tube resistance calibration

    abstract public void broadcastTubeResistanceResponse(String tubeResistanceResponse);

    /*
     *  send KNob response
     */

    abstract protected void broadcastKnobResponse(String knobResponse);

    abstract protected void broadcastGraphLimits(String data);


    abstract protected void broadcastAlarmMuteUmuteResponse(String value);

    abstract protected void broadcastNeoNateSensorConnectResponse();

    abstract protected void broadcastNeoNateSensorDisconnectResponse();

    abstract protected void broadcastNebuliserResponse();

    abstract protected void broadcastHMACDataResponse();

    abstract protected void broadcastOxygenResponse(String value);

    abstract protected void broadcastInspiratoryHoldResponse();

    abstract protected void broadcastExpiratoryHoldResponse();

    abstract protected void broadcastManualBreathResponse();

    abstract protected void broadcastHomeResponse();

    abstract protected void broadcastLockResponse();

    abstract protected void broadcastPowerSwitchOffResponse();

    abstract protected void broadcastxResponse(String value);

    abstract protected void broadcastPowerSwitchOnResponse();

    abstract protected void broadcastCalibrationErrorResponse(String pressure, String flow, String dutycycle);
    abstract protected void broadcastUsbCommunicationData(Boolean isVenti, Boolean isHid);

    String pPlatValue = "";
    String statusVGV = "";


    /*
     * Send date time to ventilator
     */
    public void sendCurrentDateTime() {
        send("DT@" + AppUtils.ventDateTimeFormatter.format(new Date()) + "#");
    }

    public void sendConfigurationToVentilator() {

        sendConfigurationToVentilator(null);
    }


    public void sendAlarmLimitsToVentilator() {
        final ConfigurationArrayList settings = getAlarmSettingsList();
        String prefix = "L,";
        String data = prefix + settings + ",#";
        send(data);
        Log.i("ALARMSETTINGCHECK", "Sent -> " + data);
    }

    public void sendConfigurationToVentilator(ConfigurationMiddleware middleware) {

        final ConfigurationArrayList settings = (middleware != null) ? middleware.modify(getControlSettingsList()) : getControlSettingsList();
        String prefix = "S,";
        String data = prefix + settings.toString() + ",#";
        send(data);
        Log.i("CONFIGCHECK", "Sent -> " + data);
    }

    private ConfigurationArrayList getAlarmSettingsList() {
        final PreferenceManager prefManager = new PreferenceManager(getApplicationContext());

        Log.i("setTimer",prefManager.readLastSettingsTime());

        List<Float> pip = Arrays.asList(prefManager.readPipLimits());
        List<Float> vte = Arrays.asList(prefManager.readVteLimits());
        List<Float> peep = Arrays.asList(prefManager.readPeepLimits());
        List<Float> rr = Arrays.asList(prefManager.readRRLimits());
        List<Float> mve = Arrays.asList(prefManager.readMveLimits());
        List<Float> fio2 = Arrays.asList(prefManager.readFiO2Limits());
        List<Float> spo2 = Arrays.asList(prefManager.readSpO2Limits());

        List<String> flattenList = Stream.of(
                pip,
                vte,
                peep,
                rr,
                mve,
                fio2,
                spo2
        ).flatMap(List::stream).map(e -> String.valueOf(e.intValue())).collect(Collectors.toList());
        ConfigurationArrayList configs = new ConfigurationArrayList();
        configs.addAll(flattenList);
        return configs;
    }

    public ConfigurationArrayList getControlSettingsList() {

        final PreferenceManager prefManager = new PreferenceManager(getApplicationContext());
        prefManager.setLastSettingsTime(AppUtils.getCurrentDateTime());

        String pip = String.valueOf(prefManager.readPip().intValue());
        String vti = String.valueOf(prefManager.readVti().intValue());
        String peep = String.valueOf(prefManager.readPEEP().intValue());
        String trigFlow = String.valueOf(prefManager.readTrigFlow());
        final boolean isPplatDeltaRequired = Configs.getModeCategory(prefManager.readVentilationMode()) != Configs.MODE_NIV;
        int pplatReading = isPplatDeltaRequired ? (prefManager.readPplat().intValue() + prefManager.readPEEP().intValue()) : prefManager.readPEEP().intValue() + prefManager.readPplat().intValue();
        pPlatValue = String.valueOf(pplatReading);
        String inhaleTime = String.format("%.1f", prefManager.readTinsp());
        String neoMinInhaleTime = "0.6";
        String peakFlow = String.valueOf(prefManager.readPeakFlow().intValue());
        String fio2 = String.valueOf(prefManager.readFiO2().intValue());
        String supportPressure = String.valueOf(prefManager.readSupportPressure().intValue() + prefManager.readPEEP().intValue()); // SP = SP + PEEP
        int compensatedSlopeValue = prefManager.readSlope().intValue() * 10; // slope value 2 means 20 on the backend

        // Doing it reverse as per embedded Team instruction..
        String slope = "";

        if (Build.VERSION.SDK_INT >= 27) {
            slope = "0";
        } else {
            if(compensatedSlopeValue == 20){
                slope = "0";
            } else if (compensatedSlopeValue == 0) {
                slope = "20";
            }else{
                slope = String.valueOf(compensatedSlopeValue);
            }
        }

        String inspPause = String.valueOf(0);
        String flow = String.valueOf(prefManager.readFlow());
        String peepValve = String.valueOf(prefManager.readPeepValve() / 10);
        String rr = String.valueOf(prefManager.readRR().intValue());
        String targetVolume = String.valueOf(prefManager.readTargetVolume().intValue());
        String frequency = String.valueOf(prefManager.readFrequency());
        String tlow = "";
        if (VentilatorApp.Companion.getSelectedOptions() == null){
            switch (prefManager.readSelectedOptions()) {
                case PRONGS_NAME:
                    tlow = "0";
                    break;
                case INVASIVE_NAME:
                    tlow = "0";
                    break;
                case NON_INVASIVE_NAME:
                    tlow = "1";
                    break;
            }
        }else{
            switch (VentilatorApp.Companion.getSelectedOptions()) {
                case PRONGS_NAME:
                    tlow = "0";
                    break;
                case INVASIVE_NAME:
                    tlow = "0";
                    break;
                case NON_INVASIVE_NAME:
                    tlow = "1";
                    break;
            }
        }

        String texp = String.valueOf(prefManager.readTexp().intValue());
        String statusApnea = String.valueOf(prefManager.readApneaSettingsStatus() ? 1 : 0);
        String rrApnea = String.valueOf(prefManager.readRRApnea().intValue());
        String tApnea = String.valueOf(prefManager.readTApnea().intValue());

        String vtApnea = String.valueOf(prefManager.readVtApnea().intValue());
        String trigFlowApnea = String.valueOf(prefManager.readTrigFlowApnea());

        String statusNeoNate = String.valueOf(prefManager.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT && prefManager.readNeoNateActiveStatus() ? 1 : 0);

        String autoFlow = String.valueOf(prefManager.readAutoFlow());
        if (prefManager.readLastVentMode() == Configs.MODE_PC_PRVC) prefManager.setVGVStatus(true);
        statusVGV = String.valueOf(prefManager.readVGVStatus() ? 1 : 0);

        String circuitOptions = "";
        switch (prefManager.readSelectedOptions()) {
            case PRONGS_NAME:
                circuitOptions = "0";
                break;
            case INVASIVE_NAME:
                circuitOptions = "1";
                break;
            case NON_INVASIVE_NAME:
                circuitOptions = "2";
                break;
        }

        ConfigurationArrayList configs = new ConfigurationArrayList();
        configs.add(pip);
        if (prefManager.readModeType() == Configs.ModeType.TYPE_Pressure) {
            configs.add(targetVolume);
        } else {
            configs.add(vti);
        }
        configs.add(peep);
        configs.add(rr);
        configs.add(trigFlow);
        configs.add(pPlatValue);
        if(prefManager.readCurrentUid() == Configs.PatientProfile.TYPE_NEONAT && inhaleTime == "0.5") {
            configs.add(neoMinInhaleTime);
        } else{
            configs.add(inhaleTime);
        }
        configs.add(flow);
        if (Configs.isFio2SettingAvailable) configs.add(fio2);
        configs.add(supportPressure);
        configs.add(slope);
        configs.add(tlow);
        configs.add(texp);
        configs.add(statusApnea);
        configs.add(rrApnea);
        configs.add(tApnea);
        configs.add(vtApnea);
        configs.add(trigFlowApnea);
        configs.add(inspPause);
        configs.add(peepValve);
        configs.add(statusNeoNate);
        configs.add(statusVGV);
        return configs;
    }


    public class LocalBinder extends Binder {
        public CommunicationService getService() {
            return CommunicationService.this;
        }
    }

}
