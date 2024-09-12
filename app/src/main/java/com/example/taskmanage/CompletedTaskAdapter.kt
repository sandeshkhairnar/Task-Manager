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
    function: () -> Unit
) : ListAdapter<Task, CompletedTaskAdapter.CompletedTaskViewHolder>(TaskDiffCallback()) {

    inner class CompletedTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskNameTextView: TextView = itemView.findViewById(R.id.completedTaskNameTextView)
        private val completionTimeTextView: TextView = itemView.findViewById(R.id.completionTimeTextView)
        private val durationTextView: TextView = itemView.findViewById(R.id.taskDurationTextView)

        fun bind(task: Task) {
            taskNameTextView.text = task.name
            completionTimeTextView.text = formatDate(task.completionTime)
            durationTextView.text = formatDuration(task.assignTimeDuration)
            itemView.setOnClickListener { onItemClick(task) }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

        private fun formatDuration(durationInMinutes: Int): String {
            val hours = durationInMinutes / 60
            val minutes = durationInMinutes % 60
            return String.format("%02d:%02d:00", hours, minutes)
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
