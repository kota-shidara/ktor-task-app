package com.example.com.example.task.application.service

import com.example.com.example.task.domain.infrastracture.cloudtasks.TaskEnqueuer
import com.example.com.example.task.domain.infrastracture.notion.NotionExporter
import com.example.com.example.task.domain.model.TaskStatus
import com.example.com.example.task.domain.repository.TaskRepository
import org.slf4j.LoggerFactory

class NotionExportService(
    private val taskEnqueuer: TaskEnqueuer,
    private val notionExporter: NotionExporter,
    private val taskRepository: TaskRepository
) {

    private val logger = LoggerFactory.getLogger(NotionExportService::class.java)

    fun requestExport(userId: Int) {
        logger.info("Requesting Notion export for userId=$userId")
        taskEnqueuer.enqueueNotionExport(userId)
    }

    suspend fun executeExport(userId: Int) {
        logger.info("Executing Notion export for userId=$userId")
        val tasks = taskRepository.findAllByUserId(userId)
        val incompleteTasks = tasks.filter { it.status != TaskStatus.COMPLETED }
        logger.info("Found ${incompleteTasks.size} incomplete tasks for userId=$userId")
        notionExporter.exportTasks(incompleteTasks)
    }
}
