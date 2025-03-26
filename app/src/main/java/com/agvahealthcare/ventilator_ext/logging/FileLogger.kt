package com.agvahealthcare.ventilator_ext.logging

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.logging.FileLogger.Companion.PATH_FOLDER_DEVELOPERS
import com.agvahealthcare.ventilator_ext.utility.VENTILATOR_DATA
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.time.Duration

/*
 * Created by MOHIT MALHOTRA
 *
 * FileLogger : Class is customized to log data
 * in a separate file under a specific app folder
 */

abstract class FileLogger {

    companion object {

        private const val TAG = "FileLogger"
        const val dataNotFound = "Data Not Found"
        private const val PATH_DEBUG_LOG_FILE = "debug_report"
        private const val PATH_EXCEPTION_LOG_FILE = "crash_report"
        private const val PATH_FOLDER_HOUR = ".Hours"
        private const val PATH_FOLDER_DEVELOPERS = ".Developers"

        @SuppressLint("SimpleDateFormat")
        private val fileDateTimeFormatter = SimpleDateFormat("YYYYMMdd_HHmmss")

        // writing of calibration file
        fun writeCalibrationData(
            data: String
        ): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                PATH_FOLDER_HOUR + File.separator + "calibration"
            )
            val isPathAccessible = path.exists() || path.mkdirs()

            try {
                if (isPathAccessible) {

                    val file = File(path, "calibration_data")
                    if (file.exists()) file.delete()

                    if (file.createNewFile()) {
                        val fileOutPutStream = FileOutputStream(file)
                        isSuccess = true
                        fileOutPutStream.write(data.toByteArray())
                        fileOutPutStream.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return isSuccess
        }

        //reading of calibration file
        fun readCalibrationData(): String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                PATH_FOLDER_HOUR + File.separator + "calibration"
            )
            filePath = File(filePath, "calibration_data")
            try {
                if (filePath.exists()) {
                    val data = filePath.readText()
                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }
            return dataNotFound
        }


        // save service hours
        fun writeServiceFile(
            data: String
        ): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                PATH_FOLDER_HOUR + File.separator + "Service"
            )
            val isPathAccessible = path.exists() || path.mkdirs()

            Log.i("valueGetOP", "helloasd")
            try {
                if (isPathAccessible) {

                    val file = File(path, "service_hours")
                    if (file.exists()) file.delete()

                    if (file.createNewFile()) {
                        val fileOutPutStream = FileOutputStream(file)
                        isSuccess = true
                        Log.i("valueGetOP", "$data new")
                        fileOutPutStream.write(data.toByteArray())
                        fileOutPutStream.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("valueGetOP", e.toString())
            }


            return isSuccess
        }

        //reading of file
        fun readServiceFile(): String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                PATH_FOLDER_HOUR + File.separator + "Service"
            )
            filePath = File(filePath, "service_hours")
            try {
                if (filePath.exists()) {
                    val data = filePath.readText()
                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }
            return dataNotFound
        }

        // save operating hours
        fun writeOPFile(
            data: String
        ): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                PATH_FOLDER_HOUR + File.separator + "OP"
            )
            val isPathAccessible = path.exists() || path.mkdirs()

            Log.i("valueGetOP", "helloasd")
            try {
                if (isPathAccessible) {

                    val file = File(path, "operating_hours")
                    if (file.exists()) file.delete()

                    if (file.createNewFile()) {
                        val fileOutPutStream = FileOutputStream(file)
                        isSuccess = true
                        Log.i("valueGetOP", "$data new")
                        fileOutPutStream.write(data.toByteArray())
                        fileOutPutStream.close()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("valueGetOP", e.toString())
            }


            return isSuccess
        }

        //reading of file
        fun readOPFile(): String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                PATH_FOLDER_HOUR + File.separator + "OP"
            )
            filePath = File(filePath, "operating_hours")
            try {
                if (filePath.exists()) {
                    val data = filePath.readText()
                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }
            return dataNotFound
        }

        fun writeAlarmFile(
            fileName: String,
            data: String,
        ): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "alarm"
            )
            val isPathAccessible = path.exists() || path.mkdirs()

            if (isPathAccessible) {

                val file = File(path, fileName)

                if (file.exists()) {
                    isSuccess = true
                    val fileOutPutStream = FileOutputStream(file, true)
                    fileOutPutStream.write(data.toByteArray())
                    fileOutPutStream.close()
                } else {
                    if (file.createNewFile()) {
                        isSuccess = true
                        val fileOutPutStream = FileOutputStream(file)
                        fileOutPutStream.write(data.toByteArray())
                        fileOutPutStream.close()
                    }
                }
            }
            return isSuccess
        }

        //reading of file
        fun readAlarmFile(fileName: String): String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "alarm"
            )
            filePath = File(filePath, fileName)
            try {
                if (filePath.exists()) {
                    val data = filePath.readText()
                    filePath.delete()
                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }
            return dataNotFound
        }

        fun writeTrendGraphFile(
            ctx: Context,
            fileName: String,
            data: String,
        ): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "trend"
            )
            val isPathAccessible = path.exists() || path.mkdirs()

            if (isPathAccessible) {

                val file = File(path, fileName)

                try {
                    if (file.exists()) {

                        val fileData = file.readLines()

                        if (fileData.size <= 2000) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file, true)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        } else {

                            // create temp file
                            val tempFile = File(path, "temp_$fileName")
                            for (i in fileData.indices) {
                                if (i != 0) {
                                    if (tempFile.exists()) {
                                        val fileOutPutStream = FileOutputStream(tempFile, true)
                                        fileOutPutStream.write(fileData[i].toByteArray())
                                        fileOutPutStream.close()
                                    } else {
                                        if (tempFile.createNewFile()) {
                                            val fileOutPutStream = FileOutputStream(tempFile)
                                            fileOutPutStream.write(fileData[i].toByteArray())
                                            fileOutPutStream.close()
                                        }
                                    }
                                }
                            }
                            file.delete()
                            tempFile.renameTo(file)
                            writeTrendGraphFile(ctx, fileName, data)
                        }

                    } else {
                        if (file.createNewFile()) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("asdaqe213", e.message.toString())
                }

            }
            return isSuccess
        }

        fun writeTrendLungsDynamicsFile(
            ctx: Context,
            fileName: String,
            data: String,
        ): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "lungs_dynamics"
            )
            val isPathAccessible = path.exists() || path.mkdirs()

            if (isPathAccessible) {

                val file = File(path, fileName)

                try {
                    if (file.exists()) {

                        val fileData = file.readLines()

                        if (fileData.size <= 2000) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file, true)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()

                        } else {

                            // create temp file
                            val tempFile = File(path, "temp_trend_lung_dynamics")
                            for (i in fileData.indices) {
                                if (i != 0) {
                                    if (tempFile.exists()) {
                                        val fileOutPutStream = FileOutputStream(tempFile, true)
                                        fileOutPutStream.write(fileData[i].toByteArray())
                                        fileOutPutStream.close()
                                    } else {
                                        if (tempFile.createNewFile()) {
                                            val fileOutPutStream = FileOutputStream(tempFile)
                                            fileOutPutStream.write(fileData[i].toByteArray())
                                            fileOutPutStream.close()
                                        }
                                    }
                                }
                            }
                            file.delete()
                            tempFile.renameTo(file)
                            writeTrendGraphFile(ctx, fileName, data)
                        }

                    } else {
                        if (file.createNewFile()) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("asdaqe213", e.message.toString())
                }
            }
            return isSuccess
        }

        fun writeHL7Fragment(
            ctx: Context,
            data: String,
        ): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "hl7"
            )
            val isPathAccessible = path.exists() || path.mkdirs()

            if (isPathAccessible) {

                val file = File(path, "HL7")

                try {
                    if (file.exists()) {

                        val fileData = file.readLines()

                        if (fileData.size <= 500) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file, true)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        } else {

                            // create temp file
                            val tempFile = File(path, "temp_hl7")
                            for (i in fileData.indices) {
                                if (i != 0) {
                                    if (tempFile.exists()) {
                                        val fileOutPutStream = FileOutputStream(tempFile, true)
                                        fileOutPutStream.write(fileData[i].toByteArray())
                                        fileOutPutStream.close()
                                    } else {
                                        if (tempFile.createNewFile()) {
                                            val fileOutPutStream = FileOutputStream(tempFile)
                                            fileOutPutStream.write(fileData[i].toByteArray())
                                            fileOutPutStream.close()
                                        }
                                    }
                                }
                            }
                            file.delete()
                            tempFile.renameTo(file)
                            writeTrendGraphFile(ctx, "HL7", data)
                        }

                    } else {
                        if (file.createNewFile()) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("asdaqe213", e.message.toString())
                }

            }
            return isSuccess
        }

        //reading of file
        fun readHL7File(uhid:String) : String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "hl7"
            )
            filePath = File(filePath, "HL7")
            try {

                if (filePath.exists()) {

                    var data = ""
                    val fileData = filePath.readText().split("|") as ArrayList<String>
                    fileData.removeAt(fileData.size - 1)
                    fileData.reverse()

                    for(i in 0 until fileData.size){
                        data += if (fileData[i].split(",")[0] == uhid) fileData[i] else dataNotFound
                    }

                    Log.i("value_check_hl7", fileData.size.toString() + " - " + data)

                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("value_check_hl7", e.message.toString())
            }
            return dataNotFound
        }

        fun writeEventFile(
            ctx: Context,
            fileName: String,
            data: String,
        ): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "event"
            )
            val isPathAccessible = path.exists() || path.mkdirs()

            if (isPathAccessible) {

                val file = File(path, fileName)

                try {
                    if (file.exists()) {

                        val fileData = file.readLines()

                        if (fileData.size <= 500) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file, true)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        } else {

                            // create temp file
                            val tempFile = File(path, "temp_event")
                            for (i in fileData.indices) {
                                if (i != 0) {
                                    if (tempFile.exists()) {
                                        val fileOutPutStream = FileOutputStream(tempFile, true)
                                        fileOutPutStream.write(fileData[i].toByteArray())
                                        fileOutPutStream.close()
                                    } else {
                                        if (tempFile.createNewFile()) {
                                            val fileOutPutStream = FileOutputStream(tempFile)
                                            fileOutPutStream.write(fileData[i].toByteArray())
                                            fileOutPutStream.close()
                                        }
                                    }
                                }
                            }
                            file.delete()
                            tempFile.renameTo(file)
                            writeTrendGraphFile(ctx, fileName, data)
                        }

                    } else {
                        if (file.createNewFile()) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("asdaqe213", e.message.toString())
                }

            }
            return isSuccess
        }


        fun writeEventFileDevelopers(
            ctx: Context,
            fileName: String,
            data: String,
        ): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                PATH_FOLDER_DEVELOPERS + File.separator + "Event_Developers"
            )
            val isPathAccessible = path.exists() || path.mkdirs()

            if (isPathAccessible) {

                val file = File(path, fileName)

                try {
                    if (file.exists()) {

                        val fileData = file.readLines()

                        if (fileData.size <= 500) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file, true)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        } else {

                            // create temp file
                            val tempFile = File(path, "temp_event_developers")
                            for (i in fileData.indices) {
                                if (i != 0) {
                                    if (tempFile.exists()) {
                                        val fileOutPutStream = FileOutputStream(tempFile, true)
                                        fileOutPutStream.write(fileData[i].toByteArray())
                                        fileOutPutStream.close()
                                    } else {
                                        if (tempFile.createNewFile()) {
                                            val fileOutPutStream = FileOutputStream(tempFile)
                                            fileOutPutStream.write(fileData[i].toByteArray())
                                            fileOutPutStream.close()
                                        }
                                    }
                                }
                            }
                            file.delete()
                            tempFile.renameTo(file)
                            writeTrendGraphFile(ctx, fileName, data)
                        }

                    } else {
                        if (file.createNewFile()) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("asdaqe213", e.message.toString())
                }

            }
            return isSuccess
        }

        fun writeAlarmFile(
            ctx: Context,
            fileName: String,
            data: String,
        ): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "alarm"
            )
            val isPathAccessible = path.exists() || path.mkdirs()

            if (isPathAccessible) {

                val file = File(path, fileName)

                try {
                    if (file.exists()) {

                        val fileData = file.readLines()

                        if (fileData.size <= 500) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file, true)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        } else {

                            // create temp file
                            val tempFile = File(path, "temp_alarm")
                            for (i in fileData.indices) {
                                if (i != 0) {
                                    if (tempFile.exists()) {
                                        val fileOutPutStream = FileOutputStream(tempFile, true)
                                        fileOutPutStream.write(fileData[i].toByteArray())
                                        fileOutPutStream.close()
                                    } else {
                                        if (tempFile.createNewFile()) {
                                            val fileOutPutStream = FileOutputStream(tempFile)
                                            fileOutPutStream.write(fileData[i].toByteArray())
                                            fileOutPutStream.close()
                                        }
                                    }
                                }
                            }
                            file.delete()
                            tempFile.renameTo(file)
                            writeTrendGraphFile(ctx, fileName, data)
                        }

                    } else {
                        if (file.createNewFile()) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.i("asdaqe213", e.message.toString())
                }

            }
            return isSuccess
        }

        fun readTrendFileAsPerParamAndDuration(fileName: String,paramIndex: Int, duration: Int): String {

            var durationCount = 0
            when(fileName){
                "trend_five_min" -> durationCount = 12
                "trend_two_min" -> durationCount = 30
                "trend_ten_min" -> durationCount = 6
            }

            val validDurationCount = duration * durationCount

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "trend"
            )
            filePath = File(filePath, fileName)
            try {

                if (filePath.exists()) {

                    var data = ""
                    val fileData = filePath.readText().split("|") as ArrayList<String>
                    fileData.removeAt(fileData.size - 1)
                    fileData.reverse()
                    // get data as per duration
                    if (fileData.size < validDurationCount) {
                        for (i in 0 until fileData.size) {
                            data += if (i != fileData.size - 1) fileData[i].split(",")[0].split(" ")[1] + "~" + fileData[i].split(",")[paramIndex] + "|"
                            else fileData[i].split(",")[0].split(" ")[1] + "~" + fileData[i].split(",")[paramIndex]
                        }
                    } else {
                        for (i in 0 until validDurationCount) {
                            data += if (i != validDurationCount - 1) fileData[i].split(",")[0].split(" ")[1] + "~" + fileData[i].split(",")[paramIndex] + "|"
                            else fileData[i].split(",")[0].split(" ")[1] + "~" + fileData[i].split(",")[paramIndex]
                        }
                    }

                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dataNotFound
        }

        fun readLungsDynamicsFile(paramIndex: Int): String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "lungs_dynamics"
            )
            filePath = File(filePath, "trend_lung_dynamics")

            try {
                if (filePath.exists()) {
                    val fileData = filePath.readText().split("|") as ArrayList<String>
                    fileData.removeAt(fileData.size - 1)
                    fileData.reverse()

                    return fileData[0].split(",")[paramIndex]
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return dataNotFound
        }

        //reading of file
        fun readTrendFile(fileName: String,uhid: String, startIndex: Int, endIndex: Int): String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "trend"
            )
            filePath = File(filePath, fileName)
            try {

                if (filePath.exists()) {


                    var data = ""
                    var fileData = filePath.readText().split("|") as ArrayList<String>
                    fileData.removeAt(fileData.size - 1)
                    // adding filter as per UHID
                    fileData = (fileData.filter { s ->
                        Log.i("Log.ia",s)
                        s.split(",")[20] == uhid

                    }) as ArrayList<String>

                    fileData.reverse()

                    Log.i("value_check_events", fileData.size.toString())

                    if (startIndex >= 0) {
                        if (endIndex <= fileData.size) {
                            for (i in startIndex until endIndex) {
                                data += fileData[i] + "|"
                            }
                        } else {
                            // for first 8 items
                            if (startIndex == 0) {
                                for (i in fileData.indices) {
                                    data += fileData[i] + "|"
                                }
                            }
                            // for last item
                            else data = dataNotFound
                        }
                    }
                    // since data not added
                    else data = dataNotFound

                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }
            return dataNotFound
        }

        //reading of file
        fun readUhidFile(fileName: String): String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "event"
            )
            filePath = File(filePath, fileName)
            try {

                if (filePath.exists()) {

                    var data = ""
                    val fileData = filePath.readText().split("|") as ArrayList<String>
                    fileData.removeAt(fileData.size - 1)
                    fileData.reverse()

                    for (i in 0 until fileData.size){
                        if (i == fileData.size-1) data += fileData[i].split(",")[2]
                        else  data += fileData[i].split(",")[2] + "|"
                    }
                    Log.i("data_get",data.toString())
                    return if (data == "") dataNotFound else data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }
            return dataNotFound
        }

        //reading of file
        fun readEventFile(fileName: String,uhid:String, startIndex: Int, endIndex: Int): String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "event"
            )
            Log.i("value_check_events", "$startIndex , $endIndex")
            filePath = File(filePath, fileName)
            try {

                if (filePath.exists()) {

                    var data = ""
                    var fileData = filePath.readText().split("|") as ArrayList<String>
                    fileData.removeAt(fileData.size - 1)
                    // adding filter as per UHID
                    fileData = (fileData.filter { s ->
                        Log.i("Log.ia",s)
                        s.split(",")[2] == uhid

                    }) as ArrayList<String>

                    fileData.reverse()

                    Log.i("value_check_events", fileData.size.toString())

                    if (startIndex >= 0) {
                        if (endIndex <= fileData.size) {
                            for (i in startIndex until endIndex) {
                                data += fileData[i] + "|"
                            }
                        } else {
                            // for first 8 items
                            if (startIndex == 0) {
                                for (i in fileData.indices) {
                                    data += fileData[i] + "|"
                                }
                            }
                            // for last item
                            else if (startIndex < fileData.size) {
                                for (i in startIndex until fileData.size) {
                                    data += fileData[i] + "|"
                                }
                            } else data = dataNotFound
                        }
                    }
                    // since data not added
                    else data = dataNotFound

                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }
            return dataNotFound
        }

        //reading of file
        fun readEventFileDevelopers(fileName: String): String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                PATH_FOLDER_DEVELOPERS + File.separator + "Event_Developers"
            )
            filePath = File(filePath, fileName)
            try {

                if (filePath.exists()) {
                    return filePath.readText()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }
            return dataNotFound
        }

        //reading of alarm file
        fun readAlarmFile(fileName: String,uhid: String, startIndex: Int, endIndex: Int): String {

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "alarm"
            )
            filePath = File(filePath, fileName)
            try {

                if (filePath.exists()) {

                    var data = ""
                    var fileData = filePath.readText().split("|") as ArrayList<String>
                    fileData.removeAt(fileData.size - 1)
                    // adding filter as per UHID
                    fileData = (fileData.filter { s ->
                        Log.i("Log.ia",s)
                        s.split(",")[3] == uhid

                    }) as ArrayList<String>

                    fileData.reverse()

                    Log.i("value_check_events", fileData.size.toString())

                    if (startIndex >= 0) {
                        if (endIndex <= fileData.size) {
                            for (i in startIndex until endIndex) {
                                data += fileData[i] + "|"
                            }
                        } else {
                            // for first 8 items
                            if (startIndex == 0) {
                                for (i in fileData.indices) {
                                    data += fileData[i] + "|"
                                }
                            }
                            // for last item
                            else if (startIndex < fileData.size) {
                                for (i in startIndex until fileData.size) {
                                    data += fileData[i] + "|"
                                }
                            } else data = dataNotFound
                        }
                    }
                    // since data not added
                    else data = dataNotFound

                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }
            return dataNotFound
        }


        fun d(ctx: Context?, err: Throwable) {
            if (ctx == null) {
                Log.w(TAG, "Context is null, Unable to log data")
                return
            }

            d(ctx, err.stackTraceToString())
        }

        fun d(ctx: Context, data: String) {
            if (!writeFile(ctx, PATH_DEBUG_LOG_FILE, data)) {
                Log.w(TAG, "Unable to write to " + PATH_DEBUG_LOG_FILE)
            }
        }

        fun e(ctx: Context?, err: Throwable) {
            if (ctx == null) {
                Log.w(TAG, "Context is null, Unable to log data")
                return
            }
            e(ctx, err.stackTraceToString())
        }

        fun e(ctx: Context, data: String) {
            if (!writeFile(ctx, PATH_EXCEPTION_LOG_FILE, data)) {
                Log.w(TAG, "Unable to write to " + PATH_EXCEPTION_LOG_FILE)
            }
        }

        // write crash file
        fun writeModeFile(ctx: Context, data: String): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_MODE
            )
            val isPathAccessible = path.exists() || path.mkdirs()
            if (isPathAccessible) {
                Log.i(TAG, "Creating folder for AgVa")

                val file = File(path, "mode")
                try {
                    if (file.exists()) {

                        isSuccess = true
                        val fileOutPutStream = FileOutputStream(file)
                        fileOutPutStream.write(data.toByteArray())
                        fileOutPutStream.close()

                    } else {
                        if (file.createNewFile()) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return isSuccess
        }

        fun readModeFile(): String {
            val filePath = Environment.getExternalStorageDirectory()
                .toString() + File.separator + AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_MODE + File.separator

            val file = File(filePath, "mode")
            var data = dataNotFound
            try {
                if (file.exists()) {
                    data = file.readText()
                    return data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return data
        }

        // write crash file
        fun writeCrashFile(data: String): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_LOGS
            )
            val isPathAccessible = path.exists() || path.mkdirs()
            if (isPathAccessible) {
                Log.i(TAG, "Creating folder for AgVa")

                val file = File(path, "crash")
                try {
                    if (file.createNewFile()) {
                        isSuccess = true
                        val fileOutPutStream = FileOutputStream(file)
                        fileOutPutStream.write(data.toByteArray())
                        fileOutPutStream.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            return isSuccess
        }

        fun readCrashFile(): String {
            val filePath = Environment.getExternalStorageDirectory()
                .toString() + File.separator + AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_LOGS + File.separator

            val file = File(filePath, "crash")
            var data = dataNotFound
            try {
                if (file.exists()) {
                    data = file.readText()
                    return data
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return data
        }

        fun deleteCrashFile() {
            val filePath = Environment.getExternalStorageDirectory()
                .toString() + File.separator + AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_LOGS + File.separator

            val file = File(filePath, "crash")
            file.delete()
        }

        fun writeBaseUrl(ctx: Context, data: String): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_LOGS
            )
            val isPathAccessible = path.exists() || path.mkdirs()
            if (isPathAccessible) {
                Log.i(TAG, "Creating folder for AgVa")

                val file = File(path, "baseurl")
                try {
                    if (file.exists()) {

                        isSuccess = true
                        val fileOutPutStream = FileOutputStream(file)
                        fileOutPutStream.write(data.toByteArray())
                        fileOutPutStream.close()

                    } else {
                        if (file.createNewFile()) {
                            isSuccess = true
                            val fileOutPutStream = FileOutputStream(file)
                            fileOutPutStream.write(data.toByteArray())
                            fileOutPutStream.close()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return isSuccess
        }

        fun readBaseUrl(): String {
            val filePath = Environment.getExternalStorageDirectory()
                .toString() + File.separator + AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_LOGS + File.separator

            val file = File(filePath, "baseurl")
            try {
                if (file.exists()) {

                    val data = file.readText()
                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClearAlarm", e.message.toString())
            }

            return "http://3.25.213.83:8000"
        }

        //writing of file
        private fun writeFile(ctx: Context, fileName: String, data: String): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_LOGS
            )
            val isPathAccessible = path.exists() || path.mkdirs()
            if (isPathAccessible) {
                Log.i(TAG, "Creating folder for AgVa")

                val file = File(path, "${fileName}_${fileDateTimeFormatter.format(Date())}.log")
                try {
                    if (file.createNewFile()) {
                        FileWriter(file, true).use {
                            it.write(data)
                            it.flush()
                            isSuccess = true
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return isSuccess
        }

        //reading of file
        private fun readFile(ctx: Context, fileName: String): String? {
            val filePath = Environment.getExternalStorageDirectory()
                .toString() + File.separator + AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_LOGS + File.separator + "${fileName}_${
                fileDateTimeFormatter.format(
                    Date()
                )
            }.log"
            try {
                ctx.openFileInput(filePath).use { fis ->
                    try {
                        BufferedReader(InputStreamReader(fis)).use { reader ->
                            val buffer = StringBuffer()
                            var line: String? = ""
                            while (reader.readLine().also { line = it } != null) buffer.append(line)
                            return buffer.toString()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

    }


}