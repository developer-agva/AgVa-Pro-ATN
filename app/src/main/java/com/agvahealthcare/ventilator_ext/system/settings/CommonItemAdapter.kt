package com.agvahealthcare.ventilator_ext.system.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import de.hdodenhof.circleimageview.CircleImageView


interface onDropDownSelectionListener {
    fun onItemSelect(text: String,colorInt: Int )
}

interface onTrendDropDownSelectionListener {
    fun onTrendItemSelect(text: String)
}

data class CommonItemData(
    var colorInt : Int,
    var text: String
)

class CommonTrendConfigsAdapter(private var dataList: ArrayList<String>, private var onClick : onTrendDropDownSelectionListener, private var isTrendParam:Boolean) : RecyclerView.Adapter<CommonTrendConfigsAdapter.CommonTrendConfigsViewHolder>() {

    var selectedIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonTrendConfigsViewHolder {
        return if (isTrendParam){
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.setup_select_layout, parent, false)
            CommonTrendConfigsViewHolder(itemView,isTrendParam)
        }else{
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.setup_select_layout, parent, false)
            CommonTrendConfigsViewHolder(itemView,isTrendParam)
        }
    }

    override fun onBindViewHolder(holder: CommonTrendConfigsViewHolder, position: Int) {
        val data = dataList[position]

        if (selectedIndex == position) holder.itemLayout?.setBackgroundResource(R.drawable.background_grey_border_yellow)
        else holder.itemLayout?.setBackgroundResource(R.color.dark_grey)

        holder.itemLayout?.setOnClickListener {
            onClick.onTrendItemSelect("")
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun forwardIndex(){
        if (selectedIndex+1 < dataList.size) selectedIndex++
        else selectedIndex = 0
        notifyDataSetChanged()
    }

    fun backwardIndex(){
        if (selectedIndex > 0) selectedIndex--
        else selectedIndex = dataList.size-1
        notifyDataSetChanged()
    }

    fun clickOnSelectedIndex(){
        if (selectedIndex != -1) onClick.onTrendItemSelect("")
    }

    class CommonTrendConfigsViewHolder (view: View,private var isTrendParam: Boolean) : RecyclerView.ViewHolder(view){

        var itemLayout: ConstraintLayout? = null
        var txtDropDownItem: TextView? = null
        var cbLayout: ConstraintLayout? = null
        init {

            itemLayout = view.findViewById(R.id.itemLayout)
            txtDropDownItem = view.findViewById(R.id.txtColorText)
            cbLayout = view.findViewById(R.id.cbLayout)
        }

    }
}


class CommonDropDownAdapter(private var context: Context,private var dataList: ArrayList<CommonItemData>, private var onClick : onDropDownSelectionListener) : RecyclerView.Adapter<CommonDropDownAdapter.CommonDropDownViewHolder>() {


    var selectedIndex = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonDropDownViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.color_select_layout, parent, false)
        return CommonDropDownViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommonDropDownViewHolder, position: Int) {
        val data = dataList[position]

        holder.txtDropDownItem?.text = data.text
        holder.cbLayout?.setBackgroundColor(data.colorInt)

        if (selectedIndex == position) holder.itemLayout?.setBackgroundResource(R.drawable.background_grey_border_yellow)
        else holder.itemLayout?.setBackgroundResource(R.color.dark_grey)

        holder.itemLayout?.setOnClickListener {
            onClick.onItemSelect(data.text,data.colorInt)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun forwardIndex(){
        if (selectedIndex+1 < dataList.size) selectedIndex++
        else selectedIndex = 0
        notifyDataSetChanged()
    }

    fun backwardIndex(){
        if (selectedIndex > 0) selectedIndex--
        else selectedIndex = dataList.size-1
        notifyDataSetChanged()
    }

    fun clickOnSelectedIndex(){
        if (selectedIndex != -1) onClick.onItemSelect(dataList[selectedIndex].text,dataList[selectedIndex].colorInt)
    }

    class CommonDropDownViewHolder (view: View) : RecyclerView.ViewHolder(view){
        var itemLayout: ConstraintLayout? = null
        var txtDropDownItem: TextView? = null
        var cbLayout: ConstraintLayout? = null
        init {

            itemLayout = view.findViewById(R.id.itemLayout)
            txtDropDownItem = view.findViewById(R.id.txtColorText)
            cbLayout = view.findViewById(R.id.cbLayout)
        }

    }
}

class CommonSetupAdapter(private var dataList: ArrayList<String>, private var onClick : onDropDownSelectionListener) : RecyclerView.Adapter<CommonSetupAdapter.CommonSetupViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonSetupViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.setup_select_layout, parent, false)
        return CommonSetupViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CommonSetupViewHolder, position: Int) {
        val data = dataList[position]

        holder.txtDropDownItem?.text = data

        holder.itemLayout?.setOnClickListener {
            onClick.onItemSelect(data,0)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    inner class CommonSetupViewHolder (view: View) : RecyclerView.ViewHolder(view){
        var itemLayout: ConstraintLayout? = null
        var txtDropDownItem: TextView? = null
        init {

            itemLayout = view.findViewById(R.id.itemLayout)
            txtDropDownItem = view.findViewById(R.id.txtColorText)
        }

    }
}
