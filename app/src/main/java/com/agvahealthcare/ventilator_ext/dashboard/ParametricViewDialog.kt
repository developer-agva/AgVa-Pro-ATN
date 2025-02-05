package com.agvahealthcare.ventilator_ext.dashboard

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.agvahealthcare.ventilator_ext.R
import kotlinx.android.synthetic.main.dialog_parameteric_view.*


class ParametricViewDialog : DialogFragment() {

    companion object {
        const val TAG = "ParametricViewDialog"

        fun newInstance(): ParametricViewDialog {
            val fragment = ParametricViewDialog()
//            fragment.listener = clickListener
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.dialog_parameteric_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPicker()
    }

    private fun setUpPicker() {

        // Set string values
        val data = arrayOf("RR", "PEEP", "Ppeak", "VTE", "FiO2")

        numberPicker.minValue = 1
        numberPicker.maxValue = data.size
        numberPicker.displayedValues = data
        // Set fading edge enabled
        numberPicker.isFadingEdgeEnabled = true
        // Set scroller enabled
        numberPicker.isScrollerEnabled = true
        // Set wrap selector wheel
        numberPicker.wrapSelectorWheel = true
        // Set accessibility description enabled
        numberPicker.isAccessibilityDescriptionEnabled = true


    }

    override fun onStart() {
        super.onStart()

        val width = resources.getDimensionPixelSize(R.dimen.popup_width)
        val height = resources.getDimensionPixelSize(R.dimen.popup_height)
        dialog?.window?.setLayout(width, height)
        dialog?.window?.setGravity(Gravity.TOP or Gravity.END)

    }

}