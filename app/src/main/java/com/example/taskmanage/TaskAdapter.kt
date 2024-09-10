package com.example.taskmanage

import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val taskList: MutableList<Task>,
    private val startTaskTimer: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskNameTextView: TextView = view.findViewById(R.id.taskNameTextView)
        val countdownTextView: TextView = view.findViewById(R.id.countdownTextView)
        val progressBar: ProgressBar = view.findViewById(R.id.taskProgressBar)
        val pauseResumeButton: Button = view.findViewById(R.id.pauseResumeButton)
        var countDownTimer: CountDownTimer? = null

        private fun startCountdown(task: Task) {
            countDownTimer?.cancel() // Cancel any existing timer

            countDownTimer = object : CountDownTimer(task.remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d("TaskAdapter", "OnTick: Task '${task.name}' remainingTime: $millisUntilFinished")
                    task.remainingTime = millisUntilFinished
                    updateCountdownText(task)
                    progressBar.progress = (task.timeAssign - task.remainingTime).toInt()
                }

                override fun onFinish() {
                    Log.d("TaskAdapter", "OnFinish: Task '${task.name}' completed.")
                    task.isCompleted = true
                    // Remove task from active list and add to completed
                    taskList.remove(task)
                    notifyDataSetChanged() // Notify adapter of task removal
                    // Notify HomeFragment to update completed tasks if necessary
                    startTaskTimer(task) // This should only be called if you need to handle post-completion actions
                }


            }.start()
        }

        fun bind(task: Task) {
            taskNameTextView.text = task.name
            progressBar.max = task.timeAssign.toInt()
            progressBar.progress = (task.timeAssign - task.remainingTime).toInt()
            updateCountdownText(task)

            // Set initial button text based on the task state
            pauseResumeButton.text = if (task.isPaused) "Start" else "Pause"

            // Set up pause/resume button click
            pauseResumeButton.setOnClickListener {
                task.isPaused = !task.isPaused
                if (task.isPaused) {
                    countDownTimer?.cancel()
                    pauseResumeButton.text = "Start"
                } else {
                    startCountdown(task)
                    pauseResumeButton.text = "Pause"
                }
            }

            // Start countdown if task is not paused and remaining time is valid
            if (!task.isPaused && task.remainingTime > 0) {
                startCountdown(task)
            }
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
        holder.bind(taskList[position])
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}
