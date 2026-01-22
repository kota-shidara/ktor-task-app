package com.example.com.example.user.application.plugins

import com.example.com.example.user.application.dto.RegisterRequest
import io.ktor.server.plugins.requestvalidation.RequestValidationConfig
import io.ktor.server.plugins.requestvalidation.ValidationResult

fun RequestValidationConfig.configureUserValidation() {
    validate<RegisterRequest> { request ->
        val reasons = mutableListOf<String>()
        if (request.name.isBlank()) {
            reasons.add("nameを入力してください。")
        } else if (request.name.length < 3) {
            reasons.add("nameは3文字以上にしてください")
        }

        if (request.email.isBlank()) {
            reasons.add("Email cannot be blank")
        } else if (!request.email.contains("@")) {
            reasons.add("Email must be valid")
        }

        if (request.password.isBlank()) {
            reasons.add("Password cannot be blank")
        } else if (request.password.length < 6) {
            reasons.add("Password must be at least 6 characters long")
        }

        if (reasons.isNotEmpty()) {
            ValidationResult.Invalid(reasons)
        } else {
            ValidationResult.Valid
        }
    }
}