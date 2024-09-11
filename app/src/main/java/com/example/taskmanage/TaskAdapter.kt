// File: TaskAdapter.kt
package com.example.taskmanage

import android.os.CountDownTimer
import android.util.Log
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
    private val startTaskTimer: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var taskList: MutableList<Task> = mutableListOf()

    init {
        taskViewModel.taskList.observe(lifecycleOwner, Observer { tasks ->
            taskList = tasks.toMutableList()
            notifyDataSetChanged()
        })
    }

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val taskNameTextView: TextView = view.findViewById(R.id.taskNameTextView)
        val countdownTextView: TextView = view.findViewById(R.id.countdownTextView)
        val progressBar: ProgressBar = view.findViewById(R.id.taskProgressBar)
        val pauseResumeButton: Button = view.findViewById(R.id.pauseResumeButton)
        private var countDownTimer: CountDownTimer? = null

        fun bind(task: Task) {
            taskNameTextView.text = task.name
            progressBar.max = task.timeAssign.toInt()
            progressBar.progress = (task.timeAssign - task.remainingTime).toInt()
            updateCountdownText(task)

            pauseResumeButton.text = if (task.isPaused) "Start" else "Pause"

            pauseResumeButton.setOnClickListener {
                task.isPaused = !task.isPaused
                if (task.isPaused) {
                    countDownTimer?.cancel()
                    countDownTimer = null
                    pauseResumeButton.text = "Start"
                } else {
                    startCountdown(task)
                    pauseResumeButton.text = "Pause"
                }
                taskViewModel.addTask(task)
            }

            if (!task.isPaused && task.remainingTime > 0) {
                startCountdown(task)
            }
        }

        private fun startCountdown(task: Task) {
            countDownTimer?.cancel()
            countDownTimer = object : CountDownTimer(task.remainingTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    task.remainingTime = millisUntilFinished
                    updateCountdownText(task)
                    progressBar.progress = (task.timeAssign - task.remainingTime).toInt()
                }

                override fun onFinish() {
                    task.isCompleted = true
                    taskViewModel.removeTask(task)
                    startTaskTimer(task)
                }
            }.start()
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
