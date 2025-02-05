package com.agvahealthcare.ventilator_ext.system.services

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.model.ServiceIssueModel
import com.agvahealthcare.ventilator_ext.utility.callback.OnIssueSelectListener

class IssueAdapter(private var dataList: ArrayList<ServiceIssueModel>,private var onClick : OnIssueSelectListener) :
    RecyclerView.Adapter<IssueAdapter.ServiceViewHolder>() {

    fun updateDataList(newList : ArrayList<ServiceIssueModel>){
        dataList = newList
        notifyDataSetChanged()
    }

    var selectedIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.service_issue_item_layout, parent, false)
        return ServiceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val data = dataList[position]

        holder.apply {
            cbIssue?.text = data.issue
            cbIssue?.isChecked = data.isTrue
        }

        holder.cbIssue?.setOnClickListener {
            onClick.issueSelect(data.issue,position)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var cbIssue: CheckBox? = null
        var parentLayout: ConstraintLayout? = null

        init {
            cbIssue = view.findViewById(R.id.cbIssue)
            parentLayout = view.findViewById(R.id.issueLayoutService)
        }
    }
}