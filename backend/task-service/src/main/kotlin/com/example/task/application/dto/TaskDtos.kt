package com.example.com.example.task.application.dto

import com.example.com.example.task.domain.model.TaskPriority
import com.example.com.example.task.domain.model.TaskStatus
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: Int? = null,
    val title: String,
    val description: String?,
    val priority: String,
    val status: String = TaskStatus.PENDING.name,
    val userId: Int? = null,
)
