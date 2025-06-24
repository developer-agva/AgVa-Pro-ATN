import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.api.model.Log
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.logs.trends.DataFromDataBaseAdapter
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.system.settings.CommonSetupAdapter
import com.agvahealthcare.ventilator_ext.system.settings.onDropDownSelectionListener
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import kotlinx.android.synthetic.main.fragment_logs_table_demo.leftButton
import kotlinx.android.synthetic.main.fragment_logs_table_demo.mainLayoutTrends
import kotlinx.android.synthetic.main.fragment_logs_table_demo.rightButton
import kotlinx.android.synthetic.main.fragment_logs_table_demo.rvTwo
import kotlinx.android.synthetic.main.fragment_logs_table_demo.tv_time_1
import kotlinx.android.synthetic.main.fragment_logs_table_demo.tv_time_2
import kotlinx.android.synthetic.main.fragment_logs_table_demo.tv_time_3
import kotlinx.android.synthetic.main.fragment_logs_table_demo.tv_time_4
import kotlinx.android.synthetic.main.fragment_logs_table_demo.tv_time_5
import kotlinx.android.synthetic.main.fragment_logs_table_demo.tv_time_6
import kotlinx.android.synthetic.main.fragment_logs_table_demo.tv_time_7
import kotlinx.android.synthetic.main.fragment_logs_table_demo.tv_time_8
import kotlinx.android.synthetic.main.fragment_logs_table_demo.tv_time_9
import kotlinx.android.synthetic.main.fragment_logs_table_demo.txtUhid
import kotlinx.android.synthetic.main.fragment_logs_table_demo.txtWaitTrends
import kotlinx.android.synthetic.main.fragment_logs_table_demo.uhidLayout
import kotlinx.android.synthetic.main.fragment_logs_table_demo.uhidRecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogsTrendsFragment : Fragment(), View.OnClickListener, onDropDownSelectionListener {
    private var dataFromDataBaseAdapter: DataFromDataBaseAdapter? = null
    private var dashBoardViewModel: DashBoardViewModel? = null
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var timer: CountDownTimer? = null
    private var list = ArrayList<String>()
    private var startIndex = 0
    private var endIndex = 9
    private var uhidAdapter: CommonSetupAdapter? = null
    private var clickUhidLayout = false
    private var defaultUhid = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_logs_table_demo, container, false)

        view.setOnClickListener {
            uhidRecyclerView.visibility = View.GONE
        }
        return view

    }

    override fun onItemSelect(text: String, colorInt: Int) {

        uhidRecyclerView.visibility = View.GONE
        uhidAdapter = null

        if (clickUhidLayout) {
            defaultUhid = text
            setupDataDefault()
        }
        clickUhidLayout = false
    }

    private fun setupUhidLayout(uhidList: java.util.ArrayList<String>) {
        uhidAdapter = CommonSetupAdapter(uhidList, this)
        uhidRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = uhidAdapter
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
        preferenceManager = PreferenceManager(requireContext())
        openingTimer()
        leftButton.setOnClickListener(this)
        rightButton.setOnClickListener(this)

        uhidLayout.setOnClickListener {
            clickUhidLayout = true
            uhidRecyclerView.visibility = View.VISIBLE
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

    private fun setUpTrendAdapter() {
        val paramList = "Parameter,Mode,PIP,PEEP,Mean Airway,Vti,Vte,MVe,MVi,FiO₂,RR,I:E,Tinsp,Texp,Average Leak,Spo2,PR,Dyn Comp.,Spont VT,Spont RR"
        val unitList = "Unit,Mode Type,cmH₂O,cmH₂O,cmH₂O,mL,mL,litre,litre,%,BPM,Ratio,sec,sec,%,%,BPM,mL/cmH₂O,mL,BPM"
        list.add(paramList)
        list.add(unitList)
        dataFromDataBaseAdapter = DataFromDataBaseAdapter(list)

        rvTwo.setHasFixedSize(true)
        rvTwo.setItemViewCacheSize(10);
        linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvTwo.apply {
            layoutManager = linearLayoutManager
            adapter = dataFromDataBaseAdapter
        }
    }

    private fun openingTimer() {
        mainLayoutTrends.visibility = View.GONE
        txtWaitTrends.visibility = View.VISIBLE
        timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(milliSec: Long) {}

            override fun onFinish() {
                VentilatorApp.isTrendsFirstTime = true
                setUpTrendAdapter()
                mainLayoutTrends.visibility = View.VISIBLE
                txtWaitTrends.visibility = View.GONE
                defaultUhid = PreferenceManager(requireContext()).readUHID()
                setupDataDefault()
            }
        }.start()
    }

    private fun updateTimeOnView(dataList: ArrayList<String>) {
        for (i in 0 until dataList.size - 1) {

            when (i) {

                0 -> {
                    tv_time_1.visibility = View.VISIBLE
                    tv_time_1.text = dataList[i].split(",")[0].split(" ")[1]
                }

                1 -> {
                    tv_time_2.visibility = View.VISIBLE
                    tv_time_2.text = dataList[i].split(",")[0].split(" ")[1]
                }

                2 -> {
                    tv_time_3.visibility = View.VISIBLE
                    tv_time_3.text = dataList[i].split(",")[0].split(" ")[1]
                }

                3 -> {
                    tv_time_4.visibility = View.VISIBLE
                    tv_time_4.text = dataList[i].split(",")[0].split(" ")[1]
                }

                4 -> {
                    tv_time_5.visibility = View.VISIBLE
                    tv_time_5.text = dataList[i].split(",")[0].split(" ")[1]
                }

                5 -> {
                    tv_time_6.visibility = View.VISIBLE
                    tv_time_6.text = dataList[i].split(",")[0].split(" ")[1]
                }

                6 -> {
                    tv_time_7.visibility = View.VISIBLE
                    tv_time_7.text = dataList[i].split(",")[0].split(" ")[1]
                }

                7 -> {
                    tv_time_8.visibility = View.VISIBLE
                    tv_time_8.text = dataList[i].split(",")[0].split(" ")[1]
                }

                8 -> {
                    tv_time_9.visibility = View.VISIBLE
                    tv_time_9.text = dataList[i].split(",")[0].split(" ")[1]
                }
            }

        }
    }

    fun scrollForward() = rightButton.callOnClick()
    fun scrollBack() = leftButton.callOnClick()

    override fun onPause() {
        super.onPause()
        dashBoardViewModel?.updateIsLogsTrendsFragmentVisible(false)
    }

    override fun onResume() {
        super.onResume()
        dashBoardViewModel?.updateIsLogsTrendsFragmentVisible(true)
    }

    fun setupDataDefault() {
        txtUhid.text = "UHID : $defaultUhid"
        val data = FileLogger.readTrendFile(
            "trends",
            defaultUhid,
            startIndex,
            endIndex
        )
        if (data != "Data Not Found") {
            val listData = data.split("|") as ArrayList<String>
            dashBoardViewModel?.logsDateUpdate?.postValue(listData[0].split(",")[0])
            updateTimeOnView(listData)
            dataFromDataBaseAdapter?.updateDataList(listData)
        }
    }

    override fun onClick(view: View) {
        val tempStartIndex = startIndex
        val tempEndIndex = endIndex
        when (view.id) {
            R.id.rightButton -> {
                val data = FileLogger.readTrendFile(
                    "trends",
                    defaultUhid,
                    ++startIndex,
                    ++endIndex
                )
                if (data != "Data Not Found") {
                    val listData = data.split("|") as ArrayList<String>
                    dashBoardViewModel?.logsDateUpdate?.postValue(listData[0].split(",")[0])
                    updateTimeOnView(listData)
                    dataFromDataBaseAdapter?.updateDataList(listData)
                } else {
                    startIndex = tempStartIndex
                    endIndex = tempEndIndex
                }
            }

            R.id.leftButton -> {
                val data = FileLogger.readTrendFile(
                    "trends",
                    defaultUhid,
                    --startIndex,
                    --endIndex
                )
                if (data != "Data Not Found") {
                    val listData = data.split("|") as ArrayList<String>
                    dashBoardViewModel?.logsDateUpdate?.postValue(listData[0].split(",")[0])
                    updateTimeOnView(listData)
                    dataFromDataBaseAdapter?.updateDataList(listData)
                } else {
                    startIndex = tempStartIndex
                    endIndex = tempEndIndex
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}