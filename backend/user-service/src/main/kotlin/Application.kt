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

data class PubSubConfig(val projectId: String, val topicUserRegistered: String, val topicUserDeleted: String)

fun Application.resolvePubSubConfig(): PubSubConfig {
    val projectId = System.getenv("PUBSUB_PROJECTID")
        ?: environment.config.propertyOrNull("pubsub.projectId")?.getString()
        ?: error("pubsub.projectId is not configured. Set PUBSUB_PROJECTID env var or pubsub.projectId in application.yaml")
    val topicUserRegistered = System.getenv("PUBSUB_TOPIC_USER_REGISTERED")
        ?: environment.config.propertyOrNull("pubsub.topics.userRegistered")?.getString()
        ?: error("pubsub.topics.userRegistered is not configured. Set PUBSUB_TOPIC_USER_REGISTERED env var or pubsub.topics.userRegistered in application.yaml")
    val topicUserDeleted = System.getenv("PUBSUB_TOPIC_USER_DELETED")
        ?: environment.config.propertyOrNull("pubsub.topics.userDeleted")?.getString()
        ?: error("pubsub.topics.userDeleted is not configured. Set PUBSUB_TOPIC_USER_DELETED env var or pubsub.topics.userDeleted in application.yaml")
    return PubSubConfig(projectId, topicUserRegistered, topicUserDeleted)
}

fun Application.module() {
    val pubSubConfig = resolvePubSubConfig()

    configureSerialization()
    configureDatabases()
    configureValidation()
    configurePubSub(pubSubConfig)
    configureRouting(pubSubConfig)
}

fun Application.configurePubSub(config: PubSubConfig) {
    PubSubInitializer.ensureTopicExists(config.projectId, config.topicUserRegistered, config.topicUserDeleted)
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
