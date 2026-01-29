package com.example.com.example.user.interfaces.api

import com.example.com.example.user.application.dto.LoginRequest
import com.example.com.example.user.application.dto.RegisterRequest
import com.example.com.example.user.application.service.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.userRoute(userService: UserService) {
    route("/register") {
        post {
            val request = call.receive<RegisterRequest>()
            try {
                val res = userService.register(request)
                call.respond(HttpStatusCode.Created, res)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "登録に失敗しました")
            }
        }
    }

    route("/login") {
        post {
            val request = call.receive<LoginRequest>()
            val res = userService.login(request)
            if (res != null) {
                call.respond(HttpStatusCode.OK, res)
            } else {
                call.respond(HttpStatusCode.Unauthorized, "認証情報が正しくありません")
            }
        }
    }

    route("/users/me") {
        delete {
            val authHeader = call.request.headers["X-User-Authorization"]
            val userId = authHeader?.removePrefix("Bearer dummy-token-")?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@delete
            }

            val deleted = userService.deleteUser(userId)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "ユーザーが見つかりません")
            }
        }
    }
}