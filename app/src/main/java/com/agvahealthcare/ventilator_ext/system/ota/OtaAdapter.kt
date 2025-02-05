package com.agvahealthcare.ventilator_ext.system.ota

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.api.model.DataX

class OtaAdapter(private var dataList: ArrayList<DataX>,private val otaClickListener: OtaClickListener) :
    RecyclerView.Adapter<OtaAdapter.OtaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ota_layout, parent, false)
        return OtaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OtaViewHolder, position: Int) {

        val data = dataList[position]

        holder.txtOta?.text = "Version: ${data.version}"
        holder.txtOtaDateTime?.text = "Date: ${data.dateTime}"

        holder.btnInstall?.setOnClickListener {
            otaClickListener.onClickOta(data.app_url)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class OtaViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var otaLayout: ConstraintLayout? = null
        var btnInstall: ConstraintLayout? = null
        var txtOta: TextView? = null
        var txtOtaDateTime: TextView? = null

        init {
            otaLayout = view.findViewById(R.id.otaLayout)
            btnInstall = view.findViewById(R.id.btnIntall)
            txtOta = view.findViewById(R.id.txtOta)
            txtOtaDateTime = view.findViewById(R.id.txtOtaDateTime)
        }
    }
}