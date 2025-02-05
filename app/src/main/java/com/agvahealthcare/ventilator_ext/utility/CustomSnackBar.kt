package com.agvahealthcare.ventilator_ext.utility

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.agvahealthcare.ventilator_ext.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout


class CustomSnackBar {

    companion object {

        fun showSnackBar(relative: View, msg: String?): View { // Create the SnackBar
            val snackBar = Snackbar.make(relative, "", Snackbar.LENGTH_LONG)

            //inflate view
            val inflater =
                relative.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val snackView: View = inflater.inflate(R.layout.custom_snackbar, null)
            // White background
            snackBar.view.setBackgroundColor(Color.TRANSPARENT)
            val tvMsg = snackView.findViewById<TextView>(R.id.textMsg)
            tvMsg.text = msg
            snackBar.view.setPadding(225, 0, 1020, 0)
            val snackBarView = snackBar.view as SnackbarLayout
            snackBarView.addView(snackView, 0)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            lp.gravity = Gravity.BOTTOM
            lp.topMargin = 877
            snackBarView.layoutParams = lp

            snackBar.apply {

                duration = 2000
                isGestureInsetBottomIgnored = true
                show()
            }

            return snackView
        }
    }


}
