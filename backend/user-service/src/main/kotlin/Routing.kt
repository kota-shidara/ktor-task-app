package com.example

import com.example.com.example.user.application.event.EventPublisher
import com.example.com.example.user.application.event.NoOpEventPublisher
import com.example.com.example.user.application.service.UserService
import com.example.com.example.user.domain.infrastracture.messaging.PubSubEventPublisher
import com.example.com.example.user.domain.infrastracture.repository.ExposedUserRepository
import com.example.com.example.user.interfaces.api.userRoute
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        val repository = ExposedUserRepository()
        val eventPublisher = createEventPublisher()
        val userService = UserService(repository, eventPublisher)

        userRoute(userService)

        route("/v1") {
            get("/test") {
                call.respondText("Hello World 2!")
            }
        }
    }
}

private fun Application.createEventPublisher(): EventPublisher {
    val projectId = System.getenv("GCP_PROJECT_ID")
        ?: environment.config.propertyOrNull("pubsub.projectId")?.getString()
    val topicId = System.getenv("PUBSUB_TOPIC_USER_EVENTS")
        ?: environment.config.propertyOrNull("pubsub.topicId")?.getString()

    return if (projectId != null && topicId != null) {
        log.info("Pub/Sub publisher enabled: project={}, topic={}", projectId, topicId)
        PubSubEventPublisher(projectId, topicId)
    } else {
        log.info("Pub/Sub publisher disabled: missing projectId or topicId")
        NoOpEventPublisher()
    }
}
