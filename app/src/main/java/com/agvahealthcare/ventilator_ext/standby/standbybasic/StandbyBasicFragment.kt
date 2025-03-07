package com.agvahealthcare.ventilator_ext.standby

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterAdapter
import com.agvahealthcare.ventilator_ext.control.basic.ControlParameterClickListener
import com.agvahealthcare.ventilator_ext.databinding.FragmentStandbybasicBinding
import com.agvahealthcare.ventilator_ext.model.ControlParameterModel
import com.agvahealthcare.ventilator_ext.utility.GridSpacingItemDecoration
import com.agvahealthcare.ventilator_ext.utility.utils.Configs


class StandbyBasicFragment(
    private var dataList: ArrayList<ControlParameterModel>,
    private var controlParameterClickListener: ControlParameterClickListener?
) : StandbyControlSettingFragment() {

    private var controlParamAdapter: ControlParameterAdapter? = null

    // knob highlight logic starts here
    @SuppressLint("NotifyDataSetChanged")
    fun highlightAdapterPosition(highlightedIndex: Int) {
        controlParamAdapter?.selectedIndex = highlightedIndex
        controlParamAdapter?.notifyDataSetChanged()
    }

    fun handleClick(highlightedIndex: Int) {
        if (highlightedIndex != -1) controlParameterClickListener?.onClick(highlightedIndex, dataList[highlightedIndex])
    }
    // knob highlight logic ends here


    private lateinit var binding : FragmentStandbybasicBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentStandbybasicBinding.inflate(layoutInflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setControlParameters()
    }

    override fun getControlParameters() = controlParamAdapter?.getItems()

    override fun setControlParameters() {
        controlParamAdapter = ControlParameterAdapter(
            requireContext(),
            dataList,
            controlParameterClickListener,
            Configs.ControlSettingType.BASIC
        )
        (binding.recyclerViewControls.itemAnimator as SimpleItemAnimator).supportsChangeAnimations =
            false
        binding.recyclerViewControls.apply {
            layoutManager = object : GridLayoutManager(requireContext(), 6) {
                override fun canScrollVertically(): Boolean = false
            }
            addItemDecoration(GridSpacingItemDecoration(6, 90))
            adapter = controlParamAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun notifyAdapter() = controlParamAdapter?.notifyDataSetChanged()

    @SuppressLint("NotifyDataSetChanged")
    override fun dataList(filterVentParameterTiles: ArrayList<ControlParameterModel>) {
        dataList = filterVentParameterTiles
        controlParamAdapter?.apply {
            addFilterData(dataList)
            notifyDataSetChanged()
        }
    }

    override fun notifyItemChangedAdapter(position: Int) = controlParamAdapter?.notifyItemChanged(position)
}