package com.agvahealthcare.ventilator_ext.exceptions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.agvahealthcare.ventilator_ext.MainActivity
import com.agvahealthcare.ventilator_ext.ShutDownActivity
import com.agvahealthcare.ventilator_ext.SplashActivity
import com.agvahealthcare.ventilator_ext.VentilatorApp
import com.agvahealthcare.ventilator_ext.VentilatorApp.Companion.currentActivityName
import com.agvahealthcare.ventilator_ext.api.ServerLogger
import com.agvahealthcare.ventilator_ext.dashboard.DashBoardActivity
import com.agvahealthcare.ventilator_ext.location.DefaultLocationClient
import com.agvahealthcare.ventilator_ext.location.LocationClient
import com.agvahealthcare.ventilator_ext.logging.FileLogger
import com.agvahealthcare.ventilator_ext.service.UsbService
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.DoubleArraySerializer
import org.jetbrains.anko.getStackTraceString
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class AppLevelExceptionHandler(var context: Context) : Thread.UncaughtExceptionHandler {

    private lateinit var locationClient : LocationClient
    private val serviceScope = CoroutineScope(Dispatchers.Main)

    @SuppressLint("SimpleDateFormat")
    private val dateTimeFormatter = SimpleDateFormat("YYYYMMdd_HHmmss")

    override fun uncaughtException(thread: Thread, exception: Throwable) {

        Log.i("ERROR_CHECK",exception.stackTraceToString())

        try {
            FileLogger.writeCrashFile(exception.stackTraceToString())
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("APP_EXCEPTION_HANDLER", e.localizedMessage)
        }

        when (currentActivityName) {

            "Splash" -> {
                Intent(context, SplashActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            }
            "Shutdown" -> {
                Intent(context, ShutDownActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            }

            "Main" -> {
                Intent(context, MainActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            }

            "Dash" -> {
                Intent(context, DashBoardActivity::class.java).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                }
            }
        }
        exitProcess(2)
    }

    infix fun Array<StackTraceElement>.printTrace(index: Int?) {
        if (index != null) {
            get(index).apply {
                Log.e("STACKTRACE POSITION $index", "==========================\n\n")
                Log.e("STACKTRACE FILENAME", this.fileName)
                Log.e("STACKTRACE METHOD", methodName)
                Log.e("STACKTRACE LINE NUMBER", lineNumber.toString())
                Log.e("STACKTRACE CLASSNAME", className)
            }
        } else {
            this.forEachIndexed { i, data ->
                data.apply {
                    Log.e("\nSTACKTRACE POSITION $i", "==========================\n\n")
                    Log.e("STACKTRACE FILENAME", this.fileName)
                    Log.e("STACKTRACE METHOD", methodName)
                    Log.e("STACKTRACE LINE NUMBER", lineNumber.toString())
                    Log.e("STACKTRACE CLASSNAME", className)
                }
            }
        }
    }
}