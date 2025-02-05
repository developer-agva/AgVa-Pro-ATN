package com.agvahealthcare.ventilator_ext.utility;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

/**
 * Created by MOHIT MALHOTRA on 30-04-2018.
 */

public abstract class ToastFactory {

    @SuppressLint("StaticFieldLeak")
    public static View snackView;

    public static void setSnackBar(View rootLayout, String snackTitle) {
        snackView = CustomSnackBar.Companion.showSnackBar(rootLayout, snackTitle);
        snackView.setVisibility(View.VISIBLE);
    }

    public static void dismissSnackBar() {
        if(snackView!=null) {
            snackView.setVisibility(View.GONE);
        }
    }

    public static void connectionError(Context context){

        if(context != null) {
            makeText(context, "Check your internet connection", LENGTH_SHORT).show();
        }
    }

    public static void fillRequiredFields(Context context){

        if(context != null) {
            makeText(context, "Please fill the required fields", LENGTH_SHORT).show();
        }
    }

    public static  void custom(Context context, String msg){
        if(context != null) {
            makeText(context, msg, LENGTH_SHORT).show();
        }
    }


}
