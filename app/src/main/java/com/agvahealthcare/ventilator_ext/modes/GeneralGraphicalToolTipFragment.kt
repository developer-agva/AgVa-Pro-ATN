package com.agvahealthcare.ventilator_ext.modes

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import com.agvahealthcare.ventilator_ext.BuildConfig
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.utility.VENTILATOR_MODES
import com.agvahealthcare.ventilator_ext.utility.hideSystemUI
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.*
import kotlinx.android.synthetic.main.fragment_pcv_setting_dialog.*
import kotlinx.android.synthetic.main.text_knob_view.*

class GeneralGraphicalToolTipFragment : GraphicTooltipFragment("SettingPcv") {

    private var preferenceManager: PreferenceManager? = null
    private var closeListener: OnDismissDialogListener? = null
    private var modeCode = 0
    private var isStatus: Boolean? = null

    companion object {
        const val GGTTTAG = "SettingPcv"
        var fragment: GeneralGraphicalToolTipFragment? = null
        private const val KEY_HEIGHT = "KEY_HEIGHT"
        private const val KEY_WIDTH = "KEY_WIDTH"
        private const val KEY_STATUS = "KEY_STATUS"

        fun newInstance(
            height: Int?,
            width: Int?,
            mode: Int,
            status: Boolean?,
            closeListener: OnDismissDialogListener?,
        ): GeneralGraphicalToolTipFragment {
            val args = Bundle()
            height?.let { args.putInt(KEY_HEIGHT, it) }
            width?.let { args.putInt(KEY_WIDTH, it) }
            status?.let { args.putBoolean(KEY_STATUS, it) }

            return GeneralGraphicalToolTipFragment().apply {
                this.closeListener = closeListener
                this.modeCode = mode
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_pcv_setting_dialog, container, false)


        return view

    }

    override fun onStart() {
        super.onStart()

        val heightDialog = arguments?.getInt(GeneralGraphicalToolTipFragment.KEY_HEIGHT)
        val widthDialog = arguments?.getInt(GeneralGraphicalToolTipFragment.KEY_WIDTH)
        isStatus = arguments?.getBoolean(GeneralGraphicalToolTipFragment.KEY_STATUS)
        setHeightWidth(heightDialog, widthDialog, isStatus)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        preferenceManager = PreferenceManager(requireContext())
        // setHeightWidth()

        VentilatorApp.globalModeType?.let {
            if (it == Configs.ModeType.TYPE_Volume) textViewPeepPiSign.visibility = View.GONE
            else textViewPeepPiSign.visibility = View.VISIBLE
        } ?: kotlin.run {
            if (preferenceManager?.readModeType() == Configs.ModeType.TYPE_Volume) textViewPeepPiSign.visibility =
                View.GONE
            else textViewPeepPiSign.visibility = View.VISIBLE
        }


        setDataOnViewViaPreferences()
        handleUIAccToCondition()

        checkMode()
        imageViewCross.setOnClickListener {
            closeListener?.handleDialogClose()
        }

    }

    // created at 16 may 2023
    private fun handleUIAccToCondition() {

        preferenceManager?.apply {

            if (modeCode == MODE_PC_PSV) {
                textViewPeepPiSign.visibility = View.GONE
                textViewPi.text = "${getString(R.string.support_pressure)}=${readPplat()} %"
            } else if (modeCode == MODE_VCV_ACV || modeCode == MODE_PC_AC) {
                textViewPi.visibility = View.GONE
            } else if (modeCode == MODE_NIV_CPAP || modeCode == MODE_NIV_NCPAP || modeCode == MODE_NC_CPAP) {
                textViewIERatio.visibility = View.GONE
                textViewTinsp.visibility = View.GONE
                textViewTexp.visibility = View.GONE
                textViewPeepPiSign.visibility = View.GONE
            }
        }
    }

    override fun setDataOnViewViaPreferences() {
        preferenceManager?.apply {

            val buff = "${getString(R.string.ti)} = ${readTinsp()} ${getString(R.string.hint_s)}"
            textViewTinsp.text = "${getString(R.string.ti)} = ${String.format("%.2f",
                VentilatorApp.testingConditonMap.get(Configs.LBL_TINSP)?.let {
                    it
                } ?: kotlin.run {
                    readTinsp()
                }
            )} ${getString(R.string.hint_s)}"

            val setTexp = Configs.calculateTexp(readRR().toInt(), readTinsp())
            textViewTexp.text = "${getString(R.string.te)} = ${String.format("%.2f",setTexp.toFloat())} ${getString(R.string.hint_s)}"

            textViewPeepPi.text =
                "${getString(R.string.peep)} = ${readPEEP().toInt()} ${getString(R.string.hint_cmH2o)}"

            val setTot = Configs.calculateTtot(readRR().toInt())
            textViewTtot.text = "${getString(R.string.titot)} = $setTot"

            Log.i("mode_code", modeCode.toString())

            textViewPeepPiSign.text = "${getString(R.string.pinsp)} = ${readPplat()} cmH₂O"

            //String.format("%02d",read)String.format("%02d",readSupportPressure())}
            //textViewPi.text="${getString(R.string.ps)} = "+String.format("%02f",readSupportPressure())

            textViewPi.text = "${getString(R.string.support_pressure)}=${readSupportPressure()} cmH₂O"

            textViewTrigERatio.text =
                "${getString(R.string.tExp)} = ${readTexp().toInt().toString()} %"


            textViewIERatio.text = "${getString(R.string.ieratio) + " "} = ${
                " " +
                        Configs.calculateIERatio(
                            VentilatorApp.testingConditonMap.get(Configs.LBL_RR)?.let {
                                it.toInt()
                            } ?: kotlin.run {
                                readRR().toInt()
                            },
                            VentilatorApp.testingConditonMap.get(Configs.LBL_TINSP)?.let {
                                it
                            } ?: kotlin.run {
                                readTinsp()
                            }
                        )
            }"
        }
    }


    override fun updateDataOnView(parameter: ControlParameterModel) {

        preferenceManager?.apply {
            when (parameter.ventKey) {

                Configs.LBL_TEXP -> {
                    try {
                        textViewTrigERatio.text = "${getString(R.string.tExp)} = ${
                            String.format(
                                "%.1f",
                                parameter.reading.toInt().toFloat()
                            )
                        }${R.string.hint_sec}"
                        Log.i("CHECK_TRIGGER", parameter.reading.toString())

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                Configs.LBL_TINSP -> {
                    try {
                        val setTexp = Configs.calculateTexp(
                            VentilatorApp.testingConditonMap.get(Configs.LBL_RR)?.let {
                                it.toInt()
                            } ?: kotlin.run {
                                readRR().toInt()
                            }, parameter.reading.toFloat()
                        )
                        textViewTexp.text = "${getString(R.string.te)} = ${String.format("%.2f",setTexp.toFloat())} ${getString(R.string.hint_s)}"

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    val calculatedValue = Configs.calculateIERatio(
                        VentilatorApp.testingConditonMap.get(Configs.LBL_RR)?.let {
                            it.toInt()
                        } ?: kotlin.run {
                            readRR().toInt()
                        }, parameter.reading.toFloat()
                    )

                    if (calculatedValue.split(":")[1].toFloat() < 1.0f) {
                        ieRatioBack.setBackgroundResource(R.drawable.red_rect)
                    } else {
                        ieRatioBack.setBackgroundResource(R.drawable.rectangle)
                    }

                    textViewIERatio.text =
                        "${getString(R.string.ieratio) + " "} = ${" " + calculatedValue}"

                    textViewTinsp.text = "${getString(R.string.ti)} = ${
                        String.format(
                            "%.2f",
                            parameter.reading.toFloat()
                        )
                    } ${getString(R.string.hint_s)}"
                }

                Configs.LBL_RR -> {
                    try {

                        val setTexp = Configs.calculateTexp(
                            parameter.reading.toInt(),
                            VentilatorApp.testingConditonMap.get(Configs.LBL_TINSP)?.let {
                                it
                            } ?: kotlin.run {
                                readTinsp()
                            })

                        textViewTexp.text = "${getString(R.string.te)} = $setTexp ${getString(R.string.hint_s)}"
                        val setTot = Configs.calculateTtot(parameter.reading.toInt())
                        textViewTtot.text = "${getString(R.string.titot)} = $setTot"
                        val calculatedValue = calculateIERatio(
                            parameter.reading.toInt(),
                            VentilatorApp.testingConditonMap.get(Configs.LBL_TINSP)?.let {
                                it
                            } ?: kotlin.run {
                                readTinsp()
                            })

                        if (calculatedValue.split(":")[1].toFloat() < 1.0f) {
                            ieRatioBack.setBackgroundResource(R.drawable.red_rect)
                        } else {
                            ieRatioBack.setBackgroundResource(R.drawable.rectangle)
                        }
                        textViewIERatio.text = "${getString(R.string.ieratio) + " "}=${
                            " " + Configs.calculateIERatio(
                                parameter.reading.toInt(),
                                VentilatorApp.testingConditonMap.get(Configs.LBL_TINSP)?.let {
                                    it
                                } ?: kotlin.run {
                                    readTinsp()
                                })
                        }"
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                Configs.LBL_PPLAT -> {
                    textViewPeepPiSign.text =
                        "${getString(R.string.pinsp)} = ${parameter.reading.toInt()} ${getString(R.string.hint_cmH2o)}"
                }

                Configs.LBL_PEEP -> {
                    textViewPeepPi.text =
                        "${getString(R.string.peep)} = ${parameter.reading.toInt()} cmH₂O"
                }

                Configs.LBL_SUPPORT_PRESSURE -> {
                    textViewPi.text = "${getString(R.string.support_pressure)} = ${
                        String.format(
                            "%.1f",
                            parameter.reading.toFloat()
                        )
                    }"
                }

                else -> {}

            }
        }

    }

    private fun checkMode() {

        when (modeCode) {
            Configs.MODE_PC_CMV -> {
                textViewPi.visibility = View.GONE
                textViewTrigERatio.visibility = View.GONE
                // textViewPs.visibility = View.GONE

            }
            Configs.MODE_VCV_CMV -> {
                textViewPi.visibility = View.GONE
                textViewTrigERatio.visibility = View.GONE
            }

        }

    }
}


fun GeneralGraphicalToolTipFragment.setHeightWidth(
    heightDialog: Int?,
    widthDialog: Int?,
    status: Boolean?
) {
    dialog?.window?.apply {
        isCancelable = false
        setGravity(Gravity.START or Gravity.BOTTOM)
        decorView.apply {
            val params: WindowManager.LayoutParams = attributes

            params.x = 80
            params.y = 10

            params.height = 400
            params.width = 780

            params.dimAmount = 0.0F
            params.screenBrightness = 5.0F

            attributes = params
        }
    }


    hideSystemUI()


}
