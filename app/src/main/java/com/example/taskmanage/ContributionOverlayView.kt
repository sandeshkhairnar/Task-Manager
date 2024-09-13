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
        color = Color.parseColor("#8040B665") // More green, with a slight reduction in red
        style = Paint.Style.FILL
    }

    private val currentDatePaint = Paint().apply {
        color = Color.parseColor("#8040B665") // Orange color for the current date
        style = Paint.Style.FILL
    }

    private var completedDates: Set<Long> = emptySet()
    private var currentDate: Long = Calendar.getInstance().timeInMillis

    fun setCompletedDates(dates: Set<Long>) {
        completedDates = dates
        invalidate() // Request a redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val dotRadius = 50f // Increased dot radius for larger circles
        val dotSpacing = width / 7f // Adjust spacing if needed

        for (date in completedDates) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = date
            }
            // Calculate x and y position for the dot based on date
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
            val x = (dayOfWeek * dotSpacing) + (dotSpacing / 2)
            val y = height / 2f

            val paintToUse = if (date == currentDate) currentDatePaint else paint
            canvas.drawCircle(x, y, dotRadius, paintToUse)
        }
    }
}
