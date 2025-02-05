package com.agvahealthcare.ventilator_ext.logs.alarm

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.database.entities.AlarmDBModel
import com.agvahealthcare.ventilator_ext.database.entities.EventDataModel
import com.agvahealthcare.ventilator_ext.utility.FIRST_FILTER_NAME
import com.agvahealthcare.ventilator_ext.utility.LOG_TYPE_DEBUG
import com.agvahealthcare.ventilator_ext.utility.utils.AlarmConfiguration
import com.agvahealthcare.ventilator_ext.utility.utils.Configs

class AlarmAdapter(private var dataList: ArrayList<String>) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private var selectedIndex : Int = 0

    fun updateDataList(newList : ArrayList<String>,isMinus:Boolean?){
        dataList = newList
        if (isMinus == true) selectedIndex = newList.size-1 else selectedIndex = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm_data, parent, false)

        return AlarmViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {


        val data = dataList[position]
        val createdAt = data.split(",")[2]
        val uhid = data.split(",")[3]
        val message = data.split(",")[1]
        val key = data.split(",")[0]

        if (createdAt.isNotEmpty()){
            holder.tvStartAlarmDate?.text = createdAt.split(" ")[0]
            holder.tvStartAlarmtime?.text = createdAt.split(" ")[1]
        }
        if (uhid == FIRST_FILTER_NAME){
            holder.textViewUHID?.text="-"
        } else{
            holder.textViewUHID?.text=uhid
        }
        if (position == selectedIndex){
            holder.layoutId?.setBackgroundResource(R.color.ack_green)
            holder.tvStartAlarmDate?.setTextColor(Color.WHITE)
            holder.tvAlarmType?.setTextColor(Color.WHITE)
            holder.tvStartAlarmtime?.setTextColor(Color.WHITE)
            holder.textViewUHID?.setTextColor(Color.WHITE)
            holder.tvEndAlarmDate?.setTextColor(Color.WHITE)

        }else{
            if (position % 2 == 0){
                holder.layoutId?.setBackgroundColor(Color.parseColor("#FFFFFF"))
                holder.tvStartAlarmDate?.setTextColor(Color.BLACK)
                holder.tvAlarmType?.setTextColor(Color.BLACK)
                holder.tvStartAlarmtime?.setTextColor(Color.BLACK)
                holder.textViewUHID?.setTextColor(Color.BLACK)
                holder.tvEndAlarmDate?.setTextColor(Color.BLACK)
            } else {
                holder.layoutId?.setBackgroundColor(Color.parseColor("#eeeeee"))
                holder.tvStartAlarmDate?.setTextColor(Color.BLACK)
                holder.tvAlarmType?.setTextColor(Color.BLACK)
                holder.tvStartAlarmtime?.setTextColor(Color.BLACK)
                holder.textViewUHID?.setTextColor(Color.BLACK)
                holder.tvEndAlarmDate?.setTextColor(Color.BLACK)

            }
        }

        holder.tvAlarmType?.text = message

        if (key != "0"){
            val ackPriority = AlarmConfiguration.getPriority(key)
            when(ackPriority){
                Configs.AlarmType.ALARM_CRITICAL_LEVEL ->{
                    holder.tvEndAlarmDate?.text="Critical"
                    //holder.tvEndAlarmDate?.setTextColor(Color.parseColor("#E57373"));
                    holder.tvcolorpriority?.setBackgroundColor(Color.parseColor("#E57373"));
                    //     holder.layoutId?.setBackgroundColor(Color.parseColor("#E57373"));
                }
                Configs.AlarmType.ALARM_HIGH_LEVEL -> {
                    holder.tvEndAlarmDate?.text="High"
                    //holder.tvEndAlarmDate?.setTextColor(Color.parseColor("#E57373"));
                    holder.tvcolorpriority?.setBackgroundColor(Color.parseColor("#E57373"));
                    //   holder.layoutId?.setBackgroundColor(Color.parseColor("#E57373"));
                }
                Configs.AlarmType.ALARM_MEDIUM_LEVEL -> {
                    holder.tvEndAlarmDate?.text="Medium"
                    //holder.tvEndAlarmDate?.setTextColor(Color.parseColor("#E57373"));
                    holder.tvcolorpriority?.setBackgroundColor(Color.parseColor("#FFBF00"));
                    //     holder.layoutId?.setBackgroundColor(Color.parseColor("#FBC02D"));
                }
                Configs.AlarmType.ALARM_LOW_LEVEL -> {
                    holder.tvEndAlarmDate?.text="Low"
                    //holder.tvEndAlarmDate?.setTextColor(Color.parseColor("#FFFFDD59"));
                    holder.tvcolorpriority?.setBackgroundColor(Color.parseColor("#FFFFDD59"));
                    //   holder.layoutId?.setBackgroundColor(Color.parseColor("#FFFFDD59"));
                }
            }
        }
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
    fun setSelectionUpward() = setSelection(selectedIndex - 1)

    override fun getItemCount(): Int {
        return dataList.size-1
    }

    class AlarmViewHolder (view: View) : RecyclerView.ViewHolder(view){
        var tvAlarmType: TextView? = null
        var tvcolorpriority: Button?=null
        var tvStartAlarmDate: TextView? = null
        var tvStartAlarmtime: TextView?=null
        var tvEndAlarmDate: TextView? = null
        var textViewUHID: TextView?=null
        var layoutId: LinearLayoutCompat?= null
        init {
            tvAlarmType = view.findViewById(R.id.textViewAlarmType)
            tvStartAlarmDate = view.findViewById(R.id.textStartAlarmDate)
            tvStartAlarmtime = view.findViewById(R.id.textStartAlarmtime)
            tvEndAlarmDate = view.findViewById(R.id.textEndAlarmDate)
            tvcolorpriority = view.findViewById(R.id.textcolorpriority)
            textViewUHID = view.findViewById(R.id.textViewUHID)
            layoutId = view.findViewById(R.id.layoutid)
        }

    }
}