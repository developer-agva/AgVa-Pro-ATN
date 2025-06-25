package com.agvahealthcare.ventilator_ext.hl7comm

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import kotlinx.android.synthetic.main.fragment_h_l7_communication.btnSendData
import kotlinx.android.synthetic.main.fragment_h_l7_communication.etptDOB
import kotlinx.android.synthetic.main.fragment_h_l7_communication.etptName
import kotlinx.android.synthetic.main.fragment_h_l7_communication.ptGenderValue
import kotlinx.android.synthetic.main.fragment_h_l7_communication.spHL7CommValue
import kotlinx.android.synthetic.main.fragment_h_l7_communication.spUHID
import java.net.Socket


class HL7CommunicationFragment : Fragment() {

    private lateinit var dataStoreManager: DataStoreManager
    private var preferenceManager: PreferenceManager? = null

    private val versionList = listOf("version 2", "version 3")
    private val uhidList = listOf("abc123","add1231","asas1242","NAD1221","ffn3232","thfdg434")



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val hl7Message = createHL7Message()
//        Log.d("HL7MEssage", hl7Message)
//        sendHL7Message(hl7Message)
         preferenceManager = PreferenceManager(requireContext())

        val hl7Message = createHL7Message()
        sendHL7Message(hl7Message)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_h_l7_communication, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setUpAdapters()
        initData()
        setupOnClickListener()


    }



    private fun initData(){
        ptGenderValue.text = preferenceManager?.readGender().toString()

    }

    private fun setupOnClickListener(){
        etptDOB.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isEditing) return
                isEditing = true

                var cleanText = s.toString().replace("/", "") // Remove existing slashes
                val length = cleanText.length

                val formattedText = when {
                    length >= 4 -> "${cleanText.substring(0, 2)}/${cleanText.substring(2, 4)}/${cleanText.substring(4)}"
                    length >= 2 -> "${cleanText.substring(0, 2)}/${cleanText.substring(2)}"
                    else -> cleanText
                }

                etptDOB.setText(formattedText)
                etptDOB.setSelection(formattedText.length) // Move cursor to the end

                isEditing = false
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnSendData.setOnClickListener {

            ToastFactory.custom(requireContext(),"Clicked")
            val hl7Message = createHL7Message()
            sendHL7Message(hl7Message)
        }

    }

    private fun setUpAdapters(){
        val versionListadapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,versionList)
        versionListadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val uhidAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,uhidList)
        uhidAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        spUHID.adapter = uhidAdapter

        spUHID.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }


        spHL7CommValue.adapter = versionListadapter

        spHL7CommValue.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {


            }
        }
    }

    private fun createHL7Message(): String {
        return try {
            // Constructing a basic HL7 message
            val patientId = spUHID.selectedItem.toString()
            val patientLastName = etptName.text.toString()
            val patientFirstName = etptName.text.toString()
            val dateOfBirth = etptDOB.text.toString() // Format: YYYYMMDD
            val gender = preferenceManager?.readGender()

            // HL7 message format (ADT A01 Example)
            val hl7Message = "MSH|^~\\&|HOSPITAL|DEPT|HFIR|HL7COMM|202503110930||ADT^A01|MSG1234|P|2.3\r" +
                    "PID|||$patientId||$patientLastName^$patientFirstName||$dateOfBirth|$gender\r"
            Log.d("HL7Message", hl7Message)
            hl7Message
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