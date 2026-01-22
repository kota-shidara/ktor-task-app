package com.example.com.example.task.domain.infrastracture.persistence

import com.example.com.example.task.domain.model.TaskPriority
import com.example.com.example.task.domain.model.TaskStatus
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object TaskTable : IntIdTable("tasks") {
    val title = varchar("title", 128)
    val description = text("description").nullable()
    val priority = enumerationByName("priority", 20, TaskPriority::class).default(TaskPriority.MEDIUM)
    val status = enumerationByName("status", 50, TaskStatus::class).default(TaskStatus.PENDING)
    val userId = integer("user_id")
}

class TaskEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TaskEntity>(TaskTable)

    var title by TaskTable.title
    var description by TaskTable.description
    var priority by TaskTable.priority
    var status by TaskTable.status
    var userId by TaskTable.userId
}