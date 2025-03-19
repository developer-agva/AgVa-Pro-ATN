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
import com.agvahealthcare.ventilator_ext.databinding.FragmentHL7CommunicationBinding
import com.agvahealthcare.ventilator_ext.manager.DataStoreManager
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs.Gender
import java.net.Socket


class HL7CommunicationFragment : Fragment() {

    private lateinit var binding: FragmentHL7CommunicationBinding
    private var preferenceManager: PreferenceManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())

        val hl7Message = createHL7Message()
        sendHL7Message(hl7Message)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentHL7CommunicationBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()
        setupOnClickListener()
    }

    private fun initData() {

        preferenceManager?.apply {
            binding.etGender.setText(if (readGender() == Gender.TYPE_MALE) "Male" else "Female")
            binding.etUhid.setText(readUHID())
            binding.etDoctorName.setText(readDoctorName())
            binding.etFirstName.setText(readFirstName())
            binding.etLastName.setText(readLastName())
            binding.etDischargeDate.setText(readDischargeDate())
            binding.etAdmitDate.setText(readAdmitDate())
            binding.etDOB.setText(readDOB())
            binding.etEmergencyContact.setText(readContactNumber())
        }
    }

    private fun setupOnClickListener() {

        binding.btnDischargeNow.setOnClickListener {

            preferenceManager?.apply {
                setDoctorName(binding.etDoctorName.text.toString())
                setFirstName(binding.etFirstName.text.toString())
                setLastName(binding.etLastName.text.toString())
                setEmergencyContact(binding.etEmergencyContact.text.toString())
                setDOB(binding.etDOB.text.toString())
            }

            ToastFactory.custom(requireContext(), "Clicked")
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
            val gender = preferenceManager?.readGender()

            // HL7 message format (ADT A01 Example)
            val hl7Message =
                "MSH|^~\\&|HOSPITAL|DEPT|HFIR|HL7COMM|202503110930||ADT^A01|MSG1234|P|2.3\r" +
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