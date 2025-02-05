package com.agvahealthcare.ventilator_ext.dashboard.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel
import com.agvahealthcare.ventilator_ext.utility.utils.Configs


class SecondaryObservedParameterAdapter(
    private val ctx: Context,
    private var observedValueList: ArrayList<ObservedParameterModel>
) : RecyclerView.Adapter<SecondaryObservedParameterAdapter.VHSecondaryObservedParameterAdapter>() {
    val preferenceManager = PreferenceManager(ctx)

    var selectedIndex = -1

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VHSecondaryObservedParameterAdapter {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data, parent, false)

        return VHSecondaryObservedParameterAdapter(itemView)
    }

    override fun onBindViewHolder(holder: VHSecondaryObservedParameterAdapter, position: Int) {
        holder.bind(position, observedValueList)

        // COLOR OF TILE
        observedValueList.apply {
            updateGraphics(holder, this[position], position)
        }
    }


    private fun addMarginEnd(
        holder: VHSecondaryObservedParameterAdapter,
        @DimenRes marginRes: Int
    ) {
        holder.layoutPanel?.apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
                resources.getDimension(R.dimen._45sdp).toInt(),
                resources.getDimension(R.dimen.top_bar_height).toInt(),
            ).apply {
                setMargins(0, 0, resources.getDimension(marginRes).toInt(), 0)
            }
        }
    }

    private fun updateGraphics(
        holder: SecondaryObservedParameterAdapter.VHSecondaryObservedParameterAdapter,
        model: ObservedParameterModel,
        position: Int
    ) {

        if (position == selectedIndex) {

            if (!model.actualValue.equals("-".trim())) {

                if (isNotValid(model)) {
                    holder.layoutPanel?.setBackgroundResource(R.drawable.background_red_border_yellow)
                } else {
                    holder.layoutPanel?.setBackgroundResource(R.drawable.background_black_border_yellow)
                }
            } else holder.layoutPanel?.setBackgroundResource(R.drawable.background_black_border_yellow)

        } else {
            if (!model.actualValue.equals("-".trim())) {

                if (isNotValid(model)) {
                    holder.layoutPanel?.setBackgroundResource(R.drawable.background_red_border)
                } else {
                    holder.layoutPanel?.setBackgroundResource(R.drawable.background_black)
                }
            } else holder.layoutPanel?.setBackgroundResource(R.drawable.background_black)
        }
    }

    private fun isNotValid(model: ObservedParameterModel): Boolean {
        val actualValue = model.actualValue.toDoubleOrNull()
        Log.i("TILECHECK", "${model.label} has actualValue = ${model.actualValue}")

        return if (actualValue != null) {
            if (model.lowerLimitValue != null && model.upperLimitValue != null) {
                val lowerLimit: Double = model.lowerLimitValue.toDouble()
                val upperLimit: Double = model.upperLimitValue.toDouble()
                Log.i(
                    "ALARMLIMITCHECK",
                    (model.label + model.labelSubscript) + " ---> ACTUAL_VALUE" + actualValue + "LOWER_LIMIT" + lowerLimit + "UPPER_LIMIT" + upperLimit
                )

                val isWarningActivated = (model.label + model.labelSubscript).run {
                    when (this) {
                        Configs.LBL_PIP -> preferenceManager.readPipLimitState()
                        Configs.LBL_PEEP -> preferenceManager.readPeepLimitState()
                        Configs.LBL_VTE -> preferenceManager.readVteLimitState()
                        Configs.LBL_FIO2 -> preferenceManager.readFio2LimitState()
                        Configs.LBL_RR -> preferenceManager.readRRLimitState()
                        Configs.LBL_VTI -> preferenceManager.readVtiLimitState()
                        Configs.LBL_AVERAGE_LEAK -> preferenceManager.readLeakLimitState()
                        Configs.LBL_MVE -> preferenceManager.readMveLimitState()
                        Configs.LBL_SPO2 -> preferenceManager.readSpO2LimitState()
                        else -> false
                    }
                }

                val isParamOutofLimit = actualValue < lowerLimit || actualValue > upperLimit;
                isWarningActivated && isParamOutofLimit

            } else false
        } else false
    }


    override fun getItemCount(): Int {
        return observedValueList.size
    }

    fun addModeEntry(observedList: ArrayList<ObservedParameterModel>) {
        observedValueList = observedList
    }

    class VHSecondaryObservedParameterAdapter(row: View) : RecyclerView.ViewHolder(row) {
        var tvLabel: TextView? = null
        var tvValue: TextView? = null
        var tvUnit: TextView? = null
        var layoutPanel: LinearLayoutCompat? = null

        init {
            // Define click listener for the ViewHolder's View.
            tvLabel = row.findViewById(R.id.textViewLabel)
            tvValue = row.findViewById(R.id.textViewValue)
            tvUnit = row.findViewById(R.id.textViewUnit)
            layoutPanel = row.findViewById(R.id.mainLayoutPanel)


        }

        fun bind(position: Int, dataTiles: ArrayList<ObservedParameterModel>?) {
            val tile = dataTiles?.get(position)
            // bind data
            if (tile != null) {
                if (tile.labelSubscript != null) tvLabel?.text = tile.label + tile.labelSubscript
                else tvValue?.text = tile.label
                tvValue?.text = if (Configs.LBL_TRIGGER.equals(tile.label)) tile.actualValue?.get(0)
                    ?.toString() else tile.actualValue
                tvUnit?.text = tile.units
            }
        }

    }
}