package com.agvahealthcare.ventilator_ext.system.ota

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.agvahealthcare.ventilator_ext.DownloadController
import com.agvahealthcare.ventilator_ext.R
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.downloadId
import com.agvahealthcare.ventilator_ext.api.ServerLogger
import com.agvahealthcare.ventilator_ext.databinding.FragmentDebugBinding
import com.agvahealthcare.ventilator_ext.databinding.FragmentOTABinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface OtaClickListener {
    fun onClickOta(url: String)
}

class OTAFragment : Fragment(), OtaClickListener {
    private lateinit var binding:FragmentOTABinding
    private var downloadController: DownloadController? = null
    private var downloadScope = CoroutineScope(Dispatchers.IO)
    private var showListScope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOTABinding.inflate(layoutInflater,container,false)
        return binding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        binding.btnCancelDownload.setOnClickListener {
            downloadController?.removeDownload()
        }
    }

    private fun showDownloadProgress() {

        downloadScope.launch {
            while (true) {
                delay(500L)
                withContext(Dispatchers.Main) {
                    Log.i("values_check_download","$downloadId")
                    if (downloadId != 0L) {
                        binding.txtDownloadingStatus.visibility = View.VISIBLE
                        binding.btnCancelDownload.visibility = View.VISIBLE
                        binding.pbDownload.visibility = View.VISIBLE
                        binding.otaRecyclerView.visibility = View.INVISIBLE
                        binding.pbDownload.progress = VentilatorApp.currentDownloadProgress
                        binding.txtDownloadingStatus.text = "${VentilatorApp.currentDownloadProgress}%"
                    } else {
                        binding.txtDownloadingStatus.visibility = View.GONE
                        binding.btnCancelDownload.visibility = View.GONE
                        binding.pbDownload.visibility = View.GONE
                        binding.otaRecyclerView.visibility = View.VISIBLE
                        VentilatorApp.currentDownloadProgress = 0
                    }
                }
            }
        }.start()
    }

    private fun setupAdapter() {

        showListScope.launch {
            ServerLogger.getAppHistory()?.let {
                withContext(Dispatchers.Main) {
                    binding.txtNoDataOTA.visibility = View.GONE
                    binding.txtDownloadingStatus.visibility = View.GONE
                    binding.btnCancelDownload.visibility = View.GONE
                    binding.pbDownload.visibility = View.GONE
                    binding.otaRecyclerView.visibility = View.VISIBLE
                    binding.otaRecyclerView.adapter = OtaAdapter(it, this@OTAFragment)
                    showDownloadProgress()
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    binding.txtNoDataOTA.visibility = View.VISIBLE
                    binding.txtDownloadingStatus.visibility = View.GONE
                    binding.btnCancelDownload.visibility = View.GONE
                    binding.pbDownload.visibility = View.GONE
                    binding.otaRecyclerView.visibility = View.INVISIBLE
                }
            }
        }.start()
    }

    override fun onClickOta(url: String) {
        downloadController = DownloadController(requireContext(), url, false)
        downloadController?.enqueueDownload()
    }

    override fun onDestroy() {
        showListScope.cancel()
        downloadScope.cancel()
        downloadController?.removeDownload()
        super.onDestroy()
    }
}