package com.example.com.example.task.domain.repository

import com.example.com.example.task.domain.model.Task

interface TaskRepository {
    suspend fun findById(id: Int): Task?
    suspend fun findAllByUserId(userId: Int): List<Task>
    suspend fun create(task: Task): Task
    suspend fun update(task: Task): Boolean
    suspend fun delete(id: Int): Boolean
}