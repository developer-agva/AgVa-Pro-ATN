package com.agvahealthcare.ventilator_ext.dashboard.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity

class PrimaryActionAdapter(
    private val ctx: Context,
    private val mList: ArrayList<ActionDataModel>,
    private val clickListener: PrimaryActionClickListener
):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    interface PrimaryActionClickListener {
        fun onClick(position: Int)
    }

    companion object {
        const val TEXT_TYPE = 1
        const val IMAGE_TYPE = 2
    }

    var selectedIndex = -1

    private var chargingIcon = R.drawable.ic_plug_out_rotate
    private var batteryIcon = R.drawable.ic_battery_full_updated
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            TEXT_TYPE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.action_item_text, parent, false)
                TextViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.action_item, parent, false)
                ImageViewHolder(view)
            }
        }


    }

    fun updateBatteryData(connectionStatus: Int, batteryInfo : Int){
        chargingIcon =  connectionStatus
        batteryIcon = batteryInfo
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
       return mList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (mList[position]) {
            is ActionDataModel.ActionModelText -> TEXT_TYPE
            is ActionDataModel.ActionModel -> IMAGE_TYPE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val actionModel = mList[position]

        when (actionModel) {
            is ActionDataModel.ActionModelText -> {
                val viewHolder = holder as TextViewHolder

                viewHolder.bind(actionModel.text)

                viewHolder.itemView.setOnClickListener {
                    clickListener.onClick(holder.bindingAdapterPosition)
                }

                if (selectedIndex == position){
                    holder.mainLayoutPanel.setBackgroundResource(R.drawable.background_black_border_yellow)
                }else{
                    holder.mainLayoutPanel.setBackgroundColor(Color.BLACK)
                }

            }
            is ActionDataModel.ActionModel -> {
                val viewHolder = holder as ImageViewHolder

                viewHolder.mainLayoutPanel.setBackgroundResource(R.color.button_grey_background)

                if (position == 8){
                    if (chargingIcon == R.drawable.ic_plug_out_rotate) viewHolder.mainLayoutPanel.setBackgroundResource(R.color.red)
                    else viewHolder.mainLayoutPanel.setBackgroundResource(R.color.button_grey_background)
                    viewHolder.imageView.setImageResource(chargingIcon)
                }
                else if (position == 7) viewHolder.imageView.setImageResource(batteryIcon)
                else viewHolder.imageView.setImageResource(actionModel.image)

                viewHolder.itemView.setOnClickListener {
                    clickListener.onClick(holder.bindingAdapterPosition)
                }

                if (selectedIndex == position){
                    holder.mainLayoutPanel.setBackgroundResource(R.drawable.background_black_border_yellow)
                }else{

                    if (position == 9 && (ctx as DashBoardActivity).isLocked){
                        viewHolder.mainLayoutPanel.setBackgroundResource(R.color.preCalib_amber)
                        viewHolder.imageView.setImageResource(R.drawable.ic_lock_black)
                    }else if (position == 9 && !(ctx as DashBoardActivity).isLocked){
                        viewHolder.mainLayoutPanel.setBackgroundResource(R.color.black)
                        viewHolder.imageView.setImageResource(R.drawable.ic_unlocked_icon)
                    }
                    else holder.mainLayoutPanel.setBackgroundColor(Color.BLACK)
                }

            }
        }

    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.ivItem)
        val mainLayoutPanel: ConstraintLayout = itemView.findViewById(R.id.mainLayoutPanel)
    }

    inner class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_Label)
        val mainLayoutPanel: ConstraintLayout = itemView.findViewById(R.id.mainLayoutPanel)
        fun bind(text: String) {
            textView.text = text
        }
    }


}