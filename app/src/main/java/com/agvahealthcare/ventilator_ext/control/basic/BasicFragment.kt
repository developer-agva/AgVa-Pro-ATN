package com.agvahealthcare.ventilator_ext.control.basic

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.utility.GridSpacingItemDecoration
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.fragment_basic.*


class BasicFragment(private var dataList : ArrayList<ControlParameterModel>, private  var controlParameterClickListener: ControlParameterClickListener?) : Fragment() {

    private var controlParamAdapter : ControlParameterAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_basic, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpAlarmsData()
    }

    fun getControlParameter() = controlParamAdapter?.getItems()

    private fun setUpAlarmsData() {
        controlParamAdapter = ControlParameterAdapter(requireContext(), dataList, controlParameterClickListener, Configs.ControlSettingType.BASIC)

        recyclerViewControls?.apply {
            layoutManager = object: GridLayoutManager(requireContext(), 6){
                override fun canScrollVertically(): Boolean = false
            }
            addItemDecoration(GridSpacingItemDecoration(6, 90))
            adapter = controlParamAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun notifyAdapter() = controlParamAdapter?.notifyDataSetChanged()


    @SuppressLint("NotifyDataSetChanged")
    fun dataList(filterVentParameterTiles: ArrayList<ControlParameterModel>) {
        dataList = filterVentParameterTiles
        controlParamAdapter?.apply{
            addFilterData(dataList)
            notifyDataSetChanged()
        }
    }
}