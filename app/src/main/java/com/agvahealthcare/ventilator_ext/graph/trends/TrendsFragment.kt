package com.agvahealthcare.ventilator_ext.graph.trends

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.system.SystemDialogFragment
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_diagnostic_check.*
import kotlinx.android.synthetic.main.fragment_trends.*

class TrendsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_trends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
    }

    private fun setupClickListener() {

        includeButton_1H.buttonView.text = getString(R.string.hint_1_h)
        includeButton_6H.buttonView.text = getString(R.string.hint_6_h)
        includeButton_12H.buttonView.text = getString(R.string.hint_12_h)
        includeButton_24H.buttonView.text = getString(R.string.hint_24_h)
        includeButton_72H.buttonView.text = getString(R.string.hint_72_h)
        includeButtonConfirm.buttonView.text = getString(R.string.hint_confirm)

        includeButtonConfirm.buttonView.setPadding(40,0,40,0)

        includeButton_1H.buttonView.setOnClickListener {

        }

        includeButton_6H.buttonView.setOnClickListener {

        }

        includeButton_12H.buttonView.setOnClickListener {

        }

        includeButton_24H.buttonView.setOnClickListener {

        }

        includeButton_72H.buttonView.setOnClickListener {

        }

        includeButtonConfirm.buttonView.setOnClickListener {

        }
    }



}
