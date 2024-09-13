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
    private val contributionOverlayView: ContributionOverlayView

    private var completedDates: Set<Long> = emptySet()

    init {
        orientation = VERTICAL

        calendarView = CalendarView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        contributionOverlayView = ContributionOverlayView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        addView(calendarView)
        addView(contributionOverlayView)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.timeInMillis
            // Optionally, update the overlay based on the selected date
        }
    }

    fun setCompletedDates(dates: Set<Long>) {
        completedDates = dates
        contributionOverlayView.setCompletedDates(dates)
    }
}
