package com.example.taskmanage

import androidx.lifecycle.LiveData

class TaskRepository(private val taskDao: TaskDao) {

    // Get active (incomplete) tasks
    fun getTasks(): LiveData<List<Task>> = taskDao.getTasks()

    // Get completed tasks
    suspend fun insertCompletedTask(task: Task) = taskDao.insertTask(task)
    fun getCompletedTasks() = taskDao.getCompletedTasks()    // Insert a new task
    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    // Update an existing task
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    // Delete a task
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

}