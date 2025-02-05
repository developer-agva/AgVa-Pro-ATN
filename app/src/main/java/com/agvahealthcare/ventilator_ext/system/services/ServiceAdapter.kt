package com.agvahealthcare.ventilator_ext.system.services

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.api.model.serviceDataModel.Data
import com.agvahealthcare.ventilator_ext.utility.callback.OnServiceClickListener

class ServiceAdapter(private var context : Context,private var dataList: ArrayList<Data>,private var serviceClick : OnServiceClickListener) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    var selectedIndex: Int = 0

    fun updateDataList(newList : ArrayList<Data>){
        dataList = newList
        selectedIndex = 0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.service_item_layout, parent, false)
        return ServiceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {

        val data = dataList[position]

        holder.txtTicketNo?.text = data.ServiceRequestSerialNo
        holder.txtIssue?.text = data.ServiceRequestMessage
        holder.txtDate?.text = data.ServiceRequestDate
        holder.txtStatus?.text = data.ticketStatus

        if (data.ticketStatus == "Open"){
            holder.txtStatus?.setBackgroundColor(context.resources.getColor(R.color.preCalib_amber))
            holder.txtStatus?.setTextColor(Color.BLACK)
        }else{
            holder.txtStatus?.setBackgroundColor(context.resources.getColor(R.color.ack_red));
            holder.txtStatus?.setTextColor(Color.WHITE)
        }

        if ((position) == selectedIndex){
            holder.parentLayout?.setBackgroundResource(R.color.ack_green)
            holder.txtTicketNo?.setTextColor(Color.WHITE)
            holder.txtIssue?.setTextColor(Color.WHITE)
            holder.txtDate?.setTextColor(Color.WHITE)
        }
        else{
            if ((position) % 2 == 0){
                holder.parentLayout?.setBackgroundColor(Color.parseColor("#FFFFFF"))
                holder.txtTicketNo?.setTextColor(Color.BLACK)
                holder.txtIssue?.setTextColor(Color.BLACK)
                holder.txtDate?.setTextColor(Color.BLACK)
            } else {
                holder.parentLayout?.setBackgroundColor(Color.parseColor("#eeeeee"))
                holder.txtTicketNo?.setTextColor(Color.BLACK)
                holder.txtIssue?.setTextColor(Color.BLACK)
                holder.txtDate?.setTextColor(Color.BLACK)
            }
        }

        holder.parentLayout?.setOnClickListener {
            serviceClick.serviceClick(data)
        }

    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelection(pos: Int){
        if(pos in 0 until itemCount) {
            selectedIndex = pos
            notifyDataSetChanged()
        }
    }

    fun getSelection() = selectedIndex

    fun setSelectionDownward() = setSelection(selectedIndex + 1)
    fun setSelectionUpword() = setSelection(selectedIndex - 1)


    class ServiceViewHolder (view: View) : RecyclerView.ViewHolder(view){

        var txtTicketNo: TextView? = null
        var txtDate: TextView? = null
        var txtIssue: TextView?=null
        var txtStatus: Button?=null
        var parentLayout: ConstraintLayout?=null
        init {
            txtDate = view.findViewById(R.id.txtDate)
            txtTicketNo= view.findViewById(R.id.txtTicketNo)
            txtIssue= view.findViewById(R.id.txtIssue)
            txtStatus = view.findViewById(R.id.txtStatus)
            parentLayout = view.findViewById(R.id.parentlayout)
        }
    }
}