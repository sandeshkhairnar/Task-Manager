// File: TaskAdapter.kt
package com.example.taskmanage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val taskViewModel: TaskViewModel,
    kFunction1: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var taskList: MutableList<Task> = mutableListOf()

    init {
        taskViewModel.taskList.observe(lifecycleOwner, Observer { tasks ->
            taskList = tasks.toMutableList()
            notifyDataSetChanged()
        })
    }

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val taskNameTextView: TextView = view.findViewById(R.id.taskNameTextView)
        private val countdownTextView: TextView = view.findViewById(R.id.countdownTextView)
        private val progressBar: ProgressBar = view.findViewById(R.id.taskProgressBar)
        private val pauseResumeButton: Button = view.findViewById(R.id.pauseResumeButton)

        fun bind(task: Task) {
            taskNameTextView.text = task.name
            progressBar.max = task.timeAssign.toInt()
            progressBar.progress = (task.timeAssign - task.remainingTime).toInt()
            updateCountdownText(task)
            updateButtonState(task)

            pauseResumeButton.setOnClickListener {
                if (task.isPaused) {
                    taskViewModel.resumeTask(task)
                } else {
                    taskViewModel.pauseTask(task)
                }
                updateButtonState(task)
            }

            // Observe currentTask to update UI accordingly
            taskViewModel.currentTask.observe(lifecycleOwner, Observer { currentTask ->
                if (currentTask?.id == task.id) {
                    updateCountdownText(currentTask)
                    progressBar.progress = (currentTask.timeAssign - currentTask.remainingTime).toInt()
                }
            })
        }

        private fun updateButtonState(task: Task) {
            pauseResumeButton.text = if (task.isPaused) "Start" else "Pause"
        }

        private fun updateCountdownText(task: Task) {
            val seconds = (task.remainingTime / 1000) % 60
            val minutes = (task.remainingTime / (1000 * 60)) % 60
            val hours = (task.remainingTime / (1000 * 60 * 60))
            countdownTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    // ... rest of the adapter implementation


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position])
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}
