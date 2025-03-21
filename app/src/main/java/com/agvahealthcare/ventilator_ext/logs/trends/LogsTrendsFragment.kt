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
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.databinding.FragmentLogsTableDemoBinding
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.logs.trends.DataFromDataBaseAdapter
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.utils.Configs

class LogsTrendsFragment : Fragment(), View.OnClickListener {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var dataFromDataBaseAdapter: DataFromDataBaseAdapter? = null
    private var dashBoardViewModel: DashBoardViewModel? = null
    private lateinit var binding: FragmentLogsTableDemoBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var timer: CountDownTimer? = null
    private var list = ArrayList<String>()
    private var startIndex = 0
    private var endIndex = 9

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogsTableDemoBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]
        preferenceManager = PreferenceManager(requireContext())
        openingTimer()
        binding.leftButton.setOnClickListener(this)
        binding.rightButton.setOnClickListener(this)
    }

    private fun setUpTrendAdapter() {
        val paramList = "Parameter,Mode,PIP,PEEP,Mean Airway,Vti,Vte,MVe,MVi,FiO₂,RR,I:E,Tinsp,Texp,Average Leak,Spo2,PR,Dyn Comp.,Spont VT,Spont RR"
        val unitList = "Unit,Mode Type,cmH₂O,cmH₂O,cmH₂O,mL,mL,litre,litre,%,BPM,Ratio,sec,sec,%,%,BPM,mL/cmH₂O,mL,BPM"
        list.add(paramList)
        list.add(unitList)
        dataFromDataBaseAdapter = DataFromDataBaseAdapter(list)

        binding.rvTwo.setHasFixedSize(true)
        binding.rvTwo.setItemViewCacheSize(10);
        linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTwo.apply {
            layoutManager = linearLayoutManager
            adapter = dataFromDataBaseAdapter
        }
    }

    private fun openingTimer() {
        binding.mainLayoutTrends.visibility = View.GONE
        binding.txtWaitTrends.visibility = View.VISIBLE
        timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(milliSec: Long) {}

            override fun onFinish() {
                VentilatorApp.isTrendsFirstTime = true
                setUpTrendAdapter()
                binding.mainLayoutTrends.visibility = View.VISIBLE
                binding.txtWaitTrends.visibility = View.GONE
                setupDataDefault()
            }
        }.start()
    }

    private fun updateTimeOnView(dataList: ArrayList<String>) {
        for (i in 0 until dataList.size - 1) {

            when (i) {

                0 -> {
                    binding.tvTime1.visibility = View.VISIBLE
                    binding.tvTime1.text = dataList[i].split(",")[0].split(" ")[1]
                }

                1 -> {
                    binding.tvTime2.visibility = View.VISIBLE
                    binding.tvTime2.text = dataList[i].split(",")[0].split(" ")[1]
                }

                2 -> {
                    binding.tvTime3.visibility = View.VISIBLE
                    binding.tvTime3.text = dataList[i].split(",")[0].split(" ")[1]
                }

                3 -> {
                    binding.tvTime4.visibility = View.VISIBLE
                    binding.tvTime4.text = dataList[i].split(",")[0].split(" ")[1]
                }

                4 -> {
                    binding.tvTime5.visibility = View.VISIBLE
                    binding.tvTime5.text = dataList[i].split(",")[0].split(" ")[1]
                }

                5 -> {
                    binding.tvTime6.visibility = View.VISIBLE
                    binding.tvTime6.text = dataList[i].split(",")[0].split(" ")[1]
                }

                6 -> {
                    binding.tvTime7.visibility = View.VISIBLE
                    binding.tvTime7.text = dataList[i].split(",")[0].split(" ")[1]
                }

                7 -> {
                    binding.tvTime8.visibility = View.VISIBLE
                    binding.tvTime8.text = dataList[i].split(",")[0].split(" ")[1]
                }

                8 -> {
                    binding.tvTime9.visibility = View.VISIBLE
                    binding.tvTime9.text = dataList[i].split(",")[0].split(" ")[1]
                }
            }

        }
    }

    fun scrollForward() = binding.rightButton.callOnClick()
    fun scrollBack() = binding.leftButton.callOnClick()

    override fun onPause() {
        super.onPause()
        dashBoardViewModel?.updateIsLogsTrendsFragmentVisible(false)
    }

    override fun onResume() {
        super.onResume()
        dashBoardViewModel?.updateIsLogsTrendsFragmentVisible(true)
    }

    fun setupDataDefault() {

        val data = FileLogger.readTrendFile(
            Configs.getTrendsFileName(preferenceManager),
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
                    Configs.getTrendsFileName(preferenceManager),
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
                    Configs.getTrendsFileName(preferenceManager),
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