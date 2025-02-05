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
import kotlinx.android.synthetic.main.fragment_o_t_a.btnCancelDownload
import kotlinx.android.synthetic.main.fragment_o_t_a.otaRecyclerView
import kotlinx.android.synthetic.main.fragment_o_t_a.pbDownload
import kotlinx.android.synthetic.main.fragment_o_t_a.txtDownloadingStatus
import kotlinx.android.synthetic.main.fragment_o_t_a.txtNoDataOTA
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

    private var downloadController: DownloadController? = null
    private var downloadScope = CoroutineScope(Dispatchers.IO)
    private var showListScope = CoroutineScope(Dispatchers.IO)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_o_t_a, container, false)
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        btnCancelDownload.setOnClickListener {
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
                        txtDownloadingStatus.visibility = View.VISIBLE
                        btnCancelDownload.visibility = View.VISIBLE
                        pbDownload.visibility = View.VISIBLE
                        otaRecyclerView.visibility = View.INVISIBLE
                        pbDownload.progress = VentilatorApp.currentDownloadProgress
                        txtDownloadingStatus.text = "${VentilatorApp.currentDownloadProgress}%"
                    } else {
                        txtDownloadingStatus.visibility = View.GONE
                        btnCancelDownload.visibility = View.GONE
                        pbDownload.visibility = View.GONE
                        otaRecyclerView.visibility = View.VISIBLE
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
                    txtNoDataOTA.visibility = View.GONE
                    txtDownloadingStatus.visibility = View.GONE
                    btnCancelDownload.visibility = View.GONE
                    pbDownload.visibility = View.GONE
                    otaRecyclerView.visibility = View.VISIBLE
                    otaRecyclerView.adapter = OtaAdapter(it, this@OTAFragment)
                    showDownloadProgress()
                }
            } ?: run {
                withContext(Dispatchers.Main) {
                    txtNoDataOTA.visibility = View.VISIBLE
                    txtDownloadingStatus.visibility = View.GONE
                    btnCancelDownload.visibility = View.GONE
                    pbDownload.visibility = View.GONE
                    otaRecyclerView.visibility = View.INVISIBLE
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