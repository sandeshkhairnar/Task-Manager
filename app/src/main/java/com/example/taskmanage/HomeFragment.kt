package com.example.taskmanage

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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

        completedTaskAdapter = TaskAdapter(viewLifecycleOwner, taskViewModel) {}
        val completedRecyclerView: RecyclerView = view.findViewById(R.id.completedTaskRecyclerView)
        completedRecyclerView.adapter = completedTaskAdapter
        completedRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe task lists from ViewModel
        taskViewModel.taskList.observe(viewLifecycleOwner) {
            taskAdapter.notifyDataSetChanged()
        }

        taskViewModel.completedTaskList.observe(viewLifecycleOwner) {
            completedTaskAdapter.notifyDataSetChanged()
        }

        // Observe current running task for timer updates
        taskViewModel.currentTask.observe(viewLifecycleOwner) { task ->
            // Update UI when a task is running or completed
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

        // Initialize UI elements with null checks to avoid NullPointerException
        val taskNameEditText: EditText? = dialogView.findViewById(R.id.dialogTaskNameEditText)
        timeAssignTextView = dialogView.findViewById(R.id.dialogTimeAssignTextView)
        val descriptionEditText: EditText? = dialogView.findViewById(R.id.dialogDescriptionEditText)
        val repeatSpinner: Spinner? = dialogView.findViewById(R.id.dialogRepeatSpinner)

        selectedTimeInMillis = 0L // Default time value

        // Set up time picker dialog
        timeAssignTextView.setOnClickListener {
            showTimePickerDialog()
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add New Task")
            .setPositiveButton("Add Task") { dialog, _ ->
                // Get text and handle potential nulls gracefully
                val taskName = taskNameEditText?.text?.toString()?.trim() ?: ""
                val timeAssign = selectedTimeInMillis
                val description = descriptionEditText?.text?.toString()?.trim() ?: ""
                val repeatOption = repeatSpinner?.selectedItem?.toString() ?: ""

                // Validate input fields
                if (taskName.isNotBlank() && timeAssign > 0) {
                    val task = Task(
                        name = taskName,
                        timeInMillis = timeAssign,
                        description = description,
                        repeatOption = repeatOption,
                        remainingTime = timeAssign,
                        timeAssign = timeAssign,
                        isPaused = false
                    )
                    taskViewModel.addTask(task)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please fill in all fields and set a time.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        dialogBuilder.create().show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                // Convert selected time to milliseconds
                val hoursInMillis = selectedHour * 3600 * 1000
                val minutesInMillis = selectedMinute * 60 * 1000
                selectedTimeInMillis = (hoursInMillis + minutesInMillis).toLong()
                // Update the TextView with the selected time
                timeAssignTextView.text = String.format("%02d:%02d:00", selectedHour, selectedMinute)
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    private fun startTaskTimer(task: Task) {
        taskViewModel.startTaskTimer(task) // Start the timer via ViewModel
    }
}
