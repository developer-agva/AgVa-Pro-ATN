package com.agvahealthcare.ventilator_ext.logs.alarm

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
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.uhidDataListAlarm
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import kotlinx.android.synthetic.main.fragment_events.*
import kotlinx.android.synthetic.main.fragment_logs_alarm.*


class AlarmFragment : Fragment(), View.OnClickListener {
    private lateinit var mAlarmViewModel: AlarmViewModel
    private lateinit var buttonLayout: LinearLayoutCompat
    private var dashBoardViewModel: DashBoardViewModel? = null
    private var mAdapter: AlarmAdapter? = null
    lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var spinnerAlarm: Spinner
    private var dataList = ArrayList<String>()
    private var uhid = ""
    private var startIndex = 0
    private var endIndex = 9

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_logs_alarm, container, false)

        return view
    }

    override fun onPause() {
        super.onPause()
        dashBoardViewModel?.updateIsAlarmsFragmentVisible(false)
    }

    override fun onResume() {
        super.onResume()
        dashBoardViewModel?.updateIsAlarmsFragmentVisible(true)
    }

    private fun setupAdapter(){

        if (VentilatorApp.uhidDataListAlarm.size == 0) buttonLayout.visibility = View.GONE
        else buttonLayout.visibility = View.VISIBLE

        val alarmScrollAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, VentilatorApp.uhidDataListAlarm)
        spinnerAlarm.apply {
            adapter = alarmScrollAdapter
            this.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    uhid = uhidDataListAlarm[position]
                    setupDataDefault(uhid)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }
    }


    private fun setupDataDefault(uhid: String) {

        val data = FileLogger.readAlarmFile("alarm",startIndex,endIndex)
        if (data != "Data Not Found"){
            dataList = data.split("|") as ArrayList<String>
            setUpAlarmsData()
        }
    }



    override fun onClick(view: View) {

        val tempStartIndex = startIndex
        val tempEndIndex = endIndex

        when (view.id) {

            R.id.topButtonAlarm -> {
                Log.i("value_check_events","top")
                mAdapter?.apply {
                    mLayoutManager.apply {
                        val firstVisibleItemIndex = findFirstVisibleItemPosition()
                        if (getSelection() == firstVisibleItemIndex) {
                            startIndex -= 9
                            endIndex -= 9
                            val data = FileLogger.readAlarmFile("alarm",startIndex,endIndex)
                            if (data != "Data Not Found"){
                                val listData = data.split("|") as java.util.ArrayList<String>
                                mAdapter?.updateDataList(listData,true)
                            }else {
                                startIndex = tempStartIndex
                                endIndex = tempEndIndex
                            }
                        }
                        setSelectionUpward()
                    }
                }
            }

            R.id.bottomButtonAlarm -> {
                Log.i("value_check_events","bottom")
                mAdapter?.apply {
                    mLayoutManager.apply {
                        val lastVisibleItemIndex = findLastCompletelyVisibleItemPosition()

                        if (getSelection() != dataList.size - 1) {
                            if (getSelection() == lastVisibleItemIndex) {
                                startIndex += 9
                                endIndex += 9
                                val data = FileLogger.readAlarmFile("alarm",startIndex,endIndex)
                                if (data != "Data Not Found"){
                                    val listData = data.split("|") as java.util.ArrayList<String>
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAlarmViewModel = ViewModelProvider(this).get(AlarmViewModel::class.java)
        dashBoardViewModel = ViewModelProvider(requireActivity()).get(DashBoardViewModel::class.java)
        spinnerAlarm = view.findViewById<Spinner>(R.id.spinnerAlarm)
        buttonLayout = view.findViewById(R.id.btnLayout)

        topButtonAlarm.setOnClickListener(this)
        bottomButtonAlarm.setOnClickListener(this)

        setupAdapter()
        setupDataDefault("")
    }



    private fun setUpAlarmsData() {
        mAdapter = AlarmAdapter(dataList)
        mLayoutManager = LinearLayoutManager(requireContext())

        recyclerViewAlarm?.apply {
            layoutManager = mLayoutManager
            adapter = mAdapter
            isVerticalScrollBarEnabled = true
            itemAnimator = null
        }
    }

    fun scrollForward() = bottomButtonAlarm.callOnClick()
    fun scrollBack() = topButtonAlarm.callOnClick()
}
