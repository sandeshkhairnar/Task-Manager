package com.example.taskmanage

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val taskList: LiveData<List<Task>> = repository.getTasks()
    val completedTaskList: LiveData<List<Task>> = repository.getCompletedTasks()

    val allTasks: LiveData<List<Task>> = MediatorLiveData<List<Task>>().apply {
        addSource(taskList) { tasks ->
            value = tasks + (completedTaskList.value ?: emptyList())
        }
        addSource(completedTaskList) { completedTasks ->
            value = (taskList.value ?: emptyList()) + completedTasks
        }
    }

    private val _currentTask = MutableLiveData<Task?>()
    val currentTask: LiveData<Task?> get() = _currentTask

    private var countDownTimer: CountDownTimer? = null

    fun startTaskTimer(task: Task) {
        _currentTask.value = task
        countDownTimer?.cancel() // Cancel any existing timer

        countDownTimer = object : CountDownTimer(task.remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                task.remainingTime = millisUntilFinished
                _currentTask.value = task // Update the LiveData
                updateTask(task) // Update the task in the database
            }

            override fun onFinish() {
                task.isCompleted = true
                removeTask(task)
                addCompletedTask(task)
                _currentTask.value = null
            }
        }.start()
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    fun deleteCompletedTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task) // Assuming completed tasks are stored in the same table
    }

    fun addTask(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun completeTask(task: Task) = viewModelScope.launch {
        task.isCompleted = true
        task.completionTime = System.currentTimeMillis()
        repository.updateTask(task)
    }

    fun pauseTask(task: Task) {
        countDownTimer?.cancel()
        task.isPaused = true
        updateTask(task)
    }

    fun resumeTask(task: Task) {
        task.isPaused = false
        startTaskTimer(task)
    }

    fun stopTaskTimer() {
        countDownTimer?.cancel()
        _currentTask.value = null
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun removeTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun addCompletedTask(task: Task) {
        viewModelScope.launch {
            repository.insertCompletedTask(task)
        }
    }
}