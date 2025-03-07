package com.agvahealthcare.ventilator_ext.monitoring.general

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.agvahealthcare.ventilator_ext.databinding.FragmentGeneralBinding
import com.agvahealthcare.ventilator_ext.model.ObservedParameterModel

class GeneralFragment : Fragment(){

    private lateinit var binding:FragmentGeneralBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGeneralBinding.inflate(layoutInflater,container,false)
        return binding.root
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
        binding.recyclerViewGeneral.apply {
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