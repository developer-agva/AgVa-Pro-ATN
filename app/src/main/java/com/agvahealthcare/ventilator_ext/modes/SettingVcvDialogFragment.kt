package com.agvahealthcare.ventilator_ext.modes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.fragment_vcv_setting_dialog.*

class SettingVcvDialogFragment : GraphicTooltipFragment("SettingVcv") {

    private var preferenceManager: PreferenceManager? = null
    private var closeListener: OnDismissDialogListener? = null

    companion object {


        var fragment: SettingVcvDialogFragment? = null
        fun newInstance(
            closeListener: OnDismissDialogListener?,
        ): SettingVcvDialogFragment {
            if(fragment == null) {
                fragment = SettingVcvDialogFragment()
                fragment?.closeListener = closeListener
            }
            return fragment!!
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_vcv_setting_dialog, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        //setHeightWidth()

        imageViewCross.setOnClickListener {
            // listener which closes both knob dialog and this dialog
            closeListener?.handleDialogClose()
        }
        setDataOnViewViaPreferences()
    }

    override fun setDataOnViewViaPreferences() {

        preferenceManager?.apply {

            val setTot = Configs.calculateTtot(readRR().toInt())
            textViewTtot.text = "${getString(R.string.titot)} = $setTot"

            textViewTinsp.text = "${getString(R.string.ti)} = ${readTinsp()} ${getString(R.string.hint_s)}"

            val setTexp = Configs.calculateTexp(readRR().toInt(),readTinsp() )
            textViewTexp.text = "${getString(R.string.te)} = $setTexp ${getString(R.string.hint_s)}"

            textViewTi.text = "${getString(R.string.ti)}"

            textViewTp.text = "${getString(R.string.tplat)}"

            textViewVPeak.text =  "${getString(R.string.vpeak)}"

            textViewVtSigh.text = "${getString(R.string.vt)} = ${readVti().toInt()} ${getString(R.string.hint_ml)}"

        }

    }

    override fun updateDataOnView(parameter: ControlParameterModel) {

        preferenceManager?.apply {
            when(parameter.ventKey){
                Configs.LBL_TINSP -> {
                    try {
                        val setTexp = Configs.calculateTexp(readRR().toInt(), parameter.reading.toFloat())
                        textViewTexp.text = "${getString(R.string.te)} = $setTexp ${getString(R.string.hint_s)}"
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                    textViewTinsp.text =  "${getString(R.string.ti)} = ${String.format("%.1f", parameter.reading.toFloat())} ${getString(R.string.hint_s)}"
                }

                Configs.LBL_RR -> {
                    try {
                        val setTot = Configs.calculateTtot(parameter.reading.toInt())
                        textViewTtot.text = "${getString(R.string.titot)} = $setTot"

                        val setTexp = Configs.calculateTexp(parameter.reading.toInt(),readTinsp() )
                        textViewTexp.text = "${getString(R.string.te)} = $setTexp ${getString(R.string.hint_s)}"
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }


                Configs.LBL_VTI -> {
                    textViewVtSigh.text = "${getString(R.string.vt)} = ${parameter.reading.toInt()} ${getString(R.string.hint_ml)}"
                }
            }

            textViewTi.text = "${getString(R.string.ti)}"
            textViewTp.text = "${getString(R.string.tplat)}"
            textViewVPeak.text =  "${getString(R.string.vpeak)}"
        }

    }
}