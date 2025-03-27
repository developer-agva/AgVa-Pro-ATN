package com.agvahealthcare.ventilator_ext.hl7comm

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.databinding.FragmentHL7CommunicationBinding
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.system.settings.CommonSetupAdapter
import com.agvahealthcare.ventilator_ext.system.settings.onDropDownSelectionListener
import com.agvahealthcare.ventilator_ext.utility.FIRST_FILTER_NAME
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.Gender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket


class HL7CommunicationFragment() : Fragment(), onDropDownSelectionListener {

    private lateinit var binding: FragmentHL7CommunicationBinding
    private var preferenceManager: PreferenceManager? = null
    private var mAdapter: CommonSetupAdapter? = null
    private var clickUhidLayout = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())

        val hl7Message = createHL7Message()
        sendHL7Message(hl7Message)
    }

    override fun onItemSelect(text: String, colorInt: Int) {

        binding.uhidRecyclerView.visibility = View.GONE
        mAdapter = null

        if (clickUhidLayout) {
            updateViewViaPreferences(text)
            binding.etUhid.setText(text)
        }
        clickUhidLayout = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHL7CommunicationBinding.inflate(layoutInflater, container, false)
        binding.root.setOnClickListener {
            binding.uhidRecyclerView.visibility = View.GONE
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etUhid.setText(preferenceManager?.readUHID())
        updateViewViaPreferences(preferenceManager!!.readUHID())
        setupOnClickListener()

        binding.etUhid.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(p0: TextView?, p1: Int, p2: KeyEvent?): Boolean {
                if (p1 == EditorInfo.IME_ACTION_DONE) {
                    updateViewViaPreferences(binding.etUhid.text.toString())
                    AppUtils.hideKeyBoard(requireContext(), binding.etUhid)
                    return true
                }
                return false
            }
        })
    }

    private fun updateViewViaPreferences(uhid: String) {

        preferenceManager?.apply {

            if (readDischargeDate(uhid) == "" && readAdmitDate(uhid) == "")
            {
                val data = FileLogger.readHL7File(uhid)
                if (data != FileLogger.dataNotFound){
                    setUHID(data.split(",")[0])
                    setFirstName(uhid,data.split(",")[1])
                    setLastName(uhid,data.split(",")[2])
                    setDOB(uhid,data.split(",")[3])
                    setContactNumber(uhid,data.split(",")[4])
                    setAdmitDate(uhid,data.split(",")[5])
                    setDischargeDate(uhid,data.split(",")[6])
                    setDoctorName(uhid,data.split(",")[7])
                    setPatientGender(uhid,if(data.split(",")[8] == "Male") Gender.TYPE_MALE else Gender.TYPE_FEMALE)
                }
            }

            binding.etFirstName.setText(readFirstName(uhid))
            binding.etLastName.setText(readLastName(uhid))
            binding.etDOB.setText(readDOB(uhid))
            binding.etGender.setText(if (readPatientGender(uhid) == Gender.TYPE_MALE) "MALE" else "FEMALE")
            binding.etEmergencyContact.setText(readContactNumber(uhid))
            binding.etAdmitDate.setText(readAdmitDate(uhid))
            binding.etDischargeDate.setText(readDischargeDate(uhid))
            binding.etDoctorName.setText(readDoctorName(uhid))
        }
    }

    private fun setupUhidLayout(uhidList: ArrayList<String>) {
        mAdapter = CommonSetupAdapter(uhidList, this)
        binding.uhidRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }


    private fun setupOnClickListener() {

        binding.ivUhid.setOnClickListener {
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

        binding.btnSendHl7Command.setOnClickListener {

            preferenceManager?.apply {
                if (readUHID() == FIRST_FILTER_NAME) setUHID(binding.etUhid.text.toString())
                setFirstName(binding.etUhid.text.toString(), binding.etFirstName.text.toString())
                setLastName(binding.etUhid.text.toString(), binding.etLastName.text.toString())
                setDOB(binding.etUhid.text.toString(), binding.etDOB.text.toString())
                setContactNumber(
                    binding.etUhid.text.toString(),
                    binding.etEmergencyContact.text.toString()
                )
                setAdmitDate(binding.etUhid.text.toString(), binding.etAdmitDate.text.toString())
                setDischargeDate(binding.etUhid.text.toString(), AppUtils.getCurrentDateTime())
                setDoctorName(binding.etUhid.text.toString(), binding.etDoctorName.text.toString())
                setPatientGender(
                    binding.etUhid.text.toString(),
                    if (binding.etGender.text.toString() == "MALE") Gender.TYPE_MALE else Gender.TYPE_FEMALE
                )
            }

            CoroutineScope(Dispatchers.Main).launch {
                preferenceManager?.apply {
                    val data =
                        "${readUHID()},${readFirstName(readUHID())},${readLastName(readUHID())},${
                            readDOB(readUHID())
                        },${readContactNumber(readUHID())},${readAdmitDate(readUHID())},${
                            readDischargeDate(
                                readUHID()
                            )
                        },${readDoctorName(readUHID())},${readPatientGender(readUHID())}|"

                    withContext(Dispatchers.IO){
                        FileLogger.writeHL7Fragment(requireContext(),data)
                    }
                }
            }

            ToastFactory.custom(requireContext(), "Requesting HL7..")
            val hl7Message = createHL7Message()
            sendHL7Message(hl7Message)
        }
    }

    private fun createHL7Message(): String {
        return try {
            // Constructing a basic HL7 message
            val patientId = binding.etUhid.text.toString()
            val patientLastName = binding.etLastName.text.toString()
            val patientFirstName = binding.etFirstName.text.toString()
            val dateOfBirth = binding.etDOB.text.toString() // Format: YYYYMMDD
            val gender = preferenceManager?.readPatientGender("UHID")

            // HL7 message format (ADT A01 Example)
            "MSH|^~\\&|HOSPITAL|DEPT|HFIR|HL7COMM|202503110930||ADT^A01|MSG1234|P|2.3\rPID|||$patientId||$patientLastName^$patientFirstName||$dateOfBirth|$gender\r"
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun sendHL7Message(hl7Message: String) {
        Thread {
            try {
                val socket = Socket("server-ip-address", 2575)
                val outputStream = socket.getOutputStream()

                // Send HL7 message
                outputStream.write((hl7Message + "\r").toByteArray())
                outputStream.flush()

                // Receive response
                val inputStream = socket.getInputStream()
                val response = ByteArray(1024)
                inputStream.read(response)

                Log.d("HL7Response", String(response))
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }


}