package com.example.com.example.task.application.service

import com.example.com.example.task.application.dto.TaskDto
import com.example.mock.MockNotionExporter
import com.example.mock.MockNotionExportEnqueuer
import com.example.mock.MockTaskRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotionExportServiceTest {

    private val mockRepository = MockTaskRepository()
    private val mockEnqueuer = MockNotionExportEnqueuer()
    private val mockExporter = MockNotionExporter()
    private val taskService = TaskService(mockRepository)
    private val notionExportService = NotionExportService(mockEnqueuer, mockExporter, mockRepository)

    @Test
    fun `requestExport should enqueue a task for the given userId`() {
        notionExportService.requestExport(1)

        assertEquals(1, mockEnqueuer.enqueuedUserIds.size)
        assertEquals(1, mockEnqueuer.enqueuedUserIds[0])
    }

    @Test
    fun `executeExport should export only incomplete tasks`() = runBlocking {
        taskService.create(TaskDto(title = "Pending Task", description = "desc", priority = "HIGH", status = "PENDING"), userId = 1)
        taskService.create(TaskDto(title = "In Progress Task", description = "desc", priority = "MEDIUM", status = "PENDING"), userId = 1)

        val completedDto = taskService.create(TaskDto(title = "Completed Task", description = "desc", priority = "LOW", status = "PENDING"), userId = 1)
        taskService.update(completedDto.id!!, TaskDto(id = completedDto.id, title = "Completed Task", description = "desc", priority = "LOW", status = "COMPLETED"), userId = 1)

        notionExportService.executeExport(1)

        assertEquals(1, mockExporter.exportedTasks.size)
        val exported = mockExporter.exportedTasks[0]
        assertEquals(2, exported.size)
        assertTrue(exported.all { it.status.name != "COMPLETED" })
    }

    @Test
    fun `executeExport should export empty list when all tasks are completed`() = runBlocking {
        val dto = taskService.create(TaskDto(title = "Task", description = "desc", priority = "HIGH", status = "PENDING"), userId = 2)
        taskService.update(dto.id!!, TaskDto(id = dto.id, title = "Task", description = "desc", priority = "HIGH", status = "COMPLETED"), userId = 2)

        notionExportService.executeExport(2)

        assertEquals(1, mockExporter.exportedTasks.size)
        assertTrue(mockExporter.exportedTasks[0].isEmpty())
    }
}
