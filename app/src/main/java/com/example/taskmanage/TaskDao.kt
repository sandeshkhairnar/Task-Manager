package com.example.taskmanage

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {

    // Retrieve all active tasks
    @Query("SELECT * FROM task_table WHERE isCompleted = 0")
    fun getActiveTasks(): LiveData<List<Task>>

    // Retrieve all completed tasks
    @Query("SELECT * FROM task_table WHERE isCompleted = 1")
    fun getCompletedTasks(): LiveData<List<Task>>

    // Insert a new task
    @Insert
    suspend fun insertTask(task: Task)

    // Delete a task by its ID
    @Query("DELETE FROM task_table WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)

    // Update an existing task
    @Update
    suspend fun updateTask(task: Task)
}
