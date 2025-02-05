package com.agvahealthcare.ventilator_ext.system.debug

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R

class DebugAdapter( private var dataList: MutableList<String>) : RecyclerView.Adapter<DebugAdapter.DebugViewHolder>() {


    fun updateDataList(newList: MutableList<String>) {
        dataList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebugViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_debug_layout, parent, false)
        return DebugViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DebugViewHolder, position: Int) {

        holder.txtDebugData?.text = dataList[position]
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    class DebugViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var txtDebugData: TextView? = null

        init {
            txtDebugData = view.findViewById(R.id.txtDebugData)
        }
    }
}