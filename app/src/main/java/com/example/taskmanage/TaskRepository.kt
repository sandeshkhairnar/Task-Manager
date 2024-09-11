// File: TaskRepository.kt
package com.example.taskmanage

import androidx.lifecycle.LiveData

class TaskRepository(private val taskDao: TaskDao) {

    // Get active tasks
    fun getActiveTasks(): LiveData<List<Task>> = taskDao.getActiveTasks()

    // Get completed tasks
    fun getCompletedTasks(): LiveData<List<Task>> = taskDao.getCompletedTasks()

    // Insert a new task
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    // Delete a task by its ID
    suspend fun deleteTask(taskId: Int) {
        taskDao.deleteTask(taskId)
    }

    // Update an existing task
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }
}
