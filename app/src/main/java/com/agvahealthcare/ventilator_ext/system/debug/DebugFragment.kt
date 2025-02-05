package com.agvahealthcare.ventilator_ext.system.debug

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.api.model.Log
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import kotlinx.android.synthetic.main.content_button_layout.view.buttonView
import kotlinx.android.synthetic.main.fragment_advanced_calibration.includeButtonTurbine
import kotlinx.android.synthetic.main.fragment_debug.buttonDevelopersEvents
import kotlinx.android.synthetic.main.fragment_debug.buttonHid
import kotlinx.android.synthetic.main.fragment_debug.buttonPermissions
import kotlinx.android.synthetic.main.fragment_debug.buttonToReboot
import kotlinx.android.synthetic.main.fragment_debug.buttonVenti
import kotlinx.android.synthetic.main.fragment_debug.debugLayout
import kotlinx.android.synthetic.main.fragment_debug.debugRecyclerView
import kotlinx.android.synthetic.main.fragment_diagnostic_check.includeButtonAmberLED
import kotlinx.android.synthetic.main.fragment_diagnostic_check.includeButtonNebulizer
import kotlinx.android.synthetic.main.fragment_diagnostic_check.includeButtonOxyValve
import kotlinx.android.synthetic.main.fragment_diagnostic_check.includeButtonPurge
import kotlinx.android.synthetic.main.fragment_diagnostic_check.includeButtonRedLED
import kotlinx.android.synthetic.main.fragment_test_calib.includeButtonExhaleValve

class DebugFragment : Fragment() {

    private var debugViewModel: DebugViewModel? = null
    private var debugAdapter: DebugAdapter? = null
    private var isVenti: Boolean? = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_debug, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        debugViewModel = ViewModelProvider(requireActivity())[DebugViewModel::class.java]

        android.util.Log.i("testing_tag", activity.toString())

        try {
            (requireActivity() as MainActivity).returnDebugCommandsToSocket("Stop Debug")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setupView()
        setupAdapter()
        setOnClickListeners()
        observers()
    }

    private fun setupView(){
        buttonVenti.callOnClick()
    }

    override fun onPause() {
        try {
            (requireActivity() as MainActivity).returnDebugCommandsToSocket("Start Debug")
        } catch (e: Exception) {
           e.printStackTrace()
        }
        super.onPause()
    }

    private fun observers() {

        debugViewModel?.ventiLiveData?.observe(viewLifecycleOwner) {
            if (isVenti == true) {
                val list = it.reversed()  as ArrayList<String>
                debugAdapter?.updateDataList(list)

                var data = ""
                for (i in 0 until  list.size){
                    data += list[i]
                    if (i != it.size-1)   data += "|"
                }
                try {
                    (requireActivity() as MainActivity).sendDebugDataToLiveWindow(data)
                } catch (e: Exception) {
                   e.printStackTrace()
                }
            }
        }

        debugViewModel?.hidLiveData?.observe(viewLifecycleOwner) {
            if (isVenti == false) {
                val list = it.reversed()  as ArrayList<String>
                debugAdapter?.updateDataList(list)

                var data = ""
                for (i in 0 until  list.size){
                    data += list[i]
                    if (i != it.size-1)   data += "|"
                }
                try {
                    (requireActivity() as MainActivity).sendDebugDataToLiveWindow(data)
                } catch (e: Exception) {
                   e.printStackTrace()
                }
            }
        }

    }

    private fun setupAdapter() {
        debugAdapter = DebugAdapter(ArrayList())
        debugRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        debugRecyclerView.adapter = debugAdapter
    }
    
    fun getCommandsFromLiveWindow(command: String) {
        when (command) {

            "Start Knob Data" ->{
                buttonHid.callOnClick()
            }

            "Start Venti Data" ->{
                buttonVenti.callOnClick()
            }
        }
    }

    private fun highlightAndNormaliseButtons(view: View){
        buttonVenti.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        buttonHid.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        buttonDevelopersEvents.setBackgroundResource(R.drawable.background_primary_btn_rounded)
        buttonPermissions.setBackgroundResource(R.drawable.background_primary_btn_rounded)

        view.setBackgroundResource(R.drawable.background_green_border)
    }

    private fun setOnClickListeners() {

        buttonVenti.setOnClickListener { it ->
            isVenti = true
            debugLayout.visibility = View.VISIBLE
            highlightAndNormaliseButtons(it)

            debugViewModel?.ventiLiveData?.value?.let { it1 ->
                debugAdapter?.updateDataList(it1.reversed() as ArrayList<String>)
            }
            try {
                (requireActivity() as MainActivity).returnDebugCommandsToSocket("Start Venti Data")
                (requireActivity() as MainActivity).returnDebugCommandsToSocket("Stop Knob Data")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        buttonToReboot.setOnClickListener {
            Runtime.getRuntime().exec("reboot")
        }

        buttonPermissions.setOnClickListener {
            debugLayout.visibility = View.GONE
            highlightAndNormaliseButtons(it)
        }

        buttonHid.setOnClickListener {
            isVenti = false
            debugLayout.visibility = View.VISIBLE
            highlightAndNormaliseButtons(it)

            debugViewModel?.hidLiveData?.value?.let { it1 ->
                debugAdapter?.updateDataList(it1.reversed() as ArrayList<String>)
            }
            try {
                (requireActivity() as MainActivity).returnDebugCommandsToSocket("Start Knob Data")
                (requireActivity() as MainActivity).returnDebugCommandsToSocket("Stop Venti Data")
            }catch (E:Exception){
                E.printStackTrace()
            }

        }

        buttonDevelopersEvents.setOnClickListener {
            isVenti = null
            debugLayout.visibility = View.VISIBLE
            highlightAndNormaliseButtons(it)

            val data = FileLogger.readEventFileDevelopers("eventForDevelopers")
            android.util.Log.i("dataCHeck", data)
            if (data != "Data Not Found") {
                val listData = data.split("|").reversed() as java.util.ArrayList<String>
                if (listData.size > 20) {
                    val newList = listData.subList(0, 20)
                    debugAdapter?.updateDataList(newList)
                } else debugAdapter?.updateDataList(listData)
            }else{
                debugAdapter?.updateDataList(ArrayList<String>())
            }
        }
    }

}