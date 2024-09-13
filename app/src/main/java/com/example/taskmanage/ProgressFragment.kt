package com.example.taskmanage


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class ProgressFragment : Fragment() {

    private lateinit var contributionCalendar: GitHubContributionCalendar
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

        contributionCalendar = view.findViewById(R.id.contributionCalendar)
        tasksRecyclerView = view.findViewById(R.id.completedTasksRecyclerView)
        dateTextView = view.findViewById(R.id.dateTextView)
        noTasksTextView = view.findViewById(R.id.noTasksTextView)

        setupTasksRecyclerView()
        observeTasks()
    }

    private fun setupTasksRecyclerView() {
        completedTaskAdapter = CompletedTaskAdapter(
            onItemClick = { /* Handle completed task click if needed */ },
            onDeleteClick = { task ->
                taskViewModel.deleteCompletedTask(task)
            },
            viewLifecycleOwner = viewLifecycleOwner
        )
        tasksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = completedTaskAdapter
        }
    }

    private fun observeTasks() {
        taskViewModel.completedTaskList.observe(viewLifecycleOwner) { completedTasks ->
            updateCompletedDates(completedTasks)
            val currentDate = Calendar.getInstance().timeInMillis
            updateTasksForDate(currentDate)
        }
    }

    private fun updateCompletedDates(completedTasks: List<Task>) {
        val completedDates = completedTasks.map { task ->
            Calendar.getInstance().apply {
                timeInMillis = task.completionTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.toSet()

        contributionCalendar.setCompletedDates(completedDates)
    }

    private fun updateTasksForDate(date: Long) {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
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
            dateTextView.text = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)

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