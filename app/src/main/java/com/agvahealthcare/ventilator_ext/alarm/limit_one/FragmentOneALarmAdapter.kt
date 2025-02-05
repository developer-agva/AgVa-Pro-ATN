package com.agvahealthcare.ventilator_ext.alarm.limit_one

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import kotlinx.android.synthetic.main.alarm_data.view.*

class FragmentOneALarmAdapter (private var list:ArrayList<String>): RecyclerView.Adapter<FragmentOneALarmAdapter.FragmentOneViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FragmentOneViewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.alarm_data,parent,false)
        return FragmentOneViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FragmentOneViewHolder, position: Int) {
        val tile=list[position]
        if (tile=="PIP"){
            holder.tvValue?.text="PIP"
            holder.tvUnit?.text="cmH2O"
            holder.tvUpperLimitProgressView?.progress=80
            holder.tvLowerLimitProgressView?.progress=0
            holder.tvLowerLimitTextView?.text="0"
            holder.tvUpperLimitTextView?.text="80"
        } else if (tile=="Vte"){
            holder.tvValue?.text="Vte"
            holder.tvUnit?.text="I/min"
            holder.tvUpperLimitProgressView?.progress=2000
            holder.tvLowerLimitProgressView?.progress=0
            holder.tvLowerLimitTextView?.text="0"
            holder.tvUpperLimitTextView?.text="2000"
        } else if (tile=="PEEP"){
            holder.tvValue?.text="PEEP"
            holder.tvUnit?.text="b/min"
            holder.tvUpperLimitProgressView?.progress=50
            holder.tvLowerLimitProgressView?.progress=0
            holder.tvLowerLimitTextView?.text="0"
            holder.tvUpperLimitTextView?.text="50"
        } else if (tile=="RR"){
            holder.tvValue?.text="RR"
            holder.tvUnit?.text="bpm"
            holder.tvUpperLimitProgressView?.progress=100
            holder.tvLowerLimitProgressView?.progress=0
            holder.tvLowerLimitTextView?.text="0"
            holder.tvUpperLimitTextView?.text="100"
        } else if (tile=="MVi"){
            holder.tvValue?.text="MVi"
            holder.tvUnit?.text="litre"
            holder.tvUpperLimitProgressView?.progress=50
            holder.tvLowerLimitProgressView?.progress=0
            holder.tvLowerLimitTextView?.text="0"
            holder.tvUpperLimitTextView?.text="50"
        }


    }

    override fun getItemCount(): Int {
        return list.size
    }

     class FragmentOneViewHolder(view:View) : RecyclerView.ViewHolder(view)  {

            var tvValue:TextView?=null
            var tvUnit:TextView?=null
            var tvLowerLimitProgressView:ProgressBar?=null
            var tvUpperLimitProgressView:ProgressBar?=null
            var tvLowerLimitTextView:TextView?=null
            var tvUpperLimitTextView:TextView?=null
         init {
             var tvValue=view.textViewValue
             var tvUnit=view.textViewUnit
             var tvLowerLimitProgressView= view.progress_bar_lowerlimit
             var tvUpperLimitProgressView=view.progress_bar_upperlimit
             var tvLowerLimitTextView=view.textViewlowerlimit
             var tvUpperLimitTextView=view.textView_upperlimit
         }

    }

}