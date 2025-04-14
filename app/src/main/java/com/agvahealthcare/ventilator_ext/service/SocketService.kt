package com.agvahealthcare.ventilator_ext.service

import android.content.Context
import android.util.Log
import com.agvahealthcare.ventilator_ext.utility.DialogBoxFactory
import com.agvahealthcare.ventilator_ext.utility.ToastFactory
import com.agvahealthcare.ventilator_ext.utility.utils.Configs
import com.agvahealthcare.ventilator_ext.utility.utils.FormValidation.runOnUiThread
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

class SocketService(private val ctx: Context):Socket() {

    var etco2Data = 0f

    var cuffPressure = 0f

     fun sendMessageToServer(message:String) {
         ToastFactory.custom(ctx,"Called")
        Thread {
            try {
                outputStream.write((message + "\n").toByteArray())
                Log.d("SocketClient", "Message sent: $message")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SocketClient", "Error sending message: ${e.message}")
            }
        }.start()
    }
     fun startReadingFromServer() {
        Thread {
            try {
                val reader = BufferedReader(InputStreamReader(inputStream))
                while (true) { // Continue reading in a loop
                    val message = reader.readLine()

                    Log.d("SocketClient", "Received message: $message")
                    if (message != null) {
                        runOnUiThread {
                            if (message.startsWith("ACK")) {
                                // Handle ACK messages
                                Log.i("CHECK_ACK_SOCKET",message.toString())
                                handleAckMessage(message)
                            } else {
                                // Handle other messages
                                handleMessage(message)
                            }
                        }
                    } else {
                        // Connection might have been closed by the server
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("SocketClient", "Error receiving message: ${e.message}")
            }
        }.start()
    }

    private fun handleAckMessage(ackMessage: String) {

        val ackCode = ackMessage.substring(3,ackMessage.length -1)
        val checkACK = ackCode.toString()
        Log.i("CHECK_ACK_LOG",checkACK)
        when (ackCode) {
            "5181" -> {
                Log.i("SocketClient", "Received ACK5181")
                DialogBoxFactory.showNeonateSensorDialog(ctx,"EtCuff inflation started")
            }
            "5183" -> {

                DialogBoxFactory.showNeonateSensorDialog(ctx,"Et-Cuff process stopped due to leakage")

            }

            "5189" ->{
                DialogBoxFactory.showNeonateSensorDialog(ctx,"Et-Cuff process stopped")
            }


            "5185" ->{
                DialogBoxFactory.showNeonateSensorDialog(ctx,"Et-Cuff deflation completed")
            }

            // Handle other ACK codes if needed
            else -> {
                // Handle unknown ACK codes or future ACK codes
                Log.i("SocketClientCode", "Received unknown ACK: $ackCode")
            }
        }
    }

    private fun handleMessage(message: String) {
        // Process regular messages here
        val cleanedMessage = message.substring(2, message.length - 1)
        val dataParts = cleanedMessage.split(",")
        if (dataParts.size >= 2) {
            val xValue = dataParts[1].toFloat()
            etco2Data = dataParts[0].toFloat()
            cuffPressure = dataParts[2].toFloat()

        }
    }
}