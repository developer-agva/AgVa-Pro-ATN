package com.agvahealthcare.ventilator_ext.system.wifi


import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.R
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import kotlinx.android.synthetic.main.fragment_wi_fi.toggle_location_permission
import kotlinx.android.synthetic.main.fragment_wi_fi.toggle_storage_permission
import kotlinx.android.synthetic.main.fragment_wi_fi.toggle_wifi
import kotlinx.android.synthetic.main.fragment_wi_fi.txtConnectionStatus
import kotlinx.android.synthetic.main.fragment_wi_fi.txtNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class WiFiFragment : Fragment() {

    private var showConnectionStatusThread = CoroutineScope(Dispatchers.IO)
    private var wifiSSID = "agva_venti"
    private var wifiPass = "ag1234va"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wi_fi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtNote.text = "NOTE : Please Change Your Personal Hotspot Name With '$wifiSSID' And Password With '$wifiPass'"
        toggle_wifi.isOn = WifiUtils.withContext(requireContext()).isWifiConnected
        toggle_location_permission.isOn = (requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        toggle_storage_permission.isOn = requireActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        showConnectionStatusThread.launch {
            while (true) {
                withContext(Dispatchers.Main) {
                    if (WifiUtils.withContext(requireContext()).isWifiConnected) txtConnectionStatus.text =
                        "CONNECTED" else txtConnectionStatus.text = "DISCONNECTED"
                }
            }
        }.start()

        toggle_wifi.setOnToggledListener { _, isOn ->
            if (isOn) {
                WifiUtils.withContext(requireContext()).enableWifi()

                WifiUtils.withContext(requireContext())
                    .connectWith(wifiSSID, wifiPass)
                    .setTimeout(40000)
                    .onConnectionResult(object : ConnectionSuccessListener {
                        override fun success() {
                            txtConnectionStatus.text = "CONNECTED"
                        }

                        override fun failed(errorCode: ConnectionErrorCode) {
                            txtConnectionStatus.text = "DISCONNECTED"
                        }
                    })
                    .start()
            } else {
                WifiUtils.withContext(requireContext()).disableWifi()
            }
        }

        toggle_location_permission.setOnToggledListener { _, isOn ->
            if (isOn){
                if (requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Log.i("APP_EXCEPTION_HANDLER", "Already has all permissions")
                }
                else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION),
                        112
                    )
                }
            }
        }

        toggle_storage_permission.setOnToggledListener { _, isOn ->
            if (isOn){
                if (requireActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Log.i("APP_EXCEPTION_HANDLER", "Already has all permissions")
                }
                else {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        111
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        showConnectionStatusThread.cancel()
        super.onDestroy()
    }

}