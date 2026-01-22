package com.example

import com.example.com.example.user.application.service.UserService
import com.example.com.example.user.domain.infrastracture.repository.ExposedUserRepository
import com.example.com.example.user.interfaces.api.userRoute
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        val repository = ExposedUserRepository()
        val userService = UserService(repository)

        userRoute(userService)

        route("/v1") {
            get("/test") {
                call.respondText("Hello World 2!")
            }
        }
    }
}
