package com.agvahealthcare.ventilator_ext.system.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnDismissDialogListener
import com.agvahealthcare.ventilator_ext.callback.OnKnobPressListener
import com.agvahealthcare.ventilator_ext.callback.OnLimitChangeListener
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.KnobDialog
import com.agvahealthcare.ventilator_ext.utility.TUBE_MAX_VALUE
import kotlinx.android.synthetic.main.fragment_tube_comp.*
import kotlinx.android.synthetic.main.knob_progress_view.view.*

class TubeCompFragment: DialogFragment(),OnKnobPressListener,OnLimitChangeListener,OnDismissDialogListener {
    private var tubeDiaDialog : KnobDialog?= null
    private var prefManager: PreferenceManager? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tube_comp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDataViaPreference()
    }
    private fun setDataViaPreference() {
        prefManager?.apply {
            progress_tubeComp.param_progress_bar.maxProgress= TUBE_MAX_VALUE.toDouble()

            progress_tubeComp.param_progress_bar.setCurrentProgress(readTubeDia().toInt().toDouble())

            progress_tubeComp.textView.setText(""+readTubeDia().toInt())

        }
    }

    override fun handleDialogClose() {
        tubeDiaDialog?.takeIf { it.isVisible }?.dismiss()

    }

    override fun onKnobPress(previousValue: Float, newValue: Float) {

        Log.i("DIAMETER_CHECK", "Set value = $newValue")
        progress_tubeComp.param_progress_bar.maxProgress= TUBE_MAX_VALUE.toDouble()
        progress_tubeComp.param_progress_bar.setCurrentProgress(newValue.toInt().toDouble())
        progress_tubeComp.textView.setText(""+newValue.toInt())
        prefManager?.setTubeDia(newValue)
    }

    override fun onLimitChange(previousValue: Float, newValue: Float) {
        TODO("Not yet implemented")
        view.let {
            (it?.param_progress_bar as? ProgressBar)?.apply {
                this.progress = newValue.toInt()
            }

            (it?.textView as? TextView)?.apply {
                this.text = newValue.toInt().toString()
            }
        }
    }


}
