package com.agvahealthcare.ventilator_ext.alarm.buffer

import android.os.Bundle


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.dashboard.BufferAlarmRecyclerAdapter
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.model.AlarmModel
import kotlinx.android.synthetic.main.content_button_layout.view.*
import kotlinx.android.synthetic.main.fragment_buffer.*

class BufferFragment : Fragment() {
    private var ackList:ArrayList<AlarmModel> = arrayListOf()
    private var bufferAdapter:BufferAlarmRecyclerAdapter?=null
    private var dashBoardViewModel: DashBoardViewModel?=null
    companion object{

        fun newInstance():BufferFragment{
            val args=Bundle()
            val fragment=BufferFragment()
            fragment.arguments=args
            return fragment
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("ACTIVITY_LIFECYCLE", "ON_CREATE_VIEW $javaClass")
        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]

        return inflater.inflate(R.layout.fragment_buffer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        includeButtonReset?.buttonView?.text= this@BufferFragment.getString(R.string.hint_reset)
        includeButtonReset?.buttonView?.setBackgroundResource(R.color.trans_grey)

        setupClickListener()
        dashBoardViewModel?.alarms?.observe(viewLifecycleOwner) {
            bufferAdapter = BufferAlarmRecyclerAdapter(ArrayList(it))
            rvAlarms.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = bufferAdapter
            }
        }
        /*dashBoardViewModel?.arrayListTemp?.observe(viewLifecycleOwner) {
            bufferAdapter = BufferAlarmRecyclerAdapter(ArrayList(it))
            rvAlarms.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = bufferAdapter
            }
        }*/
    }

    private fun setupClickListener() {
        //includeButtonDefaults.buttonView.text = getString(R.string.hint_auto)
        includeButtonReset?.buttonView?.setOnClickListener {
           // notifyBufferAlarmAdapter()
            dashBoardViewModel?.alarms?.value?.clear()
            notifyBufferAlarmAdapter()
        }
    }

    private fun updateList(ackList: ArrayList<AlarmModel>){
        this.ackList = ackList
        notifyBufferAlarmAdapter()
    }

    fun notifyBufferAlarmAdapter() = bufferAdapter?.notifyDataSetChanged()

}
