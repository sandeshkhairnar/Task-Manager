// File: HomeFragment.kt
package com.example.taskmanage

import android.app.TimePickerDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class HomeFragment : Fragment() {

    private val taskViewModel: TaskViewModel by viewModels {
        val repository = TaskRepository(TaskDatabase.getDatabase(requireContext()).taskDao())
        TaskViewModelFactory(repository)
    }

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var completedTaskAdapter: TaskAdapter
    private lateinit var timeAssignTextView: TextView
    private var selectedTimeInMillis: Long = 0
    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize RecyclerViews
        taskAdapter = TaskAdapter(viewLifecycleOwner, taskViewModel, ::startTaskTimer)
        val recyclerView: RecyclerView = view.findViewById(R.id.taskRecyclerView)
        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        completedTaskAdapter = TaskAdapter(viewLifecycleOwner, taskViewModel, {})
        val completedRecyclerView: RecyclerView = view.findViewById(R.id.completedTaskRecyclerView)
        completedRecyclerView.adapter = completedTaskAdapter
        completedRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe task lists from ViewModel
        taskViewModel.taskList.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.notifyDataSetChanged()
        }

        taskViewModel.completedTaskList.observe(viewLifecycleOwner) { tasks ->
            completedTaskAdapter.notifyDataSetChanged()
        }

        // Set up Add Task button
        val addTaskButton: Button = view.findViewById(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            showAddTaskDialog()
        }

        return view
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)

        // Initialize UI elements
        val taskNameEditText: EditText = dialogView.findViewById(R.id.dialogTaskNameEditText)
        timeAssignTextView = dialogView.findViewById(R.id.dialogTimeAssignTextView)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.dialogDescriptionEditText)
        val repeatSpinner: Spinner = dialogView.findViewById(R.id.dialogRepeatSpinner)

        // Set up a default time value to prevent NullPointerException
        selectedTimeInMillis = 0L

        // Set up time picker dialog
        timeAssignTextView.setOnClickListener {
            showTimePickerDialog()
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add New Task")
            .setPositiveButton("Add Task") { dialog, _ ->
                val taskName = taskNameEditText.text?.toString().orEmpty()
                val timeAssign = selectedTimeInMillis // Ensure selectedTimeInMillis is being passed
                val description = descriptionEditText.text?.toString().orEmpty()
                val repeatOption = repeatSpinner.selectedItem?.toString() ?: "No Repeat" // Default value to avoid null

                // Ensure task name is not blank and timeAssign is valid
                if (taskName.isNotBlank() && timeAssign > 0) {
                    // Create a task object and add it to the ViewModel
                    val task = Task(
                        name = taskName,
                        timeInMillis = timeAssign,
                        description = description,
                        repeatOption = repeatOption,
                        remainingTime = timeAssign,
                        timeAssign = timeAssign
                    )
                    taskViewModel.addTask(task) // Add task to ViewModel
                } else {
                    // Provide user feedback if the task is invalid
                    Toast.makeText(requireContext(), "Please fill in all fields and set a time.", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.create().show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                // Convert selected time into milliseconds
                val hoursInMillis = selectedHour * 3600 * 1000
                val minutesInMillis = selectedMinute * 60 * 1000
                val secondsInMillis = 0 // Assuming seconds are not needed here
                selectedTimeInMillis = (hoursInMillis + minutesInMillis + secondsInMillis).toLong()
                timeAssignTextView.text = String.format("%02d:%02d:00", selectedHour, selectedMinute)
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    private fun startTaskTimer(task: Task) {
        countDownTimer?.cancel() // Cancel any previous timer

        countDownTimer = object : CountDownTimer(task.timeAssign, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                task.remainingTime = millisUntilFinished // Update remaining time
                taskViewModel.updateTask(task) // Update task in ViewModel
                // Optionally update UI with remaining time
            }

            override fun onFinish() {
                task.isCompleted = true
                taskViewModel.removeTask(task) // Remove the task from ViewModel
                taskViewModel.addCompletedTask(task) // Add to completed tasks
                Toast.makeText(requireContext(), "Task '${task.name}' completed!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }
}
