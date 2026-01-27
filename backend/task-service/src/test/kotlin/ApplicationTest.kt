package com.example

import com.example.com.example.task.application.service.TaskService
import com.example.com.example.task.domain.model.Task
import com.example.com.example.task.domain.model.TaskPriority
import com.example.com.example.task.domain.model.TaskStatus
import com.example.com.example.task.domain.repository.TaskRepository
import com.example.com.example.task.interfaces.api.taskRoute
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class MockTaskRepository : TaskRepository {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    override suspend fun findById(id: Int): Task? = tasks.find { it.id == id }

    override suspend fun findAllByUserId(userId: Int): List<Task> = tasks.filter { it.userId == userId }

    override suspend fun create(task: Task): Task {
        val newTask = task.copy(id = nextId++)
        tasks.add(newTask)
        return newTask
    }

    override suspend fun update(task: Task): Boolean {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index == -1) return false
        tasks[index] = task
        return true
    }

    override suspend fun delete(id: Int): Boolean {
        return tasks.removeIf { it.id == id }
    }
}

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
