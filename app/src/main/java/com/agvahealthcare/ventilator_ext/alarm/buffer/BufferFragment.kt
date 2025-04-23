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
import com.agvahealthcare.ventilator_ext.dashboard.adapter.PrimaryActionAdapter
import com.agvahealthcare.ventilator_ext.databinding.FragmentBufferBinding
import com.agvahealthcare.ventilator_ext.model.AlarmModel
import com.agvahealthcare.ventilator_ext.utility.LOG_TYPE_INFO
import java.util.PriorityQueue

class BufferFragment : Fragment() {
    private var ackList:ArrayList<AlarmModel> = arrayListOf()
    private var bufferAdapter:BufferAlarmRecyclerAdapter?=null
    private var dashBoardViewModel: DashBoardViewModel?=null
    private lateinit var binding : FragmentBufferBinding

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
        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]

        binding = FragmentBufferBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.includeButtonReset.buttonView.text= this@BufferFragment.getString(R.string.hint_reset)
        binding.includeButtonReset.buttonView.setBackgroundResource(R.color.trans_grey)

        bufferAdapter = BufferAlarmRecyclerAdapter(ArrayList())
        binding.rvAlarms.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bufferAdapter
        }
        dashBoardViewModel?.alarms?.observe(viewLifecycleOwner) {
            Log.i("buffer_reset_alarm",it.toString())
            it?.let { it1 ->
                bufferAdapter?.updateList(ArrayList(it1))
            }
        }
        binding.includeButtonReset.buttonView.setOnClickListener {
            dashBoardViewModel?.alarms?.value = PriorityQueue<AlarmModel>()
        }
    }
}
