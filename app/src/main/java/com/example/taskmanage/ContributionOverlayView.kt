package com.example.taskmanage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*

class ContributionOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.parseColor("#8040B665") // Semi-transparent green overlay
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val currentDatePaint = Paint().apply {
        color = Color.parseColor("#FFB865") // Orange for current date
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var completedDates: Set<Long> = emptySet()
    private var currentDate: Long = Calendar.getInstance().timeInMillis

    fun setCompletedDates(dates: Set<Long>) {
        completedDates = dates
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val dotRadius = (width * 0.04f).coerceAtMost(height * 0.04f) // Slightly smaller radius
        val dotSpacing = width / 7f
        val verticalOffset = height * 0.11f
        val horizontalOffset = height * -0.065f // Adjust this value to move circles left or right

        for (date in completedDates) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = date
            }
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            val x = (dayOfWeek * dotSpacing) + (dotSpacing / 2) + horizontalOffset
            val y = (height / 2f) + verticalOffset // Adjusted y-position

            val paintToUse = if (date == currentDate) currentDatePaint else paint
            canvas.drawCircle(x, y, dotRadius, paintToUse)
        }
    }

}