package com.agvahealthcare.ventilator_ext.utility.seek_bar

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.drawable.Drawable
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class ThumbEntity(
    private val centerPosition: PointF,
    private var progress: Float,
    private val startAngle: Float,
    private val thumbRadius: Float,
    private val thumbDrawable: Drawable
) {

    companion object {
        private const val DEGREE_TO_RADIAN_RATIO = 0.0174533
        private const val RADIUS_ADJUSTMENT = 4
    }

    init {
        updatePosition(progress )
    }

    fun draw(canvas: Canvas, progress: Float) {
        this.progress = progress

        updatePosition(progress)

        thumbDrawable.draw(canvas)
    }

    private fun updatePosition(progress: Float) {
        val seekbarRadius = min(centerPosition.x, centerPosition.y) - thumbRadius -5

        val angle = (startAngle + (360 - 2 * startAngle) * progress) * DEGREE_TO_RADIAN_RATIO

        val indicatorX = centerPosition.x - sin(angle) * seekbarRadius
        val indicatorY = cos(angle) * (seekbarRadius) + centerPosition.y

        thumbDrawable.setBounds(
            (indicatorX  - thumbRadius).toInt() - RADIUS_ADJUSTMENT,
            (indicatorY - thumbRadius).toInt() - RADIUS_ADJUSTMENT ,
            (indicatorX + thumbRadius).toInt() + RADIUS_ADJUSTMENT,
            (indicatorY + thumbRadius).toInt() + RADIUS_ADJUSTMENT
        )

    }


}