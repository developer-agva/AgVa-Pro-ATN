package com.agvahealthcare.ventilator_ext.logs.trends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.model.DataStoreModel

class ContainerOfNested(ctx:Context,list: ArrayList<ArrayList<DataStoreModel>>) : RecyclerView.Adapter<ContainerOfNested.ContainerViewHolder>() {
    var context:Context = ctx
    var list:ArrayList<ArrayList<DataStoreModel>> = list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContainerViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.parameter_units_unnesteditem,parent,false)
        return ContainerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContainerViewHolder, position: Int) {
        var currentItem= list?.get(position)
       /* var dataFromDataBaseAdapter=DataFromDataBaseAdapter(context, currentItem,)
        holder.rvUnNested?.apply {
            layoutManager = LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL,false)
            adapter=dataFromDataBaseAdapter
        }*/
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }



    override fun getItemCount(): Int {
        return list.size
    }

    class ContainerViewHolder(view : View) : RecyclerView.ViewHolder(view){
        var rvUnNested:RecyclerView?=null
        init {
            rvUnNested=view.findViewById(R.id.rv_unnested)
        }
    }

}