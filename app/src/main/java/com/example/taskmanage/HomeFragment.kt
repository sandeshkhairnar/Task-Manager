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
import android.util.Log
import java.util.*

class HomeFragment : Fragment() {

    private val taskViewModel: TaskViewModel by viewModels {
        val repository = TaskRepository(TaskDatabase.getDatabase(requireContext()).taskDao())
        TaskViewModelFactory(repository)
    }

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var completedTaskAdapter: CompletedTaskAdapter
    private lateinit var timeAssignTextView: TextView
    private var selectedTimeInMillis: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        initializeTaskRecyclerView(view)
        initializeCompletedTaskRecyclerView(view)
        observeTaskLists()
        observeCurrentTask()
        setupAddTaskButton(view)

        return view
    }

    private fun initializeTaskRecyclerView(view: View) {
        taskAdapter = TaskAdapter(
            viewLifecycleOwner,
            taskViewModel
        ) { task ->
            taskViewModel.completeTask(task)
            Log.d("HomeFragment", "Task completed: ${task.name}")
        }
        val recyclerView: RecyclerView = view.findViewById(R.id.taskRecyclerView)
        recyclerView.adapter = taskAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initializeCompletedTaskRecyclerView(view: View) {
        completedTaskAdapter = CompletedTaskAdapter(
            onItemClick = { task ->
                // Handle completed task click if needed
                Log.d("HomeFragment", "Completed task clicked: ${task.name}")
            },
            viewLifecycleOwner = viewLifecycleOwner,
            param = { /* Additional param function if needed */ }
        )
        val completedRecyclerView: RecyclerView = view.findViewById(R.id.completedTaskRecyclerView)
        completedRecyclerView.adapter = completedTaskAdapter
        completedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeTaskLists() {
        taskViewModel.taskList.observe(viewLifecycleOwner) { tasks ->
            val ongoingTasks = tasks.filter { !it.isCompleted }
            taskAdapter.submitList(ongoingTasks)
            Log.d("HomeFragment", "Ongoing tasks updated: ${ongoingTasks.size}")
        }

        taskViewModel.completedTaskList.observe(viewLifecycleOwner) { completedTasks ->
            completedTaskAdapter.submitList(completedTasks)
            Log.d("HomeFragment", "Completed tasks updated: ${completedTasks.size}")
        }
    }

    private fun observeCurrentTask() {
        taskViewModel.currentTask.observe(viewLifecycleOwner) { task ->
            // Update UI when a task is running or completed
            Log.d("HomeFragment", "Current task updated: ${task?.name}")
        }
    }

    private fun setupAddTaskButton(view: View) {
        val addTaskButton: Button = view.findViewById(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            showAddTaskDialog()
        }
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)

        val taskNameEditText: EditText = dialogView.findViewById(R.id.dialogTaskNameEditText)
        timeAssignTextView = dialogView.findViewById(R.id.dialogTimeAssignTextView)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.dialogDescriptionEditText)
        val repeatSpinner: Spinner = dialogView.findViewById(R.id.dialogRepeatSpinner)

        selectedTimeInMillis = 0L

        timeAssignTextView.setOnClickListener {
            showTimePickerDialog()
        }

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Add New Task")
            .setPositiveButton("Add Task") { dialog, _ ->
                val taskName = taskNameEditText.text.toString().trim()
                val timeAssign = selectedTimeInMillis
                val description = descriptionEditText.text.toString().trim()
                val repeatOption = repeatSpinner.selectedItem?.toString() ?: "None" // Provide a default value if null

                if (taskName.isNotBlank() && timeAssign > 0) {
                    val task = Task(
                        name = taskName,
                        timeInMillis = timeAssign,
                        description = description,
                        repeatOption = repeatOption,
                        remainingTime = timeAssign,
                        timeAssign = timeAssign,
                        isPaused = false,
                        isCompleted = false,
                        completionTime = 0L
                    )
                    taskViewModel.addTask(task)
                    Log.d("HomeFragment", "New task added: $taskName")
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
                val hoursInMillis = selectedHour * 3600 * 1000
                val minutesInMillis = selectedMinute * 60 * 1000
                selectedTimeInMillis = (hoursInMillis + minutesInMillis).toLong()
                timeAssignTextView.text = String.format("%02d:%02d:00", selectedHour, selectedMinute)
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }
}