package com.example.com.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driverClassName = System.getenv("STORAGE_DRIVER_CLASSNAME")
            ?: config.property("storage.driverClassName").getString()
        val jdbcURL = System.getenv("STORAGE_JDBCURL")
            ?: config.property("storage.jdbcURL").getString()
        val user = System.getenv("STORAGE_USER")
            ?: config.property("storage.user").getString()
        val password = System.getenv("STORAGE_PASSWORD")
            ?: config.property("storage.password").getString()

        runFlyway(jdbcURL, user, password)
        val database = Database.connect(hikari(driverClassName, jdbcURL, user, password))
    }

    private fun runFlyway(url: String, user: String, pass: String) {
        val flyway = Flyway.configure()
            .dataSource(url, user, pass)
            .baselineOnMigrate(true)
            .load()
        flyway.migrate()
    }

    private fun hikari(driver: String, url: String, user: String, pass: String): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = driver
        config.jdbcUrl = url
        config.username = user
        config.password = pass
        config.maximumPoolSize = 3
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        config.validate()
        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend Transaction.() -> T) =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
