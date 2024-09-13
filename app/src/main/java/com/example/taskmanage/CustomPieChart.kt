package com.example.taskmanage

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CustomPieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.LEFT
    }
    private val rect = RectF()
    private var data: List<TaskSlice> = emptyList()
    private val centerHole = 0.6f // Percentage of radius to leave as hole

    data class TaskSlice(val name: String, val duration: Float, val color: Int)

    fun setData(newData: List<TaskSlice>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val total = data.sumOf { it.duration.toDouble() }.toFloat()
        var startAngle = 0f

        val diameter = min(width, height).toFloat()
        val radius = diameter / 2f
        val centerX = width / 2f
        val centerY = height / 2f

        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        data.forEach { slice ->
            val sweepAngle = 360f * (slice.duration / total)
            paint.color = slice.color
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)

            // Draw labels
            val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val labelRadius = radius * 1.1f // Place label just outside the pie
            val x = (centerX + cos(midAngle) * labelRadius).toFloat()
            val y = (centerY + sin(midAngle) * labelRadius).toFloat()

            canvas.drawText(slice.name, x, y, textPaint)

            startAngle += sweepAngle
        }

        // Draw center hole
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius * centerHole, paint)
    }
}