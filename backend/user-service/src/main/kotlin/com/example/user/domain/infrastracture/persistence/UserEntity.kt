package com.example.com.example.user.domain.infrastracture.persistence

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("users") {
    val name = varchar("name", 128)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 64)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UserTable)

    var name by UserTable.name
    var email by UserTable.email
    var password by UserTable.password
}