package com.agvahealthcare.ventilator_ext.dashboard.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterClickListener
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.utility.utils.Configs


class PrimaryControlParameterAdapter(
    private val ctx: Context,
    private val parameters: ArrayList<ControlParameterModel>,
    private var primaryControlParamClickListener: ControlParameterClickListener? = null,
    ) : RecyclerView.Adapter<PrimaryControlParameterAdapter.VHPrimaryControlParameterAdapter>() {
    val preferenceManager = PreferenceManager(ctx)
    companion object {
        @JvmStatic
        val MARGIN_END = 4

    }

    var selectedIndex = -1
//    var tiles : ArrayList<ControlParameterModel> = ArrayList(parameterTiles.subList(0, Configs.PARAMETER_TILE_COUNT))


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int, ): VHPrimaryControlParameterAdapter {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_control_parameter, parent, false)

        val totalMarginSpace = ((this.itemCount - 2) * MARGIN_END);
        itemView.layoutParams = RecyclerView.LayoutParams(
            (parent.width - totalMarginSpace) / this.itemCount,
            parent.height
        )

        return VHPrimaryControlParameterAdapter(itemView)
    }

    override fun onBindViewHolder(holder: VHPrimaryControlParameterAdapter, position: Int) {
        val tile = parameters[position]

        if (position < itemCount - 1) {
            (holder.itemView.layoutParams as? RecyclerView.LayoutParams)?.let {
                holder.itemView.layoutParams = it.apply { setMargins(0, 0, MARGIN_END, 0) }
                Log.i("MARGINCHECK", "Applying margin on position = $position")
            }
        }

        if (VentilatorApp.fio2ChangeFlag == true)
        {
            if (tile.ventKey == Configs.LBL_FIO2){
                Log.i("CHECK_HERE_COLOR", "COLOR CHECKED")
                holder.mainLayoutPanel?.setBackgroundResource(R.color.teal_700)
                holder.tvValue?.setTextColor(Color.WHITE)
                holder.tvReading?.setTextColor(Color.WHITE)
                holder.tvUnit?.setTextColor(Color.WHITE)
            }

            else{

                setSelection(holder,tile.isIsselected,position)
            }

        }else{
            Log.i("CHECK_HERE_COLOR", "COLOR CHECKED1")
            setSelection(holder,tile.isIsselected,position)
        }

        // set style
//        setSelection(holder, tile.isIsselected)
        holder.itemView.setOnClickListener {
            if (tile.isVoid) primaryControlParamClickListener?.onClick(position, tile)
        }

        // bind data
        if (tile.ventKey.equals(Configs.LBL_TINSP) || tile.ventKey.equals(Configs.LBL_TLOW)) {
            try{
                val read: Float = tile.reading.toFloat()
                holder.tvReading?.text = String.format("%.1f", read)

                if (tile.ventKey.equals(Configs.LBL_TINSP) && preferenceManager!!.readIETileStatus()){
                    holder.tvReading?.text = Configs.calculateIERatio(
                        VentilatorApp.testingConditonMap[Configs.LBL_RR]?.let {
                            it.toInt()
                        } ?: kotlin.run {
                            preferenceManager.readRR().toInt()
                        },
                        VentilatorApp.testingConditonMap[Configs.LBL_TINSP]?.let {
                            it
                        } ?: kotlin.run {
                            tile.reading.toFloat()
                        }
                    )
                }
            } catch (e: Exception){
                holder.tvReading?.text = tile.reading
                e.printStackTrace()
            }

        } else {
            try{
                val read: Int = tile.reading.toInt()
                holder.tvReading?.text = read.toString()
            } catch (e: Exception){
                holder.tvReading?.text = tile.reading
                e.printStackTrace()
            }
        }
        holder.tvValue?.text = tile.title
        holder.tvUnit?.text = tile.units

        if (tile.ventKey.equals(Configs.LBL_TINSP) && preferenceManager!!.readIETileStatus()){
            holder.tvValue?.text = Configs.LBL_IE_RATIO
            holder.tvUnit?.text = ""
        }
    }


    private fun setSelection(holder: VHPrimaryControlParameterAdapter, isSelected: Boolean,position: Int){

        if (selectedIndex == position){
            if (isSelected) {
                holder.mainLayoutPanel?.setBackgroundResource(R.drawable.background_white_border_yellow)
                holder.tvValue?.setTextColor(Color.BLACK)
                holder.tvReading?.setTextColor(Color.BLACK)
                holder.tvUnit?.setTextColor(Color.BLACK)
            } else {
                holder.mainLayoutPanel?.setBackgroundResource(R.drawable.background_black_border_yellow)
                holder.tvValue?.setTextColor(Color.WHITE)
                holder.tvReading?.setTextColor(Color.WHITE)
                holder.tvUnit?.setTextColor(Color.WHITE)
            }
        }else {
            if (isSelected) {
                holder.mainLayoutPanel?.setBackgroundColor(Color.WHITE)
                holder.tvValue?.setTextColor(Color.BLACK)
                holder.tvReading?.setTextColor(Color.BLACK)
                holder.tvUnit?.setTextColor(Color.BLACK)
            } else {
                holder.mainLayoutPanel?.setBackgroundColor(Color.BLACK)
                holder.tvValue?.setTextColor(Color.WHITE)
                holder.tvReading?.setTextColor(Color.WHITE)
                holder.tvUnit?.setTextColor(Color.WHITE)
            }
        }
    }

    override fun getItemCount(): Int {
        return parameters.size
    }

    class VHPrimaryControlParameterAdapter(view: View) : RecyclerView.ViewHolder(view) {

        var tvValue: AppCompatTextView? = null
        var tvUnit: AppCompatTextView? = null
        var tvReading: AppCompatTextView? = null
        var mainLayoutPanel: ConstraintLayout?=null

        init {
            tvValue = view.findViewById(R.id.textViewValue)
            tvReading = view.findViewById(R.id.textViewReading)
            tvUnit = view.findViewById(R.id.textViewUnit)
            mainLayoutPanel=view.findViewById(R.id.mainLayoutPanel)
        }


    }

}