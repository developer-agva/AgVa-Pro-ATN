package com.agvahealthcare.ventilator_ext.dashboard

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.model.AlarmModel
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.AlarmType

class BufferAlarmRecyclerAdapter(private var ackList:ArrayList<AlarmModel>):RecyclerView.Adapter<BufferAlarmRecyclerAdapter.BufferAlarmViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BufferAlarmViewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.list_item_stand_drop_down,parent,false)
        return BufferAlarmViewHolder(itemView)
    }

    fun updateList(newList:ArrayList<AlarmModel>){
        ackList = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: BufferAlarmViewHolder, position: Int) {
        val tile =ackList.get(position)
        //holder.tv_cell_stand_status?.text=tile.message
        if (position % 2 == 0){
            holder.rlMain?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        } else {
            holder.rlMain?.setBackgroundColor(Color.parseColor("#eeeeee"))
        }
        if (tile.createdAt.isNotEmpty()){
            holder.tv_cell_stand_date?.setText(tile.createdAt)
        } else {
            holder.tv_cell_stand_date?.setText("-")
        }
        holder.tv_cellStandTitle?.text=tile.message

//        Log.d("priority",tile.priority.toString())
        when(tile.priority){
            in listOf<AlarmType>(AlarmType.ALARM_CRITICAL_LEVEL) -> {
                holder.tv_cell_stand_priority?.text="Critical"
                holder.tv_cell_stand_priority?.setTextColor(Color.parseColor("#E57373"));
            }

            AlarmType.ALARM_HIGH_LEVEL -> {
                holder.tv_cell_stand_priority?.text="High"
                holder.tv_cell_stand_priority?.setTextColor(Color.parseColor("#E57373"));
            }

            AlarmType.ALARM_MEDIUM_LEVEL -> {
                holder.tv_cell_stand_priority?.text="Medium"
                holder.tv_cell_stand_priority?.setTextColor(Color.parseColor("#FBC02D"));
            }
            AlarmType.ALARM_LOW_LEVEL -> {
                holder.tv_cell_stand_priority?.text="Low"
                holder.tv_cell_stand_priority?.setTextColor(Color.parseColor("#9E9E9E"));
            }
            else -> holder.tv_cell_stand_priority?.text="N/A"
        }
       // holder.tv_cell_stand_priority?.setText(tile.priority.toString())

    }

    override fun getItemCount(): Int {
        return ackList.size
    }

    class BufferAlarmViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
                var tv_cellStandTitle:TextView?=null
                var tv_cell_stand_status:TextView?=null
                var rlMain:LinearLayoutCompat?=null
                var tv_cell_stand_date : AppCompatTextView?=null
                var tv_cell_stand_priority :AppCompatTextView?=null
            init {
                tv_cellStandTitle=itemView.findViewById(R.id.cell_stand_title)
                tv_cell_stand_status=itemView.findViewById(R.id.cell_stand_status)
                rlMain=itemView.findViewById(R.id.rl_main)
                tv_cell_stand_date=itemView.findViewById(R.id.cell_stand_date)
                tv_cell_stand_priority=itemView.findViewById(R.id.cell_stand_priority)
            }
    }

}