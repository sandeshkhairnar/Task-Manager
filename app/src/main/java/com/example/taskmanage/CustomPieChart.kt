package com.example.taskmanage

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

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
    private var selectedSliceIndex: Int = -1
    private val selectedSliceOffset = 20f // How much to offset the selected slice
    private val minLabelDistance = 120f // Increased minimum distance between label and pie edge
    private val maxLabelLength = 10 // Maximum number of characters for a label before truncating

    data class TaskSlice(val name: String, val duration: Int, val color: Int)

    fun setData(newData: List<TaskSlice>) {
        data = newData
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val total = data.sumOf { it.duration.toDouble() }.toFloat()
        var startAngle = 0f

        val diameter = min(width, height).toFloat() * 0.7f // Further reduced chart size to 70%
        val radius = diameter / 2f - selectedSliceOffset
        val centerX = width / 2f
        val centerY = height / 2f

        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        data.forEachIndexed { index, slice ->
            val sweepAngle = 360f * (slice.duration / total)
            paint.color = slice.color

            if (index == selectedSliceIndex) {
                // Draw selected slice larger
                val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                val offsetX = (cos(midAngle) * selectedSliceOffset).toFloat()
                val offsetY = (sin(midAngle) * selectedSliceOffset).toFloat()

                val selectedRect = RectF(rect)
                selectedRect.offset(offsetX, offsetY)
                canvas.drawArc(selectedRect, startAngle, sweepAngle, true, paint)
            } else {
                canvas.drawArc(rect, startAngle, sweepAngle, true, paint)
            }

            // Draw labels
            val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val labelRadius = radius * 1.2f + (if (index == selectedSliceIndex) selectedSliceOffset else 0f)
            var x = (centerX + cos(midAngle) * labelRadius).toFloat()
            var y = (centerY + sin(midAngle) * labelRadius).toFloat()

            // Adjust label position if it's too close to the edge
            val distanceToRightEdge = width - x
            val distanceToLeftEdge = x
            val distanceToTopEdge = y
            val distanceToBottomEdge = height - y

            if (distanceToRightEdge < minLabelDistance) {
                x = width - minLabelDistance
            } else if (distanceToLeftEdge < minLabelDistance) {
                x = minLabelDistance
            }

            if (distanceToTopEdge < minLabelDistance) {
                y = minLabelDistance
            } else if (distanceToBottomEdge < minLabelDistance) {
                y = height - minLabelDistance
            }

            // Truncate label if it's too long
            val truncatedName = if (slice.name.length > maxLabelLength)
                slice.name.substring(0, maxLabelLength) + "..."
            else
                slice.name

            // Increase text size for selected slice and show duration
            if (index == selectedSliceIndex) {
                textPaint.textSize = 40f
                textPaint.isFakeBoldText = true
                canvas.drawText(truncatedName, x, y, textPaint)
                canvas.drawText(formatDuration(slice.duration), x, y + 45f, textPaint)
            } else {
                textPaint.textSize = 30f
                textPaint.isFakeBoldText = false
                canvas.drawText(truncatedName, x, y, textPaint)
            }

            startAngle += sweepAngle
        }

        // Draw center hole
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius * centerHole, paint)
    }

    // ... (rest of the class remains the same)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x - (width / 2f)
            val y = event.y - (height / 2f)

            // Calculate the angle of the touch point
            var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
            if (angle < 0) angle += 360f

            // Find which slice was touched
            var startAngle = 0f
            val total = data.sumOf { it.duration.toDouble() }.toFloat()

            data.forEachIndexed { index, slice ->
                val sweepAngle = 360f * (slice.duration / total)
                if (angle >= startAngle && angle < startAngle + sweepAngle) {
                    selectedSliceIndex = if (selectedSliceIndex == index) -1 else index
                    invalidate()
                    return true
                }
                startAngle += sweepAngle
            }
        }
        return super.onTouchEvent(event)
    }

    private fun formatDuration(durationInSeconds: Int): String {
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        val seconds = durationInSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }


}