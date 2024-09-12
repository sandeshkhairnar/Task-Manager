// File: Task.kt
package com.example.taskmanage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val timeInMillis: Long,
    val description: String,
    val repeatOption: String,
    var remainingTime: Long,
    val timeAssign: Long,
    val assignTimeDuration: Int, // Duration in minutes
    var isCompleted: Boolean = false,
    var isPaused: Boolean = false,
    var completionTime: Long = 0 // Changed from String to Long
)
