package com.agvahealthcare.ventilator_ext.utility.utils;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.lang.reflect.Field;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by MOHIT MALHOTRA on 23-10-2018.
 */

public abstract class AppUtils {

    public static final String PATH_FOLDER_AGVA = ".AgVa";
    public static final String PATH_FOLDER_LOGS = "logs";
    public static final String PATH_FOLDER_MODE = "mode";
    public static final String PATH_FOLDER_SYSSNAPSHOT = "snapshots";
    public static final String PATH_FOLDER_SERVICE_AND_OP_MOD = ".servop";
    public static final long DURATION_VIBRATION = 70; // in milliseconds


    public static final SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat timeHourMinuteFormatter = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat errorDateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    public static final SimpleDateFormat dateTimeFormatterTest = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public static final SimpleDateFormat ventDateTimeFormatter = new SimpleDateFormat("yyyyMMddHH:mm:ss");
    public static final SimpleDateFormat dateFormatterReverse = new SimpleDateFormat("yyy-MM-dd");
    public static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    public static DecimalFormat decimalFormat = new DecimalFormat("0");


    // Does not let the screen die for the @param activity
    public static void keepScreenAlive(AppCompatActivity activity, boolean isAlive) {
        if (isAlive) activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Enable the @param activity to run in full screen
//    public static void enableFullScreen(AppCompatActivity activity) {
//
//        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        activity.getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
//        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

//        hideSystemUI(activity);
//
//        activity.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
//            if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.VISIBLE) {
//                new Handler().postDelayed(()-> hideSystemUI(activity), 4500);
//            }
//        });
//    }


    public static void hideSFSystemUI(Context ctx) {
        Log.i("SF_CHECK", "Intent for showing system");
        Intent intent = new Intent(IntentFactory.ACTION_SF_SHOW_FULL_SCREEN);
        ComponentName componentName = new ComponentName("com.promobitech.mobilock.pro", "com.promobitech.mobilock.component.FullScreenAddRemoveReceiver");
        intent.setComponent(componentName);
        ctx.sendBroadcast(intent);
    }

    public static void showSFSystemUI(Context ctx) {
        Log.i("SF_CHECK", "Intent for hiding system");
        Intent intent = new Intent(IntentFactory.ACTION_SF_HIDE_FULL_SCREEN);
        ComponentName componentName = new ComponentName("com.promobitech.mobilock.pro", "com.promobitech.mobilock.component.FullScreenAddRemoveReceiver");
        intent.setComponent(componentName);
        ctx.sendBroadcast(intent);
    }

  /*  public static String getLastBestTime(LocationManager locationManager) {

        Date machineDate = new Date();
        if(locationManager != null) {
            try {
                Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                long GPSLocationTime = 0;
                if (null != locationGPS) {
                    Log.i("TIMER_CHECK", "Found GPS");
                            GPSLocationTime = locationGPS.getTime();
                }

                long NetLocationTime = 0;

                if (null != locationNet) {
                    Log.i("TIMER_CHECK", "Found NETWORK");
                    NetLocationTime = locationNet.getTime();
                }

                Log.i("TIMER_CHECK", "GPS : " + GPSLocationTime + " ,  \nNETWORK : " + NetLocationTime + " ,  \nMACHINE : " + machineDate);

                long time = GPSLocationTime > NetLocationTime ? GPSLocationTime : NetLocationTime;
                return timeHourMinuteFormatter.format(new Date(time));
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        return timeHourMinuteFormatter.format(machineDate);
    }*/

    public static void changeScreenBrightness(final Window window, int brightnessLevel) {
        if (window != null && brightnessLevel >= 0 && brightnessLevel <= 255) {
            final float brightness = brightnessLevel / (float) 255;
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.screenBrightness = brightness;
            window.setAttributes(lp);
        }
    }

 /*   public static String readConfigFile(Context ctx, String fileName){

        final String filePath = Environment.getExternalStorageDirectory() + File.separator + PATH_FOLDER_AGVA + File.separator + fileName;


        try(FileInputStream fis = ctx.openFileInput(filePath)){

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(fis))){

                StringBuffer buffer = new StringBuffer();

                String line = reader.readLine();
                String mode = line.substring(line.indexOf("=")+1).trim();
                if(mode.length() != 0 && "Bluetooth".equalsIgnoreCase(mode))
                    CommunicationService.setCommunicationModeToBluetooth();

                return buffer.toString();

            } catch (Exception e){
                e.printStackTrace();
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }*/


    public static boolean isSimAvailable(Context ctx) {

        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            try {
                TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);  //gets the current TelephonyManager
                return !(tm.getSimState() == TelephonyManager.SIM_STATE_ABSENT);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        return false;
    }

    // Returns IMEI number for the device if sim is available
//    public static String getIMEI(Context ctx) {
//        String imei = "unavailable";
//
//        final boolean isPermissionGranted = ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
//        if (isPermissionGranted && isSimAvailable(ctx)) {
//
//            try {
//                TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
//                imei = telephonyManager.getDeviceId();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//        return imei;
//    }
//
//    public static String getPhoneNumber(Context ctx) {
//        String tel = "unavailable";
//
//        final boolean isPermissionGranted = ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
//        if (isPermissionGranted && isSimAvailable(ctx)) {
//
//            try {
//                TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
//                tel = telephonyManager.getLine1Number();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        return tel;
//
//    }


//    public static void shutdownDevice(Context ctx) {
//        try {
//
////            String myStringArray[]= {"logcat","-d"};
////
////            Runtime.getRuntime().exec(myStringArray);
//
//            Runtime.getRuntime().exec("am broadcast -a com.outform.hidebar").waitFor();

//            Runtime.getRuntime().exec("input keyevent KEYCODE_POWER").waitFor();
//
//            Runtime.getRuntime().exec("reboot -p");
//
////            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//
////            PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
////            if (pm != null) pm.reboot(null);
//        } catch (Exception e) {
//            Log.e("ADB_AGVA", "Unable to kill the app : " + e.getMessage());
//            e.printStackTrace();
//        }
//    }


    public static String getMacAddress(){
//        String stringMac = "c8:13:32:0e:9a:e6";  banglore
//        String stringMac = "c9:13:32:0e:9a:e10"; Delhi
        String stringMac = "c9:13:32:0e:9a:e11";
        return stringMac;
//change the MAX address here
       /* try{
            List<NetworkInterface> networkInterfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());
            String stringMac = "";
            for(NetworkInterface networkInterface : networkInterfaceList)
            {
                if(networkInterface.getName().equalsIgnoreCase("wlon0"));
                {
                    for(int i = 0 ;i <networkInterface.getHardwareAddress().length; i++){
                        String stringMacByte = Integer.toHexString(networkInterface.getHardwareAddress()[i]& 0xFF);
                        if(stringMacByte.length() == 1)
                        {
                            stringMacByte = "0" +stringMacByte;
                        }
                        stringMac = stringMac + stringMacByte.toUpperCase() + ":";
                    }
                    break;
                }
            }
            return stringMac;
        }catch (SocketException e)
        {
            e.printStackTrace();
        }
        return  "0";*/
    }



    public static boolean checkInternet(Context ctx) {
        ConnectivityManager conMgr =  (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        return netInfo!=null &&  netInfo.isConnected() && netInfo.isAvailable();
    }




    public static String getCurrentDateReverse() {
        return AppUtils.errorDateTimeFormatter.format(new Date());
    }

    public static String getCurrentDateTime() {
        return AppUtils.dateTimeFormatterTest.format(new Date());
    }
    public static String getCurrentDateTimeForTrends() {
        return AppUtils.dateTimeFormatter.format(new Date());
    }
    public static String getCurrentTime(){
        return AppUtils.timeFormatter.format(new Date());
    }

    public static String getCurrentDate() {
        return AppUtils.dateFormatter.format(new Date());
    }

    public static String getTimeForTrendsTime(String date) {
        return AppUtils.timeHourMinuteFormatter.format(date);
    }

    public static String[] splitDateTime(String dateTime) {
        // Split the date-time string using the space character
        String[] parts = dateTime.split(" ");

        // Return the array containing date and time components
        return parts;
    }

    public static String getDateForTrendsdate(String date) {
        return AppUtils.dateFormatter.format(date);
    }

   /* public static String getDeviceSerial(){
        String serialNum = android.os.Build.getSerial();
        return serialNum;
    }*/

    public static String getDeviceName(){
        String modelName=android.os.Build.MODEL;
        return modelName;
    }
    public static String getAndroidVersion(){
        String androidVersion=android.os.Build.VERSION.SDK;
        return androidVersion;
    }
    public static String getAndroidVersionName(){
        Field[] fields = Build.VERSION_CODES.class.getFields();
        String osName = fields[Build.VERSION.SDK_INT + 1].getName();
        return osName;
    }

    public static float getBatteryStatus(Context context){
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //Intent batteryStatus = registerReceiver(null, ifilter);
        Intent batteryStatus= context.registerReceiver(null,ifilter);
        int level=batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
        int scale=batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE,-1);
       // int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        //int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        return (batteryPct*100);
    }
    public static void setAlarmVolumeToMax(Context context){
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int  currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        int maxValue = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
        if (currentVolume < maxValue){
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,maxValue,0);
        }
    }
    public static void hideKeyBoard(Context context, View view){
        try {
            InputMethodManager imm=(InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);
        } catch (Error error){
            Log.e("error","Keyboard hiding issue"+error);
        }

    }
}