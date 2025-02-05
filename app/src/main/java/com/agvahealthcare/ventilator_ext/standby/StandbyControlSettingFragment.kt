package com.agvahealthcare.ventilator_ext.standby

import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel

abstract class StandbyControlSettingFragment : Fragment() {

    abstract fun notifyAdapter() : Unit?;
    abstract fun setControlParameters();
    abstract fun getControlParameters() : ArrayList<ControlParameterModel>?;
    abstract fun dataList(filterVentParameterTiles: ArrayList<ControlParameterModel>);
    abstract fun notifyItemChangedAdapter(position: Int): Unit?;
}