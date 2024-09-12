package com.example.taskmanage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class CompletedTaskAdapter(
    private val onItemClick: (Task) -> Unit,
    private val viewLifecycleOwner: LifecycleOwner,
    private val param: (Any) -> Unit
) : ListAdapter<Task, CompletedTaskAdapter.CompletedTaskViewHolder>(TaskDiffCallback()) {

    inner class CompletedTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskNameTextView: TextView = itemView.findViewById(R.id.completedTaskNameTextView)
        private val completionTimeTextView: TextView = itemView.findViewById(R.id.completionTimeTextView)
        private val durationTextView: TextView = itemView.findViewById(R.id.taskDurationTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.completedTaskDescriptionTextView) // Add this TextView for description

        fun bind(task: Task) {
            taskNameTextView.text = task.name
            completionTimeTextView.text = getCurrentTimeFormatted() // Display current date and time
            durationTextView.text = formatDuration(task.assignTimeDuration) // Display formatted duration
            descriptionTextView.text = task.description // Display task description
            itemView.setOnClickListener { onItemClick(task) }
        }

        private fun getCurrentTimeFormatted(): String {
            val date = Date()
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return format.format(date)
        }

        private fun formatDuration(durationInMinutes: Int): String {
            val totalSeconds = durationInMinutes * 60
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletedTaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_completed_task, parent, false)
        return CompletedTaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompletedTaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
