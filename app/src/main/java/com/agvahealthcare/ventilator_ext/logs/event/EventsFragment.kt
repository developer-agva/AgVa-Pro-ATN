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
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.system.settings.CommonSetupAdapter
import com.agvahealthcare.ventilator_ext.system.settings.onDropDownSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class EventsFragment : Fragment(), View.OnClickListener, onDropDownSelectionListener {

    private lateinit var binding: FragmentEventsBinding
    private lateinit var mEventViewModel: EventViewModel
    private var dataList = ArrayList<String>()
    private var dashBoardViewModel: DashBoardViewModel? = null
    lateinit var mLayoutManager: LinearLayoutManager
    var mAdapter: EventAdapter? = null
    private var uhid = ""
    private var startIndex = 0
    private var endIndex = 9
    private var uhidAdapter: CommonSetupAdapter? = null
    private var clickUhidLayout = false
    private var defaultUhid = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsBinding.inflate(layoutInflater, container, false)
        mEventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        dashBoardViewModel =
            ViewModelProvider(requireActivity()).get(DashBoardViewModel::class.java)
        binding.root.setOnClickListener {
            binding.uhidRecyclerView.visibility = View.GONE
        }
        return binding.root
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

    private fun setupUhidLayout(uhidList: ArrayList<String>) {
        uhidAdapter = CommonSetupAdapter(uhidList, this)
        binding.uhidRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = uhidAdapter
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
        defaultUhid = PreferenceManager(requireContext()).readUHID()
        setupDataDefault(defaultUhid)

        binding.uhidLayout.setOnClickListener {
            clickUhidLayout = true
            binding.uhidRecyclerView.visibility = View.VISIBLE

            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val data = FileLogger.readUhidFile("event")
                    if (data != FileLogger.dataNotFound) {
                        val list = (data.split("|") as java.util.ArrayList<String>)
                        list.removeLast()
                        val newList = list.toSet()

                        withContext(Dispatchers.Main) {
                            if (newList.size > 1) setupUhidLayout(newList.toList() as java.util.ArrayList<String>)
                        }
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onClick(view: View) {

        val tempStartIndex = startIndex
        val tempEndIndex = endIndex

        when (view.id) {

            R.id.topButton -> {
                Log.i("value_check_events", "top")
                mAdapter?.apply {
                    mLayoutManager.apply {
                        val firstVisibleItemIndex = findFirstVisibleItemPosition()
                        if (getSelection() == firstVisibleItemIndex) {
                            startIndex -= 9
                            endIndex -= 9
                            val data = FileLogger.readEventFile("event",defaultUhid, startIndex, endIndex)
                            if (data != "Data Not Found") {
                                val listData = data.split("|") as ArrayList<String>
                                mAdapter?.updateDataList(listData, true)
                            } else {
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

                        Log.i("value_check_events", "${getSelection()} - $lastVisibleItemIndex")


                        if (getSelection() != dataList.size - 1) {

                            if (getSelection() == lastVisibleItemIndex) {
                                startIndex += 9
                                endIndex += 9
                                val data = FileLogger.readEventFile("event",defaultUhid, startIndex, endIndex)
                                if (data != "Data Not Found") {
                                    val listData = data.split("|") as ArrayList<String>
                                    mAdapter?.updateDataList(listData, false)
                                } else {
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
        binding.txtUhid.text = "UHID : $uhid"
        CoroutineScope(Dispatchers.IO).launch {
            val data = FileLogger.readEventFile("event", uhid, startIndex, endIndex)
            if (data != "Data Not Found") {

                withContext(Dispatchers.Main) {
                    dataList = data.split("|") as ArrayList<String>
                    setDataForEvents()
                }
            }
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
