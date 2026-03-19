package com.example.com.example.task.interfaces.api

import com.example.com.example.task.application.service.NotionExportService
import com.example.com.example.task.domain.infrastracture.cloudtasks.NotionExportPayload
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("InternalRoutes")

fun Route.internalRoutes(notionExportService: NotionExportService) {
    route("/internal") {
        post("/notion-export") {
            val queueHeader = call.request.headers["X-CloudTasks-QueueName"]
            if (queueHeader == null) {
                logger.warn("Rejected /internal/notion-export: missing X-CloudTasks-QueueName header")
                call.respond(HttpStatusCode.Forbidden, mapOf("message" to "Forbidden"))
                return@post
            }

            try {
                val payload = call.receive<NotionExportPayload>()
                notionExportService.executeExport(payload.userId)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Notion export completed"))
            } catch (e: Exception) {
                logger.error("Failed to execute notion export", e)
                call.respond(HttpStatusCode.InternalServerError, mapOf("message" to "Export failed"))
            }
        }
    }
}
