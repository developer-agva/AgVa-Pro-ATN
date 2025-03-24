package com.agvahealthcare.ventilator_ext.system.wifi


import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.agvahealthcare.ventilator_ext.databinding.FragmentWiFiBinding
import com.thanosfisherman.wifiutils.WifiUtils
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class   WiFiFragment : Fragment() {

    private var showConnectionStatusThread = CoroutineScope(Dispatchers.IO)
    private var wifiSSID = "agva_venti"
    private var wifiPass = "ag1234va"
    private var wifiManager: WifiManager? = null
    private lateinit var binding:FragmentWiFiBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWiFiBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        wifiManager = requireContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        binding.txtNote.text = "NOTE : Please Change Your Personal Hotspot Name With '$wifiSSID' And Password With '$wifiPass'"
        binding.toggleWifi.isOn = WifiUtils.withContext(requireContext()).isWifiConnected
        binding.toggleLocationPermission.isOn = (requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && requireActivity().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        binding.toggleStoragePermission.isOn = requireActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

        showConnectionStatusThread.launch {
            while (true) {
                withContext(Dispatchers.Main) {
                    if (WifiUtils.withContext(requireContext()).isWifiConnected) {
                        binding.txtConnectionStatus.text = "CONNECTED"
                        getIpAddress(wifiManager)
                    } else {
                        binding.txtConnectionStatus.text = "DISCONNECTED"
                        binding.txtIpAddress.text = "IP Address Not Found"
                    }
                }
            }
        }.start()

        binding.toggleWifi.setOnToggledListener { _, isOn ->
            if (isOn) {
                WifiUtils.withContext(requireContext()).enableWifi()

                WifiUtils.withContext(requireContext())
                    .connectWith(wifiSSID, wifiPass)
                    .setTimeout(40000)
                    .onConnectionResult(object : ConnectionSuccessListener {
                        override fun success() {
                            binding.txtConnectionStatus.text = "CONNECTED"
                            getIpAddress(wifiManager)
                        }

                        override fun failed(errorCode: ConnectionErrorCode) {
                            binding.txtConnectionStatus.text = "DISCONNECTED"
                            binding.txtIpAddress.text = "IP Address Not Found"
                        }
                    })
                    .start()
            } else {
                WifiUtils.withContext(requireContext()).disableWifi()
            }
        }

        binding.toggleLocationPermission.setOnToggledListener { _, isOn ->
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

        binding.toggleStoragePermission.setOnToggledListener { _, isOn ->
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

    // command to connect via wifi
    // adb tcpip 5555
    // adb connect ipaddress:5555


    private fun getIpAddress(wifiManager: WifiManager?) {
        try {
            val ipAddress =
                Formatter.formatIpAddress(wifiManager!!.connectionInfo.ipAddress).toString()
            Log.i("macAddressOfAndroid", ipAddress)
            binding.txtIpAddress.text = "IP Address : $ipAddress:5555"
        } catch (e: Exception) {
            binding.txtIpAddress.text = "IP Address Not Found"
            e.printStackTrace()
        }
    }


    override fun onDestroy() {
        showConnectionStatusThread.cancel()
        super.onDestroy()
    }

}