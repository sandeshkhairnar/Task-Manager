package com.example.taskmanage

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    // Retrieve all active tasks
    @Query("SELECT * FROM task_table WHERE isCompleted = 0")
    fun getActiveTasks(): LiveData<List<Task>>

    // Retrieve all completed tasks
    @Query("SELECT * FROM task_table WHERE isCompleted = 1")
    fun getCompletedTasks(): LiveData<List<Task>>

    // Delete a task by its ID
    @Query("DELETE FROM task_table WHERE id = :taskId")
    suspend fun deleteTask(taskId: Int)

    // Update an existing task
    @Update
    suspend fun updateTask(task: Task)
}