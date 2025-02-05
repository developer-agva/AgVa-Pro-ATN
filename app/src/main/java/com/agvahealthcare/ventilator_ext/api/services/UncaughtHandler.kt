package com.agvahealthcare.ventilator_ext.api.services

import android.os.Handler


internal class UncaughtHandler(handler: Handler) : Thread.UncaughtExceptionHandler {

    private val mHandler: Handler
    init {
        mHandler = handler
    }
    override fun uncaughtException(thread: Thread?, e: Throwable?) {
        mHandler.post(Runnable { throw Exception(e) })
    }



}