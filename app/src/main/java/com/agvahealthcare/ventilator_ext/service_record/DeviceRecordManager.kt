
package com.agvahealthcare.ventilator_ext.service_record

import android.annotation.SuppressLint
import android.os.Environment
import android.util.Log
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class DeviceRecordManager {

    companion object {
        const val TAG = "SERVICEMODULE"
        const val FILENAME_INCEPTION = "inception.json"
        const val COUNT_INTERVAL = 1000 * 60  // 1 min
        const val COUNT_LIFESPAN = 1000 * 60 * 60 * 24 // 24 hours

        @SuppressLint("SimpleDateFormat") private val dateTimeFormatter = AppUtils.dateTimeFormatter

        fun readInceptionRecord() : DeviceInceptionRecord?{
            val inceptionFile = File(Environment.getExternalStorageDirectory(), AppUtils.PATH_FOLDER_AGVA +  File.separator + AppUtils.PATH_FOLDER_SERVICE_AND_OP_MOD + File.separator + FILENAME_INCEPTION)
            if(inceptionFile.exists() && inceptionFile.canRead()) {
                val inceptionDate = FileReader(inceptionFile).use {
                    try {
                        Gson().fromJson(it.readText(), DeviceInceptionRecord::class.java)
                    } catch (e: JsonSyntaxException){
                        e.printStackTrace()
                        Log.i(TAG, "FILE MAY BE TEMPERED !")
                        null
                    }

                }

                return inceptionDate
            }

            return null
        }

        fun createInceptionRecord() : Boolean{
            val inceptionDateTime = dateTimeFormatter.format(Date())
            var isSuccess = false
            val path = File(Environment.getExternalStorageDirectory(), AppUtils.PATH_FOLDER_AGVA +  File.separator + AppUtils.PATH_FOLDER_SERVICE_AND_OP_MOD )
            val isPathAccessible = path.exists() || path.mkdirs()
            if(isPathAccessible){

                Log.i(TAG, "Creating folder for AgVa")

                val file = File(path, FILENAME_INCEPTION)
                try {
                    val isFileAccessible = file.exists() || file.createNewFile()

                    if(isFileAccessible){
                        FileWriter(file, false).use {
                                writer -> {
                                val inceptionJson = Gson().toJson(DeviceInceptionRecord(inceptionDateTime)).toString()
                                writer.write(inceptionJson)
                            Log.i("INCEPTION_DATE_TIME",inceptionDateTime.toString())
                            }
                        }

                        if(file.setReadOnly()) Log.i(TAG, "Inception file is READ ONLY")
                    }
                    isSuccess = true

                } catch (e: Exception){
                    Log.e(TAG,"Unable to write the file due to ${e.localizedMessage}")
                    e.printStackTrace()
                }

            }
            return isSuccess
        }


    }
}
