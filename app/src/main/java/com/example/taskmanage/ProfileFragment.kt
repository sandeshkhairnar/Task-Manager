package com.example.taskmanage

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import java.util.*
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {

    private lateinit var customPieChart: CustomPieChart
    private lateinit var datePickerButton: Button
    private lateinit var totalTimeText: TextView

    private val taskViewModel: TaskViewModel by viewModels {
        val repository = TaskRepository(TaskDatabase.getDatabase(requireContext()).taskDao())
        TaskViewModelFactory(repository)
    }

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
        datePickerButton = view.findViewById(R.id.datePickerButton)
        totalTimeText = view.findViewById(R.id.totalTimeText)

        setupDatePicker()
        observeCompletedTasks()
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        updateChartForDate(calendar.time)  // Initialize with current date
        datePickerButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    updateChartForDate(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun observeCompletedTasks() {
        taskViewModel.completedTaskList.observe(viewLifecycleOwner) { tasks ->
            updateChartForDate(Calendar.getInstance().time)
        }
    }

    private fun updateChartForDate(date: Date) {
        val calendar = Calendar.getInstance().apply { time = date }
        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val completedTasksForDate = taskViewModel.completedTaskList.value?.filter { task ->
            task.completionTime in startOfDay..endOfDay
        } ?: emptyList()

        val colors = listOf(Color.rgb(66, 133, 244), Color.rgb(219, 68, 55),
            Color.rgb(244, 180, 0), Color.rgb(15, 157, 88),
            Color.rgb(171, 71, 188), Color.rgb(0, 172, 193))

        val data = completedTasksForDate.mapIndexed { index, task ->
            CustomPieChart.TaskSlice(task.name, task.assignTimeDuration.toFloat(), colors[index % colors.size])
        }

        customPieChart.setData(data)

        val totalTimeInMinutes = completedTasksForDate.sumOf { it.assignTimeDuration }
        val hours = TimeUnit.MINUTES.toHours(totalTimeInMinutes.toLong())
        val minutes = totalTimeInMinutes % 60

        totalTimeText.text = "Total Completed Time: ${hours}h ${minutes}m"

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        datePickerButton.text = dateFormat.format(date)
    }
}