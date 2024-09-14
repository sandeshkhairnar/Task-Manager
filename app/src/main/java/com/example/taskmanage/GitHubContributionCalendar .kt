package com.example.taskmanage

import android.content.Context
import android.util.AttributeSet
import android.widget.CalendarView
import android.widget.FrameLayout
import android.widget.LinearLayout
import java.util.*

class GitHubContributionCalendar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val frameLayout: FrameLayout
    private val calendarView: CalendarView
    private val contributionOverlayView: ContributionOverlayView

    private var completedDates: Set<Long> = emptySet()
    private var onDateChangeListener: ((Long) -> Unit)? = null
    private var lastSelectedDate: Long = Calendar.getInstance().timeInMillis

    init {
        orientation = VERTICAL

        frameLayout = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        calendarView = CalendarView(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        }

        contributionOverlayView = ContributionOverlayView(context).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }

        frameLayout.addView(calendarView)
        frameLayout.addView(contributionOverlayView)
        addView(frameLayout)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.timeInMillis
            lastSelectedDate = selectedDate
            updateOverlayForMonth(year, month)
            onDateChangeListener?.invoke(selectedDate)
        }
    }

    fun setCompletedDates(dates: Set<Long>) {
        completedDates = dates
        updateOverlayForCurrentMonth()
    }

    fun setOnDateChangeListener(listener: (Long) -> Unit) {
        onDateChangeListener = listener
    }

    private fun updateOverlayForCurrentMonth() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = calendarView.date
        updateOverlayForMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
    }

    private fun updateOverlayForMonth(year: Int, month: Int) {
        val startOfMonth = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endOfMonth = Calendar.getInstance().apply {
            set(year, month, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val visibleDates = completedDates.filter { date ->
            date in startOfMonth.timeInMillis..endOfMonth.timeInMillis
        }.toSet()

        contributionOverlayView.setCompletedDates(visibleDates)

        // Update the last selected date to be within the current month
        val updatedSelectedDate = Calendar.getInstance().apply {
            timeInMillis = lastSelectedDate
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            // Ensure the day is valid for the new month
            val maxDay = getActualMaximum(Calendar.DAY_OF_MONTH)
            set(Calendar.DAY_OF_MONTH, get(Calendar.DAY_OF_MONTH).coerceAtMost(maxDay))
        }.timeInMillis

        lastSelectedDate = updatedSelectedDate
        onDateChangeListener?.invoke(updatedSelectedDate)
    }
}