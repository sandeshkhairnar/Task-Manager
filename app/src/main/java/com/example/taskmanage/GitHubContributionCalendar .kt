package com.example.taskmanage


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.CalendarView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.taskmanage.R
import java.util.*

class GitHubContributionCalendar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val calendarView: CalendarView
    private val contributionView: ContributionView

    private var completedDates: Set<Long> = emptySet()

    init {
        orientation = VERTICAL

        calendarView = CalendarView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        contributionView = ContributionView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 50)
        }

        addView(calendarView)
        addView(contributionView)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.timeInMillis
            contributionView.setSelectedDate(selectedDate)
        }
    }

    fun setCompletedDates(dates: Set<Long>) {
        completedDates = dates
        contributionView.setCompletedDates(dates)
    }

    private inner class ContributionView(context: Context) : View(context) {
        private val paint = Paint()
        private var selectedDate: Long = 0

        init {
            paint.style = Paint.Style.FILL
        }

        fun setSelectedDate(date: Long) {
            selectedDate = date
            invalidate()
        }

        fun setCompletedDates(dates: Set<Long>) {
            completedDates = dates
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val dotRadius = 10f
            val dotSpacing = 20f
            val startX = width / 2f - (3 * dotSpacing)
            val centerY = height / 2f

            for (i in -3..3) {
                val date = Calendar.getInstance().apply {
                    timeInMillis = selectedDate
                    add(Calendar.DAY_OF_MONTH, i)
                }.timeInMillis

                val x = startX + (i + 3) * dotSpacing
                paint.color = getColorForDate(date)
                canvas.drawCircle(x, centerY, dotRadius, paint)
            }
        }

        private fun getColorForDate(date: Long): Int {
            return if (date in completedDates) {
                ContextCompat.getColor(context, R.color.teal_700)
            } else {
                ContextCompat.getColor(context, R.color.teal_200)
            }
        }
    }
}