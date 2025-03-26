package com.agvahealthcare.ventilator_ext.utility;

import static com.agvahealthcare.ventilator_ext.utility.ConstantKt.PASSWORD_INFO_SYSTEM;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.fonts.FontStyle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.cardview.widget.CardView;

import com.agvahealthcare.ventilator_ext.R;
import com.agvahealthcare.ventilator_ext.api.model.serviceDataModel.Data;
import com.agvahealthcare.ventilator_ext.callback.OnModeChangeListener;
import com.agvahealthcare.ventilator_ext.callback.OtpVerifyListener;
import com.agvahealthcare.ventilator_ext.callback.PasswordCallbackListener;
import com.agvahealthcare.ventilator_ext.callback.SimpleCallbackListener;
import com.agvahealthcare.ventilator_ext.callback.UserInteractionAwareCallback;
import com.agvahealthcare.ventilator_ext.dashboard.DischargeListener;
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager;
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager;
import com.agvahealthcare.ventilator_ext.model.VentMode;
import com.agvahealthcare.ventilator_ext.system.o2Regulation.O2RegulationCheckViewModel;
import com.agvahealthcare.ventilator_ext.utility.callback.OnIssueCloseListener;
import com.agvahealthcare.ventilator_ext.utility.callback.SingleValueCallbackListener;
import com.agvahealthcare.ventilator_ext.utility.utils.GenericKeyEvent;
import com.agvahealthcare.ventilator_ext.utility.utils.GenericTextWatcher;
import com.google.android.material.textfield.TextInputLayout;
import com.uk.tastytoasty.TastyToasty;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import cdflynn.android.library.checkview.CheckView;


/**
 * Created by MOHIT MALHOTRA on 14-09-2018.
 */

public class DialogBoxFactory {

    public static AlertDialog dialogView;
    private static Handler handler = new Handler();

    public static void dismissDialogs() {
        if (dialogView != null) {
            dialogView.dismiss();
        }
    }

    public static AlertDialog showServicePaymentDialog(Context ctx) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_payment_service_dialog, null, false);

        TextView deviceIdText = view.findViewById(R.id.deviceIdText);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);

        deviceIdText.setText("Device Id : " + Settings.Secure.getString(
                ctx.getContentResolver(), Settings.Secure.ANDROID_ID
        ));

        setShutDownDialogView(dialog, true);

        dialog.show();

        return dialog;
    }

    public static void showSystemLockDialog(Context ctx) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.system_lock_dialog_layout, null, false);
        TextView tvMsg = view.findViewById(R.id.tvSystemLockDesc);
        Button btnOk = view.findViewById(R.id.btLock);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();

        dialog.setCancelable(true);

        btnOk.setOnClickListener(v -> {
            dialog.cancel();
        });

        dialog.show();

        setNeonateDialogView(dialog);
    }

//    public static AlertDialog showDialog(Context ctx, String title, String message, SimpleCallbackListener listener) {
//        return showDialog(ctx, title, message, null, listener);
//    }

    // created by Masoom 6 march 2023
//    public static void showGraphChangeDialog(Context ctx, String graphType , ChangeGraphListener click) {
//
//        View view = LayoutInflater.from(ctx).inflate(R.layout.change_graph_type_layout, null, false);
//        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
//
//        TextView tvPressureGraph = view.findViewById(R.id.tvPressureGraph);
//        TextView tvVolumeGraph = view.findViewById(R.id.tvVolumeGraph);
//        TextView tvFlowGraph = view.findViewById(R.id.tvFlowGraph);
//
//
//        tvPressureGraph.setOnClickListener(v ->{
//            click.setGraphType(graphType);
//            dialog.cancel();
//        });
//
//        tvVolumeGraph.setOnClickListener(v ->{
//            click.setGraphType(graphType);
//            dialog.cancel();
//        });
//
//        tvFlowGraph.setOnClickListener(v ->{
//            click.setGraphType(graphType);
//            dialog.cancel();
//        });
//
//
//        dialog.show();
//        setNeonateDialogView(dialog);
//    }

    public static AlertDialog showNetworkInfoDialog(Context ctx, SimpleCallbackListener onCLickSend, PasswordCallbackListener clickClose, String pass,String msg) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_info_reset, null, false);

        EditText etOtp1 = view.findViewById(R.id.etOtp1);
        EditText etOtp2 = view.findViewById(R.id.etOtp2);
        EditText etOtp3 = view.findViewById(R.id.etOtp3);
        EditText etOtp4 = view.findViewById(R.id.etOtp4);
        TextView title = view.findViewById(R.id.txtMessage);

        title.setText(msg);

        ImageView cross_img = view.findViewById(R.id.cross_img);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);

        InfoResetTextWatchers(etOtp1, etOtp2, etOtp3, etOtp4);

        setShutDownDialogView(dialog, true);

        cross_img.setOnClickListener(v -> {
            clickClose.closeDialog();
            dialog.dismiss();
        });

        etOtp4.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {

                    String password = etOtp1.getText().toString() + etOtp2.getText().toString() + etOtp3.getText().toString() + etOtp4.getText().toString();
                    if (password.equals(pass)) {
                        onCLickSend.doAction();
                        dialog.dismiss();
                    } else {
                        ToastFactory.custom(ctx, "password is wrong");
                        etOtp1.getText().clear();
                        etOtp2.getText().clear();
                        etOtp3.getText().clear();
                        etOtp4.getText().clear();
                        etOtp1.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        dialog.show();

        return dialog;
    }

    public static void showServiceRegisterDialog(Context ctx, String desc) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.neonate_sensor_dialog_layout, null, false);
        TextView tvMsg = view.findViewById(R.id.tvNeonateDesc);
        Button btnOk = view.findViewById(R.id.btnNeonate);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();

        tvMsg.setText(desc);
        dialog.setCancelable(false);

        btnOk.setOnClickListener(v -> {
            dialog.cancel();
        });

        dialog.show();

        setSuccessfulServiceDialog(dialog);
    }

    public static AlertDialog showOtpVerifyDialog(Context ctx, OtpVerifyListener onCLickSend, PasswordCallbackListener clickClose, String message, Boolean isServiceOpen) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_info_reset, null, false);

        EditText etOtp1 = view.findViewById(R.id.etOtp1);
        EditText etOtp2 = view.findViewById(R.id.etOtp2);
        EditText etOtp3 = view.findViewById(R.id.etOtp3);
        EditText etOtp4 = view.findViewById(R.id.etOtp4);
        TextView msg = view.findViewById(R.id.txtMessage);

        ImageView cross_img = view.findViewById(R.id.cross_img);

        msg.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);

        InfoResetTextWatchers(etOtp1, etOtp2, etOtp3, etOtp4);

        setShutDownDialogView(dialog, true);

        cross_img.setOnClickListener(v -> {
            clickClose.closeDialog();
            dialog.dismiss();
        });

        etOtp4.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {

                    String password = etOtp1.getText().toString() + etOtp2.getText().toString() + etOtp3.getText().toString() + etOtp4.getText().toString();
                    onCLickSend.doAction(password,isServiceOpen);

                    return true;
                }
                return false;
            }
        });

        dialog.show();

        return dialog;
    }

    public static void setSuccessfulServiceDialog(AlertDialog dialogView) {
        DialogBoxFactory.dialogView = dialogView;

        dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Objects.requireNonNull(dialogView.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogView.getWindow().getAttributes());
        lp.width = 700;
        lp.height = 500;
        lp.y = 1000;
        lp.x = 0;

        dialogView.getWindow().setAttributes(lp);
        // to make the window background transparent

        Log.d("dialog", String.valueOf(dialogView.getWindow().getAttributes().width));
        Log.d("dialog", String.valueOf(dialogView.getWindow().getAttributes().height));
        Log.d("dialog", String.valueOf(dialogView.getWindow().getAttributes().gravity));
    }

    public static AlertDialog showDialog(Context ctx, String title, String message, String btnText, SimpleCallbackListener clickListener) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_alert, null, false);
        TextView tvTitle = view.findViewById(R.id.tvHead);
        TextView tvMsg = view.findViewById(R.id.tvDesc);
        Button btnOk = view.findViewById(R.id.btnOk);

        if (title != null && !title.isEmpty()) tvTitle.setText(title);
        if (message != null && !message.isEmpty()) tvMsg.setText(message);
        if (btnText != null && !btnText.isEmpty()) btnOk.setText(btnText);


        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);


        btnOk.setOnClickListener(v -> {
            if (clickListener != null) clickListener.doAction();
            dialog.cancel();
        });

        setDialogView(dialog, false);
        dialog.show();

        return dialog;
    }

    public static AlertDialog showInfoResetDialog(Context ctx, SimpleCallbackListener onCLickSend, PasswordCallbackListener clickClose,String pass,String msg) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_info_reset, null, false);

        EditText etOtp1 = view.findViewById(R.id.etOtp1);
        EditText etOtp2 = view.findViewById(R.id.etOtp2);
        EditText etOtp3 = view.findViewById(R.id.etOtp3);
        EditText etOtp4 = view.findViewById(R.id.etOtp4);
        TextView title = view.findViewById(R.id.txtMessage);

        title.setText(msg);

        ImageView cross_img = view.findViewById(R.id.cross_img);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);

        InfoResetTextWatchers(etOtp1, etOtp2, etOtp3, etOtp4);

        setShutDownDialogView(dialog, true);

        cross_img.setOnClickListener(v -> {
            clickClose.closeDialog();
            dialog.dismiss();
        });

        etOtp4.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {

                    String password = etOtp1.getText().toString() + etOtp2.getText().toString() + etOtp3.getText().toString() + etOtp4.getText().toString();
                    if (password.equals(pass)) {
                        onCLickSend.doAction();
                        dialog.dismiss();
                    } else {
                        ToastFactory.custom(ctx, "password is wrong");
                        etOtp1.getText().clear();
                        etOtp2.getText().clear();
                        etOtp3.getText().clear();
                        etOtp4.getText().clear();
                        etOtp1.requestFocus();
                    }
                    return true;
                }
                return false;
            }
        });

        dialog.show();

        return dialog;
    }

    public static AlertDialog showServiceDialog(Context ctx, Data data, OnIssueCloseListener issueClose) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_service, null, false);

        ImageView cross_img = view.findViewById(R.id.iv_cross);
        TextView tvTicketNumber = view.findViewById(R.id.tvTicketNumber);
        TextView name = view.findViewById(R.id.tvNameValue);
        TextView contactNo = view.findViewById(R.id.tvContactName);
        TextView email = view.findViewById(R.id.tvEmailValue);
        TextView hospitalName = view.findViewById(R.id.tvHospitalName);
        TextView wardNo = view.findViewById(R.id.tvWardName);
        TextView departmentName = view.findViewById(R.id.tvDepartmentName);
        TextView issues = view.findViewById(R.id.issues);
        TextView remark = view.findViewById(R.id.remarksValue);
        TextView remarkTitle = view.findViewById(R.id.tvRemarks);
        Button closeTicket = view.findViewById(R.id.closeTicket);
        EditText personName = view.findViewById(R.id.etName);
        EditText uid = view.findViewById(R.id.uidDetail);
        TextInputLayout nameLayout = view.findViewById(R.id.nameLayout);
        TextInputLayout uidLayout = view.findViewById(R.id.etUID);


        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);

        tvTicketNumber.setText("Ticket No :" + data.getServiceRequestSerialNo());
        name.setText(data.getServiceRequestName());
        contactNo.setText(data.getServiceRequestContactNo());
        email.setText(data.getServiceRequestEmail());
        hospitalName.setText(data.getServiceRequestHospitalName());
        wardNo.setText(data.getServiceRequestWardNo());
        departmentName.setText(data.getServiceRequestDepartment());
        issues.setText(data.getServiceRequestMessage());
        remark.setText(data.getServiceEngName());

        if (data.getTicketStatus().equals("Open")){
            closeTicket.setVisibility(View.VISIBLE);
            uidLayout.setVisibility(View.VISIBLE);
            nameLayout.setVisibility(View.VISIBLE);
            remark.setVisibility(View.GONE);
            remarkTitle.setVisibility(View.GONE);
        }else{
            closeTicket.setVisibility(View.GONE);
            uidLayout.setVisibility(View.GONE);
            nameLayout.setVisibility(View.GONE);
            remark.setVisibility(View.VISIBLE);
            remarkTitle.setVisibility(View.VISIBLE);
        }

        setTicketViewDialog(dialog,true);

        cross_img.setOnClickListener(v -> {
            dialog.dismiss();
        });

        closeTicket.setOnClickListener(v -> {
            if(personName.length()!= 0){
                issueClose.issueClose(data.getServiceRequest_id(),data.getServiceRequestContactNo(),personName.getText().toString(),uid.getText().toString());
                dialog.dismiss();
            }else{
                TastyToasty.orange(
                        ctx,
                        "Please fill the details to close ticket",
                        R.drawable.ic_info_small
                ).show();
            }

        });

        dialog.show();


        return dialog;
    }

    private static void setTicketViewDialog(AlertDialog dialogView, boolean status) {
        DialogBoxFactory.dialogView = dialogView;
        // to make the window background transparent
        if (dialogView.getWindow() != null)
            dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Objects.requireNonNull(dialogView.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        if (Objects.requireNonNull(dialogView.getWindow()).getAttributes() != null) {
            WindowManager.LayoutParams wmlp = dialogView.getWindow().getAttributes();
            if (status) {
                wmlp.gravity = Gravity.BOTTOM | Gravity.START;
            } else {
                wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER;
            }

            wmlp.height = 300;
            wmlp.width = 100;
            wmlp.dimAmount = 0.7F;
            wmlp.screenBrightness = 10.0F;
            wmlp.x = 500;//x position
            wmlp.y = 325;//y position
            dialogView.getWindow().setAttributes(wmlp);
        }
    }


    private static void InfoResetTextWatchers(EditText etOtp1, EditText etOtp2, EditText etOtp3, EditText etOtp4) {

        etOtp1.addTextChangedListener(new GenericTextWatcher(etOtp1, etOtp2));
        etOtp2.addTextChangedListener(new GenericTextWatcher(etOtp2, etOtp3));
        etOtp3.addTextChangedListener(new GenericTextWatcher(etOtp3, etOtp4));
        etOtp4.addTextChangedListener(new GenericTextWatcher(etOtp4, null));

        etOtp1.addTextChangedListener(new GenericKeyEvent(etOtp1, null));
        etOtp2.addTextChangedListener(new GenericKeyEvent(etOtp2, etOtp1));
        etOtp3.addTextChangedListener(new GenericKeyEvent(etOtp3, etOtp2));
        etOtp4.addTextChangedListener(new GenericKeyEvent(etOtp4, etOtp3));

    }

    public static AlertDialog showSettingsSaved(Context ctx, boolean isAccepted) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_settings_response, null, false);
        CheckView cv = view.findViewById(R.id.cvTick);
        ImageView ivCross = view.findViewById(R.id.ivCross);
        TextView tvMsg = view.findViewById(R.id.tvMsg);
        TextView tvMsgDesc = view.findViewById(R.id.tvMsgDesc);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(true);

        dialog.setOnShowListener(d -> {
            if (isAccepted) {
                tvMsg.setText("Success !");
                tvMsgDesc.setText("Settings saved");
                ivCross.setVisibility(View.GONE);
                cv.setVisibility(View.VISIBLE);
                cv.check();
            } else {
                tvMsg.setText("Failure !");
                tvMsgDesc.setText("Error in saving settings");
                cv.setVisibility(View.GONE);
                ivCross.setVisibility(View.VISIBLE);

            }

        });

        // to make the window background transparent
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        return dialog;
    }


    public static AlertDialog selectModeConfirmationDialog(Activity ctx, String message, VentMode newMode, OnModeChangeListener onModeChangeListener) {
        return selectModeConfirmationDialog(ctx, message, newMode, onModeChangeListener, null);

    }

    public static AlertDialog startVentConfirmationDialog(Activity ctx, String message) {
        return startVentConfirmationDialog(ctx, message);
    }


    //For progress standby
    public static void showStandbyProgress(Context ctx, String desc) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.standby_progress_layout, null, false);
        TextView tvMsg = view.findViewById(R.id.tvProgressDesc);
        ImageView crossImage = view.findViewById(R.id.cross_image);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();

        tvMsg.setText(desc);
        dialog.setCancelable(false);

        crossImage.setOnClickListener(v -> {
            dialog.cancel();
        });

        dialog.show();

        setNeonateDialogView(dialog);
    }

    public static void showNeonateSensorDialog(Context ctx, String desc) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.neonate_sensor_dialog_layout, null, false);
        TextView tvMsg = view.findViewById(R.id.tvNeonateDesc);
        Button btnOk = view.findViewById(R.id.btnNeonate);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();

        tvMsg.setText(desc);
        dialog.setCancelable(false);

        btnOk.setOnClickListener(v -> {
            dialog.cancel();
        });

        dialog.show();

        setNeonateDialogView(dialog);
    }


    // created by masoom on 13 jan 2023
    public static void setNeonateDialogView(AlertDialog dialogView) {
        DialogBoxFactory.dialogView = dialogView;

        dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Objects.requireNonNull(dialogView.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogView.getWindow().getAttributes());
        lp.width = 400;
        lp.height = 420;
        lp.y = 1000;
        lp.x = 0;

        dialogView.getWindow().setAttributes(lp);
        // to make the window background transparent

        Log.d("dialog", String.valueOf(dialogView.getWindow().getAttributes().width));
        Log.d("dialog", String.valueOf(dialogView.getWindow().getAttributes().height));
        Log.d("dialog", String.valueOf(dialogView.getWindow().getAttributes().gravity));
    }

    // created by masoom on 6 march 2023
    public static void setLimitReachedDialogView(AlertDialog dialogView) {
        DialogBoxFactory.dialogView = dialogView;

        dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Objects.requireNonNull(dialogView.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialogView.getWindow().getAttributes());

        lp.x = 600;
        lp.y = 102;
        lp.dimAmount = 0.0F;
        lp.screenBrightness = 5.0F;
        lp.width = 675;
        lp.height = 100;

        dialogView.getWindow().setAttributes(lp);
    }


    public static AlertDialog selectModeConfirmationDialog(Activity ctx, String message, VentMode newMode, OnModeChangeListener onModeChangeListener, SimpleCallbackListener onCancelListener) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_mode_confirmation, null, false);
        TextView tvConfMsg = view.findViewById(R.id.tvConfMessage);

        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);
        tvConfMsg.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();

        btnYes.setOnClickListener(v -> {
            onModeChangeListener.onModeChange(newMode);
            dialog.cancel();
        });
        btnNo.setOnClickListener((v) -> {
            if (onCancelListener != null) onCancelListener.doAction();
            dialog.cancel();
        });
        // to make the window background transparent
        final Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setCallback(new UserInteractionAwareCallback(window.getCallback(), ctx));
        }
        dialog.setCancelable(false);
        setDialogView(dialog, true);

        dialog.show();
        return dialog;
    }

    public static AlertDialog showCommandDialog(Context ctx, SingleValueCallbackListener onSendListener) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_command, null, false);
        EditText etCommand = view.findViewById(R.id.etCmd);
        ImageButton btnSend = view.findViewById(R.id.btnSend);
        Button btnKillAll = view.findViewById(R.id.btnSuspendAll);
        Button btnFastBoot = view.findViewById(R.id.btnFastBoot);
        Button btnSelfTest = view.findViewById(R.id.btnSelfTest);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(true);

        btnSend.setOnClickListener(v -> {
            if (onSendListener != null && !TextUtils.isEmpty(etCommand.getText()))
                onSendListener.doAction(etCommand.getText().toString());
            dialog.cancel();
        });

        btnKillAll.setOnClickListener(v -> {
            if (onSendListener != null)
                onSendListener.doAction(ctx.getResources().getString(R.string.cmd_vent_killall));
            dialog.cancel();
        });

        btnFastBoot.setOnClickListener(v -> {
            if (onSendListener != null)
                onSendListener.doAction(ctx.getResources().getString(R.string.cmd_vent_fastboot));
            dialog.cancel();
        });

        btnSelfTest.setOnClickListener(v -> {
            if (onSendListener != null)
                onSendListener.doAction(ctx.getResources().getString(R.string.cmd_vent_selftest));
            dialog.cancel();
        });


        setDialogView(dialog, true);

        dialog.show();

        return dialog;
    }


    public static AlertDialog showTwoBtnDialog(Context ctx, String title, String message, SimpleCallbackListener onSuccessListener) {
        return showTwoBtnDialog(ctx, title, message, onSuccessListener, null);

    }


    public static AlertDialog showTwoBtnDialog(Context ctx, String title, String message, SimpleCallbackListener onSuccessListener, SimpleCallbackListener onCancelListener) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_two_btn_design, null, false);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMsg = view.findViewById(R.id.tvMessage);
        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);


        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);

        tvTitle.setText(title);
        tvMsg.setText(message);
        btnYes.setOnClickListener((v) -> {
            if (onSuccessListener != null) onSuccessListener.doAction();
            dialog.cancel();
        });
        btnNo.setOnClickListener((v) -> {
            if (onCancelListener != null) onCancelListener.doAction();
            dialog.cancel();
        });

        setDialogView(dialog, true);

        dialog.show();
        return dialog;
    }

    public static AlertDialog showO2CalibrateDialog(Context ctx, SimpleCallbackListener onAccept) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_o2_calibrate, null, false);
        View viewStep1 = view.findViewById(R.id.viewStep1);
        View viewStep2 = view.findViewById(R.id.viewStep2);
        View viewStep3 = view.findViewById(R.id.viewStep3);

        View.OnClickListener listener = (v) -> ((CheckBox) view.findViewWithTag("step" + v.getTag().toString())).setChecked(true);

        viewStep1.setOnClickListener(listener);
        viewStep2.setOnClickListener(listener);
        viewStep3.setOnClickListener(listener);

        ArrayList<CheckBox> checkBoxes = new ArrayList<>();

        CheckBox cbStep1 = view.findViewById(R.id.cbStep1);

        checkBoxes.add(cbStep1);

        CheckBox cbStep2 = view.findViewById(R.id.cbStep2);
        checkBoxes.add(cbStep2);

        CheckBox cbStep3 = view.findViewById(R.id.cbStep3);
        checkBoxes.add(cbStep3);

        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);
        View mainLayout = view.findViewById(R.id.layoutMain);
        View progressLayout = view.findViewById(R.id.layoutProgress);

        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(true);

        btnYes.setOnClickListener(v -> {

            boolean isAllClicked = true;

            for (CheckBox cb : checkBoxes) {
                if (!cb.isChecked()) {
                    isAllClicked = false;
                    break;
                }
            }

            if (isAllClicked) {
                dialog.setCancelable(false);


                onAccept.doAction();

                if (progressLayout.getVisibility() != View.VISIBLE) {
                    mainLayout.setVisibility(View.GONE);
                    progressLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        btnNo.setOnClickListener(v -> dialog.cancel());
        setDialogView(dialog, false);

        //setDialogViewMainActivity(dialog, false);

        dialog.show();

        return dialog;

    }


    private static void setShutDownDialogView(AlertDialog dialogView, boolean status) {
        DialogBoxFactory.dialogView = dialogView;
        // to make the window background transparent
        if (dialogView.getWindow() != null)
            dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Objects.requireNonNull(dialogView.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        if (Objects.requireNonNull(dialogView.getWindow()).getAttributes() != null) {
            WindowManager.LayoutParams wmlp = dialogView.getWindow().getAttributes();
            if (status) {
                wmlp.gravity = Gravity.BOTTOM | Gravity.START;
            } else {
                wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER;
            }

            wmlp.height = 300;
            wmlp.width = 200;
            wmlp.dimAmount = 0.7F;
            wmlp.screenBrightness = 10.0F;
            wmlp.x = 500;//x position
            wmlp.y = 325;//y position
            dialogView.getWindow().setAttributes(wmlp);
        }
    }

    static int i;
    static int timer;
    static int delay;

    public static AlertDialog showTubeDialog(String msg, Context ctx, SimpleCallbackListener onclickShutDown) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_calibration, null, false);

        Button btnCalibration = view.findViewById(R.id.btnCalibration);
        Button btnCancelCalibration = view.findViewById(R.id.btnCancelCalibration);
        CardView cvLayout = view.findViewById(R.id.cardViewLayout);
        TextView textView = view.findViewById(R.id.tv);
        ProgressBar pbar = view.findViewById(R.id.PROG);
        TextView dialogMessage = view.findViewById(R.id.etCmd);
        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);
        i = 0;
        timer = 80;

        cvLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                btnCancelCalibration.setVisibility(View.VISIBLE);
                return true;
            }
        });

        btnCalibration.setOnClickListener(v -> {
            if (onclickShutDown != null) onclickShutDown.doAction();
            dialog.cancel();
        });


        btnCancelCalibration.setOnClickListener(v -> dialog.cancel());

        setShutDownDialogView(dialog, true);

        dialog.show();

        new Thread(new Runnable() {
            public void run() {
                while (i < timer) {
                    i += 1;
                    handler.post(new Runnable() {
                                     public void run() {
                                         pbar.setProgress(i);
                                         textView.setText(i + " / " + pbar.getMax() + " %");
                                         if (i == timer) textView.setText("it is taking longer than usual...");
                                     }
                                 }
                    );
                    try {
                        // Sleep for 50 ms to show progress you can change it as well.
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        return dialog;
    }

    public static AlertDialog showO2RegulationStatusDialog(String msg, Context ctx, SimpleCallbackListener onclickShutDown) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_calibration_regulate, null, false);

        Button btnCalibration = view.findViewById(R.id.btnCalibration);
        Button btnCancelCalibration = view.findViewById(R.id.btnCancelCalibration);
        TextView textView = view.findViewById(R.id.tv);
        ProgressBar pbar = view.findViewById(R.id.PROG);
        TextView dialogMessage = view.findViewById(R.id.etCmd);
        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);


        btnCalibration.setOnClickListener(v -> {
            if (onclickShutDown != null) onclickShutDown.doAction();
            dialog.cancel();
        });


        btnCancelCalibration.setOnClickListener(v -> dialog.cancel());

        setShutDownDialogView(dialog, true);

        dialog.show();

        return dialog;
    }

    public static AlertDialog showshutScreenDialog(Context ctx) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_shut_screen, null, false);

        Button btnCalibration = view.findViewById(R.id.btnCalibration);
        Button btnCancelCalibration = view.findViewById(R.id.btnCancelCalibration);

        ProgressBar pbar = view.findViewById(R.id.PROG);
        TextView dialogMessage = view.findViewById(R.id.etCmd);
//        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);


//        btnCancelCalibration.setOnClickListener(v -> dialog.cancel());

        setShutDownDialogView(dialog, true);

        dialog.show();


        return dialog;
    }

    public static AlertDialog showStartupCheckDialog(String msg, Context ctx, String value) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_startup_check, null, false);

        CardView cvLayout = view.findViewById(R.id.cardViewLayout);
        LinearLayout turbineLayout = view.findViewById(R.id.turbineLayout);
        TextView dialogMessage = view.findViewById(R.id.etCmd);
        TextView tvInspiratorFlow = view.findViewById(R.id.tvInspiratoryInfo);
        TextView tvExpiratorFlow = view.findViewById(R.id.tvExpiratoryInfo);
        TextView tvInspiratorPressure = view.findViewById(R.id.tvInspiratoryPressureInfo);
        TextView tvO2Pressure = view.findViewById(R.id.tvO2PressureSensorInfo);
        TextView tvO2SensorInfo = view.findViewById(R.id.tvO2SensorInfo);
        TextView tvNeo = view.findViewById(R.id.tvNeoInfo);
        ImageView ivInspiratorFlow = view.findViewById(R.id.ivInspiratory);
        ImageView ivExpiratorFlow = view.findViewById(R.id.ivExpiratory);
        ImageView ivInspiratorPressure = view.findViewById(R.id.ivInspiratoryPressure);
        ImageView ivO2Pressure = view.findViewById(R.id.ivO2Pressure);
        ImageView ivO2Sensor = view.findViewById(R.id.ivO2Sensor);
        ImageView ivNeo = view.findViewById(R.id.ivNeo);
        ImageView closeView = view.findViewById(R.id.iv_cross);
        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);
        // conditions
        ArrayList<String> list = new ArrayList(Arrays.asList(value.split(",")));
        Log.i("list_sizeswd",String.valueOf(list.get(1)) + ", " + String.valueOf(list.get(5)));

        if (list.size() >= 12){
            list.set(11,"1");
            try{
                if (Float.parseFloat(list.get(1)) < 3.0f && Float.parseFloat(list.get(5)) > 3.0f) turbineLayout.setVisibility(View.GONE);
                else if (Float.parseFloat(list.get(1)) > 3.0f && Float.parseFloat(list.get(5)) < 3.0f) turbineLayout.setVisibility(View.GONE);
                else if (Float.parseFloat(list.get(1)) > 3.0f && Float.parseFloat(list.get(5)) > 3.0f) turbineLayout.setVisibility(View.GONE);
                else turbineLayout.setVisibility(View.VISIBLE);
            }catch (Exception e){
                e.printStackTrace();
            }

            if (Objects.equals(list.get(8), "1")){
                ivInspiratorFlow.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_green_circle_tick));
                tvInspiratorFlow.setText("Expiratory Flow Sensor Pass");
            }
            else {
                ivInspiratorFlow.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_red_cross));
                tvInspiratorFlow.setText("Expiratory Flow Sensor Failed");
            }

            if (Objects.equals(list.get(7), "1")){
                ivExpiratorFlow.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_green_circle_tick));
                tvExpiratorFlow.setText("Inspiratory Flow Sensor Pass");
            }

            else {
                ivExpiratorFlow.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_red_cross));
                tvExpiratorFlow.setText("Inspiratory Flow Sensor Failed");
            }

            if (Objects.equals(list.get(9), "1")){
                ivInspiratorPressure.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_green_circle_tick));
                tvInspiratorPressure.setText("Inspiratory Pressure Sensor Pass");
            }
            else {
                ivInspiratorPressure.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_red_cross));
                tvInspiratorPressure.setText("Inspiratory Pressure Sensor Failed");
            }

            if (Objects.equals(list.get(10), "1")){
                ivO2Pressure.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_green_circle_tick));
                tvO2Pressure.setText("O2 Pressure Sensor Pass");
            }
            else {
                ivO2Pressure.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_red_cross));
                tvO2Pressure.setText("O2 Pressure Sensor Failed");
            }

            if (Objects.equals(list.get(11), "1")){
                ivO2Sensor.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_green_circle_tick));
                tvO2SensorInfo.setText("O2 Sensor Pass");
            }
            else {
                ivO2Sensor.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_red_cross));
                tvO2SensorInfo.setText("O2 Sensor Failed");
            }

            if (Objects.equals(list.get(12), "1")){
                ivNeo.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_green_circle_tick));
                tvNeo.setText("Neo Sensor Pass");
            }
            else {
                ivNeo.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_red_cross));
                tvNeo.setText("Neo Sensor Failed");
            }
        }

        closeView.setOnClickListener(v -> {
            dialog.cancel();
        });


        setShutDownDialogView(dialog, true);

        dialog.show();

        return dialog;
    }


    public static AlertDialog showRestartStatusDialog(Context ctx) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_restart, null, false);

        Button btnCancelCalibration = view.findViewById(R.id.btnCancelCalibration);
        GridLayout cancelLayout = view.findViewById(R.id.cancelLayout);
        CardView cvLayout = view.findViewById(R.id.cardViewLayout);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);

        cvLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                cancelLayout.setVisibility(View.VISIBLE);
                return true;
            }
        });

        btnCancelCalibration.setOnClickListener(v -> dialog.cancel());

        setShutDownDialogView(dialog, true);

        dialog.show();

        return dialog;
    }


    public static AlertDialog showCalibrationStatusDialog(String msg, Context ctx, SimpleCallbackListener onclickShutDown) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_calibration, null, false);

        Button btnCalibration = view.findViewById(R.id.btnCalibration);
        Button btnCancelCalibration = view.findViewById(R.id.btnCancelCalibration);
        CardView cvLayout = view.findViewById(R.id.cardViewLayout);
        TextView textView = view.findViewById(R.id.tv);
        ProgressBar pbar = view.findViewById(R.id.PROG);
        TextView dialogMessage = view.findViewById(R.id.etCmd);
        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);
        i = 0;
        timer = 80;

        cvLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                btnCancelCalibration.setVisibility(View.VISIBLE);
                return true;
            }
        });

        btnCalibration.setOnClickListener(v -> {
            if (onclickShutDown != null) onclickShutDown.doAction();
            dialog.cancel();
        });


        btnCancelCalibration.setOnClickListener(v -> dialog.cancel());

        setShutDownDialogView(dialog, true);

        dialog.show();

        new Thread(new Runnable() {
            public void run() {
                while (i < timer) {
                    i += 1;
                    handler.post(new Runnable() {
                                     public void run() {
                                         pbar.setProgress(i);
                                         textView.setText(i + " / " + pbar.getMax() + " %");
                                         if (i == timer) textView.setText("it is taking longer than usual...");
                                     }
                                 }
                    );
                    try {
                        // Sleep for 50 ms to show progress you can change it as well.
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        return dialog;
    }


    public static AlertDialog showShutDownStatusDialog(String msg, Context ctx) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layoutdialog_shutdown_status, null, false);

        TextView dialogMessage = view.findViewById(R.id.etCmd);
        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(true);

        setShutDownDialogView(dialog, true);

        dialog.show();

        return dialog;
    }

    public static AlertDialog showBatteryFailureStatusDialog(Context ctx, String msg) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_battery_failure, null, false);

        Button btnCancel = view.findViewById(R.id.btnCancel);

        TextView dialogMessage = view.findViewById(R.id.etCmd);
        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(true);

        btnCancel.setOnClickListener(v -> dialog.cancel());

        setShutDownDialogView(dialog, true);

        dialog.show();

        return dialog;
    }

    public static AlertDialog showNeoSensorFailureWarning(Context ctx) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.neo_sensor_warning, null, false);

//        Button btnStandby = view.findViewById(R.id.btnStandby);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        TextView dialogMessage = view.findViewById(R.id.etCmd);
//        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(true);


//        btnStandby.setOnClickListener(v -> {
//            if (onclickStandby != null) onclickStandby.doAction();
//            dialog.cancel();
//        });

        btnCancel.setOnClickListener(v -> dialog.cancel());

        setShutDownDialogView(dialog, true);

        dialog.show();

        return dialog;
    }


    public static AlertDialog showBatteryCriticallyLowStatusDialog(String msg, Context ctx) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_battery_critical, null, false);

//        Button btnStandby = view.findViewById(R.id.btnStandby);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        TextView dialogMessage = view.findViewById(R.id.etCmd);
        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(true);

        btnCancel.setOnClickListener(v -> dialog.cancel());

        setShutDownDialogView(dialog, true);



        dialog.show();

        return dialog;
    }

    public static AlertDialog showVentilationStatusDialog(String msg, Context ctx, DischargeListener onDischarge,SimpleCallbackListener onclickStandby) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_vent_status, null, false);

        Button btnStandby = view.findViewById(R.id.btnStandby);
        ImageView ivCancel = view.findViewById(R.id.ivCancel);
        Button btnDischarge = view.findViewById(R.id.btnDischarge);

        TextView dialogMessage = view.findViewById(R.id.etCmd);
        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(true);

        btnStandby.setOnClickListener(v -> {
            if (onclickStandby != null) onclickStandby.doAction();
            dialog.cancel();

        });

        btnDischarge.setOnClickListener(v -> {
            if (onDischarge != null) onDischarge.onDischarge();
            dialog.cancel();
        });

        ivCancel.setOnClickListener(v ->{
            dialog.cancel();
        });

        setShutDownDialogView(dialog, true);

        dialog.show();

        return dialog;
    }

    public static AlertDialog showCalibrationErrorDialog(String msg, Context ctx, SimpleCallbackListener onclickStandby) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_calibration_error, null, false);

        //  Button btnStandby = view.findViewById(R.id.btnStandby);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        TextView dialogMessage = view.findViewById(R.id.etCmd);
        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(true);

        btnCancel.setOnClickListener(v -> dialog.cancel());

        setShutDownDialogView(dialog, true);

        dialog.show();

        return dialog;
    }

    public static AlertDialog showO2RegulationCailbration(String msg, Context ctx, O2RegulationCheckViewModel viewModel) {

        View view = LayoutInflater.from(ctx).inflate(R.layout.layout_dialog_calibration_error, null, false);

        //  Button btnStandby = view.findViewById(R.id.btnStandby);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        TextView dialogMessage = view.findViewById(R.id.etCmd);
        dialogMessage.setText(msg);
        AlertDialog dialog = new AlertDialog.Builder(ctx).setView(view).create();
        dialog.setCancelable(false);

        btnCancel.setOnClickListener(v -> dialog.cancel());

        setShutDownDialogView(dialog, true);

        dialog.show();

        return dialog;
    }


    public static void setDialogView(AlertDialog dialogView, boolean status) {
        DialogBoxFactory.dialogView = dialogView;
        // to make the window background transparent
        if (dialogView.getWindow() != null)
            dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Objects.requireNonNull(dialogView.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        if (Objects.requireNonNull(dialogView.getWindow()).getAttributes() != null) {
            WindowManager.LayoutParams wmlp = dialogView.getWindow().getAttributes();
            if (status) {
                wmlp.gravity = Gravity.BOTTOM | Gravity.START;
            } else {
                wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER;
            }

            wmlp.height = 300;
            wmlp.width = 200;
            wmlp.dimAmount = 0.7F;
            wmlp.screenBrightness = 10.0F;
            wmlp.x = 250;//x position
            wmlp.y = 125;//y position
            dialogView.getWindow().setAttributes(wmlp);
        }


    }


    public static void setDialogViewMainActivity(AlertDialog dialogView, boolean status) {
        DialogBoxFactory.dialogView = dialogView;
        // to make the window background transparent
        if (dialogView.getWindow() != null)
            dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Objects.requireNonNull(dialogView.getWindow()).getAttributes() != null) {
            WindowManager.LayoutParams wmlp = dialogView.getWindow().getAttributes();
            if (status) {
                wmlp.gravity = Gravity.BOTTOM | Gravity.START;
            } else {
                wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER;
            }

            wmlp.height = 300;
            wmlp.width = 200;
            wmlp.dimAmount = 0.0F;
            wmlp.screenBrightness = 5.0F;
            wmlp.x = 270;   //x position
            wmlp.y = 125;   //y position
            dialogView.getWindow().setAttributes(wmlp);
        }

    }


    public static void setDialogViewMainSystemDialog(AlertDialog dialogView, boolean status) {
        DialogBoxFactory.dialogView = dialogView;
        // to make the window background transparent
        if (dialogView.getWindow() != null)
            dialogView.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        if (Objects.requireNonNull(dialogView.getWindow()).getAttributes() != null) {
            WindowManager.LayoutParams wmlp = dialogView.getWindow().getAttributes();
            if (status) {
                wmlp.gravity = Gravity.BOTTOM | Gravity.START;
            } else {
                wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER;
            }

            wmlp.height = 300;
            wmlp.width = 200;
            wmlp.dimAmount = 0.0F;
            wmlp.screenBrightness = 5.0F;
            wmlp.x = 260;   //x position
            wmlp.y = 105;   //y position
            dialogView.getWindow().setAttributes(wmlp);
        }

    }


}