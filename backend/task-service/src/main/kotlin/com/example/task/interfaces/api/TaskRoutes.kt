package com.example.com.example.task.interfaces.api

import com.example.com.example.task.application.dto.TaskDto
import com.example.com.example.task.application.service.TaskService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.taskRoute(taskService: TaskService) {
    route("/tasks") {
        get {
            val authHeader = call.request.headers["X-User-Authorization"]
            val userId = authHeader?.removePrefix("Bearer dummy-token-")?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val tasks = taskService.getTasksForUser(userId)
            call.respond(tasks)
        }

        post {
            val authHeader = call.request.headers["X-User-Authorization"]
            val userId = authHeader?.removePrefix("Bearer dummy-token-")?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val dto = call.receive<TaskDto>()
            try {
                val res = taskService.create(dto, userId)
                call.respond(HttpStatusCode.Created, res)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "タスクが作成できませんでした。")
            }
        }

        put("/{id}") {
            val authHeader = call.request.headers["X-User-Authorization"]
            val userId = authHeader?.removePrefix("Bearer dummy-token-")?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@put
            }

            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }

            val dto = call.receive<TaskDto>()
            val result = taskService.update(id, dto, userId)

            if (result) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete("/{id}") {
            val authHeader = call.request.headers["X-User-Authorization"]
            val userId = authHeader?.removePrefix("Bearer dummy-token-")?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }

            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            val result = taskService.delete(id, userId)
            if (result) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
