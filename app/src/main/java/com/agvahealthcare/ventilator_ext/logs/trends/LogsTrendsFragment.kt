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
import com.agvahealthcare.ventilator_ext.logs.trends.ParameterAndUnitsAdapter

class LogsTrendsFragment : Fragment(), View.OnClickListener {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var parameterAndUnitsAdapter: ParameterAndUnitsAdapter? = null
    private var dataFromDataBaseAdapter: DataFromDataBaseAdapter? = null
    private var dashBoardViewModel: DashBoardViewModel? = null
    private var timer: CountDownTimer? = null
    private lateinit var binding : FragmentLogsTableDemoBinding
    private var startIndex = 0
    private var endIndex = 8

    private var list = ArrayList<String>()
    private lateinit var linearLayoutManager: LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogsTableDemoBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    companion object {

        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        fun newInstance(param1: String?, param2: String?): LogsTrendsFragment {
            val fragment = LogsTrendsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dashBoardViewModel = ViewModelProvider(requireActivity())[DashBoardViewModel::class.java]

        openingTimer()
        binding.leftButton.setOnClickListener(this)
        binding.rightButton.setOnClickListener(this)
    }

    private fun setUpTrendAdapter() {
        dataFromDataBaseAdapter = DataFromDataBaseAdapter(requireContext(), list)

        binding.rvTwo.setHasFixedSize(true)
        binding.rvTwo.setItemViewCacheSize(10);
        linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvTwo.apply {
            layoutManager = linearLayoutManager
            adapter = dataFromDataBaseAdapter
        }
    }

    private fun openingTimer() {
        binding.mainLayoutTrends.visibility = View.GONE
        binding.txtWaitTrends.visibility = View.VISIBLE
        timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(milliSec: Long) {

            }

            override fun onFinish() {

                VentilatorApp.isTrendsFirstTime = true

                binding.seekBar.setOnTouchListener { v, event -> true }

                setUpTrendAdapter()

                parameterAndUnitsAdapter = ParameterAndUnitsAdapter()

                binding.rvParameterConstants.apply {
                    layoutManager = LinearLayoutManager(requireActivity())
                    adapter = parameterAndUnitsAdapter
                }

                binding.mainLayoutTrends.visibility = View.VISIBLE
                binding.txtWaitTrends.visibility = View.GONE
                setupDataDefault()
            }
        }.start()
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

    private fun setupDataDefault() {

        val data = FileLogger.readTrendFile("trends",startIndex,endIndex)
        if (data != "Data Not Found"){
            val listData = data.split("|") as ArrayList<String>
            dashBoardViewModel?.logsDateUpdate?.postValue(listData[0].split(",")[0])
            dataFromDataBaseAdapter?.updateDataList(listData)
        }
    }

    override fun onClick(view: View) {
        val tempStartIndex = startIndex
        val tempEndIndex = endIndex
        when (view.id) {
            R.id.rightButton ->{
                val data = FileLogger.readTrendFile("trends",++startIndex,++endIndex)
                if (data != "Data Not Found"){
                    val listData = data.split("|") as ArrayList<String>
                    dashBoardViewModel?.logsDateUpdate?.postValue(listData[0].split(",")[0])
                    dataFromDataBaseAdapter?.updateDataList(listData)
                }
                else {
                    startIndex = tempStartIndex
                    endIndex = tempEndIndex
                }
            }

            R.id.leftButton ->{
                val data = FileLogger.readTrendFile("trends",--startIndex,--endIndex)
                if (data != "Data Not Found"){
                    val listData = data.split("|") as ArrayList<String>
                    dashBoardViewModel?.logsDateUpdate?.postValue(listData[0].split(",")[0])
                    dataFromDataBaseAdapter?.updateDataList(listData)
                }else {
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