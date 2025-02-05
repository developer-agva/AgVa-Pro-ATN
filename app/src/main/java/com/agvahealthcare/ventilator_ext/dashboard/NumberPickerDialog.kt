package com.agvahealthcare.ventilator_ext.dashboard

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.agvahealthcare.ventilator_ext.R


class NumberPickerDialog : DialogFragment() {

    companion object {
        const val TAG = "NumberPickerDialog"

        fun newInstance(): NumberPickerDialog {
            return NumberPickerDialog()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_number_picker, container, false)

    }

    override fun onStart() {
        super.onStart()

        val width = resources.getDimensionPixelSize(R.dimen.popup_width)
        val height = resources.getDimensionPixelSize(R.dimen.popup_height)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setGravity(Gravity.TOP or Gravity.END)

    }

}