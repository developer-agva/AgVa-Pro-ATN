package com.agvahealthcare.ventilator_ext.utility;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.agvahealthcare.ventilator_ext.R;
import com.agvahealthcare.ventilator_ext.dashboard.BaseActivity;


public class STPWindowHolder {

    private Context context;
    private AlertDialog dialog;
    private ImageView btnCross;
    private TextView tvResponse;


    public STPWindowHolder(Context context) {
        this.context = context;
    }

    public void show(){
        View view = LayoutInflater.from(context).inflate( R.layout.layout_dialog_selftest, null, false);
        btnCross = view.findViewById(R.id.btnCross);
        tvResponse = view.findViewById(R.id.tvResponse);

        this.dialog = new AlertDialog.Builder(context).setView(view).create();
        dialog.setCancelable(true);


        btnCross.setOnClickListener(v -> {
            dialog.cancel();

            // kill APP after exiting STP
            if (context instanceof BaseActivity) {
                BaseActivity activity = (BaseActivity) context;
                activity.finish();
            }
        });

        // to make the window background transparent
        if(dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public boolean isVisible(){
        return (dialog != null && dialog.isShowing());
    }

    public void clearWindow(){
        if(tvResponse != null){
            tvResponse.setText("");
        }
    }

    public void putResponse(String text){
        if(tvResponse != null){
            tvResponse.setText(tvResponse.getText() + text);
        }
    }

    public void showError(){
        putResponse("\n[!] Self test process terminated");
    }

    public void hide(){
        if(dialog != null && dialog.isShowing()) dialog.cancel();
    }

}
