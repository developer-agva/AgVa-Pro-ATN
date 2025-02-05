package com.agvahealthcare.ventilator_ext.dashboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.model.AlarmModel
import com.agvahealthcare.ventilator_ext.utility.ToastFactory

class DropDownAdapter(
    private val actionViewDelegate: ViewActions,
    private val ackList: ArrayList<AlarmModel>, private val ctx:Context) : RecyclerView.Adapter<DropDownAdapter.StandViewHolder?>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StandViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_stand_drop_down, parent, false)
        return StandViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StandViewHolder, position: Int) {
        holder.bind(position)
    }


    inner class StandViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val standTitleTV: TextView
        private val standStatusTV: TextView
        fun bind(position: Int) {
            if (ackList.isNotEmpty()) {
                standTitleTV.text = actionViewDelegate?.getStandTitle(position)
                standStatusTV.text = actionViewDelegate?.getStandStatus(position)
                itemView.setSelected(actionViewDelegate?.selectedStand == position)
            }

        }

        private val standViewItemClickListener = View.OnClickListener {
           // ToastFactory.custom(ctx,"The onclick of the drop down is triggered")
            val lastSelectedPosition = actionViewDelegate.selectedStand
            actionViewDelegate.selectedStand = getAdapterPosition()
            notifyItemChanged(lastSelectedPosition)
            notifyItemChanged(getAdapterPosition())
            actionViewDelegate.collapseDropDown()
        }

        init {
            standTitleTV = itemView.findViewById<View>(R.id.cell_stand_title) as TextView
            standStatusTV = itemView.findViewById<View>(R.id.cell_stand_status) as TextView
            itemView.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    itemView.context,
                    R.drawable.stand_drop_down_selector
                )
            )

            if (ackList.isNotEmpty())
                itemView.setOnClickListener(standViewItemClickListener)
        }
    }

    interface ViewActions {
        fun collapseDropDown()
        fun getStandTitle(standId: Int): String?
        fun getStandStatus(standId: Int): String?
        var selectedStand: Int
    }

    override fun getItemCount(): Int {
        return ackList.size
    }


}
