package com.example.taskmanage

data class Task(
    val name: String,
    var timeInMillis: Long, // Total time assigned to the task
    var description: String,
    var repeatOption: String,
    var remainingTime: Long = timeInMillis, // Remaining time
    var isPaused: Boolean = true, // Paused state
    var isCompleted: Boolean = false, // Completion status
    val timeAssign: Long,           // Time assigned in milliseconds

)
