package com.agvahealthcare.ventilator_ext.service;

import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.DIAG_TOOL_CHECK;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.FIO2_RESPONSE;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.GRAPH_LIMITS;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.HARDWARE_SERIAL_NUMBER;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.MUTE_UNMUTE_RESPONSE;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.OR_TOOL_CHECK;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.SELF_TEST_CHECK;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.SENSOR_ANALYSIS;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.TUBE_COMPLIANCE_CALIBRATION;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.TUBE_RESISTANCE_CALIBRATION;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.USB_HID_DATA;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.USB_VENTILATOR_DATA;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_ACK;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_BATTERY_HEALTH;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_BATTERY_LEVEL;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_BATTERY_TTE;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_CONTROL_KNOB;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_CONTROL_SUB_MODE;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_DATA;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_DATA_SEND;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_DEV_NAME_RESPONSE;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_HANDSHAKE_CALIBRATION;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_HEATSENSE_DATA;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_KNOB_X;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_MOTOR_LIFE;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_RAW_DATA;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_SELF_TEST_STATUS;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_SENSOR_CALIBRATION_RESULT;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_SENSOR_CALIBRATION_TAG;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_SOFTWARE_VERSION;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_STANDBY_STATUS;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_WIFI_CONNECTION_RESPONSE;
import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.VENTILATOR_WIFI_DEVS;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import android.os.Handler;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.agvahealthcare.ventilator_ext.VentilatorApp;
import com.agvahealthcare.ventilator_ext.callback.SerialInputOutputHIDManager;
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager;
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils;
import com.agvahealthcare.ventilator_ext.utility.utils.Configs;
import com.agvahealthcare.ventilator_ext.utility.utils.IntentFactory;

import com.hoho.android.usbserial.driver.FtdiSerialDriver;

import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import io.socket.client.IO;

class CustomProber {

    static UsbSerialProber getCustomProber() {
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x1234, 0xabcd, FtdiSerialDriver.class); // e.g. device with custom VID+PID
//        customTable.addProduct(0x1a86, 0x7523, Ch34xSerialDriver.class); // e.g. device with custom VID+PID
        return new UsbSerialProber(customTable);
    }
}

public class UsbService extends CommunicationService implements SerialInputOutputManager.Listener, SerialInputOutputHIDManager.Listener {

    //For the Main PCB 9025 is the Vendor ID
    private static final int ARDUINO_VENDOR_ID_VENTILATOR = 9025;
    private static final int DEFAULT_BAUD_RATE_VENTILATOR = 9600;
    private static final int READ_DELAY = 11;
    private static final int ARDUINO_VENDOR_ID_HID = 1003;
    private static final int DEFAULT_BAUD_RATE_HID = 9600;
    private UsbManager usbManager;
    private UsbSerialPort usbVentilatorPort;
    private UsbSerialPort usbWriteVentilatorPort;
    private UsbSerialPort usbHIDPort;
    private final StringBuffer dataBufferVentilator = new StringBuffer();
    private final StringBuffer dataBufferHID = new StringBuffer();
    private Thread bufferReadingThreadVentilator;
    private Thread bufferReadingThreadHID;
    private PreferenceManager preferenceManager;
    @Override
    public void onNewData(byte[] data) {
        readBytesDataVentilator(new String(data));
    }
    @Override
    public void onNewHIDData(byte[] data) {
        Log.i("USB_CHECK", "hid raw data coming");
        readBytesDataHID(new String((data)));
    }
    @Override
    public void onRunError(Exception e) {
        Log.i("USB_CHECK", "venti raw data error");
    }
    @Override
    public void onRunErrorHID(Exception e) {
        Log.i("USB_CHECK", "hid raw data error");
    }

    private class ReadingRunnableVentilator implements Runnable {
        private volatile boolean isProcessingData = false;
        private long startTime = 0;

        @Override
        public void run() {
            Log.i("USB_CHECK", "venti reading thread started");

            if (dataBufferVentilator.length() > 0)
                sendBroadcast(new Intent(IntentFactory.ACTION_VENTILATOR_FAILURE_GENERATED));
            else sendBroadcast(new Intent(IntentFactory.ACTION_VENTILATOR_FAILURE_REMOVED));
            while (true) {

                if (!isProcessingData) {
                    startTime = System.currentTimeMillis();
                }
                if (dataBufferVentilator.length() > 120) {
                    dataBufferVentilator.delete(0, dataBufferVentilator.length());
                }
                if (dataBufferVentilator.length() > 0) {
                    String buffData = dataBufferVentilator.toString();
                    VentilatorApp.Companion.setVentiData(dataBufferVentilator + " : " + AppUtils.getCurrentTime());
                    Log.i("READ_CHECK_VENTI", dataBufferVentilator.toString());
                    try {
                        // To separated the ACKNOWLEDGEMENTS
                        if (buffData.contains(Configs.PREFIX_ACK)) {
                            // +1 for ACK code number (Don't remove -1 +1 this is for understanding)
                            try {
                                int ackStartIndex = buffData.indexOf(Configs.PREFIX_ACK);

                                // Check if ackStartIndex is valid (-1 means the prefix was not found)
                                if (ackStartIndex != -1) {
                                    int ackTerminalIndex = buffData.indexOf(Configs.SUFFIX_ACK, ackStartIndex);

                                    // Check if ackTerminalIndex is valid
                                    if (ackTerminalIndex != -1) {
                                        String ack = buffData.substring(ackStartIndex, ackTerminalIndex);
                                        Log.d("ACK_CHECK", ack);
                                        broadcastAcknowledgement(ack);

                                        dataBufferVentilator.delete(ackStartIndex, ackTerminalIndex + Configs.SUFFIX_ACK.length());
                                    }
                                }
                            } catch (Exception e) {
                                Log.i("MASOOM", "BUFFER_HERE" + buffData);
                            }
                        }
                        else if (buffData.contains(Configs.PREFIX_BATTERY)) {

                            int btstrt = buffData.indexOf(Configs.PREFIX_BATTERY);
                            int btryTerminalIndex = buffData.indexOf(Configs.DELIMITER_BATTERY);

                            String btryCheckData = buffData.substring(btstrt + 5, btryTerminalIndex);
                            try {
                                String[] btryCheck = btryCheckData.split(",");
                                String batteryLevel = btryCheck[0];
                                String batteryHealth = btryCheck[1];
                                String batteryRemainingTime = btryCheck[2];
                                broadcastBatteryStatus(batteryLevel, batteryHealth, batteryRemainingTime);
                                Log.i("BATTERY", batteryLevel + " - " + batteryHealth + " -" + " " + batteryRemainingTime);
                                dataBufferVentilator.delete(btstrt, btryTerminalIndex + 1);
                                Log.i("HEREDBV", dataBufferVentilator.toString());
                            } catch (Exception e) {
                                dataBufferVentilator.delete(0, dataBufferVentilator.length());
                            }
                            Log.i("BATTRERYSTARTINTT", buffData);
                        }
                        else if (buffData.contains(Configs.PREFIX_TUBE_COMPLIANCE)) {

//                            try {
                            Log.i("testingCompliance", buffData);
                            int tubeComplianceStartIndex = buffData.indexOf(Configs.PREFIX_TUBE_COMPLIANCE);
                            int tubeComplianceTerminalIndex = buffData.indexOf(Configs.COMMON_TUBE_DELIMETER);

                            String tubeComplianceData = buffData.substring(tubeComplianceStartIndex + 5, tubeComplianceTerminalIndex);
                            broadcastTubeComplianceResponse(tubeComplianceData);
                            dataBufferVentilator.delete(tubeComplianceStartIndex, tubeComplianceTerminalIndex + 1);

                        }
                        else if (buffData.contains(Configs.PREFIX_STARTUP_CHECK_START)) {
                            int staCheckIndex = buffData.indexOf(Configs.PREFIX_STARTUP_CHECK_START);
                            int staCheckTerminalIndex = buffData.indexOf(Configs.SUFFIX_STATUP_CHECK_STOP);

                            try {
                                if (staCheckIndex < staCheckTerminalIndex) {
                                    String startCheckData = buffData.substring(staCheckIndex + 4, staCheckTerminalIndex);
                                    broadcastStartupcheckData(startCheckData);
                                    Log.i("CHECK_START", startCheckData);
                                    dataBufferVentilator.delete(staCheckIndex, staCheckTerminalIndex + 1);
                                }
                            } catch (Exception e) {
                                dataBufferVentilator.delete(0, dataBufferVentilator.length());
                            }
                        }

                        else if (buffData.contains("T@")){
                            int selfTestStartIndex = buffData.indexOf("T@");
                            int selfTestTerminalIndex = buffData.indexOf("!");
                            if (selfTestStartIndex < selfTestTerminalIndex) {
                                String selfTestData = buffData.substring(selfTestStartIndex + 2, selfTestTerminalIndex);
                                Log.i("CHECK_SELF_TEST_DATA", selfTestData);
                                broadcastSelfTestData(selfTestData);
                                dataBufferVentilator.delete(selfTestStartIndex, selfTestTerminalIndex + 1);
                            }
                        }

                        else if (buffData.contains(Configs.PREFIX_OR_START)) {

                            int o2StartIndex = buffData.indexOf(Configs.PREFIX_OR_START);
                            int o2BeginIndex = buffData.indexOf(Configs.PREFIX_OR_START);
                            int o2TerminalIndex = buffData.indexOf(Configs.SUFFIX_OR_START);
                            Log.i("value_ors", "in Data");
                            String o2Data = buffData.substring(o2StartIndex + 3, o2TerminalIndex);
                            Log.i("value_ors", o2Data);
                            broadcastOxygenRegulationData(o2Data);
                            dataBufferVentilator.delete(o2BeginIndex, o2TerminalIndex + 1);

                        }
                        else if (buffData.contains(Configs.PREFIX_TUBE_RESISTANCE)) {
//                            try {

                            Log.i("testing", "resistance");
                            int tubeResistanceStartIndex = buffData.indexOf(Configs.COMMON_TUBE_LIMITER);
                            int tubeResistanceTerminalIndex = buffData.indexOf(Configs.COMMON_TUBE_DELIMETER);

                            Log.i("testing", buffData.substring(tubeResistanceStartIndex + 1, tubeResistanceTerminalIndex));
                            String tubeResistanceData = buffData.substring(tubeResistanceStartIndex + 1, tubeResistanceTerminalIndex);
                            broadcastTubeResistanceResponse(tubeResistanceData);
                            dataBufferVentilator.delete(tubeResistanceStartIndex, tubeResistanceTerminalIndex + 1);

//                            } catch (Exception e) {
//                                dataBufferVentilator.delete(0, dataBufferVentilator.length());
//                            }
                        }
                        else if (buffData.contains(Configs.PREFIX_DEVICE_NAME_REQUEST)) {
//                            try {
                            // +1 for STANDBY STATUS code number (Don't remove -1 +1 this is for understanding)
                            int deviceNameReqStartIndex = buffData.indexOf(Configs.PREFIX_DEVICE_NAME_REQUEST);
                            int deviceNameReqTerminalIndex = buffData.indexOf(Configs.PREFIX_DEVICE_NAME_REQUEST) + Configs.PREFIX_DEVICE_NAME_REQUEST.length() + Configs.DEVICE_NAME_REQUEST_LENGTH - 1;
                            String deviceNameReqResponse = buffData.substring(deviceNameReqStartIndex + Configs.PREFIX_DEVICE_NAME_REQUEST.length(), deviceNameReqTerminalIndex + 1);
                            broadcastDeviceNameRequested(deviceNameReqResponse);
                            dataBufferVentilator.delete(deviceNameReqStartIndex, deviceNameReqTerminalIndex + 1);

//                            } catch (Exception e) {
//                                dataBufferVentilator.delete(0, dataBufferVentilator.length());
//                            }
                        }
                        else if (buffData.contains(Configs.PREFIX_HANDSHAKE_CALIBRATE)) {
//                            try {
                            Log.i("RAWREAD", buffData.substring(buffData.indexOf(Configs.PREFIX_HANDSHAKE_CALIBRATE)));
                            // +1 for STANDBY STATUS code number (Don't remove -1 +1 this is for understanding)
                            int handshakeCalibrationStartIndex = buffData.indexOf(Configs.PREFIX_HANDSHAKE_CALIBRATE);
                            int handshakeCalibrationTerminalIndex = buffData.indexOf(Configs.PREFIX_HANDSHAKE_CALIBRATE) + Configs.PREFIX_HANDSHAKE_CALIBRATE.length() + Configs.HANDSHAKE_CALIBRATE_LENGTH - 1;
                            String handshakeCalibrationValue = buffData.substring(handshakeCalibrationStartIndex + Configs.PREFIX_HANDSHAKE_CALIBRATE.length(), handshakeCalibrationTerminalIndex + 1);
                            broadcastHandshakeCalibration(handshakeCalibrationValue);
                            dataBufferVentilator.delete(handshakeCalibrationStartIndex, handshakeCalibrationTerminalIndex + 1);
//                            } catch (Exception e) {
//                                dataBufferVentilator.delete(0, dataBufferVentilator.length());
//                            }

                        }
                        else if (buffData.contains(Configs.QB_NEONATE_SENSOR_CONNECTED)) {

//                            try {
                            int neoSignalStartIndex = buffData.indexOf(Configs.QB_NEONATE_SENSOR_CONNECTED);
                            Log.i("NEO_SENSOR", buffData);
                            broadcastNeoNateSensorConnectResponse();
                            dataBufferVentilator.delete(neoSignalStartIndex, neoSignalStartIndex + Configs.QB_NEONATE_SENSOR_CONNECTED.length());
//                            } catch (Exception e) {
//                                dataBufferVentilator.delete(0, dataBufferVentilator.length());
//                            }

                        }
                        else if (buffData.contains(Configs.QB_NEONATE_SENSOR_DISCONNECTED)) {
//                            try {
                            int neoSignalDStartIndex = buffData.indexOf(Configs.QB_NEONATE_SENSOR_DISCONNECTED);
                            broadcastNeoNateSensorDisconnectResponse();
                            dataBufferVentilator.delete(neoSignalDStartIndex, neoSignalDStartIndex + Configs.QB_NEONATE_SENSOR_DISCONNECTED.length());
//                            } catch (Exception e) {
//                                dataBufferVentilator.delete(0, dataBufferVentilator.length());
//                            }


                        }
                        else if (buffData.contains(Configs.LIMITER_HARDWARE_SERIAL_NUMBER)) {
//                            try {
                            Log.i("check_serial_number_value", buffData);
                            int serialNumberStartIndex = buffData.indexOf(Configs.LIMITER_HARDWARE_SERIAL_NUMBER);
                            int serialNumberTerminalIndex = buffData.indexOf(Configs.DELIMITER_HARDWARE_SERIAL_NUMBER);
                            if (serialNumberStartIndex < serialNumberTerminalIndex) {
                                String data = buffData.substring(serialNumberStartIndex + 3, serialNumberTerminalIndex);
                                Log.i("check_serial_number_value", data);
                                broadcastHardwareSerialNumber(data);
                                dataBufferVentilator.delete(serialNumberStartIndex, serialNumberTerminalIndex + 1);
                            }
//                            } catch (Exception e) {
//                                dataBufferVentilator.delete(0, dataBufferVentilator.length());
//                            }

                        }
                        else if (buffData.contains(Configs.LIMITER_GRAPH_LIMIT)) {
//                            try {
                            Log.i("CHECKGRAPH_VALUE0", buffData);
                            int graphStartIndex = buffData.indexOf(Configs.LIMITER_GRAPH_LIMIT);
                            int graphTerminalIndex = buffData.indexOf(Configs.DELIMITER_GRAPH_LIMIT);
                            if (graphStartIndex < graphTerminalIndex) {
                                String data = buffData.substring(graphStartIndex + 3, graphTerminalIndex);
                                Log.i("CHECKGRAPH_VALUE", data);
                                broadcastGraphLimits(data);
                                dataBufferVentilator.delete(graphStartIndex, graphTerminalIndex + 1);
                            }

//                            } catch (Exception e) {
//                                dataBufferVentilator.delete(0, dataBufferVentilator.length());
//                            }

                        }
                        else if(buffData.contains(Configs.LIMITER_VENTI_LIVE)){
                            int liveDataStartIndex = buffData.indexOf(Configs.LIMITER_VENTI_LIVE);
                            int liveDataTerminalIndex = buffData.indexOf(Configs.DELIMITER_VENTI_LIVE);
                            if (liveDataStartIndex < liveDataTerminalIndex) {
                                String liveData = buffData.substring(liveDataStartIndex + 2, liveDataTerminalIndex);
                                Log.i("CHECK_LIVE_DATA", liveData);
                                broadcastVentiLiveData(liveData);
                                dataBufferVentilator.delete(liveDataStartIndex, liveDataTerminalIndex + 1);
                            }
                        }
                        else if (buffData.contains(Configs.PREFIX_CALIBRATION_ERROR)) {
//                            try {
                            //String sizeToDelete = buffData.substring(buffData.indexOf(buffData.indexOf("E"),buffData.indexOf("!")));
                            String calibErrorData = buffData.substring(buffData.indexOf("@") + 1, buffData.indexOf("!"));
                            String[] calibDataArray = calibErrorData.split(",");
                            String pressure = calibDataArray[0];
                            String flow = calibDataArray[1];
                            String dutycycle = calibDataArray[2];
                            Log.i("CHECK_ERROR_DATA", "Pressure " + pressure + "Flow " + flow + "dutycycle " + dutycycle);

                            broadcastCalibrationErrorResponse(pressure, flow, dutycycle);

                            dataBufferVentilator.delete(buffData.indexOf("E"), buffData.indexOf("!"));


//                            } catch (Exception e) {
//                                dataBufferVentilator.delete(0, dataBufferVentilator.length());
//                            }
                        }
                        else {

                            if (buffData.contains("{")) {
                                int dataTerminalIndex = buffData.indexOf("{");
                                String data = buffData.substring(0, dataTerminalIndex + 1);
                                broadcastDiagnosticData(data);
                                dataBufferVentilator.delete(0, dataTerminalIndex + 1);
                            }

                            // Ventilator data
                            if (buffData.contains("#")) {
                                int dataTerminalIndex = buffData.indexOf("#");
                                String data = buffData.substring(0, dataTerminalIndex + 1);
                                Log.i("CHECKED_DATA", "D Data is" + data);
                                broadcastData(data);
                                dataBufferVentilator.delete(0, dataTerminalIndex + 1);
                            }

                            if (buffData.contains(Configs.PREFIX_HARDWARE_VERSION)) {
                                if (buffData.contains("$")) {
                                    int updationStartIndex = buffData.indexOf(Configs.PREFIX_HARDWARE_VERSION);
                                    int updationTerminalIndex = buffData.indexOf("$");
                                    if (updationStartIndex < updationTerminalIndex) {
                                        String softwareUpdateData = buffData.substring(updationStartIndex, updationTerminalIndex);
                                        broadcastSoftwareVersion(softwareUpdateData);
                                        dataBufferVentilator.delete(updationStartIndex, updationTerminalIndex + 1);
                                    }
                                }
                            }

                            if (buffData.contains(Configs.PREFIX_SENSOR_CALIBRATION)) {

                                int sensorCalibStartIndex = buffData.indexOf(Configs.PREFIX_SENSOR_CALIBRATION);
                                int sensorCalibTerminalIndex = buffData.indexOf(Configs.PREFIX_SENSOR_CALIBRATION) + Configs.PREFIX_SENSOR_CALIBRATION.length() + Configs.SENSOR_CALIBRATION_REQUEST_LENGTH - 1;

                                if (sensorCalibStartIndex < sensorCalibTerminalIndex) {
                                    String sensorCalibrationData = buffData.substring(sensorCalibStartIndex + Configs.PREFIX_SENSOR_CALIBRATION.length(), sensorCalibTerminalIndex + 1);
                                    if (sensorCalibrationData.length() == Configs.SENSOR_CALIBRATION_REQUEST_LENGTH) {
                                        broadcastCalibrationSensorAnalysis(sensorCalibrationData.substring(0, Configs.TAG_SENSOR_LENGTH), sensorCalibrationData.substring(1, Configs.SENSOR_CALIBRATION_REQUEST_LENGTH));
                                        dataBufferVentilator.delete(sensorCalibStartIndex, sensorCalibTerminalIndex + 1);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.i("READ_THREAD_CHECK", "Index shortage");
                        e.getMessage();
                    }
                    startTime = System.currentTimeMillis();
                    isProcessingData = false;
                } else {
                    // No data to process, check if the timer has exceeded 3 seconds
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime > 3000) {
                        dataBufferVentilator.delete(0, dataBufferVentilator.length());
                        Log.i("READ_CHECK_VENTI", "Buffer cleared due to timeout");
                    }
                }
                try {
                    Thread.sleep(READ_DELAY);
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
        }
    }

    private class ReadingRunnableHID implements Runnable {
        @Override
        public void run() {
            Log.i("USB_CHECK", "hid reading thread started");
            while (true) {
                if (dataBufferHID.length() > 0) {

                    if (dataBufferHID.length() > 5) {
                        dataBufferHID.delete(0, dataBufferHID.length());
                    }
                    String buffData2 = dataBufferHID.toString();
                    VentilatorApp.Companion.setHidData(dataBufferHID + " : " + AppUtils.getCurrentTime());
                    Log.i("READ_CHECK_HID", "Reading thread started  " + " " + buffData2);

                    try {
                        if (buffData2.contains("X")) {
                            int prefixPlusStartIndex = buffData2.indexOf("X");
                            int prefixPlusTerminalIndex = buffData2.indexOf("X") + Configs.KNOB_LENGTH;
                            String xValue = buffData2.substring(prefixPlusStartIndex, prefixPlusTerminalIndex);
                            broadcastKnobResponse(xValue);
                            dataBufferHID.delete(prefixPlusStartIndex, prefixPlusTerminalIndex + 1);
                        }

                        if (buffData2.contains(Configs.PREFIX_PLUS)) {
                            int prefixPlusStartIndex = buffData2.indexOf(Configs.PREFIX_PLUS);
                            int prefixPlusTerminalIndex = buffData2.indexOf(Configs.PREFIX_PLUS) + Configs.KNOB_LENGTH;
                            String plusValue = buffData2.substring(prefixPlusStartIndex, prefixPlusTerminalIndex);
                            broadcastKnobResponse(plusValue);
                            dataBufferHID.delete(prefixPlusStartIndex, prefixPlusTerminalIndex + 1);

                        } else if (buffData2.contains(Configs.PREFIX_MINUS)) {
                            int prefixMinusStartIndex = buffData2.indexOf(Configs.PREFIX_MINUS);
                            int prefixMinusTerminalIndex = buffData2.indexOf(Configs.PREFIX_MINUS) + Configs.KNOB_LENGTH;
                            String minusValue = buffData2.substring(prefixMinusStartIndex, prefixMinusTerminalIndex);
                            broadcastKnobResponse(minusValue);
                            dataBufferHID.delete(prefixMinusStartIndex, prefixMinusTerminalIndex + 1);

                        } else if (buffData2.contains(Configs.PREFIX_AND)) {
                            int prefixAndStartIndex = buffData2.indexOf(Configs.PREFIX_AND);
                            int prefixAndTerminalIndex = buffData2.indexOf(Configs.PREFIX_AND) + Configs.KNOB_LENGTH;
                            String andValue = buffData2.substring(prefixAndStartIndex, prefixAndTerminalIndex);
                            broadcastKnobResponse(andValue);
                            dataBufferHID.delete(prefixAndStartIndex, prefixAndTerminalIndex + 1);

                        } else if (buffData2.contains(Configs.QB_ALARM_MUTE_UNMUTE)) {
                            int muteOptionStartIndex = buffData2.indexOf(Configs.QB_ALARM_MUTE_UNMUTE);
                            broadcastAlarmMuteUmuteResponse(buffData2.substring(muteOptionStartIndex, muteOptionStartIndex + Configs.QB_ALARM_MUTE_UNMUTE.length()));
                            dataBufferHID.delete(muteOptionStartIndex, muteOptionStartIndex + Configs.QB_ALARM_MUTE_UNMUTE.length());

                        } else if (buffData2.contains(Configs.QB_NEBULISER)) {
                            int nebuliserStartIndex = buffData2.indexOf(Configs.QB_NEBULISER);
                            broadcastNebuliserResponse();
                            dataBufferHID.delete(nebuliserStartIndex, nebuliserStartIndex + Configs.QB_NEBULISER.length());

                        } else if (buffData2.contains(Configs.QB_OXYGEN)) {
                            int oxygenStartIndex = buffData2.indexOf(Configs.QB_OXYGEN);
                            broadcastOxygenResponse(buffData2.substring(oxygenStartIndex, oxygenStartIndex + Configs.QB_OXYGEN.length()));
                            dataBufferHID.delete(oxygenStartIndex, oxygenStartIndex + Configs.QB_OXYGEN.length());

                        } else if (buffData2.contains(Configs.QB_INSPIRATORY_HOLD)) {
                            int inspiratoryStartIndex = buffData2.indexOf(Configs.QB_INSPIRATORY_HOLD);
                            broadcastInspiratoryHoldResponse();
                            dataBufferHID.delete(inspiratoryStartIndex, inspiratoryStartIndex + Configs.QB_INSPIRATORY_HOLD.length());

                        } else if (buffData2.contains(Configs.QB_EXPIRATORY_HOLD)) {
                            int expiratoryStartIndex = buffData2.indexOf(Configs.QB_EXPIRATORY_HOLD);
                            broadcastExpiratoryHoldResponse();
                            dataBufferHID.delete(expiratoryStartIndex, expiratoryStartIndex + Configs.QB_EXPIRATORY_HOLD.length());

                        } else if (buffData2.contains(Configs.QB_MANUAL_BREATH)) {
                            int manualStartIndex = buffData2.indexOf(Configs.QB_MANUAL_BREATH);
                            broadcastManualBreathResponse();
                            dataBufferHID.delete(manualStartIndex, manualStartIndex + Configs.QB_MANUAL_BREATH.length());

                        } else if (buffData2.contains(Configs.QB_HOME)) {
                            int homeStartIndex = buffData2.indexOf(Configs.QB_HOME);
                            broadcastHomeResponse();
                            dataBufferHID.delete(homeStartIndex, homeStartIndex + Configs.QB_HOME.length());

                        } else if (buffData2.contains(Configs.QB_LOCK)) {
                            int lockStartIndex = buffData2.indexOf(Configs.QB_LOCK);
                            broadcastLockResponse();
                            dataBufferHID.delete(lockStartIndex, lockStartIndex + Configs.QB_LOCK.length());

                        } else if (buffData2.contains(Configs.QB_POWER_SWITCH)) {
                            int switchStopIndex = buffData2.indexOf(Configs.QB_POWER_SWITCH);
                            broadcastPowerSwitchOffResponse();
                            dataBufferHID.delete(switchStopIndex, switchStopIndex + Configs.QB_POWER_SWITCH.length());

                        } else if (buffData2.contains(Configs.QB_POWER_ON)) {
                            int switchStartIndex = buffData2.indexOf(Configs.QB_POWER_ON);
                            broadcastPowerSwitchOnResponse();
                            dataBufferHID.delete(switchStartIndex, switchStartIndex + Configs.QB_POWER_ON.length());
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        Log.i("READ_CHECK_HID", "Index shortage");
                        e.getMessage();
                    }

                }
            }
        }
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent == null || intent.getAction() == null) return;
            switch (intent.getAction()) {
                case IntentFactory.ACTION_USB_PERMISSION_VENTILATOR:
                    Log.i("USB_CHECK", "venti permission broadcast started");
                    if (intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)) {
                        Log.i("USB_CHECK", "venti permission broadcast started and get permission");
                        openConnectionToReadVentilator(true);
                    }
                    break;

                case IntentFactory.ACTION_USB_PERMISSION_HID:
                    Log.i("USB_CHECK", "hid permission broadcast started");
                    if (intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)) {
                        Log.i("USB_CHECK", "hid permission broadcast started and get permission");
                        openConnectionToReadHID(true);
                    }
                    break;

                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    Log.i("USB_CHECK", "devices attached");
                    sendBroadcast(new Intent(IntentFactory.ACTION_DEVICE_CONNECTED));
                    break;

                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    Log.i("USB_CHECK", "devices detached");
                    sendBroadcast(new Intent(IntentFactory.ACTION_DEVICE_DISCONNECTED));
                    usbVentilatorPort = null;
                    preferenceManager.setBoundDevice(null, null);
                    broadcastUsbCommunicationData(isVentilatorConnected(), isHIDConnected());
                    break;
            }
        }
    };

    private IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(IntentFactory.ACTION_USB_PERMISSION_VENTILATOR);
        filter.addAction(IntentFactory.ACTION_USB_PERMISSION_HID);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        return filter;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        preferenceManager = new PreferenceManager(this);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        registerReceiver(receiver, getIntentFilter());

        Log.i("USB_CHECK", "usb service onCreate Called");
        Log.i("USB_SERVICE_STATUS", "Created Service onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i("USB_CHECK", "usb service onStartCommand Called");
        preferenceManager.setServiceStatus(true);

//        Notification notification = new NotificationCompat.Builder(this)
//                .setContentTitle("AgVa Service")
//                .setContentText("Services Started")
//                .build();
//        startForeground(1, notification);

        Log.i("USB_SERVICE_STATUS", "Started service onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        Log.i("USB_CHECK", "usb service onDestroy Called");
        unregisterReceiver(receiver);
        try {
            closeConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        dataBufferVentilator.delete(0, dataBufferVentilator.length());
        dataBufferHID.delete(0, dataBufferHID.length());
        preferenceManager.setServiceStatus(false);
//        usbVentilator = null;// NEW SHARED PREFERENCE STATUS
//        usbHID = null;
        super.onDestroy();

    }

    @Override
    public boolean isVentilatorConnected() {


        for (Map.Entry<String, UsbDevice> entry : usbManager.getDeviceList().entrySet()) {
            Log.i("CHECK_MANAGER", String.valueOf(entry.getValue().getVendorId()));
            if (entry.getValue().getVendorId() == ARDUINO_VENDOR_ID_VENTILATOR) return true;
        }
        return false;
    }

    @Override
    public boolean isHIDConnected() {
        for (Map.Entry<String, UsbDevice> entry : usbManager.getDeviceList().entrySet()) {
            if (entry.getValue().getVendorId() == ARDUINO_VENDOR_ID_HID) return true;
        }
        return false;
    }

    @Override
    public void sendBroadcastHandshakeCompleted() {
        Log.i("HANDSHAKE CHECK", "Double handshake completed");
        sendBroadcast(new Intent(IntentFactory.ACTION_HANDSHAKE_COMPLETED));
    }

    @Override
    protected void broadcastAcknowledgement(String ack) {
        Log.w("ACK CHECK", ack);
        Intent i = new Intent(IntentFactory.ACTION_ACK_AVAILABLE);
        i.putExtra(VENTILATOR_ACK, ack);
        sendBroadcast(i);
    }

    @Override
    protected void broadcastVentiLiveData(String data) {

    }

    @Override
    protected void broadcastBatteryStatus(String btryLevel, String btryHealth, String remainingTime) {
        Log.w("BATTERY CHECK", btryLevel);
        try {
            Intent i = new Intent(IntentFactory.ACTION_BATTERY_STATUS_AVAILABLE);
            i.putExtra(VENTILATOR_BATTERY_LEVEL, Integer.valueOf(btryLevel));
            i.putExtra(VENTILATOR_BATTERY_HEALTH, Integer.valueOf(btryHealth));
            i.putExtra(VENTILATOR_BATTERY_TTE, Integer.valueOf(remainingTime));
            sendBroadcast(i);
        } catch (NumberFormatException e) {
            e.getMessage();
            // ServerLogger.Companion.e(UsbService.this, e);
        }
    }

    @Override
    protected void broadcastCalibrationErrorResponse(String pressure, String flow, String dutycycle) {
        try {
            Intent i = new Intent(IntentFactory.ACTION_CALIBRATION_ERROR_DATA);
            i.putExtra("pressure", pressure);
            i.putExtra("flow", flow);
            i.putExtra("dutycycle", dutycycle);
            sendBroadcast(i);
        } catch (Exception e) {
            Log.w("Calibration_error", "Unable to parse some values of calibration error response");
            e.getMessage();
        }
    }

    @Override
    protected void broadcastHeatSensorStatus(ArrayList<String> values) {
        Log.w("HEATSENSE_CHECK", values.toString());
        try {
            Intent i = new Intent(IntentFactory.ACTION_HEATSENSE_STATUS_AVAILABLE);
            i.putIntegerArrayListExtra(VENTILATOR_HEATSENSE_DATA, new ArrayList<>(values.stream().map(Integer::valueOf).collect(Collectors.toList())));
            sendBroadcast(i);
        } catch (Exception e) {
            Log.w("HEATSENSE_CHECK", "Unable to parse some values");

            e.getMessage();
            // ServerLogger.Companion.e(UsbService.this, e);
        }
    }

    @Override
    protected void broadcastSensorAnalysis(String sensorAnalysis) {
        Log.w("SENSOR_ANALYSIS_CHECK", String.valueOf(sensorAnalysis));
        try {
            Intent i = new Intent(IntentFactory.ACTION_SENSOR_AVAILABILITY_RESPONSE);
            String[] value = sensorAnalysis.split(",");
            ArrayList<String> listValue = new ArrayList<>();

            Collections.addAll(listValue, value);
            i.putStringArrayListExtra(SENSOR_ANALYSIS, listValue);
            i.putExtra("Sensor_LIST_SIZE", listValue.size());
            sendBroadcast(i);
        } catch (NumberFormatException e) {
            dataBufferVentilator.delete(0, dataBufferVentilator.length());
            e.getMessage();
            //  ServerLogger.Companion.e(UsbService.this, e);
        }
    }

    @Override
    protected void broadcastCalibrationSensorAnalysis(String sensorTag, String calibrationStatus) {

        Log.w("CALIBRATION_CHECK", sensorTag + " sensor calibration = " + calibrationStatus);

        try {

            Intent i = new Intent(IntentFactory.ACTION_SENSOR_CALIBRATION_RESPONSE);
            i.putExtra(VENTILATOR_SENSOR_CALIBRATION_TAG, sensorTag);
            i.putExtra(VENTILATOR_SENSOR_CALIBRATION_RESULT, Integer.valueOf(calibrationStatus));

            sendBroadcast(i);
        } catch (NumberFormatException e) {
            e.getMessage();
            //  ServerLogger.Companion.e(UsbService.this, e);
        }

    }


    @Override
    protected void broadcastDeviceNameRequested(String deviceNameReqCode) {
        Log.w("DEVICENAME CHECK", "Requested by ventilator");
        try {
            if (deviceNameReqCode != null) {
                Intent i = null;
                switch (deviceNameReqCode) {
                    case "0":
                        i = new Intent(IntentFactory.ACTION_DEVICE_NAME_REQUESTED);
                        break;

                    case "1":
                        i = new Intent(IntentFactory.ACTION_DEVICE_NAME_RESPONSED);
                        i.putExtra(VENTILATOR_DEV_NAME_RESPONSE, true);
                        break;

                    case "2":
                        i = new Intent(IntentFactory.ACTION_DEVICE_NAME_RESPONSED);
                        i.putExtra(VENTILATOR_DEV_NAME_RESPONSE, false);
                        break;
                }

                if (i != null) sendBroadcast(i);
            }
        } catch (NumberFormatException e) {
            e.getMessage();
            //  ServerLogger.Companion.e(UsbService.this, e);
        }
    }

    @Override
    protected void broadcastMotorLifeLevelStatus(String motorLifeLevel) {
        Log.w("MOTORLIFE CHECK", motorLifeLevel);
        try {
            Intent i = new Intent(IntentFactory.ACTION_MOTOR_LIFE_STATUS_AVAILABLE);
            i.putExtra(VENTILATOR_MOTOR_LIFE, Integer.valueOf(motorLifeLevel));
            sendBroadcast(i);
        } catch (NumberFormatException e) {
            e.getMessage();
            // ServerLogger.Companion.e(UsbService.this, e);
        }
    }

    private void broadcastHandshakeCalibration(String calibrationValue) {
        Log.i("HS_CALIB_CHECK", calibrationValue);
        try {
            Intent i = new Intent(IntentFactory.ACTION_HANDSHAKE_CALIBRATION_AVAILABLE);
            i.putExtra(VENTILATOR_HANDSHAKE_CALIBRATION, calibrationValue);
            sendBroadcast(i);
        } catch (NumberFormatException e) {
            e.getMessage();
            //   ServerLogger.Companion.e(UsbService.this, e);
        }
    }

    @Override
    protected void broadcastStandbyResponse(String standbyResponse) {
        Log.w("STANDBY_CHECK", standbyResponse);
        try {
            Intent i = new Intent(IntentFactory.ACTION_STANDBY_STATUS_AVAILABLE);
            i.putExtra(VENTILATOR_STANDBY_STATUS, Integer.valueOf(standbyResponse));
            sendBroadcast(i);
        } catch (NumberFormatException e) {
            e.getMessage();
            //      ServerLogger.Companion.e(UsbService.this, e);
        }
    }


    @Override
    protected void broadcastWifiConnectionResponse(String wifiConnectionResponse) {

        Log.w("STANDBY_CHECK", wifiConnectionResponse);
        try {
            Intent i = new Intent(IntentFactory.ACTION_VENTILATOR_WIFI_CONNECTION_RESPONSED);
            i.putExtra(VENTILATOR_WIFI_CONNECTION_RESPONSE, Integer.parseInt(wifiConnectionResponse) == 1);
            sendBroadcast(i);
        } catch (NumberFormatException e) {
            e.getMessage();
            //ServerLogger.Companion.e(UsbService.this, e);
        }
    }

    @Override
    protected void broadcastSelfTestResponse(String selftestResponse) {
        Log.w("SELFTEST_CHECK", selftestResponse);
        try {
            Intent i = new Intent(IntentFactory.ACTION_SELF_TEST_STATUS_AVAILABLE);
            i.putExtra(VENTILATOR_SELF_TEST_STATUS, Integer.valueOf(selftestResponse));
            sendBroadcast(i);
        } catch (NumberFormatException e) {
            e.getMessage();
            //ServerLogger.Companion.e(UsbService.this, e);
        }
    }


    @Override
    protected void broadcastData(String data) {
        Log.w("SMART DATA CHECK", data);
        Intent i = new Intent(IntentFactory.ACTION_DATA_AVAILABLE);
        i.putExtra(VENTILATOR_DATA, data);
        sendBroadcast(i);
    }

    @Override
    protected void broadcastScannedWifiDevices(String devicesJson) {
        Log.w("PLAIN DATA CHECK", devicesJson);
        try {
            JSONArray json = new JSONArray(devicesJson);
            boolean isJsonValid = (json.length() > 0);
            if (isJsonValid) {
                Intent i = new Intent(IntentFactory.ACTION_VENTILATOR_WIFI_CONNECTION_REQUESTED);
                i.putExtra(VENTILATOR_WIFI_DEVS, devicesJson);
                sendBroadcast(i);
            }
        } catch (JSONException e) {
            Log.i("WIFIDEVICES CHECK", "[INVALID JSON] Unable to parse the data from ventilator");
            e.getMessage();
            //ServerLogger.Companion.e(UsbService.this, e);

        }

    }

    //For the Software Version in the info Fragment.
    @Override
    protected void broadcastSoftwareVersion(String softwareUpdateData) {
        Log.w("SOFTVERSION CHECK", softwareUpdateData);
        try {
            Intent i = new Intent(IntentFactory.ACTION_SOFTWARE_VERSION_AVAILABLE);
            i.putExtra(VENTILATOR_SOFTWARE_VERSION, softwareUpdateData);
            sendBroadcast(i);
        } catch (NumberFormatException e) {
            e.getMessage();
            //ServerLogger.Companion.e(UsbService.this, e);
        }
    }

    @Override
    protected void broadcastRawData(String data) {
        Log.w("PLAIN DATA CHECK", data);
        Intent i = new Intent(IntentFactory.ACTION_RAW_DATA_AVAILABLE);
        i.putExtra(VENTILATOR_RAW_DATA, data);
        sendBroadcast(i);
    }

    @Override
    public void startReading() {
        Log.i("USB_CHECK", "usb services startReading called");
        if (usbManager != null) {
            openConnectionToReadVentilator(null);
            openConnectionToReadHID(null);
        }

        // starting watch dog surveillance
        new Handler().postDelayed(this::startWatchDog, 500);

        Log.i("S_THREAD_CHECK", "START Ventilator Thread = " + bufferReadingThreadVentilator + " | isAlive = " + bufferReadingThreadVentilator);
        Log.i("S_THREAD_CHECK", "START HID Thread = " + bufferReadingThreadHID + " | isAlive = " + bufferReadingThreadHID);

        // start monitoring buffer and read data concurrently
        if (bufferReadingThreadVentilator == null) bufferReadingThreadVentilator = new Thread(new ReadingRunnableVentilator());
        if (!bufferReadingThreadVentilator.isAlive()) bufferReadingThreadVentilator.start();

        if (bufferReadingThreadHID == null)
            bufferReadingThreadHID = new Thread(new ReadingRunnableHID());
        if (!bufferReadingThreadHID.isAlive()) bufferReadingThreadHID.start();

        Log.i("S_THREAD_CHECK", "START Ventilator Thread = " + bufferReadingThreadVentilator + " | isAlive = " + bufferReadingThreadVentilator);
        Log.i("S_THREAD_CHECK", "START HID Thread = " + bufferReadingThreadHID + " | isAlive = " + bufferReadingThreadHID);

    }

    @Override
    public void stopReading() {
        stopWatchDog();
        Log.i("USB_CHECK", "usb services stopReading called");

        Log.i("S_THREAD_CHECK", "STOP Ventilator Thread = " + bufferReadingThreadVentilator + " | isAlive = " + bufferReadingThreadVentilator);
        Log.i("S_THREAD_CHECK", "STOP HID Thread = " + bufferReadingThreadHID + " | isAlive = " + bufferReadingThreadHID);

        if (bufferReadingThreadVentilator != null) {
            if (bufferReadingThreadVentilator.isAlive() && !bufferReadingThreadVentilator.isInterrupted()) {
                bufferReadingThreadVentilator.interrupt();
            }
        }

        if (bufferReadingThreadHID != null) {
            if (bufferReadingThreadHID.isAlive() && !bufferReadingThreadHID.isInterrupted()) {
                bufferReadingThreadHID.interrupt();
            }
        }
    }

    @Override
    public void sendDatatoKnob(String data) {
        Log.i("WRITE_CHECK_KNOB", "DATA : " + data);
        if (usbHIDPort != null) {
            try {
                usbHIDPort.write(data.getBytes(), 0);
            } catch (IOException e) {
                e.getMessage();
            }
            Log.i("WRITE_CHECK_KNOB", "DATA : " + data);
        } else {
            Log.i("USB_SERVICE_STOPPED", "USB SERVICE IS STOPPED");
        }
    }

    //to send data
    @Override
    public void send(String data) {
        Log.i("WRITE_CHECK", "PRE_DATA : " + data);
        if (usbWriteVentilatorPort != null) {
            if (data.isEmpty()) return;
            Log.i("WRITE_CHECK", "NO_EMPTY_DATA : " + data);
            if (isVentilatorMode(data)) {
                int mode = Integer.parseInt(data);
                if (Configs.isOxygenLevelsAvailable) {

                    try {
                        usbWriteVentilatorPort.write(data.getBytes(), 0);
                    } catch (IOException e) {
                        e.getMessage();
                    }

                    Log.i("new_ventilation", data);

                } else
                    //   usbVentilator.write((data + 1).getBytes());   // Forced High Oxygen mode [Temporary fix]
                    try {
                        usbWriteVentilatorPort.write(data.getBytes(), 0);
                    } catch (IOException e) {
                        e.getMessage();
                    }

                Log.i("MODE_CHECK", "Sending mode = " + data + " to ventilator");
                Intent i = new Intent(IntentFactory.ACTION_SUBMODE_SET);
                i.putExtra(VENTILATOR_CONTROL_SUB_MODE, mode);
                sendBroadcast(i);

            } else {
                try {
                    usbWriteVentilatorPort.write(data.getBytes(), 0);
                } catch (IOException e) {
                    e.getMessage();
                }

                Intent i = new Intent(IntentFactory.ACTION_DATA_SENT);
                i.putExtra(VENTILATOR_DATA_SEND, data);
                sendBroadcast(i);
            }

            Log.i("WRITE_CHECK", "DATA : " + data);
        } else {
            Log.i("USB_SERVICE_STOPPED", "USB SERVICE IS STOPPED");
        }
    }

    @Override
    public void broadcastTubeComplianceResponse(String tubeComplianceResponse) {
        if (tubeComplianceResponse != null) {
            Log.i("testingCompliance", "DATA : " + tubeComplianceResponse);
            Intent i = new Intent(IntentFactory.ACTION_COMPLIANCE_CALIBRATION_RESPONSE);
            i.putExtra(TUBE_COMPLIANCE_CALIBRATION, tubeComplianceResponse);
            sendBroadcast(i);
        }
    }

    @Override
    protected void broadcastDiagnosticData(String data) {
        if (data != null) {
            Log.i("diaDataTesting", "DATA : " + data);
            Intent i = new Intent(IntentFactory.ACTION_DIAGNOSTIC_TOOL_CHECK);
            i.putExtra(DIAG_TOOL_CHECK, data);
            sendBroadcast(i);
        }
    }

    @Override
    protected void broadcastSelfTestData(String data) {
        if (data != null) {
            Log.i("SELF_TEST_CHECK", "DATA : " + data);
            Intent i = new Intent(IntentFactory.ACTION_SELF_TEST_DATA_AVAILABLE);
            i.putExtra(SELF_TEST_CHECK, data);
            sendBroadcast(i);
        }
    }

    @Override
    protected void broadcastStartupcheckData(String startupCheckAnalysis) {
        Log.i("STARTUP_CHECK", String.valueOf(startupCheckAnalysis));
        try {

            Intent inte = new Intent(IntentFactory.ACTION_STARTUP_CHECK);
            inte.putExtra("startupCheckAnalysis", startupCheckAnalysis);
            sendBroadcast(inte);
        } catch (NumberFormatException e) {
            dataBufferVentilator.delete(0, dataBufferVentilator.length());
            e.getMessage();
        }
    }

    @Override
    protected void broadcastOxygenRegulationData(String data) {
        if (data != null) {
            Log.i("O2_REGILATION_CHECK", "DATA : " + data);
            Intent i = new Intent(IntentFactory.ACTION_O2_REGILATION_TOOL_CHECK);
            i.putExtra(OR_TOOL_CHECK, data);
            sendBroadcast(i);
        }
    }

    @Override
    public void checkConnection() {
    }

    @Override
    public void broadcastTubeResistanceResponse(String tubeResistanceResponse) {
        if (tubeResistanceResponse != null) {
            Log.i("testing", "DATA : " + tubeResistanceResponse);
            Intent i = new Intent(IntentFactory.ACTION_RESISTANCE_CALIBRATION_RESPONSE);
            i.putExtra(TUBE_RESISTANCE_CALIBRATION, tubeResistanceResponse);
            sendBroadcast(i);
        }
    }


    @Override
    protected void broadcastKnobResponse(String knobResponse) {
        if (knobResponse != null) {
            Log.i("KNOB_CHECK", "DATA : " + knobResponse);
            Intent i = new Intent(IntentFactory.ACTION_KNOB_CHANGE);
            i.putExtra(VENTILATOR_CONTROL_KNOB, knobResponse);
            sendBroadcast(i);
        }
    }


    @Override
    protected void broadcastAlarmMuteUmuteResponse(String value) {
        Log.i("muteValue", "broadcast send");
        Intent i = new Intent(IntentFactory.ACTION_MUTE_UNMUTE);
        i.putExtra(MUTE_UNMUTE_RESPONSE, value);
        sendBroadcast(i);
    }

    @Override
    protected void broadcastNeoNateSensorConnectResponse() {
        sendBroadcast(new Intent(IntentFactory.ACTION_NEO_SENSOR_CONNECT));
    }

    @Override
    protected void broadcastNeoNateSensorDisconnectResponse() {
        sendBroadcast(new Intent(IntentFactory.ACTION_NEO_SENSOR_DISCONNECT));
    }

    @Override
    protected void broadcastNebuliserResponse() {
        sendBroadcast(new Intent(IntentFactory.ACTION_NEBULISER));
    }

    @Override
    protected void broadcastOxygenResponse(String value) {
        Intent i = new Intent(IntentFactory.ACTION_OXYGEN_100);
        i.putExtra(FIO2_RESPONSE, value);
        sendBroadcast(i);
    }

    @Override
    protected void broadcastInspiratoryHoldResponse() {
        sendBroadcast(new Intent(IntentFactory.ACTION_INSPIRATORY_HOLD));
    }

    @Override
    protected void broadcastExpiratoryHoldResponse() {
        sendBroadcast(new Intent(IntentFactory.ACTION_EXPIRATORY_HOLD));
    }

    @Override
    protected void broadcastManualBreathResponse() {
        sendBroadcast(new Intent(IntentFactory.ACTION_MANUAL_BREATH));
    }

    @Override
    protected void broadcastHomeResponse() {
        sendBroadcast(new Intent(IntentFactory.ACTION_HOME));
    }

    @Override
    protected void broadcastLockResponse() {
        sendBroadcast(new Intent(IntentFactory.ACTION_LOCK));
    }

    @Override
    protected void broadcastPowerSwitchOffResponse() {
        sendBroadcast(new Intent(IntentFactory.ACTION_POWER_OFF));
    }

    @Override
    protected void broadcastxResponse(String valuex) {
        if (valuex != null) {
            Log.i("KNOB_CHECK", "DATA : " + valuex);
            Intent i = new Intent(IntentFactory.ACTION_KNOB_X);
            i.putExtra(VENTILATOR_KNOB_X, valuex);
            sendBroadcast(i);
        }
    }

    @Override
    protected void broadcastPowerSwitchOnResponse() {
        sendBroadcast(new Intent(IntentFactory.ACTION_POWER_ON));
    }


    private boolean isVentilatorMode(String ventData) {
        try {
            return Configs.isValidVentilatorMode(getApplicationContext(), Integer.parseInt(ventData));
        } catch (Exception e) {
            e.getMessage();
            return false;
        }
    }

    private boolean isOxygenSupportedVentilatorMode(String ventData) {
        try {
            return Configs.isValidOxygenSupportedMode(getApplicationContext(), Integer.parseInt(ventData));
        } catch (Exception e) {
            e.getMessage();
            //ServerLogger.Companion.e(UsbService.this, e);
            return false;
        }

    }

    public void closeConnection() throws IOException {

        Log.i("USB_CHECK", "close connection method called with hid port: " + usbHIDPort + " venti original port : " + usbVentilatorPort + " venti temp port : " + usbWriteVentilatorPort);
        if (usbVentilatorPort != null) {
            usbVentilatorPort.close();
            usbVentilatorPort = null;
            usbWriteVentilatorPort = null;
        }
        if (usbHIDPort != null) {
            usbHIDPort.close();
            usbHIDPort = null;
        }
    }


    public void openConnectionToReadVentilator(Boolean permissionGranted) {
        Log.i("USB_CHECK", "venti open connection method called with : " + usbWriteVentilatorPort + " & " + permissionGranted);
        UsbDevice device = null;
        for (UsbDevice v : usbManager.getDeviceList().values())
            if (v.getVendorId() == ARDUINO_VENDOR_ID_VENTILATOR)
                device = v;
        if (device == null) {
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if (driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if (driver == null) {
            return;
        }
        usbVentilatorPort = driver.getPorts().get(0);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if (usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            Intent intent = new Intent(IntentFactory.ACTION_USB_PERMISSION_VENTILATOR);
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(UsbService.this, 0, intent, 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }

        try {
            usbVentilatorPort.open(usbConnection);
            usbVentilatorPort.setParameters(DEFAULT_BAUD_RATE_VENTILATOR, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            SerialInputOutputManager ioManager = new SerialInputOutputManager(usbVentilatorPort, this);
            ioManager.start();
            usbWriteVentilatorPort = usbVentilatorPort;

        } catch (IOException e) {
            e.getMessage();
        }


        broadcastUsbCommunicationData(isVentilatorConnected(), isHIDConnected());
    }

    @Override
    protected void broadcastUsbCommunicationData(Boolean venti, Boolean hid) {
        Intent i = new Intent(IntentFactory.ACTION_CHECK_USB_CONNECTIONS);
        i.putExtra(USB_HID_DATA, hid);
        i.putExtra(USB_VENTILATOR_DATA, venti);
        sendBroadcast(i);
    }

    public void openConnectionToReadHID(Boolean permissionGranted) {
        Log.i("USB_CHECK", "hid open connection method called with : " + usbHIDPort + " & " + permissionGranted);
        UsbDevice device = null;
        for (UsbDevice v : usbManager.getDeviceList().values()) {
            if (v.getVendorId() == ARDUINO_VENDOR_ID_HID)
                device = v;
        }
        if (device == null) {
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
        if (driver == null) {
            driver = CustomProber.getCustomProber().probeDevice(device);
        }
        if (driver == null) {
            return;
        }
        usbHIDPort = driver.getPorts().get(0);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if (usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            Intent intent = new Intent(IntentFactory.ACTION_USB_PERMISSION_HID);
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(UsbService.this, 0, intent, 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }

        try {
            usbHIDPort.open(usbConnection);
            usbHIDPort.setParameters(DEFAULT_BAUD_RATE_HID, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            SerialInputOutputHIDManager ioManagerHID = new SerialInputOutputHIDManager(usbHIDPort, this);
            ioManagerHID.start();
        } catch (IOException e) {
            e.getMessage();
        }
        broadcastUsbCommunicationData(isVentilatorConnected(), isHIDConnected());
    }

    private void readBytesDataVentilator(String receivedData) {
        Log.i("USB_CHECK", "read bytes venti method called with buffer : " + dataBufferVentilator + " & new data : " + receivedData);
        // appending to the data buffer
        if (!receivedData.trim().isEmpty()) {
            dataBufferVentilator.append(receivedData);
            informWatchDog();
        }
    }

    private void readBytesDataHID(String receivedData) {
//        Log.i("USB_CHECK", "read bytes hid method called with buffer : " + dataBufferHID + " & new data : " + receivedData);
        // appending to the data buffer
        if (!receivedData.trim().isEmpty()) {
            dataBufferHID.append(receivedData);
        }
    }

//    // created by masoom on 04 jan 2023
//    @Override
//    public void broadcastHFNCModeResponse(String[] value) {
//        if (value.length != 0) {
//            Intent i = new Intent(IntentFactory.ACTION_HFNC_RESPONSE);
//            i.putExtra(HFNC_MODE, value);
//            sendBroadcast(i);
//        }
//    }

    @Override
    protected void broadcastGraphLimits(String data) {
        Intent i = new Intent(IntentFactory.ACTION_GRAPH_LIMIT_CHANGE);
        i.putExtra(GRAPH_LIMITS, data);
        sendBroadcast(i);
    }

    @Override
    protected void broadcastHardwareSerialNumber(String data) {
        Intent i = new Intent(IntentFactory.ACTION_HARDWARE_SERIAL_NUMBER);
        i.putExtra(HARDWARE_SERIAL_NUMBER, data);
        sendBroadcast(i);
    }

    protected void finalize() {

    }
}

