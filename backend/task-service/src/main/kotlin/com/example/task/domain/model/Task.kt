package com.example.com.example.task.domain.model

enum class TaskPriority { LOW, MEDIUM, HIGH }

enum class TaskStatus { PENDING, IN_PROGRESS, COMPLETED }

data class Task(
    val id: Int? = null,
    val title: String,
    val description: String?,
    val priority: TaskPriority,
    val status: TaskStatus,
    val userId: Int
)
