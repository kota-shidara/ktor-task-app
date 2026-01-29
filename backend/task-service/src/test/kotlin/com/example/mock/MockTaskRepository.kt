package com.example.mock

import com.example.com.example.task.domain.model.Task
import com.example.com.example.task.domain.repository.TaskRepository

class MockTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    override suspend fun findById(id: Int): Task? = tasks.find { it.id == id }

    override suspend fun findAllByUserId(userId: Int): List<Task> = tasks.filter { it.userId == userId }

    override suspend fun create(task: Task): Task {
        val newTask = task.copy(id = nextId++)
        tasks.add(newTask)
        return newTask
    }

    override suspend fun update(task: Task): Boolean {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index == -1) return false
        tasks[index] = task
        return true
    }

    override suspend fun delete(id: Int): Boolean {
        return tasks.removeIf { it.id == id }
    }

    override suspend fun deleteAllByUserId(userId: Int): Int {
        val count = tasks.count { it.userId == userId }
        tasks.removeIf { it.userId == userId }
        return count
    }
}
