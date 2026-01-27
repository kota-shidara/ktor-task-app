package com.example

import com.example.com.example.task.application.service.TaskService
import com.example.com.example.task.interfaces.api.taskRoute
import com.example.mock.MockTaskRepository
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testTasksEndpointReturnsUnauthorizedWithoutAuthHeader() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json()
        }
        routing {
            val repository = MockTaskRepository()
            val taskService = TaskService(repository)
            taskRoute(taskService)
        }
        client.get("/tasks").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testTasksEndpointReturnsOkWithValidAuthHeader() = testApplication {
        install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
            json()
        }
        routing {
            val repository = MockTaskRepository()
            val taskService = TaskService(repository)
            taskRoute(taskService)
        }
        client.get("/tasks") {
            header("X-User-Authorization", "Bearer dummy-token-1")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
