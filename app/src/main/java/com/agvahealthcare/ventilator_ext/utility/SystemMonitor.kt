package com.agvahealthcare.ventilator_ext.utility

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class SystemMonitor {

    companion object
    {
        const val TAG = "SYSMONITOR"
        const val MONITOR_INTERVAL = 1000 * 10L
        const val MONITOR_LIFECYCLE = 1000 * 60 * 60 * 24 * 2L

        @SuppressLint("SimpleDateFormat") private val dateTimeFormatter = SimpleDateFormat("YYYYMMdd_HHmmss")

        private var timer: CountDownTimer?  = null

        private fun getMemoryInfo(): InputStream = Runtime.getRuntime().exec("dumpsys meminfo").inputStream
        private fun getCpuInfo(): InputStream = Runtime.getRuntime().exec("dumpsys cpuinfo").inputStream

        private fun createSnapshot(): Boolean {

            var isSuccess = false
            val path = File(Environment.getExternalStorageDirectory(), AppUtils.PATH_FOLDER_AGVA +  File.separator + AppUtils.PATH_FOLDER_SYSSNAPSHOT )
            val isPathAccessible = path.exists() || path.mkdirs()

            if (isPathAccessible) {

                Log.i(TAG, "Creating folder for AgVa")

                val file = File(path, "SYSINFO_${dateTimeFormatter.format(Date())}.snapshot")

                try {

                    if(file.createNewFile()){
                        FileWriter(file, true).use { writer ->

                            val memoryReader = getMemoryInfo().bufferedReader()
                            val cpuReader = getCpuInfo().bufferedReader()

                            writer.write("================= MEMORY SNAPSHOT =================\n")
                            memoryReader.lines().forEach{ writer.write("$it\n") }
                            memoryReader.close()


                            writer.write("\n\n================= CPU SNAPSHOT =================\n")
                            cpuReader.lines().forEach{ writer.write("$it\n") }
                            cpuReader.close()

                            //Need not to declare the flush here already the close function is called.

                            writer.flush()
                            isSuccess = true
                        }
                    }


                } catch (e: Exception) {
                    Log.e(TAG, "Unable to write the file due to ${e.localizedMessage}")
                    e.printStackTrace()
                }
            }
            return isSuccess
        }

 /*       fun start(){
            if(timer != null) return

            timer = object: CountDownTimer(MONITOR_LIFECYCLE, MONITOR_INTERVAL){


                override fun onTick(p0: Long) {
                    //if(createSnapshot()) Log.i(SystemMonitor.TAG, "Created file successfully")
                   // else  Log.i(SystemMonitor.TAG, "Unable to write the file")
                }

                override fun onFinish() {
                    Log.i(SystemMonitor.TAG, "Sys monitor thread closed")
                }

            }

            timer?.start()
        }

        fun stop(){
            timer?.cancel()
            timer = null
        }*/
    }






}