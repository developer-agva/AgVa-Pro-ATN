package com.agvahealthcare.ventilator_ext.custom_dialogs

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.databinding.WaveSelectionGraphLayoutBinding
import com.agvahealthcare.ventilator_ext.system.settings.CommonSetupAdapter
import com.agvahealthcare.ventilator_ext.system.settings.onDropDownSelectionListener
import com.agvahealthcare.ventilator_ext.utility.hideSystemUI

class WaveSelectionDialogFragment(
    private var onClickListener: onDropDownSelectionListener,
    private var onCloseListener: OnDismissDialogListener,
    private var dataList:ArrayList<String>
) : DialogFragment() {
    private lateinit var binding : WaveSelectionGraphLayoutBinding
    private var mAdapter: CommonSetupAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
       binding = WaveSelectionGraphLayoutBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomDialog)

        binding.txtTitle.text = "Module Selection"
        setupAdapter()

        binding.ivCrossWave.setOnClickListener {
            onCloseListener.handleDialogClose()
        }

    }

    private fun setupAdapter() {
        binding.recyclerViewGraphWave.layoutManager = LinearLayoutManager(requireContext())
        mAdapter = CommonSetupAdapter(
            dataList,
            onClickListener
        )
        binding.recyclerViewGraphWave.adapter = mAdapter
    }

    override fun onStart() {
        super.onStart()
        setHeightWidth()
    }

    private fun setHeightWidth() {

        dialog?.window?.apply {
            decorView.apply {
                val params: WindowManager.LayoutParams = attributes
                params.dimAmount = 0.0F
                params.screenBrightness = 5.0F
                params.width = 400
                params.height = 145

                // Set your desired x and y coordinates here
                params.x = -30 // example x coordinate
                params.y = 15 // example y coordinate

                attributes = params
            }
        }
        hideSystemUI()

    }

}




