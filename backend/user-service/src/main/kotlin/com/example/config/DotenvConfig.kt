package com.example.config

import io.github.cdimascio.dotenv.Dotenv

object DotenvConfig {
    private val dotenv: Dotenv = Dotenv.configure()
        .ignoreIfMissing()
        .load()

    fun get(key: String): String? =
        System.getenv(key) ?: dotenv[key]
}
