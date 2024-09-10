package com.example.taskmanage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.fragment.app.Fragment
import java.util.Calendar

class ProgressFragment : Fragment() {

    private lateinit var calendarView: CalendarView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout with the CalendarView
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the CalendarView
        calendarView = view.findViewById(R.id.calendarView)

        // Set the calendar to the current date
        val currentDate = Calendar.getInstance().timeInMillis
        calendarView.date = currentDate
    }
}
