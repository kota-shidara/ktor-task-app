package com.example

import com.example.com.example.task.application.service.TaskService
import com.example.com.example.task.interfaces.api.taskRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(taskService: TaskService) {
    routing {
        taskRoute(taskService)
    }
}
