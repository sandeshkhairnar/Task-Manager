package com.example.taskmanage

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
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
import kotlin.math.abs

class HomeFragment : Fragment() {

    private val taskViewModel: TaskViewModel by viewModels {
        val repository = TaskRepository(TaskDatabase.getDatabase(requireContext()).taskDao())
        TaskViewModelFactory(repository)
    }

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var completedTaskAdapter: CompletedTaskAdapter
    private lateinit var timeAssignTextView: TextView
    private var selectedTimeInMillis: Long = 0
    private lateinit var addTaskImageButton: ImageView
    private var dX: Float = 0f
    private var dY: Float = 0f

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
            lifecycleOwner = viewLifecycleOwner,
            taskViewModel = taskViewModel,
            onTaskCompleted = { task ->
                taskViewModel.completeTask(task)
                Log.d("HomeFragment", "Task completed: ${task.name}")
            },
            onDeleteClick = { task ->
                taskViewModel.deleteTask(task)
                Log.d("HomeFragment", "Task deleted: ${task.name}")
            }
        )
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
            onDeleteClick = { task ->
                taskViewModel.deleteCompletedTask(task)
                Log.d("HomeFragment", "Completed task deleted: ${task.name}")
            },
            viewLifecycleOwner = viewLifecycleOwner
        )
        val completedRecyclerView: RecyclerView = view.findViewById(R.id.completedTaskRecyclerView)
        completedRecyclerView.adapter = completedTaskAdapter
        completedRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        taskViewModel.completedTaskList.observe(viewLifecycleOwner) { completedTasks ->
            Log.d("HomeFragment", "Completed tasks updated: ${completedTasks.size}")
            completedTaskAdapter.submitList(completedTasks)

            if (completedTasks.isNotEmpty()) {
                completedRecyclerView.visibility = View.VISIBLE
            } else {
                completedRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun observeTaskLists() {
        taskViewModel.taskList.observe(viewLifecycleOwner) { tasks ->
            val ongoingTasks = tasks.filter { !it.isCompleted }
            taskAdapter.submitList(ongoingTasks)
            Log.d("HomeFragment", "Ongoing tasks updated: ${ongoingTasks.size}")

            val taskRecyclerView: RecyclerView = view?.findViewById(R.id.taskRecyclerView) ?: return@observe
            if (ongoingTasks.isNotEmpty()) {
                taskRecyclerView.visibility = View.VISIBLE
            } else {
                taskRecyclerView.visibility = View.GONE
            }

            updateVisibility()
        }

        taskViewModel.completedTaskList.observe(viewLifecycleOwner) { completedTasks ->
            completedTaskAdapter.submitList(completedTasks)
            Log.d("HomeFragment", "Completed tasks updated: ${completedTasks.size}")

            val completedRecyclerView: RecyclerView = view?.findViewById(R.id.completedTaskRecyclerView) ?: return@observe
            if (completedTasks.isNotEmpty()) {
                completedRecyclerView.visibility = View.VISIBLE
            } else {
                completedRecyclerView.visibility = View.GONE
            }

            updateVisibility()
        }
    }

    private fun updateVisibility() {
        val taskRecyclerView: RecyclerView = view?.findViewById(R.id.taskRecyclerView) ?: return
        val completedRecyclerView: RecyclerView = view?.findViewById(R.id.completedTaskRecyclerView) ?: return

        if (taskRecyclerView.visibility == View.GONE && completedRecyclerView.visibility == View.GONE) {
            completedRecyclerView.visibility = View.GONE
        }
    }

    private fun observeCurrentTask() {
        taskViewModel.currentTask.observe(viewLifecycleOwner) { task ->
            // Update UI when a task is running or completed
            Log.d("HomeFragment", "Current task updated: ${task?.name}")
        }
    }

    private fun setupAddTaskButton(view: View) {
        addTaskImageButton = view.findViewById(R.id.addTaskImageButton)

        // Reset button position to bottom right corner
        addTaskImageButton.post {
            addTaskImageButton.x = (view.width - addTaskImageButton.width - 16f * resources.displayMetrics.density)
            addTaskImageButton.y = (view.height - addTaskImageButton.height - 16f * resources.displayMetrics.density)
        }

        var startX = 0f
        var startY = 0f
        var isMoved = false

        addTaskImageButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX
                    startY = event.rawY
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    isMoved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val movedX = event.rawX + dX
                    val movedY = event.rawY + dY
                    v.animate()
                        .x(movedX)
                        .y(movedY)
                        .setDuration(0)
                        .start()
                    isMoved = true
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMoved || (abs(event.rawX - startX) < 10 && abs(event.rawY - startY) < 10)) {
                        v.performClick()
                    }
                    true
                }
                else -> false
            }
        }

        addTaskImageButton.setOnClickListener {
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
                val repeatOption = repeatSpinner.selectedItem?.toString() ?: "None"

                // Calculate the assign time duration in minutes
                val assignTimeDuration = (timeAssign / (60 * 1000)).toInt()

                if (taskName.isNotBlank() && timeAssign > 0) {
                    val task = Task(
                        name = taskName,
                        timeInMillis = timeAssign,
                        description = description,
                        repeatOption = repeatOption,
                        remainingTime = timeAssign,
                        timeAssign = timeAssign,
                        assignTimeDuration = assignTimeDuration,
                        isPaused = false,
                        isCompleted = false,
                        completionTime = 0L
                    )
                    taskViewModel.addTask(task)
                    Log.d("HomeFragment", "New task added: $taskName with duration: $assignTimeDuration minutes")
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

    private fun adjustRecyclerViewHeight(recyclerView: RecyclerView, size: Int) {
        recyclerView.post {
            val itemCount = (recyclerView.adapter as? TaskAdapter)?.itemCount ?: 0
            if (itemCount > 0) {
                val itemHeight = recyclerView.getChildAt(0)?.height ?: 0
                val totalHeight = itemCount * itemHeight
                recyclerView.layoutParams.height = totalHeight
                recyclerView.requestLayout()
            } else {
                recyclerView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                recyclerView.requestLayout()
            }
        }
    }
}