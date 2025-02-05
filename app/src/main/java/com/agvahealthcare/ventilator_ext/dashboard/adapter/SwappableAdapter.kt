package com.agvahealthcare.ventilator_ext.dashboard.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.callback.OnObserveValueSwapListener
import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel

class SwappableAdapter(
    private val mList: ArrayList<ObservedParameterModel>,
    private val selectedModel: ObservedParameterModel,
    private val onObserveValueSwapListener: OnObserveValueSwapListener?,
//    private val onObservePopUpRefress: OnObservePopUpRefress
) : RecyclerView.Adapter<SwappableAdapter.ViewHolder>() {


    private var swapSelectedIndex: Int = -1

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_observer_popup_layout, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val tileModel = mList[position]

        // sets the image to the imageview from our itemHolder class
        holder.textView.setOnClickListener {
            swap(tileModel)
        }
        if(tileModel.isSelectedAsSwappable){
            holder.linearLayout.setBackgroundResource(R.color.ack_green)
            holder.textView.setTextColor(Color.WHITE)
        }else{
            holder.linearLayout.setBackgroundResource(R.color.white)
            holder.textView.setTextColor(R.color.black)
        }
        // sets the text to the textview from our itemHolder class
        holder.textView.text = tileModel.label

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    fun swapWithCurrentSelection() = swap(mList[swapSelectedIndex])

    private fun swap(tileModel: ObservedParameterModel) = onObserveValueSwapListener?.onSwap(selectedModel, tileModel)


    @SuppressLint("NotifyDataSetChanged")
    fun setSelection(pos: Int){
        if(pos in 0 until itemCount) {

            mList.apply {
                forEach { it.isSelectedAsSwappable = false }
                this[pos].isSelectedAsSwappable = true;
                swapSelectedIndex = pos
            }

            notifyDataSetChanged()
        }
    }

    fun getSelection() = swapSelectedIndex

    fun setSelectionForward() = setSelection(swapSelectedIndex + 1)
    fun setSelectionBackward() = setSelection(swapSelectedIndex - 1)

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {

        val textView: TextView = itemView.findViewById(R.id.titleView)
        val linearLayout: LinearLayoutCompat = itemView.findViewById(R.id.linearLayout)
    }
}
