package com.agvahealthcare.ventilator_ext.logs.event

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.uhidDataListEvent
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.databinding.FragmentEventsBinding
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import java.util.*


class EventsFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentEventsBinding
    private lateinit var mEventViewModel: EventViewModel
    private var dataList = ArrayList<String>()
    private var dashBoardViewModel: DashBoardViewModel? = null
    lateinit var mLayoutManager: LinearLayoutManager
    var mAdapter: EventAdapter? = null
    private var uhid = ""
    private var startIndex = 0
    private var endIndex = 9

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsBinding.inflate(layoutInflater,container,false)
        mEventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        dashBoardViewModel =
            ViewModelProvider(requireActivity()).get(DashBoardViewModel::class.java)

        return binding.root
    }

    private fun setupSpinnerAdapter(){

        if (uhidDataListEvent.size == 0) binding.btnLayout.visibility = View.GONE
        else binding.btnLayout.visibility = View.VISIBLE

        val eventScrollAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            VentilatorApp.uhidDataListEvent
        )

        binding.spinnerEvent.apply {
            adapter = eventScrollAdapter
            this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    uhid = uhidDataListEvent[position]
                    setupDataDefault(uhid)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }
    }


    override fun onResume() {
        super.onResume()

        dashBoardViewModel?.updateIsEventsFragmentVisible(true)
    }

    override fun onPause() {
        super.onPause()

        dashBoardViewModel?.updateIsEventsFragmentVisible(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("eventTesting", "event")
        binding.topButton.setOnClickListener(this)
        binding.bottomButton.setOnClickListener(this)

        setupSpinnerAdapter()
        setupDataDefault("")
    }

    override fun onClick(view: View) {

        val tempStartIndex = startIndex
        val tempEndIndex = endIndex

        when (view.id) {

            R.id.topButton -> {
                Log.i("value_check_events","top")
                mAdapter?.apply {
                    mLayoutManager.apply {
                        val firstVisibleItemIndex = findFirstVisibleItemPosition()
                        if (getSelection() == firstVisibleItemIndex) {
                            startIndex -= 9
                            endIndex -= 9
                            val data = FileLogger.readEventFile("event",startIndex,endIndex)
                            if (data != "Data Not Found"){
                                val listData = data.split("|") as ArrayList<String>
                                mAdapter?.updateDataList(listData,true)
                            }else {
                                startIndex = tempStartIndex
                                endIndex = tempEndIndex
                            }
                        }
                        setSelectionUpword()
                    }
                }
            }

            R.id.bottomButton -> {

                mAdapter?.apply {
                    mLayoutManager.apply {
                        val lastVisibleItemIndex = findLastCompletelyVisibleItemPosition()

                        Log.i("value_check_events","${getSelection()} - $lastVisibleItemIndex")


                        if (getSelection() != dataList.size - 1) {

                            if (getSelection() == lastVisibleItemIndex) {
                                startIndex += 9
                                endIndex += 9
                                val data = FileLogger.readEventFile("event",startIndex,endIndex)
                                if (data != "Data Not Found"){
                                    val listData = data.split("|") as ArrayList<String>
                                    mAdapter?.updateDataList(listData,false)
                                }
                                else {
                                    startIndex = tempStartIndex
                                    endIndex = tempEndIndex
                                }
                            }
                            setSelectionDownward()
                        }
                    }
                }
            }
        }
    }

    private fun setupDataDefault(uhid: String) {

        val data = FileLogger.readEventFile("event",startIndex,endIndex)
        if (data != "Data Not Found"){
            dataList = data.split("|") as ArrayList<String>
            setDataForEvents()
        }
    }

    fun scrollForward() = binding.bottomButton.callOnClick()
    fun scrollBack() = binding.topButton.callOnClick()

    private fun setDataForEvents() {
        mAdapter = EventAdapter(dataList)
        mLayoutManager = LinearLayoutManager(requireContext())

        binding.recyclerViewEvents.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            isVerticalScrollBarEnabled = true
            itemAnimator = null
        }
    }


}
