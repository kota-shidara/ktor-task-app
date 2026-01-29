package com.example

import com.example.com.example.user.application.plugins.configureUserValidation
import com.example.com.example.user.pubsub.PubSubInitializer
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureValidation()
    configurePubSub()
    configureRouting()
}

fun Application.configurePubSub() {
    val projectId = environment.config.propertyOrNull("pubsub.projectId")?.getString() ?: "local-project"
    val topicId = environment.config.propertyOrNull("pubsub.topicId")?.getString() ?: "user-deleted"
    PubSubInitializer.ensureTopicExists(projectId, topicId)
}

fun Application.configureValidation() {
    install(RequestValidation) {
        configureUserValidation()
    }
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }
    }
}
