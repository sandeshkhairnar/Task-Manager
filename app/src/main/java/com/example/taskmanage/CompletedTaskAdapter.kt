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
    private val param: (Any) -> Unit // Add this parameter
) : ListAdapter<Task, CompletedTaskAdapter.CompletedTaskViewHolder>(TaskDiffCallback()) {

    inner class CompletedTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskNameTextView: TextView = itemView.findViewById(R.id.completedTaskNameTextView)
        private val completionTimeTextView: TextView = itemView.findViewById(R.id.completionTimeTextView)

        fun bind(task: Task) {
            taskNameTextView.text = task.name
            completionTimeTextView.text = formatCompletionTime(task.completionTime)
            itemView.setOnClickListener { onItemClick(task) }
        }

        private fun formatCompletionTime(completionTime: Long): String {
            val date = Date(completionTime)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return format.format(date)
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