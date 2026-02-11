package com.example

import com.example.com.example.task.application.service.NotionExportService
import com.example.com.example.task.application.service.TaskService
import com.example.com.example.task.interfaces.api.internalRoutes
import com.example.com.example.task.interfaces.api.taskRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(taskService: TaskService, notionExportService: NotionExportService) {
    routing {
        taskRoute(taskService, notionExportService)
        internalRoutes(notionExportService)
    }
}
