package com.agvahealthcare.ventilator_ext.limits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.utility.setHeightWidthPercent
import kotlinx.android.synthetic.main.fragment_limit_dialog.*

class LimitDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "LimitDialog"
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"

        fun newInstance(height: Int?, width: Int?,closeListener : OnDismissDialogListener?): LimitDialogFragment {
            val args = Bundle()
            height?.let { args.putInt(KEY_HEIGHT, it) }
            width?.let { args.putInt(KEY_WIDTH, it) }
            val fragment = LimitDialogFragment()
            fragment.arguments = args
            fragment.closeListener = closeListener
            return fragment
        }
    }
    private var closeListener: OnDismissDialogListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_limit_dialog, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)
        setUpData()
        setupClickListener()

    }

    private fun setupClickListener() {

        imageViewCross.setOnClickListener {
            closeListener?.handleDialogClose()
            dismiss()
        }

    }

    private fun setUpData() {


    }

    override fun onStart() {
        super.onStart()
        val heightDialog = arguments?.getInt(KEY_HEIGHT)
        val widthDialog = arguments?.getInt(KEY_WIDTH)

        setHeightWidthPercent(heightDialog , widthDialog , true)
    }

}