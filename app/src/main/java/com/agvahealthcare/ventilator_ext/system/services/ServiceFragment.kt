package com.agvahealthcare.ventilator_ext.system.services

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.api.ServerLogger
import com.agvahealthcare.ventilator_ext.api.model.serviceDataModel.Data
import com.agvahealthcare.ventilator_ext.api.model.serviceDataModel.ServiceCloseRequestModel
import com.agvahealthcare.ventilator_ext.api.model.serviceDataModel.ServiceOtpVerifyModel
import com.agvahealthcare.ventilator_ext.api.model.serviceDataModel.ServiceRequestModel
import com.agvahealthcare.ventilator_ext.callback.OtpVerifyListener
import com.agvahealthcare.ventilator_ext.callback.PasswordCallbackListener
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardViewModel
import com.agvahealthcare.ventilator_ext.database.entities.ServiceDataModel
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.model.ServiceIssueModel
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import com.agvahealthcare.ventilator_ext.utility.callback.OnIssueCloseListener
import com.agvahealthcare.ventilator_ext.utility.callback.OnIssueSelectListener
import com.agvahealthcare.ventilator_ext.utility.callback.OnServiceClickListener
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.FormValidation
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.android.synthetic.main.fragment_service.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList


class ServiceFragment : Fragment(), OtpVerifyListener, PasswordCallbackListener,
    OnIssueCloseListener, OnServiceClickListener,
    OnIssueSelectListener {

    private var mAdapter: ServiceAdapter? = null
    private var mIssueAdapter: IssueAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var mIssueLayoutManager: LinearLayoutManager? = null
    private var preferenceManager: PreferenceManager? = null
    private var dataStoreManager: DataStoreManager? = null
    private var mServiceViewModel: ServiceViewModel? = null
    private var mMainActivityViewModel: MainActivityViewModel? = null

    private var otherFieldIsEmpty = false
    private var dataList: ArrayList<Data> = ArrayList()
    private var issueList: ArrayList<ServiceIssueModel> = ArrayList()
    private var issues = ""
    private var deviceId = ""



    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_service, container, false)

        if (view !is EditText) {
            view.setOnTouchListener { _, _ ->
                AppUtils.hideKeyBoard(requireContext(), nameET)
                AppUtils.hideKeyBoard(requireContext(), contactNoET)
                AppUtils.hideKeyBoard(requireContext(), departmentET)
                AppUtils.hideKeyBoard(requireContext(), wardET)
                AppUtils.hideKeyBoard(requireContext(), emailET)
                false
            }
        }
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        dataStoreManager = DataStoreManager(requireContext())
        mServiceViewModel = ViewModelProvider(requireActivity())[ServiceViewModel::class.java]
        mMainActivityViewModel = ViewModelProvider(requireActivity())[MainActivityViewModel::class.java]

        deviceId = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ANDROID_ID
        )

        val input = "https://wa.me/7330405060?text=Hi, i need support for this ventilator id - +${deviceId}"

        setupClickListener()
        setUpServiceData()

        val qrCodeBitmap = qrCode(input)
        qrCode.setImageBitmap(qrCodeBitmap)

        mServiceViewModel?.readAllService(deviceId)

        observeData()
        changeTextListener()
    }

    private fun observeData() {

        mServiceViewModel?.serviceDataTemp?.distinctUntilChanged()?.observe(viewLifecycleOwner) {
            it?.let {
                if (it.isNotEmpty()) {
                    dataList = it
                    mAdapter?.updateDataList(it)
                }
            }
        }
    }

    // action for reset service hours
    override fun doAction(otpData: String, isServiceOpen: Boolean) {

        // for service open
        if (isServiceOpen) {
            includeButtonRaiseRequest.setBackgroundResource(R.drawable.background_grey_border_white)
            txtRaiseServiceButtonName.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )

            CoroutineScope(Dispatchers.IO).launch {
                val requestApi = ServiceOtpVerifyModel()
                requestApi.apply {
                    did = Settings.Secure.getString(
                        requireContext().contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    otp = otpData
                }

                val response = ServerLogger.sendOtpVerifyRequest(requestApi)
                Log.i("reasdsawd", response.toString())

                withContext(Dispatchers.Main) {
                    if (response) {

                        DialogBoxFactory.dismissDialogs()

                        val request = ServiceDataModel(
                            issues,
                            nameET.text.toString(),
                            departmentET.text.toString(),
                            wardET.text.toString(),
                            hospitalET.text.toString(),
                            emailET.text.toString(),
                            contactNoET.text.toString(),
                            AppUtils.getCurrentDateTime()
                        )
                        mServiceViewModel?.addService(requireContext(), request)


                        DialogBoxFactory.showServiceRegisterDialog(context,"Your complaint has been successfully registered")

//                        ToastFactory.custom(
//                            requireContext(),
//                            requireContext().getString(R.string.info_otp_verify)
//                        )
                        normaliseUI()
                    } else {
                        ToastFactory.custom(
                            requireContext(),
                            requireContext().getString(R.string.info_otp_wrong)
                        )
                    }

                }
            }
        }

        // for service close
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val requestApi = ServiceOtpVerifyModel()
                requestApi.apply {
                    did = Settings.Secure.getString(
                        requireContext().contentResolver,
                        Settings.Secure.ANDROID_ID
                    )
                    otp = otpData
                }

                val response = ServerLogger.sendOtpVerifyForTicketClose(requestApi)
                Log.i("reasdsawd", response.toString())

                withContext(Dispatchers.Main) {
                    if (response) {

                        DialogBoxFactory.dismissDialogs()

                        preferenceManager?.apply {
                            setServiceHoursStartTime(System.currentTimeMillis())
                            setServiceHoursEndTime(0L)
                            setDashBoardRunningTimeForService(0L)

                            withContext(Dispatchers.IO){
                                FileLogger.writeServiceFile("0")
                            }

                            try {
                                (requireActivity() as DashBoardActivity).lastServiceRunningTime = 0L
                            }catch (e:Exception){
                                mMainActivityViewModel?.serviceHours?.postValue(String.format("%d hr, %d min", 0L, 0L))
                            }
                        }

                        ToastFactory.custom(
                            requireContext(),
                            requireContext().getString(R.string.info_service_closed)
                        )
                        normaliseUI()
                    } else {
                        ToastFactory.custom(
                            requireContext(),
                            requireContext().getString(R.string.info_otp_wrong)
                        )
                    }
                }
            }
        }
    }

    override fun closeDialog() {

        includeButtonRaiseRequest.setBackgroundResource(R.drawable.background_grey_border_white)
        txtRaiseServiceButtonName.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.black
            )
        )
        AppUtils.hideKeyBoard(requireContext(), nameET)
        AppUtils.hideKeyBoard(requireContext(), contactNoET)
        AppUtils.hideKeyBoard(requireContext(), departmentET)
        AppUtils.hideKeyBoard(requireContext(), wardET)
        AppUtils.hideKeyBoard(requireContext(), emailET)

    }

    override fun onPause() {
        otherFieldIsEmpty = false
        normaliseUI()
        super.onPause()
    }

    private fun normaliseUI() {
        mServiceViewModel?.readAllService(deviceId)
        mainLayout.visibility = View.VISIBLE
        addServiceLayout.visibility = View.GONE

        nameET.text?.clear()
        contactNoET.text?.clear()
        departmentET.text?.clear()
        emailET.text?.clear()
        wardET.text?.clear()
    }

    private fun setupClickListener() {

        backBtn.setOnClickListener {
            normaliseUI()
        }

        includeButtonAddService.setOnClickListener {
            mainLayout.visibility = View.GONE
            addServiceLayout.visibility = View.VISIBLE
            setUpServiceIssueData()
        }

        includeButtonRaiseRequest.setOnClickListener {

            issues = ""
            for (i in 0 until issueList.size) {
                if (issueList[i].isTrue) {
                    if (i == issueList.size) issues += issueList[i].issue
                    else issues += issueList[i].issue + ","
                }
            }

            Log.i("asd3123",issues.toString())

            if (!FormValidation.service_details_validations(
                    nameET,
                    contactNoET,
                    emailET,
                    hospitalET,
                    wardET,
                    departmentET
                )
            ) ToastFactory.custom(requireContext(), "Please enter service details...")
            else if (issues.isEmpty()) ToastFactory.custom(
                requireContext(),
                "Please select issues..."
            )
            else {
                includeButtonRaiseRequest.setBackgroundResource(R.drawable.background_green_border)
                txtRaiseServiceButtonName.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val requestApi = ServiceRequestModel()
                    requestApi.apply {
                        did = deviceId
                        date = AppUtils.getCurrentDateTime()
                        name = nameET.text.toString()
                        department = departmentET.text.toString()
                        hospitalName = hospitalET.text.toString()
                        wardNo = wardET.text.toString()
                        email = emailET.text.toString()
                        contactNo = contactNoET.text.toString()
                        message = issues
                    }

                    val response = ServerLogger.sendServiceRequest(requestApi)

                    withContext(Dispatchers.Main) {

                        response?.let {
                            if (it.body()?.status == 201){
                                DialogBoxFactory.dismissDialogs()
                                DialogBoxFactory.showOtpVerifyDialog(
                                    requireContext(), this@ServiceFragment, this@ServiceFragment,
                                    requireContext().resources.getString(R.string.info_otp_sent),
                                    true
                                )
                            }else {
                                // show error dialog for msg
                                ToastFactory.custom(requireContext(),"Service request already raised..")
                            }
                        }

                    }
                }
            }
        }

    }

    private fun qrCode(inputValue: String): Bitmap? {
        val hints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"

        try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(inputValue.toString(), BarcodeFormat.QR_CODE, 512, 512, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            return bmp
        } catch (e: WriterException) {
            e.printStackTrace()
        }



        return null
    }


    fun updateValueOnKnobChange(data: String?) {
        when (data) {
            Configs.PREFIX_PLUS -> {

                mAdapter?.apply {
                    mLayoutManager?.apply {
                        if (getSelection() != dataList.size - 1) {
                            setSelectionDownward()
                        }
                    }
                }
            }

            Configs.PREFIX_MINUS -> {
                mAdapter?.apply {
                    mLayoutManager?.apply {
                        setSelectionUpword()
                    }
                }
            }

            Configs.PREFIX_AND -> {
                serviceClick(dataList[mAdapter?.selectedIndex!!])
            }
        }
    }

    override fun serviceClick(data: Data) {
        // open Dialog
        DialogBoxFactory.dismissDialogs()
        DialogBoxFactory.showServiceDialog(
            requireContext(),
            data, this
        )
    }

    private fun setUpServiceData() {
        mAdapter = ServiceAdapter(requireContext(), dataList, this)
        mLayoutManager = LinearLayoutManager(requireContext())

        recyclerViewService?.apply {
            layoutManager = mLayoutManager
            adapter = mAdapter
            isVerticalScrollBarEnabled = true
            itemAnimator = null
        }
    }

    override fun issueSelect(issue: String, position: Int) {

        issues = issue
        issueList[position].isTrue = !issueList[position].isTrue
    }

    override fun issueClose(_id: String, contactNo: String,name:String,uid:String) {
        CoroutineScope(Dispatchers.IO).launch {
            val requestApi = ServiceCloseRequestModel()
            requestApi.apply {
                this.contactNo = contactNo
                this._id = _id
                this.uid = uid
                this.serviceEngName = name
            }

            val response = ServerLogger.sendServiceCloseRequest(requestApi)

            withContext(Dispatchers.Main) {
                if (response) {
                    DialogBoxFactory.dismissDialogs()
                    DialogBoxFactory.showOtpVerifyDialog(
                        requireContext(), this@ServiceFragment, this@ServiceFragment,
                        requireContext().resources.getString(R.string.info_otp_sent),
                        false
                    )
                }
            }
        }
    }

    private fun setUpServiceIssueData() {

        val infoVenti = preferenceManager?.readVentiDetails()?.split(",")
        wardET.setText(infoVenti?.get(0) ?: "")
        hospitalET.setText(infoVenti?.get(1) ?: "")
        departmentET.setText(infoVenti?.get(2) ?: "")

        issueList.clear()
        issueList.add(ServiceIssueModel(requireContext().getString(R.string.issue_1), false))
        issueList.add(ServiceIssueModel(requireContext().getString(R.string.issue_2), false))
        issueList.add(ServiceIssueModel(requireContext().getString(R.string.issue_3), false))
        issueList.add(ServiceIssueModel(requireContext().getString(R.string.issue_4), false))
        issueList.add(ServiceIssueModel(requireContext().getString(R.string.issue_5), false))
        issueList.add(ServiceIssueModel(requireContext().getString(R.string.issue_6), false))
        issueList.add(ServiceIssueModel(requireContext().getString(R.string.issue_7), false))

        mIssueAdapter = IssueAdapter(issueList, this)
        mIssueLayoutManager = GridLayoutManager(requireContext(), 2)

        issueRecyclerView?.apply {
            layoutManager = mIssueLayoutManager
            adapter = mIssueAdapter
            isVerticalScrollBarEnabled = true
            itemAnimator = null
        }
    }

    private fun changeTextListener(){

        nameET.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_NEXT) {
                    if (nameET.text?.length!! < 5 || nameET?.text?.startsWith(" ")!!){
                        nameLayout.boxStrokeColor = requireContext().resources.getColor(R.color.red)
                    }
                    else {
                        contactNoET.requestFocus()
                    }
                    return true
                }
                return false
            }
        })

        contactNoET.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_NEXT) {
                    if (contactNoET.text?.length!! < 10 || contactNoET?.text?.startsWith("0")!!){
                        phoneLayout.boxStrokeColor = requireContext().resources.getColor(R.color.red)
                    }
                    else {
                        emailET.requestFocus()
                    }
                    return true
                }
                return false
            }
        })

        emailET.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_NEXT) {
                    if (!emailET.text?.matches(Regex(FormValidation.emailPattern))!!){
                        emailLayout.boxStrokeColor = requireContext().resources.getColor(R.color.red)
                    }
                    else {
                        wardET.requestFocus()
                    }
                    return true
                }
                return false
            }
        })


        wardET.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_NEXT) {
                    if (wardET.text?.isEmpty()!!){
                        WardLayout.boxStrokeColor = requireContext().resources.getColor(R.color.red)
                    }
                    else {
                        departmentET.requestFocus()
                    }
                    return true
                }
                return false
            }
        })

        departmentET.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_NEXT) {
                    if (departmentET.text?.isEmpty()!!){
                        DepartmentLayout.boxStrokeColor = requireContext().resources.getColor(R.color.red)
                    }
                    return true
                }
                return false
            }
        })

        nameET.addTextChangedListener {
            if (it?.length!! < 5 || it.startsWith(" ")){
                nameLayout.boxStrokeColor = requireContext().resources.getColor(R.color.red)
            }else{
                nameLayout.boxStrokeColor = requireContext().resources.getColor(R.color.green)
            }
        }

        contactNoET.addTextChangedListener {
            if (it?.length!! < 10 || it.startsWith("0")){
                phoneLayout.boxStrokeColor = requireContext().resources.getColor(R.color.red)
            }else{
                phoneLayout.boxStrokeColor = requireContext().resources.getColor(R.color.green)
            }
        }

        emailET.addTextChangedListener {
            if (!emailET.text?.matches(Regex(FormValidation.emailPattern))!!){
                emailLayout.boxStrokeColor = requireContext().resources.getColor(R.color.red)
            }else{
                emailLayout.boxStrokeColor = requireContext().resources.getColor(R.color.green)
            }
        }

        wardET.addTextChangedListener {
            if (wardET.text?.isEmpty()!!){
                WardLayout.boxStrokeColor = requireContext().resources.getColor(R.color.red)
            }else{
                WardLayout.boxStrokeColor = requireContext().resources.getColor(R.color.green)
            }
        }


        departmentET.addTextChangedListener {
            if (departmentET.text?.isEmpty()!!){
                DepartmentLayout.boxStrokeColor = requireContext().resources.getColor(R.color.red)
            }else{
                DepartmentLayout.boxStrokeColor = requireContext().resources.getColor(R.color.green)
            }
        }

    }
}