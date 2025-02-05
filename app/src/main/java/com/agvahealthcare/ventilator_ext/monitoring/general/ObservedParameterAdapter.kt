package com.agvahealthcare.ventilator_ext.monitoring.general

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel
import com.agvahealthcare.ventilator_ext.utility.utils.Configs

class ObservedParameterAdapter(private var observedValueList: ArrayList<ObservedParameterModel>?) : RecyclerView.Adapter<ObservedParameterAdapter.VHObservedParameterAdapter>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VHObservedParameterAdapter {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_general_data, parent, false)
        return VHObservedParameterAdapter(itemView)
    }

    override fun onBindViewHolder(holder: VHObservedParameterAdapter, position: Int) {

        holder.tvUnit?.text = observedValueList?.get(position)?.units
        holder.tvLabel?.text = observedValueList?.get(position)?.label
        if (observedValueList?.get(position)?.label==Configs.LBL_TRIGGER){
            holder.tvValue?.text = observedValueList?.get(position)?.actualValue?.substring(0,1)
        } else{
            holder.tvValue?.text = observedValueList?.get(position)?.actualValue
        }
    }

    override fun getItemCount(): Int {
        return observedValueList?.size!!
    }

    fun setModelList(observedList: ArrayList<ObservedParameterModel>?) {
        observedValueList = observedList
    }

    class VHObservedParameterAdapter(row: View) : RecyclerView.ViewHolder(row) {
        var tvLabel: TextView? = null
        var tvValue: TextView? = null
        var tvUnit: TextView? = null
        var mainLayoutPanel: ConstraintLayout? = null

        init {
            // Define click listener for the ViewHolder's View.
            tvLabel = row.findViewById(R.id.textViewLabel)
            tvValue = row.findViewById(R.id.textViewValue)
            tvUnit = row.findViewById(R.id.textViewUnit)
            mainLayoutPanel = row.findViewById(R.id.mainLayoutPanel)
        }
    }
}