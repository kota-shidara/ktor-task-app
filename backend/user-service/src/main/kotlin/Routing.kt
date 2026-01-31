package com.example

import com.example.com.example.user.application.service.UserService
import com.example.com.example.user.domain.infrastracture.messaging.PubSubUserEventPublisher
import com.example.com.example.user.domain.infrastracture.repository.ExposedUserRepository
import com.example.com.example.user.interfaces.api.userRoute
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(pubSubConfig: PubSubConfig) {
    routing {
        val repository = ExposedUserRepository()
        val eventPublisher = PubSubUserEventPublisher(pubSubConfig.projectId, pubSubConfig.topicUserDeleted)
        val userService = UserService(repository, eventPublisher)

        userRoute(userService)

        route("/v1") {
            get("/test") {
                call.respondText("Hello World 2!")
            }
        }
    }
}
