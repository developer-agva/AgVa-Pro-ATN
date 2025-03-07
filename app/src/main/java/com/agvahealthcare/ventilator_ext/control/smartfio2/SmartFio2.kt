package com.agvahealthcare.ventilator_ext.control.smartfio2

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterAdapter
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterClickListener
import com.agvahealthcare.ventilator_ext.databinding.FragmentSmartFio2Binding
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.standby.StandbyControlSettingFragment
import com.agvahealthcare.ventilator_ext.utility.GridSpacingItemDecoration
import com.agvahealthcare.ventilator_ext.utility.utils.Configs



class SmartFio2(private var dataList:MutableList<ControlParameterModel>, private var controlParamClickListener: ControlParameterClickListener?=null): StandbyControlSettingFragment(){

    private var controlParamsAdapter: ControlParameterAdapter? = null
    private var prefManager: PreferenceManager? = null
    private var mMainActivityViewModel : MainActivityViewModel? = null
    private lateinit var binding : FragmentSmartFio2Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSmartFio2Binding.inflate(layoutInflater,container,false)
        return binding.root
    }

    // knob highlight logic starts here

    @SuppressLint("NotifyDataSetChanged")
    fun highlightAdapterPosition(highlightedIndex : Int) {
        controlParamsAdapter?.selectedIndex = highlightedIndex
        controlParamsAdapter?.notifyDataSetChanged()
    }

    fun handleClick(highlightedIndex: Int) {
        if (highlightedIndex == 0) {
            if (VentilatorApp.isFromControlFragment == true) controlParamClickListener?.onStateChange(
                !(prefManager?.readSmartFiO2Status()!!),
                Configs.ControlSettingType.SmartFio2,
                highlightedIndex
            )
            else controlParamClickListener?.onStateChange(
                !(prefManager?.readSmartFiO2StatusTemp()!!),
                Configs.ControlSettingType.SmartFio2,
                highlightedIndex
            )
            notifyAdapter()
        } else {
            if (highlightedIndex != -1) controlParamClickListener?.onClick(highlightedIndex-1, dataList[highlightedIndex-1])
        }
    }

    // knob highlight logic ends here

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefManager = PreferenceManager(requireContext())

        mMainActivityViewModel = ViewModelProvider(requireActivity()).get(MainActivityViewModel::class.java)

        observePipValue()

        setControlParameters()
    }

    private fun observePipValue(){
        mMainActivityViewModel?.twoTileResponse?.observe(viewLifecycleOwner){

            // prefManager?.setPipViaVGV(it)

            for (i in dataList){
                if (i.title == "Plimit"){
                    i.lowerLimit = it.toDouble()
                    controlParamsAdapter?.notifyDataSetChanged()
                }
            }
        }
    }


    override fun getControlParameters() = controlParamsAdapter?.getItems()

    override fun setControlParameters() {
        controlParamsAdapter =
            ControlParameterAdapter(
                requireContext(),
                dataList as ArrayList<ControlParameterModel>,
                controlParamClickListener,
                Configs.ControlSettingType.SmartFio2
            )

        binding.recyclerViewsmartFiO2.apply {
            layoutManager = object: GridLayoutManager(requireContext(), 6){
                override fun canScrollVertically(): Boolean = false
            }
            addItemDecoration(GridSpacingItemDecoration(6, 90))
            adapter = controlParamsAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")

    override fun notifyAdapter() = controlParamsAdapter?.notifyDataSetChanged()


    @SuppressLint("NotifyDataSetChanged")
    override fun dataList(filterVentParameterTiles: ArrayList<ControlParameterModel>) {
        dataList = filterVentParameterTiles
        controlParamsAdapter?.apply{
            addFilterData(dataList as ArrayList<ControlParameterModel>)
            notifyDataSetChanged()
        }
    }

    override fun notifyItemChangedAdapter(position: Int) = controlParamsAdapter?.notifyItemChanged(position)
}

