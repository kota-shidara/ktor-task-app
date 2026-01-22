package com.example

import com.example.com.example.task.application.service.TaskService
import com.example.com.example.task.domain.infrastracture.repository.ExposedTaskRepository
import com.example.com.example.task.interfaces.api.taskRoute
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection
import java.sql.DriverManager
import org.jetbrains.exposed.sql.*

fun Application.configureRouting() {
    routing {
        val repository = ExposedTaskRepository()
        val taskService = TaskService(repository)

        taskRoute(taskService)
    }
}
