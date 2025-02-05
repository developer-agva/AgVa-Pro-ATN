package com.agvahealthcare.ventilator_ext.modes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*
import kotlinx.android.synthetic.main.fragment_simv_setting_dialog.*

@Deprecated("Deprecated. Use SettingsPcvDialogFragment")
class SettingSimvDialogFragment : GraphicTooltipFragment("SettingSimv") {

    private var preferenceManager: PreferenceManager? = null
    private var closeListener: OnDismissDialogListener? = null
    companion object {

        var fragment: SettingSimvDialogFragment? = null
        fun newInstance(
            closeListener: OnDismissDialogListener?,
        ): SettingSimvDialogFragment {


            if(fragment == null){
                fragment = SettingSimvDialogFragment()
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
        return inflater.inflate(R.layout.fragment_simv_setting_dialog, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        //setHeightWidth()
        setDataOnViewViaPreferences()
        imageViewCross.setOnClickListener {
            // listener which closes both knob dialog and this dialog
            closeListener?.handleDialogClose()
        }


    }

    override fun setDataOnViewViaPreferences() {

        preferenceManager?.apply {
            textViewTi.text =  "${getString(R.string.ti)} = ${readTinsp()} ${getString(R.string.hint_s)}"

            textViewTiMax.text = "${getString(R.string.ti_max)} = ${readTinsp()} ${getString(R.string.hint_s)}"

            val setTot = calculateTtot(readRR().toInt())
            textViewTtot.text = "${getString(R.string.titot)} = $setTot"

            textViewPs.text = "${getString(R.string.ps)} = ${readPplat().toInt()} ${getString(R.string.hint_cmH2o)}"

            textViewPeepPs.text = "${getString(R.string.peep_plus_ps)} = ${readPEEP().toInt() + readPplat().toInt()} ${getString(R.string.hint_cmH2o)}"

            textViewVpeak.text = "${getString(R.string.vpeak)}"

            textViewTrigE.text =  "${getString(R.string.trig_exp)}"


        }

    }


     override fun updateDataOnView(parameter: ControlParameterModel) {

        preferenceManager?.apply {
            when(parameter.ventKey){
                LBL_TINSP -> {
                    textViewTi.text =  "${getString(R.string.ti)} = ${String.format("%.1f", parameter.reading.toFloat())} ${getString(R.string.hint_s)}"
                    textViewTiMax.text = "${getString(R.string.ti_max)} = ${String.format("%.1f", parameter.reading.toFloat())} ${getString(R.string.hint_s)}"
                }

                LBL_RR -> {
                    try {
                        val setTot = calculateTtot(parameter.reading.toInt())
                        textViewTtot.text = "${getString(R.string.titot)} = $setTot"
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                }

                LBL_PPLAT -> {
                    textViewPs.text = "${getString(R.string.ps)} = ${parameter.reading.toInt()} ${getString(R.string.hint_cmH2o)}"
                    textViewPeepPs.text = "${getString(R.string.peep_plus_ps)} = ${readPEEP().toInt() + parameter.reading.toInt()} ${getString(R.string.hint_cmH2o)}"
                }

                LBL_PEEP -> {
                    textViewPeepPs.text = "${getString(R.string.peep_plus_ps)} = ${parameter.reading.toInt() + readPplat().toInt()} ${getString(R.string.hint_cmH2o)}"
                }
            }



            textViewVpeak.text = "${getString(R.string.vpeak)}"
            textViewTrigE.text =  "${getString(R.string.trig_exp)}"


        }

    }
}