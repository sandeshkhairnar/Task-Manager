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
        textSize = 24f
        textAlign = Paint.Align.LEFT
    }
    private val rect = RectF()
    private var data: List<TaskSlice> = emptyList()
    private val centerHole = 0.6f
    private var selectedSliceIndex: Int = -1
    private val selectedSliceOffset = 20f
    private val minLabelDistance = 30f // Reduced for closer positioning
    private val labelPadding = 10f // Reduced for closer positioning
    private val maxLabelLength = 10
    private val labelOffset = 1.1f // Reduced to bring labels closer
    private val leftSideLabelOffset = 25f // Reduced for closer positioning

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

        val diameter = min(width, height).toFloat() * 0.8f // Increased chart size
        val radius = diameter / 2f - selectedSliceOffset
        val centerX = width / 2f
        val centerY = height / 2f

        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        val labelPositions = mutableListOf<Triple<PointF, String, Boolean>>()

        data.forEachIndexed { index, slice ->
            val sweepAngle = 360f * (slice.duration / total)
            paint.color = slice.color

            if (index == selectedSliceIndex) {
                val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                val offsetX = (cos(midAngle) * selectedSliceOffset).toFloat()
                val offsetY = (sin(midAngle) * selectedSliceOffset).toFloat()

                val selectedRect = RectF(rect)
                selectedRect.offset(offsetX, offsetY)
                canvas.drawArc(selectedRect, startAngle, sweepAngle, true, paint)
            } else {
                canvas.drawArc(rect, startAngle, sweepAngle, true, paint)
            }

            val midAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val labelRadius = radius * labelOffset + (if (index == selectedSliceIndex) selectedSliceOffset else 0f)
            var x = (centerX + cos(midAngle) * labelRadius).toFloat()
            var y = (centerY + sin(midAngle) * labelRadius).toFloat()

            // Adjust position for left side labels
            if (x < centerX) {
                x -= leftSideLabelOffset
            } else {
                x += leftSideLabelOffset
            }

            val truncatedName = if (slice.name.length > maxLabelLength)
                slice.name.substring(0, maxLabelLength) + "..."
            else
                slice.name

            labelPositions.add(Triple(PointF(x, y), truncatedName, index == selectedSliceIndex))

            startAngle += sweepAngle
        }

        val adjustedLabelPositions = adjustLabelPositions(labelPositions, centerX, centerY)

        adjustedLabelPositions.forEach { (point, label, isSelected) ->
            textPaint.textSize = 24f
            textPaint.isFakeBoldText = isSelected

            // Adjust text alignment based on position
            if (point.x < centerX) {
                textPaint.textAlign = Paint.Align.RIGHT
            } else {
                textPaint.textAlign = Paint.Align.LEFT
            }

            canvas.drawText(label, point.x, point.y, textPaint)

            if (isSelected) {
                val slice = data[selectedSliceIndex]
                val durationText = formatDuration(slice.duration)
                canvas.drawText(durationText, point.x, point.y + 30f, textPaint)
            }
        }

        paint.color = Color.WHITE
        canvas.drawCircle(centerX, centerY, radius * centerHole, paint)
    }

    private fun adjustLabelPositions(
        labelPositions: List<Triple<PointF, String, Boolean>>,
        centerX: Float,
        centerY: Float
    ): List<Triple<PointF, String, Boolean>> {
        val adjustedPositions = labelPositions.toMutableList()

        for (i in adjustedPositions.indices) {
            val (point, label, isSelected) = adjustedPositions[i]
            var newX = point.x
            var newY = point.y

            // Ensure labels don't go off-screen
            val textWidth = textPaint.measureText(label)
            if (newX < centerX) {
                newX = newX.coerceIn(textWidth, centerX - minLabelDistance)
            } else {
                newX = newX.coerceIn(centerX + minLabelDistance, width - textWidth)
            }
            newY = newY.coerceIn(textPaint.textSize, height - textPaint.textSize)

            for (j in 0 until i) {
                val (otherPoint, _, _) = adjustedPositions[j]
                val distance = sqrt((newX - otherPoint.x).pow(2) + (newY - otherPoint.y).pow(2))
                if (distance < minLabelDistance) {
                    val angle = atan2(newY - centerY, newX - centerX)
                    newX = (centerX + cos(angle) * (labelOffset * rect.width() / 2 + labelPadding)).toFloat()
                    newY = (centerY + sin(angle) * (labelOffset * rect.height() / 2 + labelPadding)).toFloat()
                }
            }

            adjustedPositions[i] = Triple(PointF(newX, newY), label, isSelected)
        }

        return adjustedPositions
    }

    // ... (onTouchEvent and formatDuration methods remain the same)


    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x - (width / 2f)
            val y = event.y - (height / 2f)

            var angle = Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
            if (angle < 0) angle += 360f

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