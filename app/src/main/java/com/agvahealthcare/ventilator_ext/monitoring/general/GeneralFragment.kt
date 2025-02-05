package com.agvahealthcare.ventilator_ext.monitoring.general

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel
import kotlinx.android.synthetic.main.fragment_general.*

class GeneralFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_general, container, false)
    }

    private var observedList: ArrayList<ObservedParameterModel>? = null
    private var mAdapter : ObservedParameterAdapter ? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observedList = arguments?.getSerializable("observedList") as ArrayList<ObservedParameterModel>
        setUpGeneralData()
    }

    // RecyclerView Data Setup
    private fun setUpGeneralData() {
        mAdapter = ObservedParameterAdapter(observedList)
        recyclerViewGeneral?.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = mAdapter
        }
    }

    fun setUpModeData(observedValueList: ArrayList<ObservedParameterModel>) {
        observedList = observedValueList
        mAdapter?.setModelList(observedList)
        mAdapter?.notifyDataSetChanged()
    }
}