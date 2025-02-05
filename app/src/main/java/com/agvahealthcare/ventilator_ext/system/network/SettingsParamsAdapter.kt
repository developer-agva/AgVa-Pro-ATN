package com.agvahealthcare.ventilator_ext.system.network

import androidx.recyclerview.widget.RecyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.settings_item.view.*

class SettingsParamsAdapter(
    private val context: Context,
    private val items: ArrayList<String>,
    private val prefManager: PreferenceManager?
) :
    RecyclerView.Adapter<SettingsParamsAdapter.TableViewHolder>() {

    class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.settings_item, parent, false)
        return TableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {

        val item = items[position]

        when (position) {
            0 -> holder.itemView.labelTextView.text = "PIP"

            1 -> {
                if (prefManager?.readModeType() == Configs.ModeType.TYPE_Pressure) holder.itemView.labelTextView.text = "Target Volume"
                else holder.itemView.labelTextView.text = "VTI"
            }

            2 -> holder.itemView.labelTextView.text = "Peep"
            3 -> holder.itemView.labelTextView.text = "RR"
            4 -> holder.itemView.labelTextView.text = "TrigFlow"
            5 -> holder.itemView.labelTextView.text = "pPLat"
            6 -> holder.itemView.labelTextView.text = "InhaleTime"
            7 -> holder.itemView.labelTextView.text = "Flow"
            8 -> holder.itemView.labelTextView.text = "Fio2"
            9 -> holder.itemView.labelTextView.text = "Support Pressure"
            10 -> holder.itemView.labelTextView.text = "Slope"
            11 -> holder.itemView.labelTextView.text = "tLow"
            12 -> holder.itemView.labelTextView.text = "Texp"
            13-> holder.itemView.labelTextView.text = "statusApnea"
            14 -> holder.itemView.labelTextView.text = "rrApnea"
            15 -> holder.itemView.labelTextView.text = "tApnea"
            16 -> holder.itemView.labelTextView.text = "vtApnea"
            17 -> holder.itemView.labelTextView.text = "trigFlowApnea"
            18 -> holder.itemView.labelTextView.text = "inspPause"
            19 -> holder.itemView.labelTextView.text = "Peep Valve"
            20 -> holder.itemView.labelTextView.text = "Status Neonate"
            21 -> holder.itemView.labelTextView.text = "Status VGV"

        }

        holder.itemView.valueTextView.text = item
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
