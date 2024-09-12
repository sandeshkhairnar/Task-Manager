package com.example.taskmanage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.Date

class ProgressFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var dateTextView: TextView
    private lateinit var noTasksTextView: TextView
    private lateinit var completedTaskAdapter: CompletedTaskAdapter

    private val taskViewModel: TaskViewModel by viewModels {
        val repository = TaskRepository(TaskDatabase.getDatabase(requireContext()).taskDao())
        TaskViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        tasksRecyclerView = view.findViewById(R.id.completedTasksRecyclerView)
        dateTextView = view.findViewById(R.id.dateTextView)
        noTasksTextView = view.findViewById(R.id.noTasksTextView)

        setupCalendarView()
        setupTasksRecyclerView()
        observeTasks()
    }

    private fun setupCalendarView() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.time
            updateTasksForDate(selectedDate)
        }
    }

    private fun setupTasksRecyclerView() {
        completedTaskAdapter = CompletedTaskAdapter(
            onItemClick = { /* Handle completed task click if needed */ },
            viewLifecycleOwner = viewLifecycleOwner
        ) { /* Additional function if needed */ }
        tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = completedTaskAdapter
        }
    }

    private fun observeTasks() {
        taskViewModel.completedTaskList.observe(viewLifecycleOwner) { completedTasks ->
            val currentDate = Calendar.getInstance().time
            updateTasksForDate(currentDate)
        }
    }

    private fun updateTasksForDate(date: Date) {
        val calendar = Calendar.getInstance().apply { time = date }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        taskViewModel.completedTaskList.value?.let { allCompletedTasks ->
            val tasksForDate = allCompletedTasks.filter { task ->
                val taskCalendar = Calendar.getInstance().apply { timeInMillis = task.completionTime }
                taskCalendar.get(Calendar.YEAR) == year &&
                        taskCalendar.get(Calendar.MONTH) == month &&
                        taskCalendar.get(Calendar.DAY_OF_MONTH) == day
            }

            completedTaskAdapter.submitList(tasksForDate)
            dateTextView.text = String.format("%04d-%02d-%02d", year, month + 1, day)

            if (tasksForDate.isEmpty()) {
                noTasksTextView.visibility = View.VISIBLE
                tasksRecyclerView.visibility = View.GONE
            } else {
                noTasksTextView.visibility = View.GONE
                tasksRecyclerView.visibility = View.VISIBLE
            }
        }
    }
}