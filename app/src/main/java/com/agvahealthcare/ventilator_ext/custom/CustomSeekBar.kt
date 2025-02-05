package com.agvahealthcare.ventilator_ext.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.agvahealthcare.ventilator_ext.R
import kotlin.math.abs

open class CustomSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.LineChartStyle,
) : View(context, attrs, defStyleAttr) {

    private var paint = Paint()

    var min = 0f
    var max = 0f
    var progress = 0f
    var isMin : Boolean? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = resources.getColor(R.color.black)
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 3f

        canvas.drawRect(0f, (height / 2) + 5f, width.toFloat(), (height / 2) - 5f, paint)

        paint.color = resources.getColor(R.color.ack_red)
        paint.style = Paint.Style.FILL

        // find actual max value by getting difference of them
        val actualMax = abs(max-min)
        val steps = (width-20f) / actualMax

        // subtract min value from current progress so we can show progress from 0 to max Width on UI
        progress = (abs(progress - min) * steps) + 10f

        Log.d("SeekBarView", "Current Status : $progress ,$actualMax,$min ,$width")

        if (progress in 0f..width.toFloat()-10f) {
            canvas.drawCircle(progress, (height / 2).toFloat(), 10f, paint)
        }
    }

    fun changeProgress(progress : Float) {

        isMin = progress <= this.progress
        this.progress = progress
        invalidate()
    }

}