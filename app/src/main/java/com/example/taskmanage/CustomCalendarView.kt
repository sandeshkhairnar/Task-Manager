package com.example.taskmanage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val calendar = Calendar.getInstance()
    private var selectedDay = calendar.get(Calendar.DAY_OF_MONTH)

    private val dayOfWeek = arrayOf("S", "M", "T", "W", "T", "F", "S")

    init {
        paint.color = Color.BLACK
        textPaint.color = Color.BLACK
        textPaint.textAlign = Paint.Align.CENTER
        headerPaint.color = Color.BLACK
        headerPaint.textAlign = Paint.Align.CENTER
        headerPaint.typeface = Typeface.DEFAULT_BOLD
        selectedPaint.color = Color.parseColor("#6200EE")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val cellWidth = width / 7
        val cellHeight = (height - 100) / 7 // Reserve space for header

        // Draw header
        headerPaint.textSize = 50f
        canvas.drawText(getMonthYearString(), width / 2, 75f, headerPaint)

        // Draw day of week headers
        textPaint.textSize = 35f
        for (i in dayOfWeek.indices) {
            canvas.drawText(dayOfWeek[i], cellWidth * i + cellWidth / 2, 150f, textPaint)
        }

        // Draw days
        textPaint.textSize = 40f
        var day = 1
        for (row in 0 until 6) {
            for (col in 0 until 7) {
                if (day <= getDaysInMonth()) {
                    val x = col * cellWidth + cellWidth / 2
                    val y = row * cellHeight + cellHeight / 2 + 175f

                    if (day == selectedDay) {
                        canvas.drawCircle(x, y, cellWidth / 2 - 10, selectedPaint)
                        textPaint.color = Color.WHITE
                    } else {
                        textPaint.color = Color.BLACK
                    }

                    canvas.drawText(day.toString(), x, y + 15, textPaint)
                    day++
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val cellWidth = width / 7f
            val cellHeight = (height - 100) / 7f
            val row = ((event.y - 175) / cellHeight).toInt()
            val col = (event.x / cellWidth).toInt()
            val day = row * 7 + col + 1

            if (day <= getDaysInMonth()) {
                selectedDay = day
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getMonthYearString(): String {
        val monthFormat = android.text.format.DateFormat.format("MMMM yyyy", calendar)
        return monthFormat.toString()
    }

    private fun getDaysInMonth(): Int {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun setDate(year: Int, month: Int) {
        calendar.set(year, month, 1)
        selectedDay = 1
        invalidate()
    }

    fun getSelectedDate(): Triple<Int, Int, Int> {
        return Triple(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            selectedDay
        )
    }
}