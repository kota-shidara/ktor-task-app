package com.example

import com.example.com.example.task.application.service.NotionExportService
import com.example.com.example.task.application.service.TaskService
import com.example.com.example.task.interfaces.api.internalRoutes
import com.example.com.example.task.interfaces.api.taskRoute
import com.example.mock.MockNotionExporter
import com.example.mock.MockNotionExportEnqueuer
import com.example.mock.MockTaskRepository
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    private fun ApplicationTestBuilder.setupRouting(): Pair<TaskService, NotionExportService> {
        val repository = MockTaskRepository()
        val taskService = TaskService(repository)
        val notionExportService = NotionExportService(MockNotionExportEnqueuer(), MockNotionExporter(), repository)

        install(ContentNegotiation) {
            json()
        }
        routing {
            taskRoute(taskService, notionExportService)
            internalRoutes(notionExportService)
        }

        return taskService to notionExportService
    }

    @Test
    fun testTasksEndpointReturnsUnauthorizedWithoutAuthHeader() = testApplication {
        setupRouting()
        client.get("/tasks").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testTasksEndpointReturnsOkWithValidAuthHeader() = testApplication {
        setupRouting()
        client.get("/tasks") {
            header("X-User-Authorization", "Bearer dummy-token-1")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

    @Test
    fun testExportNotionReturnsUnauthorizedWithoutAuthHeader() = testApplication {
        setupRouting()
        client.post("/tasks/export-notion").apply {
            assertEquals(HttpStatusCode.Unauthorized, status)
        }
    }

    @Test
    fun testExportNotionReturnsAcceptedWithValidAuthHeader() = testApplication {
        setupRouting()
        client.post("/tasks/export-notion") {
            header("X-User-Authorization", "Bearer dummy-token-1")
        }.apply {
            assertEquals(HttpStatusCode.Accepted, status)
        }
    }

    @Test
    fun testInternalNotionExportReturnsForbiddenWithoutCloudTasksHeader() = testApplication {
        setupRouting()
        client.post("/internal/notion-export") {
            contentType(ContentType.Application.Json)
            setBody("""{"userId": 1}""")
        }.apply {
            assertEquals(HttpStatusCode.Forbidden, status)
        }
    }

    @Test
    fun testInternalNotionExportReturnsOkWithCloudTasksHeader() = testApplication {
        setupRouting()
        client.post("/internal/notion-export") {
            contentType(ContentType.Application.Json)
            header("X-CloudTasks-QueueName", "test-queue")
            setBody("""{"userId": 1}""")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
