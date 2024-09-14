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
        isAntiAlias = true // For smoother circles
    }

    private val currentDatePaint = Paint().apply {
        color = Color.parseColor("#FFB865") // Orange color for the current date
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private var completedDates: Set<Long> = emptySet()
    private var currentDate: Long = Calendar.getInstance().timeInMillis

    // Set the completed dates and redraw the view
    fun setCompletedDates(dates: Set<Long>) {
        completedDates = dates
        invalidate() // Request a redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val dotRadius = (width * 0.05f).coerceAtMost(height * 0.05f) // Dynamic radius based on view size
        val dotSpacing = width / 7f // One for each day of the week

        // Loop through completed dates and draw circles
        for (date in completedDates) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = date
            }
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0, Saturday = 6
            val x = (dayOfWeek * dotSpacing) + (dotSpacing / 2) // Position the dot in the center of its "day"
            val y = height / 2f // Center vertically

            val paintToUse = if (date == currentDate) currentDatePaint else paint
            canvas.drawCircle(x, y, dotRadius, paintToUse)
        }
    }
}
