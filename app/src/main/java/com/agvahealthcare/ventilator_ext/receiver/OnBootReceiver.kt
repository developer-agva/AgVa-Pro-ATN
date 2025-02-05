package com.agvahealthcare.ventilator_ext.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.agvahealthcare.ventilator_ext.SplashActivity

class OnBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.action?.apply {
            // Started new activity
            if (equals(Intent.ACTION_BOOT_COMPLETED)) {
                val i = Intent(context, SplashActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
            }
        }

    }
}