package com.example.taskmanage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val lifecycleOwner: LifecycleOwner,
    private val taskViewModel: TaskViewModel,
    private val onTaskCompleted: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val taskNameTextView: TextView = view.findViewById(R.id.taskNameTextView)
        private val taskDescriptionTextView: TextView = view.findViewById(R.id.taskDescriptionTextView)
        private val countdownTextView: TextView = view.findViewById(R.id.countdownTextView)
        private val progressBar: ProgressBar = view.findViewById(R.id.taskProgressBar)
        private val pauseResumeButton: Button = view.findViewById(R.id.pauseResumeButton)
        private val completeButton: Button = view.findViewById(R.id.completeButton)

        fun bind(task: Task) {
            taskNameTextView.text = task.name
            taskDescriptionTextView.text = task.description
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

            completeButton.setOnClickListener {
                onTaskCompleted(task)
            }

            taskViewModel.currentTask.observe(lifecycleOwner, Observer { currentTask ->
                if (currentTask?.id == task.id) {
                    updateCountdownText(currentTask)
                    progressBar.progress = (currentTask.timeAssign - currentTask.remainingTime).toInt()
                }
            })
        }

        private fun updateButtonState(task: Task) {
            pauseResumeButton.text = if (task.isPaused) "Resume" else "Pause"
        }

        private fun updateCountdownText(task: Task) {
            val seconds = (task.remainingTime / 1000) % 60
            val minutes = (task.remainingTime / (1000 * 60)) % 60
            val hours = (task.remainingTime / (1000 * 60 * 60))
            countdownTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}