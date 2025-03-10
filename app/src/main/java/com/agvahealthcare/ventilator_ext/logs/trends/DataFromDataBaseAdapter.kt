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

class DataFromDataBaseAdapter(private var dataList:ArrayList<String>) : RecyclerView.Adapter<DataFromDataBaseAdapter.DFDViewHolder>() {

    private var selectedIndex : Int = 0

    fun updateDataList(newList: ArrayList<String>){
        dataList.clear()
        val paramList = "Parameter,Mode,PIP,PEEP,Mean Airway,Vti,Vte,MVe,MVi,FiO₂,RR,I:E,Tinsp,Texp,Average Leak,Spo2,PR,Dyn Comp.,Spont VT,Spont RR"
        val unitList = "Unit,Mode Type,cmH₂O,cmH₂O,cmH₂O,mL,mL,litre,litre,%,BPM,Ratio,sec,sec,%,%,BPM,mL/cmH₂O,mL,BPM"

        dataList.add(paramList)
        dataList.add(unitList)
        dataList.addAll(newList)

        selectedIndex = 0
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DFDViewHolder {
        Log.i("tetingmasoom","4")
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.parameter_units_itemtwo,parent,false)
        return DFDViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DFDViewHolder, position: Int) {
        val dataStore = dataList[position]

        if (position >= 2) {
//            holder.tv_time?.text = dataStore.split(",")[0].split(" ")[1]
            holder.tv_pip?.text = dataStore.split(",")[1]
            holder.tv_sp?.text = dataStore.split(",")[2]
            holder.tv_peep?.text = dataStore.split(",")[3]
            holder.tv_ma?.text = dataStore.split(",")[4]
            holder.tv_vti?.text = dataStore.split(",")[5]
            holder.tv_vte?.text = dataStore.split(",")[6]
            holder.tv_mve?.text = dataStore.split(",")[7]
            holder.tv_mvi?.text = dataStore.split(",")[8]
            holder.tv_fio?.text = dataStore.split(",")[9]
            holder.tv_rr?.text = dataStore.split(",")[10]
            holder.tv_ie?.text = "1 : " + dataStore.split(",")[11]
            holder.tv_tinsp?.text = dataStore.split(",")[12]
            holder.tv_texp?.text = dataStore.split(",")[13]
            holder.tv_leak?.text = dataStore.split(",")[14]
            holder.tv_spo2?.text = dataStore.split(",")[15]
            holder.tv_pr?.text = dataStore.split(",")[16]
            holder.tv_dynamic_compliance?.text = dataStore.split(",")[17]
            holder.tv_spont_rr?.text = dataStore.split(",")[18]
            holder.tv_spont_vt?.text = dataStore.split(",")[19]
        }
        else{
//            holder.tv_time?.text = dataStore.split(",")[0]
            holder.tv_pip?.text = dataStore.split(",")[1]
            holder.tv_sp?.text = dataStore.split(",")[2]
            holder.tv_peep?.text = dataStore.split(",")[3]
            holder.tv_ma?.text = dataStore.split(",")[4]
            holder.tv_vti?.text = dataStore.split(",")[5]
            holder.tv_vte?.text = dataStore.split(",")[6]
            holder.tv_mve?.text = dataStore.split(",")[7]
            holder.tv_mvi?.text = dataStore.split(",")[8]
            holder.tv_fio?.text = dataStore.split(",")[9]
            holder.tv_rr?.text = dataStore.split(",")[10]
            holder.tv_ie?.text = dataStore.split(",")[11]
            holder.tv_tinsp?.text = dataStore.split(",")[12]
            holder.tv_texp?.text = dataStore.split(",")[13]
            holder.tv_leak?.text = dataStore.split(",")[14]
            holder.tv_spo2?.text = dataStore.split(",")[15]
            holder.tv_pr?.text = dataStore.split(",")[16]
            holder.tv_dynamic_compliance?.text = dataStore.split(",")[17]
            holder.tv_spont_rr?.text = dataStore.split(",")[18]
            holder.tv_spont_vt?.text = dataStore.split(",")[19]
        }
    }


    override fun getItemCount(): Int {
        return if (dataList.size > 2) dataList.size - 1 else dataList.size
    }
    override fun getItemViewType(position: Int): Int {
        return position
    }

    class DFDViewHolder(view : View) : RecyclerView.ViewHolder(view){
        //        var tv_time : TextView?= null
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
        var tv_dynamic_compliance : TextView?= null
        var tv_spont_vt : TextView?= null
        var tv_spont_rr : TextView?= null
        init {
//            tv_date=view.findViewById(R.id.tv_date)
//            tv_time = view.findViewById(R.id.tv_time)
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
            tv_spont_vt = view.findViewById(R.id.tv_spont_vt)
            tv_spont_rr = view.findViewById(R.id.tv_spont_rr)
            tv_dynamic_compliance = view.findViewById(R.id.tv_dynamic_compliance)
        }
    }
}