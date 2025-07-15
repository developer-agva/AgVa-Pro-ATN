package com.agvahealthcare.ventilator_ext.system.selftest

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService
import kotlinx.android.synthetic.main.fragment_self_test.dataProcessingTextView
import kotlinx.android.synthetic.main.fragment_self_test.self_test_recycler_view


data class SelfTestModelClass(
    val sensorName: String,
    val sensorStatus: Int,
    val sensorStatusText: String,
    val sensorValue: String
)

class SelfTestFragment(private var communicationService: CommunicationService?) : Fragment() {

    private var selfTestViewModel: SelfTestViewModel? = null
    private var preferenceManager: PreferenceManager? = null
    private var selfTestAdapter: SelfTestAdapter? = null

    private var listOfSensors = arrayListOf(
        "CHECK I2C LINE",
        "ADC Response",
        "Insp Flow Sensor",
        "Exp Flow Sensor (SMI)",
        "Insp Pressure Sensor",
        "Exp Pressure Sensor",
        "Battery Voltage",
        "Battery Current",
        "Oxygen Pressure Sensor",
        "Oxygen Sensor (Voltage)",
        "NEO PCB CHECK",
        "SPO2 SENSOR CHECK"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_self_test, container, false)
        return view
    }

    override fun onStart() {
        super.onStart()
        communicationService?.send("CM+SELF1")
    }

    override fun onPause() {
        super.onPause()
        communicationService?.send("CM+SELF0")
    }

    private fun setupSelfTestAdapter(selfTestList: ArrayList<SelfTestModelClass>) {
        selfTestAdapter = SelfTestAdapter(selfTestList)
        self_test_recycler_view.adapter = selfTestAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selfTestViewModel = ViewModelProvider(requireActivity())[SelfTestViewModel::class.java]
        preferenceManager = PreferenceManager(requireContext())

        selfTestViewModel?.selfTestData?.observe(viewLifecycleOwner) { selfTestData ->
            dataProcessingTextView.visibility = View.GONE
            if (selfTestData.isNotEmpty() && selfTestData != "null") {
                showDataOnUI(selfTestData)
            }
        }

    }

    private fun showDataOnUI(selfTestData: String) {
        Log.i("SelfTestFragment", "Received self test data: $selfTestData")
        val selfTestList = selfTestData.split(",") as ArrayList<String>
        val selfTestModelList = ArrayList<SelfTestModelClass>()
        selfTestModelList.add(SelfTestModelClass("SENSOR NAME", 0, "SENSOR STATUS", "SENSOR VALUE"))

        for (i in 0 until selfTestList.size) {
            if (selfTestList[i].isEmpty()) continue
            val sensorStatus = if (selfTestList[i].split("|")[1] == "1") 1 else 0
            selfTestModelList.add(SelfTestModelClass(listOfSensors[i], sensorStatus, "", selfTestList[i].split("|")[0]))
        }

        Log.i("SelfTestFragment", "Received self test data: ${selfTestModelList.size}")
        setupSelfTestAdapter(selfTestModelList)
    }

}

class SelfTestAdapter(private var selfTestList: ArrayList<SelfTestModelClass>) :
    RecyclerView.Adapter<SelfTestAdapter.SelfTestViewHolder>() {

    inner class SelfTestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val sensorName: TextView = itemView.findViewById(R.id.itemTitle)
        val sensorStatus: ImageView = itemView.findViewById(R.id.itemStatus)
        val sensorValue: TextView = itemView.findViewById(R.id.itemValue)
        val sensorStatusHeading: TextView = itemView.findViewById(R.id.itemStatusHeading)
        val sensorTitleHeading: TextView = itemView.findViewById(R.id.itemTitleHeading)
        val sensorValueHeading: TextView = itemView.findViewById(R.id.itemValueHeading)
        val viewH1: View = itemView.findViewById(R.id.viewH1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelfTestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.self_test_single_test, parent, false)
        return SelfTestViewHolder(view)
    }

    override fun getItemCount(): Int {
        return selfTestList.size
    }

    override fun onBindViewHolder(holder: SelfTestViewHolder, position: Int) {
        val selfTestItem = selfTestList[position]

        if (position == 0){
            holder.sensorStatusHeading.visibility = View.VISIBLE
            holder.sensorTitleHeading.visibility = View.VISIBLE
            holder.sensorValueHeading.visibility = View.VISIBLE
            holder.viewH1.visibility = View.VISIBLE
            holder.sensorStatus.visibility = View.GONE
            holder.sensorName.visibility = View.GONE
            holder.sensorValue.visibility = View.GONE
        } else {
            holder.sensorStatusHeading.visibility = View.GONE
            holder.sensorTitleHeading.visibility = View.GONE
            holder.sensorValueHeading.visibility = View.GONE
            holder.viewH1.visibility = View.GONE
            holder.sensorStatus.visibility = View.VISIBLE
            holder.sensorName.visibility = View.VISIBLE
            holder.sensorValue.visibility = View.VISIBLE
        }

        holder.sensorStatusHeading.text = selfTestItem.sensorStatusText
        holder.sensorValueHeading.text = selfTestItem.sensorValue
        holder.sensorTitleHeading.text = selfTestItem.sensorName

        holder.sensorName.text = selfTestItem.sensorName
        holder.sensorValue.text = selfTestItem.sensorValue

        if (selfTestItem.sensorStatus == 1) holder.sensorStatus.setImageResource(R.drawable.ic_green_circle_tick)
        else holder.sensorStatus.setImageResource(R.drawable.ic_red_cross)
    }
}

