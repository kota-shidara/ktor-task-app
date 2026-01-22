package com.example.com.example.task.application.service

import com.example.com.example.task.application.dto.TaskDto
import com.example.com.example.task.domain.model.Task
import com.example.com.example.task.domain.model.TaskPriority
import com.example.com.example.task.domain.model.TaskStatus
import com.example.com.example.task.domain.repository.TaskRepository

class TaskService(private val repository: TaskRepository) {
    suspend fun getTasksForUser(userId: Int): List<TaskDto> {
        return repository.findAllByUserId(userId).map { it.toDto() }
    }

    suspend fun create(dto: TaskDto, userId: Int): TaskDto {
        val task = Task(
            title = dto.title,
            description = dto.description,
            priority = try { TaskPriority.valueOf(dto.priority) } catch (e: Exception) {
                TaskPriority.MEDIUM},
            status = TaskStatus.PENDING,
            userId = userId
        )
        val createdTask = repository.create(task)
        return createdTask.toDto()
    }

    suspend fun update(taskId: Int, dto: TaskDto, userId: Int): Boolean {
        val existing = repository.findById(taskId) ?: return false
        if (existing.userId != userId) return false

        val updatedTask = existing.copy(
            title = dto.title,
            description = dto.description,
            priority = try {
                TaskPriority.valueOf(dto.priority)
            } catch (e: Exception) { existing.priority },
            status = try {
                TaskStatus.valueOf(dto.status)
            } catch (e: Exception) { existing.status }
        )

        return repository.update(updatedTask)
    }

    suspend fun delete(taskId: Int, userId: Int): Boolean {
        val existing = repository.findById(taskId) ?: return false
        if (existing.userId != userId) {
            return false
        }

        return repository.delete(taskId)
    }

    private fun Task.toDto() = TaskDto(
        id = this.id,
        title = this.title,
        description = this.description,
        priority = this.priority.name,
        status = this.status.name,
        userId = this.userId
    )

}
