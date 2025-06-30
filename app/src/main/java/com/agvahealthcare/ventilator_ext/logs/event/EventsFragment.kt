package com.agvahealthcare.ventilator_ext.logs.event

import android.os.Build
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
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.uhidDataListEvent
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.database.entities.EventDataModel
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_events.*
import kotlinx.android.synthetic.main.fragment_logs_alarm.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class EventsFragment : Fragment(), View.OnClickListener {

    private lateinit var mEventViewModel: EventViewModel
    lateinit var buttonLayout : LinearLayoutCompat
    private var dataList = ArrayList<String>()
    private var dashBoardViewModel: DashBoardViewModel? = null
    lateinit var mLayoutManager: LinearLayoutManager
    var mAdapter: EventAdapter? = null
    private var uhid = ""
    private var startIndex = 0
    private var endIndex = 9
    private var steps = 9

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_events, container, false)
        mEventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        dashBoardViewModel =
            ViewModelProvider(requireActivity()).get(DashBoardViewModel::class.java)
        buttonLayout = view.findViewById(R.id.btnLayout)

        steps = if (Build.VERSION.SDK_INT >= 27) {
            // This is Android 7.0 (API 24) or higher
            10
        } else {
            // Below Android 7.0
            9
        }

        endIndex = steps
        
        return view
    }

    private fun setupSpinnerAdapter(){

        if (uhidDataListEvent.size == 0) buttonLayout.visibility = View.GONE
        else buttonLayout.visibility = View.VISIBLE

        val eventScrollAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            VentilatorApp.uhidDataListEvent
        )

        spinnerEvent.apply {
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

                override fun onNothingSelected(parent: AdapterView<*>?) {}
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
        topButton.setOnClickListener(this)
        bottomButton.setOnClickListener(this)

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
                            startIndex -= steps
                            endIndex -= steps
                            val data = FileLogger.readEventFile("event",
                                PreferenceManager(requireContext()).readUHID(),startIndex,endIndex)
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
                                startIndex += steps
                                endIndex += steps
                                val data = FileLogger.readEventFile("event",PreferenceManager(requireContext()).readUHID(),startIndex,endIndex)
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

        val data = FileLogger.readEventFile("event",PreferenceManager(requireContext()).readUHID(),startIndex,endIndex)
        if (data != "Data Not Found"){
            dataList = data.split("|") as ArrayList<String>
            setDataForEvents()
        }
    }

    fun scrollForward() = bottomButton.callOnClick()
    fun scrollBack() = topButton.callOnClick()

    private fun setDataForEvents() {
        mAdapter = EventAdapter(dataList)
        mLayoutManager = LinearLayoutManager(requireContext())

        recyclerViewEvents?.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            isVerticalScrollBarEnabled = true
            itemAnimator = null
        }
    }


}
