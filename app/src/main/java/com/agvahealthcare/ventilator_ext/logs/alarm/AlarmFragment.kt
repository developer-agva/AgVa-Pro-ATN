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
import com.agvahealthcare.ventilator_ext.databinding.FragmentLogsAlarmBinding
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.system.settings.CommonSetupAdapter
import com.agvahealthcare.ventilator_ext.system.settings.onDropDownSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AlarmFragment : Fragment(), View.OnClickListener, onDropDownSelectionListener {
    private lateinit var mAlarmViewModel: AlarmViewModel
    private lateinit var buttonLayout: LinearLayoutCompat
    private var dashBoardViewModel: DashBoardViewModel? = null
    private var mAdapter: AlarmAdapter? = null
    lateinit var mLayoutManager: LinearLayoutManager
    private var dataList = ArrayList<String>()
    private var startIndex = 0
    private var endIndex = 11
    private var uhidAdapter: CommonSetupAdapter? = null
    private var clickUhidLayout = false
    private var defaultUhid = ""
    private lateinit var binding : FragmentLogsAlarmBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogsAlarmBinding.inflate(layoutInflater,container,false)
        binding.root.setOnClickListener {
            binding.uhidRecyclerView.visibility = View.GONE
        }
        return binding.root
    }

    override fun onPause() {
        super.onPause()
        dashBoardViewModel?.updateIsAlarmsFragmentVisible(false)
    }

    override fun onResume() {
        super.onResume()
        dashBoardViewModel?.updateIsAlarmsFragmentVisible(true)
    }

    override fun onItemSelect(text: String, colorInt: Int) {

        binding.uhidRecyclerView.visibility = View.GONE
        mAdapter = null

        if (clickUhidLayout) {
            defaultUhid = text
            setupDataDefault(defaultUhid)
        }
        clickUhidLayout = false
    }


    private fun setupUhidLayout(uhidList: java.util.ArrayList<String>) {
        uhidAdapter = CommonSetupAdapter(uhidList, this)
        binding.uhidRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = uhidAdapter
        }
    }

    private fun setupDataDefault(uhid: String) {
        binding.txtUhid.text = "UHID : $uhid"
        val data = FileLogger.readAlarmFile("alarm",uhid,startIndex,endIndex)
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
                            startIndex -= 11
                            endIndex -= 11
                            val data = FileLogger.readAlarmFile("alarm",defaultUhid,startIndex,endIndex)
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
                                startIndex += 11
                                endIndex += 11
                                val data = FileLogger.readAlarmFile("alarm",defaultUhid,startIndex,endIndex)
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

        binding.topButtonAlarm.setOnClickListener(this)
        binding.bottomButtonAlarm.setOnClickListener(this)
        defaultUhid = PreferenceManager(requireContext()).readUHID()
        setupDataDefault(defaultUhid)

        binding.uhidLayout.setOnClickListener {
            clickUhidLayout = true
            binding.uhidRecyclerView.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                val data = FileLogger.readUhidFile("event")
                if (data != FileLogger.dataNotFound) {
                    val list = (data.split("|") as java.util.ArrayList<String>).toSet()

                    withContext(Dispatchers.Main) {
                        setupUhidLayout(list.toList() as java.util.ArrayList<String>)
                    }
                }
            }
        }
    }

    private fun setUpAlarmsData() {
        mAdapter = AlarmAdapter(dataList)
        mLayoutManager = LinearLayoutManager(requireContext())

        binding.recyclerViewAlarm.apply {
            layoutManager = mLayoutManager
            adapter = mAdapter
            isVerticalScrollBarEnabled = true
            itemAnimator = null
        }
    }

    fun scrollForward() = binding.bottomButtonAlarm.callOnClick()
    fun scrollBack() = binding.topButtonAlarm.callOnClick()
}
