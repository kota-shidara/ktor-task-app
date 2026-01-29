package com.example.com.example.task.application.service

import com.example.com.example.task.application.dto.TaskDto
import com.example.mock.MockTaskRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TaskServiceTest {

    private val mockRepository = MockTaskRepository()
    private val taskService = TaskService(mockRepository)

    @Test
    fun `deleteAllTasksForUser should delete all tasks for given user`() = runBlocking {
        taskService.create(TaskDto(title = "Task 1", description = "desc", priority = "HIGH", status = "PENDING"), userId = 1)
        taskService.create(TaskDto(title = "Task 2", description = "desc", priority = "LOW", status = "PENDING"), userId = 1)
        taskService.create(TaskDto(title = "Task 3", description = "desc", priority = "MEDIUM", status = "PENDING"), userId = 2)

        val deletedCount = taskService.deleteAllTasksForUser(1)

        assertEquals(2, deletedCount)
        assertTrue(taskService.getTasksForUser(1).isEmpty())
        assertEquals(1, taskService.getTasksForUser(2).size)
    }

    @Test
    fun `deleteAllTasksForUser should return 0 when user has no tasks`() = runBlocking {
        val deletedCount = taskService.deleteAllTasksForUser(999)

        assertEquals(0, deletedCount)
    }
}
