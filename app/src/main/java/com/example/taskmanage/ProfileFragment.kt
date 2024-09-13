package com.example.taskmanage

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import java.util.*
import java.text.SimpleDateFormat

class ProfileFragment : Fragment() {

    private lateinit var customPieChart: CustomPieChart
    private lateinit var dateRangeSpinner: Spinner
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var totalTimeText: TextView

    private val taskViewModel: TaskViewModel by viewModels {
        val repository = TaskRepository(TaskDatabase.getDatabase(requireContext()).taskDao())
        TaskViewModelFactory(repository)
    }

    private var startDate: Calendar = Calendar.getInstance()
    private var endDate: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customPieChart = view.findViewById(R.id.customPieChart)
        dateRangeSpinner = view.findViewById(R.id.dateRangeSpinner)
        startDateButton = view.findViewById(R.id.startDateButton)
        endDateButton = view.findViewById(R.id.endDateButton)
        totalTimeText = view.findViewById(R.id.totalTimeText)

        setupDateRangeSpinner()
        setupDatePickers()
        observeCompletedTasks()
    }

    private fun setupDateRangeSpinner() {
        val options = arrayOf("Today", "This Week", "This Month", "This Year", "Custom")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dateRangeSpinner.adapter = adapter

        dateRangeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                when (position) {
                    0 -> setTodayRange()
                    1 -> setThisWeekRange()
                    2 -> setThisMonthRange()
                    3 -> setThisYearRange()
                    4 -> {
                        startDateButton.visibility = View.VISIBLE
                        endDateButton.visibility = View.VISIBLE
                    }
                }
                if (position != 4) {
                    startDateButton.visibility = View.GONE
                    endDateButton.visibility = View.GONE
                    updateChartForDateRange(startDate.time, endDate.time)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupDatePickers() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val selectedDate = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            }
            if (startDate.after(endDate)) {
                endDate = selectedDate
                endDateButton.text = formatDate(endDate.time)
            } else {
                startDate = selectedDate
                startDateButton.text = formatDate(startDate.time)
            }
            updateChartForDateRange(startDate.time, endDate.time)
        }

        startDateButton.setOnClickListener {
            showDatePicker(startDate, dateSetListener)
        }

        endDateButton.setOnClickListener {
            showDatePicker(endDate, dateSetListener)
        }
    }

    private fun showDatePicker(date: Calendar, listener: DatePickerDialog.OnDateSetListener) {
        DatePickerDialog(
            requireContext(),
            listener,
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setTodayRange() {
        startDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        endDate = Calendar.getInstance()
    }

    private fun setThisWeekRange() {
        startDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        endDate = Calendar.getInstance()
    }

    private fun setThisMonthRange() {
        startDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        endDate = Calendar.getInstance()
    }

    private fun setThisYearRange() {
        startDate = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_YEAR, 1)
        }
        endDate = Calendar.getInstance()
    }

    private fun observeCompletedTasks() {
        taskViewModel.completedTaskList.observe(viewLifecycleOwner) { tasks ->
            updateChartForDateRange(startDate.time, endDate.time)
        }
    }

    private fun updateChartForDateRange(start: Date, end: Date) {
        val completedTasksForRange = taskViewModel.completedTaskList.value?.filter { task ->
            task.completionTime in start.time..end.time
        } ?: emptyList()

        val colors = listOf(
            Color.rgb(66, 133, 244), Color.rgb(219, 68, 55),
            Color.rgb(244, 180, 0), Color.rgb(15, 157, 88),
            Color.rgb(171, 71, 188), Color.rgb(0, 172, 193)
        )

        val data = completedTasksForRange.mapIndexed { index, task ->
            // Convert assignTimeDuration from minutes to seconds
            CustomPieChart.TaskSlice(task.name, task.assignTimeDuration * 60, colors[index % colors.size])
        }

        customPieChart.setData(data)

        val totalTimeInSeconds = completedTasksForRange.sumOf { it.assignTimeDuration * 60 }
        val hours = totalTimeInSeconds / 3600
        val minutes = (totalTimeInSeconds % 3600) / 60
        val seconds = totalTimeInSeconds % 60

        totalTimeText.text = "Total Completed Time: ${String.format("%02d:%02d:%02d", hours, minutes, seconds)}"
    }


    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(date)
    }
}