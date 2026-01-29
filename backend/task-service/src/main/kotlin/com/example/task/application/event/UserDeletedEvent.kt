package com.example.com.example.task.application.event

import kotlinx.serialization.Serializable

@Serializable
data class UserDeletedEvent(
    val userId: Int,
    val email: String
)
