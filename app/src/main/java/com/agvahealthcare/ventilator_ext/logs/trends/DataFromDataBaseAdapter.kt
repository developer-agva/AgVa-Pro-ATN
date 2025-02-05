package com.agvahealthcare.ventilator_ext.logs.trends

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils

class DataFromDataBaseAdapter(ctx:Context, dataList:ArrayList<String>) : RecyclerView.Adapter<DataFromDataBaseAdapter.DFDViewHolder>() {
    var context:Context = ctx
    var list:ArrayList<String> =  dataList

    private var selectedIndex : Int = 0

    fun updateDataList(newList: ArrayList<String>){
        list = newList
        selectedIndex = 0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DFDViewHolder {
        Log.i("tetingmasoom","4")
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.parameter_units_itemtwo,parent,false)
        return DFDViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DFDViewHolder, position: Int) {
        val dataStore = list[position]

        holder.tv_time?.text = dataStore.split(",")[0].split(" ")[1]
        holder.tv_pip?.text = dataStore.split(",")[1]
        holder.tv_sp?.text =  dataStore.split(",")[2]
        holder.tv_peep?.text = dataStore.split(",")[3]
        holder.tv_ma?.text = dataStore.split(",")[4]
        holder.tv_vti?.text = dataStore.split(",")[5]
        holder.tv_vte?.text = dataStore.split(",")[6]
        holder.tv_mve?.text = dataStore.split(",")[7]
        holder.tv_mvi?.text = dataStore.split(",")[8]
        holder.tv_fio?.text = dataStore.split(",")[9]
        holder.tv_rr?.text = dataStore.split(",")[10]
        holder.tv_ie?.text = "1 : "+dataStore.split(",")[11]
        holder.tv_tinsp?.text = dataStore.split(",")[12]
        holder.tv_texp?.text = dataStore.split(",")[13]
        holder.tv_leak?.text = dataStore.split(",")[14]
        holder.tv_spo2?.text = dataStore.split(",")[15]
        holder.tv_pr?.text = dataStore.split(",")[16]
    }


    override fun getItemCount(): Int {
        return list.size - 1
    }
    override fun getItemViewType(position: Int): Int {
        return position
    }


    class DFDViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var tv_date:TextView?=null
        var tv_time : TextView?= null
        var tv_pip : TextView?= null
        var tv_sp : TextView?= null
        var tv_peep : TextView?= null
        var tv_ma : TextView?= null
        var tv_vti : TextView?= null
        var tv_vte : TextView?= null
        var tv_mve : TextView?= null
        var tv_mvi : TextView?= null
        var tv_fio : TextView?= null
        var tv_rr : TextView?= null
        var tv_ie : TextView?= null
        var tv_tinsp : TextView?= null
        var tv_texp : TextView?= null
        var tv_leak : TextView?= null
        var tv_spo2 : TextView?= null
        var tv_pr : TextView?= null

        var lineone: View?=null
        var linetwo: View?=null
        var linethree: View?=null
        var linefour: View?=null
        var linefive: View?=null
        var linesix: View?=null
        var lineseven: View?=null
        var lineeight: View?=null
        var linenine: View?=null
        var lineten: View?=null
        var lineeleven: View?=null
        var linetwelve: View?=null
        var linethirteen: View?=null
        var linefourteen: View?=null
        var linefifteen: View?=null
        init {
            tv_date=view.findViewById(R.id.tv_date)
            tv_time = view.findViewById(R.id.tv_time)
            tv_pip = view.findViewById(R.id.tv_pip)
            tv_sp = view.findViewById(R.id.tv_sp)
            tv_peep = view.findViewById(R.id.tv_peep)
            tv_ma = view.findViewById(R.id.tv_ma)
            tv_vti = view.findViewById(R.id.tv_vti)
            tv_vte = view.findViewById(R.id.tv_vte)
            tv_mve = view.findViewById(R.id.tv_mve)
            tv_mvi = view.findViewById(R.id.tv_mvi)
            tv_fio = view.findViewById(R.id.tv_fio)
            tv_rr = view.findViewById(R.id.tv_rr)
            tv_ie = view.findViewById(R.id.tv_ie)
            tv_tinsp = view.findViewById(R.id.tv_tinsp)
            tv_texp = view.findViewById(R.id.tv_texp)
            tv_leak = view.findViewById(R.id.tv_leak)
            tv_spo2 = view.findViewById(R.id.tv_spo2)
            tv_pr = view.findViewById(R.id.tv_pr)

        }
    }
}