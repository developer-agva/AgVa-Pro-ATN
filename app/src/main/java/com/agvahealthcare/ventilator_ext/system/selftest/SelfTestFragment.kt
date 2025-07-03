package com.agvahealthcare.ventilator_ext.system.selftest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.agvahealthcare.ventilator_ext.MainActivityViewModel
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.service.CommunicationService


class SelfTestFragment(private var communicationService: CommunicationService?) : Fragment() {

    private var selfTestViewModel : SelfTestViewModel? = null
    private var preferenceManager : PreferenceManager? = null


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selfTestViewModel = ViewModelProvider(requireActivity())[SelfTestViewModel::class.java]
        preferenceManager = PreferenceManager(requireContext())
        initDataAsPerPreference()

        selfTestViewModel?.selfTestData?.observe(viewLifecycleOwner) { selfTestData ->
            if (selfTestData.isNotEmpty() && selfTestData != "null") {
                preferenceManager?.saveSelfTestData(selfTestData)
                initDataAsPerPreference()
            }
        }
    }

    private fun initDataAsPerPreference() {
        val selfTestData = preferenceManager?.readSelfTestData()
        if (!selfTestData.isNullOrEmpty()) {
            selfTestViewModel?.selfTestData?.value = selfTestData



        }
    }

}