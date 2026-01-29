package com.example

import com.example.com.example.user.application.service.UserService
import com.example.com.example.user.domain.infrastracture.messaging.PubSubUserEventPublisher
import com.example.com.example.user.domain.infrastracture.repository.ExposedUserRepository
import com.example.com.example.user.interfaces.api.userRoute
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val projectId = environment.config.propertyOrNull("pubsub.projectId")?.getString() ?: "local-project"
    val topicId = environment.config.propertyOrNull("pubsub.topicId")?.getString() ?: "user-deleted"

    routing {
        val repository = ExposedUserRepository()
        val eventPublisher = PubSubUserEventPublisher(projectId, topicId)
        val userService = UserService(repository, eventPublisher)

        userRoute(userService)

        route("/v1") {
            get("/test") {
                call.respondText("Hello World 2!")
            }
        }
    }
}
