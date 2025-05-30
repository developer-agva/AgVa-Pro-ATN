package com.agvahealthcare.ventilator_ext.logging

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import com.agvahealthcare.ventilator_ext.manager.PreferenceManager
import com.agvahealthcare.ventilator_ext.utility.utils.AppUtils
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import java.io.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

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


        fun writeTrendGraphFile(
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

                        val fileData = file.readText().split("|") as ArrayList<String>
                        fileData.removeAt(fileData.size - 1)

                        // handle for duplicates
                        if (fileData[fileData.size - 1].split(",")[0] != data.split(",")[0]) {
                            if (fileData.size <= 144) {
                                isSuccess = true
                                val fileOutPutStream = FileOutputStream(file, true)
                                fileOutPutStream.write(if (data.contains("|")) data.toByteArray() else "$data|".toByteArray())
                                fileOutPutStream.close()
                            } else {
                                // create temp file
                                val tempFile = File(path, "temp_$fileName")
                                for (i in fileData.indices) {
                                    if (i != 0) {
                                        if (tempFile.exists()) {
                                            val fileOutPutStream = FileOutputStream(tempFile, true)
                                            fileOutPutStream.write(if (fileData[i].contains("|")) fileData[i].toByteArray() else "${fileData[i]}|".toByteArray())
                                            fileOutPutStream.close()
                                        } else {
                                            if (tempFile.createNewFile()) {
                                                val fileOutPutStream = FileOutputStream(tempFile)
                                                fileOutPutStream.write(if (fileData[i].contains("|")) fileData[i].toByteArray() else "${fileData[i]}|".toByteArray())
                                                fileOutPutStream.close()
                                            }
                                        }
                                    }
                                }
                                file.delete()
                                tempFile.renameTo(file)
                                writeTrendGraphFile(fileName, data)
                            }
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
                    Log.i("MasoomTesting", e.message.toString())
                }
            }
            return isSuccess
        }

        private fun generateTimeFrames(): ArrayList<String> {
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance().apply {
                set(Calendar.SECOND, 0)  // Optional, to clean up seconds
                set(Calendar.MILLISECOND, 0) // Optional, to clean up milliseconds
            }

            val ansForCurrentTime =
                formatter.format(calendar.time).toString().split(" ")[1].split(":")[1].toInt() % 10
            if (ansForCurrentTime != 0) calendar.add(Calendar.MINUTE, -ansForCurrentTime)

            val timeLabels = ArrayList<String>()

            for (i in 0 until 1440 step 10) {
                timeLabels.add(formatter.format(calendar.time))
                calendar.add(Calendar.MINUTE, -10)
            }

            timeLabels.reverse()
            return timeLabels
        }

        fun readTrendFileAndUpdateMissings(
            fileName: String,
            prefManager: PreferenceManager
        ): Boolean {
            var filePath = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "trend"
            )
            filePath = File(filePath, fileName)
            try {
                if (filePath.exists()) {

                    Log.i("FILEDATA", filePath.readText())
                    val fileData = filePath.readText().split("|") as ArrayList<String>

                    fileData.removeAt(fileData.size - 1)

                    val timeFrames = generateTimeFrames()

                    var t = 0
                    var d = 0

                    while (t < timeFrames.size && d < fileData.size) {

                        Log.i(
                            "MasoomTesting",
                            "${timeFrames[t]} - ${fileData[d].split(",")[0]} | ${fileData.size} - ${timeFrames.size}"
                        )
                        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                        val date1 = dateFormat.parse(timeFrames[t])
                        val date2 = dateFormat.parse(fileData[d].split(",")[0])

                        val calendar1 = Calendar.getInstance().apply { time = date1!! }
                        val calendar2 = Calendar.getInstance().apply { time = date2!! }

                        if (calendar1.before(calendar2)) {
                            val zeroTrends =
                                "${timeFrames[t]},NA,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,${prefManager.readUHID()}|"
                            writeTrendGraphFile("trends_timeframes_demo", zeroTrends)
                            t++
                        } else {
                            writeTrendGraphFile("trends_timeframes_demo", fileData[d] + "|")
                            d++
                        }
                    }

                    while (t < timeFrames.size) {
                        val zeroTrends =
                            "${timeFrames[t]},NA,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,${prefManager.readUHID()}|"
                        writeTrendGraphFile("trends_timeframes_demo", zeroTrends)
                        t++
                    }

                    while (d < fileData.size) {
                        writeTrendGraphFile("trends_timeframes_demo", fileData[d] + "|")
                        d++
                    }

                    filePath.delete()
                    File(
                        File(
                            Environment.getExternalStorageDirectory(),
                            AppUtils.PATH_FOLDER_AGVA + File.separator + "trend"
                        ), "trends_timeframes_demo"
                    ).renameTo(
                        File(
                            File(
                                Environment.getExternalStorageDirectory(),
                                AppUtils.PATH_FOLDER_AGVA + File.separator + "trend"
                            ),
                            Configs.trendTenMin
                        )
                    )

                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }

            return true
        }

        //reading of file
        fun readTrendFile(fileName: String, uhid: String, startIndex: Int, endIndex: Int): String {

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

                    fileData.reverse()

//                    // adding filter as per UHID
//                    fileData = (fileData.filter { s ->
//                        Log.i("Log.ia", s)
//                        s.split(",")[20] == uhid
//                    }) as ArrayList<String>

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

        fun readTrendFileAsPerParamAndDuration(
            fileName: String,
            paramIndex: Int,
            duration: String
        ): String {

            var requiredHours = 0
            var steps = 0

            var filePath = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + "trend"
            )

            when (duration) {
                "1 hour" -> requiredHours = 6
                "8 hours" -> requiredHours = 48
                "12 hours" -> requiredHours = 72
                "24 hours" -> requiredHours = 144
            }

            when (duration) {
                "1 hour" -> steps = 1
                "8 hours" -> steps = 6
                "12 hours" -> steps = 6
                "24 hours" -> steps = 6
            }

            filePath = File(filePath, fileName)
            try {

                if (filePath.exists()) {

                    var data = ""
                    val fileData = filePath.readText().split("|") as ArrayList<String>
                    fileData.removeAt(fileData.size - 1)
                    fileData.reverse()
                    var count = 0
                    // get data as per duration

                    // case : for 1 hour don't need to calculate the average for hour we only print every 10 min data
                    if (requiredHours == 6) {
                        for (i in 0 until fileData.size) {
                            if (count++ < requiredHours) data += fileData[i].split(",")[0].split(" ")[1] + "~" + fileData[i].split(
                                ","
                            )[paramIndex] + "|"
                        }
                    }
                    else {
                        var currentHours = AppUtils.getTimeForTrendsTime().split(":")[0].toInt()
                        var value = 0
                        for (i in 0 until if (requiredHours <= fileData.size) requiredHours else fileData.size) {

                            val time = fileData[i].split(",")[0].split(" ")[1]

                            if (time != (if (currentHours in 0.. 9) "0$currentHours:00" else "$currentHours:00")){
                                value += fileData[i].split(",")[paramIndex].toFloat().toInt()
                                value /= 2
                            }else{
                                if (currentHours == 0) currentHours = 23 else currentHours-- // to get the previous hour data
                                data += "$time~$value|"
                                value = 0
                            }
                            Log.i("SALIMTESTING", data)
                        }
                    }
                    if (data != "") {
                        val newData = data.substring(0, data.length - 1)
                        Log.i("testing_build", "What We Get : $newData")
                        return newData
                    } else return dataNotFound
                }

            } catch (e: Exception) {
                Log.i("value_lungs", e.message.toString())
                e.printStackTrace()
            }
            return dataNotFound
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
                            writeTrendLungsDynamicsFile(ctx, fileName, data)
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
                            writeHL7Fragment(ctx, "HL7")
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
        fun readHL7File(uhid: String): String {

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

                    for (i in 0 until fileData.size) {
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
                            writeEventFile(ctx, fileName, data)
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
                            writeEventFileDevelopers(ctx, fileName, data)
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
                            writeAlarmFile(ctx, fileName, data)
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

                    for (i in 0 until fileData.size) {
                        data += fileData[i].split(",")[2] + "|"
                    }
                    Log.i("data_get", data.toString())
                    return if (data == "") dataNotFound else data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClear", e.message.toString())
            }
            return dataNotFound
        }

        //reading of file
        fun readEventFile(fileName: String, uhid: String, startIndex: Int, endIndex: Int): String {

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
                        Log.i("Log.ia", s)
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
        fun readAlarmFile(fileName: String, uhid: String, startIndex: Int, endIndex: Int): String {

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
                        Log.i("Log.ia", s)
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

        // write dispatch date
        fun writeDispatchDate(ctx: Context, data: String): Boolean {
            var isSuccess = false

            val path = File(
                Environment.getExternalStorageDirectory(),
                AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_LOGS
            )
            val isPathAccessible = path.exists() || path.mkdirs()
            if (isPathAccessible) {
                Log.i(TAG, "Creating folder for AgVa")

                val file = File(path, "dispatch_date")
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

        fun readDispatchDate(): String {
            val filePath = Environment.getExternalStorageDirectory()
                .toString() + File.separator + AppUtils.PATH_FOLDER_AGVA + File.separator + AppUtils.PATH_FOLDER_LOGS + File.separator

            val file = File(filePath, "dispatch_date")
            try {
                if (file.exists()) {

                    val data = file.readText()
                    return data
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.i("dataClearAlarm", e.message.toString())
            }

            return dataNotFound
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
//            return "http://172.23.100.127:8000"
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