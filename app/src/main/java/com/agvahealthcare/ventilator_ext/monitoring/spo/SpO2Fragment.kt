package com.agvahealthcare.ventilator_ext.monitoring.spo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel
import com.agvahealthcare.ventilator_ext.monitoring.general.ObservedParameterAdapter
import kotlinx.android.synthetic.main.fragment_spo.*

class SpO2Fragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_spo, container, false)

    }

    private var valueList: ArrayList<ObservedParameterModel>? = null
    private var mAdapter : ObservedParameterAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        valueList = arguments?.getSerializable("observedSpHList") as ArrayList<ObservedParameterModel>
        setUpSpo2Data()
    }


    private fun setUpSpo2Data() {
        mAdapter = ObservedParameterAdapter(valueList)
        recyclerViewSpO.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = mAdapter
        }

    }

    fun setData(dataList: ArrayList<ObservedParameterModel>) {
        valueList = dataList
        mAdapter?.setModelList(valueList)
        mAdapter?.notifyDataSetChanged()
    }


}