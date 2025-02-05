package com.agvahealthcare.ventilator_ext.logs.event

import androidx.annotation.Nullable
import androidx.recyclerview.widget.DiffUtil
import com.agvahealthcare.ventilator_ext.database.entities.AlarmDBModel
import com.agvahealthcare.ventilator_ext.database.entities.EventDataModel

class EventDiffUtils(var oldList :ArrayList<EventDataModel>, var newList: ArrayList<EventDataModel>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    @Nullable
    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        // Implement method if you're going to use ItemAnimator
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}