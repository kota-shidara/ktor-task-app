package com.example.com.example.user.domain.model

data class User(
    val id: Int? = null,
    val email: String,
    val name: String,
    val password: String
) {
    init {
        require(name.isNotBlank()) { "nameを入力してください" }
        require(name.length >= 3) { "nameは3文字以上にしてください" }
        require(email.contains("@")) { "正しいemailを入力してください" }
    }
}
