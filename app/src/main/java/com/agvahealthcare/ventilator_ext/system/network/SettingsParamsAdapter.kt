package com.agvahealthcare.ventilator_ext.system.network

import androidx.recyclerview.widget.RecyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.utils.Configs

class SettingsParamsAdapter(
    private val context: Context,
    private val items: ArrayList<String>,
    private val prefManager: PreferenceManager?
) :
    RecyclerView.Adapter<SettingsParamsAdapter.TableViewHolder>() {

    class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtLebel :TextView? = null
        var txtValue :TextView? = null

        init {
            txtLebel = itemView.findViewById(R.id.labelTextView)
            txtValue = itemView.findViewById(R.id.valueTextView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.settings_item, parent, false)
        return TableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {

        val item = items[position]

        when (position) {
            0 -> holder.txtLebel?.text = "PIP"

            1 -> {
                if (prefManager?.readModeType() == Configs.ModeType.TYPE_Pressure) holder.txtLebel?.text = "Target Volume"
                else holder.txtLebel?.text = "VTI"
            }

            2 -> holder.txtLebel?.text = "Peep"
            3 -> holder.txtLebel?.text = "RR"
            4 -> holder.txtLebel?.text = "TrigFlow"
            5 -> holder.txtLebel?.text = "pPLat"
            6 -> holder.txtLebel?.text = "InhaleTime"
            7 -> holder.txtLebel?.text = "Flow"
            8 -> holder.txtLebel?.text = "Fio2"
            9 -> holder.txtLebel?.text = "Support Pressure"
            10 -> holder.txtLebel?.text = "Slope"
            11 -> holder.txtLebel?.text = "tLow"
            12 -> holder.txtLebel?.text = "Texp"
            13-> holder.txtLebel?.text = "statusApnea"
            14 -> holder.txtLebel?.text = "rrApnea"
            15 -> holder.txtLebel?.text = "tApnea"
            16 -> holder.txtLebel?.text = "vtApnea"
            17 -> holder.txtLebel?.text = "trigFlowApnea"
            18 -> holder.txtLebel?.text = "inspPause"
            19 -> holder.txtLebel?.text = "Peep Valve"
            20 -> holder.txtLebel?.text = "Status Neonate"
            21 -> holder.txtLebel?.text = "Status VGV"

        }

        holder.txtValue?.text = item
    }

    override fun getItemCount(): Int {
        return items.size
    }
}
